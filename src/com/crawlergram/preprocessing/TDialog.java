/*
 * Title: TDialog.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing;

import com.crawlergram.db.DBStorageReduced;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TDialog {

    private Integer id;
    private String type;
    private Long accessHash;
    private String username;
    private Integer flags;
    private List<TMessage> messages = new ArrayList<>();

    public TDialog(Integer id, String type, Long accessHash, String username, Integer flags) {
        this.id = id;
        this.type = type;
        this.accessHash = accessHash;
        this.username = username;
        this.flags = flags;
    }

    public TDialog() {
        this.id = 0;
        this.type = "";
        this.accessHash = 0L;
        this.username = "";
        this.flags = 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAccessHash() {
        return accessHash;
    }

    public void setAccessHash(Long accessHash) {
        this.accessHash = accessHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getFlags() {
        return flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    public List<TMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<TMessage> messages) {
        this.messages = messages;
    }

    /**
     * converts data from DB to dialog
     *
     * @param info info from CHATS or USERS collections
     */
    public static TDialog telegramDialogFromMongoDocument(Document info) {
        Integer id = (Integer) info.get("_id");
        String type = (String) info.get("class");
        Integer flags = (Integer) info.get("flags");
        Long accessHash = 0L;
        if (type.equals("User") || type.equals("Channel")) {
            accessHash = (Long) info.get("accessHash");
        }
        String username = "";
        if (type.equals("Chat") || type.equals("Channel")) {
            username = (String) info.get("title");
        } else if (type.equals("User")) {
            username = info.get("userName") + " " + info.get("firstName") + " " + info.get("lastName");
        }
        return new TDialog(id, type, accessHash, username, flags);
    }

    /**
     * converts data from DB to dialogs
     *
     * @param docs documents
     */
    public static List<TDialog> telegramDialogsFromDB(List<Object> docs){
        List<TDialog> dialogs = new ArrayList<>();
        for (Object doc: docs){
            if (doc instanceof Document)
                dialogs.add(telegramDialogFromMongoDocument((Document) doc));
        }
        return dialogs;
    }

    /**
     * reads messages of this dialog
     *
     * @param dbStorage storage instance
     */
    public void loadMessages(DBStorageReduced dbStorage){
        this.messages = TMessage.tMessagesFromDB(dbStorage.readMessages(this));
    }

    /**
     * reads messages from given interval of this dialog
     *
     * @param dbStorage storage instance
     * @param dateFrom storage instance
     * @param dateTo storage instance
     */
    public void loadMessages(DBStorageReduced dbStorage, int dateFrom, int dateTo){
        this.messages = TMessage.tMessagesFromDB(dbStorage.readMessages(this, dateFrom, dateTo));
    }

    public void clearMessages(){
        this.messages.clear();
    }

    /**
     * Returns the language if it's the best language in the most of the cases
     *
     * @param threshhold min threshold of popularity
     */
    public String getDialogsBestLang(double threshhold) {
        Integer totalCount = 0;
        Integer bestCount = -1;
        String bestLang = "UNKNOWN";
        Map<String, Integer> langsCounts = new HashMap<>();
        for (TMessage msg : messages) {
            String bestlang = msg.getBestLang();
            if (!langsCounts.containsKey(bestlang)) langsCounts.put(bestlang, 0);
            langsCounts.put(bestlang, langsCounts.get(bestlang) + 1);
        }
        for (Map.Entry<String, Integer> count : langsCounts.entrySet()) {
            if (count.getValue() > bestCount) {
                bestCount = count.getValue();
                bestLang = count.getKey();
            }
            totalCount += count.getValue();
        }
        if ((totalCount > 0) && ((bestCount / (double) totalCount) >= threshhold)) {
            return bestLang;
        } else {
            return "UNKNOWN";
        }
    }

}
