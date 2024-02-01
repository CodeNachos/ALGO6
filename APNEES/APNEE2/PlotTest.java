package APNEE2;

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

class PlotTest {
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

        try {
            writer = new FileWriter(dataFileDB);
            for (int p = 0; p < nb_pts1; p++) {
                writer.write(String.valueOf(x1[p]));
                writer.write(" ");
                writer.write(String.valueOf(y1[p]));
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error creating data file");
            System.exit(1);
        }

        try {
            writer = new FileWriter(dataFileDP);
            for (int p = 0; p < nb_pts2; p++) {
                writer.write(String.valueOf(x2[p]));
                writer.write(" ");
                writer.write(String.valueOf(y2[p]));
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error creating data file");
            System.exit(1);
        }

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
        String s1, s2, result;
        int nbPLSSC = 0;
        int nbPLSSC_DP = 0;

        s1 = new String();
        s2 = new String();
        String[] test_strings;

        for (int t = 0; t < tests.length; t++) {
            System.out.println("Running test " + tests[t].getName());
            test_strings = ReadTest(tests[t]);
            s1 = test_strings[0]; s2 = test_strings[1];

            
            if (s1.length() < 30 && s2.length() < 30) {
                startTime = System.nanoTime();
                result = RecherchePLSSC.PLSSC(s1, s2);
                endTime = System.nanoTime();
                PLSSC_test_time[nbPLSSC] = (endTime - startTime) / 1.0E9;
                PLSSC_test_length[nbPLSSC] = Math.max(s1.length(), s2.length());
                System.out.println("PLSSC    " +  s1.length() + "\t" + s2.length() + "\t" + ((endTime - startTime)/1.0E9));
                nbPLSSC++;
            }

            startTime = System.nanoTime();
            result = RecherchePLSSC.PLSSC_PD(s1, s2);
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
        System.out.println("done!");
    }


}