package com.example.myapplication.domain.pathfinding;

import com.example.myapplication.Model.domain.pathfinding.AStarPathfinder;
import com.example.myapplication.Model.domain.pathfinding.Node;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for the A* pathfinding algorithm.
 * <p>
 * These tests are pure JUnit (no Android dependencies), demonstrating
 * that the pathfinding domain logic is fully decoupled from the
 * Android platform.
 * </p>
 */
public class AStarPathfinderTest {

    // ========== 1. Basic Path Finding ==========

    @Test
    public void testFindsPathInOpenGrid() {
        // A fully walkable 5x5 grid
        boolean[][] grid = createOpenGrid(5, 5);

        AStarPathfinder pathfinder = new AStarPathfinder(grid);
        List<int[]> path = pathfinder.findPath(0, 0, 4, 4);

        assertNotNull("Path should not be null", path);
        assertFalse("Path should not be empty", path.isEmpty());

        // Verify path ends at the goal
        int[] last = path.get(path.size() - 1);
        assertEquals("Goal X should be 4", 4, last[0]);
        assertEquals("Goal Y should be 4", 4, last[1]);
    }

    @Test
    public void testShortestPathLengthInOpenGrid() {
        // In an open 5x5 grid with 8-dir movement,
        // from (0,0) to (4,4) the shortest path is 4 diagonal steps
        boolean[][] grid = createOpenGrid(5, 5);

        AStarPathfinder pathfinder = new AStarPathfinder(grid);
        List<int[]> path = pathfinder.findPath(0, 0, 4, 4);

        assertEquals("Diagonal path from (0,0) to (4,4) should be 4 steps",
                4, path.size());
    }

    // ========== 2. Obstacle Avoidance ==========

    @Test
    public void testNavigatesAroundObstacle() {
        // 5x5 grid with a wall blocking the diagonal path
        //  . . . . .
        //  . X X X .
        //  . X . . .
        //  . X . . .
        //  . . . . .
        boolean[][] grid = createOpenGrid(5, 5);
        grid[1][1] = false;
        grid[1][2] = false;
        grid[1][3] = false;
        grid[2][1] = false;
        grid[3][1] = false;

        AStarPathfinder pathfinder = new AStarPathfinder(grid);
        List<int[]> path = pathfinder.findPath(0, 0, 4, 4);

        assertNotNull("Path should exist around obstacle", path);
        assertFalse("Path should not be empty", path.isEmpty());

        // Verify path ends at the goal
        int[] last = path.get(path.size() - 1);
        assertEquals(4, last[0]);
        assertEquals(4, last[1]);

        // Verify no path node goes through a wall
        for (int[] node : path) {
            assertTrue("Path node (" + node[0] + "," + node[1]
                            + ") should be on walkable terrain",
                    grid[node[1]][node[0]]);
        }
    }

    // ========== 3. No Path Available ==========

    @Test
    public void testReturnsEmptyListWhenNoPathExists() {
        // 5x5 grid with goal completely walled off
        boolean[][] grid = createOpenGrid(5, 5);
        // Wall around (4,4)
        grid[3][3] = false;
        grid[3][4] = false;
        grid[4][3] = false;

        AStarPathfinder pathfinder = new AStarPathfinder(grid);
        List<int[]> path = pathfinder.findPath(0, 0, 4, 4);

        assertTrue("Path should be empty when goal is unreachable",
                path.isEmpty());
    }

    // ========== 4. Same Start and Goal ==========

    @Test
    public void testSameStartAndGoalReturnsEmptyPath() {
        boolean[][] grid = createOpenGrid(5, 5);

        AStarPathfinder pathfinder = new AStarPathfinder(grid);
        List<int[]> path = pathfinder.findPath(2, 2, 2, 2);

        assertTrue("Same start/goal should return empty path",
                path.isEmpty());
    }

    // ========== 5. Boundary Cases ==========

    @Test
    public void testOutOfBoundsStartReturnsEmptyPath() {
        boolean[][] grid = createOpenGrid(5, 5);

        AStarPathfinder pathfinder = new AStarPathfinder(grid);
        List<int[]> path = pathfinder.findPath(-1, 0, 4, 4);

        assertTrue("Out-of-bounds start should return empty path",
                path.isEmpty());
    }

    @Test
    public void testOutOfBoundsGoalReturnsEmptyPath() {
        boolean[][] grid = createOpenGrid(5, 5);

        AStarPathfinder pathfinder = new AStarPathfinder(grid);
        List<int[]> path = pathfinder.findPath(0, 0, 5, 5);

        assertTrue("Out-of-bounds goal should return empty path",
                path.isEmpty());
    }

    @Test
    public void testUnwalkableStartReturnsEmptyPath() {
        boolean[][] grid = createOpenGrid(5, 5);
        grid[0][0] = false;

        AStarPathfinder pathfinder = new AStarPathfinder(grid);
        List<int[]> path = pathfinder.findPath(0, 0, 4, 4);

        assertTrue("Unwalkable start should return empty path",
                path.isEmpty());
    }

    @Test
    public void testUnwalkableGoalReturnsEmptyPath() {
        boolean[][] grid = createOpenGrid(5, 5);
        grid[4][4] = false;

        AStarPathfinder pathfinder = new AStarPathfinder(grid);
        List<int[]> path = pathfinder.findPath(0, 0, 4, 4);

        assertTrue("Unwalkable goal should return empty path",
                path.isEmpty());
    }

    // ========== 6. Diagonal Corner-Cutting Prevention ==========

    @Test
    public void testCannotCutThroughCorners() {
        // 3x3 grid where corner-cutting should be blocked:
        //  . X .
        //  X . .
        //  . . .
        boolean[][] grid = createOpenGrid(3, 3);
        grid[0][1] = false;  // Wall at (1,0)
        grid[1][0] = false;  // Wall at (0,1)

        AStarPathfinder pathfinder = new AStarPathfinder(grid);
        List<int[]> path = pathfinder.findPath(0, 0, 2, 2);

        // The direct diagonal from (0,0) to (1,1) should be blocked
        // because cutting between walls at (1,0) and (0,1)
        // Path must go around if possible
        // Actually, (0,0) is surrounded by walls on two sides,
        // so diagonal move to (1,1) is blocked
        if (!path.isEmpty()) {
            // First step should NOT be the diagonal (1,1)
            int[] first = path.get(0);
            if (first[0] == 1 && first[1] == 1) {
                fail("A* should not cut through the corner between walls");
            }
        }
    }

    // ========== 7. Large Grid Performance ==========

    @Test
    public void testLargeGridPerformance() {
        // Test on a 100x100 grid to verify performance is acceptable
        boolean[][] grid = createOpenGrid(100, 100);

        AStarPathfinder pathfinder = new AStarPathfinder(grid);

        long startTime = System.nanoTime();
        List<int[]> path = pathfinder.findPath(0, 0, 99, 99);
        long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;

        assertFalse("Path should be found in large grid", path.isEmpty());
        assertTrue("A* should complete in < 1000ms on 100x100 grid, took: "
                + elapsedMs + "ms", elapsedMs < 1000);
    }

    // ========== 8. Node Class Tests ==========

    @Test
    public void testNodeEquality() {
        Node a = new Node(3, 7);
        Node b = new Node(3, 7);
        Node c = new Node(3, 8);

        assertEquals("Nodes with same coords should be equal", a, b);
        assertNotEquals("Nodes with different coords should not be equal", a, c);
        assertEquals("Equal nodes should have same hashCode",
                a.hashCode(), b.hashCode());
    }

    @Test
    public void testNodeComparison() {
        Node a = new Node(0, 0);
        Node b = new Node(1, 1);

        a.setGCost(1.0);
        a.setHCost(2.0); // fCost = 3.0

        b.setGCost(2.0);
        b.setHCost(3.0); // fCost = 5.0

        assertTrue("Node with lower fCost should sort first",
                a.compareTo(b) < 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullGridThrowsException() {
        new AStarPathfinder(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyGridThrowsException() {
        new AStarPathfinder(new boolean[0][0]);
    }

    // ========== Helper ==========

    private boolean[][] createOpenGrid(int width, int height) {
        boolean[][] grid = new boolean[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                grid[row][col] = true;
            }
        }
        return grid;
    }
}
