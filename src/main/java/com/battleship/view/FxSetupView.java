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
        java.net.URL bgUrl = getClass().getResource("/ui/menu/setup_bg.png");
        if (bgUrl != null) {
            setStyle("-fx-background-image: url('" + bgUrl.toExternalForm() + "'); -fx-background-size: cover; -fx-background-position: center;");
        } else {
            setStyle("-fx-background-color: linear-gradient(to bottom, #0A1628, #0D2137);");
        }
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

    private StackPane createImgBtn(String path, double width) {
        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView();
        try {
            iv.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream(path)));
            iv.setPreserveRatio(true);
            iv.setFitWidth(width);
        } catch (Exception e) {
            System.err.println("Failed to load image: " + path);
        }
        
        StackPane pane = new StackPane(iv);
        pane.setCursor(Cursor.HAND);
        
        pane.setOnMouseEntered(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(150), pane);
            st.setToX(1.05); st.setToY(1.05); st.play();
        });
        pane.setOnMouseExited(e -> {
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(150), pane);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
        pane.setOnMousePressed(e -> {
            pane.setScaleX(1.08); pane.setScaleY(1.08);
        });
        pane.setOnMouseReleased(e -> {
            pane.setScaleX(1.05); pane.setScaleY(1.05);
        });
        
        return pane;
    }

    private Node buildTop() {
        BorderPane top = new BorderPane();
        top.setPadding(new Insets(0, 0, 12, 0));

        StackPane back = createImgBtn("/ui/menu/btn_backmenu.png", 120);
        back.setOnMouseClicked(e -> {
            controller.getSound().playClick();
            controller.goToMenu();
        });
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

        StackPane reset = createImgBtn("/ui/menu/btn_reset.png", 140);
        StackPane auto = createImgBtn("/ui/menu/btn_auto.png", 140);
        
        reset.setOnMouseClicked(e -> {
            controller.getSound().playClick();
            setupController.resetPlacement();
            refresh();
        });
        auto.setOnMouseClicked(e -> {
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

    private void drawGrid() {
        gridPane.getChildren().removeIf(node -> "SHIP_GRAPHIC".equals(node.getId()));

        for (Node node : gridPane.getChildren()) {
            if ("SHIP_GRAPHIC".equals(node.getId())) continue;
            Color fill = Color.web("#1E5F8C"); // Default ocean color
            node.setStyle("-fx-background-color: " + toRgba(fill) + "; -fx-border-color: rgba(41,121,255,0.45); -fx-border-width: 0.5;");
        }

        double cellSize = Constants.CELL_SIZE;
        
        // Draw placed ships
        for (Ship ship : controller.getHumanPlayer().getShips()) {
            if (ship.isPlaced()) {
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
                gridPane.getChildren().add(shipGraphic);
            }
        }
        
        // Draw preview ship
        if (hoverRow >= 0 && hoverCol >= 0 && setupController != null && !setupController.allShipsPlaced()) {
            Ship current = setupController.getCurrentShip();
            if (current != null) {
                int r = hoverRow;
                int c = hoverCol;
                if (setupController.isHorizontal() && c + current.getSize() > Constants.GRID_SIZE) {
                    c = Constants.GRID_SIZE - current.getSize();
                } else if (!setupController.isHorizontal() && r + current.getSize() > Constants.GRID_SIZE) {
                    r = Constants.GRID_SIZE - current.getSize();
                }

                boolean valid = setupController.canPlace(r, c);
                Node previewGraphic = createShipGraphic(current.getSize(), setupController.isHorizontal(), cellSize, true, valid);
                previewGraphic.setId("SHIP_GRAPHIC");
                GridPane.setRowIndex(previewGraphic, r);
                GridPane.setColumnIndex(previewGraphic, c);
                if (setupController.isHorizontal()) {
                    GridPane.setColumnSpan(previewGraphic, current.getSize());
                    GridPane.setRowSpan(previewGraphic, 1);
                } else {
                    GridPane.setRowSpan(previewGraphic, current.getSize());
                    GridPane.setColumnSpan(previewGraphic, 1);
                }
                gridPane.getChildren().add(previewGraphic);
            }
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
