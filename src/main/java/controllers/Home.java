package controllers;

import com.esprit.knowlity.Service.EvaluationService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

public class Home extends Application {
    private Stage mainStage;
    private com.esprit.knowlity.controller.student.EvaluationFormController evalFormController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        System.out.println("App started with args: " + getParameters().getRaw());

        // Check for evaluationId in arguments (deeplink handling)
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

        // If deeplink is present, handle it
        if (evalIdStr != null) {
            handleDeeplink(evalIdStr);
            return;
        }

        // Default: Load login page
        loadLoginPage();
    }

    /**
     * Loads the default login page (loginPage.fxml).
     */
    private void loadLoginPage() {
        Platform.runLater(() -> {
            try {
                // Load the FXML file
                Parent root = FXMLLoader.load(getClass().getResource("/user/loginPage.fxml"));

                // Set the scene
                Scene scene = new Scene(root);
                mainStage.setScene(scene);

                // Set the application icon
                try {
                    mainStage.getIcons().add(new Image(getClass().getResourceAsStream("/watermark.png")));
                } catch (Exception e) {
                    System.err.println("Failed to load application icon: " + e.getMessage());
                }

                // Configure the stage
                mainStage.setTitle("Knowlity App");
                mainStage.setMaximized(true); // Open window in maximized mode

                // Show the stage
                mainStage.show();
            } catch (IOException e) {
                System.err.println("Error loading login page: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Handles a deeplink by loading the evaluation form for the given evaluationId.
     */
    private void handleDeeplink(String evalIdStr) {
        Platform.runLater(() -> {
            try {
                int evaluationId = Integer.parseInt(evalIdStr);
                // Assuming EvaluationService and Evaluation exist in your project
                com.esprit.knowlity.Service.EvaluationService evaluationService = new EvaluationService();
                com.esprit.knowlity.Model.Evaluation evaluation = evaluationService.getEvaluationById(evaluationId);

                if (evaluation != null) {
                    // Load evaluation form FXML (adjust path as per your project structure)
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/evaluation_form.fxml"));
                    Parent root = loader.load();

                    // Store the controller
                    evalFormController = loader.getController();
                    evalFormController.setEvaluation(evaluation);

                    // Set the scene
                    Scene scene = new Scene(root);
                    mainStage.setScene(scene);

                    // Set the application icon
                    try {
                        mainStage.getIcons().add(new Image(getClass().getResourceAsStream("/watermark.png")));
                    } catch (Exception e) {
                        System.err.println("Failed to load application icon: " + e.getMessage());
                    }

                    // Configure the stage
                    mainStage.setTitle("Passer l'évaluation");
                    mainStage.setMaximized(true);

                    // Show the stage
                    mainStage.show();
                    mainStage.toFront();
                } else {
                    System.err.println("Evaluation not found for ID: " + evaluationId);
                    // Fallback to login page if evaluation is not found
                    loadLoginPage();
                }
            } catch (Exception e) {
                System.err.println("Error handling deeplink: " + e.getMessage());
                e.printStackTrace();
                // Fallback to login page on error
                loadLoginPage();
            }
        });
    }
}