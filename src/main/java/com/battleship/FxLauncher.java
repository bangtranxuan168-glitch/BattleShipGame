package com.battleship;

import com.battleship.controller.GameController;
import com.battleship.model.GameState;
import com.battleship.util.Constants;
import com.battleship.util.SoundManager;
import com.battleship.view.FxGameView;
import com.battleship.view.FxResultView;
import com.battleship.view.FxSetupView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;

public class FxLauncher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private MediaPlayer mediaPlayer;
    private GameController controller;
    private BorderPane root;
    private FxSetupView setupView;
    private FxGameView gameView;
    private FxResultView resultView;
    private StackPane menuLayer;

    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.setPrefSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        root.setPickOnBounds(false);

        Node bg = createBackground();
        VBox menu = createMenu(stage);

        menuLayer = new StackPane(menu);
        menuLayer.setPickOnBounds(false);
        StackPane.setAlignment(menu, Pos.BOTTOM_CENTER);
        StackPane.setMargin(menu, new Insets(0, 0, 34, 0));

        StackPane stack = new StackPane(bg, root, menuLayer);
        Scene scene = new Scene(stack, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        scene.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.R && setupView != null) setupView.requestFocus(); });

        stage.setTitle(Constants.GAME_TITLE);
        stage.setScene(scene);
        stage.show();
    }

    private void showMenu(boolean visible) {
        if (menuLayer != null) menuLayer.setVisible(visible);
    }

    private Node createBackground() {
        URL videoUrl = getClass().getResource("/ui/menu/videobg.mp4");
        if (videoUrl == null) videoUrl = getClass().getResource("/ui/menu/bg.mp4");
        if (videoUrl == null) videoUrl = getClass().getResource("/ui/menu/Tao_Video_Theo_Yeu_Cau.mp4");
        if (videoUrl == null) {
            Region fallback = new Region();
            fallback.setPrefSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            fallback.setBackground(new Background(new BackgroundFill(Color.web("#0A1628"), null, null)));
            return fallback;
        }

        Media media = new Media(videoUrl.toExternalForm());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setMute(true);

        MediaView view = new MediaView(mediaPlayer);
        view.setPreserveRatio(true);
        view.setSmooth(true);

        StackPane wrap = new StackPane(view);
        wrap.setStyle("-fx-background-color: #0A1628;");
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(wrap.widthProperty());
        clip.heightProperty().bind(wrap.heightProperty());
        wrap.setClip(clip);

        wrap.widthProperty().addListener((obs, o, n) -> updateVideoFit(view, wrap.getWidth(), wrap.getHeight()));
        wrap.heightProperty().addListener((obs, o, n) -> updateVideoFit(view, wrap.getWidth(), wrap.getHeight()));
        mediaPlayer.setOnReady(() -> {
            updateVideoFit(view, wrap.getWidth(), wrap.getHeight());
            mediaPlayer.play();
        });
        return wrap;
    }

    private void updateVideoFit(MediaView view, double w, double h) {
        if (mediaPlayer == null || mediaPlayer.getMedia() == null || w <= 0 || h <= 0) return;
        double vW = mediaPlayer.getMedia().getWidth();
        double vH = mediaPlayer.getMedia().getHeight();
        if (vW <= 0 || vH <= 0) return;
        double vRatio = vW / vH;
        double winRatio = w / h;
        if (winRatio < vRatio) {
            view.setFitHeight(h);
            view.setFitWidth(-1);
        } else {
            view.setFitWidth(w);
            view.setFitHeight(-1);
        }
    }

    private VBox createMenu(Stage stage) {
        VBox panel = new VBox(12);
        panel.setAlignment(Pos.BOTTOM_CENTER);
        panel.setPrefWidth(460);
        panel.setMaxWidth(460);
        panel.setPadding(new Insets(0, 24, 42, 24));
        panel.setStyle("-fx-background-color: rgba(0,0,0,0.0);");


        Button easy = bigBtn("DỄ", "#2C4C72", "#466784");
        Button hard = bigBtn("KHÓ", "#455E76", "#687C8D");
        easy.setMinWidth(162);
        hard.setMinWidth(162);
        easy.setPrefHeight(56);
        hard.setPrefHeight(56);
        easy.setOnAction(e -> startGame(stage, false));
        hard.setOnAction(e -> startGame(stage, true));

        HBox mainRow = new HBox(14, easy, hard);
        mainRow.setAlignment(Pos.CENTER);

        Button settings = smallBtn("⚙ CÀI ĐẶT");
        Button help = smallBtn("? HƯỚNG DẪN");
        settings.setPrefWidth(170);
        help.setPrefWidth(170);
        settings.setOnAction(e -> showSettingsDialog(stage));
        help.setOnAction(e -> showHelpPages(stage));

        HBox secondaryRow = new HBox(10, settings, help);
        secondaryRow.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(mainRow, secondaryRow);
        return panel;
    }

    private Button bigBtn(String text, String from, String to) {
        Button b = new Button(text);
        b.setMinHeight(48);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 15));
        String base = "-fx-background-radius: 10; -fx-text-fill: #E9F2FF; -fx-font-weight: bold; " +
                "-fx-background-color: linear-gradient(to bottom, " + from + ", " + to + ");";
        b.setStyle(base + "-fx-border-color: rgba(255,255,255,0.20); -fx-border-radius: 10; -fx-border-width: 1;");
        b.setOnMouseEntered(e -> b.setStyle(base + "-fx-border-color: rgba(255,255,255,0.40); -fx-border-radius: 10; -fx-border-width: 1;"));
        b.setOnMouseExited(e -> b.setStyle(base + "-fx-border-color: rgba(255,255,255,0.20); -fx-border-radius: 10; -fx-border-width: 1;"));
        return b;
    }

    private Button smallBtn(String text) {
        Button b = new Button(text);
        b.setMinHeight(24);
        b.setFont(Font.font("System", FontWeight.BOLD, 10));
        String base = "-fx-background-radius: 6; -fx-text-fill: #1E2A33; -fx-background-color: rgba(255,255,255,0.48);";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-radius: 6; -fx-text-fill: #1E2A33; -fx-background-color: rgba(255,255,255,0.62);"));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    private void showSettingsDialog(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Cài đặt");

        VBox box = new VBox(14);
        box.setPadding(new Insets(18));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: linear-gradient(to bottom, #0B1626, #12253B); -fx-background-radius: 18;");

        Label title = new Label("CÀI ĐẶT");
        title.setTextFill(Color.web("#F2F7FF"));
        title.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 22));

        Button soundToggle = new Button(controller != null && controller.getSound().isSoundEnabled() ? "🔊 Âm thanh: BẬT" : "🔇 Âm thanh: TẮT");
        soundToggle.setMaxWidth(Double.MAX_VALUE);
        soundToggle.setPrefHeight(42);
        soundToggle.setStyle("-fx-background-radius: 10; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: linear-gradient(to bottom, #2B4A6A, #3B5D7E);");
        soundToggle.setOnAction(e -> {
            if (controller != null) {
                controller.getSound().toggleSound();
                soundToggle.setText(controller.getSound().isSoundEnabled() ? "🔊 Âm thanh: BẬT" : "🔇 Âm thanh: TẮT");
            }
        });

        Button exitGame = new Button("❌ Thoát game");
        exitGame.setMaxWidth(Double.MAX_VALUE);
        exitGame.setPrefHeight(42);
        exitGame.setStyle("-fx-background-radius: 10; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: linear-gradient(to bottom, #7A1C1C, #A53131);");
        exitGame.setOnAction(e -> Platform.exit());

        Button close = new Button("Đóng");
        close.setMaxWidth(Double.MAX_VALUE);
        close.setPrefHeight(36);
        close.setStyle("-fx-background-radius: 10; -fx-text-fill: #1E2A33; -fx-font-weight: bold; -fx-background-color: rgba(255,255,255,0.55);");
        close.setOnAction(e -> dialog.close());

        box.getChildren().addAll(title, soundToggle, exitGame, close);
        dialog.setScene(new Scene(box, 300, 240));
        dialog.showAndWait();
    }

    private void showHelpPages(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Hướng dẫn");

        String[] pagesTitle = {
                "1. Bàn chơi",
                "2. Đặt tàu",
                "3. Lượt chơi",
                "4. Phá hủy tàu",
                "5. Điều kiện thắng"
        };
        String[] pagesText = {
                "Battleship là trò chơi dành cho 2 người chơi hoặc người chơi đấu với máy. Mỗi người có một bảng lưới thường là 8 × 8 ô, được đánh dấu theo hàng (1–8) và cột (A–H).",
                "Mỗi người đặt các tàu chiến của mình lên bảng. Tàu có thể đặt ngang hoặc dọc. Các tàu không được chồng lên nhau và đối thủ không biết vị trí tàu.",
                "Hai người lần lượt chọn tọa độ, ví dụ B5 hoặc D7, để bắn vào bảng của đối thủ. Nếu bắn trúng tàu thì là Hit, nếu bắn trượt thì là Miss.",
                "Khi tất cả các ô của một tàu bị bắn trúng thì tàu đó bị đánh chìm (Sunk).",
                "Người nào phá hủy toàn bộ tàu của đối thủ trước sẽ chiến thắng."
        };

        final int[] index = {0};

        StackPane rootPane = new StackPane();
        rootPane.setPrefSize(820, 560);
        rootPane.setStyle("-fx-background-color: linear-gradient(to bottom, #081322, #0A1B2E, #0D2740);");

        Region glow1 = new Region();
        glow1.setStyle("-fx-background-color: rgba(77,195,247,0.10); -fx-background-radius: 999;");
        glow1.setPrefSize(420, 420);
        StackPane.setAlignment(glow1, Pos.TOP_LEFT);
        StackPane.setMargin(glow1, new Insets(40, 0, 0, 50));

        Region glow2 = new Region();
        glow2.setStyle("-fx-background-color: rgba(255,215,106,0.08); -fx-background-radius: 999;");
        glow2.setPrefSize(260, 260);
        StackPane.setAlignment(glow2, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(glow2, new Insets(0, 70, 60, 0));

        VBox card = new VBox(16);
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(560);
        card.setPadding(new Insets(20, 22, 18, 22));
        card.setStyle(
                "-fx-background-color: rgba(8, 16, 28, 0.72);" +
                "-fx-background-radius: 24;" +
                "-fx-border-color: rgba(255,255,255,0.10);" +
                "-fx-border-radius: 24;" +
                "-fx-border-width: 1;"
        );

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label logo = new Label("⚓");
        logo.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 24));
        logo.setTextFill(Color.web("#FFD76A"));

        VBox headerText = new VBox(2);
        Label title = new Label();
        title.setTextFill(Color.web("#F2F7FF"));
        title.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 20));
        Label subtitle = new Label("Battle Guide");
        subtitle.setTextFill(Color.web("#8FB8D8"));
        subtitle.setFont(Font.font("System", FontWeight.BOLD, 10));
        headerText.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(logo, headerText);

        Label content = new Label();
        content.setWrapText(true);
        content.setTextFill(Color.web("#D8E6F4"));
        content.setFont(Font.font("System", 13));
        content.setMaxWidth(510);
        content.setLineSpacing(4);

        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.setStyle("-fx-background-color: rgba(255,255,255,0.12);");

        Button prev = new Button("←");
        Button next = new Button("→");
        Button close = new Button("Đóng");
        for (Button b : new Button[]{prev, next, close}) {
            b.setStyle("-fx-background-radius: 12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(255,255,255,0.12);");
            b.setPrefHeight(38);
        }
        prev.setPrefWidth(48);
        next.setPrefWidth(48);
        close.setPrefWidth(108);

        HBox nav = new HBox(10, prev, next, close);
        nav.setAlignment(Pos.CENTER_RIGHT);

        HBox dots = new HBox(8);
        dots.setAlignment(Pos.CENTER);
        Label[] dotLabels = new Label[pagesTitle.length];
        for (int i = 0; i < pagesTitle.length; i++) {
            Label d = new Label("●");
            d.setFont(Font.font("System", FontWeight.BOLD, 10));
            d.setTextFill(i == 0 ? Color.web("#FFD76A") : Color.web("#567086"));
            dotLabels[i] = d;
            dots.getChildren().add(d);
        }

        Runnable refresh = () -> {
            title.setText(pagesTitle[index[0]]);
            content.setText(pagesText[index[0]]);
            prev.setDisable(index[0] == 0);
            next.setDisable(index[0] == pagesTitle.length - 1);
            for (int i = 0; i < dotLabels.length; i++) {
                dotLabels[i].setTextFill(i == index[0] ? Color.web("#FFD76A") : Color.web("#567086"));
            }
        };

        prev.setOnAction(e -> { if (index[0] > 0) index[0]--; refresh.run(); });
        next.setOnAction(e -> { if (index[0] < pagesTitle.length - 1) index[0]++; refresh.run(); });
        close.setOnAction(e -> dialog.close());

        card.getChildren().addAll(header, divider, content, dots, nav);
        rootPane.getChildren().addAll(glow1, glow2, card);
        StackPane.setAlignment(card, Pos.CENTER);
        refresh.run();

        dialog.setScene(new Scene(rootPane));
        dialog.showAndWait();
    }

    private void updateHelpPage(Label title, Label content, String[] titles, String[] texts, int idx) {
        title.setText(titles[idx]);
        content.setText(texts[idx]);
    }

    private void startGame(Stage stage, boolean hardMode) {
        if (mediaPlayer != null) mediaPlayer.stop();
        showMenu(false);

        SoundManager sound = new SoundManager();
        controller = new GameController(sound);
        controller.startNewGame(hardMode);

        setupView = new FxSetupView(controller);
        gameView = new FxGameView(controller);
        resultView = new FxResultView(controller);

        controller.setOnShowSetup(() -> Platform.runLater(() -> root.setCenter(setupView)));
        controller.setOnShowGame(() -> Platform.runLater(() -> root.setCenter(gameView)));
        controller.setOnShowResult(won -> Platform.runLater(() -> resultView.showResult(won)));
        controller.setOnGameOver(won -> Platform.runLater(() -> root.setCenter(resultView)));

        root.setCenter(setupView);
        setupView.requestFocus();
    }
}
