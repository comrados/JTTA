/*
 * Title: TextDoc.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */
package com.crawlergram.textclassification.naivebayes.structures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextDoc {

    public Map<String, Integer> tokenCounts;

    public String category;

    public TextDoc(List<String> tokens){
        this.tokenCounts = getTokenCounts(tokens);
    }

    /**
     * Counts the number of occurrences of the tokens inside the text.
     *
     * @param tokens
     * @return
     */
    private Map<String, Integer> getTokenCounts(List<String> tokens) {
        Map<String, Integer> counts = new HashMap<>();
        if (tokens == null)
            System.out.println();
        for (String token: tokens){
            if (token == null)
                System.out.println();
            Integer counter = counts.get(token);
            if (counter == null) counter = 0;
            counts.put(token, ++counter);
        }
        return counts;
    }

}
