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

    public static void main(String[] args) {
        final Path dir = Paths.get("data", "generatedSequences", "default");
        final Charset charset = Charset.forName("UTF-8");
        //seeds make sure that we get the same random
        final int mainSeed = 2938474; // based on this seed other numbers will be generated
        final int numberOfSeeds = 3; // 3 different seeds sets
        final int firstSize = 2; // first file will have 2 ints/Strings
        final int numberOfDoublings = 10; //so max number will be 1024
        final int[] bounds = {10, 100, 100000};
        final int[] maxWordLengths = {2, 10, 100};
        final int[] similarityCoefficients = {1, 3}; // so probablity of coefficets will be 1/2 or 1/4

        Random rand = new Random(mainSeed);

        // get 3 random numbers to make regular random number generator
        long[] seeds = new long[numberOfSeeds];
        for (int i = 0; i < numberOfSeeds; i++) {
            seeds[i] = rand.nextLong();
        }

        // how many digits max id will use
        // how many digits have number
        // 5 different ways
        int fileNumDigits = (int) Math.log10((3 + similarityCoefficients.length)
                * numberOfSeeds * numberOfDoublings
                * (bounds.length + maxWordLengths.length)) + 1;

        // i number of doublings
        // size -wielkość zestawu danych
        //
        for (int i = 0, size = firstSize, counter = 1; i < numberOfDoublings;
                i++, size *= 2) {
            for (long seed : seeds) { // we got 3 seeds
                for (int bound : bounds) {
                    int[] seq = genIntUniform(size, bound, seed);
                    //001_intUniform_
                    Path file = dir.resolve(String.format("%0" + fileNumDigits
                            + "d_intUniform_%d.dat", counter, size));
                    try (PrintWriter writer = new PrintWriter(
                            Files.newBufferedWriter(file, charset))) {
                        writer.format("id=%0" + fileNumDigits + "d", counter);
                        writer.println(", dataType=int, size="
                                + size + ", genType=uniform, bound=" + bound
                                + ", seed=" + seed);
                        for (int val : seq) {
                            writer.println(val);
                        }
                        counter++;
                    } catch (IOException x) {
                        System.err.format("IOException: %s%n", x);
                    }
                    
                    seq = genIntSorted(size, bound, seed);
                    file = dir.resolve(String.format(
                            "%03d_intSorted_%d.dat", counter, size));
                    try (PrintWriter writer = new PrintWriter(
                            Files.newBufferedWriter(file, charset))) {
                        writer.format("id=%0" + fileNumDigits + "d", counter);
                        writer.println(", dataType=int, size="
                                + size + ", genType=sorted, bound=" + bound
                                + ", seed=" + seed);
                        for (int val : seq) {
                            writer.println(val);
                        }
                        counter++;
                    } catch (IOException x) {
                        System.err.format("IOException: %s%n", x);
                    }

                    seq = genIntInverse(size, bound, seed);
                    file = dir.resolve(String.format(
                            "%03d_intInverse_%d.dat", counter, size));
                    try (PrintWriter writer = new PrintWriter(
                            Files.newBufferedWriter(file, charset))) {
                        writer.format("id=%0" + fileNumDigits + "d", counter);
                        writer.println(", dataType=int, size="
                                + size + ", genType=inverse, bound=" + bound
                                + ", seed=" + seed);
                        for (int val : seq) {
                            writer.println(val);
                        }
                        counter++;
                    } catch (IOException x) {
                        System.err.format("IOException: %s%n", x);
                    }

                    for (int simCoeff : similarityCoefficients) {
                        seq = genIntSimilar(size, bound, simCoeff, seed);
                        file = dir.resolve(String.format(
                                "%03d_intSimilar_%d.dat", counter, size));
                        try (PrintWriter writer = new PrintWriter(
                                Files.newBufferedWriter(file, charset))) {
                            writer.format("id=%0" + fileNumDigits + "d",
                                    counter);
                            writer.println(", dataType=int, size=" + size
                                    + ", genType=similar, simCoeff=" + simCoeff
                                    + ", bound=" + bound + ", seed=" + seed);
                            for (int val : seq) {
                                writer.println(val);
                            }
                            counter++;
                        } catch (IOException x) {
                            System.err.format("IOException: %s%n", x);
                        }
                    }
                }
                for (int maxWordLen : maxWordLengths) {
                    String[] seq = genStringUniform(size, maxWordLen, seed);
                    Path file = dir.resolve(String.format("%0" + fileNumDigits
                            + "d_StringUniform_%d.dat", counter, size));
                    try (PrintWriter writer = new PrintWriter(
                            Files.newBufferedWriter(file, charset))) {
                        writer.format("id=%0" + fileNumDigits + "d", counter);
                        writer.println(", dataType=String, size="
                                + size + ", genType=uniform, maxWordLength="
                                + maxWordLen + ", seed=" + seed);
                        for (String val : seq) {
                            writer.println(val);
                        }
                        counter++;
                    } catch (IOException x) {
                        System.err.format("IOException: %s%n", x);
                    }
                    
                    seq = genStringSorted(size, maxWordLen, seed);
                    file = dir.resolve(String.format(
                            "%03d_StringSorted_%d.dat", counter, size));
                    try (PrintWriter writer = new PrintWriter(
                            Files.newBufferedWriter(file, charset))) {
                        writer.format("id=%0" + fileNumDigits + "d", counter);
                        writer.println(", dataType=String, size="
                                + size + ", genType=sorted, maxWordLength="
                                + maxWordLen + ", seed=" + seed);
                        for (String val : seq) {
                            writer.println(val);
                        }
                        counter++;
                    } catch (IOException x) {
                        System.err.format("IOException: %s%n", x);
                    }

                    seq = genStringInverse(size, maxWordLen, seed);
                    file = dir.resolve(String.format(
                            "%03d_StringInverse_%d.dat", counter, size));
                    try (PrintWriter writer = new PrintWriter(
                            Files.newBufferedWriter(file, charset))) {
                        writer.format("id=%0" + fileNumDigits + "d", counter);
                        writer.println(", dataType=String, size="
                                + size + ", genType=inverse, maxWordLength="
                                + maxWordLen + ", seed=" + seed);
                        for (String val : seq) {
                            writer.println(val);
                        }
                        counter++;
                    } catch (IOException x) {
                        System.err.format("IOException: %s%n", x);
                    }

                    for (int simCoeff : similarityCoefficients) {
                        seq = genStringSimilar(size, maxWordLen, simCoeff, seed);
                        file = dir.resolve(String.format(
                                "%03d_StringSimilar_%d.dat", counter, size));
                        try (PrintWriter writer = new PrintWriter(
                                Files.newBufferedWriter(file, charset))) {
                            writer.format("id=%0" + fileNumDigits + "d",
                                    counter);
                            writer.println(", dataType=String, size=" + size
                                    + ", genType=similar, simCoeff=" + simCoeff
                                    + ", maxWordLength=" + maxWordLen
                                    + ", seed=" + seed);
                            for (String val : seq) {
                                writer.println(val);
                            }
                            counter++;
                        } catch (IOException x) {
                            System.err.format("IOException: %s%n", x);
                        }
                    }
                }

            }
        }
    }

    //non-negative ints below given bound
    // bound / max number
    // seed will generate same numbers with the same probability
    public static int[] genIntUniform(int length, int bound, long seed) {
        Random rand = new Random(seed);
        int[] seq = new int[length];
        for (int i = 0; i < length; i++) {
            seq[i] = rand.nextInt(bound);
        }
        return seq;
    }


    public static String[] genStringUniform(int length, int maxWordLength,
            long seed) {
        Random rand = new Random(seed);
        String[] seq = new String[length];
        for (int i = 0; i < length; i++) {
            //word length must be positive, with max number
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
    public static String[] genStringInverse(int length, int maxWordLength,
            long seed) {
        String[] seq = genStringUniform(length, maxWordLength, seed);
        //using a lambda expression for reverse sorting
        Arrays.sort(seq, (String a, String b) -> b.compareTo(a));
        return seq;
    }

    public static int[] genIntInverse(int length, int bound, long seed) {
        int[] seq = genIntUniform(length, bound, seed);
        //dont know how to do it similarly as in genStringInv
        Arrays.sort(seq);
        for (int i = 0, j = seq.length - 1; i < seq.length / 2; i++, j--) {
            int tmp = seq[i];
            seq[i] = seq[j];
            seq[j] = tmp;
        }
        return seq;
    }

    public static String[] genStringSorted(int length, int maxWordLength,
            long seed) {
        String[] seq = genStringUniform(length, maxWordLength, seed);
        Arrays.sort(seq);
        return seq;
    }

    public static int[] genIntSorted(int length, int bound, long seed) {
        int[] seq = genIntUniform(length, bound, seed);
        Arrays.sort(seq);
        return seq;
    }

    //smaller coeff results in sequence of more similar elements (coeff <= 0 
    //gives a sequence where all elements are the same
    public static int[] genIntSimilar(int length, int bound, int coeff,
            long seed) {
        //get random numbers
        int[] seq = genIntUniform(length, bound, seed);
        if (coeff < 0) {
            coeff = 0;
        }
        double rescaledCoeff = 1.0 / (coeff + 1); // rescaled to be sure that number is between 0 to 1
        Random rand = new Random(seed);
        for (int i = 1; i < seq.length; i++) {
            if (rand.nextDouble() < rescaledCoeff) {
                seq[i] = seq[rand.nextInt(i)]; // take one of the previous elements
            }
        }
        return seq;
    }

    public static String[] genStringSimilar(int length, int maxWordLength,
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
