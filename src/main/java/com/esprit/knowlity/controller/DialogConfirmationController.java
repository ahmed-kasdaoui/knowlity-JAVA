package com.esprit.knowlity.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.util.function.Consumer;

public class DialogConfirmationController {
    @FXML private StackPane rootPane;
    @FXML private Label titleLabel;
    @FXML private Text messageLabel;
    @FXML private ImageView iconView;
    @FXML private Button okButton;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    private Consumer<Boolean> resultHandler;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void setIcon(Image icon) {
        iconView.setImage(icon);
    }

    public void setResultHandler(Consumer<Boolean> handler) {
        this.resultHandler = handler;
    }

    @FXML
    private void initialize() {
        okButton.setOnAction(e -> handleResult(true));
        cancelButton.setOnAction(e -> handleResult(false));
    }

    private void handleResult(boolean confirmed) {
        FadeTransition fade = new FadeTransition(Duration.millis(200), rootPane);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            dialogStage.close();
            if (resultHandler != null) resultHandler.accept(confirmed);
        });
        fade.play();
    }

    public static void showDialog(String title, String message, Consumer<Boolean> resultHandler) {
        showDialog(title, message, null, resultHandler);
    }

    public static void showDialog(String title, String message, Image icon, Consumer<Boolean> resultHandler) {
        Stage ownerStage = null;
        try {
            FXMLLoader loader = new FXMLLoader(DialogConfirmationController.class.getResource("/com/esprit/knowlity/view/DialogConfirmation.fxml"));
            Parent root = loader.load();
            DialogConfirmationController controller = loader.getController();
            Stage dialogStage = new Stage();
            controller.setDialogStage(dialogStage);
            controller.setTitle(title);
            controller.setMessage(message);
            if (icon != null) controller.setIcon(icon);
            controller.setResultHandler(resultHandler);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.TRANSPARENT);

            // Blur background
            ownerStage = getOwnerWindow();
            javafx.scene.Node ownerRoot = null;
            if (ownerStage != null && ownerStage.getScene() != null) {
                ownerRoot = ownerStage.getScene().getRoot();
                if (ownerRoot != null) {
                    javafx.scene.effect.GaussianBlur blur = new javafx.scene.effect.GaussianBlur(16);
                    ownerRoot.setEffect(blur);
                }
            }

            dialogStage.setResizable(false);
            dialogStage.setTitle(title);
            root.setOpacity(0);
            FadeTransition fade = new FadeTransition(Duration.millis(200), root);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
            dialogStage.showAndWait();

            // Remove blur after dialog closes
            if (ownerRoot != null) {
                ownerRoot.setEffect(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (resultHandler != null) resultHandler.accept(false);
        }
    }

    // Utility to get the currently focused stage (owner window)
    private static Stage getOwnerWindow() {
        for (Window window : javafx.stage.Window.getWindows()) {
            if (window.isFocused() && window instanceof Stage) {
                return (Stage) window;
            }
        }
        return null;
    }
}
