package com.battleship;

import com.battleship.controller.GameController;
import com.battleship.model.GameState;
import com.battleship.util.Constants;
import com.battleship.util.SoundManager;
import com.battleship.view.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main.java – Application entry point.
 *
 * Wiring order:
 *  1. Create JFrame + CardLayout container
 *  2. Create SoundManager
 *  3. Create GameController (needs mainPanel + cardLayout)
 *  4. Create all Views (each receives the controller)
 *  5. Register views into CardLayout
 *  6. Wire screen-transition lifecycle callbacks onto the controller
 *  7. Show main menu
 */
public class Main {

    public static void main(String[] args) {
        // All Swing operations must run on the Event Dispatch Thread
        SwingUtilities.invokeLater(Main::launchGame);
    }

    private static void launchGame() {
        // ── Look and Feel ──────────────────────────────────────────────────
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { /* use default LnF if unavailable */ }

        // ── Root frame ─────────────────────────────────────────────────────
        JFrame frame = new JFrame(Constants.GAME_TITLE);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        frame.setMinimumSize(new Dimension(900, 650));
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);

        // Confirm before closing
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int opt = JOptionPane.showConfirmDialog(frame,
                        "Bạn có chắc muốn thoát game?", "Thoát",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (opt == JOptionPane.YES_OPTION) System.exit(0);
            }
        });

        // ── CardLayout container ───────────────────────────────────────────
        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(Constants.COLOR_OCEAN_DARK);

        // ── Services ───────────────────────────────────────────────────────
        SoundManager soundManager = new SoundManager();

        // ── Controller ─────────────────────────────────────────────────────
        GameController controller = new GameController(mainPanel, cardLayout, soundManager);

        // ── Views ──────────────────────────────────────────────────────────
        // Each view receives the controller.
        // We pass the frame reference to MainMenuView so it can open the
        // InstructionView dialog properly.
        MainMenuView menuView    = new MainMenuView(controller, frame);
        SetupView    setupView   = new SetupView(controller);
        GameView     gameView    = new GameView(controller);
        ResultView   resultView  = new ResultView(controller);

        // ── Register screens into CardLayout ───────────────────────────────
        mainPanel.add(menuView,   Constants.SCREEN_MENU);
        mainPanel.add(setupView,  Constants.SCREEN_SETUP);
        mainPanel.add(gameView,   Constants.SCREEN_GAME);
        mainPanel.add(resultView, Constants.SCREEN_RESULT);

        // ── Wire lifecycle callbacks ────────────────────────────────────────
        // GameController calls these after switching screens.
        controller.setOnShowSetup(() -> setupView.refresh());
        controller.setOnShowGame(()  -> gameView.refresh());
        controller.setOnShowResult(playerWon -> resultView.showResult(playerWon));

        // game-over callback: disable enemy board clicks
        controller.setOnGameOver(playerWon -> {
            // resultView.showResult is handled by setOnShowResult above
        });

        // ── Assemble and display ───────────────────────────────────────────
        frame.setContentPane(mainPanel);
        frame.setVisible(true);

        // Show main menu first
        cardLayout.show(mainPanel, Constants.SCREEN_MENU);

        System.out.println("==============================================");
        System.out.println("  ⚓ HẢI LỤC THIẾT THẦN – Battleship Game  ");
        System.out.println("==============================================");
        System.out.println("  Phím tắt:");
        System.out.println("  - Setup: Click để đặt tàu, R để xoay");
        System.out.println("  - Game : Click ô địch để bắn");
        System.out.println("==============================================");
    }
}
