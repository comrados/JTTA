/*
 * Title: TCResults.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.structures.results;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TCResults {

    private List<Map<String, Double>> scores; // maps category and score for this category

    public TCResults() {
        this.scores = new ArrayList<>();
    }

    public TCResults(List<Map<String, Double>> scores) {
        this.scores = scores;
    }

    public List<Map<String, Double>> getScores() {
        return scores;
    }

    public void setScores(List<Map<String, Double>> scores) {
        this.scores = scores;
    }
}
