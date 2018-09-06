/*
 * Title: NaiveBayesModel.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.textclassification.models;

import com.crawlergram.preprocessing.models.Tokenizer;
import com.crawlergram.structures.TMessage;
import com.crawlergram.structures.dialog.TDialog;
import com.crawlergram.structures.results.TCResults;
import com.crawlergram.textclassification.naivebayes.NaiveBayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NaiveBayesModel implements ClassificationModel {

    private NaiveBayes nb;
    private String modelPath;

    public NaiveBayesModel(NaiveBayesModelBuilder builder) {
        this.nb = builder.nb;
        this.modelPath = builder.modelPath;
    }

    /**
     * Text classification
     *
     * @param dialog    dialog
     */
    @Override
    public TCResults classify(TDialog dialog) {
        TCResults res = new TCResults();
        nb.loadModel(modelPath);
        if (nb.getModel().isValid()) {
            for (TMessage msg : dialog.getMessages()) {
                res.getScores().add(nb.classify(msg.getTokens()));
            }
        }
        return res;
    }

    /**
     * Model training
     *
     * @param trainingFiles map of training files: <category, file path>
     */
    @Override
    public void trainModel(Map<String, File> trainingFiles) {
        Map<String, List<List<String>>> trainingSet = openTrainingFiles(trainingFiles);
        if (!trainingSet.isEmpty()){
            nb.train(trainingSet);
        } else {
            System.out.println("Impossible to train NB model");
            System.exit(1);
        }
    }

    /**
     * converts training files of different categories to map
     *
     * @param trainingFiles training files
     * @return
     */
    private Map<String, List<List<String>>> openTrainingFiles(Map<String, File> trainingFiles) {
        Map<String, List<List<String>>> trainingSet = new HashMap<>();
        for (Map.Entry<String, File> entry : trainingFiles.entrySet()) {
            String category = entry.getKey();
            File path = entry.getValue();
            try {
                List<List<String>> data = openTrainingFile(path);
                trainingSet.put(category, data);
            } catch (IOException e) {
                System.out.println("Impossible to open file: " + category);
                return new HashMap<>();
            }
        }
        return trainingSet;
    }

    /**
     * Opens training file and tokenizes content
     *
     * @param file file
     * @throws IOException
     */
    private List<List<String>> openTrainingFile(File file) throws IOException {
        List<List<String>> lines = new ArrayList<>();
        Tokenizer t = new Tokenizer.TokenizerBuilder(true).build();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(t.tokenizeToList(line));
            }
        }
        return lines;
    }

    /**
     * save trainded model
     *
     * @param modelPath path to save the model
     */
    @Override
    public void saveModel(String modelPath) {
        nb.saveModel(modelPath);
    }

    public static class NaiveBayesModelBuilder {

        NaiveBayes nb = new NaiveBayes();
        private String modelPath;

        public NaiveBayesModelBuilder setChiSquareThreshold(double chiSquareThreshold) {
            this.nb.setChiSquareThreshold(chiSquareThreshold);
            return this;
        }

        public NaiveBayesModelBuilder setModelPath(String modelPath) {
            this.modelPath = modelPath;
            return this;
        }

        /**
         * builder
         */
        public NaiveBayesModelBuilder() {}

        public NaiveBayesModel build() {
            return new NaiveBayesModel(this);
        }
    }

}
