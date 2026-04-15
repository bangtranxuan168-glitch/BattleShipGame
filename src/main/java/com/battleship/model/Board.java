package com.battleship.model;

import com.battleship.util.Constants;

/**
 * Board.java – Model class representing a 10×10 game board.
 *
 * Cell states (stored in int[][] grid):
 *   0 = EMPTY   – no ship, not fired at
 *   1 = SHIP    – ship present, not yet hit
 *   2 = HIT     – ship cell that was hit
 *   3 = MISS    – empty cell that was fired at
 *   4 = SUNK    – part of a fully-sunk ship
 */
public class Board {

    // ─── Fields ──────────────────────────────────────────────────────────────
    private final int[][] grid;
    private final int     size;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public Board() {
        this.size = Constants.GRID_SIZE;
        this.grid = new int[size][size];
        // All cells start as EMPTY (0 is Java's default for int arrays)
    }

    // ─── Placement Helpers ───────────────────────────────────────────────────

    /**
     * Checks whether a ship can be placed at the given position.
     * Verifies bounds and ensures no overlap with existing ships.
     *
     * @param startRow   top-left row
     * @param startCol   top-left column
     * @param shipSize   number of cells the ship occupies
     * @param horizontal true = horizontal orientation
     * @return true if placement is valid
     */
    public boolean canPlaceShip(int startRow, int startCol, int shipSize, boolean horizontal) {
        for (int i = 0; i < shipSize; i++) {
            int r = horizontal ? startRow : startRow + i;
            int c = horizontal ? startCol + i : startCol;

            // Out-of-bounds check
            if (r < 0 || r >= size || c < 0 || c >= size) return false;

            // Overlap check (must be EMPTY)
            if (grid[r][c] != Constants.EMPTY) return false;
        }
        return true;
    }

    /**
     * Places a ship on the board by marking its cells as SHIP.
     * Caller must first call canPlaceShip() for validation.
     *
     * @param ship       the Ship to place
     */
    public void placeShip(Ship ship) {
        for (var pos : ship.getPositions()) {
            grid[pos.x][pos.y] = Constants.SHIP;
        }
    }

    /**
     * Removes a ship from the board (resets its cells to EMPTY).
     */
    public void removeShip(Ship ship) {
        for (var pos : ship.getPositions()) {
            if (pos.x >= 0 && pos.x < size && pos.y >= 0 && pos.y < size) {
                grid[pos.x][pos.y] = Constants.EMPTY;
            }
        }
    }

    // ─── Fire Logic ──────────────────────────────────────────────────────────

    /**
     * Fires at the given cell.
     * @return true if a ship was hit, false if miss
     */
    public boolean fireAt(int row, int col) {
        if (grid[row][col] == Constants.SHIP) {
            grid[row][col] = Constants.HIT;
            return true;
        } else {
            grid[row][col] = Constants.MISS;
            return false;
        }
    }

    /**
     * Marks all cells of a sunk ship with SUNK state (for visual effect).
     */
    public void markSunk(Ship ship) {
        for (var pos : ship.getPositions()) {
            grid[pos.x][pos.y] = Constants.SUNK;
        }
    }

    /**
     * Returns true if the cell has already been fired at.
     */
    public boolean isAlreadyFired(int row, int col) {
        int state = grid[row][col];
        return state == Constants.HIT || state == Constants.MISS || state == Constants.SUNK;
    }

    // ─── Reset ───────────────────────────────────────────────────────────────

    /**
     * Resets the entire board to EMPTY state.
     */
    public void reset() {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                grid[r][c] = Constants.EMPTY;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public int[][] getGrid()         { return grid; }
    public int     getSize()         { return size; }
    public int     getCell(int r, int c) { return grid[r][c]; }
}
