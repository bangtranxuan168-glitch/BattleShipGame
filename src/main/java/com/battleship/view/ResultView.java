package com.battleship.view;

import com.battleship.controller.GameController;
import com.battleship.model.GameState;
import com.battleship.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * ResultView.java – Game result screen (WIN / LOSE).
 *
 * Features:
 *  - Large animated YOU WIN / YOU LOSE banner
 *  - Player vs Rival avatar display
 *  - Crown icon on winner
 *  - Rematch button → restarts from setup screen
 *  - Back to Menu button
 *  - Particle confetti effect on WIN
 */
public class ResultView extends JPanel {

    private final GameController controller;

    private JLabel resultLabel;
    private JLabel subLabel;

    // Confetti animation
    private Timer confettiTimer;
    private final java.util.List<ConfettiParticle> particles = new java.util.ArrayList<>();
    private boolean playerWon = false;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public ResultView(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(Constants.COLOR_OCEAN_DARK);
        buildUI();
    }

    // ─── Show result ─────────────────────────────────────────────────────────

    /**
     * Called when navigating to this screen with the outcome.
     * @param playerWon true if human player won
     */
    public void showResult(boolean playerWon) {
        this.playerWon = playerWon;

        if (playerWon) {
            resultLabel.setText("🏆  YOU WIN!");
            resultLabel.setForeground(new Color(0xFFD700));
            subLabel.setText("Xuất sắc! Bạn đã tiêu diệt toàn bộ hạm đội địch!");
            startConfetti();
        } else {
            resultLabel.setText("💀  YOU LOSE");
            resultLabel.setForeground(new Color(0xEF5350));
            subLabel.setText("Hạm đội của bạn đã bị đánh chìm. Thử lại nào!");
            stopConfetti();
        }
        repaint();
    }

    // ─── UI Construction ─────────────────────────────────────────────────────

    private void buildUI() {
        JPanel center = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                drawBackground(g);
                if (playerWon && !particles.isEmpty()) drawConfetti(g);
            }
        };
        center.setOpaque(false);
        add(center, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        // ── Result banner ─────────────────────────────────────────────────
        resultLabel = new JLabel("", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 56));
        resultLabel.setForeground(Constants.COLOR_ACCENT);
        gbc.gridy = 0;
        center.add(resultLabel, gbc);

        subLabel = new JLabel("", SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        subLabel.setForeground(Constants.COLOR_TEXT_SECONDARY);
        gbc.gridy = 1;
        center.add(subLabel, gbc);

        // ── Avatar cards ──────────────────────────────────────────────────
        JPanel avatarPanel = createAvatarPanel();
        gbc.gridy = 2;
        center.add(avatarPanel, gbc);

        // ── Buttons ───────────────────────────────────────────────────────
        JPanel btnPanel = createButtonPanel();
        gbc.gridy = 3;
        center.add(btnPanel, gbc);
    }

    private JPanel createAvatarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 0));
        panel.setOpaque(false);

        panel.add(createAvatarCard("👨‍✈️", "NGƯỜI CHƠI", !playerWon));
        panel.add(createAvatarCard("🤖", "RIVAL AI",    playerWon));

        return panel;
    }

    private JPanel createAvatarCard(String icon, String name, boolean isLoser) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Card background
                g2.setColor(new Color(0x0D2137));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                // Border
                Color borderColor = isLoser ? new Color(0x54, 0x6E, 0x7A, 120) : new Color(0xFF, 0xD7, 0x00, 200);
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 18, 18);
                g2.dispose();
            }
        };
        card.setPreferredSize(new Dimension(140, 160));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        card.setOpaque(false);

        // Crown for winner
        JLabel crown = new JLabel(!isLoser ? "👑" : " ", SwingConstants.CENTER);
        crown.setFont(new Font("Arial", Font.PLAIN, 24));
        crown.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Avatar emoji
        JLabel avatar = new JLabel(icon, SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Name
        JLabel nameLbl = new JLabel(name, SwingConstants.CENTER);
        nameLbl.setFont(new Font("Arial", Font.BOLD, 13));
        nameLbl.setForeground(isLoser ? Constants.COLOR_TEXT_SECONDARY : Constants.COLOR_ACCENT);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Win/Lose tag
        JLabel tag = new JLabel(isLoser ? "❌ THUA" : "✅ THẮNG", SwingConstants.CENTER);
        tag.setFont(new Font("Arial", Font.BOLD, 12));
        tag.setForeground(isLoser ? new Color(0xEF5350) : new Color(0x66BB6A));
        tag.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(crown);
        card.add(avatar);
        card.add(Box.createVerticalStrut(6));
        card.add(nameLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(tag);

        return card;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setOpaque(false);

        JButton rematchBtn = createStyledButton("⚡  CHƠI LẠI", Constants.COLOR_BTN_EASY, Constants.COLOR_BTN_EASY_HOV);
        rematchBtn.addActionListener(e -> {
            controller.getSound().playClick();
            stopConfetti();
            controller.rematch();
            // Notify SetupView to refresh
        });

        JButton menuBtn = createStyledButton("🏠  VỀ MENU", Constants.COLOR_BTN_NEUTRAL, Constants.COLOR_BTN_HOV);
        menuBtn.addActionListener(e -> {
            controller.getSound().playClick();
            stopConfetti();
            controller.goToMenu();
        });

        panel.add(menuBtn);
        panel.add(rematchBtn);
        return panel;
    }

    // ─── Background ───────────────────────────────────────────────────────────

    private void drawBackground(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // Dark ocean gradient
        GradientPaint gp = new GradientPaint(0, 0, new Color(0x0A1628),
                0, getHeight(), new Color(0x0D2137));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    // ─── Confetti ────────────────────────────────────────────────────────────

    private void startConfetti() {
        particles.clear();
        for (int i = 0; i < 60; i++) {
            particles.add(new ConfettiParticle(getWidth(), getHeight()));
        }
        if (confettiTimer != null) confettiTimer.stop();
        confettiTimer = new Timer(16, e -> {
            for (ConfettiParticle p : particles) p.update(getHeight());
            repaint();
        });
        confettiTimer.start();
    }

    private void stopConfetti() {
        particles.clear();
        if (confettiTimer != null) confettiTimer.stop();
    }

    private void drawConfetti(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        for (ConfettiParticle p : particles) p.draw(g2);
        g2.dispose();
    }

    /** A single confetti particle. */
    private static class ConfettiParticle {
        private double x, y, vx, vy;
        private final Color color;
        private double angle, angularVelocity;
        private final java.util.Random rng = new java.util.Random();

        ConfettiParticle(int width, int height) {
            reset(width, height, true);
            Color[] colors = {
                new Color(0xFFD700), new Color(0xFF4081), new Color(0x40C4FF),
                new Color(0x69F0AE), new Color(0xFF6D00), new Color(0xEA80FC)
            };
            color = colors[rng.nextInt(colors.length)];
        }

        void reset(int w, int h, boolean fromTop) {
            x  = rng.nextInt(w);
            y  = fromTop ? -10 : rng.nextInt(h);
            vx = (rng.nextDouble() - 0.5) * 2;
            vy = rng.nextDouble() * 3 + 1;
            angularVelocity = (rng.nextDouble() - 0.5) * 0.2;
        }

        void update(int height) {
            x += vx; y += vy; angle += angularVelocity;
            if (y > height + 10) { reset(400, height, true); }
        }

        void draw(Graphics2D g2) {
            g2.setColor(color);
            var t = AffineTransform.getRotateInstance(angle, x, y);
            g2.transform(t);
            g2.fillRect((int)x - 4, (int)y - 4, 8, 4);
            g2.setTransform(new AffineTransform()); // reset
        }
    }

    // ─── Styled button helper ─────────────────────────────────────────────────

    private JButton createStyledButton(String text, Color normal, Color hover) {
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(Constants.FONT_BUTTON);
        btn.setPreferredSize(new Dimension(180, 50));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
