/*
 * Title: DataLoader.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.structures;

import com.crawlergram.db.DBStorageReduced;
import com.crawlergram.structures.dialog.TDialog;
import com.crawlergram.structures.dialog.TDialogComparator;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class TLoader {

    DBStorageReduced dbStorage = null;
    private List<TDialog> dialogs; // loaded dialogs
    private int current; // current dialog number
    private int dateTo; // messages date from (global)
    private int dateFrom; // messages date to (global)

    public int getDateTo() {
        return dateTo;
    }

    public TLoader setDateTo(int dateTo) {
        this.dateTo = dateTo;
        return this;
    }

    public int getDateFrom() {
        return dateFrom;
    }

    public TLoader setDateFrom(int dateFrom) {
        this.dateFrom = dateFrom;
        return this;
    }

    public List<TDialog> getDialogs() {
        return dialogs;
    }

    public DBStorageReduced getDbStorage() {
        return dbStorage;
    }

    public TLoader setDbStorage(DBStorageReduced dbStorage) {
        this.dbStorage = dbStorage;
        return this;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public TLoader(TLoaderBuilder builder) {
        this.dbStorage = builder.dbStorage;
        this.dateTo = builder.dateTo;
        this.dateFrom = builder.dateFrom;
        this.current = builder.current;
        List<Object> docs = dbStorage.getDialogs();
        this.dialogs = TDialog.telegramDialogsFromDB(dbStorage.getDialogs());
        Collections.sort(this.dialogs, new TDialogComparator());
    }

    public boolean hasNext() {
        if (current < this.dialogs.size()) {
            return true;
        } else {
            return false;
        }
    }

    public TDialog next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return dialogs.get(current++);
    }

    public TDialog getDialogByIndex(int ind) {
        return dialogs.get(ind);
    }

    public TDialog getDialogById(int id) {
        for (TDialog d : dialogs)
            if (d.getId() == id)
                return d;
        return null;
    }

    public TDialog getCurrentDialog() {
        return dialogs.get(current);
    }

    public int size() {
        return dialogs.size();
    }

    public void reset(){
        this.current = 0;
    }

    /**
     * loads messages for dialog
     *
     * @param dialog dialog
     */
    public List<TMessage> loadMessages(TDialog dialog) {
        return TMessage.tMessagesFromDB(dbStorage.readMessages(dialog, dateFrom, dateTo));
    }

    /**
     * loads messages for current dialog
     */
    public List<TMessage> loadMessages() {
        return TMessage.tMessagesFromDB(dbStorage.readMessages(dialogs.get(current), dateFrom, dateTo));
    }

    /**
     * loads messages for dialog
     *
     * @param dialog   dialog
     * @param dateFrom date from
     * @param dateTo   date to
     */
    public List<TMessage> loadMessages(TDialog dialog, int dateFrom, int dateTo) {
        return TMessage.tMessagesFromDB(dbStorage.readMessages(dialog, dateFrom, dateTo));
    }

    /**
     * loads messages for current dialog
     *
     * @param dateFrom date from
     * @param dateTo   date to
     */
    public List<TMessage> loadMessages(int dateFrom, int dateTo) {
        return TMessage.tMessagesFromDB(dbStorage.readMessages(dialogs.get(current), dateFrom, dateTo));
    }

    public static class TLoaderBuilder {

        DBStorageReduced dbStorage = null;
        private List<TDialog> dialogs; // loaded dialogs
        private int current = 0; // current dialog number
        private int dateTo = 0; // messages date from (global)
        private int dateFrom = 0; // messages date to (global)

        public TLoaderBuilder setDateTo(int dateTo) {
            this.dateTo = dateTo;
            return this;
        }

        public TLoaderBuilder setDateFrom(int dateFrom) {
            this.dateFrom = dateFrom;
            return this;
        }

        public TLoaderBuilder(DBStorageReduced dbStorage) {
            this.dbStorage = dbStorage;
        }

        public TLoader build() {
            return new TLoader(this);
        }
    }

}
