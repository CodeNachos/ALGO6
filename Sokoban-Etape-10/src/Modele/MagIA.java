package Modele;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

import Global.Configuration;
import Modele.UtilIA.FinalState;
import Modele.UtilIA.FloydWarshall;
import Modele.UtilIA.GameState;
import Modele.UtilIA.Position2D;
import Structures.Sequence;


public class MagIA extends IA {

    static private Position2D[] DIRECTIONS = new Position2D[] {new Position2D(0, 1), new Position2D(1, 0), new Position2D(0, -1), new Position2D(-1, 0)};

    private Map<Integer, Position2D> objectives;
    private Map<Integer, Position2D> boxes;

    Niveau baseLevel;

    GameState finalState;
    GameState initialState;

    int[][] distances;

    public Sequence<Coup> joue() {
        baseLevel = getBaseLevel();

        distances = FloydWarshall.initGraph(baseLevel);
        distances = FloydWarshall.floydWarshall(distances);
        
        objectives = getObjectives();
        boxes = getBoxes();
        Position2D playerPos = new Position2D(niveau.lignePousseur(), niveau.colonnePousseur());
        initialState = new GameState(playerPos, boxes, 0);
        finalState = FinalState.computeFinalState(boxes, objectives, playerPos, baseLevel, distances);
        FinalState.printBoxPositions(new GameState(playerPos, boxes, 0));
        System.out.println("final");
        FinalState.printBoxPositions(finalState);
        //List<Position2D> tiles = getAccessibleTiles(new GameState(playerPos, boxes, 0));
        //for (Position2D t: tiles) {
        //    System.out.println(t + " | ");
        //}
        
        List<GameState> path = getSolution(boxes, objectives, playerPos);
        for (GameState p: path) {
            System.out.print(p.playerPos + " | ");
        }
        System.out.println();

        List<Position2D> playerPath = new ArrayList<>();
        GameState currentState = initialState;
        Position2D lastPos;
        for (GameState next: path) {
            List<Position2D> playerToState = getPlayerPath(currentState, next.playerPos);
            for (Position2D pp : playerToState) {
                if (playerPath.size() > 0 && playerPath.get(playerPath.size()-1).equals(pp))
                    continue;
                playerPath.add(pp);
            }
            playerPath.add(next.playerPos.add(next.direction));
            currentState = next;
            for (Position2D p: playerPath) {
            System.out.print(p + " | ");
            }
            System.out.println();
        }
        
        

        Sequence<Coup> resultat = Configuration.nouvelleSequence();
        int curL = niveau.pousseurL;
        int curC = niveau.pousseurC;
        
        //for (GameState s: path) {
        //    int dL = s.playerPos.getL()-curL;
        //    int dC = s.playerPos.getC()-curC;
        //    resultat.insereQueue(niveau.deplace(dL, dC)); 
        //    curL = s.playerPos.getL();
        //    curC = s.playerPos.getC();
        //}
        
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

    private Map<Integer, Position2D> getBoxes() {
        Map<Integer, Position2D> objectives = new HashMap<>();
        int id = 0;
        for (int l = 0; l < niveau.lignes(); l++) {
            for (int c = 0; c < niveau.colonnes(); c++) {
                if (niveau.aCaisse(l, c)) {
                    objectives.put(id++, new Position2D(l, c));
                }
            }
        }
        return objectives;
    }

    private Map<Integer, Position2D> getObjectives() {
        Map<Integer, Position2D> objectives = new HashMap<>();
        int id = 0;
        for (int l = 0; l < niveau.lignes(); l++) {
            for (int c = 0; c < niveau.colonnes(); c++) {
                if (niveau.aBut(l, c)) {
                    objectives.put(id++, new Position2D(l, c));
                }
            }
        }
        return objectives;
    }
    
    // Heuristic function minimizing distance in high-dimensional space
    private double heuristic(GameState state, Map<Integer,Position2D> objectivesPosition) {
        // Calculate Manhattan distance between box coordinates of current state and final state
        double distance = 0.0;
        
        for (Integer boxId : state.boxPos.keySet()) {
            Position2D boxPos = state.boxPos.get(boxId);
            Position2D finalBoxPos = finalState.boxPos.get(boxId);
            
            distance += (Math.abs(boxPos.l - finalBoxPos.l) + Math.abs(boxPos.c - finalBoxPos.c)); 
        }
        
        return distance;
    }

    // A* algorithm to find solution
    public List<GameState> getSolution(Map<Integer, Position2D> boxPositions, Map<Integer, Position2D> objectivePositions, Position2D playerPosition) {
        PriorityQueue<GameState> queue = new PriorityQueue<>();
        HashSet<GameState> visited = new HashSet<>();
        Map<GameState, GameState> cameFrom = new HashMap<>();

        GameState initial = new GameState(playerPosition, new HashMap<>(boxPositions), 0); // Cloning boxPositions
        initial.setPriority(heuristic(initial, objectivePositions)); // Set initial priority based on heuristic
        queue.add(initial);

        while (!queue.isEmpty()) {
            GameState current = queue.poll();
            visited.add(current);
            // Generate successor states by moving accessible boxes
            List<GameState> nextStates = getAdjacentStates(current);
            
            for (GameState newState : nextStates) {
                if (visited.contains(newState)) {
                    continue;
                }   
                newState.setPriority(heuristic(newState, objectivePositions));
                if (!queue.contains(newState)) {
                    queue.add(newState);
                }
                cameFrom.put(newState, current);

                if (isFinalState(newState)) {
                    return reconstructPath(newState, cameFrom);
                }
            }
        }

        // No solution found
        return null;
    }



    // Function to reconstruct the path from the final state to the initial state
    private List<GameState> reconstructPath(GameState current, Map<GameState, GameState> cameFrom) {
        List<GameState> path = new ArrayList<>();
        while (cameFrom.containsKey(current)) {
            path.add(current);
            current = cameFrom.get(current);
        }
        System.out.println();
        Collections.reverse(path);
        return path;
    }

    private List<GameState> getAdjacentStates(GameState state) {
        Niveau tryLevel = populateBaseLevel(state);

        List<GameState> states = new ArrayList<>();

        Queue<Position2D> queue = new LinkedList<>();
        HashSet<Position2D> visited = new HashSet<>();

        queue.add(state.playerPos); // start at state player pos

        while (!queue.isEmpty()) {
            Position2D pos = queue.poll();
            if (visited.contains(pos)) {
                continue;
            }

            visited.add(pos);
            
            for (Position2D dir : DIRECTIONS) {
                Position2D dest = new Position2D(pos.l + dir.l, pos.c + dir.c);
                if (!isValidPosition(dest))
                    continue;

                if (tryLevel.estOccupable(dest.l, dest.c)) {
                    queue.add(new Position2D(dest.l, dest.c));
                } else if (tryLevel.aCaisse(dest.l, dest.c)) {
                    

                    GameState newState = isValidMove(state, pos, dir);
                    if (newState != null) {
                        states.add(newState);
                    }
                }
            }

        }

        return states;
    }

    private boolean isValidPosition(Position2D p) {
        return (p.getL() <= niveau.lignes() && p.getL() >= 0 && p.getC() <= niveau.colonnes() && p.getC() >= 0);
    }

    private GameState isValidMove(GameState current, Position2D tile, Position2D direction) {
        Niveau tryLevel = populateBaseLevel(current);
    
        int destL = tile.getL() + direction.getL();
        int destC = tile.getC() + direction.getC();
        
        if (!isValidPosition(new Position2D(destL, destC)))
            return null;
        
        GameState resultat = current.clone();
        
        if (tryLevel.aCaisse(destL, destC)) {
            int dCaisL = destL + direction.getL();
            int dCaisC = destC + direction.getC();
            
            if (tryLevel.estOccupable(dCaisL, dCaisC)) {
                int b = 0;
                while (current.boxPos.get(b).getL() != destL || current.boxPos.get(b).getC() != destC) {
                    b++;
                }
                
                int finalBoxL =  finalState.boxPos.get(b).getL();
                int finalBoxC =  finalState.boxPos.get(b).getC();
                if (dCaisL != finalBoxL && dCaisC != finalBoxC) {   // if box not on final position check for deadlock state
                    if (distances[dCaisL * niveau.colonnes() + dCaisC][finalBoxL * niveau.colonnes() + finalBoxC] == FloydWarshall.INF) { // deadlock state
                        return null; 
                    }
                }

                resultat.boxPos.get(b).move(dCaisL, dCaisC);
            } else {
                return null;
            }
        } else {
            return null;
        }
        if (!tryLevel.aMur(destL, destC)) {
            resultat.playerPos.move(tile.getL(), tile.getC());
            resultat.direction = direction;
            return resultat;
        }
        
        return null;
    }

    private boolean isFinalState(GameState state) {
        for (int b = 0; b < state.boxPos.size(); b++) {
            if (!objectives.containsValue(state.boxPos.get(b))) {
                return false;
            }
        }
        return true;
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

    @SuppressWarnings("static-access")
    private Niveau populateBaseLevel(GameState state) {
        Niveau stateLevel = baseLevel.clone();
        //stateLevel.ajoute(niveau.POUSSEUR, state.playerPos.getL(), state.playerPos.getC());
        for (int b = 0; b < state.boxPos.size(); b++) {
            stateLevel.ajoute(niveau.CAISSE, state.boxPos.get(b).getL(), state.boxPos.get(b).getC());
        }
        return stateLevel;
    }

    public List<Position2D> getPlayerPath(GameState current, Position2D target) {
        Niveau currentLevel = populateBaseLevel(current);
        PriorityQueue<PositionPriority> queue = new PriorityQueue<>();
        HashSet<Position2D> visited = new HashSet<>();
        Map<Position2D, Position2D> cameFrom = new HashMap<>();

        PositionPriority initial = new PositionPriority(current.playerPos, 0); 
        initial.setPriority(current.playerPos.distance(target)); 
        queue.add(initial);
        

        while (!queue.isEmpty()) {
            Position2D pos = queue.poll().position;
            visited.add(pos);
            // Generate successor states by moving the player
            for (Position2D direction : DIRECTIONS) {
                Position2D newPos = pos.add(direction);
                if (!visited.contains(newPos) && currentLevel.estOccupable(newPos.l, newPos.c)) {
                    PositionPriority newEntry = new PositionPriority(newPos, newPos.distance(target));
                    queue.add(newEntry);
                    cameFrom.put(newPos, pos);

                    if (newPos.equals(target)) {
                        return reconstructPathPlayer(newPos, cameFrom);
                    }
                }
            }
        }

        // No solution found
        return null;
    }

     // Function to reconstruct the path from the target to the initial position
     private List<Position2D> reconstructPathPlayer(Position2D pos, Map<Position2D, Position2D> cameFrom) {
        List<Position2D> path = new ArrayList<>();
        while (cameFrom.containsKey(pos)) {
            path.add(pos);
            pos = cameFrom.get(pos);
        }
        Collections.reverse(path);
        return path;
    }

    private static class PositionPriority implements Comparable<PositionPriority> {
        Position2D position;
        int priority;
        
        public PositionPriority (Position2D position, int priority) {
            this.position = position;
            this.priority = priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        @Override
        public int compareTo(PositionPriority other) {
            return Integer.compare(other.priority, this.priority);
        }
    }


}