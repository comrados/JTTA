/*
 * Title: Preprocessor.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing.preprocessor;

import com.crawlergram.structures.dialog.TDialog;

public interface Preprocessor {

    /**
     * interface for preprocessor classes
     */
    TDialog run(TDialog dialog);

}
