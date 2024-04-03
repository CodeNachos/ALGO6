package Modele.UtilIA;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FinalState {

    // Function to solve Sokoban using stable marriage approach
    static public GameState computeFinalState(Map<Integer, Position2D> boxPositions, Map<Integer, Position2D> objectivePositions, Position2D playerPosition) {
        // Initialize arrays to track preferences
        Map<Integer, Integer> boxPreferences = new HashMap<>();
        Map<Integer, Integer> objectivePreferences = new HashMap<>();
        
        // Populate box preferences
        for (Integer boxId : boxPositions.keySet()) {
            double minDistance = Double.MAX_VALUE;
            Integer preferredObjectiveId = null;
            for (Integer objectiveId : objectivePositions.keySet()) {
                double distance = boxPositions.get(boxId).distance(objectivePositions.get(objectiveId));
                if (distance < minDistance) {
                    minDistance = distance;
                    preferredObjectiveId = objectiveId;
                }
            }
            boxPreferences.put(boxId, preferredObjectiveId);
        }

        // Populate objective preferences
        for (Integer objectiveId : objectivePositions.keySet()) {
            double minDistance = Double.MAX_VALUE;
            Integer preferredBoxId = null;
            for (Integer boxId : boxPositions.keySet()) {
                double distance = objectivePositions.get(objectiveId).distance(boxPositions.get(boxId));
                if (distance < minDistance) {
                    minDistance = distance;
                    preferredBoxId = boxId;
                }
            }
            objectivePreferences.put(objectiveId, preferredBoxId);
        }

        // Perform stable marriage
        Map<Integer, Integer> matches = stableMarriage(boxPreferences, objectivePreferences);

        // Construct the game state with matched box positions
        Map<Integer, Position2D> matchedBoxPositions = new HashMap<>();
        for (Integer objectiveId : objectivePositions.keySet()) {
            Integer matchedBoxId = matches.get(objectiveId);
            if (matchedBoxId != null) {
                matchedBoxPositions.put(matchedBoxId, boxPositions.get(matchedBoxId));
            }
        }

        // Return the game state
        return new GameState(playerPosition, matchedBoxPositions, 0); // Distance can be initialized as 0 or calculated as needed
    }
    
    // Stable marriage algorithm
    static private Map<Integer, Integer> stableMarriage(Map<Integer, Integer> boxPreferences, Map<Integer, Integer> objectivePreferences) {
        Map<Integer, Integer> matches = new HashMap<>();
        Set<Integer> unmatchedBoxes = new HashSet<>(boxPreferences.keySet());

        while (!unmatchedBoxes.isEmpty()) {
            Integer boxId = unmatchedBoxes.iterator().next();
            Integer preferredObjectiveId = boxPreferences.get(boxId);
            Integer currentMatchId = matches.get(preferredObjectiveId);

            if (currentMatchId == null || isObjectiveBetterMatched(preferredObjectiveId, boxId, objectivePreferences)) {
                matches.put(preferredObjectiveId, boxId);
                unmatchedBoxes.remove(boxId);
                if (currentMatchId != null) {
                    unmatchedBoxes.add(currentMatchId);
                }
            }
        }
        
        return matches;
    }

    // Function to determine if an objective prefers a new box over its current match
    static private boolean isObjectiveBetterMatched(Integer objectiveId, Integer newBoxId,  Map<Integer, Integer> objectivePreferences) {
        return objectivePreferences.get(objectiveId) == newBoxId;
    }

}
