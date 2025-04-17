package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import tn.esprit.models.EventRegistration;
import tn.esprit.services.ServiceEventRegistration;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventRegistrationListController {

    @FXML
    private Button createButton;
    @FXML
    private Button backToEventsButton;
    @FXML
    private TableView<EventRegistration> registrationsTable;
    @FXML
    private TableColumn<EventRegistration, Integer> idColumn;
    @FXML
    private TableColumn<EventRegistration, Integer> eventIdColumn;
    @FXML
    private TableColumn<EventRegistration, Integer> userIdColumn;
    @FXML
    private TableColumn<EventRegistration, String> statusColumn;
    @FXML
    private TableColumn<EventRegistration, String> registrationDateColumn;
    @FXML
    private TableColumn<EventRegistration, Void> actionsColumn;

    private final ServiceEventRegistration serviceEventRegistration;
    private ObservableList<EventRegistration> registrationsList;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EventRegistrationListController() {
        this.serviceEventRegistration = new ServiceEventRegistration();
    }

    @FXML
    public void initialize() {
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        eventIdColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleIntegerProperty(
                    cellData.getValue().getEvent() != null ?
                            cellData.getValue().getEvent().getId() : 0
            ).asObject();
        });
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        registrationDateColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getRegistrationDate() != null ?
                            cellData.getValue().getRegistrationDate().format(dateFormatter) : "N/A"
            );
        });

        // Actions column
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button showButton = new Button("Show");
            private final Button editButton = new Button("Edit");
            private final HBox actionsHBox = new HBox(5, showButton, editButton);

            {
                showButton.getStyleClass().add("submit-btn");
                editButton.getStyleClass().add("submit-btn");

                showButton.setOnAction(event -> {
                    EventRegistration registration = getTableView().getItems().get(getIndex());
                    navigateToShow(registration);
                });

                editButton.setOnAction(event -> {
                    EventRegistration registration = getTableView().getItems().get(getIndex());
                    navigateToEdit(registration);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionsHBox);
            }
        });

        // Load registrations
        loadRegistrations();

        // Set up buttons
        createButton.setOnAction(event -> navigateToCreate());
        backToEventsButton.setOnAction(event -> navigateToEvents());
    }

    private void loadRegistrations() {
        List<EventRegistration> registrations = serviceEventRegistration.getAll();
        registrationsList = FXCollections.observableArrayList(registrations);
        registrationsTable.setItems(registrationsList);
    }

    private void navigateToCreate() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NewEventRegistration.fxml"));
            createButton.getScene().setRoot(loader.load());
            // NewEventRegistrationController controller = loader.getController();
            // controller.setEvent(event);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load new registration form: " + e.getMessage());
        }
    }

    private void navigateToShow(EventRegistration registration) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ShowEventRegistration.fxml"));
            createButton.getScene().setRoot(loader.load());
            ShowEventRegistrationController controller = loader.getController();
            controller.setRegistration(registration);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load show registration page: " + e.getMessage());
        }
    }

    private void navigateToEdit(EventRegistration registration) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditEventRegistration.fxml"));
            createButton.getScene().setRoot(loader.load());
            EditEventRegistrationController controller = loader.getController();
            controller.setRegistration(registration);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load edit registration form: " + e.getMessage());
        }
    }

    private void navigateToEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventList.fxml"));
            createButton.getScene().setRoot(loader.load());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load events list: " + e.getMessage());
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