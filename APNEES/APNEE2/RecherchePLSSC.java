import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class RecherchePLSSC {

    static String PLSSC(String S1, String S2) {
        if (S1.isEmpty() || S2.isEmpty()) {
            return "";
        } else if (S1.charAt(0) == S2.charAt(0)) {
            return S1.charAt(0) + PLSSC(new String(S1.substring(1)), new String(S2.substring(1)));
        } else {
            String SubS1 = PLSSC(new String(S1.substring(1)), new String(S2));
            String SubS2 = PLSSC(new String(S1), new String(S2.substring(1)));
            if (SubS1.length() >= SubS2.length()) {
                return SubS1;
            } else {
                return SubS2;
            }
        }
    }

    // Recherche d'une PLSSC de 2 chaînes, prog. dyn.
    static String PLSSC_PD(String S1, String S2) {
        int n = S1.length();
        int m = S2.length();
        int i,j;

        // create dynamic programing table
        int[][] dp_memo = new int[n+1][m+1];

        for (i = 1; i <= n; i++) {
            for (j = 1; j <= m; j++) {
                 if (S1.charAt(i-1) == S2.charAt(j-1)) {
                    dp_memo[i][j] = dp_memo[i-1][j-1]+1;
                 } else {
                    dp_memo[i][j] = Math.max(dp_memo[i-1][j], dp_memo[i][j-1]);
                 }
            }
        }

        // Retrieve plssc from table
        StringBuilder plssc_bldr = new StringBuilder();
        i = n; j = m;
        while (i > 0 && j > 0) {
            if (S1.charAt(i-1) == S2.charAt(j-1)) {
                plssc_bldr.append(S1.charAt(i-1));
                i--;
                j--;
            } else if (dp_memo[i-1][j] > dp_memo[i][j-1]) {
                 i--;
            } else {
                j--;
            }
        }
        plssc_bldr.reverse();
        return plssc_bldr.toString();

    }


    public static void main(String args[]) {

        String S1;
        String S2;

        FileInputStream input;
        BufferedReader reader;

        for (int i = 0; i < args.length; i++) {
            try {
                // Ouverture du fichier passé en argument
                input = new FileInputStream(args[i]);
                reader = new BufferedReader(new InputStreamReader(input));

                // Lecture de S1
                S1 = reader.readLine();
                // Lecture S2
                S2 = reader.readLine();

                // date de début
                long startTime = System.nanoTime();

                String result = PLSSC(S1,S2);

                // date de fin pour le calcul du temps écoulé
                long endTime = System.nanoTime();

                System.out.println("PLSSC: " + result);

                // Impression de la longueur du S1 de S2 et du temps d'exécution
                System.out.println(S1.length() + "\t" + S2.length() + "\t" + ((endTime - startTime)/1.0E9));

            } catch (FileNotFoundException e) {
                System.err.println("Erreur lors de l'ouverture du fichier " + args[i]);
            } catch (IOException e) {
                System.err.println("Erreur de lecture dans le fichier");
            }
        }
    }
}




