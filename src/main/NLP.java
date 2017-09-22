package main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * @author Carlos Chavez
 */
public class NLP {

    // ----------------------------------------------------
    // Attributes
    // ----------------------------------------------------

    public enum NLP_LIBRARY {
        MALLET
    }

    private static final String STOP_WORDS_PATH = "data/stopwords.txt";
    private String[] stopWords_;
    private NLP_LIBRARY nlp_library_ = NLP_LIBRARY.MALLET;
    private UtilsMallet utilsMallet_;
    private static Logger logger_ = LoggerFactory.getLogger(Utils.class);

    // ----------------------------------------------------
    // Methods
    // ----------------------------------------------------

    /**
     * @param nlp_library
     * @throws Exception
     */
    public NLP(NLP_LIBRARY nlp_library) throws Exception {

        // Load stop words
        loadStopWords();
        if (nlp_library == NLP_LIBRARY.MALLET) {
            utilsMallet_ = new UtilsMallet(stopWords_);
        }
        nlp_library_ = nlp_library;
    }

    /**
     * Loads the stop words
     *
     * @throws Exception
     */
    public void loadStopWords() throws Exception {
        BufferedReader brStopWordsInput = null;
        try {
            HashSet<String> stopWordsList_ = new HashSet<>();
            String inputLine;

            // Stop words
            if (Utils.validateFile(STOP_WORDS_PATH)) {
                // Load
                brStopWordsInput = Utils.getBufferedReader(STOP_WORDS_PATH);
                inputLine = brStopWordsInput.readLine();
                // Read lines
                while (inputLine != null) {
                    // Do not process empty lines or lines beginning with #
                    if (inputLine.isEmpty() || inputLine.startsWith("#")) {
                        inputLine = brStopWordsInput.readLine();
                        continue;
                    }
                    // Add line to list
                    stopWordsList_.add(inputLine.trim());
                    inputLine = brStopWordsInput.readLine();
                }
                stopWords_ = stopWordsList_.toArray(new String[stopWordsList_.size()]);
            } else
                logger_.error("File not found: " + STOP_WORDS_PATH);

        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                // Close files
                Utils.closeBufferedReader(brStopWordsInput);
            } catch (Exception ex) {
                // Don't do anything
            }
        }
    }

    /**
     * Trains and tests the model with a file
     *
     * @param filePath
     * @param removeFromString
     * @throws Exception
     */
    public void train(String filePath, int[] removeFromString) throws Exception {
        boolean[] boolRemoveFromString = Utils.convertToBooleanArray(removeFromString);
        utilsMallet_.train(filePath, boolRemoveFromString);
    }

    /**
     * Evaluates a new value using a saved model and instance pipe
     *
     * @param testValue
     * @throws Exception
     */
    public void test(String testValue) throws Exception {
        utilsMallet_.testNewInstance(testValue);
    }
}
