package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class navbarA {

    private static final Logger LOGGER = Logger.getLogger(NavBarController.class.getName());
    public ImageView test;
    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/"+fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) test.getScene().getWindow();
            stage.setTitle("Knowlity:"+fxmlFile);
            Scene currentScene = stage.getScene();
            Scene newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());

            stage.setScene(newScene);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load page: " + fxmlFile, e);
        }
    }

    public void logout(ActionEvent actionEvent) { loadPage("User/loginPage.fxml");
    }

    public void user(ActionEvent actionEvent) { loadPage("User/backUser.fxml");
    }

    public void category(ActionEvent actionEvent) {loadPage("ListeCategories.fxml");
    }

    public void matiere(ActionEvent actionEvent) {loadPage("ListeMatiere.fxml");
    }

    public void blog(ActionEvent actionEvent) {loadPage("fxml/admin_blog_list.fxml");
    }

    public void event(ActionEvent actionEvent) {loadPage("fxml/EventListA.fxml");
    }
}
