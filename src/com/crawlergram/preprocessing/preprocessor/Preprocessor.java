/*
 * Title: Preprocessor.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing.preprocessor;

import com.crawlergram.preprocessing.TDialog;
import com.crawlergram.preprocessing.TMessage;

import java.util.List;

public interface Preprocessor {

    /**
     * interface for preprocessor functions
     */
    TDialog run(TDialog dialog);

}
