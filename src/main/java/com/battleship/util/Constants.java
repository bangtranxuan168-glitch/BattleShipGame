package com.battleship.util;

import java.awt.Color;
import java.awt.Font;

/**
 * Constants.java
 * Centralized constants for the entire game.
 * Includes colors, fonts, grid sizes, and game configuration.
 */
public class Constants {

    // ─── GRID ────────────────────────────────────────────────────────────────
    public static final int GRID_SIZE      = 10;   // 10x10 board
    public static final int CELL_SIZE      = 46;   // pixels per cell
    public static final int GRID_OFFSET    = 30;   // label offset for A-J / 1-10

    // ─── BOARD CELL STATES ───────────────────────────────────────────────────
    public static final int EMPTY  = 0;
    public static final int SHIP   = 1;
    public static final int HIT    = 2;
    public static final int MISS   = 3;
    public static final int SUNK   = 4;   // sunk ship cell

    // ─── SHIP FLEET ──────────────────────────────────────────────────────────
    /** Ship sizes: Carrier=5, Battleship=4, Cruiser=3, Submarine=3, Destroyer=2 */
    public static final int[] SHIP_SIZES  = {5, 4, 3, 3, 2};
    public static final String[] SHIP_NAMES = {"Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};

    // ─── SCREEN NAMES (CardLayout keys) ──────────────────────────────────────
    public static final String SCREEN_MENU        = "MENU";
    public static final String SCREEN_INSTRUCTION = "INSTRUCTION";
    public static final String SCREEN_SETUP       = "SETUP";
    public static final String SCREEN_GAME        = "GAME";
    public static final String SCREEN_RESULT      = "RESULT";

    // ─── COLORS ──────────────────────────────────────────────────────────────
    // Deep ocean palette
    public static final Color COLOR_OCEAN_DARK    = new Color(0x0A1628);
    public static final Color COLOR_OCEAN_MID     = new Color(0x0D2137);
    public static final Color COLOR_OCEAN_LIGHT   = new Color(0x1A3A5C);

    // Grid cell colors
    public static final Color COLOR_CELL_WATER    = new Color(0x1E, 0x5F, 0x8C, 200);
    public static final Color COLOR_CELL_HOVER    = new Color(0x4F, 0xC3, 0xF7, 180);
    public static final Color COLOR_CELL_VALID    = new Color(0x66, 0xBB, 0x6A, 200);
    public static final Color COLOR_CELL_INVALID  = new Color(0xEF, 0x53, 0x50, 200);
    public static final Color COLOR_CELL_SHIP     = new Color(0x546E7A);
    public static final Color COLOR_CELL_HIT      = new Color(0xE53935);
    public static final Color COLOR_CELL_MISS     = new Color(0x78909C);
    public static final Color COLOR_CELL_SUNK     = new Color(0xB71C1C);
    public static final Color COLOR_CELL_BORDER   = new Color(0x29, 0x79, 0xFF, 120);

    // Button palette
    public static final Color COLOR_BTN_EASY      = new Color(0x1565C0);
    public static final Color COLOR_BTN_EASY_HOV  = new Color(0x1976D2);
    public static final Color COLOR_BTN_HARD      = new Color(0xB71C1C);
    public static final Color COLOR_BTN_HARD_HOV  = new Color(0xC62828);
    public static final Color COLOR_BTN_NEUTRAL   = new Color(0x263238);
    public static final Color COLOR_BTN_HOV       = new Color(0x37474F);
    public static final Color COLOR_BTN_TEXT      = Color.WHITE;
    public static final Color COLOR_ACCENT        = new Color(0xFFD700);   // gold

    // Text colors
    public static final Color COLOR_TEXT_PRIMARY  = new Color(0xECEFF1);
    public static final Color COLOR_TEXT_SECONDARY= new Color(0x90A4AE);

    // ─── FONTS ───────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE     = new Font("Arial", Font.BOLD, 42);
    public static final Font FONT_SUBTITLE  = new Font("Arial", Font.BOLD, 18);
    public static final Font FONT_BUTTON    = new Font("Arial", Font.BOLD, 16);
    public static final Font FONT_LABEL     = new Font("Arial", Font.PLAIN, 13);
    public static final Font FONT_CELL      = new Font("Arial", Font.BOLD, 18);
    public static final Font FONT_SMALL     = new Font("Arial", Font.PLAIN, 11);

    // ─── ANIMATION ───────────────────────────────────────────────────────────
    public static final int AI_DELAY_MS    = 1000;   // ms before AI fires
    public static final int ANIM_FRAMES   = 6;       // hit animation frames
    public static final int ANIM_DELAY_MS = 80;      // ms per animation frame

    // ─── WINDOW ──────────────────────────────────────────────────────────────
    public static final int WINDOW_WIDTH  = 1100;
    public static final int WINDOW_HEIGHT = 750;
    public static final String GAME_TITLE = "HẢI LỤC THIẾT THẦN";

    private Constants() { /* utility class */ }
}
