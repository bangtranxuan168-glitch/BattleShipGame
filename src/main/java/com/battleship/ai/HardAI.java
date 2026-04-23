package com.battleship.ai;

import com.battleship.model.Board;
import com.battleship.util.Constants;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * HardAI.java – Smart Hunt-and-Target AI.
 *
 * Strategy:
 *  1. TARGET mode: Scan the board for active HIT cells (cells that belong to ships not yet SUNK).
 *     - If 1 hit: Probe orthogonal neighbors.
 *     - If >= 2 hits in a line: Extend the line at both ends.
 *  2. HUNT mode: Checkerboard parity strategy to optimize search space.
 */
public class HardAI extends EasyAI {

    private final Random random = new Random();

    @Override
    public Point chooseShot(Board playerBoard) {
        List<Point> activeHits = new ArrayList<>();
        int size = playerBoard.getSize();

        // Find all active hits on the board
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (playerBoard.getCell(r, c) == Constants.HIT) {
                    activeHits.add(new Point(r, c));
                }
            }
        }

        // ── TARGET mode ──────────────────────────────────────────────
        if (!activeHits.isEmpty()) {
            List<Point> potentialTargets = new ArrayList<>();

            if (activeHits.size() == 1) {
                addValidNeighbors(activeHits.get(0), playerBoard, potentialTargets);
            } else {
                // Try to extend lines formed by multiple hits
                for (int i = 0; i < activeHits.size(); i++) {
                    for (int j = i + 1; j < activeHits.size(); j++) {
                        Point p1 = activeHits.get(i);
                        Point p2 = activeHits.get(j);

                        if (p1.x == p2.x) { // Horizontal line
                            int minCol = Math.min(p1.y, p2.y);
                            int maxCol = Math.max(p1.y, p2.y);
                            if (minCol - 1 >= 0 && !playerBoard.isAlreadyFired(p1.x, minCol - 1)) {
                                potentialTargets.add(new Point(p1.x, minCol - 1));
                            }
                            if (maxCol + 1 < size && !playerBoard.isAlreadyFired(p1.x, maxCol + 1)) {
                                potentialTargets.add(new Point(p1.x, maxCol + 1));
                            }
                        }
                        if (p1.y == p2.y) { // Vertical line
                            int minRow = Math.min(p1.x, p2.x);
                            int maxRow = Math.max(p1.x, p2.x);
                            if (minRow - 1 >= 0 && !playerBoard.isAlreadyFired(minRow - 1, p1.y)) {
                                potentialTargets.add(new Point(minRow - 1, p1.y));
                            }
                            if (maxRow + 1 < size && !playerBoard.isAlreadyFired(maxRow + 1, p1.y)) {
                                potentialTargets.add(new Point(maxRow + 1, p1.y));
                            }
                        }
                    }
                }
                
                // Fallback: If no lines could be extended (e.g. diagonal hits, or blocked lines),
                // probe neighbors of all active hits
                if (potentialTargets.isEmpty()) {
                    for (Point p : activeHits) {
                        addValidNeighbors(p, playerBoard, potentialTargets);
                    }
                }
            }

            if (!potentialTargets.isEmpty()) {
                return potentialTargets.get(random.nextInt(potentialTargets.size()));
            }
        }

        // ── HUNT mode: Checkerboard parity strategy ───────────────────────────
        List<Point> available = getAvailableCells(playerBoard);
        if (available.isEmpty()) return new Point(0, 0);

        List<Point> parityCells = new ArrayList<>();
        for (Point p : available) {
            if ((p.x + p.y) % 2 == 0) {
                boolean hasMissNeighbors = false;
                int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
                for (int[] d : dirs) {
                    int nr = p.x + d[0], nc = p.y + d[1];
                    if (nr >= 0 && nr < size && nc >= 0 && nc < size) {
                        if (playerBoard.getCell(nr, nc) == Constants.MISS) {
                            hasMissNeighbors = true;
                            break;
                        }
                    }
                }
                if (!hasMissNeighbors) {
                    parityCells.add(p);
                }
            }
        }

        if (!parityCells.isEmpty()) {
            return parityCells.get(random.nextInt(parityCells.size()));
        }

        List<Point> allParity = new ArrayList<>();
        for (Point p : available) {
            if ((p.x + p.y) % 2 == 0) allParity.add(p);
        }
        if (!allParity.isEmpty()) {
            return allParity.get(random.nextInt(allParity.size()));
        }

        return available.get(random.nextInt(available.size()));
    }

    private void addValidNeighbors(Point p, Board board, List<Point> targets) {
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        for (int[] d : dirs) {
            int r = p.x + d[0], c = p.y + d[1];
            if (r >= 0 && r < board.getSize() && c >= 0 && c < board.getSize()) {
                if (!board.isAlreadyFired(r, c)) {
                    targets.add(new Point(r, c));
                }
            }
        }
    }

    /**
     * Stateless AI no longer needs to track shot results manually.
     */
    public void onShotResult(int row, int col, boolean wasHit, boolean wasSunk) {
        // No-op
    }
}
