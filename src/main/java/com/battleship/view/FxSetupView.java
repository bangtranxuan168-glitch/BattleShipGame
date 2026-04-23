package com.battleship.view;

import com.battleship.controller.GameController;
import com.battleship.controller.SetupController;
import com.battleship.model.Ship;
import com.battleship.util.Constants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * First-pass JavaFX setup screen, ported from the Swing version.
 */
public class FxSetupView extends BorderPane {

    private final GameController controller;
    private SetupController setupController;

    private final Label currentShipLabel = new Label();
    private final Label orientationLabel = new Label();
    private final GridPane gridPane = new GridPane();
    private final VBox fleetBox = new VBox(6);

    private int hoverRow = -1;
    private int hoverCol = -1;

    public FxSetupView(GameController controller) {
        this.controller = controller;
        setPadding(new Insets(16));
        setStyle("-fx-background-color: transparent;");
        setFocusTraversable(true);

        buildUi();
        refresh();
    }

    public void refresh() {
        setupController = new SetupController(controller);
        hoverRow = -1;
        hoverCol = -1;
        refreshUi();
        drawGrid();
    }

    private void buildUi() {
        setTop(buildTop());
        setCenter(buildCenter());
        setBottom(buildBottom());

        addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.R && setupController != null) {
                setupController.toggleOrientation();
                refreshUi();
                drawGrid();
            }
        });
    }

    private Node buildTop() {
        BorderPane top = new BorderPane();
        top.setPadding(new Insets(0, 0, 12, 0));

        Button back = oceanButton("← MENU");
        back.setOnAction(e -> controller.goToMenu());
        top.setLeft(back);

        Label title = new Label("⚓ TRIỂN KHAI TÀU CHIẾN");
        title.setTextFill(Color.web("#FFD76A"));
        title.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 28));
        top.setCenter(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        return top;
    }

    private Node buildCenter() {
        HBox root = new HBox(20);
        root.setAlignment(Pos.TOP_CENTER);

        gridPane.setHgap(0);
        gridPane.setVgap(0);
        gridPane.setPadding(new Insets(20));
        gridPane.setStyle("-fx-background-color: rgba(10,22,40,0.58); -fx-background-radius: 20;");

        int size = Constants.GRID_SIZE;
        int cs = Constants.CELL_SIZE;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(cs, cs);
                cell.setMinSize(cs, cs);
                cell.setMaxSize(cs, cs);
                cell.setOnMouseEntered(e -> {
                    hoverRow = GridPane.getRowIndex(cell) == null ? 0 : GridPane.getRowIndex(cell);
                    hoverCol = GridPane.getColumnIndex(cell) == null ? 0 : GridPane.getColumnIndex(cell);
                    drawGrid();
                });
                cell.setOnMouseExited(e -> {
                    hoverRow = -1;
                    hoverCol = -1;
                    drawGrid();
                });
                cell.setOnMouseClicked(this::handleCellClick);
                gridPane.add(cell, c, r);
            }
        }

        VBox right = new VBox(12);
        right.setPrefWidth(240);
        right.setPadding(new Insets(10, 0, 0, 0));
        right.setStyle("-fx-background-color: rgba(6,12,20,0.30); -fx-background-radius: 16; -fx-padding: 16;");

        Label shipsTitle = sectionTitle("ĐỘI TÀU");
        Label placingTitle = sectionTitle("ĐANG ĐẶT");
        Label shortcutTitle = sectionTitle("PHÍM TẮT");

        currentShipLabel.setTextFill(Color.web("#FFD76A"));
        currentShipLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        orientationLabel.setTextFill(Color.web("#C7D7E5"));
        orientationLabel.setFont(Font.font("System", 12));

        fleetBox.setFillWidth(true);
        right.getChildren().addAll(shipsTitle, fleetBox, placingTitle, currentShipLabel, orientationLabel, shortcutTitle,
                hintLabel("R → Xoay tàu"), hintLabel("Click → Đặt tàu"), hintLabel("Hover → Xem trước"));

        root.getChildren().addAll(gridPane, right);
        return root;
    }

    private Node buildBottom() {
        HBox bottom = new HBox(12);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(12, 0, 0, 0));

        Button reset = oceanButton("↺ RESET");
        Button auto = oceanButton("⚡ AUTO");
        reset.setOnAction(e -> {
            controller.getSound().playClick();
            setupController.resetPlacement();
            refresh();
        });
        auto.setOnAction(e -> {
            controller.getSound().playClick();
            setupController.autoPlace();
            refreshUi();
            drawGrid();
        });

        bottom.getChildren().addAll(reset, auto);
        return bottom;
    }

    private void refreshUi() {
        fleetBox.getChildren().clear();
        for (Ship ship : controller.getHumanPlayer().getShips()) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            Label status = new Label(ship.isPlaced() ? "✔" : "○");
            status.setTextFill(ship.isPlaced() ? Color.web("#66BB6A") : Color.web("#78909C"));
            Label name = new Label(ship.getName() + " (" + ship.getSize() + ")");
            name.setTextFill(ship.isPlaced() ? Color.web("#66BB6A") : Color.web("#ECEFF1"));
            row.getChildren().addAll(status, name);
            fleetBox.getChildren().add(row);
        }

        Ship current = setupController.getCurrentShip();
        currentShipLabel.setText(current != null ? current.getName() + " (" + current.getSize() + " ô)" : "Hoàn tất!");
        orientationLabel.setText("Hướng: " + (setupController.isHorizontal() ? "Ngang" : "Dọc") + " (R)");
    }

    private void drawGrid() {
        int[][] grid = controller.getHumanPlayer().getBoard().getGrid();
        List<java.awt.Point> preview = (hoverRow >= 0 && hoverCol >= 0 && setupController != null && !setupController.allShipsPlaced())
                ? setupController.previewCells(hoverRow, hoverCol)
                : null;
        boolean valid = preview != null && setupController.canPlace(hoverRow, hoverCol);

        for (Node node : gridPane.getChildren()) {
            Integer row = GridPane.getRowIndex(node);
            Integer col = GridPane.getColumnIndex(node);
            int r = row == null ? 0 : row;
            int c = col == null ? 0 : col;
            boolean inPreview = preview != null && preview.contains(new java.awt.Point(r, c));
            Color fill = Color.web("#1E5F8C");
            if (inPreview) fill = valid ? Color.web("#66BB6A", 0.85) : Color.web("#EF5350", 0.85);
            else if (grid[r][c] == Constants.SHIP) fill = Color.web("#546E7A");

            node.setStyle("-fx-background-color: " + toRgba(fill) + "; -fx-border-color: rgba(41,121,255,0.45); -fx-border-width: 0.5;");
        }
    }

    private void handleCellClick(MouseEvent event) {
        if (setupController == null || setupController.allShipsPlaced()) return;
        Node cell = (Node) event.getSource();
        Integer row = GridPane.getRowIndex(cell);
        Integer col = GridPane.getColumnIndex(cell);
        if (row != null && col != null) {
            boolean ok = setupController.placeShip(row, col);
            if (ok) {
                controller.getSound().playClick();
                refreshUi();
                drawGrid();
            }
        }
    }

    private Button oceanButton(String text) {
        Button btn = new Button(text);
        btn.setCursor(Cursor.HAND);
        btn.setMinWidth(140);
        btn.setMinHeight(42);
        btn.setStyle("-fx-background-radius: 12; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-color: linear-gradient(to bottom, #263238, #37474F);");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-radius: 12; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-color: linear-gradient(to bottom, #37474F, #455A64);") );
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-radius: 12; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-color: linear-gradient(to bottom, #263238, #37474F);") );
        return btn;
    }

    private Label sectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setTextFill(Color.web("#2979FF"));
        lbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        return lbl;
    }

    private Label hintLabel(String text) {
        Label lbl = new Label(text);
        lbl.setTextFill(Color.web("#90A4AE"));
        lbl.setFont(Font.font("System", 11));
        return lbl;
    }

    private String toRgba(Color c) {
        return String.format("rgba(%d,%d,%d,%.3f)", (int) Math.round(c.getRed() * 255), (int) Math.round(c.getGreen() * 255), (int) Math.round(c.getBlue() * 255), c.getOpacity());
    }
}
