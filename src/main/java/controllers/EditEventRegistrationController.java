package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import tn.esprit.models.EventRegistration;

import java.io.IOException;

public class EditEventRegistrationController {

    @FXML
    private UserEventRegistrationFormController registrationFormController;
    @FXML
    private Label titleLabel;
    @FXML
    private Button backButton;

    @FXML
    public void initialize() {
        backButton.setOnAction(e -> navigateBack());
    }

    public void setRegistration(EventRegistration registration) {
        registrationFormController.setRegistration(registration);
        titleLabel.setText("Edit Registration for: " + (registration.getEvent() != null ? registration.getEvent().getTitle() : "N/A"));
    }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventRegistrationList.fxml"));
            backButton.getScene().setRoot(loader.load());
        } catch (IOException e) {
            // Handle error (e.g., show alert)
        }
    }
}