/*
 * Title: TopicModelingMain.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.topicmodeling;

import com.crawlergram.db.DBStorageReduced;
import com.crawlergram.db.mongo.MongoDBStorageReduced;
import com.crawlergram.preprocessing.PreprocessingMain;
import com.crawlergram.preprocessing.liga.LIGA;
import com.crawlergram.preprocessing.models.*;
import com.crawlergram.structures.dialog.TDialog;
import com.crawlergram.structures.TLoader;
import com.crawlergram.structures.results.TMResults;
import com.crawlergram.topicmodeling.models.ModelDMM;
import com.crawlergram.topicmodeling.models.ModelLDA;
import com.crawlergram.topicmodeling.models.TopicModel;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TopicModelingMain {

    public static Map<Integer, List<TMResults>> topicModelingLoop(TLoader tLoader, List<TopicModel> topicModels){
        System.out.println("Topic Modeling");
        HashMap<Integer, List<TMResults>> res = new HashMap<>();
        while (tLoader.hasNext()){
            TDialog current = tLoader.next();

            System.out.println(current.getId() + " " + current.getUsername());

            TopicModeling topicModeling = new TopicModeling.TopicModelingBuilder(current, topicModels).build();

            topicModeling.run();
            System.out.println();

            if (!res.containsKey(current.getId())){
                res.put(current.getId(), topicModeling.getResults());
            } else {
                List<TMResults> old = res.get(current.getId());
                old.addAll(topicModeling.getResults());
                res.put(current.getId(), old);
            }
        }
        return res;
    }

    public static void main(String[] args) {

        // preprocessing first
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
        // preprocessors.add(new MessageMerger.MessageMergerBuilder().build());
        preprocessors.add(new Tokenizer.TokenizerBuilder().build());
        preprocessors.add(new LanguageIdentificator.LanguageIdentificatorBuilder(tikaModel).build());
        preprocessors.add(new StopwordsRemover.StopwordsRemoverBuilder(stopwords).build());
        preprocessors.add(new StemmerGRAS.StemmerGRASBuilder().build());

        PreprocessingMain.preprocessingLoop(tLoader, dbStorage, preprocessors);

        tLoader.reset(); // just returns TLoader's pointer to 0th position

        List<TopicModel> topicModels = new ArrayList<>();
        topicModels.add(new ModelDMM.ModelDMMBuilder(10, 10).setIterations(100).build());
        topicModels.add(new ModelLDA.ModelLDABuilder(10, 10).setIterations(100).build());

        Map<Integer, List<TMResults>> results = topicModelingLoop(tLoader, topicModels);



        System.exit(0);

    }

}
