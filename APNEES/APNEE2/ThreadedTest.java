import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;

class ThreadedTest {
    private static String[] ReadTest(File file) {
        FileInputStream input;
        BufferedReader reader;
        
        String s1 = new String();
        String s2 = new String();

        try {
            input = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(input));

            s1 = reader.readLine();
            s2 = reader.readLine();

            reader.close();
            input.close();

        } catch (FileNotFoundException e) {
            System.err.println("Error opening file");
        } catch (IOException e) {
            System.err.println("Error reading file");
        }

        String[] result = new String[2];
        result[0] = new String(s1); result[1] = new String(s2);

        return result;
    }

    private static void GenPlotScript(double[] x1, double[]y1, int nb_pts1, double[]x2, double[] y2, int nb_pts2) {
        final String dataFileDB = "dataDB.txt";
        final String dataFileDP = "dataDP.txt";
        final String outputFile = "plotcost.gp";

        FileWriter writer;

        double[][] sortedDataDB = sortPoints(x1, y1, nb_pts1);
        double[][] sortedDataDP = sortPoints(x2, y2, nb_pts2);

        writePointsToFile(dataFileDB, sortedDataDB);
        writePointsToFile(dataFileDP, sortedDataDP);
        try {
            writer = new FileWriter(outputFile);
            writer.write("# gnuplot script (plotcost.gp)\n");
            writer.write("set terminal png\n");
            writer.write("set output 'cost.png'\n");
            writer.write("set title 'Time of Execution'\n");
            writer.write("set xlabel 'String Length'\n");
            writer.write("set ylabel 'Time(s)'\n");
            writer.write("plot  'dataDB.txt' using 1:2 with linespoints linewidth 3  title 'enumeration', ");
            writer.write("'dataDP.txt' using 1:2 with linespoints linewidth 3 title 'dynamic prog'\n");
            writer.close();
        } catch (IOException e) {
            System.err.println("Error creating plot file");
            System.exit(1);
        }
    }

    private static void writePointsToFile(String fileName, double[][] data) {
        try {
            FileWriter writer = new FileWriter(fileName);
            for (double[] point : data) {
                String formattedX = String.format("%.8f", point[0]);
                String formattedY = String.format("%.8f", point[1]);
                writer.write(formattedX);
                writer.write(" ");
                writer.write(formattedY);
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error creating data file");
            System.exit(1);
        }
    }

    private static double[][] sortPoints(double[] x, double[] y, int nb_pts) {
        double[][] sortedData = new double[nb_pts][2];
        for (int i = 0; i < nb_pts; i++) {
            sortedData[i][0] = x[i];
            sortedData[i][1] = y[i];
        }
        Arrays.sort(sortedData, (a, b) -> Double.compare(a[0], b[0]));
        return sortedData;
    }

    public static void GenPlot() {
        // Command to run (replace with your command)
        String command = "gnuplot -c plotcost.gp";

        try {
            // Create ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(command.split("\\s+"));
            
            // Redirect error stream to output stream
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Get the input stream of the process
            InputStream inputStream = process.getInputStream();
            
            // Create a BufferedReader to read the input stream
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Read the output of the process
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("gnuplot exited sucessfully");
            } else {
                System.out.println("gnuplot exited with an error code");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Usage: PlotTest <directory>");
            System.exit(1);
        }

        File dir = new File(args[0]);
        
        if (!dir.isDirectory()) {
            System.err.println("Not a directory");
            System.exit(1);
        }
        
        File[] tests = dir.listFiles();
        Arrays.sort(tests, Comparator.comparing(File::getName));
        
        double[] PLSSC_test_length = new double[tests.length];
        double[] PLSSC_DP_test_length = new double[tests.length];
        double[] PLSSC_test_time = new double[tests.length];
        double[] PLSSC_DP_test_time = new double[tests.length];
        long startTime, endTime;
        String s1, s2;
        int nbPLSSC = 0;
        int nbPLSSC_DP = 0;

        s1 = new String();
        s2 = new String();
        String[] test_strings;

        for (int t = 0; t < tests.length; t++) {
            System.out.println("Running test " + tests[t].getName());
            test_strings = ReadTest(tests[t]);
            s1 = test_strings[0]; s2 = test_strings[1];

            ThreadedPLSSC myRunnable = new ThreadedPLSSC(s1, s2);

            Thread thread = new Thread(myRunnable);

            int timeout = 10000; // 10 seconds

            // Start the thread
            startTime = System.nanoTime();
            thread.start();
            try {
                thread.join(timeout);
            
                if (myRunnable.CaugthStackOverflow()) {
                    ;
                } else if (thread.isAlive()) {
                    thread.interrupt();
                    System.out.println("PLSSC terminated: timed out");
                } else {
                    endTime = System.nanoTime();
                    PLSSC_test_time[nbPLSSC] = (endTime - startTime) / 1.0E9;
                    PLSSC_test_length[nbPLSSC] = Math.max(s1.length(), s2.length());
                    System.out.println("PLSSC    " +  s1.length() + "\t" + s2.length() + "\t" + ((endTime - startTime)/1.0E9));
                    nbPLSSC++;
                }
            } catch (InterruptedException e) {
                System.out.println("main thread interrupted ");
            }

            startTime = System.nanoTime();
            RecherchePLSSC.PLSSC_PD(s1, s2);
            endTime = System.nanoTime();
            PLSSC_DP_test_time[t] = (endTime - startTime) / 1.0E9;
            PLSSC_DP_test_length[nbPLSSC_DP] = Math.max(s1.length(), s2.length());
            System.out.println("PLSSC_PD " + s1.length() + "\t" + s2.length() + "\t" + ((endTime - startTime)/1.0E9));
            nbPLSSC_DP++;
        }

        System.out.print("Generating gnuplot scriptfile... ");
        GenPlotScript(PLSSC_test_length, PLSSC_test_time, nbPLSSC, PLSSC_DP_test_length, PLSSC_DP_test_time, nbPLSSC_DP);
        System.out.print("done!\n");

        System.out.println("Generating plot... ");
        GenPlot();
        System.out.println("done");

        System.exit(0);
    }
}

class ThreadedPLSSC implements Runnable {
    String s1, s2;
    private volatile boolean stackOverflowCaugth;
    
    public ThreadedPLSSC(String s1, String s2) {
        this.s1 = s1;
        this.s2 = s2;
        this.stackOverflowCaugth = false;
    }

    @Override
    public void run() {
        try {
            PLSSCRec(s1, s2);
        } catch (StackOverflowError e) {
            stackOverflowCaugth = true;
            System.out.println("PLSSC terminated: stack overflow");
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    // Recherche d'une PLSSC de 2 chaînes, naïf
    private String PLSSC(String s1, String s2) throws InterruptedException {
        return PLSSCRec(new String(s1), new String(s2));
    }

    private String PLSSCRec(String s1, String s2) throws InterruptedException {
        if (s1.isEmpty() || s2.isEmpty()) {
            return "";
        } else if (s1.charAt(0) == s2.charAt(0)) {
            return s1.charAt(0) + PLSSC(new String(s1.substring(1)), new String(s2.substring(1)));
        } else {
            String SubS1 = PLSSCRec(new String(s1.substring(1)), new String(s2));
            String SubS2 = PLSSCRec(new String(s1), new String(s2.substring(1)));
            if (SubS1.length() >= SubS2.length()) {
                return SubS1;
            } else {
                return SubS2;
            }
        }
    }

    public boolean CaugthStackOverflow() {
        return stackOverflowCaugth;
    } 
}