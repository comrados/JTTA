/*
 * Title: PreprocessingMain.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.preprocessing;

import com.crawlergram.db.DBStorageReduced;
import com.crawlergram.db.mongo.MongoDBStorageReduced;
import com.crawlergram.preprocessing.liga.LIGA;
import com.crawlergram.preprocessing.models.*;
import com.crawlergram.structures.dialog.TDialog;
import com.crawlergram.structures.TLoader;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PreprocessingMain {

    public static void preprocessingLoop(TLoader tLoader, DBStorageReduced dbStorage, List<PreprocessorModel> preprocessors){
        System.out.println("Preprocessing");
        while (tLoader.hasNext()){
            TDialog current = tLoader.next();
            current.loadMessages(dbStorage);

            System.out.println(current.getId() + " " + current.getUsername());

            Preprocessing preproc = new Preprocessing.PreprocessingBuilder(current, preprocessors).build();
            preproc.run();
            System.out.println();
        }
    }

    public static void main(String[] args) {

        // DB "telegram" location - localhost:27017
        // User "telegramJ" - db.createUser({user: "telegramJ", pwd: "cart", roles: [{ role: "readWrite", db: "telegram" }]})
        DBStorageReduced dbStorage = new MongoDBStorageReduced("telegramJ", "telegram", "cart", "localhost", 27017, "fs");

        // language identification model (loaded only once)
        String ligaModelPath = "res" + File.separator + "liga" + File.separator + "model_n3.liga";
        LIGA ligaModel = new LIGA().setLogLIGA(true).setMaxSearchDepth(5000).setThreshold(0.5).setN(3).loadModel(ligaModelPath);

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
        preprocessors.add(new MessageMerger.MessageMergerBuilder().build());
        preprocessors.add(new Tokenizer.TokenizerBuilder().build());
        preprocessors.add(new LanguageIdentificator.LanguageIdentificatorBuilder(tikaModel).build());
        preprocessors.add(new StopwordsRemover.StopwordsRemoverBuilder(stopwords).build());
        preprocessors.add(new StemmerGRAS.StemmerGRASBuilder().build());

        preprocessingLoop(tLoader, dbStorage, preprocessors);

        System.exit(0);
    }

}
