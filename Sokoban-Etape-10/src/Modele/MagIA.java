package Modele;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Scanner;

import Global.Configuration;
import Structures.Sequence;


public class MagIA extends IA {

    private final Position2D[] DIRECTIONS = new Position2D[] {new Position2D(0, 1), new Position2D(1, 0), new Position2D(0, -1), new Position2D(-1, 0)};

    private int NBOXES;
    private Niveau BASELEVEL;

    private HashSet<Position2D> objectives;

    public Sequence<Coup> joue() {
        NBOXES = compterCaisses();
        BASELEVEL = getBaseLevel();
        
        objectives = getObjectives();

        ArrayList<Position2D> initialBoxPos = recupererCaisses();
        Position2D initialPlayerPos = new Position2D(niveau.pousseurL, niveau.pousseurC);
        ArrayList<gameState> path = computeSolution(initialPlayerPos, initialBoxPos);

        if (path == null) { 
            //System.out.println("no path found");
            return Configuration.nouvelleSequence();
        } else {
            for (gameState s: path) {
                //System.out.print(s.playerPos + " | ");
            }
            //System.out.println();
        }

        Sequence<Coup> resultat = Configuration.nouvelleSequence();
        int curL = niveau.pousseurL;
        int curC = niveau.pousseurC;
        
        for (gameState s: path) {
            int dL = s.playerPos.getL()-curL;
            int dC = s.playerPos.getC()-curC;
            resultat.insereQueue(niveau.deplace(dL, dC)); 
            curL = s.playerPos.getL();
            curC = s.playerPos.getC();
        }
        
        return resultat;
    }

    Coup coupAvecMarque(int dL, int dC) {
		// Un coup dans la direction donnée
		Coup resultat = niveau.deplace(dL, dC);
		int pL = niveau.lignePousseur();
		int pC = niveau.colonnePousseur();
		resultat.ajouteMarque(pL, pC, 0);
		return resultat;
    }

    @SuppressWarnings("static-access")
    private Niveau getBaseLevel() {
        Niveau baseLevel = niveau.clone();
        for (int l = 0; l < baseLevel.lignes(); l++) {
            for (int c = 0; c <  baseLevel.colonnes(); c++) {
                baseLevel.supprime(niveau.POUSSEUR, l, c);
                baseLevel.supprime(niveau.CAISSE, l, c);
            }
        }
        return baseLevel;
    }

    private int compterCaisses() {
        int n = 0;
        for (int l = 0; l < niveau.lignes(); l++) {
            for (int c = 0; c < niveau.colonnes(); c++) {
                if (niveau.aCaisse(l, c)) 
                    n++;
            }
        }

        return n;
    }

    private ArrayList<Position2D> recupererCaisses() {
        ArrayList<Position2D> positions = new ArrayList<>();
        for (int l = 0; l < niveau.lignes(); l++) {
            for (int c = 0; c < niveau.colonnes(); c++) {
                if (niveau.aCaisse(l, c))
                    positions.add(new Position2D(l, c));
            }
        }
        return positions;
    }

    private ArrayList<gameState> computeSolution(Position2D player, ArrayList<Position2D> boxes) {
        


        PriorityQueue<gameState> queue = new PriorityQueue<>();
        HashSet<gameState> visited = new HashSet<>();
        Map<gameState, gameState> cameFrom = new HashMap<>();
        queue.add(new gameState(player, boxes, 0));

        while (!queue.isEmpty()) {
            gameState current = queue.poll();
            visited.add(current);
            for (Position2D direction: DIRECTIONS) {
                gameState newState = tryMove(current, direction);
                if (newState != null && !visited.contains(newState)) {
                    //RedacteurNiveau r = new RedacteurNiveau(System.out);
                    //Niveau newLVL = populateBaseLevel(newState);
                    //for (Position2D p: newState.boxPos) {System.out.print(p);}
                    //System.out.println("\n"+newState.playerPos);
                    //r.ecrisNiveau(newLVL);
                    //Scanner scanner = new Scanner(System.in);
                    //System.out.println("Press Enter to continue...");
                    //scanner.nextLine();
                    if (!queue.contains(newState))
                        queue.add(newState);
                    cameFrom.put(newState, current);

                    if (isFinalState(newState)) {
                        return reconstructPath(cameFrom, newState);
                    }
                }
                
            }
        }

        return null;
    }

    private boolean isValidPosition(Position2D p) {
        return (p.getL() <= niveau.lignes() && p.getL() >= 0 && p.getC() <= niveau.colonnes() && p.getC() >= 0);
    }

    private gameState tryMove(gameState state, Position2D direction) {
        Niveau tryLevel = populateBaseLevel(state);
       
        int destL = state.playerPos.getL() + direction.getL();
		int destC = state.playerPos.getC() + direction.getC();

        if (!isValidPosition(new Position2D(destL, destC)))
            return null;

        gameState resultat = state.clone();

		if (tryLevel.aCaisse(destL, destC)) {
			int dCaisL = destL + direction.getL();
			int dCaisC = destC + direction.getC();

			if (tryLevel.estOccupable(dCaisL, dCaisC)) {
                int b = 0;
                while (state.boxPos.get(b).getL() != destL || state.boxPos.get(b).getC() != destC) {
                    b++;
                }
				resultat.boxPos.get(b).move(dCaisL, dCaisC);
                if (resultat.boxPos.size() > 1) {

                    ArrayList<Position2D> movedBox = new ArrayList<>();
                    movedBox.add(resultat.boxPos.get(b));
                    ArrayList<gameState> boxPath = computeSolution(new Position2D(destL, destC), movedBox);
                    if (boxPath == null)
                        return null;
                    resultat.setDistance(boxPath.size());
                } else {
                    resultat.setDistance(minDistanceToObjective(resultat.boxPos.get(b)));
                }
			} else {    
				return null;
			}
		}
		if (!tryLevel.aMur(destL, destC)) {
			resultat.playerPos.move(destL, destC);
            resultat.setDistance(resultat.getDistance()+1);
			return resultat;
		}
        //System.out.println("walking into the wall");
        return null;
    }

    private int minDistanceToObjective(Position2D boxPos) {
        int minDist = Integer.MAX_VALUE;
        for (Position2D o: objectives) {
            int d = (int)boxPos.distance(o);
            if (d < minDist)
                minDist = d;
        }

        return minDist;
    }

    private HashSet<Position2D> getObjectives() {
        
        HashSet<Position2D> objectives = new HashSet<>();
        for (int l = 0; l < niveau.lignes(); l++) {
            for (int c = 0; c < niveau.colonnes(); c++) {
                if (niveau.aBut(l, c))
                    objectives.add(new Position2D(l, c));
            }
        }
        return objectives;
    }

    private boolean isFinalState(gameState state) {
        for (int b = 0; b < state.boxPos.size(); b++) {
            if (!objectives.contains(state.boxPos.get(b))) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<gameState> reconstructPath(Map<gameState, gameState> cameFrom, gameState currState) {
        ArrayList<gameState> path = new ArrayList<>();
        while(cameFrom.get(currState) != null) {
            path.add(currState);
            currState = cameFrom.get(currState);
        }
        Collections.reverse(path);
        return path;
    }

    @SuppressWarnings("static-access")
    private Niveau populateBaseLevel(gameState state) {
        Niveau stateLevel = BASELEVEL.clone();
        stateLevel.ajoute(niveau.POUSSEUR, state.playerPos.getL(), state.playerPos.getC());
        for (int b = 0; b < state.boxPos.size(); b++) {
            stateLevel.ajoute(niveau.CAISSE, state.boxPos.get(b).getL(), state.boxPos.get(b).getC());
        }
        return stateLevel;
    }


    private class Position2D {
        public int l;
        public int c;

        public Position2D(int l, int c) {
            this.l = l;
            this.c = c;
        }

        public int getL() {return l;}

        public int getC() {return c;}
        
        public void move(int l, int c) {
            this.l = l;
            this.c = c;
        }

        public double distance(Position2D other) {
            double dl = this.l - other.l;
            double dc = this.c - other.c;
            return Math.sqrt(dl * dl + dc * dc);
        }

        @Override
        public Position2D clone() {
            return new Position2D(l, c);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) { 
                return true;
            }

            if (!(other instanceof Position2D)) {
                return false;
            }

            Position2D o = (Position2D) other;

            return (o.l == this.l) && (o.c == this.c);
        }

        @Override
        public int hashCode() {
            return Objects.hash(l, c);
        }

        @Override
        public String toString() {
            return "(" + l + "," + c + ")";
        }
    }

    private class gameState implements Comparable<gameState> {
        Position2D playerPos;
        ArrayList<Position2D> boxPos;
        int distance;
        int lenght;

        public gameState(Position2D playerPosition, ArrayList<Position2D> boxPosition, int distance) {
            this.playerPos = playerPosition.clone();
            boxPos = new ArrayList<>();
            for (int b = 0; b < boxPosition.size(); b++) {
                this.boxPos.add(boxPosition.get(b).clone());
            }

            this.distance = distance;
        }

        public int getDistance() {
            return distance;
        }

        public void setDistance(int d) {
            this.distance = d;
        }

        @Override
        public gameState clone() {
            return new gameState(playerPos, boxPos, distance);
        }

        @Override
        public int compareTo(gameState other) {
            return Integer.compare(this.distance, other.distance);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            gameState other = (gameState) obj;

            if (!playerPos.equals(other.playerPos)) {
                return false;
            }
            if (boxPos.size() != other.boxPos.size()) {
                return false;
            }
            for (int i = 0; i < boxPos.size(); i++) {
                if (!boxPos.get(i).equals(other.boxPos.get(i))) {
                    return false;
                }
            }
            return true;
        }


        @Override
        public int hashCode() {
            int hashCode = Objects.hash(playerPos); // Hash code for playerPos and distance
            for (Position2D pos : boxPos) {
                hashCode = 31 * hashCode + (pos == null ? 0 : pos.hashCode());
            }
            return hashCode;
        }
    }

}