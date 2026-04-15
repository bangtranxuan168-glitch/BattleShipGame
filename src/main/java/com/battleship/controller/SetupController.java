package com.battleship.controller;

import com.battleship.model.Board;
import com.battleship.model.Player;
import com.battleship.model.Ship;
import com.battleship.util.Constants;

import java.awt.Point;
import java.util.Random;

/**
 * SetupController.java – Handles ship placement logic during the Setup phase.
 *
 * Works with SetupView to:
 *  - Track which ship is currently being placed
 *  - Validate placement positions
 *  - Place ships on the human player's board
 *  - Support random auto-placement
 */
public class SetupController {

    // ─── Dependencies ────────────────────────────────────────────────────────
    private final GameController gameController;
    private final Player         humanPlayer;

    // ─── State ───────────────────────────────────────────────────────────────
    private boolean  horizontal = true;   // current ship orientation
    private Ship     currentShip;         // the ship currently being placed

    // ─── Constructor ─────────────────────────────────────────────────────────

    public SetupController(GameController gameController) {
        this.gameController = gameController;
        this.humanPlayer    = gameController.getHumanPlayer();
        this.currentShip    = humanPlayer.getNextUnplacedShip();
    }

    // ─── Orientation ─────────────────────────────────────────────────────────

    /**
     * Toggles between horizontal and vertical orientation.
     * Called when user presses 'R'.
     */
    public void toggleOrientation() {
        horizontal = !horizontal;
    }

    public boolean isHorizontal() { return horizontal; }

    // ─── Placement Validation ────────────────────────────────────────────────

    /**
     * Returns true if the current ship can be placed at (row, col).
     */
    public boolean canPlace(int row, int col) {
        if (currentShip == null) return false;
        return humanPlayer.getBoard()
                .canPlaceShip(row, col, currentShip.getSize(), horizontal);
    }

    /**
     * Computes the list of cells the current ship would occupy at (row, col).
     * Useful for hover-preview highlighting.
     */
    public java.util.List<Point> previewCells(int row, int col) {
        java.util.List<Point> cells = new java.util.ArrayList<>();
        if (currentShip == null) return cells;

        for (int i = 0; i < currentShip.getSize(); i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            cells.add(new Point(r, c));
        }
        return cells;
    }

    // ─── Place Ship ──────────────────────────────────────────────────────────

    /**
     * Attempts to place the current ship at (row, col).
     *
     * @return true if placement succeeded; false if invalid position
     */
    public boolean placeShip(int row, int col) {
        if (!canPlace(row, col)) return false;

        currentShip.place(row, col, horizontal);
        humanPlayer.getBoard().placeShip(currentShip);

        // Advance to next unplaced ship
        currentShip = humanPlayer.getNextUnplacedShip();

        // If all ships placed → proceed to gameplay
        if (currentShip == null) {
            gameController.onSetupComplete();
        }

        return true;
    }

    // ─── Auto (Random) Placement ─────────────────────────────────────────────

    /**
     * Randomly places all remaining (unplaced) ships on the board.
     * Existing placed ships are preserved.
     */
    public void autoPlace() {
        Random rng = new Random();

        for (Ship ship : humanPlayer.getShips()) {
            if (ship.isPlaced()) continue;   // skip already-placed ships

            boolean placed = false;
            int attempts = 0;
            while (!placed && attempts < 1000) {
                boolean h   = rng.nextBoolean();
                int row     = rng.nextInt(Constants.GRID_SIZE);
                int col     = rng.nextInt(Constants.GRID_SIZE);

                if (humanPlayer.getBoard().canPlaceShip(row, col, ship.getSize(), h)) {
                    ship.place(row, col, h);
                    humanPlayer.getBoard().placeShip(ship);
                    placed = true;
                }
                attempts++;
            }
        }

        // All ships placed
        currentShip = null;
        gameController.onSetupComplete();
    }

    /**
     * Removes all placed ships and resets the board.
     * Called when the player clicks "Reset" in the setup screen.
     */
    public void resetPlacement() {
        for (Ship ship : humanPlayer.getShips()) {
            if (ship.isPlaced()) {
                humanPlayer.getBoard().removeShip(ship);
                ship.reset();
            }
        }
        horizontal  = true;
        currentShip = humanPlayer.getNextUnplacedShip();
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public Ship    getCurrentShip()    { return currentShip; }
    public boolean allShipsPlaced()    { return currentShip == null; }
}
