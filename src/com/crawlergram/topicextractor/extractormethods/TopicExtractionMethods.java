/*
 * Title: TopicExtractionMethods.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicextractor.extractormethods;

import com.crawlergram.db.DBStorageReduced;
import com.crawlergram.topicextractor.gras.GRAS;
import com.crawlergram.topicextractor.ldadmm.models.GSDMM;
import com.crawlergram.topicextractor.ldadmm.models.GSLDA;
import com.crawlergram.topicextractor.liga.LIGA;
import com.crawlergram.topicextractor.structures.TEDialog;
import com.crawlergram.topicextractor.structures.message.TEMessage;
import com.crawlergram.topicextractor.structures.results.TEResults;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

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
        List<TEDialog> dialogs = dbStorage.getDialogs();
        if ((dialogs != null) && (!dialogs.isEmpty())) {
            for (TEDialog dialog : dialogs) {
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
     */
    public static void getTopicsForOneDialog(DBStorageReduced dbStorage, TEDialog dialog, int dateFrom, int dateTo,
                                             int docThreshold, boolean msgMerging, Object lang,
                                             Map<String, Set<String>> stopwords) {
        List<TEMessage> msgs;
        // if dates valid - get only messages between these dates, otherwise - get all messages
        if (datesCheck(dateFrom, dateTo)) {
            msgs = dbStorage.readMessages(dialog, dateFrom, dateTo);
        } else {
            msgs = dbStorage.readMessages(dialog);
        }
        // check if resulting list is not empty
        if ((msgs != null) && !msgs.isEmpty()) {
            if (msgMerging) msgs = MessageMergingMethods.mergeMessages(dialog, msgs, docThreshold);
            removeEmptyMessages(msgs);
            msgs = Tokenizer.tokenizeMessages(msgs);

            getMessageLanguages(msgs, lang);

            String bestLang = getDialogsBestLang(msgs, 0.8);
            //getLangStats(msgs);

            removeStopWords(msgs, stopwords, bestLang, 0.9);

            Map<String, String> uniqueWords = getUniqueWords(msgs);
            uniqueWords = GRAS.doStemming(uniqueWords, 5, 4, 0.8);
            getTextFromStems(msgs, uniqueWords);

            GSDMM dmm = new GSDMM(msgs, 10, 0.1, 0.1, 1000, 10);
            TEResults resDMM = dmm.inference();

            GSLDA lda = new GSLDA(msgs, 10, 0.01, 0.1, 1000, 10);
            TEResults resLDA = lda.inference();

            //print some stats
            statUtils(msgs, uniqueWords);

            printTopWords(resDMM);
            printTopWords(resLDA);

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

    /**
     * if dates are wrong (from > to) or both dates equal zero -> false
     *
     * @param dateFrom date from
     * @param dateTo   date to
     */
    private static boolean datesCheck(int dateFrom, int dateTo) {
        return ((dateFrom != 0) || (dateTo != 0)) && dateFrom < dateTo;
    }

    /**
     * returns a sorted list (set) of sorted compounds of tokens
     *
     * @param msgs original msgs object (with calculated simple and compound tokens)
     */
    private static Map<String, String> getUniqueWords(List<TEMessage> msgs) {
        Map<String, String> uniqueWords = new TreeMap<>();
        for (TEMessage msg : msgs) {
            for (String token : msg.getTokens()) {
                if (!uniqueWords.containsKey(token)) uniqueWords.put(token, null);
            }
        }
        return uniqueWords;
    }

    /**
     * Creates a message consisting of stems of original words
     *
     * @param msgs        messages
     * @param uniqueWords unique words
     */
    private static void getTextFromStems(List<TEMessage> msgs, Map<String, String> uniqueWords) {
        for (TEMessage msg : msgs) {
            StringBuilder stemmedText = new StringBuilder();
            List<String> tokens = msg.getTokens();
            for (String token : tokens) {
                stemmedText.append(uniqueWords.get(token)).append(" ");
            }
            msg.setStemmedText(stemmedText.toString().trim());
        }
    }

    /**
     * Removes empty messages from the list
     *
     * @param msgs messages list
     */
    private static void removeEmptyMessages(List<TEMessage> msgs) {
        for (int i = 0; i < msgs.size(); i++) {
            if (msgs.get(i).getText().isEmpty()) {
                msgs.remove(i);
            }
        }
    }

    /**
     * Indentifies languages for each message
     *
     * @param msgs messages
     * @param lang language identification model
     */
    private static void getMessageLanguages(List<TEMessage> msgs, Object lang){
        if (lang instanceof LIGA){
            for (TEMessage msg: msgs)
                msg.setLangs(((LIGA) lang).classify(msg.getClearText()));
        }
        if ((lang instanceof LanguageDetector)){
            for (TEMessage msg: msgs){
                Map<String, Double> langs = new TreeMap<>();
                LanguageResult result = ((LanguageDetector) lang).detect(msg.getClearText());
                if (!result.isUnknown()){
                    langs.put(result.getLanguage(), (double) result.getRawScore());
                }
            }
        }

    }

    /**
     * Removes stopwords from token compounds. If chat has a dominating language - additional chack
     *
     * @param msgs messages
     * @param stopwords stopwords
     * @param bestLang best language of dialog
     */
    private static void removeStopWords(List<TEMessage> msgs, Map<String, Set<String>> stopwords,
                                        String bestLang, double langsRatio) {
        // loads best lang
        if (!bestLang.equals("UNKNOWN") && !stopwords.containsKey(bestLang))
            stopwords.put(bestLang, loadStopWords(bestLang));
        // checks msgs
        for (TEMessage msg : msgs) {
            String lang = msg.getBestLang();
            // load stopwords for "lang" if only they're not loaded before
            if (!stopwords.containsKey(lang))
                stopwords.put(lang, loadStopWords(lang));            
            List<String> tokens = msg.getTokens();
            boolean flag = false;
            for (int j = 0; j < tokens.size(); j++) {                
                if (stopwords.get(lang).contains(tokens.get(j)))
                    flag = true;
                // additionally check if LI doubts (if lang != bestLang and PbestLang/Plang > langsRatio)
                if (msg.getLangs().containsKey(bestLang))
                    if (!lang.equals(bestLang) && (msg.getLangs().get(lang)/msg.getLangs().get(bestLang) > langsRatio)
                            && stopwords.get(bestLang).contains(tokens.get(j)))
                        flag = true;
                if (flag){
                    msg.getTokens().remove(j);
                    j--;
                    flag = false;
                }                                
                    
            }
        }
    }

    /**
     * loads stop words from a file to the set
     *
     * @param language language code (i.e. en, es, de, ru etc.)
     */
    private static Set<String> loadStopWords(String language) {
        Set<String> stopWords = new TreeSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("res" + File.separator + "stopwords" + File.separator + language.toLowerCase() + ".txt"))) {
            for (String doc; (doc = br.readLine()) != null; )
                if (!doc.trim().isEmpty()) stopWords.add(doc.trim());
        } catch (IOException e) {
            System.out.println("Can't read stopwords for " + language.toUpperCase() + " language");
        }
        return stopWords;
    }

    /**
     * Returns the language if it's the best language in the most of the cases
     *
     * @param msgs messages
     * @param threshhold min threshold of popularity
     */
    private static String getDialogsBestLang(List<TEMessage> msgs, double threshhold){
        Integer totalCount = 0;
        Integer bestCount = -1;
        String bestLang = "UNKNOWN";
        Map<String, Integer> langsCounts = new HashMap<>();
        for (TEMessage msg: msgs){
            String bestlang = msg.getBestLang();
            if (!langsCounts.containsKey(bestlang)) langsCounts.put(bestlang, 0);
            langsCounts.put(bestlang, langsCounts.get(bestlang) + 1);
        }
        for (Map.Entry<String, Integer> count : langsCounts.entrySet()){
            if (count.getValue() > bestCount) {
                bestCount = count.getValue();
                bestLang = count.getKey();
            }
            totalCount += count.getValue();
        }
        if ((totalCount > 0) && ((bestCount / totalCount) >= threshhold)){
            return bestLang;
        } else {
            return "UNKNOWN";
        }
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

    private static void printTopWords(TEResults res) {
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
