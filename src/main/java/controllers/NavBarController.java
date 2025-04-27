package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class NavBarController {

    private static final Logger LOGGER = Logger.getLogger(NavBarController.class.getName());

    @FXML
    private Button ajouterButton, profilButton, deconnexionButton;

    @FXML
    private MenuItem mesCoursItem, tousCoursItem;

    @FXML
    private void handleMesCours(ActionEvent event) {
        loadPage("ListeCours.fxml"); // Teacher's course list
    }

    @FXML
    private void handleTousCours(ActionEvent event) {
        loadPage("ListeCoursEtudiant.fxml"); // All courses, student view
    }

    @FXML
    private void handleAjouterAction(ActionEvent event) {
        loadPage("AjoutCours.fxml");
    }

    @FXML
    private void handleProfilAction(ActionEvent event) {
        loadPage("Profile.fxml");
    }

    @FXML
    private void handleDeconnexionAction(ActionEvent event) {
        loadPage("Login.fxml");
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ajouterButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load page: " + fxmlFile, e);
        }
    }
}