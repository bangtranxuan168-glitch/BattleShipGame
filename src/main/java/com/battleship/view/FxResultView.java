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
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);

        resultLabel.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 28));
        resultLabel.setTextFill(Color.WHITE);

        Label sub = new Label("Trận chiến đã kết thúc");
        sub.setTextFill(Color.web("#C7D7E5"));

        Button menu = new Button("← VỀ MENU");
        menu.setOnAction(e -> controller.goToMenu());
        menu.setStyle("-fx-background-radius: 12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: linear-gradient(to bottom, #263238, #37474F);");

        Button rematch = new Button("↺ CHƠI LẠI");
        rematch.setOnAction(e -> controller.rematch());
        rematch.setStyle("-fx-background-radius: 12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: linear-gradient(to bottom, #1565C0, #1976D2);");

        box.getChildren().addAll(resultLabel, sub, rematch, menu);
        setCenter(box);
    }
}
