package com.example.myapplication.Model.domain.pathfinding;

import com.example.myapplication.Model.environments.GameMap;
import com.example.myapplication.Model.helper.GameConstants;

/**
 * Adapter that bridges the game's tile map representation with the
 * pure-domain A* pathfinding engine.
 * <p>
 * Converts {@link GameMap}'s sprite ID grid into a boolean walkability
 * grid, and provides coordinate translation between pixel space and
 * grid space.
 * </p>
 *
 * @see AStarPathfinder
 * @see GameMap
 */
public class GridAdapter {

    /**
     * Converts a GameMap's sprite ID matrix into a boolean walkability grid
     * suitable for the A* pathfinder.
     *
     * @param gameMap the game map containing sprite IDs and dimensions
     * @return a boolean[row][col] grid where {@code true} = walkable tile
     */
    public static boolean[][] toWalkableGrid(GameMap gameMap) {
        int width = gameMap.getArrayWidth();
        int height = gameMap.getArrayHeight();

        boolean[][] grid = new boolean[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int spriteId = gameMap.getSpriteID(col, row);
                grid[row][col] = GameMap.isMoveAbleBlock(spriteId);
            }
        }

        return grid;
    }

    /**
     * Converts a pixel-space X coordinate to a grid column index.
     *
     * @param pixelX the X coordinate in pixel space
     * @return the corresponding grid column index
     */
    public static int pixelToGridX(float pixelX) {
        return (int) (pixelX / GameConstants.Sprite.SIZE);
    }

    /**
     * Converts a pixel-space Y coordinate to a grid row index.
     *
     * @param pixelY the Y coordinate in pixel space
     * @return the corresponding grid row index
     */
    public static int pixelToGridY(float pixelY) {
        return (int) (pixelY / GameConstants.Sprite.SIZE);
    }

    /**
     * Converts a grid column index to a pixel-space X coordinate
     * (center of the tile).
     *
     * @param gridX the grid column index
     * @return the X coordinate at the center of the tile in pixel space
     */
    public static float gridToPixelX(int gridX) {
        return gridX * GameConstants.Sprite.SIZE + GameConstants.Sprite.SIZE / 2f;
    }

    /**
     * Converts a grid row index to a pixel-space Y coordinate
     * (center of the tile).
     *
     * @param gridY the grid row index
     * @return the Y coordinate at the center of the tile in pixel space
     */
    public static float gridToPixelY(int gridY) {
        return gridY * GameConstants.Sprite.SIZE + GameConstants.Sprite.SIZE / 2f;
    }
}
