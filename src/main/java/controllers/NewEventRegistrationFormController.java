package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import tn.esprit.models.EventRegistration;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEventRegistration;
import tn.esprit.services.ServiceEvents;
import tn.esprit.models.UserEventPreference;
import tn.esprit.services.ServiceUserEventPreference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NewEventRegistrationFormController {

    @FXML
    private TextField nameField;
    @FXML
    private Label nameErrorLabel;
    @FXML
    private TextField comingFromField;
    @FXML
    private Label comingFromErrorLabel;
    @FXML
    private TextField placesReservedField;
    @FXML
    private Label placesReservedErrorLabel;
    @FXML
    private CheckBox disabledParkingCheckBox;
    @FXML
    private Label disabledParkingErrorLabel;
    @FXML
    private CheckBox agreeTermsCheckBox;
    @FXML
    private Label agreeTermsErrorLabel;
    @FXML
    private Button submitButton;
    @FXML
    private GridPane formGrid;
    @FXML
    private VBox successMessage;

    private final ServiceEventRegistration serviceEventRegistration;
    private Events event;
    private UserEventPreference userEventPreference;
    private final ServiceUserEventPreference serviceUserEventPreference;
    private static final Pattern NAME_CITY_PATTERN = Pattern.compile("^[a-zA-Z\\s]*$");

    public NewEventRegistrationFormController() {
        this.serviceEventRegistration = new ServiceEventRegistration();
        this.serviceUserEventPreference = new ServiceUserEventPreference();
    }


    @FXML
    public void initialize() {
        // Real-time validation for Name
        nameField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                nameErrorLabel.setText("Name is required");
                nameErrorLabel.setVisible(true);
                nameErrorLabel.setManaged(true);
            } else if (!NAME_CITY_PATTERN.matcher(newValue).matches()) {
                nameErrorLabel.setText("Name must contain only letters and spaces");
                nameErrorLabel.setVisible(true);
                nameErrorLabel.setManaged(true);
            } else {
                nameErrorLabel.setVisible(false);
                nameErrorLabel.setManaged(false);
            }
        });

        // Real-time validation for Coming From
        comingFromField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                comingFromErrorLabel.setText("City is required");
                comingFromErrorLabel.setVisible(true);
                comingFromErrorLabel.setManaged(true);
            } else if (!NAME_CITY_PATTERN.matcher(newValue).matches()) {
                comingFromErrorLabel.setText("City must contain only letters and spaces");
                comingFromErrorLabel.setVisible(true);
                comingFromErrorLabel.setManaged(true);
            } else {
                comingFromErrorLabel.setVisible(false);
                comingFromErrorLabel.setManaged(false);
            }
        });

        // Real-time validation for Places Reserved
        placesReservedField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                placesReservedErrorLabel.setText("Number of places is required");
                placesReservedErrorLabel.setVisible(true);
                placesReservedErrorLabel.setManaged(true);
            } else {
                try {
                    int value = Integer.parseInt(newValue);
                    if (value < 1 || value > 5) {
                        placesReservedErrorLabel.setText("Must be between 1 and 5");
                        placesReservedErrorLabel.setVisible(true);
                        placesReservedErrorLabel.setManaged(true);
                    } else {
                        placesReservedErrorLabel.setVisible(false);
                        placesReservedErrorLabel.setManaged(false);
                    }
                } catch (NumberFormatException e) {
                    placesReservedErrorLabel.setText("Must be a number");
                    placesReservedErrorLabel.setVisible(true);
                    placesReservedErrorLabel.setManaged(true);
                }
            }
        });

        // Real-time validation for Agree Terms
        agreeTermsCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {
                agreeTermsErrorLabel.setText("You must agree to the terms and conditions");
                agreeTermsErrorLabel.setVisible(true);
                agreeTermsErrorLabel.setManaged(true);
            } else {
                agreeTermsErrorLabel.setVisible(false);
                agreeTermsErrorLabel.setManaged(false);
            }
        });

        // No validation for Disabled Parking
        disabledParkingErrorLabel.setVisible(false);
        disabledParkingErrorLabel.setManaged(false);

        // Set up submit button
        submitButton.setOnAction(e -> handleSubmit());
    }

    public void setEvent(Events event) {
        this.event = event;
    }

    private void handleSubmit() {
        if (!validateForm()) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Validation Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Please correct the errors in the form before submitting.");
            errorAlert.showAndWait();
            return;
        }

        // Confirmation Alert
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Submission");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to submit the registration?");
        confirmAlert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                submitForm();
            }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        // Validate Name
        String name = nameField.getText();
        if (name.isEmpty()) {
            nameErrorLabel.setText("Name is required");
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
            valid = false;
        } else if (!NAME_CITY_PATTERN.matcher(name).matches()) {
            nameErrorLabel.setText("Name must contain only letters and spaces");
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
            valid = false;
        } else {
            nameErrorLabel.setVisible(false);
            nameErrorLabel.setManaged(false);
        }

        // Validate Coming From
        String city = comingFromField.getText();
        if (city.isEmpty()) {
            comingFromErrorLabel.setText("City is required");
            comingFromErrorLabel.setVisible(true);
            comingFromErrorLabel.setManaged(true);
            valid = false;
        } else if (!NAME_CITY_PATTERN.matcher(city).matches()) {
            comingFromErrorLabel.setText("City must contain only letters and spaces");
            comingFromErrorLabel.setVisible(true);
            comingFromErrorLabel.setManaged(true);
            valid = false;
        } else {
            comingFromErrorLabel.setVisible(false);
            comingFromErrorLabel.setManaged(false);
        }

        // Validate Places Reserved
        String placesText = placesReservedField.getText();
        if (placesText.isEmpty()) {
            placesReservedErrorLabel.setText("Number of places is required");
            placesReservedErrorLabel.setVisible(true);
            placesReservedErrorLabel.setManaged(true);
            valid = false;
        } else {
            try {
                int value = Integer.parseInt(placesText);
                if (value < 1 || value > 5) {
                    placesReservedErrorLabel.setText("Must be between 1 and 5");
                    placesReservedErrorLabel.setVisible(true);
                    placesReservedErrorLabel.setManaged(true);
                    valid = false;
                } else {
                    placesReservedErrorLabel.setVisible(false);
                    placesReservedErrorLabel.setManaged(false);
                }
            } catch (NumberFormatException e) {
                placesReservedErrorLabel.setText("Must be a number");
                placesReservedErrorLabel.setVisible(true);
                placesReservedErrorLabel.setManaged(true);
                valid = false;
            }
        }

        // Validate Agree Terms
        if (!agreeTermsCheckBox.isSelected()) {
            agreeTermsErrorLabel.setText("You must agree to the terms and conditions");
            agreeTermsErrorLabel.setVisible(true);
            agreeTermsErrorLabel.setManaged(true);
            valid = false;
        } else {
            agreeTermsErrorLabel.setVisible(false);
            agreeTermsErrorLabel.setManaged(false);
        }

        // Disabled Parking requires no validation
        disabledParkingErrorLabel.setVisible(false);
        disabledParkingErrorLabel.setManaged(false);

        return valid;
    }

    private void submitForm() {
        try {
            int placesReserved = Integer.parseInt(placesReservedField.getText());

            if (placesReserved > event.getSeatsAvailable()) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Submission Error");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Not enough seats available!");
                errorAlert.showAndWait();
                return;
            }

            EventRegistration registration = new EventRegistration();
            registration.setEvent(event);
            registration.setUserId(1);
            registration.setName(nameField.getText());
            registration.setComingFrom(comingFromField.getText());
            registration.setPlacesReserved(placesReserved);
            registration.setDisabledParking(disabledParkingCheckBox.isSelected());

            serviceEventRegistration.add(registration);
            userpreference(event);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Registration submitted successfully!");
            successAlert.showAndWait();

            navigateBack();

        } catch (NumberFormatException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Submission Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Invalid number of places reserved!");
            errorAlert.showAndWait();
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Submission Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Error saving registration: " + e.getMessage());
            errorAlert.showAndWait();
            e.printStackTrace();
        }
    }

    private void userpreference(Events e){
        UserEventPreference preference = new UserEventPreference();
        String type = e.getType();
        String category = e.getCategory();
        preference=serviceUserEventPreference.getByUserIdCategoryAndTpe(1, category, type);
        if (preference==null){
            preference=new UserEventPreference(1,category,type,1);
            serviceUserEventPreference.add(preference);
        }else {
            int score= preference.getPreferenceScore()+1;
            preference.setPreferenceScore(score);
            serviceUserEventPreference.update(preference);
        }
    }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventListing.fxml"));
            nameField.getScene().setRoot(loader.load());
        } catch (IOException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Navigation Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Navigation Error: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
}