import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

/**
 * Class <b>DataGen</b> provides few static methods that generate files
 * containing unsorted sequences of given length, element type (int / String)
 * and generation type (uniform, many equal elements, inversely sorted)
 */
public class DataGen {
    private static final Charset charset = Charset.forName("UTF-8");

    public static void main(String[] args) {
        final Path dir = Paths.get("data", "generatedSequences", "default");
        //seeds make sure that we get the same random
        final int mainSeed = 2938474; // just a random number we chose for a seed :D
        final int numberOfSeeds = 3; // 3 different seeds sets
        final int firstSize = 2; // first file will have 2 (1024) ints/Strings
        final int numberOfDoublings = 15; //so max number will be 2048(1 048 576)
        final int[] maxWordLengths = {2, 10, 100};
        final int[] similarityCoefficients = {1, 3}; // so probability coefficient can be 1/2 or 1/4, when we create a word, every next letter of the word will have
                                                     // 1 / (coefficient + 1) chances to repeat one of the already created characters

        Random rand = new Random(mainSeed); //when we add seeds to Random() it enables to recreate same random numbers

        // get 3 random numbers to make regular random number generator
        long[] seeds = new long[numberOfSeeds];
        for (int i = 0; i < numberOfSeeds; i++) {
            seeds[i] = rand.nextLong();
        }

        // how many digits has the biggest ID
        int fileNumDigits = (int) Math.log10((3 + similarityCoefficients.length)
                * numberOfSeeds * numberOfDoublings
                * (maxWordLengths.length)) + 1;

        // i - number of doubling
        // size - how big is data set
        // counter - used as id of the file
        for (int i = 0, size = firstSize, counter = 1; i < numberOfDoublings;
                i++, size *= 2) {
            for (long seed : seeds) { // we got 3 seeds
                for (int maxWordLen : maxWordLengths) {
                    String[] seq = genStringUniform(size, maxWordLen, seed);
                    boolean writtenToFile = writeToFile(seq, dir, "d_StringUniform_%d.dat", counter, fileNumDigits, size, seed, maxWordLen, "uniform", "");
                    if(writtenToFile) {
                        counter++;
                    }
                    seq = genStringSorted(size, maxWordLen, seed);
                    writtenToFile = writeToFile(seq, dir, "d_StringSorted_%d.dat", counter, fileNumDigits, size, seed, maxWordLen, "sorted", "");
                    if(writtenToFile) {
                        counter++;
                    }

                    seq = genStringInverse(size, maxWordLen, seed);
                    writtenToFile = writeToFile(seq, dir, "d_StringInverse_%d.dat", counter, fileNumDigits, size, seed, maxWordLen, "inverse", "");
                    if(writtenToFile) {
                        counter++;
                    }

                    for (int simCoeff : similarityCoefficients) {
                        seq = genStringSimilar(size, maxWordLen, simCoeff, seed);

                        String coeff = ", simCoeff=" + simCoeff;

                        writtenToFile = writeToFile(seq, dir, "d_StringSimilar_%d.dat", counter, fileNumDigits, size, seed, maxWordLen, "similar", coeff);
                        if(writtenToFile) {
                            counter++;
                        }
                    }
                }

            }
        }
    }

    private static boolean writeToFile(String[] seq, Path dir, String nameFormat, int counter, int fileNumDigits, int size, long seed, int maxWordLen, String genType, String coeff) {
        Path file = dir.resolve(String.format("%0" + fileNumDigits
                + nameFormat, counter, size));
        try (PrintWriter writer = new PrintWriter(
                Files.newBufferedWriter(file, DataGen.charset))) {
            writer.format("id=%0" + fileNumDigits + "d", counter);
            writer.println(", dataType=String, size="
                    + size + ", genType="+ genType + coeff + ", maxWordLength="
                    + maxWordLen + ", seed=" + seed);
            for (String val : seq) {
                writer.println(val);
            }
            //counter++;
            return true;
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        return false;
    }

    // return an array of Strings in which every word has length from 1 to maxWordLength
    private static String[] genStringUniform(int length, int maxWordLength,
            long seed) {
        Random rand = new Random(seed);
        String[] seq = new String[length];
        for (int i = 0; i < length; i++) {
            //word length must be positive, with max number given by parameter maxWordLength
            int wordLength = rand.nextInt(maxWordLength) + 1;
            String word = "";
            for (int j = 0; j < wordLength; j++) {
                //0-25 small and 26-51 big letters
                int num = rand.nextInt(52);
                char letter;
                if (num < 26) {
                    letter = 'a';
                    letter += num;
                } else {
                    letter = 'A';
                    letter += num - 26;
                }
                word += letter;
            }
            seq[i] = word;
        }
        return seq;
    }

    // inverse sorted stings
    private static String[] genStringInverse(int length, int maxWordLength,
            long seed) {
        String[] seq = genStringUniform(length, maxWordLength, seed);
        //get inverse sorting
        Arrays.sort(seq, (String a, String b) -> b.compareTo(a));
        return seq;
    }


    private static String[] genStringSorted(int length, int maxWordLength,
            long seed) {
        String[] seq = genStringUniform(length, maxWordLength, seed);
        Arrays.sort(seq);
        return seq;
    }

    // smaller coeff results in sequence of more similar elements (coeff <= 0
    // gives a sequence where all elements are the same)
    private static String[] genStringSimilar(int length, int maxWordLength,
            int coeff, long seed) {
        String[] seq = genStringUniform(length, maxWordLength, seed);
        if (coeff < 0) {
            coeff = 0;
        }
        double rescaledCoeff = 1.0 / (coeff + 1);
        Random rand = new Random(seed);
        for (int i = 1; i < seq.length; i++) {
            if (rand.nextDouble() < rescaledCoeff) {
                seq[i] = seq[rand.nextInt(i)];
            }
        }
        return seq;
    }
}
