package com.battleship.view;

import com.battleship.controller.GameController;
import com.battleship.model.Board;
import com.battleship.model.Player;
import com.battleship.model.Ship;
import com.battleship.util.Constants;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.awt.Point;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * JavaFX game screen with looping video background.
 */
public class FxGameView extends BorderPane {

    private final GameController controller;

    private final Label statusLabel = new Label("⚔ LƯỢT CỦA BẠN – Chọn ô để bắn!");
    private final Label playerShipsLabel = new Label();
    private final Label enemyShipsLabel = new Label();

    private final GridPane playerGrid = new GridPane();
    private final GridPane enemyGrid = new GridPane();

    private final Map<String, Integer> hitAnimations = new HashMap<>();
    private final Timeline animationTimeline;

    private boolean enemyClickable = true;
    private int hoverRow = -1;
    private int hoverCol = -1;

    public FxGameView(GameController controller) {
        this.controller = controller;
        setPadding(new Insets(16));
        setStyle("-fx-background-color: transparent;");
        buildUi();
        refresh();

        controller.setOnBoardUpdate(() -> javafx.application.Platform.runLater(() -> {
            updateFleetStatus();
            drawBoards();
        }));

        controller.setOnPlayerTurnStart(() -> javafx.application.Platform.runLater(() -> {
            enemyClickable = true;
            statusLabel.setText("⚔ LƯỢT CỦA BẠN – Chọn ô để bắn!");
            statusLabel.setStyle("-fx-background-color: rgba(21,101,192,0.85); -fx-background-radius: 14;");
            drawBoards();
        }));

        controller.setOnAiTurnStart(() -> javafx.application.Platform.runLater(() -> {
            enemyClickable = false;
            statusLabel.setText("🤖 LƯỢT AI – Đang suy nghĩ...");
            statusLabel.setStyle("-fx-background-color: rgba(100,50,10,0.85); -fx-background-radius: 14;");
        }));

        animationTimeline = new Timeline(new KeyFrame(Duration.millis(80), e -> updateAnimations()));
        animationTimeline.setCycleCount(Timeline.INDEFINITE);
        animationTimeline.play();
    }

    public void refresh() {
        hitAnimations.clear();
        enemyClickable = true;
        statusLabel.setText("⚔ LƯỢT CỦA BẠN – Chọn ô để bắn!");
        updateFleetStatus();
        drawBoards();
    }

    private void buildUi() {
        VBox content = buildContent();
        setCenter(content);
    }

    private VBox buildContent() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(0));

        Node top = buildTop();
        Node center = buildCenter();
        Node bottom = buildBottom();

        content.getChildren().addAll(top, center, bottom);
        VBox.setVgrow(center, Priority.ALWAYS);
        return content;
    }

    private Node buildTop() {
        VBox top = new VBox(10);
        top.setAlignment(Pos.CENTER);

        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 22));
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setPadding(new Insets(12));
        statusLabel.setStyle("-fx-background-color: rgba(21,101,192,0.85); -fx-background-radius: 14;");

        top.getChildren().add(statusLabel);
        return top;
    }

    private Node buildCenter() {
        HBox center = new HBox(20);
        center.setAlignment(Pos.TOP_CENTER);
        center.setPadding(new Insets(14, 0, 14, 0));

        playerGrid.setHgap(0);
        playerGrid.setVgap(0);
        enemyGrid.setHgap(0);
        enemyGrid.setVgap(0);

        center.getChildren().addAll(boardBox("BẠN", playerGrid, false), boardBox("ĐỊCH", enemyGrid, true));
        return center;
    }

    private Node buildBottom() {
        HBox bottom = new HBox(20);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(8, 0, 0, 0));

        playerShipsLabel.setTextFill(Color.web("#90CAF9"));
        enemyShipsLabel.setTextFill(Color.web("#EF9A9A"));
        playerShipsLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        enemyShipsLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        bottom.getChildren().addAll(playerShipsLabel, enemyShipsLabel);
        return bottom;
    }

    private VBox boardBox(String title, GridPane grid, boolean enemy) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: rgba(6,12,20,0.55); -fx-background-radius: 18;");

        Label lbl = new Label(title);
        lbl.setTextFill(enemy ? Color.web("#EF9A9A") : Color.web("#90CAF9"));
        lbl.setFont(Font.font("System", FontWeight.BOLD, 16));

        StackPane wrapper = new StackPane(grid);
        wrapper.setPadding(new Insets(6));

        buildGrid(grid, enemy);
        box.getChildren().addAll(lbl, wrapper);
        return box;
    }

    private void buildGrid(GridPane grid, boolean enemy) {
        grid.getChildren().clear();
        int cs = Constants.CELL_SIZE;
        for (int r = 0; r < Constants.GRID_SIZE; r++) {
            for (int c = 0; c < Constants.GRID_SIZE; c++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(cs, cs);
                cell.setMinSize(cs, cs);
                cell.setMaxSize(cs, cs);
                cell.setUserData(new int[]{r, c});
                cell.setStyle(baseCellStyle());

                if (enemy) {
                    cell.setOnMouseEntered(e -> {
                        int[] rc = (int[]) cell.getUserData();
                        hoverRow = rc[0];
                        hoverCol = rc[1];
                        drawBoards();
                    });
                    cell.setOnMouseExited(e -> {
                        hoverRow = -1;
                        hoverCol = -1;
                        drawBoards();
                    });
                    cell.setOnMouseClicked(this::handleEnemyClick);
                    cell.setCursor(Cursor.CROSSHAIR);
                }

                grid.add(cell, c, r);
            }
        }
    }

    private void updateFleetStatus() {
        Player human = controller.getHumanPlayer();
        Player ai = controller.getAiPlayer();
        if (human == null || ai == null) return;
        long hAlive = human.getAliveShipCount();
        long aAlive = ai.getAliveShipCount();
        playerShipsLabel.setText("🚢 Tàu của bạn còn: " + hAlive + "/" + human.getShips().size());
        enemyShipsLabel.setText("💥 Tàu địch còn: " + aAlive + "/" + ai.getShips().size());
    }

    private void drawBoards() {
        if (controller.getHumanPlayer() == null || controller.getAiPlayer() == null) return;
        drawBoard(playerGrid, controller.getHumanPlayer().getBoard(), false);
        drawBoard(enemyGrid, controller.getAiPlayer().getBoard(), true);
    }

    private void drawBoard(GridPane grid, Board board, boolean enemy) {
        int[][] cells = board.getGrid();
        for (Node node : grid.getChildren()) {
            int[] rc = (int[]) node.getUserData();
            int r = rc[0];
            int c = rc[1];
            int state = cells[r][c];
            if (enemy && state == Constants.SHIP) state = Constants.EMPTY;

            boolean hover = enemy && enemyClickable && hoverRow == r && hoverCol == c && !board.isAlreadyFired(r, c);
            String key = r + ":" + c;
            int anim = hitAnimations.getOrDefault(key, -1);

            Color fill = cellColor(state, hover);
            node.setStyle(baseCellStyle(fill));

            if (node instanceof StackPane) {
                StackPane pane = (StackPane) node;
                pane.getChildren().clear();
                if (state == Constants.HIT || state == Constants.SUNK) {
                    Label mark = new Label("✕");
                    mark.setTextFill(Color.WHITE);
                    mark.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 18));
                    pane.getChildren().add(mark);
                } else if (state == Constants.MISS) {
                    Circle dot = new Circle(5, Color.web("#B0BEC5"));
                    pane.getChildren().add(dot);
                }

                if (anim >= 0) {
                    Circle pulse = new Circle(8 + anim * 3, Color.TRANSPARENT);
                    pulse.setStroke(Color.web("#FFD54F", Math.max(0.15, 1.0 - anim * 0.18)));
                    pulse.setStrokeWidth(2);
                    pane.getChildren().add(pulse);
                }
            }
        }
    }

    private void handleEnemyClick(MouseEvent event) {
        if (!enemyClickable) return;
        StackPane cell = (StackPane) event.getSource();
        int[] rc = (int[]) cell.getUserData();
        boolean ok = controller.playerFire(rc[0], rc[1]);
        if (ok) {
            triggerHitAnimation(rc[0], rc[1]);
            enemyClickable = false;
            statusLabel.setText("🤖 LƯỢT AI – Đang suy nghĩ...");
            drawBoards();
        }
    }

    private void triggerHitAnimation(int row, int col) {
        hitAnimations.put(row + ":" + col, 0);
    }

    private void updateAnimations() {
        if (hitAnimations.isEmpty()) return;
        Map<String, Integer> next = new HashMap<>();
        for (Map.Entry<String, Integer> e : hitAnimations.entrySet()) {
            int v = e.getValue() + 1;
            if (v < Constants.ANIM_FRAMES) next.put(e.getKey(), v);
        }
        hitAnimations.clear();
        hitAnimations.putAll(next);
        drawBoards();
    }

    private Color cellColor(int state, boolean hover) {
        switch (state) {
            case Constants.SHIP: return Color.web("#546E7A");
            case Constants.HIT: return Color.web("#E53935");
            case Constants.MISS: return Color.web("#78909C");
            case Constants.SUNK: return Color.web("#B71C1C");
            default: return hover ? Color.web("#4FC3F7", 0.72) : Color.web("#1E5F8C", 0.82);
        }
    }

    private String baseCellStyle() { return baseCellStyle(Color.web("#1E5F8C", 0.82)); }

    private String baseCellStyle(Color fill) {
        return "-fx-background-color: " + toRgba(fill) + "; -fx-border-color: rgba(41,121,255,0.35); -fx-border-width: 0.5;";
    }

    private String toRgba(Color c) {
        return String.format("rgba(%d,%d,%d,%.3f)", (int) Math.round(c.getRed() * 255), (int) Math.round(c.getGreen() * 255), (int) Math.round(c.getBlue() * 255), c.getOpacity());
    }
}
