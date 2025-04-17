package controllers;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import tn.esprit.models.EventRegistration;
import tn.esprit.services.ServiceEventRegistration;

public class UserEventRegistrationFormController {

    @FXML
    private TextField nameField;
    @FXML
    private Label nameLabel;
    @FXML
    private Label nameErrorLabel;
    @FXML
    private CheckBox disabledParkingCheckBox;
    @FXML
    private Label disabledParkingLabel;
    @FXML
    private Label disabledParkingErrorLabel;
    @FXML
    private TextField placesReservedField;
    @FXML
    private Label placesReservedLabel;
    @FXML
    private Label placesReservedErrorLabel;
    @FXML
    private TextField comingFromField;
    @FXML
    private Label comingFromLabel;
    @FXML
    private Label comingFromErrorLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button resetButton;

    private final ServiceEventRegistration serviceEventRegistration;
    private EventRegistration currentRegistration;

    public UserEventRegistrationFormController() {
        this.serviceEventRegistration = new ServiceEventRegistration();
    }

    @FXML
    public void initialize() {
        // Bind floating labels
        bindFloatingLabel(nameField, nameLabel);
        bindFloatingLabel(placesReservedField, placesReservedLabel);
        bindFloatingLabel(comingFromField, comingFromLabel);
        bindFloatingLabel(disabledParkingCheckBox, disabledParkingLabel);

        // Real-time validation
        nameField.textProperty().addListener((obs, old, newValue) -> validateName());
        placesReservedField.textProperty().addListener((obs, old, newValue) -> validatePlacesReserved());
        comingFromField.textProperty().addListener((obs, old, newValue) -> validateComingFrom());

        // Button actions
        saveButton.setOnAction(e -> saveRegistrationAction());
        resetButton.setOnAction(e -> resetForm());
    }

    private void bindFloatingLabel(TextField field, Label label) {
        label.styleProperty().bind(
                Bindings.when(field.textProperty().isNotEmpty().or(field.focusedProperty()))
                        .then("-fx-style-class: floating;")
                        .otherwise("")
        );
    }

    private void bindFloatingLabel(CheckBox checkBox, Label label) {
        label.styleProperty().bind(
                Bindings.when(checkBox.selectedProperty().or(checkBox.focusedProperty()))
                        .then("-fx-style-class: floating;")
                        .otherwise("")
        );
    }

    private boolean validateName() {
        String text = nameField.getText();
        if (text.isEmpty()) {
            nameErrorLabel.setText("Name is required");
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
            return false;
        }
        nameErrorLabel.setVisible(false);
        nameErrorLabel.setManaged(false);
        return true;
    }

    private boolean validatePlacesReserved() {
        String text = placesReservedField.getText();
        if (text.isEmpty()) {
            placesReservedErrorLabel.setText("Number of places is required");
            placesReservedErrorLabel.setVisible(true);
            placesReservedErrorLabel.setManaged(true);
            return false;
        }
        try {
            int value = Integer.parseInt(text);
            if (value < 1 || value > 5) {
                placesReservedErrorLabel.setText("Must be between 1 and 5");
                placesReservedErrorLabel.setVisible(true);
                placesReservedErrorLabel.setManaged(true);
                return false;
            }
            placesReservedErrorLabel.setVisible(false);
            placesReservedErrorLabel.setManaged(false);
            return true;
        } catch (NumberFormatException e) {
            placesReservedErrorLabel.setText("Must be a number");
            placesReservedErrorLabel.setVisible(true);
            placesReservedErrorLabel.setManaged(true);
            return false;
        }
    }

    private boolean validateComingFrom() {
        String text = comingFromField.getText();
        if (text.isEmpty()) {
            comingFromErrorLabel.setText("City is required");
            comingFromErrorLabel.setVisible(true);
            comingFromErrorLabel.setManaged(true);
            return false;
        }
        comingFromErrorLabel.setVisible(false);
        comingFromErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateAll() {
        return validateName() && validatePlacesReserved() && validateComingFrom();
    }

    private void saveRegistrationAction() {
        if (!validateAll()) {
            return;
        }

        try {
            if (currentRegistration != null) {
                currentRegistration.setName(nameField.getText());
                currentRegistration.setDisabledParking(disabledParkingCheckBox.isSelected());
                currentRegistration.setPlacesReserved(Integer.parseInt(placesReservedField.getText()));
                currentRegistration.setComingFrom(comingFromField.getText());
                serviceEventRegistration.update(currentRegistration);
                // Navigate back to list
                navigateBack();
            }
        } catch (Exception e) {
            nameErrorLabel.setText("Error saving registration: " + e.getMessage());
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
            e.printStackTrace();
        }
    }

    public void setRegistration(EventRegistration registration) {
        this.currentRegistration = registration;
        if (registration != null) {
            nameField.setText(registration.getName());
            disabledParkingCheckBox.setSelected(registration.isDisabledParking());
            placesReservedField.setText(registration.getPlacesReserved() != null ? String.valueOf(registration.getPlacesReserved()) : "");
            comingFromField.setText(registration.getComingFrom());
            saveButton.setText("Update"); // Match Symfony's {'button_label': 'Update'}
        } else {
            saveButton.setText("Save");
        }
    }

    private void resetForm() {
        nameField.clear();
        disabledParkingCheckBox.setSelected(false);
        placesReservedField.clear();
        comingFromField.clear();

        // Clear error labels
        nameErrorLabel.setVisible(false);
        nameErrorLabel.setManaged(false);
        disabledParkingErrorLabel.setVisible(false);
        disabledParkingErrorLabel.setManaged(false);
        placesReservedErrorLabel.setVisible(false);
        placesReservedErrorLabel.setManaged(false);
        comingFromErrorLabel.setVisible(false);
        comingFromErrorLabel.setManaged(false);

        // Reset to initial state if not editing
        if (currentRegistration == null) {
            saveButton.setText("Save");
        }
    }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventRegistrationList.fxml"));
            saveButton.getScene().setRoot(loader.load());
        } catch (java.io.IOException e) {
            nameErrorLabel.setText("Navigation Error: " + e.getMessage());
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
        }
    }
}