package com.battleship.model;

import com.battleship.util.Constants;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Ship.java – Model class representing a single ship.
 *
 * Tracks:
 *  - size (number of cells)
 *  - positions on the board (List of Points)
 *  - how many times it has been hit
 *  - name for display purposes
 */
public class Ship {

    // ─── Fields ──────────────────────────────────────────────────────────────
    private final int           size;
    private final String        name;
    private final List<Point>   positions;   // grid coordinates occupied
    private int                 hits;
    private boolean             placed;      // has this ship been placed on the board?

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Creates an unplaced ship.
     * @param size number of cells this ship spans
     * @param name display name (e.g. "Carrier")
     */
    public Ship(int size, String name) {
        this.size      = size;
        this.name      = name;
        this.positions = new ArrayList<>();
        this.hits      = 0;
        this.placed    = false;
    }

    // ─── Core Logic ──────────────────────────────────────────────────────────

    /**
     * Registers a hit on this ship.
     */
    public void hit() {
        hits++;
    }

    /**
     * Returns true when the ship has been hit on every cell.
     */
    public boolean isSunk() {
        return hits >= size;
    }

    /**
     * Checks whether this ship occupies the given grid coordinate.
     */
    public boolean occupies(int row, int col) {
        for (Point p : positions) {
            if (p.x == row && p.y == col) return true;
        }
        return false;
    }

    /**
     * Sets the ship's positions on the board.
     * @param startRow   top-left row
     * @param startCol   top-left column
     * @param horizontal true = horizontal orientation
     */
    public void place(int startRow, int startCol, boolean horizontal) {
        positions.clear();
        for (int i = 0; i < size; i++) {
            int r = horizontal ? startRow : startRow + i;
            int c = horizontal ? startCol + i : startCol;
            positions.add(new Point(r, c));
        }
        placed = true;
    }

    /**
     * Resets the ship back to unplaced state.
     */
    public void reset() {
        positions.clear();
        hits   = 0;
        placed = false;
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────

    public int          getSize()       { return size; }
    public String       getName()       { return name; }
    public List<Point>  getPositions()  { return positions; }
    public int          getHits()       { return hits; }
    public boolean      isPlaced()      { return placed; }

    @Override
    public String toString() {
        return String.format("Ship[%s, size=%d, hits=%d/%d, sunk=%b]",
                name, size, hits, size, isSunk());
    }
}
