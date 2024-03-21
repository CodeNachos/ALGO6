package Modele;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


import Global.Configuration;
import Structures.Sequence;

public class IA1Box extends IA {

    private static final int[][] DIRECTIONS = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
    
    ArrayList<int[]> cheminCaisse;
    Iterator<int[]> coup;
    RedacteurNiveau r;
    Niveau np;

    public IA1Box(Niveau niveau) {
        this.niveau = niveau;
        r = new RedacteurNiveau(System.out);
        int[] caissePos = trouveCaisse();
        cheminCaisse = caisseDijkstra(caissePos[0], caissePos[1]);
        for (int[] coup: cheminCaisse) {
            System.out.print(coup[0] + " " + coup[1] + " | ");
        }
        System.out.println("");
        coup = cheminCaisse.iterator();
    }

	@Override
	public Sequence<Coup> joue() {
		Sequence<Coup> resultat = Configuration.nouvelleSequence();
        
        int[] caissePos = trouveCaisse();
        int pc = niveau.pousseurC;
        int pl = niveau.pousseurL;
        int cc = caissePos[1];
        int cl = caissePos[0];

        if (cheminCaisse != null) {
            int[] coupCaisse = coup.next();
            r.ecrisNiveau(niveau);
            if (pl != cl - coupCaisse[0] && pc != cc - coupCaisse[1]) {
                ArrayList<int[]> cheminPousseur = pousseurDijkstra(pl, pc, cl - coupCaisse[0], cc - coupCaisse[1], true);
                for (int[] coupPousseur: cheminPousseur) {
                    resultat.insereQueue(coupAvecMarque(coupPousseur[0], coupPousseur[1]));
                }
            }
            resultat.insereQueue(coupAvecMarque(coupCaisse[0], coupCaisse[1]));

            return resultat;
        } else {
            return null;
        }
	}

    private ArrayList<int[]> pousseurDijkstra(int l, int c, int dl, int dc, boolean perspective) {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        boolean[][] visited = new boolean[niveau.lignes()][niveau.colonnes()];
        Map<Node, Node> cameFrom = new HashMap<>();
        queue.add(new Node(l, c, 0, new int[] {0,0}));
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            visited[current.l][current.c] = true;
            for (int[] direction : DIRECTIONS) {
                int newL = current.l + direction[0];
                int newC = current.c + direction[1];
                if (newC > niveau.colonnes() || newC < 0 || newL > niveau.lignes() || newL < 0) {
                    continue;
                }
                
                if (coupPousseurValide(current.l, current.c, direction[0], direction[1],  (perspective?niveau:np)) && !visited[newL][newC]) {
                    Node next = new Node(newL, newC, current.distance + 1, direction);
                    queue.add(next);
                    cameFrom.put(next, current);

                    if (newL == dl && newC == dc) {
                        return reconstructPath(cameFrom, next);
                    }
                }
            }
        }

        return null;
    }
    
    private ArrayList<int[]> caisseDijkstra(int l, int c) {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        boolean[][] visited = new boolean[niveau.lignes()][niveau.colonnes()];
        Map<Node, Node> cameFrom = new HashMap<>();
        queue.add(new Node(l, c, 0, new int[] {0,0}));

        int pl = niveau.pousseurL;
        int pc = niveau.pousseurC;
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            visited[current.l][current.c] = true;
            for (int[] direction : DIRECTIONS) {
                int newL = current.l + direction[0];
                int newC = current.c + direction[1];
                if (newC > niveau.colonnes() || newC < 0 || newL > niveau.lignes() || newL < 0) {
                    continue;
                }
                np = niveau.clone();
                np.cases[l][c] = niveau.VIDE;
                np.cases[newL][newC] = niveau.CAISSE;
                if (cameFrom.get(current) != null) {
                    np.cases[pl][pc] = niveau.VIDE;
                    np.cases[cameFrom.get(current).l-cameFrom.get(current).direction[0]][cameFrom.get(current).c-cameFrom.get(current).direction[1]] = niveau.POUSSEUR;
                }
                ArrayList<int[]> coups = coupCaisseValide(current.l, current.c, direction[0], direction[1], pl,pc);
                if (coups != null && !visited[newL][newC]) {
                    // System.out.println(direction[0] + " " + direction[1]);
                    Node next = new Node(newL, newC, current.distance + 1, direction);
                    queue.add(next);
                    cameFrom.put(next, current);

                    if (niveau.aBut(newL, newC)) {
                        return reconstructPath(cameFrom, next);
                    }
                    
                } 
            }
        }

        return null;
    }


    private ArrayList<int[]> reconstructPath(Map<Node, Node> cameFrom, Node current) {
        ArrayList<int[]> path = new ArrayList<>();
        while (cameFrom.get(current) != null) {
            path.add(current.direction);
            current = cameFrom.get(current);

        }
        Collections.reverse(path);
        return path;
    }

    Coup coupAvecMarque(int dL, int dC) {
		// Un coup dans la direction donnée
		Coup resultat = niveau.deplace(dL, dC);
		int pL = niveau.lignePousseur();
		int pC = niveau.colonnePousseur();
		resultat.ajouteMarque(pL, pC, 0);
		return resultat;
	}

    boolean coupPousseurValide(int cl, int cc, int dl, int dc,Niveau niveau) {
        int destL = cl + dl;
		int destC = cc + dc;
        return (niveau.cases[destL][destC] & (niveau.MUR | niveau.CAISSE)) == 0;
    }

    ArrayList<int[]> coupCaisseValide(int cl, int cc, int dl, int dc, int pl, int pc) {
        int destL = cl + dl;
		int destC = cc + dc;
        int invL = cl - dl;
        int invC = cc - dc;

        if (invC > niveau.colonnes() || invC < 0 || invL > niveau.lignes() || invL < 0)
            return null;

        if (niveau.aMur(destL, destC))
            return null;
        
        if (niveau.aMur(invL, invC))
            return null;
        
        if (pl == invL && pc == invC) return new ArrayList<int[]>();
        return pousseurDijkstra(pl,pc,invL,invC, false);
    }   

    int[] trouveCaisse() {
        for (int l = 0; l < niveau.lignes(); l++) {
            for (int c =0; c < niveau.colonnes(); c++) {
                if (niveau.aCaisse(l, c)) 
                    return new int[] {l,c};
            }
        }
        return null;
    }

    class Node implements Comparable<Node> {
        int c, l, distance;
        int[] direction;
    
        Node(int l, int c, int distance, int[] direction) {
            this.c = c;
            this.l = l;
            this.distance = distance;
            this.direction = direction;
        }
    
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.distance, other.distance);
        }
    }
}
