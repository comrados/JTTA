/*
 * Title: FeatureStats.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.textclassification.naivebayes.structures;

import java.util.HashMap;
import java.util.Map;

public class FeatureStats {
    /**
     * total number of Observations
     */
    public int n;
    
    /**
     * It stores the co-occurrences of Feature and Category values
     */
    public Map<String, Map<String, Integer>> featureCategoryCount;
    
    /**
     * Measures how many times each category was found in the training dataset.
     */
    public Map<String, Integer> categoryCounts;

    /**
     * Constructor
     */
    public FeatureStats() {
        n = 0;
        featureCategoryCount = new HashMap<>();
        categoryCounts = new HashMap<>();
    }
}
