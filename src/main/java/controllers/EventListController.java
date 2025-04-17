package controllers;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEvents;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventListController {

    @FXML
    private TextField searchField;
    @FXML
    private Button addButton;
    @FXML
    private Button exportPdfButton;
    @FXML
    private TableView<Events> eventsTable;
    @FXML
    private TableColumn<Events, String> titleColumn;
    @FXML
    private TableColumn<Events, String> descriptionColumn;
    @FXML
    private TableColumn<Events, String> startDateColumn;
    @FXML
    private TableColumn<Events, String> endDateColumn;
    @FXML
    private TableColumn<Events, String> typeColumn;
    @FXML
    private TableColumn<Events, Integer> maxParticipantsColumn;
    @FXML
    private TableColumn<Events, Integer> seatsAvailableColumn;
    @FXML
    private TableColumn<Events, String> locationColumn;
    @FXML
    private TableColumn<Events, String> createdAtColumn;
    @FXML
    private TableColumn<Events, Void> imageColumn;
    @FXML
    private TableColumn<Events, String> categoryColumn;
    @FXML
    private TableColumn<Events, Void> actionsColumn;

    private final ServiceEvents serviceEvents;
    private ObservableList<Events> eventsList;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EventListController() {
        this.serviceEvents = new ServiceEvents();
    }

    @FXML
    public void initialize() {
        // Set up table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        startDateColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getStartDate() != null ?
                            cellData.getValue().getStartDate().format(dateFormatter) : "N/A"
            );
        });
        endDateColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getEndDate() != null ?
                            cellData.getValue().getEndDate().format(dateFormatter) : "N/A"
            );
        });
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        maxParticipantsColumn.setCellValueFactory(new PropertyValueFactory<>("maxParticipants"));
        seatsAvailableColumn.setCellValueFactory(new PropertyValueFactory<>("seatsAvailable"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        createdAtColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getCreatedAt() != null ?
                            cellData.getValue().getCreatedAt().format(dateFormatter) : "N/A"
            );
        });
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Image column
        imageColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Events event = getTableView().getItems().get(getIndex());
                    String path = event.getImage();
                    if (!path.startsWith("/images/") && !path.equals("/images/placeholder.png")) {
                        path = "/images/" + path;
                    }
                    try {
                        InputStream stream = getClass().getResourceAsStream(path);
                        if (stream == null) {
                            System.err.println("Image not found: " + path);
                            stream = getClass().getResourceAsStream("/images/placeholder.png");
                        }
                        imageView.setImage(new Image(stream));
                    } catch (Exception e) {
                        System.err.println("Error loading image: " + path + ". Error: " + e.getMessage());
                        imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
                    }
                    setGraphic(imageView);
                }
            }
        });

        // Actions column
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox actionsHBox = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("submit-btn");
                deleteButton.getStyleClass().add("reset-btn");

                editButton.setOnAction(event -> {
                    Events eventToEdit = getTableView().getItems().get(getIndex());
                    navigateToEdit(eventToEdit);
                });

                deleteButton.setOnAction(event -> {
                    Events eventToDelete = getTableView().getItems().get(getIndex());
                    deleteEvent(eventToDelete);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionsHBox);
            }
        });

        // Load events
        loadEvents();

        // Set up search
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterEvents(newValue));

        // Set up buttons
        addButton.setOnAction(event -> navigateToAdd());
        exportPdfButton.setOnAction(event -> exportToPdf());
    }

    private void loadEvents() {
        List<Events> events = serviceEvents.getAll();
        eventsList = FXCollections.observableArrayList(events);
        eventsTable.setItems(eventsList);
    }

    private void filterEvents(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            eventsTable.setItems(eventsList);
        } else {
            ObservableList<Events> filteredList = FXCollections.observableArrayList();
            for (Events event : eventsList) {
                if (event.getTitle() != null && event.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(event);
                }
            }
            eventsTable.setItems(filteredList);
        }
    }

    private void navigateToAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventForm.fxml"));
            addButton.getScene().setRoot(loader.load());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load add event form: " + e.getMessage());
        }
    }

    private void navigateToEdit(Events event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditEvent.fxml"));
            addButton.getScene().setRoot(loader.load());
            EditEventController controller = loader.getController();
            controller.setEvent(event);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load edit event form: " + e.getMessage());
        }
    }

    private void deleteEvent(Events event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to delete this event?");
        confirmAlert.setContentText("Event: " + event.getTitle());
        confirmAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceEvents.delete(event);
                    eventsList.remove(event);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event deleted successfully.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete event: " + e.getMessage());
                }
            }
        });
    }

    private void navigateToRegistrations(Events event) {
        try {
            // Placeholder: Load registrations list view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventRegistrationsList.fxml"));
            addButton.getScene().setRoot(loader.load());
            // Pass event to controller if EventRegistrationsList.fxml exists
            // EventRegistrationsListController controller = loader.getController();
            // controller.setEvent(event);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load registrations list: " + e.getMessage());
        }
    }

    private void exportToPdf() {
        try {
            File file = new File("events_report.pdf");
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Events Report").setFontSize(20).setBold());

            float[] columnWidths = {150, 200, 120, 120, 100, 120, 120, 150, 120, 100};
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.addHeaderCell("Title");
            table.addHeaderCell("Description");
            table.addHeaderCell("Start Date");
            table.addHeaderCell("End Date");
            table.addHeaderCell("Type");
            table.addHeaderCell("Max Participants");
            table.addHeaderCell("Seats Available");
            table.addHeaderCell("Location");
            table.addHeaderCell("Created At");
            table.addHeaderCell("Category");

            for (Events event : eventsTable.getItems()) {
                table.addCell(event.getTitle() != null ? event.getTitle() : "N/A");
                table.addCell(event.getDescription() != null ? event.getDescription() : "N/A");
                table.addCell(event.getStartDate() != null ? event.getStartDate().format(dateFormatter) : "N/A");
                table.addCell(event.getEndDate() != null ? event.getEndDate().format(dateFormatter) : "N/A");
                table.addCell(event.getType() != null ? event.getType() : "N/A");
                table.addCell(String.valueOf(event.getMaxParticipants()));
                table.addCell(String.valueOf(event.getSeatsAvailable()));
                table.addCell(event.getLocation() != null ? event.getLocation() : "N/A");
                table.addCell(event.getCreatedAt() != null ? event.getCreatedAt().format(dateFormatter) : "N/A");
                table.addCell(event.getCategory() != null ? event.getCategory() : "N/A");
            }

            document.add(table);
            document.close();
            showAlert(Alert.AlertType.INFORMATION, "Success", "PDF exported successfully to events_report.pdf");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Export Error", "Failed to export PDF: " + e.getMessage());
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