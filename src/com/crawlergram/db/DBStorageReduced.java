/*
 * Title: DBStorage.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.db;

import com.crawlergram.preprocessing.TDialog;

import java.util.List;

public interface DBStorageReduced {

    /**
     * Sets target of writing or reading (table, collection, etc.) in db
     * @param target target's name
     */
    void setTarget(String target);

    /**
     * Drops target table, collection, etc. in db
     * @param target target's name
     */
    void dropTarget(String target);

    /**
     * Sets current db
     * @param database db name
     */
    void setDatabase(String database);

    /**
     * Drops current db
     */
    void dropDatabase();

    /**
     * write object to db
     * @param obj object
     */
    void write(Object obj);

    /**
     * creates single field index
     * @param field indexing field
     * @param type switch: 1 - ascending, -1 - descending, default - ascending
     */
    void createIndex(String field, int type);

    /**
     * creates composite index
     * @param fields indexing fields
     * @param types switch: 1 - ascending, -1 - descending, default - ascending
     */
    void createIndex(List<String> fields, List<Integer> types);

    /**
     * reads all messages from DB
     * @param target target dialog
     */
    List<Object> readMessages(TDialog target);

    /**
     * reads messages between two dates from DB
     * @param target target dialog
     * @param dateFrom date from
     * @param dateTo date to
     */
    List<Object> readMessages(TDialog target, int dateFrom, int dateTo);

    /**
     * returns dialogs list
     */
    List<Object> getDialogs();

    /**
     * saves files from DB to HDD
     * @param path path
     */
    void saveFilesToHDD(String path);

    /**
     * saves file from DB to HDD
     * @param path path
     * @param filePointer file id or another pointer
     */
    void saveFileToHDD(String path, Object filePointer);

}
