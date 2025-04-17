package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import tn.esprit.models.EventRegistration;
import tn.esprit.services.ServiceEventRegistration;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ShowEventRegistrationController {

    @FXML
    private Label idLabel;
    @FXML
    private Label eventTitleLabel;
    @FXML
    private Label seatsAvailableLabel;
    @FXML
    private Label userIdLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label registrationDateLabel;
    @FXML
    private Label placesReservedLabel;
    @FXML
    private Label comingFromLabel;
    @FXML
    private Label disabledParkingLabel;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private Button updateStatusButton;

    private final ServiceEventRegistration serviceEventRegistration;
    private EventRegistration currentRegistration;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ShowEventRegistrationController() {
        this.serviceEventRegistration = new ServiceEventRegistration();
    }

    @FXML
    public void initialize() {
        // Populate status ComboBox
        statusCombo.getItems().addAll("Pending", "Confirmed", "Canceled");

        // Set up event title click handler
        eventTitleLabel.setOnMouseClicked(event -> navigateToEventDetails());

        // Set up update status button
        updateStatusButton.setOnAction(event -> updateStatus());
    }

    public void setRegistration(EventRegistration registration) {
        this.currentRegistration = registration;
        if (registration != null) {
            idLabel.setText(String.valueOf(registration.getId()));
            eventTitleLabel.setText(registration.getEvent() != null ? registration.getEvent().getTitle() : "N/A");
            seatsAvailableLabel.setText(registration.getEvent() != null ? String.valueOf(registration.getEvent().getSeatsAvailable()) : "N/A");
            userIdLabel.setText(String.valueOf(registration.getUserId()));
            statusLabel.setText(registration.getStatus() != null ? registration.getStatus() : "N/A");
            registrationDateLabel.setText(registration.getRegistrationDate() != null ? registration.getRegistrationDate().format(dateFormatter) : "N/A");
            placesReservedLabel.setText(registration.getPlacesReserved() != null ? String.valueOf(registration.getPlacesReserved()) : "N/A");
            comingFromLabel.setText(registration.getComingFrom() != null ? registration.getComingFrom() : "N/A");
            disabledParkingLabel.setText(registration.isDisabledParking() ? "Yes" : "No");
            statusCombo.setValue(registration.getStatus());
        }
    }

    private void updateStatus() {
        String newStatus = statusCombo.getValue();
        if (newStatus == null) {
            showAlert(Alert.AlertType.ERROR, "Invalid Status", "Please select a status.");
            return;
        }

        if (currentRegistration != null) {
            try {
                currentRegistration.setStatus(newStatus);
                serviceEventRegistration.update(currentRegistration);
                statusLabel.setText(newStatus);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Status updated successfully.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status: " + e.getMessage());
            }
        }
    }

    private void navigateToEventDetails() {
        if (currentRegistration == null || currentRegistration.getEvent() == null) {
            showAlert(Alert.AlertType.ERROR, "No Event", "No event associated with this registration.");
            return;
        }

        try {
            // Placeholder: Load event details view (to be implemented)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ShowEvent.fxml"));
            eventTitleLabel.getScene().setRoot(loader.load());
            // Pass event to controller if ShowEvent.fxml exists
            // ShowEventController controller = loader.getController();
            // controller.setEvent(currentRegistration.getEvent());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load event details: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}