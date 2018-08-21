/*
 * Title: testld.java
 * Project: JTTE
 * Creator: Georgii Mikriukov
 * 2018
 */

import com.crawlergram.db.DBStorageReduced;
import com.crawlergram.db.mongo.MongoDBStorageReduced;
import com.crawlergram.preprocess.Tokenizer;
import com.crawlergram.preprocess.liga.LIGA;
import com.crawlergram.structures.TDialog;
import com.crawlergram.structures.message.TEMessage;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class testld {

    public static String detectLanguage(String text, LanguageDetector detector) {
            LanguageResult result = detector.detect(text);
            return result.getLanguage();
    }

    public static void main(String[] args) {

        String ligaModel = "res" + File.separator + "liga" + File.separator + "model_n3.liga";
        LIGA liga = new LIGA().setLogLIGA(true).setMaxSearchDepth(5000).setThreshold(0.5).setN(3).loadModel(ligaModel);
        DBStorageReduced dbStorage = new MongoDBStorageReduced("telegramJ", "telegram", "cart", "localhost", 27017, "fs");

        Set<String> ltl = new HashSet<>();
        ltl.add("de");
        ltl.add("en");
        ltl.add("es");
        ltl.add("fr");
        ltl.add("it");
        ltl.add("nl");

        LanguageDetector detector = null;
        try {
            detector = new OptimaizeLangDetector().loadModels(ltl);
        } catch (IOException e){}

        /*
        String ru = "Сука, блядь, пидор. Винишь ли ты меня в слепоте мысли, пидор, сука, блядь? " +
                "Я, блядь, вижу, сука, образ, блядь, пидорас, являющийся выражением, ёбаный твой рот, эмоций, блядь. " +
                "Понимаешь меня? Блядь, да проще тебе ебало набить.";
        String ru2 = Tokenizer.preprocess(ru);

        String en = "And I will strike down upon thee with great vengeance and furious anger those who attempt to poison and destroy my brothers." +
                "And you will know my name is the Lord when I lay my vengeance upon you.";
        String en2 = Tokenizer.preprocess(en);

        String en3 = "Hey dude";

        System.out.println(detectLanguage(ru2));

        System.out.println(detectLanguage(en2));
        System.out.println(liga.classify(en2));

        System.out.println(detectLanguage(en3));
        System.out.println(liga.classify(en3));
        */

        List<TDialog> dialogs = dbStorage.getDialogs();
        TDialog dialog = dialogs.get(1);

        List<TEMessage> msgs = TEMessage.topicExtractionMessagesFromMongoDocuments(dbStorage.readMessages(dialog));

        removeEmptyMessages(msgs);
        msgs = Tokenizer.tokenizeMessages(msgs);

        getMessageLanguages(msgs, liga);

        int co = 0;
        int col = 0;
        int cot = 0;
        int nonl = 0;
        int nont = 0;

        for (TEMessage msg: msgs){
            String ligaLang = msg.getBestLang();
            String tikaLang = detectLanguage(msg.getText(), detector);

            if (tikaLang == null) tikaLang = "";


            if (ligaLang.equals(tikaLang)){
                co++;
            }
            if (ligaLang.equals("en")){
                col++;
            }
            if (tikaLang.equals("en")){
                cot++;
            }
            if (ligaLang.equals("UNKNOWN")){
                nonl++;
            }
            if (tikaLang.equals("")){
                nont++;
            }
            System.out.println(msg.getBestLang() + "\t" + detectLanguage(msg.getText(), detector) + "\t" + msg.getClearText());
        }
        System.out.println();
        System.out.println("Total msgs: " + msgs.size());
        System.out.println("Coincidences: " + co);
        System.out.println();
        System.out.println("Liga == 'en': " + col);
        System.out.println("Tika == 'en': " + cot);
        System.out.println();
        System.out.println("Liga == null: " + nonl);
        System.out.println("Tika == null: " + nont);
    }

    private static void removeEmptyMessages(List<TEMessage> msgs) {
        for (int i = 0; i < msgs.size(); i++) {
            if (msgs.get(i).getText().isEmpty()) {
                msgs.remove(i);
            }
        }
    }

    private static void getMessageLanguages(List<TEMessage> msgs, LIGA liga){
        for (TEMessage msg: msgs)
            msg.setLangs(liga.classify(msg.getClearText()));
    }

}
