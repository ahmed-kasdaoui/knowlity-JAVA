package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

public class Home extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            // Load the FXML file
            Parent root = FXMLLoader.load(getClass().getResource("/ListeCours.fxml"));

            // Set the scene
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Set the application icon
            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/watermark.png")));
            } catch (Exception e) {
                System.err.println("Failed to load application icon: " + e.getMessage());
            }

            // Configure the stage
            stage.setTitle("Knowlity App");
            stage.setMaximized(true); // Ouvre la fenêtre en mode maximisé

            // Show the stage
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
