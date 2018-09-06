/*
 * Title: TextClassificationModel.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.textclassification.models;

import com.crawlergram.structures.dialog.TDialog;
import com.crawlergram.structures.results.TCResults;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ClassificationModel {

    /**
     * Text classification
     *
     * @param dialog    dialog
     */
    TCResults classify(TDialog dialog);

    /**
     * Model training
     *
     * @param trainingFiles map of training files: <category, file path>
     */
    void trainModel(Map<String, File> trainingFiles);

    /**
     * save trainded model
     *
     * @param modelPath path to save the model
     */
    void saveModel(String modelPath);

}
