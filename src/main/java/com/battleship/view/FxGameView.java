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
        java.net.URL bgUrl = getClass().getResource("/ui/menu/setup_bg.png");
        if (bgUrl != null) {
            setStyle("-fx-background-image: url('" + bgUrl.toExternalForm() + "'); -fx-background-size: cover; -fx-background-position: center;");
        } else {
            setStyle("-fx-background-color: linear-gradient(to bottom, #081322, #0A1628);");
        }
        buildUi();
        refresh();

        controller.setOnBoardUpdate(() -> javafx.application.Platform.runLater(() -> {
            updateFleetStatus();
            drawBoards();
        }));

        controller.setOnPlayerTurnStart((isHitAgain) -> javafx.application.Platform.runLater(() -> {
            enemyClickable = true;
            if (isHitAgain) {
                statusLabel.setText("💥 BẮN TRÚNG! Bạn được bắn tiếp!");
                statusLabel.setStyle("-fx-background-color: rgba(46,125,50,0.85); -fx-background-radius: 14;");
            } else {
                statusLabel.setText("⚔ LƯỢT CỦA BẠN – Chọn ô để bắn!");
                statusLabel.setStyle("-fx-background-color: rgba(21,101,192,0.85); -fx-background-radius: 14;");
            }
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

    private Node createShipGraphic(int size, boolean isHorizontal, double cellSize, boolean isPreview, boolean isValid) {
        double length = size * cellSize;
        double thickness = cellSize;
        
        StackPane innerPane = new StackPane();
        innerPane.setPrefSize(length, thickness);
        innerPane.setMinSize(length, thickness);
        innerPane.setMaxSize(length, thickness);

        java.net.URL imgUrl = getClass().getResource("/ui/menu/tau" + size + ".png");
        if (imgUrl != null) {
            javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(new javafx.scene.image.Image(imgUrl.toExternalForm()));
            iv.setFitWidth(length);
            iv.setFitHeight(thickness);
            innerPane.getChildren().add(iv);
        } else {
            // Procedural hull fallback
            javafx.scene.shape.Rectangle hull = new javafx.scene.shape.Rectangle(length - cellSize*0.15, thickness - cellSize*0.15);
            hull.setArcWidth(cellSize * 0.8);
            hull.setArcHeight(cellSize * 0.8);
            
            Color baseColor = Color.web("#90A4AE");
            if (isPreview) baseColor = isValid ? Color.web("#81C784") : Color.web("#E57373");
            
            hull.setFill(new javafx.scene.paint.LinearGradient(0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE, 
                new javafx.scene.paint.Stop(0, baseColor.brighter()), 
                new javafx.scene.paint.Stop(1, baseColor.darker())));
            hull.setStroke(Color.web("#455A64"));
            hull.setStrokeWidth(cellSize * 0.05);
            
            javafx.scene.shape.Rectangle bridge = new javafx.scene.shape.Rectangle(length * 0.25, thickness * 0.5);
            bridge.setFill(Color.web("#546E7A"));
            bridge.setArcWidth(cellSize * 0.2); 
            bridge.setArcHeight(cellSize * 0.2);

            javafx.scene.shape.Circle turret1 = new javafx.scene.shape.Circle(cellSize * 0.15, Color.web("#37474F"));
            javafx.scene.shape.Circle turret2 = new javafx.scene.shape.Circle(cellSize * 0.15, Color.web("#37474F"));
            
            innerPane.getChildren().addAll(hull, bridge, turret1, turret2);
            StackPane.setAlignment(turret1, Pos.CENTER_LEFT);
            StackPane.setAlignment(turret2, Pos.CENTER_RIGHT);
            StackPane.setMargin(turret1, new Insets(0, 0, 0, cellSize * 0.4));
            StackPane.setMargin(turret2, new Insets(0, cellSize * 0.4, 0, 0));
        }

        if (isPreview) {
            innerPane.setOpacity(0.8);
            if (!isValid && imgUrl != null) {
                javafx.scene.shape.Rectangle tint = new javafx.scene.shape.Rectangle(length, thickness);
                tint.setFill(Color.web("#FF0000", 0.4));
                tint.setArcWidth(cellSize * 0.8); tint.setArcHeight(cellSize * 0.8);
                innerPane.getChildren().add(tint);
            }
        } else {
            innerPane.setEffect(new javafx.scene.effect.DropShadow(4, Color.color(0,0,0,0.5)));
        }

        javafx.scene.Group wrapper = new javafx.scene.Group(innerPane);
        if (!isHorizontal) {
            innerPane.setRotate(90);
        }
        
        StackPane finalPane = new StackPane(wrapper);
        double finalW = isHorizontal ? length : thickness;
        double finalH = isHorizontal ? thickness : length;
        finalPane.setPrefSize(finalW, finalH);
        finalPane.setMinSize(finalW, finalH);
        finalPane.setMaxSize(finalW, finalH);
        finalPane.setMouseTransparent(true);
        
        return finalPane;
    }

    private void drawBoards() {
        if (controller.getHumanPlayer() == null || controller.getAiPlayer() == null) return;
        drawBoard(playerGrid, controller.getHumanPlayer(), false);
        drawBoard(enemyGrid, controller.getAiPlayer(), true);
    }

    private void drawBoard(GridPane grid, Player player, boolean enemy) {
        Board board = player.getBoard();
        grid.getChildren().removeIf(node -> "SHIP_GRAPHIC".equals(node.getId()));
        
        int[][] cells = board.getGrid();
        for (Node node : grid.getChildren()) {
            if ("SHIP_GRAPHIC".equals(node.getId())) continue;
            int[] rc = (int[]) node.getUserData();
            if (rc == null) continue;
            int r = rc[0];
            int c = rc[1];
            int state = cells[r][c];
            if ((enemy || true) && state == Constants.SHIP) state = Constants.EMPTY; // Always draw cells as ocean, ships are overlaid

            boolean hover = enemy && enemyClickable && hoverRow == r && hoverCol == c && !board.isAlreadyFired(r, c);
            String key = r + ":" + c;
            int anim = hitAnimations.getOrDefault(key, -1);

            Color fill = cellColor(state, hover);
            node.setStyle(baseCellStyle(fill));

            if (node instanceof StackPane) {
                StackPane pane = (StackPane) node;
                pane.getChildren().clear();
                if (cells[r][c] == Constants.HIT || cells[r][c] == Constants.SUNK) { // Must check true state, not masked state
                    Label mark = new Label("✕");
                    mark.setTextFill(Color.WHITE);
                    mark.setFont(Font.font("System", FontWeight.BOLD, 18 + (anim >= 0 ? 8 : 0)));
                    if (anim >= 0) mark.setRotate((anim * 15) % 360);
                    pane.getChildren().add(mark);
                } else if (cells[r][c] == Constants.MISS) {
                    Circle dot = new Circle(4 + (anim >= 0 ? 2 : 0), Color.web("#90A4AE"));
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
        
        double cellSize = Constants.CELL_SIZE;
        for (Ship ship : player.getShips()) {
            if (ship.isPlaced()) {
                // If enemy, only show SUNK ships. If player, show all placed ships.
                if (enemy && !ship.isSunk()) continue;
                
                Node shipGraphic = createShipGraphic(ship.getSize(), ship.isHorizontal(), cellSize, false, true);
                shipGraphic.setId("SHIP_GRAPHIC");
                GridPane.setRowIndex(shipGraphic, ship.getRow());
                GridPane.setColumnIndex(shipGraphic, ship.getCol());
                if (ship.isHorizontal()) {
                    GridPane.setColumnSpan(shipGraphic, ship.getSize());
                    GridPane.setRowSpan(shipGraphic, 1);
                } else {
                    GridPane.setRowSpan(shipGraphic, ship.getSize());
                    GridPane.setColumnSpan(shipGraphic, 1);
                }
                
                if (ship.isSunk()) {
                    shipGraphic.setEffect(new javafx.scene.effect.ColorAdjust(0, -0.8, -0.4, 0)); // Darken sunken ships
                }
                
                grid.getChildren().add(shipGraphic);
                shipGraphic.toBack(); // keep below hit markers
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
