/*
 * Title: TCMessage.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.structures.message;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
