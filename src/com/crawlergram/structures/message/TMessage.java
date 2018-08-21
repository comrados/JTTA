/*
 * Title: TTEMessage.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.structures.message;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class TMessage {

    protected Integer id;
    protected String text;
    protected Integer date;

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

    /**
     * Converts mongoDB's document to TEM (extracts text of message or media's caption)
     * Strings are set converted to lowercase
     *
     * @param doc document
     */
    public static TMessage telegramMessageFromMongoDocument(Document doc) {
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
     * Converts mongoDB's documents to TEM (extracts text of message or media's caption)
     * Strings are set converted to lowercase
     *
     * @param docs documents
     */
    public static List<TMessage> telegramMessagesFromMongoDocuments(List<Document> docs) {
        List<TMessage> msgs = new ArrayList<>();
        for (Document doc: docs){
            msgs.add(telegramMessageFromMongoDocument(doc));
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

}
