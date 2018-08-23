/*
 * Title: Preprocessor.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing;

import java.util.List;

public interface Preprocessor {

    /**
     * interface for preprocessor functions
     */
    List<TMessage> run();

}
