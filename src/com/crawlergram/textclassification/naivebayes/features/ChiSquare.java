/*
 * Title: ChiSquare.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.textclassification.naivebayes.features;

import com.crawlergram.textclassification.naivebayes.structures.FeatureStats;

import java.util.HashMap;
import java.util.Map;


public class ChiSquare implements FeatureExtractor {

    double criticalLevel;

    /**
     * Perform feature selection by using the chisquare non-parametrical statistical test.
     *
     * @param stats calculated stats
     */
    public Map<String, Double> run(FeatureStats stats) {
        Map<String, Double> selectedFeatures = new HashMap<>();

        int n1dot, n0dot, n00, n01, n10, n11;
        for (Map.Entry<String, Map<String, Integer>> entry1 : stats.featureCategoryCount.entrySet()) {
            String feature = entry1.getKey();
            Map<String, Integer> categoryList = entry1.getValue();
            //calculate the N1. (number of documents that have the feature)
            n1dot = 0;
            for (Integer count : categoryList.values())
                n1dot += count;
            //also the N0. (number of documents that DONT have the feature)
            n0dot = stats.n - n1dot;
            for (Map.Entry<String, Integer> entry2 : categoryList.entrySet()) {
                String category = entry2.getKey();
                //n11 documents that have the feature and belong on the specific category
                n11 = entry2.getValue();
                //n01 documents that do not have the particular feature BUT they belong to the specific category
                n01 = stats.categoryCounts.get(category) - n11;
                //n00 documents that don't have the feature and don't belong to the specific category
                n00 = n0dot - n01;
                //n10 documents that have the feature and don't belong to the specific category
                n10 = n1dot - n11;
                //calculate the chisquare score based on the above statistics
                Double chisquareScore = stats.n * Math.pow(n11 * n00 - n10 * n01, 2); // numerator
                chisquareScore /= ((n11 + n01) * (n11 + n10) * (n10 + n00) * (n01 + n00)); // denominator
                //if the score is larger than the critical value then add it in the list
                if (chisquareScore >= criticalLevel) {
                    Double previousScore = selectedFeatures.get(feature);
                    if (previousScore == null || chisquareScore > previousScore) {
                        selectedFeatures.put(feature, chisquareScore);
                    }
                }
            }
        }
        return selectedFeatures;
    }

    public ChiSquare(ChiSquareBuilder builder) {
        this.criticalLevel = builder.criticalLevel;
    }

    public static class ChiSquareBuilder {

        double criticalLevel;

        /**
         * builder
         *
         * @param criticalLevel critical feature level
         */
        public ChiSquareBuilder(double criticalLevel) {
            this.criticalLevel = criticalLevel;
        }

        public ChiSquare build() {
            return new ChiSquare(this);
        }
    }

}

