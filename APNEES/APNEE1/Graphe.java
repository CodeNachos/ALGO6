import java.util.*;
import java.io.*;

class Graphe {

    // Tableau de taille n, avec n le nombre de sommets du graphe.
    // Pour i allant de 0 à n-1, sommets[i] contient l'ensemble des arcs ayant pour source le noeud (i+1), dans une liste chaînée. 
    Maillon [] sommets;

    Graphe(InputStream in) throws Exception {
        lire(in);
    }

    void lire(InputStream in) throws Exception {
        Scanner s;
        int nombre_sommets;
        String specification_arc;
        String [] parties;
        int numero, source, destination, etiquette;

        s = new Scanner(in);
        nombre_sommets = s.nextInt();
        sommets = new Maillon[nombre_sommets];

        while (s.hasNext()) {
            specification_arc = s.next();
            if (!specification_arc.matches(
                        "[0-9]+/[0-9]+\\+[0-9]+/->-?[0-9]+"))
                throw new Exception("Arc mal formé : " + specification_arc);

            parties = specification_arc.split("/", 2);
            numero = Integer.valueOf(parties[0]);
            parties = parties[1].split("\\+", 2);
            source = Integer.valueOf(parties[0]) - 1;
            parties = parties[1].split("/->", 2);
            destination = Integer.valueOf(parties[0]) - 1;
            etiquette = Integer.valueOf(parties[1]);

            Maillon nouveau, courant;
            nouveau = new Maillon();
            nouveau.arc = new Arc(numero, source, destination,
                    new Etiquette(etiquette));
            nouveau.suivant = null;
            if (sommets[source] == null) {
                sommets[source] = nouveau;
            } else {
                courant = sommets[source];
                while (courant.suivant != null)
                    courant = courant.suivant;
                courant.suivant = nouveau;
            }
        }
    }

    public String toString() {
        String resultat;

        resultat = sommets.length + "\n";
        for (int i=0; i<sommets.length; i++) {
            Maillon courant;

            courant = sommets[i]; 
            while (courant != null) {
                resultat += courant.arc + "\n";
                courant = courant.suivant;
            }
        }

        return resultat;
    }



    // Retourne le nombre de sommets dans le graphe
    public int nombreSommets(){
        return sommets.length;
    }

    // Retourne le nombre d'arcs dans le graphe
    public int nombreArcs() {
        int nbArcs = 0;
        for (Maillon m: sommets) {
            while(m != null) {
                m = m.suivant;
                nbArcs++;
            }
        }
        return nbArcs;
    }


    // Cherche si un arc ayant pour source sommetSource a pour destination sommetDest
    // S'il est trouvé, le renvoie. Sinon, renvoie null.
    // Utilisé par d'autres méthodes.
    private Arc chercheArcVers(int sommetSource, int sommetDest) {
        Maillon actuel = sommets[sommetSource];
        while (actuel != null) {
            Arc candidat = actuel.arc;
            if (candidat.destination == sommetDest) return candidat;
            actuel = actuel.suivant;
        }
        return null;
    }

    // Prend en entrée sommet1 et sommet2, deux entiers correspondant à deux sommets.
    // Renvoie vrai s'il existe un arc reliant sommet1 et sommet2
    // Renvoie faux sinon
    // Renvoie également faux si un de entiers est invalide (<0 ou >= nbrSommets)
    public boolean adjacents(int sommet1, int sommet2) {
        if (sommet1 <0 || sommet2 < 0 || sommet1 >= nombreSommets() || sommet2 >=nombreSommets()) {
            return false;
        }

        // Cherche, pour les deux sommets, s'ils possèdent un arc les reliant.
        if (chercheArcVers(sommet1, sommet2) != null || chercheArcVers(sommet2, sommet1) != null) {
            return true;
        }

        // Si aucun arc ne correspond, renvoie faux
        return false;
    }

    // Prend en entrée sommet1 et sommet2, deux entiers correspondant à deux sommets.
    // Si il existe, renvoie l'arc les reliant.
    // Sinon, renvoie null.
    public Arc arcEntre(int sommet1, int sommet2) {
        if (adjacents(sommet1, sommet2)) {
            Arc candidat = chercheArcVers(sommet1, sommet2);
            if (candidat != null) return candidat;
            else return chercheArcVers(sommet2, sommet1); 
        }
        return null;
    }


    // Renvoie un tableau contenant les successeurs du sommet en entrée.
    public int[] successeurs(int sommet) {
        int successeurs[] = new int[0];
        // Si le sommet en entrée n'est pas valide, retourne un tableau vide.
        if (sommet < 0 || sommet >= nombreSommets()) return successeurs;

        // Sinon, vérifie pour tous les sommets s'ils sont adjacents, et compte le nombre trouvés.
        int nbAdjacents = 0;
        boolean adjacents[] = new boolean[nombreSommets()];
        for (int sommet2 = 0; sommet2 < nombreSommets(); sommet2++) {
            if (adjacents(sommet, sommet2)) {
                adjacents[sommet2] = true;
                nbAdjacents++;
            }
            else adjacents[sommet2] = false;
        }
        successeurs = new int[nbAdjacents];
        int indexSuccesseurs = 0;
        for (int sommet2 = 0; sommet2 < nombreSommets(); sommet2++) {
            if(adjacents[sommet2]) {
                successeurs[indexSuccesseurs] = sommet2;
                indexSuccesseurs++;
            }
        }
        return successeurs;
    }


    // Renvoie un tableau contenant les arcs du graphe
    public Arc[] arcs(){
        Arc arcs[] = new Arc[nombreArcs()];
        int index = 0;
        for (Maillon m : sommets) {
            while (m != null) {
                arcs[index] = m.arc;
                index++;
                m = m.suivant;
            }
        }
        return arcs;
    }

    /**
     * Test if given edge list is valid perfect matching
     * 
     * @param arcs list of edges
     * @return true if edges compose perfect matching, else false
     */
    public boolean IsPerfectMatching(ArrayList<Arc> arcs) {
        ArrayList<Integer> matchedVertices = new ArrayList<Integer>();
        
        for (Arc e : arcs) {
            if (matchedVertices.contains(e.source) || matchedVertices.contains(e.destination)) {
                return false;
            }
            matchedVertices.add(e.source);
            matchedVertices.add(e.destination);
        }

        if (matchedVertices.size() != nombreSommets()) {
            return false;
        }

        return true;

    }

    /**
     * Test bipartiteness of the graph
     * 
     * @return true if graph is bipartite, else false
     */
    public boolean IsBipartite() {
        // Tests bipartiteness by verifiying if its 2-colorable
        int n = nombreSommets();
        // initialize graph uncolored with possible colors {1,2}
        int[] coloring = new int[n];
        
        // checks every vertice in case of more than one connected component
        for (int v = 0; v < n; v++) {
            if (coloring[v] == 0) { // if not colored
                coloring[v] = 1;    // color it
                if (!dfs_2Coloring(v, coloring)) { // test 2 coloration for component
                    return false;   
                }
            }
        }

        return true;
    }

    /**
     * Returns partitions for a bipartite graph
     * 
     * @return a list containing the partitions if the graph is bipartite, else null
     */
    public List<ArrayList<Integer>> BipartitePartitions() {
        // Tests bipartiteness by verifiying if its 2-colorable
        int n = nombreSommets();
        // initialize graph uncolored with possible colors {1,2}
        int[] coloring = new int[n];
        
        // checks every vertice in case of more than one connected component
        for (int v = 0; v < n; v++) {
            if (coloring[v] == 0) { // if not colored
                coloring[v] = 1;    // color it
                if (!dfs_2Coloring(v, coloring)) { // test 2 coloration for component
                    return null;   
                }
            }
        }
        // graph bipartite
        // create partitions from coloring
        ArrayList<Integer> p1 = new ArrayList<>();
        ArrayList<Integer> p2 = new ArrayList<>();

        for (int v = 0; v < n; v++) {
            if (coloring[v] == 1) {
                p1.add(v+1);
            } else {
                p2.add(v+1);
            }
        }

        List<ArrayList<Integer>> partitions = new ArrayList<>();
        partitions.add(p1);
        partitions.add(p2);

        return partitions;
    }


    /**
     * Test if graph is 2-colorable using depth first search
     * 
     * @param vertex initial vertex
     * @param coloring initial graph coloring
     * @return true if 2-colorable, else false
     */
    private boolean dfs_2Coloring(int vertex,  int[] coloring) {
        // we suppose here:
        //  0 - not colored
        //  1 and 2: possible colors
        for (int neighbor : successeurs(vertex)) {
            if (coloring[neighbor] == coloring[vertex]) {
                return false;
            }
            else if (coloring[neighbor] == 0) {
                coloring[neighbor] = coloring[vertex] == 1 ? 2 : 1;
                if (!dfs_2Coloring(neighbor, coloring)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Computes a perfect matching for a bipartite graph
     * 
     * @return list of edges representing matching if graph is bipartite, else null 
     */
    public ArrayList<Arc> GetPerfectMatching() {
        // check number of vertex
        if (nombreSommets() % 2 != 0) {
            return null;
        }

        // check bipartiteness and get partitions
        List<ArrayList<Integer>> partitions = BipartitePartitions();
        if (partitions == null) {
            return null;
        }

        ArrayList<Arc> matching = Enumerate_matching(partitions.get(0), 
                                                        partitions.get(1),
                                                        new ArrayList<Arc>(Arrays.asList(arcs())), new ArrayList<Arc>());

        return matching;
    }

    private ArrayList<Arc> Enumerate_matching(ArrayList<Integer> p1, 
                                                    ArrayList<Integer> p2, 
                                                    ArrayList<Arc> a, 
                                                    ArrayList<Arc> m) {
        if (p1.isEmpty() || p2.isEmpty()) {
            return m;
        } else if (a.isEmpty()) {
            return null;
        } else {
            Arc x;
            int aux;
            ArrayList<Arc> sol;

            x = a.remove(a.size()-1); // pop last element
            sol = Enumerate_matching(p1, p2, a, m);
            if (sol != null && IsPerfectMatching(sol)) {
                return sol;
            }
            
            for (Arc e : a) {
                if (e.source == x.source || 
                    e.source == x.destination ||
                    e.destination == x.destination || 
                    e.destination == x.source) {
                        a.remove(e);
                    }
            }

            aux = p1.indexOf(x.source);
            aux = aux != -1 ? aux : p1.indexOf(x.destination);
            if (aux != -1) {
                p1.remove(aux);
            }

            aux = p2.indexOf(x.source);
            aux = aux != -1 ? aux : p2.indexOf(x.destination);
            if (aux != -1) {
                p2.remove(aux);
            }

            m.add(x);
            
            sol = Enumerate_matching(p1, p2, a, m);
            return sol;

        }
    }

}
