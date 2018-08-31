/*
 * Title: Preprocessor.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing;

import com.crawlergram.preprocessing.models.PreprocessorModel;
import com.crawlergram.structures.dialog.TDialog;

import java.util.List;

public class Preprocessing {

    private List<PreprocessorModel> preprocessors;
    private TDialog dialog;

    public List<PreprocessorModel> getPreprocessors() {
        return preprocessors;
    }

    public void setPreprocessors(List<PreprocessorModel> preprocessors) {
        this.preprocessors = preprocessors;
    }

    public TDialog getDialog() {
        return dialog;
    }

    public void setDialog(TDialog dialog) {
        this.dialog = dialog;
    }

    Preprocessing(PreprocessingBuilder builder) {
        this.dialog = builder.dialog;
        this.preprocessors = builder.preprocessors;
    }

    public void run(){
        for (PreprocessorModel preprocessor: preprocessors){
            preprocessor.run(dialog);
        }
    }

    public static class PreprocessingBuilder {

        private List<PreprocessorModel> preprocessors;
        private TDialog dialog;

        PreprocessingBuilder(TDialog dialog, List<PreprocessorModel> preprocessors){
            this.dialog = dialog;
            this.preprocessors = preprocessors;
        }

        public Preprocessing build() {
            return new Preprocessing(this);
        }

    }

}
