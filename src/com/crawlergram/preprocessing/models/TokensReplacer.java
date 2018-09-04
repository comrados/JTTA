/*
 * Title: TokensReplacer.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing.models;

import com.crawlergram.structures.TMessage;
import com.crawlergram.structures.dialog.TDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TokensReplacer implements PreprocessorModel {

    private Map<String, Map<String, List<String>>> replacements;
    private boolean langsStrict;
    private double langsRatio;
    private double popTreshold;

    public TokensReplacer(TokensReplacerBuilder builder){
        this.replacements = builder.replaces;
        this.langsStrict = builder.langsStrict;
        this.langsRatio = builder.langsRatio;
        this.popTreshold = builder.popTreshold;
    }

    @Override
    public TDialog run(TDialog dialog) {
        dialog.setMessages(replaceWords(dialog));
        String l = dialog.getDialogsBestLang(0.75);
        return dialog;
    }

    /**
     * Replaces abbreviations from user defined list from res folder
     * if langStrict == true, changes only for detected language, otherwise prefers dialogs' most popular lang (if ratio check passes)
     *
     * @param dialog
     */
    private List<TMessage> replaceWords(TDialog dialog){
        String bestLang = dialog.getDialogsBestLang(popTreshold);
        // loads best lang
        if (!bestLang.equals("UNKNOWN") && !replacements.containsKey(bestLang))
            replacements.put(bestLang, loadReplacements(bestLang));
        // checks msgs
        for (TMessage msg : dialog.getMessages()) {
            String lang = msg.getBestLang();
            // load stopwords for "lang" if only they're not loaded before
            if (!replacements.containsKey(lang))
                replacements.put(lang, loadReplacements(lang));
            List<String> tokens = msg.getTokens();
            boolean flag = false;
            boolean flagBest = false;
            for (int j = 0; j < tokens.size(); j++) {
                if (replacements.get(lang).containsKey(tokens.get(j)))
                    flag = true;
                // additionally check if LI doubts (if lang != bestLang and PbestLang/Plang > langsRatio)
                if (msg.getLangs().containsKey(bestLang))
                    if (!lang.equals(bestLang) && (msg.getLangs().get(lang) / msg.getLangs().get(bestLang) > langsRatio)
                            && replacements.get(bestLang).containsKey(tokens.get(j)))
                        flagBest = true;
                if (flagBest && !langsStrict){
                    List<String> reps = replacements.get(bestLang).get(msg.getTokens().get(j));
                    msg.getTokens().remove(j);
                    msg.getTokens().addAll(j, reps);
                    j += reps.size()-1;
                    flagBest = false;
                } else {
                    if (flag) {
                        List<String> reps = replacements.get(lang).get(msg.getTokens().get(j));
                        msg.getTokens().remove(j);
                        msg.getTokens().addAll(j, reps);
                        j += reps.size()-1;
                        flag = false;
                    }
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
    private Map<String, List<String>> loadReplacements(String language) {
        Map<String, List<String>> reps = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("res" + File.separator + "replacements" + File.separator + language.toLowerCase() + ".txt"))) {
            for (String doc; (doc = br.readLine()) != null; ) {
                if (!doc.trim().isEmpty()) {
                    String[] words = doc.trim().split("\\s+");
                    if (words.length > 1) {
                        List<String> tokens = new LinkedList<>();
                        for (int i = 1; i < words.length; i++) {
                            tokens.add(words[i]);
                        }
                        reps.put(words[0], tokens);
                    }
                }
            }
        } catch (IOException e) {
            //System.out.println("Can't read replacements for " + language.toUpperCase() + " language");
        }
        return reps;
    }

    public static class TokensReplacerBuilder {

        private Map<String, Map<String, List<String>>> replaces;
        private boolean langsStrict = false;
        private double langsRatio = 0.9;
        private double popTreshold = 0.75;

        public TokensReplacerBuilder setLangsStrict(boolean langsStrict) {
            this.langsStrict = langsStrict;
            return this;
        }

        public TokensReplacerBuilder setLangsRatio(double langsRatio) {
            this.langsRatio = langsRatio;
            return this;
        }

        public TokensReplacerBuilder setPopTreshold(double popTreshold) {
            this.popTreshold = popTreshold;
            return this;
        }

        /**
         * builder
         *
         * @param replacements map to store replacements
         */
        public TokensReplacerBuilder(Map<String, Map<String, List<String>>> replacements) {
            this.replaces = replacements;
        }

        public TokensReplacer build() {
            return new TokensReplacer(this);
        }
    }

}
