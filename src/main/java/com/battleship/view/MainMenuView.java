package com.battleship.view;

import com.battleship.controller.GameController;
import com.battleship.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * MainMenuView.java – The main menu screen.
 *
 * Features:
 *  - Animated title with gradient text
 *  - Easy / Hard mode buttons
 *  - Instructions, Sound toggle, Exit buttons
 *  - Hover animations on all buttons
 *  - Ocean-themed animated background
 */
public class MainMenuView extends JPanel {

    private final GameController controller;
    private final Frame          parentFrame;  // needed to open InstructionView dialog

    // Animation state for background wave effect
    private float waveOffset = 0f;
    private Timer waveTimer;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public MainMenuView(GameController controller, Frame parentFrame) {
        this.controller  = controller;
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(Constants.COLOR_OCEAN_DARK);
        initComponents();
        startWaveAnimation();
    }

    // ─── UI Initialization ───────────────────────────────────────────────────

    private void initComponents() {
        // Title area (top 40%)
        JPanel titlePanel = createTitlePanel();

        // Button area (center)
        JPanel buttonPanel = createButtonPanel();

        // Footer
        JPanel footerPanel = createFooterPanel();

        add(titlePanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    /** Creates the animated title label panel. */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBackground(g);
                drawTitle(g);
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 260));
        return panel;
    }

    private void drawTitle(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();

        // ── Main title ──
        Font titleFont = new Font("Arial", Font.BOLD, 52);
        g2.setFont(titleFont);
        String title = "⚓ HẢI LỤC THIẾT THẦN";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(title)) / 2;

        // Gold gradient text
        GradientPaint gp = new GradientPaint(tx, 80, new Color(0xFFD700),
                tx + fm.stringWidth(title), 80, new Color(0xFFA500));
        g2.setPaint(gp);
        g2.drawString(title, tx, 120);

        // ── Shadow effect ──
        g2.setPaint(new Color(0, 0, 0, 80));
        g2.drawString(title, tx + 3, 123);

        // ── Subtitle ──
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(new Color(0x90CAF9));
        String sub = "Nhóm 4 – Kiên Tùng Bằng";
        fm = g2.getFontMetrics();
        g2.drawString(sub, (w - fm.stringWidth(sub)) / 2, 155);

        // Decorative divider
        g2.setColor(new Color(0x29, 0x79, 0xFF, 150));
        g2.setStroke(new BasicStroke(2f));
        int lineW = 300;
        g2.drawLine((w - lineW) / 2, 170, (w + lineW) / 2, 170);
    }

    /** Creates the main action buttons in the center. */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Transparent – the parent MainMenuView paints the ocean background
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ── Difficulty row ─────────────────────────────────────────────────
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        row.setOpaque(false);

        MenuButton easyBtn = new MenuButton("🌊  DỄ  (EASY)", Constants.COLOR_BTN_EASY, Constants.COLOR_BTN_EASY_HOV);
        MenuButton hardBtn = new MenuButton("🔥  KHÓ  (HARD)", Constants.COLOR_BTN_HARD, Constants.COLOR_BTN_HARD_HOV);
        easyBtn.setPreferredSize(new Dimension(200, 54));
        hardBtn.setPreferredSize(new Dimension(200, 54));

        easyBtn.addActionListener(e -> {
            controller.getSound().playClick();
            controller.startNewGame(false);
        });
        hardBtn.addActionListener(e -> {
            controller.getSound().playClick();
            controller.startNewGame(true);
        });

        row.add(easyBtn);
        row.add(hardBtn);
        panel.add(row, gbc);

        // ── Secondary buttons ──────────────────────────────────────────────
        gbc.gridy = 1;
        MenuButton instrBtn = new MenuButton("📖  HƯỚNG DẪN", Constants.COLOR_BTN_NEUTRAL, Constants.COLOR_BTN_HOV);
        instrBtn.setPreferredSize(new Dimension(260, 46));
        instrBtn.addActionListener(e -> {
            controller.getSound().playClick();
            // Show as modal JDialog overlay
            InstructionView.show(parentFrame, controller);
        });
        JPanel instrRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        instrRow.setOpaque(false);
        instrRow.add(instrBtn);
        panel.add(instrRow, gbc);

        // ── Sound toggle ───────────────────────────────────────────────────
        gbc.gridy = 2;
        MenuButton soundBtn = new MenuButton(getSoundLabel(), Constants.COLOR_BTN_NEUTRAL, Constants.COLOR_BTN_HOV);
        soundBtn.setPreferredSize(new Dimension(260, 46));
        soundBtn.addActionListener(e -> {
            controller.getSound().toggleSound();
            soundBtn.setText(getSoundLabel());
        });
        JPanel soundRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        soundRow.setOpaque(false);
        soundRow.add(soundBtn);
        panel.add(soundRow, gbc);

        // ── Exit ───────────────────────────────────────────────────────────
        gbc.gridy = 3;
        MenuButton exitBtn = new MenuButton("❌  THOÁT", new Color(0x3E0000), new Color(0x7B0000));
        exitBtn.setPreferredSize(new Dimension(260, 46));
        exitBtn.addActionListener(e -> System.exit(0));
        JPanel exitRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        exitRow.setOpaque(false);
        exitRow.add(exitBtn);
        panel.add(exitRow, gbc);

        return panel;
    }

    private String getSoundLabel() {
        return controller.getSound().isSoundEnabled() ? "🔊  ÂM THANH: BẬT" : "🔇  ÂM THANH: TẮT";
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 40));

        JLabel footer = new JLabel("© 2024 Battleship – Java Swing MVC Edition", SwingConstants.CENTER);
        footer.setFont(Constants.FONT_SMALL);
        footer.setForeground(new Color(0x546E7A));
        panel.setLayout(new BorderLayout());
        panel.add(footer, BorderLayout.CENTER);
        return panel;
    }

    // ─── Background drawing ───────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
    }

    private void drawBackground(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;  // guard: panel not laid out yet

        // Paint the full panel dark ocean
        g2.setColor(Constants.COLOR_OCEAN_DARK);
        g2.fillRect(0, 0, w, h);

        // Subtle radial glow in center
        float radius = Math.max(w, h) * 0.6f;
        if (radius > 0) {
            Point center = new Point(w / 2, h / 2);
            RadialGradientPaint rgp = new RadialGradientPaint(
                    center, radius,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0x0D2C4A), Constants.COLOR_OCEAN_DARK}
            );
            g2.setPaint(rgp);
            g2.fillRect(0, 0, w, h);
        }

        // Wave lines
        g2.setColor(new Color(0x15, 0x65, 0xC0, 30));
        g2.setStroke(new BasicStroke(1f));
        int numWaves = 8;
        for (int i = 0; i < numWaves; i++) {
            int y = (int)(h * (i + 0.5f) / numWaves + waveOffset) % h;
            g2.drawLine(0, y, w, y);
        }
    }

    // ─── Wave Animation ───────────────────────────────────────────────────────

    private void startWaveAnimation() {
        waveTimer = new Timer(50, e -> {
            waveOffset = (waveOffset + 0.5f) % getHeight();
            repaint();
        });
        waveTimer.start();
    }

    public void stopAnimation() {
        if (waveTimer != null) waveTimer.stop();
    }

    // ─── Inner: MenuButton ────────────────────────────────────────────────────

    /**
     * A styled button with hover animation (background color transition).
     */
    static class MenuButton extends JButton {

        private Color normalColor;
        private Color hoverColor;
        private Color currentColor;
        private float hoverProgress = 0f;
        private Timer hoverTimer;

        public MenuButton(String text, Color normal, Color hover) {
            super(text);
            this.normalColor  = normal;
            this.hoverColor   = hover;
            this.currentColor = normal;

            setForeground(Constants.COLOR_BTN_TEXT);
            setFont(Constants.FONT_BUTTON);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { animateHover(true); }
                @Override public void mouseExited(MouseEvent e)  { animateHover(false); }
            });

            hoverTimer = new Timer(16, e -> {
                repaint();
            });
        }

        private void animateHover(boolean entering) {
            if (hoverTimer != null) hoverTimer.stop();
            hoverTimer = new Timer(16, null);
            final float target = entering ? 1f : 0f;
            final float step   = entering ? 0.12f : -0.12f;

            hoverTimer.addActionListener(e -> {
                hoverProgress = Math.max(0, Math.min(1, hoverProgress + step));
                currentColor = interpolateColor(normalColor, hoverColor, hoverProgress);
                repaint();
                if (hoverProgress == target) hoverTimer.stop();
            });
            hoverTimer.start();
        }

        private Color interpolateColor(Color c1, Color c2, float t) {
            int r = (int)(c1.getRed()   + (c2.getRed()   - c1.getRed())   * t);
            int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
            int b = (int)(c1.getBlue()  + (c2.getBlue()  - c1.getBlue())  * t);
            return new Color(r, g, b);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 12;
            // Background
            g2.setColor(currentColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));

            // Top highlight
            g2.setColor(new Color(255, 255, 255, 25));
            g2.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() / 2, arc, arc));

            // Border
            g2.setColor(new Color(255, 255, 255, 60));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, arc, arc));

            // Text
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
            int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }
}
