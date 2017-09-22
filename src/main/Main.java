package main;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Carlos Chavez
 */
public class Main {

    // ----------------------------------------------------
    // Attributes
    // ----------------------------------------------------

    private static final String DATA_FILE_PATH = "data/SMSSpamCollection.txt";
    private static Logger logger_ = LoggerFactory.getLogger(Utils.class);

    @Option(name = "-t", usage = "runs in training mode")
    private boolean trainingMode_ = false;
    @Option(name = "-f", usage = "training file name")
    private String trainingFile_ = DATA_FILE_PATH;
    @Option(name = "-r", usage = "Indicates what to remove from tokens. 1 removes, 0 doesn't. [EMAIL, URL, NON_ALPHA, STOP_WORDS]. ie: [0,0,1,1]")
    private int[] removeFromString_ = new int[]{0, 0, 0, 0};
    @Option(name = "-m", usage = "text to evaluate")
    private String testValue_ = "";

    // ----------------------------------------------------
    // Methods
    // ----------------------------------------------------

    public void doMain(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            // parse the arguments
            parser.parseArgument(args);
            // Validate
            if (trainingMode_ && trainingFile_.isEmpty())
                throw new CmdLineException(parser, "Training file (-f) must be set in training mode", null);
            if (!trainingMode_ && testValue_.isEmpty())
                throw new CmdLineException(parser, "Test value (-m) must be set for testing", null);

        } catch (CmdLineException e) {
            logger_.error(e.getMessage());
            logger_.error("java Main [options...]");
            // print the list of available options
            parser.printUsage(System.err);
            return;
        }

        try {
            if (trainingMode_) {
                train(trainingFile_);
            } else {
                test(testValue_);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Trains the model
     *
     * @param trainingFileName dataset for training and testing
     * @throws Exception
     */
    private void train(String trainingFileName) throws Exception {
        logger_.info("Training started...");
        NLP nlp = new NLP(NLP.NLP_LIBRARY.MALLET);
        nlp.train(trainingFileName, removeFromString_);
        logger_.info("Done");
    }

    /**
     * Tests the model for topic modelling using a test Value
     *
     * @param testValue
     * @throws Exception
     */
    private void test(String testValue) throws Exception {
        logger_.info("Testing started...");
        NLP nlp = new NLP(NLP.NLP_LIBRARY.MALLET);
        nlp.test(testValue);
        logger_.info("Done");
    }

    /**
     * Main execution
     *
     * @param args Arguments
     */
    public static void main(String[] args) {
        new Main().doMain(args);
    }
}
