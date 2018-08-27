/*
 * Title: TMessageProxy.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.structures;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class TMessageProxy {

    protected Integer id;

    public TMessageProxy() {
        this.id = 0;
    }

    public TMessageProxy(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Converts mongoDB's document to TEM (extracts text of message_old or media's caption)
     * Strings are set converted to lowercase
     *
     * @param doc document
     */
    public static TMessageProxy tMessageProxyFromMongoDoc(Document doc) {
        if (doc.get("class").equals("Message")) {
            Integer id = (Integer) doc.get("_id");
            return new TMessageProxy(id);
        } else {
            return new TMessageProxy();
        }
    }

    /**
     * Converts mongoDB's documents to TEM (extracts text of message_old or media's caption)
     * Strings are set converted to lowercase
     *
     * @param docs documents
     */
    public static List<TMessageProxy> tMessagesProxyFromMongoDocs(List<Object> docs) {
        List<TMessageProxy> msgs = new ArrayList<>();
        for (Object doc: docs){
            if (doc instanceof Document)
                msgs.add(tMessageProxyFromMongoDoc((Document) doc));
        }
        return msgs;
    }

}
