package controllers;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEvents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventFormController {

    @FXML
    private TextField titleField;
    @FXML
    private Label titleLabel;
    @FXML
    private Label titleErrorLabel;
    @FXML
    private TextArea descriptionField;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label descriptionErrorLabel;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private Label startDateLabel;
    @FXML
    private Label startDateErrorLabel;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Label endDateLabel;
    @FXML
    private Label endDateErrorLabel;
    @FXML
    private ComboBox<String> typeCombo;
    @FXML
    private Label typeLabel;
    @FXML
    private Label typeErrorLabel;
    @FXML
    private TextField maxParticipantsField;
    @FXML
    private Label maxParticipantsLabel;
    @FXML
    private Label maxParticipantsErrorLabel;
    @FXML
    private TextField seatsAvailableField;
    @FXML
    private Label seatsAvailableLabel;
    @FXML
    private Label seatsAvailableErrorLabel;
    @FXML
    private TextField locationField;
    @FXML
    private Label locationLabel;
    @FXML
    private Label locationErrorLabel;
    @FXML
    private ComboBox<String> categoryCombo;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label categoryErrorLabel;
    @FXML
    private TextField latitudeField;
    @FXML
    private Label latitudeLabel;
    @FXML
    private Label latitudeErrorLabel;
    @FXML
    private TextField longitudeField;
    @FXML
    private Label longitudeLabel;
    @FXML
    private Label longitudeErrorLabel;
    @FXML
    private Button imageButton;
    @FXML
    private Label imageLabel;
    @FXML
    private Label imageErrorLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button resetButton;

    private final ServiceEvents serviceEvents;
    private String imagePath;
    private File selectedImageFile; // Added to store the full file path temporarily

    public EventFormController() {
        this.serviceEvents = new ServiceEvents();
    }

    public void initialize() {
        // Populate ComboBoxes
        typeCombo.getItems().addAll("On-Ligne", "On-Site");
        categoryCombo.getItems().addAll("Workshop", "Hackathon", "Sports", "Networking", "Cultural");

        // Bind floating labels
        bindFloatingLabel(titleField, titleLabel);
        bindFloatingLabel(descriptionField, descriptionLabel);
        bindFloatingLabel(maxParticipantsField, maxParticipantsLabel);
        bindFloatingLabel(seatsAvailableField, seatsAvailableLabel);
        bindFloatingLabel(locationField, locationLabel);
        bindFloatingLabel(latitudeField, latitudeLabel);
        bindFloatingLabel(longitudeField, longitudeLabel);
        bindFloatingLabel(startDatePicker, startDateLabel);
        bindFloatingLabel(endDatePicker, endDateLabel);
        bindFloatingLabel(typeCombo, typeLabel);
        bindFloatingLabel(categoryCombo, categoryLabel);

        // Real-time validation
        titleField.textProperty().addListener((obs, old, newValue) -> validateTitle());
        descriptionField.textProperty().addListener((obs, old, newValue) -> validateDescription());
        startDatePicker.valueProperty().addListener((obs, old, newValue) -> validateStartDate());
        endDatePicker.valueProperty().addListener((obs, old, newValue) -> validateEndDate());
        typeCombo.valueProperty().addListener((obs, old, newValue) -> validateType());
        maxParticipantsField.textProperty().addListener((obs, old, newValue) -> validateMaxParticipants());
        seatsAvailableField.textProperty().addListener((obs, old, newValue) -> validateSeatsAvailable());
        locationField.textProperty().addListener((obs, old, newValue) -> validateLocation());
        categoryCombo.valueProperty().addListener((obs, old, newValue) -> validateCategory());
        latitudeField.textProperty().addListener((obs, old, newValue) -> validateLatitude());
        longitudeField.textProperty().addListener((obs, old, newValue) -> validateLongitude());

        // Button actions
        imageButton.setOnAction(e -> chooseImage());
        saveButton.setOnAction(e -> saveEvent());
        resetButton.setOnAction(e -> resetForm());
    }

    private void bindFloatingLabel(TextField field, Label label) {
        label.styleProperty().bind(
                Bindings.when(field.textProperty().isNotEmpty().or(field.focusedProperty()))
                        .then("-fx-style-class: floating;")
                        .otherwise("")
        );
    }

    private void bindFloatingLabel(TextArea field, Label label) {
        label.styleProperty().bind(
                Bindings.when(field.textProperty().isNotEmpty().or(field.focusedProperty()))
                        .then("-fx-style-class: floating;")
                        .otherwise("")
        );
    }

    private void bindFloatingLabel(DatePicker field, Label label) {
        label.styleProperty().bind(
                Bindings.when(field.valueProperty().isNotNull().or(field.focusedProperty()))
                        .then("-fx-style-class: floating;")
                        .otherwise("")
        );
    }

    private void bindFloatingLabel(ComboBox<?> field, Label label) {
        label.styleProperty().bind(
                Bindings.when(field.valueProperty().isNotNull().or(field.focusedProperty()))
                        .then("-fx-style-class: floating;")
                        .otherwise("")
        );
    }

    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(imageButton.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file; // Store the full file path
            imageLabel.setText(file.getName());
            validateImage();
        }
    }

    private boolean validateTitle() {
        if (titleField.getText().isEmpty()) {
            titleErrorLabel.setText("Title is required");
            titleErrorLabel.setVisible(true);
            titleErrorLabel.setManaged(true);
            return false;
        }
        titleErrorLabel.setVisible(false);
        titleErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateDescription() {
        String text = descriptionField.getText();
        if (text.isEmpty()) {
            descriptionErrorLabel.setText("Description is required");
            descriptionErrorLabel.setVisible(true);
            descriptionErrorLabel.setManaged(true);
            return false;
        }
        if (text.length() < 10) {
            descriptionErrorLabel.setText("At least 10 characters required");
            descriptionErrorLabel.setVisible(true);
            descriptionErrorLabel.setManaged(true);
            return false;
        }
        descriptionErrorLabel.setVisible(false);
        descriptionErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateStartDate() {
        LocalDate value = startDatePicker.getValue();
        if (value == null) {
            startDateErrorLabel.setText("Start date is required");
            startDateErrorLabel.setVisible(true);
            startDateErrorLabel.setManaged(true);
            return false;
        }
        if (value.isBefore(LocalDate.now())) {
            startDateErrorLabel.setText("Must be today or later");
            startDateErrorLabel.setVisible(true);
            startDateErrorLabel.setManaged(true);
            return false;
        }
        startDateErrorLabel.setVisible(false);
        startDateErrorLabel.setManaged(false);
        validateEndDate();
        return true;
    }

    private boolean validateEndDate() {
        LocalDate end = endDatePicker.getValue();
        LocalDate start = startDatePicker.getValue();
        if (end == null) {
            endDateErrorLabel.setText("End date is required");
            endDateErrorLabel.setVisible(true);
            endDateErrorLabel.setManaged(true);
            return false;
        }
        if (start != null && end.isBefore(start)) {
            endDateErrorLabel.setText("Must be after start date");
            endDateErrorLabel.setVisible(true);
            endDateErrorLabel.setManaged(true);
            return false;
        }
        endDateErrorLabel.setVisible(false);
        endDateErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateType() {
        if (typeCombo.getValue() == null) {
            typeErrorLabel.setText("Type is required");
            typeErrorLabel.setVisible(true);
            typeErrorLabel.setManaged(true);
            return false;
        }
        typeErrorLabel.setVisible(false);
        typeErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateMaxParticipants() {
        String text = maxParticipantsField.getText();
        if (text.isEmpty()) {
            maxParticipantsErrorLabel.setText("Max participants is required");
            maxParticipantsErrorLabel.setVisible(true);
            maxParticipantsErrorLabel.setManaged(true);
            return false;
        }
        try {
            int value = Integer.parseInt(text);
            if (value <= 0) {
                maxParticipantsErrorLabel.setText("Must be positive");
                maxParticipantsErrorLabel.setVisible(true);
                maxParticipantsErrorLabel.setManaged(true);
                return false;
            }
            maxParticipantsErrorLabel.setVisible(false);
            maxParticipantsErrorLabel.setManaged(false);
            validateSeatsAvailable(); // Re-validate seats
            return true;
        } catch (NumberFormatException e) {
            maxParticipantsErrorLabel.setText("Must be a number");
            maxParticipantsErrorLabel.setVisible(true);
            maxParticipantsErrorLabel.setManaged(true);
            return false;
        }
    }

    private boolean validateSeatsAvailable() {
        String text = seatsAvailableField.getText();
        String maxText = maxParticipantsField.getText();
        if (text.isEmpty()) {
            seatsAvailableErrorLabel.setText("Seats available is required");
            seatsAvailableErrorLabel.setVisible(true);
            seatsAvailableErrorLabel.setManaged(true);
            return false;
        }
        try {
            int value = Integer.parseInt(text);
            if (value <= 0) {
                seatsAvailableErrorLabel.setText("Must be positive");
                seatsAvailableErrorLabel.setVisible(true);
                seatsAvailableErrorLabel.setManaged(true);
                return false;
            }
            if (!maxText.isEmpty()) {
                try {
                    int max = Integer.parseInt(maxText);
                    if (value > max) {
                        seatsAvailableErrorLabel.setText("Cannot exceed max participants");
                        seatsAvailableErrorLabel.setVisible(true);
                        seatsAvailableErrorLabel.setManaged(true);
                        return false;
                    }
                } catch (NumberFormatException ignored) {
                    // Max participants invalid; skip this check
                }
            }
            seatsAvailableErrorLabel.setVisible(false);
            seatsAvailableErrorLabel.setManaged(false);
            return true;
        } catch (NumberFormatException e) {
            seatsAvailableErrorLabel.setText("Must be a number");
            seatsAvailableErrorLabel.setVisible(true);
            seatsAvailableErrorLabel.setManaged(true);
            return false;
        }
    }

    private boolean validateLocation() {
        if (locationField.getText().isEmpty()) {
            locationErrorLabel.setText("Location is required");
            locationErrorLabel.setVisible(true);
            locationErrorLabel.setManaged(true);
            return false;
        }
        locationErrorLabel.setVisible(false);
        locationErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateCategory() {
        if (categoryCombo.getValue() == null) {
            categoryErrorLabel.setText("Category is required");
            categoryErrorLabel.setVisible(true);
            categoryErrorLabel.setManaged(true);
            return false;
        }
        categoryErrorLabel.setVisible(false);
        categoryErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateLatitude() {
        String text = latitudeField.getText();
        if (text.isEmpty()) {
            latitudeErrorLabel.setText("Latitude is required");
            latitudeErrorLabel.setVisible(true);
            latitudeErrorLabel.setManaged(true);
            return false;
        }
        try {
            float value = Float.parseFloat(text);
            if (value < -90 || value > 90) {
                latitudeErrorLabel.setText("Must be between -90 and 90");
                latitudeErrorLabel.setVisible(true);
                latitudeErrorLabel.setManaged(true);
                return false;
            }
            latitudeErrorLabel.setVisible(false);
            latitudeErrorLabel.setManaged(false);
            return true;
        } catch (NumberFormatException e) {
            latitudeErrorLabel.setText("Must be a number");
            latitudeErrorLabel.setVisible(true);
            latitudeErrorLabel.setManaged(true);
            return false;
        }
    }

    private boolean validateLongitude() {
        String text = longitudeField.getText();
        if (text.isEmpty()) {
            longitudeErrorLabel.setText("Longitude is required");
            longitudeErrorLabel.setVisible(true);
            longitudeErrorLabel.setManaged(true);
            return false;
        }
        try {
            float value = Float.parseFloat(text);
            if (value < -180 || value > 180) {
                longitudeErrorLabel.setText("Must be between -180 and 180");
                longitudeErrorLabel.setVisible(true);
                longitudeErrorLabel.setManaged(true);
                return false;
            }
            longitudeErrorLabel.setVisible(false);
            longitudeErrorLabel.setManaged(false);
            return true;
        } catch (NumberFormatException e) {
            longitudeErrorLabel.setText("Must be a number");
            longitudeErrorLabel.setVisible(true);
            longitudeErrorLabel.setManaged(true);
            return false;
        }
    }

    private boolean validateImage() {
        if (selectedImageFile == null) { // Updated to check selectedImageFile
            imageErrorLabel.setText("Image is required");
            imageErrorLabel.setVisible(true);
            imageErrorLabel.setManaged(true);
            return false;
        }
        imageErrorLabel.setVisible(false);
        imageErrorLabel.setManaged(false);
        return true;
    }

    private boolean validateAll() {
        return validateTitle() &&
                validateDescription() &&
                validateStartDate() &&
                validateEndDate() &&
                validateType() &&
                validateMaxParticipants() &&
                validateSeatsAvailable() &&
                validateLocation() &&
                validateCategory() &&
                validateLatitude() &&
                validateLongitude() &&
                validateImage();
    }

    private void saveEvent() {
        if (!validateAll()) {
            return;
        }

        try {
            // Copy the image to the /images directory
            String imagesDirPath = "src/main/resources/images"; // Adjust path as needed
            File imagesDir = new File(imagesDirPath);
            if (!imagesDir.exists()) {
                imagesDir.mkdirs(); // Create the directory if it doesn't exist
            }

            // Generate a unique filename to avoid overwriting
            String originalFileName = selectedImageFile.getName();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
            String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;
            Path destinationPath = Paths.get(imagesDirPath, uniqueFileName);

            // Copy the file to the /images directory
            Files.copy(selectedImageFile.toPath(), destinationPath);

            // Update imagePath to the new filename (relative path or just the filename)
            imagePath = uniqueFileName;

            // Create Events object
            Events event = new Events();
            event.setTitle(titleField.getText());
            event.setDescription(descriptionField.getText());
            event.setStartDate(LocalDateTime.of(startDatePicker.getValue(), LocalTime.of(0, 0)));
            event.setEndDate(LocalDateTime.of(endDatePicker.getValue(), LocalTime.of(23, 59)));
            event.setType(typeCombo.getValue());
            event.setMaxParticipants(Integer.parseInt(maxParticipantsField.getText()));
            event.setSeatsAvailable(Integer.parseInt(seatsAvailableField.getText()));
            event.setLocation(locationField.getText());
            event.setCategory(categoryCombo.getValue());
            event.setLatitude(Float.parseFloat(latitudeField.getText()));
            event.setLongitude(Float.parseFloat(longitudeField.getText()));
            event.setImage(imagePath);
            event.setOrganizerId(1); // Default; replace with user context

            // Save event
            serviceEvents.add(event);
            imageLabel.setText("Event saved successfully");
            imageLabel.setStyle("-fx-text-fill: #5543ca;");
            resetForm();
        } catch (IOException e) {
            imageLabel.setText("Error saving image: " + e.getMessage());
            imageLabel.setStyle("-fx-text-fill: #f4524d;");
            e.printStackTrace();
        } catch (Exception e) {
            imageLabel.setText("Error saving event: " + e.getMessage());
            imageLabel.setStyle("-fx-text-fill: #f4524d;");
            e.printStackTrace();
        }
    }

    private void resetForm() {
        titleField.clear();
        descriptionField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        typeCombo.setValue(null);
        maxParticipantsField.clear();
        seatsAvailableField.clear();
        locationField.clear();
        categoryCombo.setValue(null);
        latitudeField.clear();
        longitudeField.clear();
        imagePath = null;
        selectedImageFile = null; // Reset the selected file
        imageLabel.setText("No file chosen");
        imageLabel.setStyle("-fx-text-fill: #5543ca;");

        // Clear error labels
        titleErrorLabel.setVisible(false);
        titleErrorLabel.setManaged(false);
        descriptionErrorLabel.setVisible(false);
        descriptionErrorLabel.setManaged(false);
        startDateErrorLabel.setVisible(false);
        startDateErrorLabel.setManaged(false);
        endDateErrorLabel.setVisible(false);
        endDateErrorLabel.setManaged(false);
        typeErrorLabel.setVisible(false);
        typeErrorLabel.setManaged(false);
        maxParticipantsErrorLabel.setVisible(false);
        maxParticipantsErrorLabel.setManaged(false);
        seatsAvailableErrorLabel.setVisible(false);
        seatsAvailableErrorLabel.setManaged(false);
        locationErrorLabel.setVisible(false);
        locationErrorLabel.setManaged(false);
        categoryErrorLabel.setVisible(false);
        categoryErrorLabel.setManaged(false);
        latitudeErrorLabel.setVisible(false);
        latitudeErrorLabel.setManaged(false);
        longitudeErrorLabel.setVisible(false);
        longitudeErrorLabel.setManaged(false);
        imageErrorLabel.setVisible(false);
        imageErrorLabel.setManaged(false);
    }
}