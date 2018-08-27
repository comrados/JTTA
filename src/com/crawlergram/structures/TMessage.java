/*
 * Title: TTEMessage.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.structures;

import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TMessage {

    protected Integer id;
    protected String text;
    protected Integer date;
    protected List<String> tokens = null;
    protected Map<String, Double> langs = new HashMap<>();

    public TMessage() {
        this.id = 0;
        this.text = "";
        this.date = 0;
    }

    public TMessage(Integer id, String text, Integer date) {
        this.id = id;
        this.text = text;
        this.date = date;
    }

    public TMessage(Integer id, String text, Integer date, List<String> tokens, Map<String, Double> langs) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.tokens = tokens;
        this.langs = langs;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
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
     * Converts mongoDB's document to TEM (extracts text of message_old or media's caption)
     * Strings are set converted to lowercase
     *
     * @param doc document
     */
    public static TMessage tMessageFromMongoDoc(Document doc) {
        if (doc.get("class").equals("Message")) {
            Integer id = (Integer) doc.get("_id");
            Integer date = (Integer) doc.get("date");
            String text = ((String) doc.get("message")).toLowerCase();
            if (text.isEmpty()) {
                text = getMediaCaption((Document) doc.get("media"));
            }
            return new TMessage(id, text, date);
        } else {
            return new TMessage();
        }
    }

    /**
     * Converts tokens back to the text ("clear" text)
     */
    public String getClearText() {
        if (tokens == null){
            return text;
        } else if (!tokens.isEmpty()){
            StringBuilder text = new StringBuilder();
            for (String token: tokens)
                text.append(token).append(" ");
            return text.toString().trim();
        } else {
            return "";
        }
    }

    /**
     * Converts mongoDB's documents to TEM (extracts text of message_old or media's caption)
     * Strings are set converted to lowercase
     *
     * @param docs documents
     */
    public static List<TMessage> tMessagesFromDB(List<Object> docs) {
        List<TMessage> msgs = new ArrayList<>();
        for (Object doc: docs){
            if (doc instanceof Document)
                msgs.add(tMessageFromMongoDoc((Document) doc));
        }
        return msgs;
    }

    /**
     * gets media's caption or description/title
     *
     * @param doc document
     */
    public static String getMediaCaption(Document doc) {
        if (doc != null) {
            if (doc.get("class").equals("MessageMediaDocument")) {
                return (String) doc.get("caption");
            } else if (doc.get("class").equals("MessageMediaPhoto")) {
                return (String) doc.get("caption");
            } else if (doc.get("class").equals("MessageMediaVenue")) {
                return (String) doc.get("title");
            } else if (doc.get("class").equals("MessageMediaInvoice")) {
                return (String) doc.get("title") + doc.get("description");
            } else {
                return "";
            }
        } else {
            return "";
        }
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



}
