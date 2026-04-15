package com.battleship.ai;

import com.battleship.model.Board;
import com.battleship.util.Constants;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * EasyAI.java – Simple random-shot AI.
 *
 * Strategy: Pick any cell that hasn't been fired at yet, uniformly at random.
 */
public class EasyAI {

    private final Random random = new Random();

    /**
     * Chooses the next cell to fire at on the player's board.
     * Guarantees the cell has not already been fired at.
     *
     * @param playerBoard the player's board (to check already-fired cells)
     * @return Point(row, col) of the chosen shot
     */
    public Point chooseShot(Board playerBoard) {
        List<Point> available = getAvailableCells(playerBoard);

        if (available.isEmpty()) {
            // Fallback – should never happen in a valid game
            return new Point(0, 0);
        }

        return available.get(random.nextInt(available.size()));
    }

    /**
     * Collects all cells that have not been fired at yet.
     */
    protected List<Point> getAvailableCells(Board board) {
        List<Point> cells = new ArrayList<>();
        int size = board.getSize();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (!board.isAlreadyFired(r, c)) {
                    cells.add(new Point(r, c));
                }
            }
        }
        return cells;
    }
}
