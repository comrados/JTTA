/*
 * Title: Preprocessor.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing.models;

import com.crawlergram.structures.dialog.TDialog;

public interface PreprocessorModel {

    /**
     * interface for preprocessor classes
     */
    TDialog run(TDialog dialog);

}
