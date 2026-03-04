package com.example.myapplication.Model.domain.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A* Pathfinding Algorithm implementation for a 2D tile-based grid.
 * <p>
 * Supports 8-directional movement (including diagonals) with proper
 * diagonal cost (√2 ≈ 1.414) and wall-corner collision prevention.
 * Uses Octile distance as the heuristic function, which is admissible
 * and consistent for 8-directional grids.
 * </p>
 * <p>
 * This class is a <strong>pure Java domain object</strong> with zero Android
 * dependencies, demonstrating clean separation between domain logic and
 * platform-specific code (Domain-Driven Design).
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * boolean[][] grid = GridAdapter.toWalkableGrid(spriteIds);
 * AStarPathfinder pathfinder = new AStarPathfinder(grid);
 * List<int[]> path = pathfinder.findPath(0, 0, 10, 10);
 * }</pre>
 *
 * @see Node
 * @see GridAdapter
 */
public class AStarPathfinder {

    /** Cost for horizontal/vertical movement */
    private static final double STRAIGHT_COST = 1.0;

    /** Cost for diagonal movement (√2) */
    private static final double DIAGONAL_COST = Math.sqrt(2);

    /**
     * 8-directional movement offsets.
     * Order: Right, Left, Up, Down, UpRight, UpLeft, DownRight, DownLeft
     */
    private static final int[][] DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, -1}, {0, 1},   // Cardinal
            {1, -1}, {-1, -1}, {1, 1}, {-1, 1}   // Diagonal
    };

    private final boolean[][] walkableGrid;
    private final int gridWidth;
    private final int gridHeight;

    /**
     * Creates a new A* pathfinder for the given walkability grid.
     *
     * @param walkableGrid 2D boolean array where {@code true} = walkable,
     *                     {@code false} = obstacle. Indexed as [row][col].
     * @throws IllegalArgumentException if the grid is null or empty
     */
    public AStarPathfinder(boolean[][] walkableGrid) {
        if (walkableGrid == null || walkableGrid.length == 0
                || walkableGrid[0].length == 0) {
            throw new IllegalArgumentException("Walkable grid must not be null or empty");
        }
        this.walkableGrid = walkableGrid;
        this.gridHeight = walkableGrid.length;
        this.gridWidth = walkableGrid[0].length;
    }

    /**
     * Finds the shortest path from (startX, startY) to (goalX, goalY).
     *
     * @param startX start column index
     * @param startY start row index
     * @param goalX  goal column index
     * @param goalY  goal row index
     * @return ordered list of [x, y] coordinates forming the path
     *         (excluding the start, including the goal),
     *         or an empty list if no path exists
     */
    public List<int[]> findPath(int startX, int startY, int goalX, int goalY) {
        // Boundary and walkability validation
        if (!isInBounds(startX, startY) || !isInBounds(goalX, goalY)) {
            return Collections.emptyList();
        }
        if (!walkableGrid[startY][startX] || !walkableGrid[goalY][goalX]) {
            return Collections.emptyList();
        }
        if (startX == goalX && startY == goalY) {
            return Collections.emptyList();
        }

        // Initialize the node grid
        Node[][] nodeGrid = new Node[gridHeight][gridWidth];
        for (int row = 0; row < gridHeight; row++) {
            for (int col = 0; col < gridWidth; col++) {
                nodeGrid[row][col] = new Node(col, row);
            }
        }

        Node startNode = nodeGrid[startY][startX];
        Node goalNode = nodeGrid[goalY][goalX];

        startNode.setGCost(0);
        startNode.setHCost(octileDistance(startX, startY, goalX, goalY));

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();

        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            // Goal reached — reconstruct path
            if (current.equals(goalNode)) {
                return reconstructPath(current);
            }

            closedSet.add(current);

            // Expand all 8 neighbors
            for (int[] dir : DIRECTIONS) {
                int neighborX = current.getX() + dir[0];
                int neighborY = current.getY() + dir[1];

                if (!isInBounds(neighborX, neighborY)) {
                    continue;
                }
                if (!walkableGrid[neighborY][neighborX]) {
                    continue;
                }

                Node neighbor = nodeGrid[neighborY][neighborX];
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                // Diagonal wall-corner check: prevent "cutting through" walls
                boolean isDiagonal = (dir[0] != 0 && dir[1] != 0);
                if (isDiagonal) {
                    boolean adjXWalkable = walkableGrid[current.getY()][neighborX];
                    boolean adjYWalkable = walkableGrid[neighborY][current.getX()];
                    if (!adjXWalkable || !adjYWalkable) {
                        continue; // Cannot cut through corners
                    }
                }

                double moveCost = isDiagonal ? DIAGONAL_COST : STRAIGHT_COST;
                double tentativeG = current.getGCost() + moveCost;

                if (tentativeG < neighbor.getGCost()) {
                    neighbor.setParent(current);
                    neighbor.setGCost(tentativeG);
                    neighbor.setHCost(octileDistance(
                            neighborX, neighborY, goalX, goalY));

                    // Re-add to open set (PriorityQueue handles ordering)
                    openSet.remove(neighbor);
                    openSet.add(neighbor);
                }
            }
        }

        // No path found
        return Collections.emptyList();
    }

    /**
     * Octile distance heuristic for 8-directional movement.
     * <p>
     * This is an admissible and consistent heuristic that accounts for
     * the reduced cost of diagonal movement compared to two separate
     * cardinal moves.
     * </p>
     * <p>
     * Formula: max(dx, dy) + (√2 - 1) × min(dx, dy)
     * </p>
     */
    private double octileDistance(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return Math.max(dx, dy) + (DIAGONAL_COST - STRAIGHT_COST) * Math.min(dx, dy);
    }

    /**
     * Reconstructs the path from goal to start by following parent references.
     *
     * @param goalNode the goal node with parent chain established
     * @return ordered list of [x, y] from start (exclusive) to goal (inclusive)
     */
    private List<int[]> reconstructPath(Node goalNode) {
        List<int[]> path = new ArrayList<>();
        Node current = goalNode;

        while (current.getParent() != null) {
            path.add(new int[]{current.getX(), current.getY()});
            current = current.getParent();
        }

        Collections.reverse(path);
        return path;
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < gridWidth && y >= 0 && y < gridHeight;
    }
}
