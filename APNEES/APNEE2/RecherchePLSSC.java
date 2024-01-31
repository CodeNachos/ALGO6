import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class RecherchePLSSC {

    // Recherche d'une PLSSC de 2 chaînes, naïf
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
        // à modifier
        return S1;
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




