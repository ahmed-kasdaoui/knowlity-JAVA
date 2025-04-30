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

public class CustomDialogController {
    // Preload icons for reuse
    private static final Image SUCCESS_ICON = new Image(CustomDialogController.class.getResourceAsStream("/images/success_icon.png"));
    private static final Image ERROR_ICON = new Image(CustomDialogController.class.getResourceAsStream("/images/error_icon.png"));
    private static final Image INFO_ICON = new Image(CustomDialogController.class.getResourceAsStream("/images/info_icon.png"));

    public enum DialogType {
        SUCCESS, ERROR, INFO
    }
    @FXML private StackPane rootPane;
    @FXML private Label titleLabel;
    @FXML private Text messageLabel;
    @FXML private ImageView iconView;
    @FXML private Button okButton;

    private Stage dialogStage;

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

    @FXML
    private void initialize() {
        okButton.setOnAction(e -> closeDialog());
    }

    private void closeDialog() {
        FadeTransition fade = new FadeTransition(Duration.millis(250), rootPane);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> dialogStage.close());
        fade.play();
    }

    public static void showDialog(String title, String message, Image icon) {
        Stage ownerStage = null;
        javafx.scene.Node ownerRoot = null;
        try {
            FXMLLoader loader = new FXMLLoader(CustomDialogController.class.getResource("/com/esprit/knowlity/view/CustomDialog.fxml"));
            Parent root = loader.load();
            CustomDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            controller.setDialogStage(dialogStage);
            controller.setTitle(title);
            controller.setMessage(message);
            controller.setIcon(icon);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.TRANSPARENT);

            // Blur background
            ownerStage = getOwnerWindow();
            if (ownerStage != null && ownerStage.getScene() != null) {
                ownerRoot = ownerStage.getScene().getRoot();
                if (ownerRoot != null) {
                    javafx.scene.effect.GaussianBlur blur = new javafx.scene.effect.GaussianBlur(16);
                    ownerRoot.setEffect(blur);
                }
            }

            dialogStage.setResizable(false);
            dialogStage.setTitle(title);
            // Fade in
            root.setOpacity(0);
            FadeTransition fade = new FadeTransition(Duration.millis(250), root);
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

    public static void showDialog(String title, String message, DialogType type) {
        Image icon;
        switch (type) {
            case SUCCESS:
                icon = SUCCESS_ICON;
                break;
            case ERROR:
                icon = ERROR_ICON;
                break;
            case INFO:
            default:
                icon = INFO_ICON;
                break;
        }
        showDialog(title, message, icon);
    }
}
