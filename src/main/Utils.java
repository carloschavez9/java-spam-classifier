package main;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * General Utilities
 */
public class Utils {

    /**
     * Validates if the file or directory exists in the given path
     *
     * @param path Path of the file
     * @return
     */
    public static boolean validateFile(String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     * Returns a BufferedReader given a path
     *
     * @param path Path of the file
     * @return
     * @throws FileNotFoundException
     */
    public static BufferedReader getBufferedReader(String path) throws FileNotFoundException {
        return new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
    }

    /**
     * Closes a BufferedReader
     *
     * @param br BufferedReader to use
     * @throws IOException
     */
    public static void closeBufferedReader(BufferedReader br) throws IOException {
        if (br != null)
            br.close();
    }

    /**
     * Converts an integer
     *
     * @param array
     * @return
     * @throws Exception
     */
    public static boolean[] convertToBooleanArray(int[] array) throws Exception {
        boolean[] booleanArray = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 0)
                booleanArray[i] = false;
            else if (array[i] == 1)
                booleanArray[i] = true;
            else
                throw new Exception("Invalid value. Must be 0 or 1");
        }
        return booleanArray;
    }
}
