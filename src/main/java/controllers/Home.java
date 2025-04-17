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
            Parent root = FXMLLoader.load(getClass().getResource("/ListeCategories.fxml"));

            // Set the scene
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Set the application icon
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/watermark.png"))); // Replace "/icon.png" with the path to your image file

            // Set the title of the application window
            stage.setTitle("Knowlity App");

            // Show the stage
            stage.show();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
