package com.example.myapplication.Model.domain.pathfinding;

/**
 * Represents a single node in the A* pathfinding grid.
 * <p>
 * Each node stores its grid coordinates, movement costs, and a reference
 * to its parent node for path reconstruction.
 * </p>
 * <p>
 * This class is a pure Java domain object with zero Android dependencies,
 * making it suitable for use in any Java backend or Android project.
 * </p>
 *
 * @see AStarPathfinder
 */
public class Node implements Comparable<Node> {

    private final int x;
    private final int y;

    /** Cost from start node to this node */
    private double gCost;

    /** Heuristic estimated cost from this node to the goal */
    private double hCost;

    /** Reference to the parent node for path reconstruction */
    private Node parent;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.gCost = Double.MAX_VALUE;
        this.hCost = 0;
        this.parent = null;
    }

    /**
     * Total estimated cost (f = g + h).
     * Used by the priority queue to determine node expansion order.
     */
    public double getFCost() {
        return gCost + hCost;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getGCost() {
        return gCost;
    }

    public void setGCost(double gCost) {
        this.gCost = gCost;
    }

    public double getHCost() {
        return hCost;
    }

    public void setHCost(double hCost) {
        this.hCost = hCost;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public int compareTo(Node other) {
        int fCompare = Double.compare(this.getFCost(), other.getFCost());
        if (fCompare != 0) {
            return fCompare;
        }
        // Break ties by preferring lower hCost (closer to goal)
        return Double.compare(this.hCost, other.hCost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return x == node.x && y == node.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "Node(" + x + ", " + y + ")";
    }
}
