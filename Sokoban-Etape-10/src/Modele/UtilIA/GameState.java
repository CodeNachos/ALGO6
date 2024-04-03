package Modele.UtilIA;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GameState implements Comparable<GameState> {
    public Map<Integer, Position2D> boxPos;
    public Position2D playerPos;
    public double priority;

    public GameState(Position2D playerPosition, Map<Integer, Position2D> boxPosition, double priority) {
        this.playerPos = playerPosition.clone();
        this.boxPos = new HashMap<>();
        for (int boxId : boxPosition.keySet()) {
            this.boxPos.put(boxId, boxPosition.get(boxId).clone());
        }
        this.priority = priority; // Initialize priority
    }

    // Add getter and setter for priority
    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    @Override
    public GameState clone() {
        return new GameState(playerPos, new HashMap<>(boxPos), priority);
    }

    @Override
    public int compareTo(GameState other) {
        return Double.compare(this.priority, other.priority);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GameState other = (GameState) obj;

        if (!playerPos.equals(other.playerPos)) {
            return false;
        }
        if (!boxPos.equals(other.boxPos)) {
            return false;
        }
        return true;
    }


    @Override
    public int hashCode() {
        return Objects.hash(playerPos, boxPos);
    }
}