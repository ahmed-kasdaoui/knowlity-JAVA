package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.models.Events;

public class NewEventRegistrationController {

    public VBox registrationForm;
    @FXML
    private NewEventRegistrationFormController registrationFormController;
    @FXML
    private Label titleLabel;

    public void setEvent(Events event) {
        registrationFormController.setEvent(event);
        titleLabel.setText("New Registration for: " + event.getTitle());
    }
}