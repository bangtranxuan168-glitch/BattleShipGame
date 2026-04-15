package com.battleship.controller;

import com.battleship.ai.EasyAI;
import com.battleship.ai.HardAI;
import com.battleship.model.*;
import com.battleship.util.Constants;
import com.battleship.util.SoundManager;

import javax.swing.*;
import java.awt.CardLayout;
import java.awt.Point;
import java.util.Random;

/**
 * GameController.java – Central controller for game logic.
 *
 * Responsibilities:
 *  - Manages turn flow (player ↔ AI)
 *  - Processes fire events
 *  - Checks win conditions
 *  - Switches screens via CardLayout
 *  - Coordinates sound effects
 */
public class GameController {

    // ─── Dependencies ─────────────────────────────────────────────────────────
    private final JPanel          mainPanel;     // root CardLayout panel
    private final CardLayout      cardLayout;
    private final SoundManager    sound;

    // ─── Game data ───────────────────────────────────────────────────────────
    private Player                humanPlayer;
    private Player                aiPlayer;
    private GameState             state;
    private boolean               hardMode;

    // ─── AI references ───────────────────────────────────────────────────────
    private EasyAI   easyAI;
    private HardAI   hardAI;

    // ─── View callbacks (set by views after construction) ────────────────────
    private Runnable              onPlayerTurnStart;
    private Runnable              onAiTurnStart;
    private Runnable              onBoardUpdate;
    private java.util.function.Consumer<Boolean> onGameOver;  // true = player won

    // ─── Screen lifecycle callbacks (set by Main) ─────────────────────────────
    private Runnable              onShowSetup;    // called when SETUP screen is shown
    private Runnable              onShowGame;     // called when GAME screen is shown
    private java.util.function.Consumer<Boolean> onShowResult; // called when RESULT shown

    // ─── Constructor ─────────────────────────────────────────────────────────

    public GameController(JPanel mainPanel, CardLayout cardLayout, SoundManager sound) {
        this.mainPanel  = mainPanel;
        this.cardLayout = cardLayout;
        this.sound      = sound;
    }

    // ─── Initialization ──────────────────────────────────────────────────────

    /**
     * Called when the player picks Easy or Hard mode from the main menu.
     * Resets all model state and navigates to the setup screen.
     */
    public void startNewGame(boolean hardMode) {
        this.hardMode    = hardMode;
        this.humanPlayer = new Player("Player");
        this.aiPlayer    = new Player("AI");
        this.state       = GameState.SETUP;

        // Create AI
        if (hardMode) {
            hardAI = new HardAI();
            easyAI = null;
        } else {
            easyAI = new EasyAI();
            hardAI = null;
        }

        // Place AI ships randomly
        placeAIShipsRandom();

        // Navigate to setup screen
        showScreen(Constants.SCREEN_SETUP);
    }

    /**
     * Randomly places all ships for the AI player.
     */
    public void placeAIShipsRandom() {
        Random rng = new Random();
        aiPlayer.getBoard().reset();
        // Re-init AI fleet
        for (Ship ship : aiPlayer.getShips()) {
            ship.reset();
        }

        for (Ship ship : aiPlayer.getShips()) {
            boolean placed = false;
            while (!placed) {
                boolean horizontal = rng.nextBoolean();
                int row = rng.nextInt(Constants.GRID_SIZE);
                int col = rng.nextInt(Constants.GRID_SIZE);

                if (aiPlayer.getBoard().canPlaceShip(row, col, ship.getSize(), horizontal)) {
                    ship.place(row, col, horizontal);
                    aiPlayer.getBoard().placeShip(ship);
                    placed = true;
                }
            }
        }
    }

    /**
     * Called by SetupController when all ships have been placed.
     * Transitions to gameplay.
     */
    public void onSetupComplete() {
        state = GameState.PLAYER_TURN;
        showScreen(Constants.SCREEN_GAME);
        if (onPlayerTurnStart != null) onPlayerTurnStart.run();
    }

    // ─── Fire Logic ──────────────────────────────────────────────────────────

    /**
     * Processes a shot fired by the human player at (row, col) on the AI's board.
     *
     * @return true if the cell was a valid, un-fired target; false if already fired
     */
    public boolean playerFire(int row, int col) {
        if (state != GameState.PLAYER_TURN) return false;
        if (aiPlayer.getBoard().isAlreadyFired(row, col)) return false;

        boolean hit = aiPlayer.getBoard().fireAt(row, col);
        humanPlayer.recordShot(row, col);

        if (hit) {
            sound.playHit();
            Ship hitShip = aiPlayer.getShipAt(row, col);
            if (hitShip != null) {
                hitShip.hit();
                if (hitShip.isSunk()) {
                    sound.playSunk();
                    aiPlayer.getBoard().markSunk(hitShip);
                }
            }

            // Check win
            if (aiPlayer.allShipsSunk()) {
                state = GameState.PLAYER_WIN;
                if (onBoardUpdate != null) onBoardUpdate.run();
                endGame(true);
                return true;
            }
        } else {
            sound.playMiss();
        }

        // Update board visuals
        if (onBoardUpdate != null) onBoardUpdate.run();

        // Switch to AI turn
        state = GameState.AI_TURN;
        if (onAiTurnStart != null) onAiTurnStart.run();

        scheduleAITurn();
        return true;
    }

    /**
     * Schedules the AI's shot after a delay (for better UX).
     */
    private void scheduleAITurn() {
        Timer timer = new Timer(Constants.AI_DELAY_MS, e -> {
            if (state == GameState.AI_TURN) {
                executeAITurn();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Executes the AI's shot selection and result processing.
     */
    private void executeAITurn() {
        Point shot = (hardAI != null)
                ? hardAI.chooseShot(humanPlayer.getBoard())
                : easyAI.chooseShot(humanPlayer.getBoard());

        int row = shot.x, col = shot.y;
        boolean hit = humanPlayer.getBoard().fireAt(row, col);
        aiPlayer.recordShot(row, col);

        boolean sunk = false;
        if (hit) {
            sound.playHit();
            Ship hitShip = humanPlayer.getShipAt(row, col);
            if (hitShip != null) {
                hitShip.hit();
                if (hitShip.isSunk()) {
                    sunk = true;
                    sound.playSunk();
                    humanPlayer.getBoard().markSunk(hitShip);
                }
            }
        } else {
            sound.playMiss();
        }

        // Inform HardAI of result
        if (hardAI != null) {
            hardAI.onShotResult(row, col, hit, sunk);
        }

        // Update board visuals
        if (onBoardUpdate != null) onBoardUpdate.run();

        // Check if AI won
        if (humanPlayer.allShipsSunk()) {
            state = GameState.AI_WIN;
            endGame(false);
            return;
        }

        // Back to player's turn
        state = GameState.PLAYER_TURN;
        if (onPlayerTurnStart != null) onPlayerTurnStart.run();
    }

    // ─── Game Over ───────────────────────────────────────────────────────────

    private void endGame(boolean playerWon) {
        if (playerWon) {
            sound.playWin();
        } else {
            sound.playLose();
        }
        if (onGameOver != null) {
            onGameOver.accept(playerWon);
        }
        showScreen(Constants.SCREEN_RESULT);  // triggers onShowResult callback
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    public void showScreen(String screenName) {
        cardLayout.show(mainPanel, screenName);
        // Notify lifecycle hooks so views can refresh their state
        switch (screenName) {
            case com.battleship.util.Constants.SCREEN_SETUP:
                if (onShowSetup  != null) onShowSetup.run();
                break;
            case com.battleship.util.Constants.SCREEN_GAME:
                if (onShowGame   != null) onShowGame.run();
                break;
            case com.battleship.util.Constants.SCREEN_RESULT:
                boolean won = (state == GameState.PLAYER_WIN);
                if (onShowResult != null) onShowResult.accept(won);
                break;
        }
    }

    public void goToMenu() {
        showScreen(Constants.SCREEN_MENU);
    }

    /**
     * Full game restart – keeps difficulty setting, re-randomizes AI ships,
     * resets human player, goes back to setup.
     */
    public void rematch() {
        humanPlayer.reset();
        aiPlayer.reset();
        placeAIShipsRandom();
        state = GameState.SETUP;
        showScreen(Constants.SCREEN_SETUP);
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────

    public Player       getHumanPlayer()   { return humanPlayer; }
    public Player       getAiPlayer()      { return aiPlayer; }
    public GameState    getState()         { return state; }
    public boolean      isHardMode()       { return hardMode; }
    public SoundManager getSound()         { return sound; }

    /** Called by GameView to receive turn-start notifications. */
    public void setOnPlayerTurnStart(Runnable cb)  { this.onPlayerTurnStart = cb; }
    public void setOnAiTurnStart(Runnable cb)      { this.onAiTurnStart = cb; }
    public void setOnBoardUpdate(Runnable cb)      { this.onBoardUpdate = cb; }
    public void setOnGameOver(java.util.function.Consumer<Boolean> cb) { this.onGameOver = cb; }

    /** Called by Main to hook screen lifecycle events. */
    public void setOnShowSetup(Runnable cb)                            { this.onShowSetup  = cb; }
    public void setOnShowGame(Runnable cb)                             { this.onShowGame   = cb; }
    public void setOnShowResult(java.util.function.Consumer<Boolean> cb) { this.onShowResult = cb; }
}
