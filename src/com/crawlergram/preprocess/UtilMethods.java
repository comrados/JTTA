/*
 * Title: UtilMethods.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocess;

import com.crawlergram.preprocess.liga.LIGA;
import com.crawlergram.structures.message.TMessage;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class UtilMethods {

    /**
     * if dates are wrong (from > to) or both dates equal zero -> false
     *
     * @param dateFrom date from
     * @param dateTo   date to
     */
    public static boolean datesCheck(int dateFrom, int dateTo) {
        return ((dateFrom != 0) || (dateTo != 0)) && dateFrom < dateTo;
    }

    /**
     * returns a sorted list (set) of sorted compounds of tokens
     *
     * @param msgs original msgs object (with calculated simple and compound tokens)
     */
    public static Map<String, String> getUniqueWords(List<TMessage> msgs) {
        Map<String, String> uniqueWords = new TreeMap<>();
        for (TMessage msg : msgs) {
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
    public static void getTextFromStems(List<TMessage> msgs, Map<String, String> uniqueWords) {
        for (TMessage msg : msgs) {
            StringBuilder stemmedText = new StringBuilder();
            List<String> tokens = msg.getTokens();
            for (String token : tokens) {
                stemmedText.append(uniqueWords.get(token)).append(" ");
            }
            msg.setStemmedText(stemmedText.toString().trim());
        }
    }

    /**
     * Indentifies languages for each message
     *
     * @param msgs messages
     * @param lang language identification model
     */
    public static void getMessageLanguages(List<TMessage> msgs, Object lang) {
        if (lang instanceof LIGA) {
            for (TMessage msg : msgs)
                msg.setLangs(((LIGA) lang).classify(msg.getClearText()));
        }
        if ((lang instanceof LanguageDetector)) {
            for (TMessage msg : msgs) {
                Map<String, Double> langs = new TreeMap<>();
                LanguageResult result = ((LanguageDetector) lang).detect(msg.getClearText());
                if (!result.isUnknown()) {
                    langs.put(result.getLanguage(), (double) result.getRawScore());
                }
            }
        }

    }

    /**
     * Removes stopwords from token compounds. If chat has a dominating language - additional chack
     *
     * @param msgs      messages
     * @param stopwords stopwords
     * @param bestLang  best language of dialog
     */
    public static void removeStopWords(List<TMessage> msgs, Map<String, Set<String>> stopwords,
                                       String bestLang, double langsRatio) {
        // loads best lang
        if (!bestLang.equals("UNKNOWN") && !stopwords.containsKey(bestLang))
            stopwords.put(bestLang, loadStopWords(bestLang));
        // checks msgs
        for (TMessage msg : msgs) {
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
                    if (!lang.equals(bestLang) && (msg.getLangs().get(lang) / msg.getLangs().get(bestLang) > langsRatio)
                            && stopwords.get(bestLang).contains(tokens.get(j)))
                        flag = true;
                if (flag) {
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
     * @param msgs       messages
     * @param threshhold min threshold of popularity
     */
    public static String getDialogsBestLang(List<TMessage> msgs, double threshhold) {
        Integer totalCount = 0;
        Integer bestCount = -1;
        String bestLang = "UNKNOWN";
        Map<String, Integer> langsCounts = new HashMap<>();
        for (TMessage msg : msgs) {
            String bestlang = msg.getBestLang();
            if (!langsCounts.containsKey(bestlang)) langsCounts.put(bestlang, 0);
            langsCounts.put(bestlang, langsCounts.get(bestlang) + 1);
        }
        for (Map.Entry<String, Integer> count : langsCounts.entrySet()) {
            if (count.getValue() > bestCount) {
                bestCount = count.getValue();
                bestLang = count.getKey();
            }
            totalCount += count.getValue();
        }
        if ((totalCount > 0) && ((bestCount / totalCount) >= threshhold)) {
            return bestLang;
        } else {
            return "UNKNOWN";
        }
    }
}
