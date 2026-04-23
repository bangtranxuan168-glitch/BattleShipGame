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
            URL imgUrl = getClass().getResource("/ui/menu/bg.png");
            if (imgUrl != null) {
                ImageView iv = new ImageView(new Image(imgUrl.toExternalForm()));
                iv.setFitWidth(Constants.WINDOW_WIDTH);
                iv.setFitHeight(Constants.WINDOW_HEIGHT);
                iv.setPreserveRatio(false);
                return iv;
            }
            Region fallback = new Region();
            fallback.setPrefSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
            fallback.setBackground(new Background(new BackgroundFill(Color.web("#0A1628"), null, null)));
            return fallback;
        }

        Media media = new Media(videoUrl.toExternalForm());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setMute(true);
        
        mediaPlayer.setOnError(() -> {
            System.err.println("Video Error: " + mediaPlayer.getError());
            // Retry playing
            javafx.application.Platform.runLater(() -> {
                if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                    mediaPlayer.play();
                }
            });
        });

        MediaView view = new MediaView(mediaPlayer);
        view.setPreserveRatio(true);
        view.setSmooth(true);

        URL imgUrl = getClass().getResource("/ui/menu/bg.png");
        Node fallbackBg;
        if (imgUrl != null) {
            ImageView iv = new ImageView(new Image(imgUrl.toExternalForm()));
            iv.setFitWidth(Constants.WINDOW_WIDTH);
            iv.setFitHeight(Constants.WINDOW_HEIGHT);
            iv.setPreserveRatio(false);
            fallbackBg = iv;
        } else {
            Region bgReg = new Region();
            bgReg.setStyle("-fx-background-color: #0A1628;");
            fallbackBg = bgReg;
        }

        StackPane wrap = new StackPane(fallbackBg, view);
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
        easy.setOnMouseClicked(e -> { globalSound.playClick(); startGame(stage, false); });
        hard.setOnMouseClicked(e -> { globalSound.playClick(); startGame(stage, true); });

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
        settings.setOnMouseClicked(e -> { globalSound.playClick(); showSettingsDialog(stage); });
        help.setOnMouseClicked(e -> { globalSound.playClick(); showHelpPages(stage); });

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
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        // ── Load ảnh ──────────────────────────────────────────────────
        Image imgSoundOn  = new Image(getClass().getResourceAsStream("/ui/menu/sound_onl.png"));
        Image imgSoundOff = new Image(getClass().getResourceAsStream("/ui/menu/sound_off.png"));
        Image imgExit     = new Image(getClass().getResourceAsStream("/ui/menu/btn_exit.png"));
        Image imgClose    = new Image(getClass().getResourceAsStream("/ui/menu/btn_close.png"));

        // ── Nút Âm thanh (swap ảnh khi click) ────────────────────────
        boolean soundOn = globalSound.isSoundEnabled();
        ImageView soundIV = new ImageView(soundOn ? imgSoundOn : imgSoundOff);
        soundIV.setFitWidth(420); soundIV.setFitHeight(120);
        soundIV.setPreserveRatio(true); soundIV.setSmooth(true);
        soundIV.setPickOnBounds(false);

        StackPane soundBtn = new StackPane(soundIV);
        soundBtn.setPickOnBounds(false);
        soundBtn.setCursor(javafx.scene.Cursor.HAND);
        addHoverScale(soundBtn, 1.04);
        addPressScale(soundBtn, 1.08);
        soundBtn.setOnMouseClicked(e -> {
            globalSound.playClick();
            globalSound.toggleSound();
            soundIV.setImage(globalSound.isSoundEnabled() ? imgSoundOn : imgSoundOff);
        });

        // ── Nút Thoát ─────────────────────────────────────────────────
        ImageView exitIV = new ImageView(imgExit);
        exitIV.setFitWidth(200); exitIV.setFitHeight(60);
        exitIV.setPreserveRatio(true); exitIV.setSmooth(true);
        exitIV.setPickOnBounds(false);

        StackPane exitBtn = new StackPane(exitIV);
        exitBtn.setPickOnBounds(false);
        exitBtn.setCursor(javafx.scene.Cursor.HAND);
        addHoverScale(exitBtn, 1.04);
        addPressScale(exitBtn, 1.08);
        exitBtn.setOnMouseClicked(e -> { globalSound.playClick(); Platform.exit(); });

        // ── Nút Đóng ──────────────────────────────────────────────────
        ImageView closeIV = new ImageView(imgClose);
        closeIV.setFitWidth(200); closeIV.setFitHeight(56);
        closeIV.setPreserveRatio(true); closeIV.setSmooth(true);
        closeIV.setPickOnBounds(false);

        StackPane closeBtn = new StackPane(closeIV);
        closeBtn.setPickOnBounds(false);
        closeBtn.setCursor(javafx.scene.Cursor.HAND);
        addHoverScale(closeBtn, 1.04);
        addPressScale(closeBtn, 1.08);
        closeBtn.setOnMouseClicked(e -> { globalSound.playClick(); dialog.close(); });

        // ── Layout ────────────────────────────────────────────────────
        Label title = new Label("CÀI ĐẶT");
        title.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.5), 3, 0.0, 0, 2);");

        VBox box = new VBox(10, title, soundBtn, exitBtn, closeBtn);
        box.setPadding(new Insets(25, 20, 25, 20));
        box.setAlignment(Pos.CENTER);
        box.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #0B1626, #12253B);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: #4BA3E3; -fx-border-width: 3; -fx-border-radius: 12;"
        );

        StackPane transparentRoot = new StackPane(box);
        transparentRoot.setStyle("-fx-background-color: transparent;");
        transparentRoot.setPadding(new Insets(15));

        Scene scene = new Scene(transparentRoot, 420, 360);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showHelpPages(Stage owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        String[] pagesTitle = {
                "MỤC TIÊU TRÒ CHƠI",
                "SẮP XẾP TÀU",
                "CÁCH CHƠI"
        };
        String[] pagesText = {
                "Trở thành người đầu tiên đánh chìm cả 5 tàu chiến của đối thủ.\n\n⚓ Hạm đội của bạn bao gồm:\n• Tàu Sân Bay (5 ô)\n• Tàu Thiết Giáp (4 ô)\n• Tàu Tuần Dương (3 ô)\n• Tàu Ngầm (3 ô)\n• Tàu Khu Trục (2 ô)",
                "1. Đặt 5 tàu chiến của bạn lên lưới đại dương.\n\n2. Kéo thả chuột để di chuyển vị trí tàu.\n\n3. Nhấn phím R hoặc nhấp chuột phải để xoay ngang/dọc.\n\n4. Các tàu không được xếp chồng lên nhau.\n\n5. Bạn có thể nhấn 'Sắp xếp ngẫu nhiên' để máy tự động đặt tàu.",
                "1. Bấm vào một ô trên lưới của đối phương để bắn đạn.\n\n2. Lượt chơi luân phiên giữa bạn và địch.\n\n3. 💥 Nếu trúng tàu: ô hiện dấu X đỏ.\n\n4. 💦 Nếu trượt: ô hiện dấu chấm xám.\n\n5. Khi tất cả các ô của một tàu bị bắn, tàu đó bị chìm.\n\n6. Hãy đánh chìm cả 5 tàu của địch để giành chiến thắng!"
        };

        final int[] index = {0};

        Label title = new Label();
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 22));
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        
        Label content = new Label();
        content.setWrapText(true);
        content.setTextFill(Color.WHITE);
        content.setFont(Font.font("System", FontWeight.BOLD, 15));
        content.setLineSpacing(6);
        content.setAlignment(Pos.TOP_LEFT);
        content.setMaxWidth(Double.MAX_VALUE);
        
        VBox textBox = new VBox(15, title, content);
        textBox.setPadding(new Insets(25, 20, 20, 20));
        textBox.setStyle("-fx-background-color: #0A3C66; -fx-background-radius: 8; -fx-border-color: #A3D5FF; -fx-border-width: 4; -fx-border-radius: 6;");
        textBox.setPrefHeight(360);
        textBox.setMinHeight(360);

        HBox dotsBox = new HBox(8);
        dotsBox.setAlignment(Pos.CENTER);
        Runnable updateDots = () -> {
            dotsBox.getChildren().clear();
            for (int i = 0; i < pagesTitle.length; i++) {
                javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(6);
                dot.setFill(i == index[0] ? Color.WHITE : Color.TRANSPARENT);
                dot.setStroke(Color.WHITE);
                dot.setStrokeWidth(1.5);
                dotsBox.getChildren().add(dot);
            }
        };

        Button prev = new Button("◀");
        Button next = new Button("▶");
        for (Button b : new Button[]{prev, next}) {
            b.setFont(Font.font("System", FontWeight.BOLD, 18));
            b.setTextFill(Color.web("#0A3C66"));
            String btnStyle = "-fx-background-color: linear-gradient(to bottom, #FFFFFF, #B6D8F2); -fx-background-radius: 6; -fx-border-color: #0A3C66; -fx-border-radius: 4; -fx-border-width: 2;";
            String hoverStyle = "-fx-background-color: linear-gradient(to bottom, #FFFFFF, #E6F3FF); -fx-background-radius: 6; -fx-border-color: #0A3C66; -fx-border-radius: 4; -fx-border-width: 2;";
            b.setStyle(btnStyle);
            b.setPrefSize(45, 35);
            b.setCursor(javafx.scene.Cursor.HAND);
            b.setOnMouseEntered(e -> b.setStyle(hoverStyle));
            b.setOnMouseExited(e -> b.setStyle(btnStyle));
        }

        StackPane centerNav = new StackPane(dotsBox);
        HBox.setHgrow(centerNav, Priority.ALWAYS);
        HBox navBox = new HBox(prev, centerNav, next);
        navBox.setAlignment(Pos.CENTER);
        navBox.setPadding(new Insets(0, 5, 0, 5));

        Runnable updatePage = () -> {
            title.setText(pagesTitle[index[0]]);
            content.setText(pagesText[index[0]]);
            prev.setVisible(index[0] > 0);
            next.setVisible(index[0] < pagesTitle.length - 1);
            updateDots.run();
        };

        prev.setOnAction(e -> {
            if (globalSound != null) globalSound.playClick();
            if (index[0] > 0) index[0]--;
            updatePage.run();
        });
        next.setOnAction(e -> {
            if (globalSound != null) globalSound.playClick();
            if (index[0] < pagesText.length - 1) index[0]++;
            updatePage.run();
        });

        Label helpTitle = new Label("HƯỚNG DẪN");
        helpTitle.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 32));
        helpTitle.setTextFill(Color.WHITE);
        helpTitle.setStyle("-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.4), 4, 0.0, 0, 2);");

        Button close = new Button("✖");
        close.setFont(Font.font("System", FontWeight.BOLD, 16));
        close.setTextFill(Color.WHITE);
        close.setStyle("-fx-background-color: #E74C3C; -fx-background-radius: 8; -fx-border-color: white; -fx-border-radius: 6; -fx-border-width: 2;");
        close.setPrefSize(36, 36);
        close.setCursor(javafx.scene.Cursor.HAND);
        close.setOnAction(e -> { if (globalSound != null) globalSound.playClick(); dialog.close(); });
        
        BorderPane topPane = new BorderPane();
        topPane.setCenter(helpTitle);
        topPane.setRight(close);
        BorderPane.setAlignment(close, Pos.TOP_RIGHT);
        BorderPane.setMargin(close, new Insets(-10, -10, 0, 0));

        VBox rootBox = new VBox(20, topPane, textBox, navBox);
        rootBox.setPadding(new Insets(20));
        rootBox.setStyle("-fx-background-color: linear-gradient(to bottom, #59B5F4, #2475B0); -fx-background-radius: 12; -fx-border-color: white; -fx-border-radius: 10; -fx-border-width: 4;");
        
        StackPane transparentRoot = new StackPane(rootBox);
        transparentRoot.setStyle("-fx-background-color: transparent;");
        transparentRoot.setPadding(new Insets(15));

        updatePage.run();

        Scene scene = new Scene(transparentRoot, 420, 560);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void startGame(Stage stage, boolean hardMode) {
        showMenu(false);

        // Sử dụng globalSound đã khởi tạo thay vì tạo mới
        controller = new GameController(globalSound);
        controller.startNewGame(hardMode);

        setupView = new FxSetupView(controller);
        gameView = new FxGameView(controller);
        resultView = new FxResultView(controller);

        controller.setOnShowMenu(() -> Platform.runLater(() -> {
            root.setCenter(null);
            showMenu(true);
        }));
        controller.setOnShowSetup(() -> Platform.runLater(() -> root.setCenter(setupView)));
        controller.setOnShowGame(() -> Platform.runLater(() -> root.setCenter(gameView)));
        controller.setOnShowResult(won -> Platform.runLater(() -> resultView.showResult(won)));
        controller.setOnGameOver(won -> Platform.runLater(() -> root.setCenter(resultView)));

        root.setCenter(setupView);
        setupView.requestFocus();
    }
}
