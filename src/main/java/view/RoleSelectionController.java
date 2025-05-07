package view;

import tn.knowlity.entity.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Modality;
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
            // Charger la fenêtre de login
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Pane loginRoot = loginLoader.load();
            
            // Configurer le contrôleur de login
            LoginController loginController = loginLoader.getController();
            loginController.setSelectedRole(isAdmin ? "admin" : "user");
            
            // Créer une nouvelle fenêtre modale pour le login
            Stage loginStage = new Stage();
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.setTitle("Connexion");
            loginStage.setScene(new Scene(loginRoot));
            
            // Configurer le callback de login
            loginController.setStage(loginStage);
            loginController.setCallback(new LoginController.LoginCallback() {
                @Override
                public void onLoginSuccess(Entities.User user) {

                }

                @Override
                public void onLoginSuccess(User user) {
                    try {
                        // Charger la liste des blogs après connexion réussie
                        FXMLLoader blogLoader = new FXMLLoader(getClass().getResource("/fxml/blog_list.fxml"));
                        Pane blogRoot = blogLoader.load();
                        
                        BlogListController blogController = blogLoader.getController();
                        blogController.setAdminMode(isAdmin);
                        blogController.setCurrentUser(user);
                        
                        Stage mainStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                        Scene scene = new Scene(blogRoot);
                        scene.getStylesheets().add(getClass().getResource("/styles/blog.css").toExternalForm());
                        mainStage.setScene(scene);
                        mainStage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showError("Erreur", "Impossible de charger la liste des blogs: " + e.getMessage());
                    }
                }
            });
            
            // Afficher la fenêtre de login
            loginStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la fenêtre de connexion: " + e.getMessage());
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
