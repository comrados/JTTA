/*
 * Title: TDialogComparator.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing;

import java.util.Comparator;

public class TDialogComparator implements Comparator<TDialog> {
    @Override
    public int compare(TDialog d1, TDialog d2) {
        //if dates are equal - compare IDs
        return d1.getId().compareTo(d2.getId());
    }
}
