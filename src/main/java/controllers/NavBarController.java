package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class NavBarController {

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

    public void eventListId(ActionEvent actionEvent) {loadPage("fxml/EventListing.fxml");}

    public void AllCourses(ActionEvent actionEvent) {loadPage("ListeCoursEtudiant.fxml");}

    public void MyCourses(ActionEvent actionEvent) {loadPage("ListeCoursEtudiantInscrits.fxml");}

    public void FavoriteCourses(ActionEvent actionEvent) {loadPage("ListeCoursEtudiantFavoris.fxml");}

    public void profile(ActionEvent actionEvent) {loadPage("User/editFront.fxml");}

    public void logout(ActionEvent actionEvent) {loadPage("User/loginPage.fxml");}

    public void home(ActionEvent actionEvent) {loadPage("ListeCoursEtudiant.fxml");
    }

    public void blog(ActionEvent actionEvent) {loadPage("fxml/blog_list.fxml");
    }
}