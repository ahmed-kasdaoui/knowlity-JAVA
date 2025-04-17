package view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;

public class RoleSelectionController {

    @FXML
    private void handleAdminSelection(MouseEvent event) {
        loadBlogList(event, true);
    }

    @FXML
    private void handleUserSelection(MouseEvent event) {
        loadBlogList(event, false);
    }

    private void loadBlogList(MouseEvent event, boolean isAdmin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/blog_list.fxml"));
            Pane root = loader.load();
            
            // Get the controller and set admin mode
            BlogListController controller = loader.getController();
            controller.setAdminMode(isAdmin);
            
            // Get the current stage and set the new scene
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/blog.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la liste des blogs: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
