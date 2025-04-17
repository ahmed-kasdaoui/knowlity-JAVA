package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.models.EventRegistration;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEventRegistration;
import tn.esprit.services.ServiceEvents;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class NewEventRegistrationFormController {

    @FXML
    private HBox stepIndicator;
    @FXML
    private TabPane tabPane;
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
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private VBox successMessage;

    private final ServiceEventRegistration serviceEventRegistration;
    private Events event;
    private int currentTab = 0;

    public NewEventRegistrationFormController() {
        this.serviceEventRegistration = new ServiceEventRegistration();
    }

    @FXML
    public void initialize() {
        // Disable tab selection
        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, old, newValue) -> {
            if (newValue.intValue() != currentTab) {
                tabPane.getSelectionModel().select(currentTab);
            }
        });

        // Set up navigation buttons
        prevButton.setOnAction(e -> nextPrev(-1));
        nextButton.setOnAction(e -> nextPrev(1));

        // Show initial tab
        showTab(currentTab);
    }

    public void setEvent(Events event) {
        this.event = event;
    }

    private void showTab(int n) {
        currentTab = n;
        tabPane.getSelectionModel().select(n);

        // Update step indicator
        for (int i = 0; i < stepIndicator.getChildren().size(); i++) {
            Label step = (Label) stepIndicator.getChildren().get(i);
            step.getStyleClass().remove("active");
            if (i <= n) {
                step.getStyleClass().add("active");
            }
            if (i < n) {
                step.getStyleClass().add("finish");
            }
        }

        // Update navigation buttons
        prevButton.setVisible(n > 0);
        if (n == tabPane.getTabs().size() - 1) {
            nextButton.setText("Submit");
        } else {
            nextButton.setText("Next");
        }
    }

    private void nextPrev(int n) {
        if (n == 1 && !validateForm()) {
            return;
        }

        int newTab = currentTab + n;
        if (newTab >= tabPane.getTabs().size()) {
            submitForm();
            return;
        }

        if (newTab >= 0) {
            showTab(newTab);
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        switch (currentTab) {
            case 0: // Step 1: Name
                if (nameField.getText().isEmpty()) {
                    nameErrorLabel.setText("Name is required");
                    nameErrorLabel.setVisible(true);
                    nameErrorLabel.setManaged(true);
                    valid = false;
                } else {
                    nameErrorLabel.setVisible(false);
                    nameErrorLabel.setManaged(false);
                }
                break;
            case 1: // Step 2: Coming From
                if (comingFromField.getText().isEmpty()) {
                    comingFromErrorLabel.setText("City is required");
                    comingFromErrorLabel.setVisible(true);
                    comingFromErrorLabel.setManaged(true);
                    valid = false;
                } else {
                    comingFromErrorLabel.setVisible(false);
                    comingFromErrorLabel.setManaged(false);
                }
                break;
            case 2: // Step 3: Places Reserved
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
                break;
            case 3: // Step 4: Disabled Parking (no validation needed)
                break;
            case 4: // Step 5: Agree Terms
                if (!agreeTermsCheckBox.isSelected()) {
                    agreeTermsErrorLabel.setText("You must agree to the terms and conditions");
                    agreeTermsErrorLabel.setVisible(true);
                    agreeTermsErrorLabel.setManaged(true);
                    valid = false;
                } else {
                    agreeTermsErrorLabel.setVisible(false);
                    agreeTermsErrorLabel.setManaged(false);
                }
                break;
        }

        return valid;
    }

    private void submitForm() {
        try {
            // Parse places reserved
            int placesReserved = Integer.parseInt(placesReservedField.getText());

            // Check if enough seats are available
            if (placesReserved > event.getSeatsAvailable()) {
                nameErrorLabel.setText("Not enough seats available!");
                nameErrorLabel.setVisible(true);
                nameErrorLabel.setManaged(true);
                return;
            }

            // Create registration
            EventRegistration registration = new EventRegistration();
            registration.setEvent(event);
            registration.setUserId(1); // Replace with actual user context
            registration.setName(nameField.getText());
            registration.setComingFrom(comingFromField.getText());
            registration.setPlacesReserved(placesReserved);
            registration.setDisabledParking(disabledParkingCheckBox.isSelected());

            // Add registration
            serviceEventRegistration.add(registration);

            // Update event's available seats
            ServiceEvents serviceEvents = new ServiceEvents();
            serviceEvents.updateSeatsAvailable(event.getId(),event.getSeatsAvailable() - placesReserved,placesReserved);

            // Show success message
            tabPane.setVisible(false);
            tabPane.setManaged(false);
            prevButton.setVisible(false);
            nextButton.setVisible(false);
            successMessage.setVisible(true);
            successMessage.setManaged(true);

            // Navigate back after a delay (e.g., 2 seconds)
            new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> navigateBack());
                        }
                    },
                    2000
            );
        } catch (NumberFormatException e) {
            nameErrorLabel.setText("Invalid number of places reserved!");
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
        } catch (Exception e) {
            nameErrorLabel.setText("Error saving registration: " + e.getMessage());
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
            e.printStackTrace();
        }
    }
    
    
    
    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventListing.fxml"));
            nameField.getScene().setRoot(loader.load());
        } catch (IOException e) {
            nameErrorLabel.setText("Navigation Error: " + e.getMessage());
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
        }
    }
}