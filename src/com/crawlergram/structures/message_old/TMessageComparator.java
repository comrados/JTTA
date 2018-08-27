/*
 * Title: TopicExtractionMessageComparator.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.structures.message_old;

import com.crawlergram.structures.TMessage;

import java.util.Comparator;

public class TMessageComparator implements Comparator<TMessage> {
    @Override
    public int compare(TMessage d1, TMessage d2) {
        //if dates are equal - compare IDs
        return d2.getDate().compareTo(d1.getDate()) == 0 ? d2.getId().compareTo(d1.getId()) : d2.getDate().compareTo(d1.getDate());
    }
}
