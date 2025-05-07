package com.esprit.knowlity;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFx extends Application {
    private Stage mainStage;
    private com.esprit.knowlity.controller.student.EvaluationFormController evalFormController;

    public void start(Stage stage) throws IOException {
        System.out.println("App started with args: " + getParameters().getRaw());
        this.mainStage = stage;
        // Check for evaluationId argument
        String evalIdStr = null;
        for (String arg : getParameters().getRaw()) {
            if (arg.startsWith("--evaluationId=")) {
                evalIdStr = arg.substring("--evaluationId=".length());
            } else if (arg.startsWith("knowlity://evaluate?evaluationId=")) {
                evalIdStr = arg.substring("knowlity://evaluate?evaluationId=".length());
            } else if (arg.startsWith("knowlity://evaluate/?evaluationId=")) {
                evalIdStr = arg.substring("knowlity://evaluate/?evaluationId=".length());
            } else if (arg.matches("\\d+")) {
                evalIdStr = arg;
            }
        }
        // Single instance logic
        boolean isMain = com.esprit.knowlity.SingleInstanceManager.start(evalIdStr, this::handleDeeplink);
        if (!isMain) {
            // This is a secondary instance, exit
            System.exit(0);
        }
        // If launched with deeplink, handle it
        if (evalIdStr != null) {
            handleDeeplink(evalIdStr);
            return;
        }
        // Default: show home screen
        Platform.runLater(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/home.fxml"));
            try {
                Parent root = loader.load();
                Scene scene = new Scene(root);
                if (mainStage != null) {
                    mainStage.setScene(scene);
                    mainStage.toFront();
                }
                mainStage.setTitle("Knowlity");
                mainStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Handles a deeplink argument by opening the evaluation form for the given evaluationId.
     * If already open, brings the stage to front.
     */
    private void handleDeeplink(String evalIdStr) {
        try {
            int evaluationId = Integer.parseInt(evalIdStr);
            com.esprit.knowlity.Service.EvaluationService evaluationService = new com.esprit.knowlity.Service.EvaluationService();
            com.esprit.knowlity.Model.Evaluation evaluation = evaluationService.getEvaluationById(evaluationId);
            if (evaluation != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("view/student/evaluation_form.fxml"));
                Parent root = loader.load();
                evalFormController = loader.getController();
                evalFormController.setEvaluation(evaluation);
                Scene scene = new Scene(root);
                mainStage.setScene(scene);
                mainStage.setTitle("Passer l'évaluation");
                mainStage.show();
                mainStage.toFront();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}