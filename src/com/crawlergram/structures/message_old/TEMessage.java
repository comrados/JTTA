/*
 * Title: TopicExtractionMessage.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.structures.message_old;

import com.crawlergram.structures.TMessage;
import org.bson.Document;

import java.util.*;

public class TEMessage extends TMessage {

    protected String stemmedText = null;
    protected List<String> tokens = null;
    protected Map<String, Double> langs = null;

    public TEMessage() {
        super();
    }

    public TEMessage(Integer id, String text, Integer date) {
        this.id = id;
        this.text = text;
        this.stemmedText = null;
        this.date = date;
        this.tokens = new ArrayList<>();
        this.langs = new TreeMap<>();
    }

    public String getStemmedText() {
        return stemmedText;
    }

    public void setStemmedText(String stemmedText) {
        this.stemmedText = stemmedText;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public Map<String, Double> getLangs() {
        return langs;
    }

    public void setLangs(Map<String, Double> langs) {
        this.langs = langs;
    }

    /**
     * Converts tokens back to the text ("clear" text)
     */
    public String getClearText() {
        StringBuilder text = new StringBuilder();
        for (String token: tokens)
            text.append(token).append(" ");
        return text.toString().trim();
    }

    public String getBestLang() {
        Double bestScore = -1.0;
        String bestLang = "UNKNOWN";
        // Get the best score or return unknown
        if (!langs.isEmpty())
            for (Map.Entry<String, Double> score : langs.entrySet())
                if (score.getValue() > bestScore){
                    bestScore = score.getValue();
                    bestLang = score.getKey();
                }
        return bestLang;
    }

    public static TEMessage topicExtractionMessageMessageFromMongoDocument(Document doc) {
        TMessage msg = tMessageFromMongoDoc(doc);
        return new TEMessage(msg.getId(), msg.getText(), msg.getDate());
    }

    public static List<TEMessage> topicExtractionMessagesFromMongoDocuments(List<Object> docs) {
        List<TEMessage> msgs = new ArrayList<>();
        for (Object doc: docs){
            if (doc instanceof Document)
                msgs.add(topicExtractionMessageMessageFromMongoDocument((Document) doc));
        }
        return msgs;
    }


}
