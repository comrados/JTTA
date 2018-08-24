/*
 * Title: MessageMerger.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing.preprocessor;

import com.crawlergram.preprocessing.TDialog;
import com.crawlergram.preprocessing.TMessage;
import com.crawlergram.preprocessing.gaussnewton.ExpRegMethods;
import com.crawlergram.preprocessing.gaussnewton.GaussNewton;
import com.crawlergram.preprocessing.gaussnewton.NoSquareException;
import com.crawlergram.structures.message.TMessageComparator;

import java.util.*;

public class MessageMerger implements Preprocessor {

    private static int docThreshold; // default - 100 (see MessageMergerBuilder)

    public int getDocThreshold() {
        return docThreshold;
    }

    public void setDocThreshold(int threshold) {
        docThreshold = threshold;
    }

    MessageMerger(MessageMergerBuilder builder){
        docThreshold = builder.docThreshold;
    }


    /**
     * Merges short messages of supergroups and chats to larger messages (merges by topic).
     * We assume that topic can be defined from time of respond.
     * If two messages were written in a short time interval they are likely to have the same context or topic.
     * Such messages can be used to define most popular topics of the chat (e.g. sport, news, etc.).
     * We need to merge only messages from Chats, supergroups (type of Channels) and Users.
     */
    @Override
    public TDialog run(TDialog dialog) {
        // merging if chat, supergroup or user (placer, where subscribers write something)
        // channels (not supergroups) usually are blogs, subscribers can't post there
        // if flags' 9th bit is "1" - channel is supergroup (0001 0000 0000 = 256d)
        if (!(dialog.getType().equals("Channel") && ((dialog.getFlags() & 256) == 0) &&
                (dialog.getMessages().size() > 0))) {
            dialog.setMessages(mergeChat(dialog));
        }
        dialog.setMessages(removeEmptyMessages(dialog.getMessages()));
        return dialog;
    }

    /**
     * merges (long snd short) chats to docs
     */
    private static List<TMessage> mergeChat(TDialog dialog) {
        // if number of messages < docThreshold - short chat, else - long chat
        return (dialog.getMessages().size() < docThreshold) ? mergeShortChat(dialog) : mergeLongChat(dialog);
    }

    /**
     * merges short chat messages (number of messages < threshold) to one message
     */
    private static List<TMessage> mergeShortChat(TDialog dialog) {
        List<TMessage> merged = new LinkedList<>();
        StringBuilder text = new StringBuilder();
        for (TMessage message : dialog.getMessages()) {
            if (!message.getText().isEmpty()) {
                text.append(message.getText()).append("\n");
            }
        }
        if (text.length() > 0) {
            // id and date of last message are taken
            TMessage first = dialog.getMessages().get(0);
            merged.add(new TMessage(first.getId(), text.toString().trim(), first.getDate(), first.getTokens(), first.getLangs()));
        }
        return merged;
    }

    /**
     * merges long chat messages (number of messages > threshold) if they fit time interval
     */
    private static List<TMessage> mergeLongChat(TDialog dialog) {
        Collections.sort(dialog.getMessages(), new TMessageComparator());
        // get intervals between messages to array
        List<Integer> dates = new ArrayList<>();
        for (TMessage message : dialog.getMessages()) {
            dates.add(message.getDate());
        }
        // deltas between intervals
        List<Integer> deltas = ExpRegMethods.countDeltas(dates);
        // unique deltas (sorted tree set)
        Set<Integer> deltasUnique = new TreeSet<>(deltas);
        // unique deltas counts
        List<Integer> deltasUniqueCounts = ExpRegMethods.countUniqueDeltas(deltasUnique, deltas);

        // Gauss-Newton implementation for fitting
        GaussNewton gn = new GaussNewton() {
            @Override
            public double findY(double x, double[] b) {
                return b[0] * Math.exp(-b[1] * x);
            }
        };

        // values initialization and optimisation
        double[] expModelInit = ExpRegMethods.expRegInitValues(ExpRegMethods.setToDoubles(deltasUnique), ExpRegMethods.listToDoubles(deltasUniqueCounts));
        double[] expModel = new double[2];
        try {
            expModel = gn.optimise(ExpRegMethods.setToDoubles2D(deltasUnique), ExpRegMethods.listToDoubles(deltasUniqueCounts), expModelInit);
        } catch (NoSquareException e) {
            System.out.println(e.getMessage());
        }
        int timeThreshold = (int) Math.ceil(ExpRegMethods.mathTimeThresholdCount(expModel[1], 0.01));
        // returns the list of merged documents
        return mergeByTime(dialog, timeThreshold);
    }

    /**
     * concatenates messages to one, if delta time between them is lower than time threshold
     *
     * @param timeThreshold maximum time between messages in one doc
     */
    private static List<TMessage> mergeByTime(TDialog dialog, int timeThreshold) {
        if (timeThreshold <= 0) {
            return mergeShortChat(dialog);
        } else {
            List<TMessage> mesCopy = new LinkedList<>(dialog.getMessages());
            for (int i = 0; i < mesCopy.size() - 1; i++) {
                // date of the current and next documents
                TMessage d0 = mesCopy.get(i);
                TMessage d1 = mesCopy.get(i + 1);
                // threshold criterion
                if (d0.getDate() - d1.getDate() <= timeThreshold) {
                    mesCopy.set(i, new TMessage(d0.getId(), (d0.getText() + "\n" + d1.getText()).trim(), d0.getDate(), d0.getTokens(), d0.getLangs()));
                    mesCopy.remove(i + 1);
                    // returns i back each time, when we have merge of the messages, to check for multiple merges in row
                    i--;
                }
            }
            return mesCopy;
        }
    }

    /**
     * Removes empty messages from the list
     *
     * @param msgs messages list
     */
    public static List<TMessage> removeEmptyMessages(List<TMessage> msgs) {
        for (int i = 0; i < msgs.size(); i++) {
            if ((msgs.get(i).getText() == null) || msgs.get(i).getText().trim().isEmpty()) {
                msgs.remove(i--);
            }
        }
        return msgs;
    }

    public static class MessageMergerBuilder {

        private int docThreshold = 100;

        public MessageMergerBuilder setDocThreshold(int docThreshold) {
            this.docThreshold = docThreshold;
            return this;
        }

        public MessageMergerBuilder() {

        }

        public MessageMerger build() {
            return new MessageMerger(this);
        }
    }

}
