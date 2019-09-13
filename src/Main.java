import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;

public class Main {

    public static void main(String[] args) {
        final Path dir = Paths.get("data", "generatedSequences", "default");
        final Charset charset = Charset.forName("UTF-8");

        final Path resultFile = Paths.get("data", "sortingTimes", "default", "results.csv");

        final int numberOfComparisons = 3; //can be changed as we wish
        final int numberOfExperiments = 10;
        final int count = 100; //number of time measurement per experiment

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                dir, "*.dat");
             PrintWriter writer = new PrintWriter(
                     Files.newBufferedWriter(resultFile, charset))
        ) {

            writer.println("id,method,dataType,genType,size,time(ns),sdev,misc");

            //read all files
            for (Path entry : stream) {
                try (BufferedReader reader
                             = Files.newBufferedReader(entry, charset)) {
                    String line = reader.readLine();
                    if (line != null) {
                        String id = readStringValue(line, "id");
                        String type = readStringValue(line, "dataType");
                        String result = "," + type;
                        String genType = readStringValue(line, "genType"); //uniform //inverse
                        result += "," + genType;
                        int size = readIntValue(line, "size");
                        result += "," + size;
                        String misc = ",{";
                            misc += "maxWordLength=" + readIntValue(line,
                                    "maxWordLength");
                        if (genType.equals("similar")) { // check for similarity coefficient
                            misc += ";simCoeff=" + readIntValue(line, "simCoeff");
                        }
                        misc += ";seed=" + readStringValue(line, "seed");
                        misc += "}";

                        //get read the file to get the data array for testing
                        String[] dataString = new String[size];
                        for (int i = 0; i < size; i++) {
                            dataString[i] = reader.readLine();
                        }

                        //write the PQ test results
                        double[][] resultsPQ = getPQTestResults(numberOfComparisons, dataString, numberOfExperiments, count);
                        writeTestResultsToFile(writer, id, "pq", resultsPQ, result, misc);

                        //write the Collections.sort() test results
                        double[][] resultsSort = getSortTestResults(numberOfComparisons, dataString, numberOfExperiments, count);
                        writeTestResultsToFile(writer, id, "sort", resultsSort, result, misc);
                    }
                }
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    private static void writeTestResultsToFile(PrintWriter writer, String id, String resType,
                                               double[][] results, String result, String misc) {
        writer.print(id + "," + resType);
        writer.print(result);
        writer.printf(",%1.1f,%1.3f",
                results[0][0], results[0][1]);
        writer.println(misc);
    }

    private static double[][] getPQTestResults(int numberOfComparisons, String[] dataString, int n, int count) {
        double[][] resultsPQ = new double[numberOfComparisons][2]; //two dimensional array to store mean values and standard deviations of the results
        double[] tmp = new double[2]; // to keep a result - mean and standard deviation
        for (int i = 0; i < numberOfComparisons; i++) {
            testTimeStringPQ(dataString, tmp, n, count);
            resultsPQ[i][0] = tmp[0];
            resultsPQ[i][1] = tmp[1];
        }
        Arrays.sort(resultsPQ,
                (a, b) -> Double.compare(a[1], b[1])); // sort based on standard deviation
        return resultsPQ;
    }

    private static double[][] getSortTestResults(int numberOfComparisons, String[] dataString, int n, int count) {
        double[][] resultsPQ = new double[numberOfComparisons][2]; //two dimensional array to store mean values and standard deviations of the results
        double[] tmp = new double[2]; // to keep a result - mean and standard deviation
        for (int i = 0; i < numberOfComparisons; i++) {
            testTimeStringSort(dataString, tmp, n, count);
            resultsPQ[i][0] = tmp[0];
            resultsPQ[i][1] = tmp[1];
        }
        Arrays.sort(resultsPQ,
                (a, b) -> Double.compare(a[1], b[1])); // sort based on standard deviation
        return resultsPQ;
    }

    private static String readStringValue(String line, String name) {
        int pos = line.indexOf(name + "=");
        int endPos = line.indexOf(", ", pos);
        if (endPos < 0) {
            endPos = line.length();
        }
        return line.substring(pos + name.length() + 1, endPos);
    }

    private static int readIntValue(String line, String name) {
        String value = readStringValue(line, name);
        return Integer.parseInt(value);
    }

    private static String testTimeStringPQ(String[] arr, double[] results, int n, int count) {
        String dummy = "";
        double st = 0.0, sst = 0.0;
        for (int j = 0; j < n; j++) {
            String[] seq = Arrays.copyOf(arr, arr.length);

            Timer t = new Timer();
            for (int i = 0; i < count; i++) {
                PriorityQueue<String> pq = new PriorityQueue(Arrays.asList(seq));
                dummy += pq.poll();
            }
            double time = t.check() / count;
            st += time;
            sst += time * time;
        }
        double mean = st / n;
        double sdev = Math.sqrt((sst - mean * mean * n) / (n - 1));
        results[0] = mean;
        results[1] = sdev;
        return dummy;
    }

    private static String testTimeStringSort(String[] arr, double[] results, int n, int count) {
        String dummy = "";
        double st = 0.0, sst = 0.0;
        for (int j = 0; j < n; j++) {
            String[] seq = Arrays.copyOf(arr, arr.length);

            Timer t = new Timer();
            for (int i = 0; i < count; i++) {
                Collections.sort(Arrays.asList(seq));
                dummy += seq[0];
            }
            double time = t.check() / count;
            st += time;
            sst += time * time;
        }
        double mean = st / n;
        double sdev = Math.sqrt((sst - mean * mean * n) / (n - 1));
        results[0] = mean;
        results[1] = sdev;
        return dummy;
    }
}