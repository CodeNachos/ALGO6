import java.io.*;

class EssaiGraphe {
    public static void main(String [] args) {
        FileInputStream f;
        Graphe g;

        try {
            f = new FileInputStream(args[0]);
            g = new Graphe(f);
            System.out.println(g.EstBiparti());
            Arc[] couplage = {new Arc(1, 1, 4, new Etiquette(1)), 
                new Arc(2, 2, 5, new Etiquette(1)),
                new Arc(3, 3, 6, new Etiquette(1))};
            System.out.println(g.EstCouplage(couplage));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
