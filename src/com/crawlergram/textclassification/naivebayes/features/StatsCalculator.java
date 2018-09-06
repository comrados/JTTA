/*
 * Title: StatsCalculator.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.textclassification.naivebayes.features;

import com.crawlergram.textclassification.naivebayes.structures.FeatureStats;
import com.crawlergram.textclassification.naivebayes.structures.TextDoc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsCalculator {

    /**
     * Generates a FeatureStats Object with metrics about he occurrences of the
     * keywords in categories, the number of category counts and the total number
     * of observations. These stats are used by the feature selection algorithm.
     *
     * @param messages messages
     */
    public FeatureStats calculateFeatureStats(List<TextDoc> messages) {
        FeatureStats stats = new FeatureStats();

        Map<String, Integer> featureCategoryCounts;
        for (TextDoc doc : messages) {
            ++stats.n; //increase the number of observations
            String category = doc.category;

            //increase the category counter by one
            stats.categoryCounts.merge(category, 1, (a, b) -> a + b);

            for (Map.Entry<String, Integer> entry : doc.tokenCounts.entrySet()) {
                String feature = entry.getKey();

                //get the counts of the feature in the categories
                featureCategoryCounts = stats.featureCategoryCount.get(feature);
                if (featureCategoryCounts == null) {
                    //initialize it if it does not exist
                    stats.featureCategoryCount.put(feature, new HashMap<>());
                }

                Integer featureCategoryCount = stats.featureCategoryCount.get(feature).get(category);
                if (featureCategoryCount == null) featureCategoryCount = 0;

                //increase the number of occurrences of the feature in the category
                stats.featureCategoryCount.get(feature).put(category, ++featureCategoryCount);
            }
        }

        return stats;
    }

    public StatsCalculator() {
    }

}
