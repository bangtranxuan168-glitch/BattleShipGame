package com.battleship;

import com.battleship.controller.GameController;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private SoundManager globalSound = new SoundManager();
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
        StackPane.setMargin(menu, new Insets(0, 0, 10, 0));

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
        VBox panel = new VBox(6);
        panel.setAlignment(Pos.BOTTOM_CENTER);
        panel.setPrefWidth(700);
        panel.setMaxWidth(700);
        panel.setPadding(Insets.EMPTY);
        panel.setStyle("-fx-background-color: rgba(0,0,0,0.0);");

        StackPane easy = imageButton("/ui/menu/btn_easy.png", 300, 105);
        StackPane hard = imageButton("/ui/menu/btn_hard.png", 300, 105);
        easy.setOnMouseClicked(e -> startGame(stage, false));
        hard.setOnMouseClicked(e -> startGame(stage, true));

        addHoverScale(easy, 1.06);
        addHoverScale(hard, 1.06);
        addPressScale(easy, 1.10);
        addPressScale(hard, 1.10);

        HBox mainRow = new HBox(8, easy, hard);
        mainRow.setAlignment(Pos.CENTER);

        StackPane settings = imageButton("/ui/menu/btn_caidat.png", 180, 62);
        StackPane help     = imageButton("/ui/menu/btn_huongdan.png", 180, 62);
        addHoverScale(settings, 1.06);
        addHoverScale(help, 1.06);
        addPressScale(settings, 1.08);
        addPressScale(help, 1.08);
        settings.setOnMouseClicked(e -> showSettingsDialog(stage));
        help.setOnMouseClicked(e -> showHelpPages(stage));

        HBox secondaryRow = new HBox(8, settings, help);
        secondaryRow.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(mainRow, secondaryRow);
        return panel;
    }


    /**
     * Tạo nút dùng PNG image.
     * Dùng StackPane + setPickOnBounds(false) để chỉ nhận sự kiện trên pixel có thực của PNG,
     * không kích hoạt trên vùng transparent (giải quyết cả padding và hover sai).
     */
    private StackPane imageButton(String resourcePath, double w, double h) {
        Image image = new Image(getClass().getResourceAsStream(resourcePath));
        ImageView iv = new ImageView(image);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setCache(true);
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        // Chỉ hit-test trên pixel thực của image (không có padding, không có transparent area)
        iv.setPickOnBounds(false);

        StackPane pane = new StackPane(iv);
        pane.setPickOnBounds(false);  // kết hợp với ImageView → chỉ detect trên pixel PNG
        pane.setPrefSize(w, h);
        pane.setMaxSize(w, h);
        pane.setMinSize(w, h);
        pane.setStyle("-fx-background-color: transparent;");
        pane.setCursor(javafx.scene.Cursor.HAND);
        return pane;
    }

    private void addHoverScale(Node node, double scale) {
        node.setOnMouseEntered(e -> {
            node.setScaleX(scale);
            node.setScaleY(scale);
        });
        node.setOnMouseExited(e -> {
            node.setScaleX(1.0);
            node.setScaleY(1.0);
        });
    }

    private void addPressScale(Node node, double scale) {
        node.setOnMousePressed(e -> {
            node.setScaleX(scale);
            node.setScaleY(scale);
        });
        node.setOnMouseReleased(e -> {
            node.setScaleX(1.0);
            node.setScaleY(1.0);
        });
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
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Cài đặt");
        dialog.setResizable(false);

        // ── Load ảnh ──────────────────────────────────────────────────
        Image imgSoundOn  = new Image(getClass().getResourceAsStream("/ui/menu/sound_onl.png"));
        Image imgSoundOff = new Image(getClass().getResourceAsStream("/ui/menu/sound_off.png"));
        Image imgExit     = new Image(getClass().getResourceAsStream("/ui/menu/btn_exit.png"));
        Image imgClose    = new Image(getClass().getResourceAsStream("/ui/menu/btn_close.png"));

        // ── Nút Âm thanh (swap ảnh khi click) ────────────────────────
        boolean soundOn = controller == null || controller.getSound().isSoundEnabled();
        ImageView soundIV = new ImageView(soundOn ? imgSoundOn : imgSoundOff);
        soundIV.setFitWidth(320); soundIV.setFitHeight(100);
        soundIV.setPreserveRatio(true); soundIV.setSmooth(true);
        soundIV.setPickOnBounds(false);

        StackPane soundBtn = new StackPane(soundIV);
        soundBtn.setPickOnBounds(false);
        soundBtn.setCursor(javafx.scene.Cursor.HAND);
        addHoverScale(soundBtn, 1.04);
        addPressScale(soundBtn, 1.08);
        soundBtn.setOnMouseClicked(e -> {
            if (controller != null) controller.getSound().toggleSound();
            boolean on = controller == null || controller.getSound().isSoundEnabled();
            soundIV.setImage(on ? imgSoundOn : imgSoundOff);
        });

        // ── Nút Thoát ─────────────────────────────────────────────────
        ImageView exitIV = new ImageView(imgExit);
        exitIV.setFitWidth(280); exitIV.setFitHeight(80);
        exitIV.setPreserveRatio(true); exitIV.setSmooth(true);
        exitIV.setPickOnBounds(false);

        StackPane exitBtn = new StackPane(exitIV);
        exitBtn.setPickOnBounds(false);
        exitBtn.setCursor(javafx.scene.Cursor.HAND);
        addHoverScale(exitBtn, 1.04);
        addPressScale(exitBtn, 1.08);
        exitBtn.setOnMouseClicked(e -> Platform.exit());

        // ── Nút Đóng ──────────────────────────────────────────────────
        ImageView closeIV = new ImageView(imgClose);
        closeIV.setFitWidth(280); closeIV.setFitHeight(74);
        closeIV.setPreserveRatio(true); closeIV.setSmooth(true);
        closeIV.setPickOnBounds(false);

        StackPane closeBtn = new StackPane(closeIV);
        closeBtn.setPickOnBounds(false);
        closeBtn.setCursor(javafx.scene.Cursor.HAND);
        addHoverScale(closeBtn, 1.04);
        addPressScale(closeBtn, 1.08);
        closeBtn.setOnMouseClicked(e -> dialog.close());

        // ── Layout ────────────────────────────────────────────────────
        Label title = new Label("CÀI ĐẶT");
        title.setTextFill(Color.web("#F2F7FF"));
        title.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 18));

        VBox box = new VBox(10, title, soundBtn, exitBtn, closeBtn);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setAlignment(Pos.CENTER);
        box.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #0B1626, #12253B);" +
            "-fx-background-radius: 14;"
        );

        dialog.setScene(new Scene(box, 320, 270));
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

        Label title = new Label();
        title.setTextFill(Color.web("#F2F7FF"));
        title.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 20));

        Label content = new Label();
        content.setWrapText(true);
        content.setTextFill(Color.web("#D8E6F4"));
        content.setFont(Font.font("System", 13));
        content.setMaxWidth(420);

        VBox page = new VBox(14, title, content);
        page.setPadding(new Insets(18));
        page.setAlignment(Pos.TOP_LEFT);
        page.setStyle("-fx-background-color: linear-gradient(to bottom, #0B1626, #12253B); -fx-background-radius: 18;");

        Button prev = new Button("← Trước");
        Button next = new Button("Sau →");
        Button close = new Button("Đóng");
        for (Button b : new Button[]{prev, next, close}) {
            b.setPrefHeight(34);
            b.setStyle("-fx-background-radius: 10; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: rgba(255,255,255,0.14);");
        }
        close.setOnAction(e -> dialog.close());
        prev.setOnAction(e -> {
            if (index[0] > 0) index[0]--;
            updateHelpPage(title, content, pagesTitle, pagesText, index[0]);
        });
        next.setOnAction(e -> {
            if (index[0] < pagesText.length - 1) index[0]++;
            updateHelpPage(title, content, pagesTitle, pagesText, index[0]);
        });

        HBox nav = new HBox(10, prev, next, close);
        nav.setAlignment(Pos.CENTER_RIGHT);
        prev.setDisable(true);

        VBox box = new VBox(14, page, nav);
        box.setPadding(new Insets(14));
        updateHelpPage(title, content, pagesTitle, pagesText, 0);

        dialog.setScene(new Scene(box, 480, 310));
        dialog.showAndWait();
    }

    private void updateHelpPage(Label title, Label content, String[] titles, String[] texts, int idx) {
        title.setText(titles[idx]);
        content.setText(texts[idx]);
    }

    private void startGame(Stage stage, boolean hardMode) {
        if (mediaPlayer != null) mediaPlayer.stop();
        showMenu(false);

        // Sử dụng globalSound đã khởi tạo thay vì tạo mới
        controller = new GameController(globalSound);
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
