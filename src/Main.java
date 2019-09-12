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

        final Path resultFile = Paths.get("data", "sortingTimes", "default",
                "results.csv");
        final Path horseRaceResults = Paths.get("data", "sortingTimes", "default",
                "horseRace.csv");

        final int numberOfComparisons = 3; //can be changed as we wish
        
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                    dir, "*.dat"); //read all files
            PrintWriter writer = new PrintWriter(
                    Files.newBufferedWriter(resultFile, charset));
            PrintWriter writerHR = new PrintWriter(Files.newBufferedWriter(
                    horseRaceResults, charset))) {
            
            writer.println("id,method,dataType,genType,size,time(ns),sdev,misc");
            writerHR.println("id,dataType,genType,size,winner,misc");
            
            for (Path entry : stream) {
                try (BufferedReader reader
                        = Files.newBufferedReader(entry, charset)) {
                    String line = reader.readLine();
                    if (line != null) {
                        String id = readStringValue(line, "id");                        
                        String type = readStringValue(line, "dataType");
                        boolean typeIsInt = type.equals("int"); //if not it's String
                        String result = "," + type ;
                        String genType = readStringValue(line, "genType"); //uniform //inverse
                        result += "," + genType;
                        int size = readIntValue(line, "size");
                        result += "," + size;
                        String misc = ",{";
                        if (typeIsInt) {
                            misc += "bound=" + readIntValue(line, "bound");
                        } else {
                            misc += "maxWordLength=" + readIntValue(line, 
                                    "maxWordLength");
                        }
                        if (genType.equals("similar")) { // check for similarity coefficient
                            misc += ";simCoeff=" + readIntValue(line, "simCoeff");
                        }
                        misc += ";seed=" + readStringValue(line, "seed");
                        misc += "}";
                        
                        double[] tmp = new double[2]; // to keep result (mean) and standard deviation
                        double[][] resultsPQ = new double[numberOfComparisons][2]; //two dimensional array to store mean values and standard deviations of the results
                        double[][] resultsSort = new double[numberOfComparisons][2];
                        
                        if (typeIsInt) {
                            int[] dataInt = new int[size];
                            for (int i = 0; i < size; i++) {
                                line = reader.readLine();
                                dataInt[i] = Integer.parseInt(line);
                            }
                                                        
                            writer.print(id + ",pq");
                            writerHR.print(id);
                            for(int i = 0; i < numberOfComparisons; i++){
                                testTimeIntPQ(dataInt, tmp);
                                resultsPQ[i][0] = tmp[0];
                                resultsPQ[i][1] = tmp[1];
                            }
                            Arrays.sort(resultsPQ, 
                                    (a,b) -> Double.compare(a[1], b[1])); // elements are currently sorted by standard deviation
                            writer.print(result); // result - info about data set
                            writerHR.print(result); //
                            writer.printf(",%1.1f,%1.3f", 
                                    resultsPQ[0][0], resultsPQ[0][1]); // write with the smallest standard deviation to avoid garbage collector issues
                            writer.println(misc); // rest of the info

                            //write for tests for collections.sort
                            writer.print(id + ",sort");
                            for(int i = 0; i < numberOfComparisons; i++){
                                testTimeIntSort(dataInt, tmp);
                                resultsSort[i][0] = tmp[0];
                                resultsSort[i][1] = tmp[1];
                            }
                            Arrays.sort(resultsSort, 
                                    (a,b) -> Double.compare(a[1], b[1]));
                            writer.print(result);
                            writer.printf(",%1.1f,%1.3f", 
                                    resultsSort[0][0], resultsSort[0][1]);
                            writer.println(misc);


                            int horseRaceResult = 0;
                            //numberOfComparisons = 3
                            for (int i = 0; i < numberOfComparisons; i++) {
                                if (resultsPQ[i][0] < resultsSort[i][0]) { // comparisons are based on standard deviations, but for mean
                                    horseRaceResult--;
                                } else {
                                    horseRaceResult++;
                                }
                            }
                            if (horseRaceResult < 0) { //which are faster
                                writerHR.print(",pq");
                            } else if (horseRaceResult > 0) {
                                writerHR.print(",sort");
                            } else {
                                writerHR.print(",none");
                            }
                            writerHR.println(misc);
                            
                            
                        } else {
                            String[] dataString = new String[size];
                            for (int i = 0; i < size; i++) {
                                dataString[i] = reader.readLine();
                            }
                            
                            writer.print(id + ",pq");
                            writerHR.print(id);
                            for(int i = 0; i < numberOfComparisons; i++){
                                testTimeStringPQ(dataString, tmp);
                                resultsPQ[i][0] = tmp[0];
                                resultsPQ[i][1] = tmp[1];
                            }
                            Arrays.sort(resultsPQ, 
                                    (a,b) -> Double.compare(a[1], b[1]));
                            writer.print(result);
                            writerHR.print(result);
                            writer.printf(",%1.1f,%1.3f", 
                                    resultsPQ[0][0], resultsPQ[0][1]);
                            writer.println(misc);
                            
                            writer.print(id + ",sort");
                            for(int i = 0; i < numberOfComparisons; i++){
                                testTimeStringSort(dataString, tmp);
                                resultsSort[i][0] = tmp[0];
                                resultsSort[i][1] = tmp[1];
                            }
                            Arrays.sort(resultsSort, 
                                    (a,b) -> Double.compare(a[1], b[1]));
                            writer.print(result);
                            writer.printf(",%1.1f,%1.3f", 
                                    resultsSort[0][0], resultsSort[0][1]);
                            writer.println(misc);
                            
                            int horseRaceResult = 0;                            
                            for (int i = 0; i < numberOfComparisons; i++) {
                                if (resultsPQ[i][0] < resultsSort[i][0]) {
                                    horseRaceResult--;
                                } else {
                                    horseRaceResult++;
                                }
                            }
                            if (horseRaceResult < 0) {
                                writerHR.print(",pq");
                            } else if (horseRaceResult > 0) {
                                writerHR.print(",sort");
                            } else {
                                writerHR.print(",none");
                            }
                            writerHR.println(misc);
                        }
                        
                    }

                } 
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    // get 1st line input
    private static int readIntValue(String line, String name) {
        int pos = line.indexOf(name + "=");
        int endPos = line.indexOf(", ", pos);
        if (endPos < 0) {
            endPos = line.length();
        }
        return Integer.parseInt(line.substring(
                pos + name.length() + 1, endPos));        
    }
    
    private static String readStringValue(String line, String name) {
        int pos = line.indexOf(name + "=");
        int endPos = line.indexOf(", ", pos);
        if (endPos < 0) {
            endPos = line.length();
        }
        return line.substring(pos + name.length() + 1, endPos);
    }

    //arr data from the file
    private static int testTimeIntPQ(int[] arr, double[] results) {        
        int n = 10; // repeat time checking for 10 time for the same var
        int count = 100;
        int dummy = 0;
        double st = 0.0, sst = 0.0;
        for (int j = 0; j < n; j++) {
            //create copy of array to test
            Integer[] seq = new Integer[arr.length];
            for (int i = 0; i < arr.length; i++) {
                seq[i] = arr[i];
            }
            
            Timer t = new Timer();
            //mark4 from benchamarking.java, check count changing with 1
            for (int i = 0; i < count; i++) {
                PriorityQueue<Integer> pq = new PriorityQueue(Arrays.asList(seq));
                dummy += pq.poll();
            }
            double time = t.check() / count;
            st += time;
            sst += time * time;                        
        }
        double mean = st / n;
        double sdev = Math.sqrt((sst - mean * mean * n) / (n - 1)); //as from parallel lecture we know that this can not work for very big numbers
        results[0] = mean;
        results[1] = sdev;
        return dummy;
    }
    
    private static int testTimeIntSort(int[] arr, double[] results) {        
        int n = 10; // that's how many runs we have
        int count = 100; // that's how many times timer runs for single run
        int dummy = 0;
        double st = 0.0, sst = 0.0;
        for (int j = 0; j < n; j++) {
            Integer[] seq = new Integer[arr.length];
            for (int i = 0; i < arr.length; i++) {
                seq[i] = arr[i];
            }
            
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
    
    private static String testTimeStringPQ(String[] arr, double[] results) {        
        int n = 10;
        int count = 100;
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
    
    private static String testTimeStringSort(String[] arr, double[] results) {        
        int n = 10;
        int count = 100;
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


