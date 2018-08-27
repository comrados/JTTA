/*
 * Title: TopicModeling.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicmodeling;

import com.crawlergram.structures.dialog.TDialog;
import com.crawlergram.structures.results.TMResults;
import com.crawlergram.topicmodeling.models.TopicModel;

import java.util.ArrayList;
import java.util.List;

public class TopicModeling {

    private List<TMResults> results;
    private List<TopicModel> models;
    private TDialog dialog;

    public List<TMResults> getResults() {
        return results;
    }

    public void setResults(List<TMResults> results) {
        this.results = results;
    }

    public List<TopicModel> getModels() {
        return models;
    }

    public void setModels(List<TopicModel> models) {
        this.models = models;
    }

    public TDialog getDialog() {
        return dialog;
    }

    public void setDialog(TDialog dialog) {
        this.dialog = dialog;
    }

    public void run(){
        for (TopicModel model: models){
            results.add(model.run(dialog));
        }
    }

    public TopicModeling (TopicModelingBuilder builder) {
        this.dialog = builder.dialog;
        this.models = builder.models;
        this.results = new ArrayList<>();
    }

    public static class TopicModelingBuilder {

        private List<TopicModel> models;
        private TDialog dialog;

        public TopicModelingBuilder(TDialog dialog, List<TopicModel> models){
            this.dialog = dialog;
            this.models = models;
        }

        public TopicModeling build() {
            return new TopicModeling(this);
        }

    }

}
