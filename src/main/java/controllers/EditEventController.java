package controllers;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEvents;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EditEventController {

    public Button backToEventsButton;
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
    private Button imageButton;
    @FXML
    private Label imageLabel;
    @FXML
    private Label imageFileLabel;
    @FXML
    private Label imageErrorLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button resetButton;

    private final ServiceEvents serviceEvents;
    private String imagePath;
    private File selectedImageFile; // Added to store the full file path
    private Events currentEvent;

    public EditEventController() {
        this.serviceEvents = new ServiceEvents();
    }

    @FXML
    public void initialize() {
        // Populate ComboBoxes
        typeCombo.getItems().addAll("On-Ligne", "On-Site");
        categoryCombo.getItems().addAll("Workshop", "Hackathon", "Sports", "Networking", "Cultural");

        // Bind floating labels
        bindFloatingLabel(titleField, titleLabel);
        bindFloatingLabel(descriptionField, descriptionLabel);
        bindFloatingLabel(maxParticipantsField, maxParticipantsLabel);
        bindFloatingLabel(locationField, locationLabel);
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
        locationField.textProperty().addListener((obs, old, newValue) -> validateLocation());
        categoryCombo.valueProperty().addListener((obs, old, newValue) -> validateCategory());

        // Button actions
        imageButton.setOnAction(e -> chooseImage());
        saveButton.setOnAction(e -> saveEventAction());
        resetButton.setOnAction(e -> resetForm());
        backToEventsButton.setOnAction(event -> navigateToEvents());
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
            imagePath = file.getAbsolutePath(); // Store the full path in imagePath
            imageFileLabel.setText(file.getName()); // Display only the filename
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
            return true;
        } catch (NumberFormatException e) {
            maxParticipantsErrorLabel.setText("Must be a number");
            maxParticipantsErrorLabel.setVisible(true);
            maxParticipantsErrorLabel.setManaged(true);
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

    private boolean validateImage() {
        if (selectedImageFile == null && (currentEvent == null || currentEvent.getImage() == null)) {
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
                validateLocation() &&
                validateCategory() &&
                validateImage();
    }

    private void saveEventAction() {
        if (!validateAll()) {
            return;
        }

        try {
            Events event = currentEvent != null ? currentEvent : new Events();
            event.setTitle(titleField.getText());
            event.setDescription(descriptionField.getText());
            event.setStartDate(LocalDateTime.of(startDatePicker.getValue(), LocalTime.of(0, 0)));
            event.setEndDate(LocalDateTime.of(endDatePicker.getValue(), LocalTime.of(23, 59)));
            event.setType(typeCombo.getValue());
            int maxParticipants = Integer.parseInt(maxParticipantsField.getText());
            event.setMaxParticipants(maxParticipants);
            event.setSeatsAvailable(maxParticipants); // Initialize seatsAvailable
            event.setLocation(locationField.getText());
            event.setCategory(categoryCombo.getValue());
            event.setOrganizerId(1); // Replace with user context
            event.setLongitude(30F); // Add UI field for longitude
            event.setLatitude(30F);

            // Handle image saving
            String savedImagePath = imagePath;
            if (selectedImageFile != null) { // Only copy if a new image was selected
                // Define the target directory as /images in the working directory
                String targetDir = "src/main/resources/images";;
                File directory = new File(targetDir);
                if (!directory.exists()) {
                    directory.mkdirs(); // Create the directory if it doesn't exist
                }

                // Get the original file
                File sourceFile = selectedImageFile;
                String fileName = sourceFile.getName();
                // Create a unique file name to avoid overwriting
                String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                File targetFile = new File(targetDir, uniqueFileName);

                // Copy the file to the target directory
                java.nio.file.Files.copy(
                        sourceFile.toPath(),
                        targetFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );

                // Update the imagePath to the new filename (just the filename, not the full path)
                savedImagePath = uniqueFileName;
            } else if (currentEvent != null) {
                // If editing and no new image selected, retain the existing image path
                savedImagePath = currentEvent.getImage();
            }
            event.setImage(savedImagePath);

            if (currentEvent == null) {
                serviceEvents.add(event); // Assuming a create method exists
            } else {
                serviceEvents.update(event);
            }

            imageFileLabel.setText("Event saved successfully");
            imageFileLabel.setStyle("-fx-text-fill: #28a745;");
            resetForm();
        } catch (Exception e) {
            imageFileLabel.setText("Error saving event: " + e.getMessage());
            imageFileLabel.setStyle("-fx-text-fill: #dc3545;");
            e.printStackTrace();
        }
    }

    public void setEvent(Events event) {
        this.currentEvent = event;
        if (event != null) {
            titleField.setText(event.getTitle());
            descriptionField.setText(event.getDescription());
            startDatePicker.setValue(event.getStartDate().toLocalDate());
            endDatePicker.setValue(event.getEndDate().toLocalDate());
            typeCombo.setValue(event.getType());
            maxParticipantsField.setText(String.valueOf(event.getMaxParticipants()));
            locationField.setText(event.getLocation());
            categoryCombo.setValue(event.getCategory());
            imagePath = event.getImage();
            imageFileLabel.setText(imagePath != null ? imagePath : "No file chosen");
            saveButton.setText("Update"); // Set button label to "Update" for edit mode
        } else {
            saveButton.setText("Save"); // Set button label to "Save" for create mode
        }
    }

    private void resetForm() {
        titleField.clear();
        descriptionField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        typeCombo.setValue(null);
        maxParticipantsField.clear();
        locationField.clear();
        categoryCombo.setValue(null);
        imagePath = null;
        selectedImageFile = null; // Clear the selected file
        imageFileLabel.setText("No file chosen");
        imageFileLabel.setStyle("-fx-text-fill: #333333;");
        saveButton.setText("Save");
        currentEvent = null;

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
        locationErrorLabel.setVisible(false);
        locationErrorLabel.setManaged(false);
        categoryErrorLabel.setVisible(false);
        categoryErrorLabel.setManaged(false);
        imageErrorLabel.setVisible(false);
        imageErrorLabel.setManaged(false);
    }

    private void navigateToEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventList.fxml"));
            backToEventsButton.getScene().setRoot(loader.load());
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