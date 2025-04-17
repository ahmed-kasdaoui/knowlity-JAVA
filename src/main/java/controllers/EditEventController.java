package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEvents;

import java.io.IOException;

public class EditEventController {

    @FXML
    private EventAdminFormController eventFormController;
    @FXML
    private Button backButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Label titleLabel;

    private final ServiceEvents serviceEvents;
    private Events currentEvent;

    public EditEventController() {
        this.serviceEvents = new ServiceEvents();
    }

    @FXML
    public void initialize() {
        // Bind form controller
        deleteButton.setOnAction(e -> deleteEvent());
        backButton.setOnAction(e -> navigateBack());
    }

    public void setEvent(Events event) {
        this.currentEvent = event;
        if (event != null) {
            eventFormController.setEvent(event);
            titleLabel.setText("Edit Event: " + event.getTitle());
        }
    }

    private void deleteEvent() {
        if (currentEvent == null) {
            showAlert(Alert.AlertType.ERROR, "No Event", "No event selected to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to delete this event?");
        confirmAlert.setContentText("Event: " + currentEvent.getTitle());
        confirmAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceEvents.delete(currentEvent);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event deleted successfully.");
                    navigateBack();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete event: " + e.getMessage());
                }
            }
        });
    }

    private void navigateBack() {
        try {
            // Placeholder: Load event list view (to be implemented with event.html.twig)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventList.fxml"));
            backButton.getScene().setRoot(loader.load());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load event list: " + e.getMessage());
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