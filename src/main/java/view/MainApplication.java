package view;

import Utils.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApplication extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database
            DatabaseInitializer.initializeDatabase();
            
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/role_selection.fxml"));
            Pane root = loader.load();
            
            // Create the scene and add CSS
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/blog.css").toExternalForm());
            
            // Configure and show the stage
            primaryStage.setTitle("Blog Manager");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
