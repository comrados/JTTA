/*
 * Title: FeatureExtractor.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.textclassification.naivebayes.features;

import com.crawlergram.textclassification.naivebayes.structures.FeatureStats;

import java.util.Map;

public interface FeatureExtractor {

    Map<String, Double> run(FeatureStats stats);

}
