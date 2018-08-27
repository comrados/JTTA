/*
 * Title: ModelDMM.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicmodeling.models;

import com.crawlergram.structures.dialog.TDialog;
import com.crawlergram.structures.results.TMResults;
import com.crawlergram.topicmodeling.ldadmm.models.GSDMM;

public class ModelDMM implements TopicModel {

    private int topics; // number of topics
    private double alpha; // alpha hyperparameter
    private double beta; // betha hyperparameter
    private int iterations; // number of Gibbs Sampling iterations
    private int topWords; // top topical words number

    @Override
    public TMResults run(TDialog dialog) {
        GSDMM dmm = new GSDMM(dialog.getMessages(), topics, alpha, beta, iterations, topWords);
        return dmm.inference();
    }

    public ModelDMM(ModelDMMBuilder builder){
        this.topics = builder.topics;
        this.alpha = builder.alpha;
        this.beta = builder.beta;
        this.iterations = builder.iterations;
        this.topWords = builder.topWords;
    }


    public static class ModelDMMBuilder {

        private int topics; // number of topics
        private double alpha = 0.1; // alpha hyperparameter
        private double beta = 0.1; // betha hyperparameter
        private int iterations = 1000; // number of Gibbs Sampling iterations
        private int topWords; // top topical words number

        public ModelDMMBuilder setAlpha(double alpha) {
            this.alpha = alpha;
            return this;
        }

        public ModelDMMBuilder setBeta(double beta) {
            this.beta = beta;
            return this;
        }

        public ModelDMMBuilder setIterations(int iterations) {
            this.iterations = iterations;
            return this;
        }

        public ModelDMMBuilder(int topics, int topWords) {
            this.topics = topics;
            this.topWords = topWords;
        }

        public ModelDMM build() {
            return new ModelDMM(this);
        }

    }
}
