package com.battleship.view;

import com.battleship.controller.GameController;
import com.battleship.util.Constants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * First-pass JavaFX result screen.
 */
public class FxResultView extends BorderPane {

    private final GameController controller;
    private final Label resultLabel = new Label();

    public FxResultView(GameController controller) {
        this.controller = controller;
        setPadding(new Insets(20));
        java.net.URL bgUrl = getClass().getResource("/ui/menu/setup_bg.png");
        if (bgUrl != null) {
            setStyle("-fx-background-image: url('" + bgUrl.toExternalForm() + "'); -fx-background-size: cover; -fx-background-position: center;");
        } else {
            setStyle("-fx-background-color: transparent;");
        }
        buildUi();
    }

    public void showResult(boolean playerWon) {
        resultLabel.setText(playerWon ? "🏆 BẠN ĐÃ CHIẾN THẮNG!" : "💥 BẠN ĐÃ THẤT BẠI!");
        resultLabel.setTextFill(playerWon ? Color.web("#FFD76A") : Color.web("#FF8A80"));
    }

    private void buildUi() {
        VBox box = new VBox(24);
        box.setAlignment(Pos.CENTER);

        resultLabel.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 36));
        resultLabel.setTextFill(Color.WHITE);

        Label sub = new Label("Trận chiến đã kết thúc");
        sub.setTextFill(Color.web("#C7D7E5"));
        sub.setFont(Font.font("System", 18));

        StackPane rematch = createImgBtn("/ui/menu/btn_replay.png", 200);
        rematch.setOnMouseClicked(e -> controller.rematch());

        StackPane menu = createImgBtn("/ui/menu/btn_backmenu.png", 200);
        menu.setOnMouseClicked(e -> controller.goToMenu());

        VBox buttonsBox = new VBox(16);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.getChildren().addAll(rematch, menu);
        
        box.getChildren().addAll(resultLabel, sub, buttonsBox);
        setCenter(box);
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
        pane.setCursor(javafx.scene.Cursor.HAND);
        
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
}
