package com.battleship.view;

import com.battleship.controller.GameController;
import com.battleship.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * InstructionView.java – Paginated instruction screen shown in a JDialog overlay.
 *
 * Features:
 *  - 3 pages: Game Rules / How to Place Ships / How to Fire
 *  - Dots indicator (● ○ ○) for pagination
 *  - Previous / Next navigation
 *  - Launched as a modal JDialog from the main menu
 */
public class InstructionView extends JDialog {

    private final GameController controller;

    // Pagination
    private static final int TOTAL_PAGES = 3;
    private int currentPage = 0;

    // UI references we need to update on page change
    private JPanel    contentPanel;
    private JLabel[]  dots;
    private JButton   prevBtn, nextBtn;

    // Page content
    private static final String[] PAGE_TITLES = {
        "📜  LUẬT CHƠI",
        "🚢  CÁCH ĐẶT TÀU",
        "🎯  CÁCH BẮN"
    };

    private static final String[][] PAGE_CONTENT = {
        {
            "• Mỗi người chơi có 1 bảng 10×10",
            "• Đội tàu gồm 5 chiếc: 5, 4, 3, 3, 2 ô",
            "• Người chơi và AI thay phiên nhau bắn",
            "• Bắn trúng → (X) màu đỏ",
            "• Bắn trượt → (O) màu xám",
            "• Khi tàu bị bắn hết tất cả ô → Chìm!",
            "• Ai tiêu diệt hết tàu địch trước → Thắng"
        },
        {
            "• Chọn tàu từ danh sách bên dưới",
            "• Di chuột lên bảng để xem vị trí",
            "• Nhấn chuột trái để đặt tàu",
            "• Nhấn phím  R  để xoay tàu (ngang/dọc)",
            "• Ô xanh lá = vị trí hợp lệ",
            "• Ô đỏ = vị trí không hợp lệ",
            "• Nhấn AUTO để đặt tự động",
            "• Nhấn RESET để đặt lại từ đầu"
        },
        {
            "• Sau khi đặt xong, trận đánh bắt đầu",
            "• Nhấn vào ô trên bảng của ĐỊCH để bắn",
            "• Chỉ bắn được khi là lượt của bạn",
            "• Khi tàu địch chìm → hiện toàn bộ tàu",
            "• AI Easy: bắn ngẫu nhiên",
            "• AI Hard: truy tìm tàu sau khi trúng",
            "• Tiêu diệt hết 5 tàu địch để thắng!"
        }
    };

    // ─── Constructor ─────────────────────────────────────────────────────────

    public InstructionView(Frame parent, GameController controller) {
        super(parent, "Hướng Dẫn Chơi", true);
        this.controller = controller;

        setSize(520, 460);
        setLocationRelativeTo(parent);
        setResizable(false);
        setUndecorated(true);  // we'll draw our own title bar

        initComponents();
    }

    // ─── UI ──────────────────────────────────────────────────────────────────

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Constants.COLOR_OCEAN_MID);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(0x29, 0x79, 0xFF, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // ── Title bar ─────────────────────────────────────────────────────
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        JLabel titleLbl = new JLabel(PAGE_TITLES[currentPage], SwingConstants.CENTER);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 22));
        titleLbl.setForeground(Constants.COLOR_ACCENT);
        titleBar.add(titleLbl, BorderLayout.CENTER);

        // Close button
        JButton closeBtn = createIconButton("✕");
        closeBtn.addActionListener(e -> dispose());
        titleBar.add(closeBtn, BorderLayout.EAST);

        root.add(titleBar, BorderLayout.NORTH);

        // ── Content ───────────────────────────────────────────────────────
        contentPanel = createContentPanel();
        root.add(contentPanel, BorderLayout.CENTER);

        // ── Bottom navigation ─────────────────────────────────────────────
        JPanel navPanel = createNavPanel();
        root.add(navPanel, BorderLayout.SOUTH);

        // Update title on page change
        addPropertyChangeListener("page", evt -> titleLbl.setText(PAGE_TITLES[currentPage]));

        setContentPane(root);
        setBackground(new Color(0, 0, 0, 0));
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0x0D2137));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        updateContentPanel(panel);
        return panel;
    }

    private void updateContentPanel(JPanel panel) {
        panel.removeAll();
        String[] lines = PAGE_CONTENT[currentPage];
        for (String line : lines) {
            JLabel lbl = new JLabel(line);
            lbl.setFont(new Font("Arial", Font.PLAIN, 15));
            lbl.setForeground(Constants.COLOR_TEXT_PRIMARY);
            lbl.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(lbl);
        }
        panel.revalidate();
        panel.repaint();
    }

    private JPanel createNavPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // Previous
        prevBtn = createNavButton("◀  TRƯỚC");
        prevBtn.addActionListener(e -> changePage(-1));
        prevBtn.setEnabled(currentPage > 0);

        // Dots indicator
        JPanel dotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        dotsPanel.setOpaque(false);
        dots = new JLabel[TOTAL_PAGES];
        for (int i = 0; i < TOTAL_PAGES; i++) {
            dots[i] = new JLabel(i == currentPage ? "●" : "○");
            dots[i].setFont(new Font("Arial", Font.PLAIN, 14));
            dots[i].setForeground(i == currentPage ? Constants.COLOR_ACCENT : Constants.COLOR_TEXT_SECONDARY);
            dotsPanel.add(dots[i]);
        }

        // Next
        nextBtn = createNavButton("TIẾP  ▶");
        nextBtn.addActionListener(e -> changePage(1));

        panel.add(prevBtn,   BorderLayout.WEST);
        panel.add(dotsPanel, BorderLayout.CENTER);
        panel.add(nextBtn,   BorderLayout.EAST);

        return panel;
    }

    private void changePage(int delta) {
        currentPage = Math.max(0, Math.min(TOTAL_PAGES - 1, currentPage + delta));

        // Update dots
        for (int i = 0; i < TOTAL_PAGES; i++) {
            dots[i].setText(i == currentPage ? "●" : "○");
            dots[i].setForeground(i == currentPage ? Constants.COLOR_ACCENT : Constants.COLOR_TEXT_SECONDARY);
        }

        // Update content
        updateContentPanel(contentPanel);

        // Update button states
        prevBtn.setEnabled(currentPage > 0);
        nextBtn.setEnabled(currentPage < TOTAL_PAGES - 1);

        // Fire property change so title updates
        firePropertyChange("page", -1, currentPage);
    }

    // ─── Button helpers ───────────────────────────────────────────────────────

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? Constants.COLOR_BTN_EASY : new Color(0x37474F));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(110, 36));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createIconButton(String icon) {
        JButton btn = new JButton(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0xB71C1C));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                }
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ─── Static factory ───────────────────────────────────────────────────────

    /**
     * Shows the instruction dialog as a modal overlay over the given frame.
     */
    public static void show(Frame parent, GameController controller) {
        InstructionView dlg = new InstructionView(parent, controller);
        dlg.setVisible(true);
    }
}
