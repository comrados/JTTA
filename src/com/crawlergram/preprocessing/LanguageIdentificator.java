/*
 * Title: LanguageDetector.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing;

import com.crawlergram.preprocess.liga.LIGA;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LanguageIdentificator implements Preprocessor {

    private static TDialog dialog;
    private static Object langModel;

    /**
     * Indentifies languages for each message
     */
    @Override
    public List<TMessage> run() {
        if (langModel instanceof LIGA) {
            for (TMessage msg : dialog.getMessages())
                msg.setLangs(((LIGA) langModel).classify(msg.getClearText()));
        }
        if ((langModel instanceof LanguageDetector)) {
            for (TMessage msg : dialog.getMessages()) {
                Map<String, Double> langs = new TreeMap<>();
                LanguageResult result = ((LanguageDetector) langModel).detect(msg.getClearText());
                if (!result.isUnknown()) {
                    langs.put(result.getLanguage(), (double) result.getRawScore());
                }
                msg.setLangs(langs);
            }
        }
        return dialog.getMessages();
    }


    public LanguageIdentificator(LanguageIdentificatorBuilder builder){
        dialog = builder.dialog;
        langModel = builder.langModel;
    }

    public static class LanguageIdentificatorBuilder {

        private TDialog dialog;
        private Object langModel;

        LanguageIdentificatorBuilder(TDialog dialog, Object langModel) {
            this.dialog = dialog;
            this.langModel = langModel;
        }

        public LanguageIdentificator build() {
            return new LanguageIdentificator(this);
        }
    }

}
