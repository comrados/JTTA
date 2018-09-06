/*
 * Title: NaiveBayesExample.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.textclassification;

import com.crawlergram.preprocessing.models.Tokenizer;
import com.crawlergram.structures.TMessage;
import com.crawlergram.structures.dialog.TDialog;
import com.crawlergram.structures.results.TCResults;
import com.crawlergram.textclassification.models.ClassificationModel;
import com.crawlergram.textclassification.models.NaiveBayesModel;
import com.crawlergram.textclassification.naivebayes.NaiveBayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NaiveBayesExample {

    public static List<List<String>> readLines(String file) throws IOException {

        Tokenizer t = new Tokenizer.TokenizerBuilder(true).build();

        List<List<String>> lines;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            lines = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(t.tokenizeToList(line));
            }
        }
        return lines;
    }

    public static void main(String[] args) {

        Tokenizer t = new Tokenizer.TokenizerBuilder(true).build();

        // model training part
        /*
        Map<String, File> trainingFiles = new HashMap<>();
        trainingFiles.put("english", new File("res\\naivebayes\\datasets\\training.language.en.txt"));
        trainingFiles.put("french", new File("res\\naivebayes\\datasets\\training.language.fr.txt"));
        trainingFiles.put("german", new File("res\\naivebayes\\datasets\\training.language.de.txt"));

        NaiveBayesModel nb = new NaiveBayesModel.NaiveBayesModelBuilder().build();

        nb.trainModel(trainingFiles);
        nb.saveModel("res\\naivebayes\\model.nb");
        */

        TDialog d = new TDialog();

        List<TMessage> msgs = new ArrayList<>();
        msgs.add(new TMessage(0, "I am English", 1));
        msgs.add(new TMessage(1, "Je suis Français", 1));
        msgs.add(new TMessage(2, "Ich bin Deutscher", 1));

        for (TMessage msg: msgs){
            msg.setTokens(t.tokenizeToList(msg.getText()));
        }

        d.setMessages(msgs);

        List<ClassificationModel> classificationModels = new ArrayList<>();
        classificationModels.add(new NaiveBayesModel.NaiveBayesModelBuilder().setModelPath("res\\naivebayes\\model.nb").build());

        TextClassification tc = new TextClassification.TextClassificationBuilder(d, classificationModels).build();

        List<TCResults> res = tc.run();
        for (TCResults r: res)
            for (Map<String, Double> e: r.getScores())
             System.out.println("Prediction results: " + e);

        System.out.println();

    }

}
