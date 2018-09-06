/*
 * Title: NaiveBayesModel.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */
package com.crawlergram.textclassification.naivebayes.structures;

import java.util.HashMap;
import java.util.Map;

public class NaiveBayesModel {
    /**
     * number of training observations
     */
    public int n = 0;

    /**
     * number of categories
     */
    public int c = 0;

    /**
     * number of features
     */
    public int d = 0;

    /**
     * log priors for log( P(c) )
     */
    public Map<String, Double> logPriors = new HashMap<>();

    /**
     * log likelihood for log( P(x|c) )
     */
    public Map<String, Map<String, Double>> logLikelihoods = new HashMap<>();

    public NaiveBayesModel() {
    }

    public NaiveBayesModel(FeatureStats stats) {
        n = stats.n;
        d = stats.featureCategoryCount.size();
        c = stats.categoryCounts.size();
    }

    public boolean isValid() {
        return ((n > 0) && (c > 0) && (d > 0) && !logPriors.isEmpty() && !logLikelihoods.isEmpty());
    }

}
