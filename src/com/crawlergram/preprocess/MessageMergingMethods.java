/*
 * Title: MessageMergingMethods.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocess;

import com.crawlergram.preprocessing.gaussnewton.ExpRegMethods;
import com.crawlergram.preprocessing.gaussnewton.GaussNewton;
import com.crawlergram.preprocessing.gaussnewton.NoSquareException;
import com.crawlergram.structures.TDialog;
import com.crawlergram.structures.TMessage;
import com.crawlergram.structures.message_old.TMessageComparator;

import java.util.*;

public class MessageMergingMethods {

    /**
     * Merges short messages of supergroups and chats to larger messages (merges by topic).
     * We assume that topic can be defined from time of respond.
     * If two messages were written in a short time interval they are likely to have the same context or topic.
     * Such messages can be used to define most popular topics of the chat (e.g. sport, news, etc.).
     * We need to merge only messages from Chats, supergroups (type of Channels) and Users.
     *
     * @param dialog       dialog
     * @param msgs         original messages list
     * @param docThreshold if chat has very low number of messages (< docThreshold) -> all chat is merged
     */
    public static List<TMessage> mergeMessages(TDialog dialog,
                                                List<TMessage> msgs,
                                                int docThreshold) {
        // merging if chat, supergroup or user (placer, where subscribers write something)
        // channels (not supergroups) usually are blogs, subscribers can't post there
        // if flags' 9th bit is "1" - channel is supergroup (0001 0000 0000 = 256d)
        if (!(dialog.getType().equals("Channel") && ((dialog.getFlags() & 256) == 0) && (msgs.size() > 0))) {
            msgs = mergeChat(msgs, docThreshold);
        }
        return msgs;
    }

    /**
     * merges (long snd short) chats to docs
     *
     * @param messages     messages
     * @param docThreshold threshold
     */
    private static List<TMessage> mergeChat(List<TMessage> messages, int docThreshold) {
        // if number of messages < docThreshold - short chat, else - long chat
        return (messages.size() < docThreshold) ? mergeShortChat(messages) : mergeLongChat(messages);
    }

    /**
     * merges short chat messages (number of messages < threshold) to one message_old
     *
     * @param messages all messages
     */
    private static List<TMessage> mergeShortChat(List<TMessage> messages) {
        List<TMessage> merged = new LinkedList<>();
        String text = "";
        for (TMessage message : messages) {
            if (!message.getText().isEmpty()) {
                text += message.getText() + "\n";
            }
        }
        if (!text.isEmpty()) {
            // id and date of last message_old are taken
            TMessage first = messages.get(0);
            merged.add(new TMessage(first.getId(), text, first.getDate()));
        }
        return merged;
    }

    /**
     * merges long chat messages (number of messages > threshold) if they fit time interval
     *
     * @param messages "clean" messages (without empty and service messages)
     */
    private static List<TMessage> mergeLongChat(List<TMessage> messages) {
        Collections.sort(messages, new TMessageComparator());
        // get intervals between messages to array
        List<Integer> dates = new ArrayList<>();
        for (TMessage message : messages) {
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
        return mergeByTime(messages, timeThreshold);
    }

    /**
     * concatenates messages to one, if delta time between them is lower than time threshold
     *
     * @param messages      documents
     * @param timeThreshold maximum time between messages in one doc
     */
    private static List<TMessage> mergeByTime(List<TMessage> messages, int timeThreshold) {
        if (timeThreshold <= 0) {
            return mergeShortChat(messages);
        } else {
            List<TMessage> mesCopy = new LinkedList<>(messages);
            for (int i = 0; i < mesCopy.size() - 1; i++) {
                // date of the current and next documents
                TMessage d0 = mesCopy.get(i);
                TMessage d1 = mesCopy.get(i + 1);
                // threshold criterion
                if (d0.getDate() - d1.getDate() <= timeThreshold) {
                    mesCopy.set(i, new TMessage(d0.getId(), d0.getText() + "\n" + d1.getText(), d0.getDate()));
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
            if (msgs.get(i).getText().isEmpty()) {
                msgs.remove(i);
            }
        }
        return msgs;
    }

}
