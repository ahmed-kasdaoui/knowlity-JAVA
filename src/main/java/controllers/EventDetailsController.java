package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEvents;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

public class EventDetailsController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label dateLocationLabel;
    @FXML
    private Text descriptionText;
    @FXML
    private Label LocationLabel;
    @FXML
    private Label maxParticipantsLabel;
    @FXML
    private Label seatsAvailableLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Label timerLabel;
    @FXML
    private Label userNameLabel;
    @FXML
    private Button reserveButton;
    @FXML
    private Button backButton; // New field for the back button
    @FXML
    private ImageView organizerImage;
    @FXML
    private ImageView eventImage;
    @FXML
    private WebView mapView;

    private Timeline timer;
    private final ServiceEvents serviceEvents;
    private int eventId;

    public EventDetailsController() {
        this.serviceEvents = new ServiceEvents();
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
        loadEventData();
    }

    public void initialize() {
        reserveButton.setOnAction(e -> handleReserve());
        backButton.setOnAction(e -> handleBack());
        mapView.setVisible(true);
    }

    private void loadEventData() {
        if (eventId == 0) {
            System.err.println("Event ID not set. Please ensure setEventId() is called with a valid ID.");
            return;
        }

        Events event = null;
        try {
            event = serviceEvents.getById(eventId);
        } catch (Exception e) {
            System.err.println("Error fetching event ID " + eventId + ": " + e.getMessage());
            e.printStackTrace();
        }
        if (event == null) {
            System.err.println("Event not found for ID: " + eventId);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

        titleLabel.setText(event.getTitle());
        categoryLabel.setText("Category: Event");
        dateLocationLabel.setText(event.getStartDate() != null ? event.getStartDate().format(formatter) + " | " + event.getLocation() : "TBD | " + event.getLocation());
        descriptionText.setText(event.getDescription()); // Updated
        LocationLabel.setText(event.getLocation());
        maxParticipantsLabel.setText(event.getMaxParticipants() != null ? event.getMaxParticipants() + "+ Seats" : "N/A");
        seatsAvailableLabel.setText(event.getSeatsAvailable() != null ? event.getSeatsAvailable() + " Tickets" : "N/A");
        durationLabel.setText(calculateDuration(event.getStartDate(), event.getEndDate()));
        userNameLabel.setText("Organizer Name");
        loadImage(eventImage, event.getImage());
        loadImage(organizerImage, null);

        LocalDateTime startDate = event.getStartDate();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer(startDate)));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();

        // TODO: Load map
        // mapView.getEngine().load("https://maps.google.com/maps?q=" + URLEncoder.encode(event.getLocation(), "UTF-8") + "&t=&z=13&ie=UTF8&iwloc=&output=embed");
    }

    private String calculateDuration(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return "N/A";
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return days > 1 ? days + " Days Event" : "1 Day Event";
    }

    private void updateTimer(LocalDateTime startDate) {
        if (startDate == null) {
            timerLabel.setText("Countdown: N/A");
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(now, startDate);
        long hours = ChronoUnit.HOURS.between(now, startDate) % 24;
        long minutes = ChronoUnit.MINUTES.between(now, startDate) % 60;
        long seconds = ChronoUnit.SECONDS.between(now, startDate) % 60;
        timerLabel.setText(String.format("Countdown: %d days, %d:%d:%d", days, hours, minutes, seconds));
    }

    private void handleReserve() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NewEventRegistrationForm.fxml"));
            reserveButton.getScene().setRoot(loader.load());
            NewEventRegistrationFormController controller = loader.getController();
            controller.setEvent(serviceEvents.getById(eventId));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load new registration form: " + e.getMessage());
        }
    }

    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventListing.fxml"));
            reserveButton.getScene().setRoot(loader.load());
        } catch (IOException e) {
            System.err.println("Error navigating back to EventListing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadImage(ImageView imageView, String imagePath) {
        String path = imagePath != null && !imagePath.isEmpty() ? imagePath.trim() : "/images/placeholder.png";
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