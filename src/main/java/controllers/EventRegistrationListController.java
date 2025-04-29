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
    private Button exportPdfButton;
    @FXML
    private TextField searchField;
    @FXML
    private GridPane registrationsGrid;
    private EventRegistration selectedRegistration;

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

        // Load registrations
        loadRegistrations();

        // Set up buttons
        backToEventsButton.setOnAction(event -> navigateToEvents());
        exportPdfButton.setOnAction(event -> exportToPdf());

        // Set up search
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterRegistrations(newValue));
    }

    private void loadRegistrations() {
        List<EventRegistration> registrations = serviceEventRegistration.getAll();
        registrationsList.setAll(registrations);
        filteredRegistrationsList.setAll(registrations);
        updateGrid();
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
        updateGrid();
    }

    private void updateGrid() {
        registrationsGrid.getChildren().clear();
        int column = 0;
        int row = 0;

        for (EventRegistration registration : filteredRegistrationsList) {
            // Create registration card
            VBox card = new VBox(10);
            card.getStyleClass().add("card");
            card.setStyle("-fx-background-color: #e1e1e1; -fx-border-color: -light-gray-2; -fx-border-radius: 5; -fx-padding: 15;");

            // Event Title
            Label statusLabel = new Label(registration.getStatus() != null ? registration.getStatus() : "N/A");
            statusLabel.getStyleClass().add("text-bold");
            statusLabel.setStyle("-fx-font-size: 16;");

            // Registration Date
            Label registrationDateLabel = new Label("Date: " + (registration.getRegistrationDate() != null ? registration.getRegistrationDate().format(dateFormatter) : "N/A"));
            registrationDateLabel.getStyleClass().add("text-muted");
            registrationDateLabel.setStyle("-fx-font-size: 14;");

            // Status
            Label eventTitleLabel = new Label((registration.getEvent().getTitle() != null ? registration.getEvent().getTitle() : "N/A"));
            eventTitleLabel.getStyleClass().add("text-muted");
            eventTitleLabel.setStyle("-fx-font-size: 14;");

            // Action buttons
            HBox actionBox = new HBox(10);
            Button showButton = new Button();
            Button deleteRowButton = new Button();

            // Show button
            showButton.getStyleClass().addAll("action-btn", "submit-btn");
            SVGPath showIcon = new SVGPath();
            showIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
            showIcon.getStyleClass().add("icon");
            showButton.setGraphic(showIcon);
            showButton.setTooltip(new Tooltip("Show Registration"));
            showButton.setOnAction(e -> navigateToShow(registration));

            // Delete button
            deleteRowButton.getStyleClass().addAll("action-btn", "round");
            SVGPath deleteIcon = new SVGPath();
            deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
            deleteIcon.getStyleClass().add("icon");
            deleteRowButton.setGraphic(deleteIcon);
            deleteRowButton.setTooltip(new Tooltip("Delete Registration"));
            deleteRowButton.setOnAction(e -> delete(registration));

            actionBox.getChildren().addAll(showButton, deleteRowButton);

            card.getChildren().addAll( statusLabel, registrationDateLabel, eventTitleLabel, actionBox);

            // Add to grid
            registrationsGrid.add(card, column, row);
            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    private void delete(EventRegistration registration) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to delete this registration?");
        confirmAlert.setContentText("This registration will be deleted.\nAre you sure you want to continue?");
        confirmAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceEventRegistration.delete(registration);
                    registrationsList.remove(registration);
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
            float[] columnWidths = {150, 150, 100};
            Table table = new Table(UnitValue.createPointArray(columnWidths))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(20);

            // Header cells
            String[] headers = {"Event Title", "Registration Date", "Status"};
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
                        .add(new Paragraph(registration.getRegistrationDate() != null ? registration.getRegistrationDate().format(dateFormatter) : "N/A")
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