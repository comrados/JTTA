/*
 * Title: LanguageDetector.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing.preprocessor;

import com.crawlergram.structures.TDialog;
import com.crawlergram.structures.TMessage;
import com.crawlergram.preprocessing.liga.LIGA;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

import java.util.Map;
import java.util.TreeMap;

public class LanguageIdentificator implements Preprocessor {

    private static Object langModel;

    /**
     * Indentifies languages for each message_old
     */
    @Override
    public TDialog run(TDialog dialog) {
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
        return dialog;
    }


    public LanguageIdentificator(LanguageIdentificatorBuilder builder){
        langModel = builder.langModel;
    }

    public static class LanguageIdentificatorBuilder {
        private Object langModel;

        public LanguageIdentificatorBuilder(Object langModel) {
            this.langModel = langModel;
        }

        public LanguageIdentificator build() {
            return new LanguageIdentificator(this);
        }
    }

}
