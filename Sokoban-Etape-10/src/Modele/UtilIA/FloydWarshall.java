package Modele.UtilIA;

import Modele.Niveau;

public class FloydWarshall {
    
    public static final int INF = Integer.MAX_VALUE;
    
    public static int[][] initGraph (Niveau level) {
        int[][] graph;
        // initiate graph matrix
        int nb_cases = level.lignes() * level.colonnes();

        graph = new int[nb_cases][nb_cases];

        // Initialize graph matrix with infinite graph
        for (int i = 0; i < nb_cases; i++) {
            for (int j = 0; j < nb_cases; j++) {
                graph[i][j] = (i == j) ? 0 : INF;
            }
        }
        // Initialize graph matrix with initial values
        for (int l = 0; l < level.lignes()  ; l++) {
            for (int c = 0; c < level.colonnes(); c++) {
                if (level.aMur(l,c)) 
                    continue;
                
                int currentPos = l * level.colonnes() + c;
                // Check adjacent tiles (up, down, left, right)
                for (int[] dir : new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
                    int newRow = l + dir[0];
                    int newCol = c + dir[1];
                    int invRow = l - dir[0];
                    int invCol = c - dir[1];
                    // Check if the adjacent tile is within bounds and not a wall
                    if (newRow >= 0 && newRow < level.lignes() && 
                        newCol >= 0 && newCol < level.colonnes() && 
                        invRow >= 0 && invRow < level.lignes() &&
                        invCol >= 0 && invCol < level.colonnes() &&
                        !level.aMur(newRow, newCol) && !level.aMur(invRow, invCol)) {
                            int adjacentPos = newRow * level.colonnes() + newCol;
                            graph[currentPos][adjacentPos] = 1; // Set distance to 1 if adjacent tile is reachable
                    }
                }   
            }
        }

        return graph;
    }

    public static int[][] floydWarshall(int[][] graph) {
        int n = graph.length;
        int[][] distances = new int[n][n];

        // Initialize distances matrix with the graph values
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                distances[i][j] = graph[i][j];
            }
        }

        // Floyd-Warshall algorithm
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (distances[i][k] != INF && distances[k][j] != INF &&
                        distances[i][k] + distances[k][j] < distances[i][j]) {
                        distances[i][j] = distances[i][k] + distances[k][j];
                    }
                }
            }
        }

        return distances;
    }

    public static void printDistances(int[][] distances) {
        for (int i = 0; i < distances.length; i++) {
            for (int j = 0; j < distances[i].length; j++) {
                if (distances[i][j] == FloydWarshall.INF) {
                    System.out.print("X ");
                } else {
                    System.out.print(distances[i][j] + " ");
                }
            }
            System.out.println();
        }
    }
}
