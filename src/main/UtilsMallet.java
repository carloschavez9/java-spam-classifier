package main;

import cc.mallet.classify.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;
import cc.mallet.util.Randoms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author Carlos Chavez
 */
public class UtilsMallet {

    // ----------------------------------------------------
    // Attributes
    // ----------------------------------------------------

    private static final String DATA_MODEL_MALLET = "models/model_mallet.dat";
    private static final String DATA_MODEL_INSTANCES_MALLET = "instances/instances_mallet.dat";
    private static final String EMAIL_REGEX = "([a-zA-Z0-9=*!$&_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)";
    private static final String URL_REGEX = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
    private static final int REMOVE_EMAIL = 0;
    private static final int REMOVE_URL = 1;
    private static final int REMOVE_NON_ALPHA = 2;
    private static final int REMOVE_STOP_WORDS = 3;
    private static final int K_FOLD_CROSSVALIDATION = 10;
    private static final int TRAINING = 0;
    private static final int TESTING = 1;

    private TokenSequenceRemoveStopwords tokenSequenceRemoveStopwords_;
    static Logger logger_ = LoggerFactory.getLogger(UtilsMallet.class);

    // ----------------------------------------------------
    // Methods
    // ----------------------------------------------------

    public UtilsMallet(String[] stopWords) {
        tokenSequenceRemoveStopwords_ = new TokenSequenceRemoveStopwords(false, false);
        tokenSequenceRemoveStopwords_.addStopWords(stopWords);
    }

    /**
     * Gets an instance list from a csv file
     *
     * @param filePath
     * @return
     * @throws FileNotFoundException
     */
    public InstanceList getInstanceListFromCsv(String filePath, boolean[] removeFromString) throws FileNotFoundException {
        ArrayList<Pipe> pipeList = new ArrayList<>();

        // Pipes: tokenize, map to features
        pipeList.add(new CharSequenceLowercase());

        if (removeFromString[REMOVE_EMAIL])
            pipeList.add(new CharSequenceReplace(Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE), ""));
        if (removeFromString[REMOVE_URL])
            pipeList.add(new CharSequenceReplace(Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE), ""));

        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("[\\p{L}\\p{N}]+")));

        if (removeFromString[REMOVE_NON_ALPHA])
            pipeList.add(new TokenSequenceRemoveNonAlpha());
        if (removeFromString[REMOVE_STOP_WORDS])
            pipeList.add(tokenSequenceRemoveStopwords_);

        pipeList.add(new TokenSequence2FeatureSequence());
        pipeList.add(new Target2Label());
        pipeList.add(new FeatureSequence2FeatureVector());

        InstanceList instances = new InstanceList(new SerialPipes(pipeList));
        Reader fileReader = Utils.getBufferedReader(filePath);
        instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s](.*)$"), //(\S*)[\s,]*(\S*)[\s,]*(.*)
                2, 1, 0)); // data, label, name fields

        return instances;
    }

    /**
     * Gets instance list from a string array
     *
     * @param array
     * @param removeEmail
     * @param removeURL
     * @param removeNonAlpha
     * @param removeStopWords
     * @return
     */
    public InstanceList getInstanceListFromStringArray(String[] array, boolean removeEmail, boolean removeURL, boolean removeNonAlpha, boolean removeStopWords) {
        ArrayList<Pipe> pipeList = new ArrayList<>();

        // Tokenize raw strings
        pipeList.add(new CharSequenceLowercase());

        if (removeEmail)
            pipeList.add(new CharSequenceReplace(Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE), ""));
        if (removeURL)
            pipeList.add(new CharSequenceReplace(Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE), ""));

        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("[\\p{L}\\p{N}]+")));

        if (removeNonAlpha)
            pipeList.add(new TokenSequenceRemoveNonAlpha());
        if (removeStopWords)
            pipeList.add(tokenSequenceRemoveStopwords_);

        pipeList.add(new TokenSequence2FeatureSequence());
        pipeList.add(new FeatureSequence2FeatureVector());

        InstanceList instances = new InstanceList(new SerialPipes(pipeList));
        instances.addThruPipe(new StringArrayIterator(array));

        logger_.info("Number of instances: " + instances.size());

        return instances;
    }

    /**
     * Trains instances using K fold cross validation and saves the model
     *
     * @param instances
     * @throws IOException
     */
    private void trainInstances(InstanceList instances) throws IOException {
        // Shuffle list
        instances.shuffle(new Randoms());
        double meanAccuracy = 0;

        // K fold cross validation
        logger_.info(String.format("%d fold cross validation", K_FOLD_CROSSVALIDATION));
        InstanceList.CrossValidationIterator crossValidationIterator = instances.crossValidationIterator(K_FOLD_CROSSVALIDATION);
        int i = 0;
        while (crossValidationIterator.hasNext()) {
            InstanceList[] instancesSplit = crossValidationIterator.next();
            Classifier classifier = trainClassifierNaiveBayes(instancesSplit[TRAINING]);
            Trial trial = new Trial(classifier, instancesSplit[TESTING]);
            double accuracy = trial.getAccuracy();
            meanAccuracy += accuracy;

//            for (Instance a : instancesSplit[TESTING]) {
//                logger_.info(a.getData());
//            }

            logger_.info(String.format("Fold %d, Accuracy %.5f", i + 1, accuracy));
            i++;
        }
        meanAccuracy = meanAccuracy / K_FOLD_CROSSVALIDATION;
        logger_.info(String.format("Mean Accuracy %.5f", meanAccuracy));

        // Trains the classifier with all data and saves model and instance pipe
        Classifier classifier = trainClassifierNaiveBayes(instances);
        saveModel(classifier, instances);
    }

    /**
     * Trains model using Naive Bayes
     *
     * @param trainingInstances
     * @return
     */
    private Classifier trainClassifierNaiveBayes(InstanceList trainingInstances) {
        ClassifierTrainer trainer = new NaiveBayesTrainer();
        return trainer.train(trainingInstances);
    }

    /**
     * Trains and tests the model with a file
     *
     * @param filePath
     * @param removeFromString
     * @throws Exception
     */
    public void train(String filePath, boolean[] removeFromString) throws Exception {
        InstanceList instances = getInstanceListFromCsv(filePath, removeFromString);
        trainInstances(instances);
    }

    /**
     * Evaluates a new value using a saved model and instance pipe
     *
     * @param value
     * @throws Exception
     */
    public void testNewInstance(String value) throws Exception {
        Classifier classifier = loadModel();
        InstanceList instances = loadInstancesList();

        // Load pipe for new instance
        InstanceList testing = new InstanceList(instances.getPipe());
        Instance newInstance = new Instance(value, "?", "Test Instance", null);
        testing.addThruPipe(newInstance);

        // Evaluate
        Labeling labeling = classifier.classify(newInstance).getLabeling();
        logger_.info("Best label: " + labeling.getBestLabel().toString());
    }

    /**
     * Saves a model to a file
     *
     * @param classifier
     * @param instances
     * @throws IOException
     */
    private void saveModel(Classifier classifier, InstanceList instances) throws IOException {
        // Save model
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(DATA_MODEL_MALLET)));
        oos.writeObject(classifier);
        oos.close();
        // Save instances
        instances.save(new File(DATA_MODEL_INSTANCES_MALLET));
    }

    /**
     * Loads a saved model
     *
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Classifier loadModel() throws IOException, ClassNotFoundException {
        Classifier classifier;
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(DATA_MODEL_MALLET)));
        classifier = (Classifier) ois.readObject();
        ois.close();

        return classifier;
    }

    /**
     * Loads a saved Instance List
     *
     * @return
     */
    private InstanceList loadInstancesList() {
        return InstanceList.load(new File(DATA_MODEL_INSTANCES_MALLET));
    }
}
