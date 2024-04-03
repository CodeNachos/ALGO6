package Modele.UtilIA;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import Modele.Niveau;

public class FinalState {

    static public GameState computeFinalState(Map<Integer, Position2D> boxPositions, Map<Integer, Position2D> objectivePositions,
        Position2D playerPosition, Niveau level, int[][] distances) {

        // order objectives by their priority
        PriorityQueue<ObjectPriority> objectiveQueue = new PriorityQueue<>();
        for (Integer objectiveId : objectivePositions.keySet()) {
            objectiveQueue.add(new ObjectPriority(objectiveId, computeObjectivePriority(level, objectivePositions.get(objectiveId))));
        }

        // result
        Map<Integer, Position2D> matchedBoxes = new HashMap<>();

        // assign objectives to boxes
        while (!objectiveQueue.isEmpty()) {
            int objectiveId = objectiveQueue.poll().id; // get objective having highest priority

            // get closest box = prioritary box to objective
            ObjectPriority boxPriority = new ObjectPriority(-1, Integer.MAX_VALUE);
            for (int boxId : boxPositions.keySet()) {
                // check if box available
                if (matchedBoxes.get(boxId) != null) {
                    continue;
                }

                // check if objective is accessible
                Position2D boxPos = boxPositions.get(boxId);
                Position2D objPos = objectivePositions.get(objectiveId);
                if (distances[boxPos.getL() * level.colonnes() + boxPos.getC()][objPos.getL() * level.colonnes() + objPos.getC()] == FloydWarshall.INF) {
                    continue;
                }

                // check for closest
                int p = boxPositions.get(boxId).distance(objectivePositions.get(objectiveId));
                if (p < boxPriority.priority) {
                    boxPriority.id = boxId;
                    boxPriority.priority = p;
                }
            }

            // add box and its destination to result
            matchedBoxes.put(boxPriority.id, objectivePositions.get(objectiveId));
        }

        return new GameState(playerPosition, matchedBoxes, 0);
    }

    static public int computeObjectivePriority(Niveau level, Position2D objective) {
        int priotiry = 0;
        for (int[] dir : new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
            int newRow = objective.getL() + dir[0];
            int newCol = objective.getC() + dir[1];
            if (newRow >= 0 && newRow < level.lignes() && 
                newCol >= 0 && newCol < level.colonnes() &&
                level.aMur(newRow, newCol)) {
                    priotiry += 1;
                }
        }
        return priotiry;
    }

    static public void printBoxPositions(GameState state) {
        for (Map.Entry<Integer, Position2D> entry : state.boxPos.entrySet()) {
            System.out.println("Box: " + entry.getKey() + ", Position: " + entry.getValue());
        }
    }

    private static class ObjectPriority implements Comparable<ObjectPriority> {
        int id;
        int priority;
        
        public ObjectPriority (int id, int priority) {
            this.id = id;
            this.priority = priority;
        }

        @Override
        public int compareTo(ObjectPriority other) {
            return Integer.compare(this.priority, other.priority);
        }
    }

}
