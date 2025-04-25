package controllers;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEvents;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventListController {

    @FXML
    private TextField searchField;
    @FXML
    private Button Registrationsbtn;
    @FXML
    private Button addButton;
    @FXML
    private Button exportPdfButton;
    @FXML
    private GridPane eventsGrid;

    private final ServiceEvents serviceEvents;
    private ObservableList<Events> eventsList;
    private ObservableList<Events> filteredEventsList;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EventListController() {
        this.serviceEvents = new ServiceEvents();
    }

    @FXML
    public void initialize() {
        // Initialize events lists
        eventsList = FXCollections.observableArrayList();
        filteredEventsList = FXCollections.observableArrayList();

        // Load events
        loadEvents();

        // Set up search
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterEvents(newValue));

        // Set up buttons
        Registrationsbtn.setOnAction(event -> navigateToRegistration());
        addButton.setOnAction(event -> navigateToAdd());
        exportPdfButton.setOnAction(event -> exportToPdf());
    }


    private void loadEvents() {
        List<Events> events = serviceEvents.getAll();
        System.out.println("Loaded events: " + events.size());
        eventsList.setAll(events);
        filteredEventsList.setAll(events);
        updateGrid();
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
        updateGrid();
    }

    private void updateGrid() {
        eventsGrid.getChildren().clear();
        int column = 0;
        int row = 0;

        for (Events event : filteredEventsList) {
            // Create event card
            VBox card = new VBox(10);
            card.getStyleClass().add("card");
            card.setStyle("-fx-background-color: #e1e1e1; -fx-border-radius: 50; -fx-padding: 15;");

            // Title
            Label titleLabel = new Label(event.getTitle() != null ? event.getTitle() : "N/A");
            titleLabel.getStyleClass().add("text-bold");
            titleLabel.setStyle("-fx-font-size: 16;");

            // Start Date
            Label startDateLabel = new Label("Start: " + (event.getStartDate() != null ? event.getStartDate().format(dateFormatter) : "N/A"));
            startDateLabel.getStyleClass().add("text-muted");
            startDateLabel.setStyle("-fx-font-size: 14;");

            // Category
            Label categoryLabel = new Label("Category: " + (event.getCategory() != null ? event.getCategory() : "N/A"));
            categoryLabel.getStyleClass().add("text-muted");
            categoryLabel.setStyle("-fx-font-size: 14;");

            // Action buttons
            HBox actionBox = new HBox(10);
            Button editButton = new Button();
            Button deleteButton = new Button();

            // Edit button
            editButton.getStyleClass().addAll("action-btn", "submit-btn");
            SVGPath editIcon = new SVGPath();
            editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
            editIcon.getStyleClass().add("btn-warning");
            editButton.setGraphic(editIcon);
            editButton.setTooltip(new Tooltip("Edit Event"));
            editButton.setOnAction(e -> navigateToEdit(event));

            // Delete button
            deleteButton.getStyleClass().addAll("action-btn", "submit-btn");
            SVGPath deleteIcon = new SVGPath();
            deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
            deleteIcon.getStyleClass().add("icon");
            deleteButton.setGraphic(deleteIcon);
            deleteButton.setTooltip(new Tooltip("Delete Event"));
            deleteButton.setOnAction(e -> deleteEvent(event));

            actionBox.getChildren().addAll(editButton, deleteButton);

            card.getChildren().addAll(titleLabel, startDateLabel, categoryLabel, actionBox);

            // Add to grid
            eventsGrid.add(card, column, row);
            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    private void navigateToRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventRegistrationList.fxml"));
            addButton.getScene().setRoot(loader.load());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load add event form: " + e.getMessage());
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
                    filterEvents(searchField.getText());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event deleted successfully.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete event: " + e.getMessage());
                }
            }
        });
    }

    private void exportToPdf() {
        try {
            File file = new File("events_report.pdf");
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);

            // Colors
            DeviceRgb LIGHT_BLUE = new DeviceRgb(173, 216, 230);
            DeviceRgb DARK_BLUE = new DeviceRgb(70, 130, 180);

            // Font
            PdfFont font = PdfFontFactory.createFont("Helvetica");
            PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");

            // Title page
            document.add(new Paragraph("Events Report")
                    .setFont(boldFont)
                    .setFontSize(24)
                    .setFontColor(DARK_BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(100));

            document.add(new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .setFont(font)
                    .setFontSize(12)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            document.add(new Paragraph("Event Management System")
                    .setFont(font)
                    .setFontSize(14)
                    .setFontColor(DARK_BLUE)
                    .setTextAlignment(TextAlignment.CENTER));

            // Add new page for table
            pdf.addNewPage();

            // Create table
            float[] columnWidths = {150, 100, 80};
            Table table = new Table(UnitValue.createPointArray(columnWidths))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(20);

            // Header cells
            String[] headers = {"Title", "Start Date", "Category"};
            for (String header : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(header)
                                .setFont(boldFont)
                                .setFontSize(10)
                                .setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(DARK_BLUE)
                        .setBorder(new SolidBorder(ColorConstants.WHITE, 1))
                        .setPadding(8)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE));
            }

            // Data rows
            boolean alternate = false;
            for (Events event : eventsList) {
                table.addCell(new Cell()
                        .add(new Paragraph(event.getTitle() != null ? event.getTitle() : "N/A")
                                .setFont(font)
                                .setFontSize(9))
                        .setBackgroundColor(alternate ? ColorConstants.WHITE : LIGHT_BLUE)
                        .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                        .setPadding(6));

                table.addCell(new Cell()
                        .add(new Paragraph(event.getStartDate() != null ? event.getStartDate().format(dateFormatter) : "N/A")
                                .setFont(font)
                                .setFontSize(9))
                        .setBackgroundColor(alternate ? ColorConstants.WHITE : LIGHT_BLUE)
                        .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                        .setPadding(6));

                table.addCell(new Cell()
                        .add(new Paragraph(event.getCategory() != null ? event.getCategory() : "N/A")
                                .setFont(font)
                                .setFontSize(9))
                        .setBackgroundColor(alternate ? ColorConstants.WHITE : LIGHT_BLUE)
                        .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                        .setPadding(6));

                alternate = !alternate;
            }

            document.add(table);

            // Footer
            document.add(new Paragraph("Page " + pdf.getNumberOfPages())
                    .setFont(font)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(20));

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