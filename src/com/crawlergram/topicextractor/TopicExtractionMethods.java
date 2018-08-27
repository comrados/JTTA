/*
 * Title: TopicExtractionMethods.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicextractor;

import com.crawlergram.db.DBStorageReduced;
import com.crawlergram.preprocess.MessageMergingMethods;
import com.crawlergram.preprocess.Tokenizer;
import com.crawlergram.preprocess.UtilMethods;
import com.crawlergram.preprocessing.gras.GRAS;
import com.crawlergram.structures.TMessage;
import com.crawlergram.topicmodeling.ldadmm.models.GSDMM;
import com.crawlergram.topicmodeling.ldadmm.models.GSLDA;
import com.crawlergram.structures.TDialog;
import com.crawlergram.structures.message_old.TEMessage;
import com.crawlergram.structures.results.TMResults;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class TopicExtractionMethods {

    /**
     * do topic extraction for each dialog, if dates are wrong (from > to) or both dates equal zero -> read all messages
     *
     * @param dbStorage    db storage implementation
     * @param dateFrom     date from
     * @param dateTo       date to
     * @param docThreshold if chat has very low number of messages (< docThreshold) -> all chat is merged
     * @param msgMerging   if true - artificial documents will be created from messages, preferable for LDA
     * @param lang         language identification model
     * @param stopwords    stopwords map to prevent multiple file readings
     */
    public static void getTopicsForAllDialogs(DBStorageReduced dbStorage, int dateFrom, int dateTo, int docThreshold,
                                              boolean msgMerging, Object lang, Map<String, Set<String>> stopwords) {
        // get all dialogs
        List<TDialog> dialogs = TDialog.telegramDialogsFromDB(dbStorage.getDialogs());
        if ((dialogs != null) && (!dialogs.isEmpty())) {
            for (TDialog dialog : dialogs) {
                // do for one
                getTopicsForOneDialog(dbStorage, dialog, dateFrom, dateTo, docThreshold, msgMerging, lang, stopwords);
            }
        } else {
            System.out.println("NO DIALOGS FOUND");
        }
    }

    /**
     * do topic extraction for a specific dialog, if dates are wrong (from > to) or both dates equal zero -> read all
     *
     * @param dbStorage    db storage implementation
     * @param dialog       dialog
     * @param dateFrom     date from
     * @param dateTo       date to
     * @param docThreshold if chat has very low number of messages (< docThreshold) -> all chat is merged
     * @param msgMerging   if true - artificial documents will be created from messages, preferable for LDA
     * @param lang         language identification model
     * @param stopwords    stopwords map to prevent multiple file readings
     */
    public static void getTopicsForOneDialog(DBStorageReduced dbStorage, TDialog dialog, int dateFrom, int dateTo,
                                             int docThreshold, boolean msgMerging, Object lang,
                                             Map<String, Set<String>> stopwords) {
        List<TEMessage> temsgs;
        List<TMessage> msgs;
        // if dates valid - get only messages between these dates, otherwise - get all messages
        if (UtilMethods.datesCheck(dateFrom, dateTo)) {
            msgs = TMessage.tMessagesFromDB(dbStorage.readMessages(dialog, dateFrom, dateTo));
        } else {
            msgs = TMessage.tMessagesFromDB(dbStorage.readMessages(dialog));
        }
        // check if resulting list is not empty
        if ((msgs != null) && !msgs.isEmpty()) {
            if (msgMerging) msgs = MessageMergingMethods.mergeMessages(dialog, msgs, docThreshold);
            msgs = MessageMergingMethods.removeEmptyMessages(msgs);

            temsgs = initTEMsgs(msgs);
            temsgs = Tokenizer.tokenizeMessages(temsgs);
            UtilMethods.getMessageLanguages(temsgs, lang);

            String bestLang = UtilMethods.getDialogsBestLang(temsgs, 0.8);
            UtilMethods.removeStopWords(temsgs, stopwords, bestLang, 0.9);
            Map<String, String> uniqueWords = UtilMethods.getUniqueWords(temsgs);

            uniqueWords = GRAS.doStemming(uniqueWords, 5, 4, 0.8);
            UtilMethods.getTextFromStems(temsgs, uniqueWords);

            //GSDMM dmm = new GSDMM(temsgs, 10, 0.1, 0.1, 1000, 10);
            //TMResults resDMM = dmm.inference();

            //GSLDA lda = new GSLDA(temsgs, 10, 0.01, 0.1, 1000, 10);
            //TMResults resLDA = lda.inference();

            //print some stats
            statUtils(temsgs, uniqueWords);

            //printTopWords(resDMM);
            //printTopWords(resLDA);

            try {
                saveSet("words.txt", uniqueWords);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //TODO
        } else {
            System.out.println("EMPTY MESSAGES: " + dialog.getId() + " " + dialog.getUsername());
        }
    }

    private static List<TEMessage> initTEMsgs(List<TMessage> msgs){
        List<TEMessage> temsgs = new ArrayList<>();
        for (TMessage msg: msgs){
            temsgs.add(new TEMessage(msg.getId(), msg.getText(), msg.getDate()));
        }
        msgs.clear();
        return temsgs;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private static void statUtils(List<TEMessage> msgs, Map<String, String> uniqueWords) {
        System.out.println();
        System.out.println("Number of documents: " + msgs.size());
        double l = calcL(uniqueWords);
        double tok = calcAv(msgs);
        System.out.println("Number of valid unique words: " + uniqueWords.keySet().size());
        System.out.println("Ratio tokens_in_doc/unique_words : " + String.format("%.2f", tok / uniqueWords.keySet().size() * 100) + " %");
    }


    private static void saveSet(String filename, Map<String, String> words) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, false), Charset.forName("UTF-8")));
        Set<String> keys = words.keySet();
        for (String key : keys) {
            writer.write(key + "\r\n");
        }
        writer.close();
    }

    private static double calcL(Map<String, String> words) {
        Set<String> keys = words.keySet();
        double totalL = 0.0;
        int n = keys.size();
        for (String key : keys) {
            totalL += key.length();
        }
        System.out.println("Average valid word length L: " + String.format("%.2f", totalL / n));
        return totalL / n;
    }

    private static double calcAv(List<TEMessage> msgs) {
        double totalAv = 0.0;
        int n = msgs.size();
        for (TEMessage msg : msgs) {
            totalAv += msg.getTokens().size();
        }
        System.out.println("Valid tokens per document: " + String.format("%.2f", totalAv / n));
        return totalAv / n;
    }

    private static void printTopWords(TMResults res) {
        System.out.println();
        System.out.println();
        System.out.println(res.getParameters().toString());
        for (Map<String, Double> topic : res.getTopTopicalWords()) {
            System.out.println();
            System.out.println("------------------------topic----------------------------");
            topic.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEachOrdered(x -> System.out.println(x.getKey() + " " + x.getValue()));
            System.out.println("------------------------topic end------------------------");
        }
        System.out.println("----------------------------------------------------------");
    }

    private static void getLangStats(List<TEMessage> msgs){
        Map<String, Integer> langsTotal = new HashMap<>();
        Map<String, Integer> langsPop = new HashMap<>();
        for (TEMessage msg: msgs){
            Set<String> langs = msg.getLangs().keySet();
            for (String lang: langs){
                if (!langsTotal.containsKey(lang)) langsTotal.put(lang, 0);
                langsTotal.put(lang, langsTotal.get(lang) + 1);
            }
            String bestlang = msg.getBestLang();
            if (!langsPop.containsKey(bestlang)) langsPop.put(bestlang, 0);
            langsPop.put(bestlang, langsPop.get(bestlang) + 1);
        }
        System.out.println("");
        System.out.println("Best languages: ");
        for (Map.Entry<String, Integer> entry: langsPop.entrySet()){
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        System.out.println("");
        System.out.println("All recognized languages: ");
        for (Map.Entry<String, Integer> entry: langsTotal.entrySet()){
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        System.out.println("");
    }

}
