package com.battleship.controller;

import com.battleship.ai.EasyAI;
import com.battleship.ai.HardAI;
import com.battleship.model.GameState;
import com.battleship.model.Player;
import com.battleship.model.Ship;
import com.battleship.util.Constants;
import com.battleship.util.SoundManager;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.awt.Point;
import java.util.Random;
import java.util.function.Consumer;

/**
 * JavaFX-oriented game controller.
 */
public class GameController {
    private final SoundManager sound;
    private Player humanPlayer;
    private Player aiPlayer;
    private GameState state;
    private boolean hardMode;
    private EasyAI easyAI;
    private HardAI hardAI;

    private Runnable onPlayerTurnStart;
    private Runnable onAiTurnStart;
    private Runnable onBoardUpdate;
    private Consumer<Boolean> onGameOver;
    private Runnable onShowSetup;
    private Runnable onShowGame;
    private Consumer<Boolean> onShowResult;

    public GameController(SoundManager sound) {
        this.sound = sound;
    }

    public void startNewGame(boolean hardMode) {
        this.hardMode = hardMode;
        this.humanPlayer = new Player("Player");
        this.aiPlayer = new Player("AI");
        this.state = GameState.SETUP;
        if (hardMode) { hardAI = new HardAI(); easyAI = null; }
        else { easyAI = new EasyAI(); hardAI = null; }
        placeAIShipsRandom();
        showScreen(Constants.SCREEN_SETUP);
    }

    public void placeAIShipsRandom() {
        Random rng = new Random();
        aiPlayer.getBoard().reset();
        for (Ship ship : aiPlayer.getShips()) ship.reset();
        for (Ship ship : aiPlayer.getShips()) {
            boolean placed = false;
            while (!placed) {
                boolean h = rng.nextBoolean();
                int row = rng.nextInt(Constants.GRID_SIZE);
                int col = rng.nextInt(Constants.GRID_SIZE);
                if (aiPlayer.getBoard().canPlaceShip(row, col, ship.getSize(), h)) {
                    ship.place(row, col, h);
                    aiPlayer.getBoard().placeShip(ship);
                    placed = true;
                }
            }
        }
    }

    public void onSetupComplete() {
        state = GameState.PLAYER_TURN;
        showScreen(Constants.SCREEN_GAME);
        if (onPlayerTurnStart != null) onPlayerTurnStart.run();
    }

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
                if (hitShip.isSunk()) { sound.playSunk(); aiPlayer.getBoard().markSunk(hitShip); }
            }
            if (aiPlayer.allShipsSunk()) { state = GameState.PLAYER_WIN; if (onBoardUpdate != null) onBoardUpdate.run(); endGame(true); return true; }
        } else sound.playMiss();
        if (onBoardUpdate != null) onBoardUpdate.run();
        state = GameState.AI_TURN;
        if (onAiTurnStart != null) onAiTurnStart.run();
        scheduleAITurn();
        return true;
    }

    private void scheduleAITurn() {
        PauseTransition delay = new PauseTransition(Duration.millis(Constants.AI_DELAY_MS));
        delay.setOnFinished(e -> { if (state == GameState.AI_TURN) executeAITurn(); });
        delay.play();
    }

    private void executeAITurn() {
        Point shot = (hardAI != null) ? hardAI.chooseShot(humanPlayer.getBoard()) : easyAI.chooseShot(humanPlayer.getBoard());
        int row = shot.x, col = shot.y;
        boolean hit = humanPlayer.getBoard().fireAt(row, col);
        aiPlayer.recordShot(row, col);
        boolean sunk = false;
        if (hit) {
            sound.playHit();
            Ship hitShip = humanPlayer.getShipAt(row, col);
            if (hitShip != null) {
                hitShip.hit();
                if (hitShip.isSunk()) { sunk = true; sound.playSunk(); humanPlayer.getBoard().markSunk(hitShip); }
            }
        } else sound.playMiss();
        if (hardAI != null) hardAI.onShotResult(row, col, hit, sunk);
        if (onBoardUpdate != null) onBoardUpdate.run();
        if (humanPlayer.allShipsSunk()) { state = GameState.AI_WIN; endGame(false); return; }
        state = GameState.PLAYER_TURN;
        if (onPlayerTurnStart != null) onPlayerTurnStart.run();
    }

    private void endGame(boolean playerWon) {
        if (playerWon) sound.playWin(); else sound.playLose();
        if (onGameOver != null) onGameOver.accept(playerWon);
        showScreen(Constants.SCREEN_RESULT);
    }

    public void showScreen(String screenName) {
        switch (screenName) {
            case Constants.SCREEN_SETUP: if (onShowSetup != null) onShowSetup.run(); break;
            case Constants.SCREEN_GAME: if (onShowGame != null) onShowGame.run(); break;
            case Constants.SCREEN_RESULT: if (onShowResult != null) onShowResult.accept(state == GameState.PLAYER_WIN); break;
        }
    }

    public void goToMenu() {
        state = GameState.MENU;
        if (onShowSetup != null) onShowSetup.run();
    }
    public void rematch() { humanPlayer.reset(); aiPlayer.reset(); placeAIShipsRandom(); state = GameState.SETUP; showScreen(Constants.SCREEN_SETUP); }

    public Player getHumanPlayer() { return humanPlayer; }
    public Player getAiPlayer() { return aiPlayer; }
    public GameState getState() { return state; }
    public boolean isHardMode() { return hardMode; }
    public SoundManager getSound() { return sound; }

    public void setOnPlayerTurnStart(Runnable cb) { this.onPlayerTurnStart = cb; }
    public void setOnAiTurnStart(Runnable cb) { this.onAiTurnStart = cb; }
    public void setOnBoardUpdate(Runnable cb) { this.onBoardUpdate = cb; }
    public void setOnGameOver(Consumer<Boolean> cb) { this.onGameOver = cb; }
    public void setOnShowSetup(Runnable cb) { this.onShowSetup = cb; }
    public void setOnShowGame(Runnable cb) { this.onShowGame = cb; }
    public void setOnShowResult(Consumer<Boolean> cb) { this.onShowResult = cb; }
}
