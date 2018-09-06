/*
 * Title: TopicModeling.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.textclassification;

import com.crawlergram.structures.dialog.TDialog;
import com.crawlergram.structures.results.TCResults;
import com.crawlergram.textclassification.models.ClassificationModel;

import java.util.ArrayList;
import java.util.List;

public class TextClassification {

    private List<TCResults> results = new ArrayList<>();
    private List<ClassificationModel> models;
    private TDialog dialog;

    public List<ClassificationModel> getModels() {
        return models;
    }

    public void setModels(List<ClassificationModel> models) {
        this.models = models;
    }

    public TDialog getDialog() {
        return dialog;
    }

    public void setDialog(TDialog dialog) {
        this.dialog = dialog;
    }

    public List<TCResults> run(){
        for (ClassificationModel model: models){
            results.add(model.classify(dialog));
        }
        return results;
    }

    public TextClassification(TextClassificationBuilder builder) {
        this.dialog = builder.dialog;
        this.models = builder.models;
    }

    public static class TextClassificationBuilder {

        private List<ClassificationModel> models;
        private TDialog dialog;

        public TextClassificationBuilder(TDialog dialog, List<ClassificationModel> models){
            this.dialog = dialog;
            this.models = models;
        }

        public TextClassification build() {
            return new TextClassification(this);
        }

    }

}
