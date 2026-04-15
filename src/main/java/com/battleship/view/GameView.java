package com.battleship.view;

import com.battleship.model.Board;
import com.battleship.model.GameState;
import com.battleship.model.Player;
import com.battleship.model.Ship;
import com.battleship.util.Constants;
import com.battleship.controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.HashMap;
import java.util.Map;

/**
 * GameView.java – The main gameplay screen.
 *
 * Layout:
 *  ┌───────────────────────────────────────────────────────┐
 *  │  Status bar: "YOUR TURN" / "ENEMY TURN"              │
 *  ├─────────────────────────┬─────────────────────────────┤
 *  │   Player Board          │   Enemy Board               │
 *  │   (read-only)           │   (clickable)               │
 *  ├─────────────────────────┴─────────────────────────────┤
 *  │  Fleet status bars                                    │
 *  └───────────────────────────────────────────────────────┘
 *
 * Features:
 *  - Animated explosion effect on hit cells
 *  - Fire animation (missile + burst)
 *  - Board hover highlight on enemy board
 *  - Sunk ship overlay effect
 */
public class GameView extends JPanel {

    private final GameController controller;

    // Board panels
    private BoardPanel playerBoardPanel;
    private BoardPanel enemyBoardPanel;

    // Status
    private JLabel statusLabel;
    private JLabel playerShipsLabel;
    private JLabel enemyShipsLabel;

    // Hit animation: maps (row,col) to animation frame counter
    private final Map<Point, Integer> hitAnimations = new HashMap<>();
    private Timer animTimer;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public GameView(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(0, 0));
        setBackground(Constants.COLOR_OCEAN_DARK);
        buildUI();
        registerCallbacks();
        startAnimationTimer();
    }

    // ─── UI Construction ─────────────────────────────────────────────────────

    private void buildUI() {
        // Status bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.NORTH);

        // Boards area
        JPanel boardsArea = new JPanel(new GridLayout(1, 2, 20, 0)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Constants.COLOR_OCEAN_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        boardsArea.setOpaque(false);
        boardsArea.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        // Player board (left, read-only – shows own ships)
        playerBoardPanel = new BoardPanel(false, "BẠN");
        boardsArea.add(playerBoardPanel);

        // Enemy board (right, clickable)
        enemyBoardPanel = new BoardPanel(true, "ĐỊCH");
        boardsArea.add(enemyBoardPanel);

        add(boardsArea, BorderLayout.CENTER);

        // Fleet status footer
        JPanel footer = createFooter();
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 20, 6, 20));

        statusLabel = new JLabel("YOUR TURN", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Glowing background
                Color bg = getText().contains("BẠN") || getText().contains("YOUR")
                        ? new Color(0x15, 0x65, 0xC0, 200) : new Color(0xB7, 0x1C, 0x1C, 200);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        statusLabel.setFont(new Font("Arial", Font.BOLD, 22));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setPreferredSize(new Dimension(0, 46));

        bar.add(statusLabel, BorderLayout.CENTER);
        return bar;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new GridLayout(1, 2, 20, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(6, 20, 10, 20));

        playerShipsLabel = new JLabel("", SwingConstants.CENTER);
        playerShipsLabel.setFont(new Font("Arial", Font.BOLD, 13));
        playerShipsLabel.setForeground(new Color(0x90CAF9));

        enemyShipsLabel = new JLabel("", SwingConstants.CENTER);
        enemyShipsLabel.setFont(new Font("Arial", Font.BOLD, 13));
        enemyShipsLabel.setForeground(new Color(0xEF9A9A));

        footer.add(playerShipsLabel);
        footer.add(enemyShipsLabel);

        return footer;
    }

    // ─── Callbacks ────────────────────────────────────────────────────────────

    private void registerCallbacks() {
        controller.setOnPlayerTurnStart(() -> SwingUtilities.invokeLater(() -> {
            statusLabel.setText("⚔  LƯỢT CỦA BẠN – Chọn ô để bắn!");
            enemyBoardPanel.setClickable(true);
            updateFleetStatus();
            repaint();
        }));

        controller.setOnAiTurnStart(() -> SwingUtilities.invokeLater(() -> {
            statusLabel.setText("🤖  LƯỢT AI – Đang suy nghĩ...");
            enemyBoardPanel.setClickable(false);
            repaint();
        }));

        controller.setOnBoardUpdate(() -> SwingUtilities.invokeLater(() -> {
            playerBoardPanel.repaint();
            enemyBoardPanel.repaint();
            updateFleetStatus();
        }));

        controller.setOnGameOver(playerWon -> SwingUtilities.invokeLater(() -> {
            enemyBoardPanel.setClickable(false);
        }));
    }

    /** Refreshes the fleet status labels. */
    public void updateFleetStatus() {
        Player human = controller.getHumanPlayer();
        Player ai    = controller.getAiPlayer();

        long hAlive = human.getShips().stream().filter(s -> !s.isSunk()).count();
        long aAlive = ai.getShips().stream().filter(s -> !s.isSunk()).count();

        playerShipsLabel.setText("🚢 Tàu của bạn còn: " + hAlive + "/" + human.getShips().size());
        enemyShipsLabel.setText("💥 Tàu địch còn: " + aAlive + "/" + ai.getShips().size());
    }

    /** Called when this screen is shown – initializes status and labels. */
    public void refresh() {
        statusLabel.setText("⚔  LƯỢT CỦA BẠN – Chọn ô để bắn!");
        enemyBoardPanel.setClickable(true);
        hitAnimations.clear();
        updateFleetStatus();
        playerBoardPanel.repaint();
        enemyBoardPanel.repaint();
    }

    // ─── Animation Timer ─────────────────────────────────────────────────────

    private void startAnimationTimer() {
        animTimer = new Timer(Constants.ANIM_DELAY_MS, e -> {
            if (!hitAnimations.isEmpty()) {
                boolean changed = false;
                for (var entry : new HashMap<>(hitAnimations).entrySet()) {
                    int frame = entry.getValue() + 1;
                    if (frame >= Constants.ANIM_FRAMES) {
                        hitAnimations.remove(entry.getKey());
                    } else {
                        hitAnimations.put(entry.getKey(), frame);
                    }
                    changed = true;
                }
                if (changed) {
                    playerBoardPanel.repaint();
                    enemyBoardPanel.repaint();
                }
            }
        });
        animTimer.start();
    }

    /** Triggers a hit animation at the given cell on the enemy board. */
    public void triggerHitAnimation(int row, int col) {
        hitAnimations.put(new Point(row, col), 0);
    }

    // ─── Inner class: BoardPanel ───────────────────────────────────────────────

    /**
     * Renders a single 10×10 game board.
     *
     * @param isEnemy   if true, this is the enemy board (clickable, ships hidden)
     * @param playerLabel  label shown above the board
     */
    private class BoardPanel extends JPanel {

        private final boolean isEnemy;
        private final String  label;
        private boolean       clickable = false;

        // Hover
        private int hoverRow = -1, hoverCol = -1;

        public BoardPanel(boolean isEnemy, String label) {
            this.isEnemy = isEnemy;
            this.label   = label;

            setOpaque(false);
            setLayout(new BorderLayout());

            if (isEnemy) {
                addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        Point cell = pixelToCell(e.getX(), e.getY());
                        if (cell != null) { hoverRow = cell.x; hoverCol = cell.y; }
                        else              { hoverRow = -1; hoverCol = -1; }
                        repaint();
                    }
                });

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (!clickable) return;
                        Point cell = pixelToCell(e.getX(), e.getY());
                        if (cell != null) {
                            boolean success = controller.playerFire(cell.x, cell.y);
                            if (success) triggerHitAnimation(cell.x, cell.y);
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hoverRow = -1; hoverCol = -1; repaint();
                    }
                });
            }
        }

        public void setClickable(boolean b) {
            this.clickable = b;
            setCursor(b ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
                        : Cursor.getDefaultCursor());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Calculate usable dimensions so board is centered
            int totalW = getWidth();
            int totalH = getHeight();
            int boardPx = Constants.GRID_SIZE * Constants.CELL_SIZE;
            int off     = Constants.GRID_OFFSET;

            int startX  = (totalW - boardPx - off) / 2;
            int startY  = 40;  // space for label

            // ── Board title ───────────────────────────────────────────────
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.setColor(isEnemy ? new Color(0xEF9A9A) : new Color(0x90CAF9));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, startX + off + (boardPx - fm.stringWidth(label)) / 2, startY - 8);

            // ── Column labels ─────────────────────────────────────────────
            g2.setFont(Constants.FONT_SMALL);
            g2.setColor(Constants.COLOR_TEXT_SECONDARY);
            String[] cols = {"A","B","C","D","E","F","G","H","I","J"};
            for (int c = 0; c < Constants.GRID_SIZE; c++) {
                fm = g2.getFontMetrics();
                g2.drawString(cols[c],
                        startX + off + c * Constants.CELL_SIZE + (Constants.CELL_SIZE - fm.stringWidth(cols[c])) / 2,
                        startY + off - 8);
            }

            // ── Row labels ────────────────────────────────────────────────
            for (int r = 0; r < Constants.GRID_SIZE; r++) {
                String rowLbl = String.valueOf(r + 1);
                fm = g2.getFontMetrics();
                g2.drawString(rowLbl,
                        startX + off - fm.stringWidth(rowLbl) - 4,
                        startY + off + r * Constants.CELL_SIZE + (Constants.CELL_SIZE + fm.getAscent()) / 2 - 2);
            }

            // ── Cells ─────────────────────────────────────────────────────
            Board board = isEnemy ? controller.getAiPlayer().getBoard()
                                  : controller.getHumanPlayer().getBoard();
            int[][] grid = board.getGrid();
            int cs = Constants.CELL_SIZE;

            for (int r = 0; r < Constants.GRID_SIZE; r++) {
                for (int c = 0; c < Constants.GRID_SIZE; c++) {
                    int px = startX + off + c * cs;
                    int py = startY + off + r * cs;

                    int state = grid[r][c];

                    // On enemy board: hide SHIP cells (show as EMPTY)
                    int drawState = (isEnemy && state == Constants.SHIP) ? Constants.EMPTY : state;

                    // Hover effect on enemy board
                    boolean isHover = isEnemy && hoverRow == r && hoverCol == c && clickable
                            && !board.isAlreadyFired(r, c);

                    drawCell(g2, px, py, cs, drawState, isHover, r, c);
                }
            }

            // ── Outer border ──────────────────────────────────────────────
            g2.setColor(new Color(0x29, 0x79, 0xFF, 160));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(startX + off, startY + off, boardPx, boardPx);
        }

        /** Draws a single board cell with the correct state color and effects. */
        private void drawCell(Graphics2D g2, int px, int py, int cs,
                              int state, boolean hover, int row, int col) {
            Color fill;
            String symbol = null;
            Color symbolColor = null;

            switch (state) {
                case Constants.EMPTY:
                    fill = hover ? Constants.COLOR_CELL_HOVER : Constants.COLOR_CELL_WATER;
                    break;
                case Constants.SHIP:
                    fill = Constants.COLOR_CELL_SHIP;
                    break;
                case Constants.HIT:
                    fill = Constants.COLOR_CELL_HIT;
                    symbol = "✕";
                    symbolColor = Color.WHITE;
                    break;
                case Constants.MISS:
                    fill = Constants.COLOR_CELL_MISS;
                    symbol = "○";
                    symbolColor = new Color(0xB0BEC5);
                    break;
                case Constants.SUNK:
                    fill = Constants.COLOR_CELL_SUNK;
                    symbol = "✕";
                    symbolColor = new Color(0xFF8A80);
                    break;
                default:
                    fill = Constants.COLOR_CELL_WATER;
            }

            // Fill background
            g2.setColor(fill);
            g2.fillRect(px + 1, py + 1, cs - 2, cs - 2);

            // Draw hit animation (ripple effect)
            Point animKey = new Point(row, col);
            if (hitAnimations.containsKey(animKey)) {
                int frame = hitAnimations.get(animKey);
                float progress = (float) frame / Constants.ANIM_FRAMES;
                int radius = (int)(cs * 0.5f * progress);
                int alpha  = (int)(255 * (1 - progress));
                g2.setColor(new Color(255, 200, 0, alpha));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(px + cs / 2 - radius, py + cs / 2 - radius, radius * 2, radius * 2);
            }

            // Sunk fire particles
            if (state == Constants.SUNK) {
                drawFireEffect(g2, px, py, cs);
            }

            // Draw symbol
            if (symbol != null) {
                g2.setFont(new Font("Arial", Font.BOLD, cs - 14));
                g2.setColor(symbolColor);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(symbol, px + (cs - fm.stringWidth(symbol)) / 2,
                        py + (cs + fm.getAscent() - fm.getDescent()) / 2);
            }

            // Cell border
            g2.setColor(Constants.COLOR_CELL_BORDER);
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRect(px, py, cs, cs);
        }

        /** Draws a subtle animated fire/smoke effect for sunk ship cells. */
        private void drawFireEffect(Graphics2D g2, int px, int py, int cs) {
            // Simple flame simulation: orange-red gradient dots
            long time = System.currentTimeMillis();
            int numParticles = 3;
            for (int i = 0; i < numParticles; i++) {
                double angle = (time / 200.0 + i * Math.PI * 2 / numParticles) % (Math.PI * 2);
                int ox = (int)(Math.cos(angle) * cs * 0.15);
                int oy = (int)(Math.sin(angle) * cs * 0.15) - cs / 4;
                int r  = cs / 5;

                RadialGradientPaint flame = new RadialGradientPaint(
                        new Point2D.Float(px + cs / 2 + ox, py + cs / 2 + oy),
                        r,
                        new float[]{0f, 1f},
                        new Color[]{new Color(255, 160, 0, 180), new Color(200, 50, 0, 0)}
                );
                g2.setPaint(flame);
                g2.fillOval(px + cs / 2 + ox - r, py + cs / 2 + oy - r, r * 2, r * 2);
            }
        }

        /** Converts pixel coordinates (relative to this panel) to grid (row, col). */
        Point pixelToCell(int px, int py) {
            int totalW = getWidth();
            int boardPx = Constants.GRID_SIZE * Constants.CELL_SIZE;
            int off     = Constants.GRID_OFFSET;
            int startX  = (totalW - boardPx - off) / 2;
            int startY  = 40;

            int col = (px - (startX + off)) / Constants.CELL_SIZE;
            int row = (py - (startY + off)) / Constants.CELL_SIZE;

            if (row < 0 || row >= Constants.GRID_SIZE || col < 0 || col >= Constants.GRID_SIZE) return null;
            return new Point(row, col);
        }
    }
}
