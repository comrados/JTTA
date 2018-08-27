/*
 * Title: StemmerGRAS.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing.preprocessor;

import com.crawlergram.structures.dialog.TDialog;
import com.crawlergram.structures.TMessage;
import com.crawlergram.preprocessing.gras.GRAS;

import java.util.Map;
import java.util.TreeMap;


public class StemmerGRAS implements Preprocessor {

    private int l;
    private int alpha;
    private double delta;

    public StemmerGRAS(StemmerGRASBuilder builder){
        this.l = builder.l;
        this.alpha = builder.alpha;
        this.delta = builder.delta;
    }

    @Override
    public TDialog run(TDialog dialog) {
        Map<String, String> uniqueWords = getUniqueWords(dialog);
        dialog.setUniqueWords(GRAS.doStemming(uniqueWords, l, alpha, delta));
        return dialog;
    }

    /**
     * returns a sorted set of tokens
     *
     * @param dialog original msgs object (with calculated simple and compound tokens)
     */
    private Map<String, String> getUniqueWords(TDialog dialog) {
        Map<String, String> uniqueWords = new TreeMap<>();
        for (TMessage msg : dialog.getMessages()) {
            for (String token : msg.getTokens()) {
                if (!uniqueWords.containsKey(token)) uniqueWords.put(token, null);
            }
        }
        return uniqueWords;
    }

    public static class StemmerGRASBuilder{

        private int l = 5;
        private int alpha = 4;
        private double delta = 0.8;

        public StemmerGRASBuilder setL(int l) {
            this.l = l;
            return this;
        }

        public StemmerGRASBuilder setAlpha(int alpha) {
            this.alpha = alpha;
            return this;
        }

        public StemmerGRASBuilder setDelta(double delta) {
            this.delta = delta;
            return this;
        }

        public StemmerGRASBuilder() {
        }

        public StemmerGRAS build() {
            return new StemmerGRAS(this);
        }

    }

}
