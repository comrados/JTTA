/*
 * Title: testld2.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

import com.crawlergram._old.preprocess.Tokenizer;
import com.crawlergram.db.DBStorageReduced;
import com.crawlergram.db.mongo.MongoDBStorageReduced;
import com.crawlergram.preprocessing.liga.LIGA;
import com.crawlergram.structures.dialog.TDialog;
import com.crawlergram.structures.message_old.TEMessage;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

import java.io.*;
import java.util.*;

public class testld2 {

    public static String detectLanguage(String text, LanguageDetector detector) {
            LanguageResult result = detector.detect(text);
            return result.getLanguage();
    }

    public static void main(String[] args) {

        String ligaModel = "res" + File.separator + "liga" + File.separator + "model_n3.liga";
        LIGA liga = new LIGA.LIGABuilder(0.5).setLogLIGA(true).setMaxSearchDepth(5000).build();
        liga.loadModel(ligaModel);

        Set<String> ltl = new HashSet<>();
        ltl.add("de"); ltl.add("en"); ltl.add("es"); ltl.add("fr"); ltl.add("it"); ltl.add("nl");

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

        String dir = "I:\\Work\\datasets\\liga_publication_dataset";
        DataLoader dl = new DataLoader.DataLoaderBuilder(dir).setInitDataPart(0).setTestDataPart(0.5).build();
        dl.readFilesUpper();
        Random r = new Random();
        int shufflings = 25;

        int iters = 100;

        List<Double> cs = new ArrayList<>();
        List<Double> cos = new ArrayList<>();
        List<Double> cols = new ArrayList<>();
        List<Double> cots = new ArrayList<>();
        List<Double> procls = new ArrayList<>();
        List<Double> procts = new ArrayList<>();

        for (int i= 0; i < iters; i++){
            System.out.println("Iteration " + (i+1));

            List<MutablePair<String, String>> copy = new ArrayList<>(dl.dataset);

            for (int j = 0; j <= shufflings; j++){
                long seed = r.nextLong();
                Collections.shuffle(copy, new Random(seed));
            }

            List<MutablePair<String, String>> train = dl.splitTop(copy, 0.25);
            List<MutablePair<String, String>> test = dl.splitBot(copy, 0.25);

            liga.addDataset(train, 3);

            double c = 0;
            double co = 0;
            double col = 0;
            double cot = 0;

            for (MutablePair<String, String> p: test){
                String ligaLang = getLigaLang(p.getRight(), liga);
                String tikaLang = detectLanguage(p.getRight(), detector);
                c++;
                if (ligaLang.equals(tikaLang)) co++;
                if (p.getLeft().equals(ligaLang)) col++;
                if (p.getLeft().equals(tikaLang)) cot++;
            }

            cs.add(c);
            cos.add(co);
            cols.add(col);
            cots.add(cot);
            procls.add(col/c);
            procts.add(cot/c);

            liga.dropModel();

        }

        toCsv("out.csv", ";", cs, cols, cots, procls, procts);

        double meanl = mean(procls);
        double meant = mean(procts);
        double varl = variance(procls);
        double vart = variance(procts);

        System.out.println();
        /*
        System.out.println("LogLIGA: " + liga.isLogLIGA());
        System.out.println();
        System.out.println("Total samples: " + c);
        System.out.println("Coincidences: " + co);
        System.out.println();
        System.out.println("Liga == True: " + col + String.format(" %.2f", (double) col/c * 100));
        System.out.println("Tika == True: " + cot + String.format(" %.2f", (double) cot/c * 100));
        System.out.println();
        System.out.println("Liga == null: " + nonl + String.format(" %.2f", (double) nonl/c * 100));
        System.out.println("Tika == null: " + nont + String.format(" %.2f", (double) nont/c * 100));
        */
    }

    private static String getLigaLang(String s, LIGA liga){
        Double bestScore = -1.0;
        String bestLang = "UNKNOWN";

        Map<String, Double> scores = liga.classifyAll(s, 3);

        // Get the best score or return unknown
        for (Map.Entry<String, Double> score : scores.entrySet()) {
            if (score.getValue() > bestScore && score.getValue() > liga.getThreshold()) {
                bestScore = score.getValue();
                bestLang = score.getKey();
            }
        }

        // Return best scoring language
        return bestLang;
    }

    private static void getMessageLanguages(List<TEMessage> msgs, LIGA liga){
        for (TEMessage msg: msgs)
            msg.setLangs(liga.classifyAll(msg.getClearText(), 3));
    }

    private static double sum(List<Double> nums){
        double x = 0;
        for (Double num: nums) x += num;
        return x;
    }

    private static double mean(List<Double> nums){
        double x = sum(nums)/nums.size();
        return x;
    }

    private static double variance(List<Double> nums){
        double x = 0.0;
        double m = mean(nums);
        for (Double num: nums) x += Math.pow(num - m, 2);
        return x / (nums.size() - 1);
    }

    private static void toCsv(String path, String sep, List<Double> cs,  List<Double> cols,  List<Double> cots,  List<Double> procls,  List<Double> procts) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(path));

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            //header
            bw.write("samples" + sep + "liga=TRUE" + sep + "tika=TRUE" + sep + "liga=TRUE %" + sep + "tika=TRUE %");
            bw.newLine();

            for (int i = 0; i < cs.size(); i++){
                String line = cs.get(i) + sep + cols.get(i) + sep + cots.get(i) + sep + procls.get(i) + sep + procts.get(i);
                bw.write(line);
                bw.newLine();
                bw.flush();
            }

            bw.newLine();
            bw.write(mean(cs) + sep + mean(cols) + sep + mean(cots) + sep + mean(procls) + sep + mean(procts));
            bw.newLine();
            bw.write(variance(cs) + sep + variance(cols) + sep + variance(cots) + sep + variance(procls) + sep + variance(procts));

            bw.close();

        } catch (IOException e) {
            System.err.println("Unable to write " + path);
            System.err.println(e.getMessage());
        }

    }

}
