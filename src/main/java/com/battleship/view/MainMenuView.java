package com.battleship.view;

import com.battleship.controller.GameController;
import com.battleship.util.Constants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MainMenuView extends StackPane {
    public MainMenuView(GameController controller, Runnable startEasy, Runnable startHard, Runnable showHelp) {
        setStyle("-fx-background-color: linear-gradient(to bottom, #081322, #0A1628);");
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(520);
        box.setPadding(new Insets(30));
        box.setStyle("-fx-background-color: rgba(6,12,20,0.30); -fx-background-radius: 24;");

        Label title = new Label("⚓ HẢI LỤC THIẾT THẦN");
        title.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 34));
        title.setTextFill(Color.web("#FFD76A"));
        Label subtitle = new Label("BATTLESHIP");
        subtitle.setFont(Font.font("System", FontWeight.BOLD, 15));
        subtitle.setTextFill(Color.web("#D7E8F7"));

        Button easy = btn("🌊  DỄ", "#0E5CAB", "#1188D6", e -> startEasy.run());
        Button hard = btn("🔥  KHÓ", "#8E1212", "#C62828", e -> startHard.run());
        Button help = btn("📖  HƯỚNG DẪN", "#1E2F44", "#2F425B", e -> showHelp.run());
        Button sound = btn("🔊  ÂM THANH", "#1E2F44", "#2F425B", e -> {});
        Button exit = btn("❌  THOÁT", "#3A111A", "#6D1A28", e -> System.exit(0));

        box.getChildren().addAll(title, subtitle, spacer(10), easy, hard, help, sound, exit);
        getChildren().add(box);
    }

    private Button btn(String text, String from, String to, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button b = new Button(text);
        b.setMinHeight(48);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle(styleFor(from, to));
        b.setOnAction(action);
        return b;
    }

    private String styleFor(String from, String to) {
        return "-fx-background-radius: 14; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15; " +
                "-fx-background-color: linear-gradient(to bottom, " + from + ", " + to + ");";
    }

    private Region spacer(double h) { Region r = new Region(); r.setMinHeight(h); return r; }
}
