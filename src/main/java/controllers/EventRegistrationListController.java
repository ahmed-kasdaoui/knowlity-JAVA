package controllers;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;
import tn.esprit.models.EventRegistration;
import tn.esprit.services.ServiceEventRegistration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventRegistrationListController {

    @FXML
    private Button backToEventsButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button exportPdfButton;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<EventRegistration> registrationsTable;
    @FXML
    private TableColumn<EventRegistration, String> eventIdColumn;
    @FXML
    private TableColumn<EventRegistration, String> userIdColumn;
    @FXML
    private TableColumn<EventRegistration, String> statusColumn;
    @FXML
    private TableColumn<EventRegistration, Void> actionsColumn;
    @FXML
    private TableColumn<EventRegistration, String> registrationDateColumn;

    private final ServiceEventRegistration serviceEventRegistration;
    private ObservableList<EventRegistration> registrationsList;
    private ObservableList<EventRegistration> filteredRegistrationsList;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EventRegistrationListController() {
        this.serviceEventRegistration = new ServiceEventRegistration();
    }

    @FXML
    public void initialize() {
        // Initialize registrations lists
        registrationsList = FXCollections.observableArrayList();
        filteredRegistrationsList = FXCollections.observableArrayList();
        registrationsTable.setItems(filteredRegistrationsList);

        // Set up table columns
        eventIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEvent().getTitle() != null ? cellData.getValue().getEvent().getTitle() : "N/A"));
        userIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName() != null ? cellData.getValue().getName() : "N/A"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        registrationDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getRegistrationDate() != null ?
                        cellData.getValue().getRegistrationDate().format(dateFormatter) : "N/A"
        ));

        // Actions column
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button showButton = new Button();
            private final Button deleteRowButton = new Button();
            private final HBox actionsHBox = new HBox(10, showButton, deleteRowButton);

            {
                // Style and configure Show button
                showButton.getStyleClass().addAll("action-btn", "submit-btn");
                SVGPath showIcon = new SVGPath();
                showIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
                showIcon.getStyleClass().add("icon");
                showButton.setGraphic(showIcon);
                showButton.setTooltip(new Tooltip("Show Registration"));

                // Style and configure Delete button
                deleteRowButton.getStyleClass().addAll("action-btn", "round");
                SVGPath deleteIcon = new SVGPath();
                deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                deleteIcon.setStyle("-fx-fill: white;");
                deleteIcon.getStyleClass().add("icon");
                deleteRowButton.setGraphic(deleteIcon);
                deleteRowButton.setTooltip(new Tooltip("Delete Registration"));

                // Set button actions
                showButton.setOnAction(event -> {
                    EventRegistration registration = getTableView().getItems().get(getIndex());
                    navigateToShow(registration);
                });
                deleteRowButton.setOnAction(event -> {
                    EventRegistration registration = getTableView().getItems().get(getIndex());
                    registrationsTable.getSelectionModel().select(registration);
                    deleteSelectedRegistration();
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
        backToEventsButton.setOnAction(event -> navigateToEvents());
        deleteButton.setOnAction(event -> deleteSelectedRegistration());
        exportPdfButton.setOnAction(event -> exportToPdf());

        // Set up search
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterRegistrations(newValue));
    }

    private void loadRegistrations() {
        List<EventRegistration> registrations = serviceEventRegistration.getAll();
        registrationsList.setAll(registrations);
        filteredRegistrationsList.setAll(registrations);
    }

    private void filterRegistrations(String searchText) {
        filteredRegistrationsList.clear();
        if (searchText == null || searchText.isEmpty()) {
            filteredRegistrationsList.setAll(registrationsList);
        } else {
            for (EventRegistration registration : registrationsList) {
                boolean matches = false;
                if (registration.getEvent().getTitle() != null && registration.getEvent().getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                    matches = true;
                } else if (registration.getName() != null && registration.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    matches = true;
                } else if (registration.getStatus() != null && registration.getStatus().toLowerCase().contains(searchText.toLowerCase())) {
                    matches = true;
                }
                if (matches) {
                    filteredRegistrationsList.add(registration);
                }
            }
        }
    }

    private void deleteSelectedRegistration() {
        EventRegistration selectedRegistration = registrationsTable.getSelectionModel().getSelectedItem();
        if (selectedRegistration == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a registration to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to delete this registration?");
        confirmAlert.setContentText("Event: " + (selectedRegistration.getEvent().getTitle() != null ? selectedRegistration.getEvent().getTitle() : "N/A") +
                "\nUser: " + (selectedRegistration.getName() != null ? selectedRegistration.getName() : "N/A"));
        confirmAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceEventRegistration.delete(selectedRegistration);
                    registrationsList.remove(selectedRegistration);
                    filterRegistrations(searchField.getText());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Registration deleted successfully.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete registration: " + e.getMessage());
                }
            }
        });
    }

    private void navigateToShow(EventRegistration registration) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ShowEventRegistration.fxml"));
            backToEventsButton.getScene().setRoot(loader.load());
            ShowEventRegistrationController controller = loader.getController();
            controller.setRegistration(registration);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load show registration page: " + e.getMessage());
        }
    }

    private void navigateToEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventList.fxml"));
            backToEventsButton.getScene().setRoot(loader.load());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load events list: " + e.getMessage());
        }
    }

    private void exportToPdf() {
        try {
            File file = new File("registrations_report.pdf");
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
            document.add(new Paragraph("Event Registrations Report")
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
            float[] columnWidths = {150, 150, 100, 150};
            Table table = new Table(UnitValue.createPointArray(columnWidths))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(20);

            // Header cells
            String[] headers = {"Event Title", "User Name", "Status", "Registration Date"};
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
            for (EventRegistration registration : registrationsList) {
                table.addCell(new Cell()
                        .add(new Paragraph(registration.getEvent().getTitle() != null ? registration.getEvent().getTitle() : "N/A")
                                .setFont(font)
                                .setFontSize(9))
                        .setBackgroundColor(alternate ? ColorConstants.WHITE : LIGHT_BLUE)
                        .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                        .setPadding(6));

                table.addCell(new Cell()
                        .add(new Paragraph(registration.getName() != null ? registration.getName() : "N/A")
                                .setFont(font)
                                .setFontSize(9))
                        .setBackgroundColor(alternate ? ColorConstants.WHITE : LIGHT_BLUE)
                        .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                        .setPadding(6));

                table.addCell(new Cell()
                        .add(new Paragraph(registration.getStatus() != null ? registration.getStatus() : "N/A")
                                .setFont(font)
                                .setFontSize(9))
                        .setBackgroundColor(alternate ? ColorConstants.WHITE : LIGHT_BLUE)
                        .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                        .setPadding(6));

                table.addCell(new Cell()
                        .add(new Paragraph(registration.getRegistrationDate() != null ? registration.getRegistrationDate().format(dateFormatter) : "N/A")
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
            showAlert(Alert.AlertType.INFORMATION, "Success", "PDF exported successfully to registrations_report.pdf");
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