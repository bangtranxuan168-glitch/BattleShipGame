package com.battleship.model;

/**
 * GameState.java – Enum representing all possible game states.
 *
 * The GameController transitions between these states throughout the game lifecycle.
 */
public enum GameState {

    /** Game has not started; showing main menu */
    MENU,

    /** Player is placing their ships on the board */
    SETUP,

    /** Active gameplay – player's turn to fire */
    PLAYER_TURN,

    /** Active gameplay – AI's turn to fire */
    AI_TURN,

    /** Game over – player destroyed all enemy ships */
    PLAYER_WIN,

    /** Game over – AI destroyed all player ships */
    AI_WIN
}
