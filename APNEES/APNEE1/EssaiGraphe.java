import java.io.*;

class EssaiGraphe {
    public static void main(String [] args) {
        FileInputStream f;
        Graphe g;

        try {
            f = new FileInputStream(args[0]);
            g = new Graphe(f);
            System.out.println(g.IsBipartite());
            System.out.println(g.BipartitePartitions());
            System.out.println(g.GetPerfectMatching());
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
