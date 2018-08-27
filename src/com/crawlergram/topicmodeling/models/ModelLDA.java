/*
 * Title: LDAModel.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicmodeling.models;

import com.crawlergram.structures.TDialog;
import com.crawlergram.structures.results.TMResults;
import com.crawlergram.topicmodeling.ldadmm.models.GSLDA;

public class ModelLDA implements TopicModel {

    private int topics; // number of topics
    private double alpha; // alpha hyperparameter
    private double beta; // betha hyperparameter
    private int iterations; // number of Gibbs Sampling iterations
    private int topWords; // top topical words number

    @Override
    public TMResults run(TDialog dialog) {
        GSLDA dmm = new GSLDA(dialog.getMessages(), topics, alpha, beta, iterations, topWords);
        return dmm.inference();
    }

    public ModelLDA(ModelLDABuilder builder){
        this.topics = builder.topics;
        this.alpha = builder.alpha;
        this.beta = builder.beta;
        this.iterations = builder.iterations;
        this.topWords = builder.topWords;
    }


    public static class ModelLDABuilder {

        private int topics; // number of topics
        private double alpha = 0.01; // alpha hyperparameter
        private double beta = 0.1; // betha hyperparameter
        private int iterations = 1000; // number of Gibbs Sampling iterations
        private int topWords; // top topical words number

        public ModelLDABuilder setAlpha(double alpha) {
            this.alpha = alpha;
            return this;
        }

        public ModelLDABuilder setBeta(double beta) {
            this.beta = beta;
            return this;
        }

        public ModelLDABuilder setIterations(int iterations) {
            this.iterations = iterations;
            return this;
        }

        public ModelLDABuilder(int topics, int topWords) {
            this.topics = topics;
            this.topWords = topWords;
        }

        public ModelLDA build() {
            return new ModelLDA(this);
        }

    }

}
