/*
 * Title: TopicModelingMain.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.textclassification;

import com.crawlergram.db.DBStorageReduced;
import com.crawlergram.db.mongo.MongoDBStorageReduced;
import com.crawlergram.preprocessing.PreprocessingMain;
import com.crawlergram.preprocessing.liga.LIGA;
import com.crawlergram.preprocessing.models.*;
import com.crawlergram.structures.TLoader;
import com.crawlergram.structures.dialog.TDialog;
import com.crawlergram.structures.results.TCResults;
import com.crawlergram.textclassification.models.ClassificationModel;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TextClassificationMain {

    public static void textClassificationLoop(TLoader tLoader, List<ClassificationModel> classificationModels){
        System.out.println("Topic Modeling");
        while (tLoader.hasNext()){
            TDialog current = tLoader.next();

            System.out.println(current.getId() + " " + current.getUsername());

            TextClassification textClassification = new TextClassification.TextClassificationBuilder(current, classificationModels).build();

            textClassification.run();
            System.out.println();

            //res.addAll(textClassification.getResults());
        }
    }

    public static void main(String[] args) {

        // preprocessing first
        // DB "telegram" location - localhost:27017
        // User "telegramJ" - db.createUser({user: "telegramJ", pwd: "cart", roles: [{ role: "readWrite", db: "telegram" }]})
        DBStorageReduced dbStorage = new MongoDBStorageReduced("telegramJ", "telegram", "cart", "localhost", 27017, "fs");

        // language identification model (loaded only once)
        String ligaModelPath = "res" + File.separator + "liga" + File.separator + "model_n3.liga";
        LIGA ligaModel = new LIGA.LIGABuilder(0.5).setLogLIGA(true).setMaxSearchDepth(1000).build();
        ligaModel.loadModel(ligaModelPath);

        // optional language detection using Apache Tika
        LanguageDetector tikaModel = null;
        try {
            tikaModel = new OptimaizeLangDetector().loadModels();
        } catch (IOException e){}

        // map for stopwords to prevent multiple file readings
        Map<String, Set<String>> stopwords = new TreeMap<>();

        // loads dialogs
        TLoader tLoader = new TLoader.TLoaderBuilder(dbStorage).setDateFrom(0).setDateTo(0).build();

        List<PreprocessorModel> preprocessors = new ArrayList<>();
        // preprocessors.add(new MessageMerger.MessageMergerBuilder().build());
        preprocessors.add(new Tokenizer.TokenizerBuilder(true).build());
        preprocessors.add(new LanguageIdentificator.LanguageIdentificatorBuilder(tikaModel).build());
        preprocessors.add(new StopwordsRemover.StopwordsRemoverBuilder(stopwords).build());
        preprocessors.add(new StemmerGRAS.StemmerGRASBuilder().build());

        TCResults r = new TCResults();

        PreprocessingMain.preprocessingLoop(tLoader, dbStorage, preprocessors);

        tLoader.reset(); // just returns TLoader's pointer to 0th position

        List<ClassificationModel> classificationModels = new ArrayList<>();
        //classificationModels.add();

        textClassificationLoop(tLoader, classificationModels);



        System.exit(0);

    }

}
