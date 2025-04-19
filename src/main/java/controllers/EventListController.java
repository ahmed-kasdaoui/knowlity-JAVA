package controllers;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEvents;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventListController {

    @FXML
    private TextField searchField;
    @FXML
    private Button btnSearch;
    @FXML
    private Button addButton;
    @FXML
    private Button exportPdfButton;
    @FXML
    private TableView<Events> eventsTableView;
    @FXML
    private TableColumn<Events, String> colName;
    @FXML
    private TableColumn<Events, String> colCode;
    @FXML
    private TableColumn<Events, String> colAbv;
    @FXML
    private TableColumn<Events, String> colAbv3;
    @FXML
    private TableColumn<Events, String> colAbv3Alt;
    @FXML
    private TableColumn<Events, String> colSlug;
    @FXML
    private TableColumn<Events, Void> colActions;
    @FXML
    private ComboBox<Integer> cmbEntries;
    @FXML
    private Label lblLegend;
    @FXML
    private Pagination pagination;
    @FXML
    private Hyperlink hlFirst;
    @FXML
    private Hyperlink hlLast;

    private final ServiceEvents serviceEvents;
    private ObservableList<Events> eventsList;
    private ObservableList<Events> filteredEventsList;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int DEFAULT_ENTRIES_PER_PAGE = 10;

    public EventListController() {
        this.serviceEvents = new ServiceEvents();
    }

    @FXML
    public void initialize() {
        // Initialize events lists
        eventsList = FXCollections.observableArrayList();
        filteredEventsList = FXCollections.observableArrayList();
        eventsTableView.setItems(filteredEventsList);


        // Set up table columns (mapping Events fields to screenshot columns)
        colName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle() != null ? cellData.getValue().getTitle() : "N/A"));
        colCode.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType() != null ? cellData.getValue().getType() : "N/A"));
        colAbv.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory() != null ? cellData.getValue().getCategory() : "N/A"));
        colAbv3.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStartDate() != null ? cellData.getValue().getStartDate().format(dateFormatter) : "N/A"));
        colAbv3Alt.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEndDate() != null ? cellData.getValue().getEndDate().format(dateFormatter) : "N/A"));
        colSlug.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation() != null ? cellData.getValue().getLocation() : "N/A"));

        // Set up actions column
        colActions.setCellFactory(col -> new TableCell<Events, Void>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final Button registrationsButton = new Button();
            private final HBox hbox = new HBox(10, editButton, deleteButton, registrationsButton); // Increased spacing to 10

            {
                // Set button styles
                editButton.getStyleClass().addAll("action-btn", "edit-btn");
                deleteButton.getStyleClass().addAll("action-btn", "delete-btn");
                registrationsButton.getStyleClass().addAll("action-btn", "registrations-btn");

                // Set SVG icons (simplified paths for better rendering at small sizes)
                SVGPath editIcon = new SVGPath();
                editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
                editIcon.getStyleClass().add("icon"); // Use CSS for styling
                editButton.setGraphic(editIcon);
                editButton.setTooltip(new Tooltip("Edit Event"));

                SVGPath deleteIcon = new SVGPath();
                deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                deleteIcon.getStyleClass().add("icon");
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setTooltip(new Tooltip("Delete Event"));

                SVGPath registrationsIcon = new SVGPath();
                registrationsIcon.setContent("M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z");
                registrationsIcon.getStyleClass().add("icon");
                registrationsButton.setGraphic(registrationsIcon);
                registrationsButton.setTooltip(new Tooltip("View Registrations"));

                // Set button actions
                editButton.setOnAction(event -> navigateToEdit(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> deleteEvent(getTableView().getItems().get(getIndex())));
                registrationsButton.setOnAction(event -> navigateToRegistrations(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });        // Load events
        loadEvents();

        // Set up search
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterEvents(newValue));

        // Set up buttons
        btnSearch.setOnAction(event -> showAlert(Alert.AlertType.INFORMATION, "Info", "Advanced Search functionality not implemented."));
        addButton.setOnAction(event -> navigateToAdd());
        exportPdfButton.setOnAction(event -> exportToPdf());

        // Set up pagination
        //pagination.setPageFactory(this::createPage);
        //hlFirst.setOnAction(event -> pagination.setCurrentPageIndex(0));
        //hlLast.setOnAction(event -> pagination.setCurrentPageIndex(pagination.getPageCount() - 1));
        //cmbEntries.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updatePagination());
    }

    private void loadEvents() {
        List<Events> events = serviceEvents.getAll();
        System.out.println("Loaded events: " + events.size()); // Debug log to check if events are loaded
        eventsList.setAll(events);
        filteredEventsList.setAll(events);
        //updatePagination();
    }

    private void filterEvents(String searchText) {
        filteredEventsList.clear();
        if (searchText == null || searchText.isEmpty()) {
            filteredEventsList.setAll(eventsList);
        } else {
            for (Events event : eventsList) {
                if (event.getTitle() != null && event.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredEventsList.add(event);
                }
            }
        }
        /*updatePagination();*/
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
                    filterEvents(searchField.getText());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event deleted successfully.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete event: " + e.getMessage());
                }
            }
        });
    }

    private void navigateToRegistrations(Events event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventRegistrationList.fxml"));
            eventsTableView.getScene().setRoot(loader.load());
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

            for (Events event : eventsList) {
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

    private TableView<Events> createPage(int pageIndex) {
        int entriesPerPage = 10;
        int fromIndex = pageIndex * entriesPerPage;
        int toIndex = Math.min(fromIndex + entriesPerPage, filteredEventsList.size());
        eventsTableView.setItems(FXCollections.observableArrayList(filteredEventsList.subList(fromIndex, toIndex)));
        updateLegend(fromIndex, toIndex);
        return eventsTableView;
    }

    /*private void updatePagination() {
        int entriesPerPage = cmbEntries.getValue();
        int pageCount = (int) Math.ceil((double) filteredEventsList.size() / entriesPerPage);
        pagination.setPageCount(pageCount > 0 ? pageCount : 1);
        pagination.setCurrentPageIndex(0);
        createPage(0);
    }*/

    private void updateLegend(int fromIndex, int toIndex) {
        lblLegend.setText(String.format("Showing %d to %d of %d entries.", fromIndex + 1, toIndex, filteredEventsList.size()));
    }
}