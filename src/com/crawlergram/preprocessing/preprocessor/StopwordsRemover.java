/*
 * Title: StopwordsRemover.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing.preprocessor;

import com.crawlergram.preprocessing.TDialog;
import com.crawlergram.preprocessing.TMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class StopwordsRemover implements Preprocessor {

    private static Map<String, Set<String>> stopwords;
    private double langsRatio;
    private double popTreshold;

    @Override
    public List<TMessage> run(TDialog dialog) {
        return removeStopWords(dialog, langsRatio);
    }

    StopwordsRemover(StopwordsRemoverBuilder builder){
        stopwords = builder.stopwords;
        this.langsRatio = builder.langsRatio;
        this.popTreshold = builder.popTreshold;
    }

    /**
     * Removes stopwords from token compounds. If chat has a dominating language - additional chack
     */
    public List<TMessage> removeStopWords(TDialog dialog, double langsRatio) {
        String bestLang = dialog.getDialogsBestLang(popTreshold);
        // loads best lang
        if (!bestLang.equals("UNKNOWN") && !stopwords.containsKey(bestLang))
            stopwords.put(bestLang, loadStopWords(bestLang));
        // checks msgs
        for (TMessage msg : dialog.getMessages()) {
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
        return dialog.getMessages();
    }

    /**
     * loads stop words from a file to the set
     *
     * @param language language code (i.e. en, es, de, ru etc.)
     */
    private Set<String> loadStopWords(String language) {
        Set<String> stopWords = new TreeSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("res" + File.separator + "stopwords" + File.separator + language.toLowerCase() + ".txt"))) {
            for (String doc; (doc = br.readLine()) != null; )
                if (!doc.trim().isEmpty()) stopWords.add(doc.trim());
        } catch (IOException e) {
            //System.out.println("Can't read stopwords for " + language.toUpperCase() + " language");
        }
        return stopWords;
    }

    public static class StopwordsRemoverBuilder{

        private Map<String, Set<String>> stopwords;
        private double langsRatio = 0.9;
        private double popTreshold = 0.75;

        public StopwordsRemoverBuilder setLangsRatio(double langsRatio) {
            this.langsRatio = langsRatio;
            return this;
        }

        public StopwordsRemoverBuilder setPopTreshold(double popTreshold) {
            this.popTreshold = popTreshold;
            return this;
        }

        public StopwordsRemoverBuilder(Map<String, Set<String>> stopwords) {
            this.stopwords = stopwords;
        }

        public StopwordsRemover build() {
            return new StopwordsRemover(this);
        }

    }

}
