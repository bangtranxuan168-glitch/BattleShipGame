package com.battleship.view;

import com.battleship.controller.GameController;
import com.battleship.controller.SetupController;
import com.battleship.model.Ship;
import com.battleship.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * SetupView.java – Ship placement screen.
 *
 * Features:
 *  - 10×10 interactive grid for ship placement
 *  - Hover preview showing valid (green) / invalid (red) placement
 *  - Press R to rotate ship orientation
 *  - Ship inventory panel showing remaining ships to place
 *  - AUTO button for random placement
 *  - RESET button to start over
 */
public class SetupView extends JPanel {

    private final GameController  gameController;
    private SetupController       setupController;

    // Grid state for hover preview
    private int hoverRow = -1;
    private int hoverCol = -1;

    // Ship inventory panel
    private JPanel      shipInventory;
    private JLabel      shipInfoLabel;
    private JLabel      orientLabel;
    private GridPanel   gridPanel;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public SetupView(GameController gameController) {
        this.gameController   = gameController;
        setLayout(new BorderLayout(0, 0));
        setBackground(Constants.COLOR_OCEAN_DARK);
        buildUI();
    }

    // ─── Refresh (called every time this screen is shown) ────────────────────

    /**
     * Re-initializes the SetupController and rebuilds the view for a fresh game.
     * Must be called when transitioning to this screen.
     */
    public void refresh() {
        setupController = new SetupController(gameController);
        hoverRow = -1;
        hoverCol = -1;
        refreshUI();
        gridPanel.repaint();
    }

    // ─── UI Construction ─────────────────────────────────────────────────────

    private void buildUI() {
        // ── Top bar ────────────────────────────────────────────────────────
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        // ── Center: grid + ship info ───────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(20, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Grid
        gridPanel = new GridPanel();
        center.add(gridPanel, BorderLayout.CENTER);

        // Right side panel (ships + orientation)
        JPanel rightPanel = createRightPanel();
        center.add(rightPanel, BorderLayout.EAST);

        add(center, BorderLayout.CENTER);

        // ── Bottom buttons ─────────────────────────────────────────────────
        JPanel bottomBar = createBottomBar();
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 20, 8, 20));

        JLabel title = new JLabel("⚓  TRIỂN KHAI TÀU CHIẾN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(Constants.COLOR_ACCENT);
        bar.add(title, BorderLayout.CENTER);

        JButton menuBtn = createSmallButton("← MENU");
        menuBtn.addActionListener(e -> gameController.goToMenu());
        bar.add(menuBtn, BorderLayout.WEST);

        return bar;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // Current ship info
        shipInfoLabel = new JLabel("", SwingConstants.LEFT);
        shipInfoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        shipInfoLabel.setForeground(Constants.COLOR_ACCENT);
        shipInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Orientation label
        orientLabel = new JLabel("Hướng: Ngang (R để xoay)", SwingConstants.LEFT);
        orientLabel.setFont(Constants.FONT_SMALL);
        orientLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
        orientLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(Box.createVerticalStrut(10));
        panel.add(createSectionLabel("ĐỘI TÀU"));
        panel.add(Box.createVerticalStrut(8));

        // Ship inventory
        shipInventory = new JPanel();
        shipInventory.setOpaque(false);
        shipInventory.setLayout(new BoxLayout(shipInventory, BoxLayout.Y_AXIS));
        panel.add(shipInventory);

        panel.add(Box.createVerticalStrut(20));
        panel.add(createSectionLabel("ĐANG ĐẶT"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(shipInfoLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(orientLabel);

        panel.add(Box.createVerticalStrut(16));
        panel.add(createSectionLabel("PHÍM TẮT"));
        panel.add(Box.createVerticalStrut(6));

        String[] hints = {"R → Xoay tàu", "Click → Đặt tàu", "Hover → Xem trước"};
        for (String h : hints) {
            JLabel hl = new JLabel(h);
            hl.setFont(Constants.FONT_SMALL);
            hl.setForeground(Constants.COLOR_TEXT_SECONDARY);
            hl.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(hl);
        }

        return panel;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
        bar.setOpaque(false);

        JButton autoBtn  = createActionButton("⚡  AUTO", Constants.COLOR_BTN_EASY, Constants.COLOR_BTN_EASY_HOV);
        JButton resetBtn = createActionButton("↺  RESET", Constants.COLOR_BTN_NEUTRAL, Constants.COLOR_BTN_HOV);

        autoBtn.addActionListener(e -> {
            gameController.getSound().playClick();
            setupController.autoPlace();
            refreshUI();
            gridPanel.repaint();
        });

        resetBtn.addActionListener(e -> {
            gameController.getSound().playClick();
            setupController.resetPlacement();
            refresh();
        });

        bar.add(resetBtn);
        bar.add(autoBtn);
        return bar;
    }

    // ─── UI State Updates ─────────────────────────────────────────────────────

    public void refreshUI() {
        // Update ship inventory
        shipInventory.removeAll();
        java.util.List<Ship> ships = gameController.getHumanPlayer().getShips();
        for (Ship ship : ships) {
            JPanel row = createShipRow(ship);
            shipInventory.add(row);
            shipInventory.add(Box.createVerticalStrut(4));
        }

        // Update current ship info
        Ship current = (setupController != null) ? setupController.getCurrentShip() : null;
        if (current != null) {
            shipInfoLabel.setText(current.getName() + " (" + current.getSize() + " ô)");
        } else {
            shipInfoLabel.setText("Hoàn tất!");
        }

        // Update orientation label
        if (setupController != null) {
            orientLabel.setText("Hướng: " + (setupController.isHorizontal() ? "Ngang" : "Dọc") + "  (R)");
        }

        shipInventory.revalidate();
        shipInventory.repaint();
    }

    /** Creates a small ship row showing its name, size, and placed status. */
    private JPanel createShipRow(Ship ship) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(190, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel(ship.getName());
        name.setFont(new Font("Arial", Font.PLAIN, 13));
        name.setForeground(ship.isPlaced() ? new Color(0x66BB6A) : Constants.COLOR_TEXT_PRIMARY);

        // Size indicator cells
        JPanel cells = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        cells.setOpaque(false);
        for (int i = 0; i < ship.getSize(); i++) {
            JPanel cell = new JPanel();
            cell.setPreferredSize(new Dimension(12, 12));
            cell.setBackground(ship.isPlaced() ? new Color(0x546E7A) : new Color(0x1565C0));
            cell.setBorder(BorderFactory.createLineBorder(new Color(0x29, 0x79, 0xFF, 100), 1));
            cells.add(cell);
        }

        JLabel status = new JLabel(ship.isPlaced() ? "✔" : "○");
        status.setForeground(ship.isPlaced() ? new Color(0x66BB6A) : new Color(0x546E7A));
        status.setFont(new Font("Arial", Font.BOLD, 14));

        row.add(status, BorderLayout.WEST);
        row.add(name,   BorderLayout.CENTER);
        row.add(cells,  BorderLayout.EAST);

        return row;
    }

    // ─── Inner class: GridPanel ───────────────────────────────────────────────

    /**
     * Custom panel that renders the 10×10 placement grid and handles mouse interaction.
     */
    private class GridPanel extends JPanel {

        public GridPanel() {
            int size = Constants.GRID_SIZE * Constants.CELL_SIZE + Constants.GRID_OFFSET + 2;
            setPreferredSize(new Dimension(size, size));
            setOpaque(false);
            setFocusable(true);

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    Point cell = pixelToCell(e.getX(), e.getY());
                    if (cell != null) {
                        hoverRow = cell.x;
                        hoverCol = cell.y;
                    } else {
                        hoverRow = -1; hoverCol = -1;
                    }
                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (setupController == null || setupController.allShipsPlaced()) return;
                    Point cell = pixelToCell(e.getX(), e.getY());
                    if (cell != null) {
                        boolean ok = setupController.placeShip(cell.x, cell.y);
                        if (ok) {
                            gameController.getSound().playClick();
                            refreshUI();
                        }
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverRow = -1; hoverCol = -1;
                    repaint();
                }
            });

            // R key → rotate
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_R && setupController != null) {
                        setupController.toggleOrientation();
                        refreshUI();
                        repaint();
                    }
                }
            });

            // Request focus so key events work
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { requestFocusInWindow(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int off = Constants.GRID_OFFSET;
            int cs  = Constants.CELL_SIZE;

            // Draw column labels (A-J)
            g2.setFont(Constants.FONT_SMALL);
            g2.setColor(Constants.COLOR_TEXT_SECONDARY);
            String[] cols = {"A","B","C","D","E","F","G","H","I","J"};
            for (int c = 0; c < Constants.GRID_SIZE; c++) {
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(cols[c], off + c * cs + (cs - fm.stringWidth(cols[c])) / 2, off - 8);
            }

            // Draw row labels (1-10)
            for (int r = 0; r < Constants.GRID_SIZE; r++) {
                String label = String.valueOf(r + 1);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, off - fm.stringWidth(label) - 6,
                        off + r * cs + (cs + fm.getAscent()) / 2 - 2);
            }

            // Calculate hover preview cells
            List<Point> previewCells = null;
            boolean validPlacement = false;
            if (hoverRow >= 0 && hoverCol >= 0 && setupController != null && !setupController.allShipsPlaced()) {
                previewCells  = setupController.previewCells(hoverRow, hoverCol);
                validPlacement = setupController.canPlace(hoverRow, hoverCol);
            }

            // Draw cells
            int[][] grid = gameController.getHumanPlayer().getBoard().getGrid();
            for (int r = 0; r < Constants.GRID_SIZE; r++) {
                for (int c = 0; c < Constants.GRID_SIZE; c++) {
                    int x = off + c * cs;
                    int y = off + r * cs;

                    // Determine cell color
                    Color fill;
                    boolean inPreview = previewCells != null && previewCells.contains(new Point(r, c));

                    if (inPreview) {
                        fill = validPlacement ? Constants.COLOR_CELL_VALID : Constants.COLOR_CELL_INVALID;
                    } else if (grid[r][c] == Constants.SHIP) {
                        fill = Constants.COLOR_CELL_SHIP;
                    } else {
                        fill = Constants.COLOR_CELL_WATER;
                    }

                    // Fill cell
                    g2.setColor(fill);
                    g2.fillRect(x + 1, y + 1, cs - 2, cs - 2);

                    // Border
                    g2.setColor(Constants.COLOR_CELL_BORDER);
                    g2.setStroke(new BasicStroke(0.8f));
                    g2.drawRect(x, y, cs, cs);
                }
            }

            // Outer grid border
            g2.setColor(new Color(0x29, 0x79, 0xFF, 160));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(off, off, Constants.GRID_SIZE * cs, Constants.GRID_SIZE * cs);
        }

        /** Converts pixel coords to grid (row, col), or null if outside grid. */
        Point pixelToCell(int px, int py) {
            int off = Constants.GRID_OFFSET;
            int cs  = Constants.CELL_SIZE;

            int col = (px - off) / cs;
            int row = (py - off) / cs;

            if (row < 0 || row >= Constants.GRID_SIZE || col < 0 || col >= Constants.GRID_SIZE) return null;
            return new Point(row, col);
        }
    }

    // ─── Button helpers ───────────────────────────────────────────────────────

    private JButton createSmallButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? Constants.COLOR_BTN_HOV : Constants.COLOR_BTN_NEUTRAL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(110, 34));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createActionButton(String text, Color normal, Color hover) {
        JButton btn = new JButton(text) {
            private Color cur = normal;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { cur = hover; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { cur = normal; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(cur);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(Constants.FONT_BUTTON);
        btn.setPreferredSize(new Dimension(160, 44));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 12));
        lbl.setForeground(new Color(0x2979FF));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
}
