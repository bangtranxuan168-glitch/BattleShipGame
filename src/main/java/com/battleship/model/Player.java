package com.battleship.model;

import com.battleship.util.Constants;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Player.java – Model class representing a player (human or AI).
 *
 * Holds:
 *  - Their own board (where their ships live)
 *  - Their fleet of Ships
 *  - Set of previously fired coordinates
 */
public class Player {

    // ─── Fields ──────────────────────────────────────────────────────────────
    private final String       name;
    private final Board        board;
    private final List<Ship>   ships;
    private final Set<Point>   shotsFired;   // cells this player has already fired at

    // ─── Constructor ─────────────────────────────────────────────────────────

    public Player(String name) {
        this.name       = name;
        this.board      = new Board();
        this.ships      = new ArrayList<>();
        this.shotsFired = new HashSet<>();

        // Create the fleet
        initFleet();
    }

    // ─── Fleet Initialization ────────────────────────────────────────────────

    /**
     * Builds the standard Battleship fleet:
     *  1× Carrier (5), 1× Battleship (4), 1× Cruiser (3),
     *  1× Submarine (3), 1× Destroyer (2)
     */
    private void initFleet() {
        ships.clear();
        for (int i = 0; i < Constants.SHIP_SIZES.length; i++) {
            ships.add(new Ship(Constants.SHIP_SIZES[i], Constants.SHIP_NAMES[i]));
        }
    }

    // ─── Shot Tracking ───────────────────────────────────────────────────────

    /**
     * Records a shot fired by this player at (row, col).
     */
    public void recordShot(int row, int col) {
        shotsFired.add(new Point(row, col));
    }

    /**
     * Checks if the player has already fired at a position.
     */
    public boolean hasAlreadyFiredAt(int row, int col) {
        return shotsFired.contains(new Point(row, col));
    }

    // ─── Win Condition ───────────────────────────────────────────────────────

    /**
     * Returns true when every ship in the fleet has been sunk.
     */
    public boolean allShipsSunk() {
        return ships.stream().allMatch(Ship::isSunk);
    }

    /**
     * Finds the Ship that occupies the given cell, or null if none.
     */
    public Ship getShipAt(int row, int col) {
        for (Ship s : ships) {
            if (s.occupies(row, col)) return s;
        }
        return null;
    }

    /**
     * Returns the next unplaced ship in the fleet, or null if all placed.
     */
    public Ship getNextUnplacedShip() {
        return ships.stream().filter(s -> !s.isPlaced()).findFirst().orElse(null);
    }

    /**
     * Resets the player state for a new game (keeps name).
     */
    public void reset() {
        board.reset();
        shotsFired.clear();
        initFleet();
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public String       getName()       { return name; }
    public Board        getBoard()      { return board; }
    public List<Ship>   getShips()      { return ships; }
    public Set<Point>   getShotsFired() { return shotsFired; }

    /**
     * Counts how many ships are still afloat.
     */
    public long getAliveShipCount() {
        return ships.stream().filter(s -> !s.isSunk()).count();
    }
}
