/*
 * Title: TCMessage.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.structures.message_old;

import java.util.List;
import java.util.Map;

public class TCMessage extends TEMessage {

    public TCMessage() {
        super();
    }

    public TCMessage(Integer id, String text, Integer date, List<String> tokens, Map<String, Double> langs,
                     String stemmedText) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.tokens = tokens;
        this.langs = langs;
        this.stemmedText = stemmedText;
    }

}
