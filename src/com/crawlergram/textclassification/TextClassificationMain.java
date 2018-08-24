/*
 * Title: ClassificationMain.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.textclassification;

import com.crawlergram.db.DBStorageReduced;
import com.crawlergram.db.mongo.MongoDBStorageReduced;
import com.crawlergram.preprocessing.liga.LIGA;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class TextClassificationMain {

    public static void main(String[] args){

        // DB "telegram" location - localhost:27017
        // User "telegramJ" - db.createUser({user: "telegramJ", pwd: "cart", roles: [{ role: "readWrite", db: "telegram" }]})
        DBStorageReduced dbStorage = new MongoDBStorageReduced("telegramJ", "telegram", "cart", "localhost", 27017, "fs");

        // language identification model (loaded only once)
        String ligaModel = "res" + File.separator + "liga" + File.separator + "model_n3.liga";
        LIGA liga = new LIGA().setLogLIGA(true).setMaxSearchDepth(5000).setThreshold(0.5).setN(3).loadModel(ligaModel);

        // optional language detection using Apache Tika
        LanguageDetector tikaLD = null;
        try {
            tikaLD = new OptimaizeLangDetector().loadModels();
        } catch (IOException e){}

        // map for stopwords to prevent multiple file readings
        Map<String, Set<String>> stopwords = new TreeMap<>();

        //TODO
        TextClassificationMethods.classifyAllDialogs(dbStorage, 0, 0, 100, false, tikaLD, stopwords);

        // drop model and stopwords to save memory
        liga.dropModel();
        stopwords.clear();

    }

}
