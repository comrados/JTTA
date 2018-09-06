/*
 * Title: NaiveBayes.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */

/*
 * Title: NaiveBayes.java
 * Project: JTTA
 * Creator: Georgii Mikriukov
 * 2018
 */
package com.crawlergram.textclassification.naivebayes;

import com.crawlergram.textclassification.naivebayes.features.ChiSquare;
import com.crawlergram.textclassification.naivebayes.features.FeatureExtractor;
import com.crawlergram.textclassification.naivebayes.features.StatsCalculator;
import com.crawlergram.textclassification.naivebayes.structures.FeatureStats;
import com.crawlergram.textclassification.naivebayes.structures.NaiveBayesModel;
import com.crawlergram.textclassification.naivebayes.structures.TextDoc;
import com.fasterxml.jackson.core.*;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class NaiveBayes {

    private double chiSquareThreshold; // feature extraction threshold
    private NaiveBayesModel model;

    public NaiveBayes(){
        this.chiSquareThreshold = 6.63; //equivalent to pvalue 0.01
        this.model = null;
    }

    public NaiveBayes(double chiSquareThreshold, NaiveBayesModel model){
        this.chiSquareThreshold = chiSquareThreshold;
        this.model = model;
    }

    public NaiveBayes(double chiSquareThreshold){
        this.chiSquareThreshold = chiSquareThreshold;
        this.model = null;
    }

    public NaiveBayesModel getModel() {
        return model;
    }

    public void setModel(NaiveBayesModel model) {
        this.model = model;
    }

    public double getChiSquareThreshold() {
        return chiSquareThreshold;
    }

    public void setChiSquareThreshold(double chiSquareThreshold) {
        this.chiSquareThreshold = chiSquareThreshold;
    }

    /**
     * Preprocesses the original dataset and converts it to a List of Documents.
     *
     * @param trainingSet tokenized lists of several categories
     * @return
     */
    private List<TextDoc> preprocessTrainingSet(Map<String, List<List<String>>> trainingSet) {
        List<TextDoc> dataset = new ArrayList<>();

        for (Map.Entry<String, List<List<String>>> entry: trainingSet.entrySet()){
            String category = entry.getKey();
            List<List<String>> texts = entry.getValue();

            for (List<String> text: texts){
                TextDoc doc = new TextDoc(text);
                doc.category = category;
                dataset.add(doc);
            }
        }
        return dataset;
    }

    /**
     * Gathers the required counts for the features and performs feature selection
     * on the above counts. It returns a FeatureStats object that is later used
     * for calculating the probabilities of the model.
     *
     * @param docs documents
     * @return
     */
    private FeatureStats selectFeatures(List<TextDoc> docs) {
        FeatureExtractor featureExtractor = new ChiSquare.ChiSquareBuilder(chiSquareThreshold).build();
        //the FeatureStats object contains statistics about all the features found in the documents
        //extract the stats of the docs
        FeatureStats stats = new StatsCalculator().calculateFeatureStats(docs);
        //we pass this information to the feature selection algorithm and we get a list with the selected features
        Map<String, Double> selectedFeatures = featureExtractor.run(stats);
        //clip from the stats all the features that are not selected
        Iterator<Map.Entry<String, Map<String, Integer>>> it = stats.featureCategoryCount.entrySet().iterator();
        while (it.hasNext()) {
            String feature = it.next().getKey();
            if (!selectedFeatures.containsKey(feature)) {
                //if the feature is not in the selectedFeatures list remove it
                it.remove();
            }
        }
        return stats;
    }

    /**
     * Trains a Naive Bayes classifier by using the Multinomial Model by passing
     * the trainingSet and the prior probabilities.
     *
     * @param trainingSet tokenized lists of several categories
     * @throws IllegalArgumentException
     */
    public void train(Map<String, List<List<String>>> trainingSet) {
        //preprocess the given dataset
        List<TextDoc> dataset = preprocessTrainingSet(trainingSet);

        //produce the feature stats and select the best features
        FeatureStats featureStats = selectFeatures(dataset);

        //intiliaze the model of the classifier
        model = new NaiveBayesModel(featureStats);
        model.logPriors = estimatePriors(featureStats);

        // Laplace smoothing occurances calculation (also known as add-1)
        Map<String, Double> featureOccurrencesInCategory = laplaceSmoothingOccurances(featureStats);

        //estimate log likelihoods
        model.logLikelihoods = estimateLogLikelihoods(featureStats, featureOccurrencesInCategory);
    }

    /**
     * estimates prior probabilities
     *
     * @param featureStats calculated stats
     * @return
     */
    private Map<String, Double> estimatePriors(FeatureStats featureStats){
        Map<String, Double> priors = new HashMap<>();
        for (Map.Entry<String, Integer> entry : featureStats.categoryCounts.entrySet()) {
            String category = entry.getKey();
            Integer count = entry.getValue();
            priors.put(category, Math.log((double) count / model.n));
        }
        return priors;
    }

    /**
     * Laplace Smoothing occurances calculation (also known as add-1)
     *
     * @param featureStats calculated stats
     * @return
     */
    private Map<String, Double> laplaceSmoothingOccurances(FeatureStats featureStats){
        Map<String, Double> featureOccurrencesInCategory = new HashMap<>();

        for (String category : model.logPriors.keySet()) {
            Double featureOccSum = 0.0;
            for (Map<String, Integer> categoryListOccurrences : featureStats.featureCategoryCount.values()) {
                Integer occurrences = categoryListOccurrences.get(category);
                if (occurrences != null)
                    featureOccSum += occurrences;
            }
            featureOccurrencesInCategory.put(category, featureOccSum);
        }
        return featureOccurrencesInCategory;
    }

    /**
     * estimates log Likelihoods
     *
     * @param featureStats calculated stats
     * @param featureOccurrences Laplace Smoothing occurances
     * @return
     */
    private Map<String, Map<String, Double>> estimateLogLikelihoods(FeatureStats featureStats,
                                                                    Map<String, Double> featureOccurrences){
        Map<String, Map<String, Double>> logLLs = new HashMap<>();
        for (String category : model.logPriors.keySet()) {
            for (Map.Entry<String, Map<String, Integer>> entry : featureStats.featureCategoryCount.entrySet()) {
                String feature = entry.getKey();
                Map<String, Integer> featureCategoryCounts = entry.getValue();

                Integer count = featureCategoryCounts.get(category);
                if (count == null) count = 0;

                double logLikelihood = Math.log((count + 1.0) / (featureOccurrences.get(category) + model.d));
                if (!logLLs.containsKey(feature)) {
                    logLLs.put(feature, new HashMap<>());
                }
                logLLs.get(feature).put(category, logLikelihood);
            }
        }
        return logLLs;
    }

    /**
     * Predicts the category of a text by using an already trained classifier
     * and returns its category.
     *
     * @param text tokenized text to classify
     * @return
     */
    public Map<String, Double> classify(List<String> text) {
        if (!model.isValid()){
            System.err.println("Invalid knowledge base");
            return new HashMap<>();
        }
        TextDoc doc = new TextDoc(text);

        String maxScoreCategory = null;
        Double maxScore = Double.NEGATIVE_INFINITY;

        Map<String, Double> predictionScores = new HashMap<>();
        for (Map.Entry<String, Double> prior : model.logPriors.entrySet()) {
            String category = prior.getKey();
            Double logprob = prior.getValue(); //intialize the scores with the priors

            //foreach feature of the document
            for (Map.Entry<String, Integer> tokenCount : doc.tokenCounts.entrySet()) {
                String feature = tokenCount.getKey();

                if (!model.logLikelihoods.containsKey(feature))
                    continue; //if the feature does not exist in the knowledge base skip it

                Integer occurrences = tokenCount.getValue(); //get its occurrences in text
                logprob += occurrences * model.logLikelihoods.get(feature).get(category); //multiply loglikelihood score with occurrences
            }
            predictionScores.put(category, logprob);
        }
        return predictionScores; //return the category with heighest score
    }

    /**
     * saves model
     *
     * @param path path to file
     */
    public void saveModel(String path){
        if (model.isValid()){
            try {
                JsonFactory jFactory = new JsonFactory();
                JsonGenerator jGenerator = jFactory.createGenerator(new File(path), JsonEncoding.UTF8);
                jGenerator.writeStartObject();

                jGenerator.writeNumberField("n", model.n);
                jGenerator.writeNumberField("c", model.c);
                jGenerator.writeNumberField("d", model.d);

                writeLogPriors(jGenerator);
                writeLoglikelihoods(jGenerator);

                jGenerator.writeEndObject();
                jGenerator.close();
            } catch (IOException e) {
                System.out.println("Unable to save model");
                e.printStackTrace();
            }
        }
    }

    /**
     * writes log priors
     *
     * @param jGenerator JSON generator
     * @throws IOException
     */
    private void writeLogPriors(JsonGenerator jGenerator) throws IOException{
        jGenerator.writeFieldName("priors");
        jGenerator.writeStartObject();
        for (Map.Entry<String, Double> entry: model.logPriors.entrySet())
            jGenerator.writeNumberField(entry.getKey(), entry.getValue());
        jGenerator.writeEndObject();
    }

    /**
     * writes log likelihoods
     *
     * @param jGenerator JSON generator
     * @throws IOException
     */
    private void writeLoglikelihoods(JsonGenerator jGenerator) throws IOException{
        jGenerator.writeFieldName("likelihoods");
        jGenerator.writeStartObject();
        for (Map.Entry<String, Map<String, Double>> entry: model.logLikelihoods.entrySet()){
            jGenerator.writeFieldName(entry.getKey());
            jGenerator.writeStartObject();
            writeLoglikelihood(jGenerator, entry.getValue());
            jGenerator.writeEndObject();
        }
        jGenerator.writeEndObject();
    }

    /**
     * writes single log likelihood
     *
     * @param jGenerator JSON generator
     * @param likelihood entry
     * @throws IOException
     */
    private void writeLoglikelihood(JsonGenerator jGenerator, Map<String, Double> likelihood) throws IOException{
        for (Map.Entry<String, Double> entry: likelihood.entrySet())
            jGenerator.writeNumberField(entry.getKey(), entry.getValue());
    }

    /**
     * loads model from file
     *
     * @param path path to file
     */
    public void loadModel(String path){
        try {
            model = new NaiveBayesModel();
            JsonFactory jFactory = new JsonFactory();
            JsonParser jParser = jFactory.createParser(new File(path));
            // Continue until we find the end object
            while (!jParser.nextToken().equals(JsonToken.END_OBJECT)) {
                // Get the field name
                String fieldname = jParser.getCurrentName();
                if (fieldname != null) {
                    if (fieldname.equals("n")) {
                        jParser.nextToken();
                        model.n = jParser.getIntValue();
                    } else if (fieldname.equals("c")) {
                        jParser.nextToken();
                        model.c = jParser.getIntValue();
                    } else if (fieldname.equals("d")) {
                        jParser.nextToken();
                        model.d = jParser.getIntValue();
                    } else if (fieldname.equals("priors")) {
                        jParser.nextToken();
                        readLogPriors(jParser);
                    } else if (fieldname.equals("likelihoods")) {
                        jParser.nextToken();
                        readLogLikelihoods(jParser);
                    }
                }
            }
            jParser.close();
        } catch (Exception e) {
            System.out.println("Unable to load model");
            e.printStackTrace();
        }
    }

    /**
     * reads priors
     *
     * @param jParser parser
     * @throws IOException
     */
    private void readLogPriors(JsonParser jParser) throws IOException{
        while (!jParser.nextToken().equals(JsonToken.END_OBJECT)) {
            String category = jParser.getCurrentName();
            jParser.nextToken();
            Double prior = jParser.getDoubleValue();
            model.logPriors.put(category, prior);
        }
    }

    /**
     * reads likelihoods
     *
     * @param jParser parser
     * @throws IOException
     */
    private void readLogLikelihoods(JsonParser jParser) throws IOException{
        while (!jParser.nextToken().equals(JsonToken.END_OBJECT)) {
            String term = jParser.getCurrentName();
            jParser.nextToken();

            Map<String, Double> likelihoods = new HashMap<>();
            while (!jParser.nextToken().equals(JsonToken.END_OBJECT)) {
                String category = jParser.getCurrentName();
                jParser.nextToken();
                Double prior = jParser.getDoubleValue();
                likelihoods.put(category, prior);
            }
            if (!likelihoods.isEmpty())
                model.logLikelihoods.put(term, likelihoods);
        }
    }



}
