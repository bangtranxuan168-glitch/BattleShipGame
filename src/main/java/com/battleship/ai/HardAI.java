package com.battleship.ai;

import com.battleship.model.Board;
import com.battleship.util.Constants;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * HardAI.java – Hunt-and-Target AI.
 *
 * Strategy:
 *  1. HUNT mode  – randomly fire until a hit is found.
 *  2. TARGET mode – once a hit is found, systematically fire at adjacent cells
 *     in the same direction until the ship sinks. Then revert to HUNT mode.
 *
 * This mimics how an experienced Battleship player thinks.
 */
public class HardAI extends EasyAI {

    // ─── Fields ──────────────────────────────────────────────────────────────

    private final Random        random      = new Random();
    private final Deque<Point>  targetQueue = new ArrayDeque<>();  // cells to try next
    private Point               lastHit     = null;                // most recent hit
    private Point               firstHit    = null;                // start of current ship hunt
    private boolean             dirLocked   = false;               // direction confirmed?
    private int                 dirRow      = 0;                   // direction vector row
    private int                 dirCol      = 0;                   // direction vector col

    // ─── Public API ──────────────────────────────────────────────────────────

    /**
     * Chooses the next shot using hunt-and-target strategy.
     */
    @Override
    public Point chooseShot(Board playerBoard) {
        // ── TARGET mode: drain the target queue ──────────────────────────────
        while (!targetQueue.isEmpty()) {
            Point candidate = targetQueue.poll();
            int r = candidate.x, c = candidate.y;
            if (r >= 0 && r < playerBoard.getSize()
                    && c >= 0 && c < playerBoard.getSize()
                    && !playerBoard.isAlreadyFired(r, c)) {
                return candidate;
            }
        }

        // ── HUNT mode: random shot ────────────────────────────────────────────
        List<Point> available = getAvailableCells(playerBoard);
        if (available.isEmpty()) return new Point(0, 0);
        return available.get(random.nextInt(available.size()));
    }

    /**
     * Must be called after every shot resolves so the AI can update its state.
     *
     * @param row    row of the shot
     * @param col    column of the shot
     * @param wasHit true if the shot hit a ship
     * @param wasSunk true if the hit sank the ship
     */
    public void onShotResult(int row, int col, boolean wasHit, boolean wasSunk) {
        if (wasSunk) {
            // Ship sunk → clear all targeting state, return to HUNT
            resetTargeting();
            return;
        }

        if (wasHit) {
            Point hitPoint = new Point(row, col);

            if (firstHit == null) {
                // First hit on a new ship – add orthogonal neighbors to queue
                firstHit = hitPoint;
                lastHit  = hitPoint;
                dirLocked = false;
                addOrthogonalNeighbors(row, col);
            } else {
                // Subsequent hit – lock direction and continue along that axis
                if (!dirLocked) {
                    // Determine direction from firstHit → current hit
                    dirRow = row - firstHit.x;
                    dirCol = col - firstHit.y;
                    // Normalize to unit step
                    if (dirRow != 0) dirRow = dirRow / Math.abs(dirRow);
                    if (dirCol != 0) dirCol = dirCol / Math.abs(dirCol);
                    dirLocked = true;
                }

                // Flood targets in the locked direction (both forward + backward from firstHit)
                targetQueue.clear();
                enqueueLinear(row, col, dirRow, dirCol);             // continue forward
                enqueueLinear(firstHit.x, firstHit.y, -dirRow, -dirCol); // try reverse
            }

            lastHit = hitPoint;
        }
        // If miss and we have a locked direction, the ship doesn't extend further that way.
        // The queue already has cells in the opposite direction, so no action needed.
    }

    // ─── Internal helpers ────────────────────────────────────────────────────

    /** Queues the four orthogonal neighbors of (row, col). */
    private void addOrthogonalNeighbors(int row, int col) {
        targetQueue.add(new Point(row - 1, col));
        targetQueue.add(new Point(row + 1, col));
        targetQueue.add(new Point(row, col - 1));
        targetQueue.add(new Point(row, col + 1));
    }

    /**
     * Enqueues cells starting from (startRow+dr, startCol+dc) and continuing
     * in the direction of (dr, dc) up to the board boundary.
     */
    private void enqueueLinear(int startRow, int startCol, int dr, int dc) {
        int r = startRow + dr;
        int c = startCol + dc;
        while (r >= 0 && r < Constants.GRID_SIZE && c >= 0 && c < Constants.GRID_SIZE) {
            targetQueue.addFirst(new Point(r, c));
            r += dr;
            c += dc;
        }
    }

    /** Clears all targeting state (called after sinking a ship). */
    private void resetTargeting() {
        targetQueue.clear();
        firstHit  = null;
        lastHit   = null;
        dirLocked = false;
        dirRow    = 0;
        dirCol    = 0;
    }
}
