/*
 * Title: TextClassificationMethods.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.textclassification;

import com.crawlergram.db.DBStorageReduced;
import com.crawlergram.preprocess.UtilMethods;
import com.crawlergram.structures.TDialog;
import com.crawlergram.structures.message_old.TEMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TextClassificationMethods {

    /**
     * do topic extraction for each dialog, if dates are wrong (from > to) or both dates equal zero -> read all messages
     *
     * @param dbStorage    db storage implementation
     * @param dateFrom     date from
     * @param dateTo       date to
     * @param docThreshold if chat has very low number of messages (< docThreshold) -> all chat is merged
     * @param msgMerging   if true - artificial documents will be created from messages, preferable for LDA
     * @param lang         language identification model
     * @param stopwords    stopwords map to prevent multiple file readings
     */
    public static void classifyAllDialogs(DBStorageReduced dbStorage, int dateFrom, int dateTo, int docThreshold,
                                              boolean msgMerging, Object lang, Map<String, Set<String>> stopwords) {
        // get all dialogs
        List<TDialog> dialogs = TDialog.telegramDialogsFromDB(dbStorage.getDialogs());
        if ((dialogs != null) && (!dialogs.isEmpty())) {
            for (TDialog dialog : dialogs) {
                // do for one
                classifyOneDialog(dbStorage, dialog, dateFrom, dateTo, docThreshold, msgMerging, lang, stopwords);
            }
        } else {
            System.out.println("NO DIALOGS FOUND");
        }
    }

    /**
     * do topic extraction for a specific dialog, if dates are wrong (from > to) or both dates equal zero -> read all
     *
     * @param dbStorage    db storage implementation
     * @param dialog       dialog
     * @param dateFrom     date from
     * @param dateTo       date to
     * @param docThreshold if chat has very low number of messages (< docThreshold) -> all chat is merged
     * @param msgMerging   if true - artificial documents will be created from messages, preferable for LDA
     * @param lang         language identification model
     * @param stopwords    stopwords map to prevent multiple file readings
     */
    public static void classifyOneDialog(DBStorageReduced dbStorage, TDialog dialog, int dateFrom, int dateTo,
                                         int docThreshold, boolean msgMerging, Object lang,
                                         Map<String, Set<String>> stopwords) {
        List<TEMessage> msgs = new ArrayList<>();
        // if dates valid - get only messages between these dates, otherwise - get all messages
        if (UtilMethods.datesCheck(dateFrom, dateTo)) {
            //msgs = TEMessage.topicExtractionMessagesFromMongoDocuments(dbStorage.readMessages(dialog, dateFrom, dateTo));
        } else {
            //msgs = TEMessage.topicExtractionMessagesFromMongoDocuments(dbStorage.readMessages(dialog));
        }
        // check if resulting list is not empty
        if ((msgs != null) && !msgs.isEmpty()) {

            //TODO
        } else {
            System.out.println("EMPTY MESSAGES: " + dialog.getId() + " " + dialog.getUsername());
        }
    }

}
