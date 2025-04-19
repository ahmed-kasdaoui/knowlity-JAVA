package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEvents;

import java.io.IOException;

public class EventListingController {

    @FXML
    private BorderPane root;
    @FXML
    private TextField searchQuery;
    @FXML
    private ComboBox<String> searchCategory;
    @FXML
    private TextField searchLocation;
    @FXML
    private Button showNormal;
    @FXML
    private Button showRecommended;
    @FXML
    private GridPane eventGrid;
    @FXML
    private Button moreButton;

    private ObservableList<Events> events = FXCollections.observableArrayList();
    private ObservableList<Events> recommendedEvents = FXCollections.observableArrayList();
    private ObservableList<Events> currentDisplayList = FXCollections.observableArrayList();
    private ServiceEvents serviceEvents = new ServiceEvents();
    private int displayedCount = 0;
    private final int BATCH_SIZE = 4;
    private final int COLUMNS_PER_ROW = 4;

    public void initialize() {
        // Fetch all events from the database
        try {
            events.addAll(serviceEvents.getAll());
            System.out.println("Fetched " + events.size() + " events from the database.");
        } catch (Exception e) {
            System.err.println("Error fetching events: " + e.getMessage());
            e.printStackTrace();
        }

        // For demonstration, assume recommended events are those with a specific category
        recommendedEvents.addAll(events.filtered(e -> e.getCategory() != null && e.getCategory().equalsIgnoreCase("Hackathon")));
        System.out.println("Found " + recommendedEvents.size() + " recommended events (Hackathons).");

        // Initialize with normal events
        currentDisplayList = events;
        System.out.println("Current display list size: " + currentDisplayList.size());
        displayEventsBatch(true);

        // Set up event handlers
        searchQuery.textProperty().addListener((obs, oldV, newV) -> filterEvents());
        searchCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> filterEvents());
        searchLocation.textProperty().addListener((obs, oldV, newV) -> filterEvents());

        showNormal.setOnAction(e -> {
            currentDisplayList = events;
            displayedCount = 0;
            displayEventsBatch(true);
            showNormal.setStyle("-fx-background-color: #007bff");
            showRecommended.setStyle("-fx-background-color: #6c757d");
        });

        showRecommended.setOnAction(e -> {
            currentDisplayList = recommendedEvents;
            displayedCount = 0;
            displayEventsBatch(true);
            showRecommended.setStyle("-fx-background-color: #007bff");
            showNormal.setStyle("-fx-background-color: #6c757d");
        });

        moreButton.setOnAction(e -> loadMoreEvents());
    }

    private void filterEvents() {
        String query = searchQuery.getText().toLowerCase().trim();
        String category = searchCategory.getValue() != null ? searchCategory.getValue().toLowerCase() : "";
        String location = searchLocation.getText().toLowerCase().trim();

        ObservableList<Events> filtered = events.filtered(e ->
                (query.isEmpty() || (e.getTitle() != null && e.getTitle().toLowerCase().contains(query))) &&
                        (category.isEmpty() || (e.getCategory() != null && e.getCategory().toLowerCase().equals(category))) &&
                        (location.isEmpty() || (e.getLocation() != null && e.getLocation().toLowerCase().contains(location)))
        );

        currentDisplayList = filtered;
        displayedCount = 0;
        displayEventsBatch(true);
    }

    private void displayEventsBatch(boolean clearList) {
        if (clearList) {
            eventGrid.getChildren().clear();
            displayedCount = 0;
        }

        int startIndex = displayedCount;
        int endIndex = Math.min(startIndex + BATCH_SIZE, currentDisplayList.size());

        if (currentDisplayList.isEmpty()) {
            Label noEventsLabel = new Label("No events found.");
            noEventsLabel.getStyleClass().add("no-events-label");
            eventGrid.add(noEventsLabel, 0, 0);
        } else {
            for (int i = startIndex; i < endIndex; i++) {
                Events event = currentDisplayList.get(i);
                VBox card = createEventCard(event);
                int row = i / COLUMNS_PER_ROW;
                int col = i % COLUMNS_PER_ROW;
                eventGrid.add(card, col, row);
            }
        }

        displayedCount = endIndex;
        moreButton.setVisible(displayedCount < currentDisplayList.size());
    }

    private void loadMoreEvents() {
        displayEventsBatch(false);
    }

    private VBox createEventCard(Events event) {
        VBox card = new VBox(5);
        card.getStyleClass().add("event-card");

        // Load image, fallback to placeholder if null or invalid
        Image img;
        String imagePath = event.getImage() != null ? event.getImage().trim() : "/images/placeholder.png";
        if (!imagePath.startsWith("/images/") && !imagePath.equals("/images/placeholder.png")) {
            imagePath = "/images/" + imagePath;
            System.out.println("Adjusted image path to: " + imagePath + " for event: " + event.getTitle());
        }
        try {
            System.out.println("Attempting to load image: " + imagePath + " for event: " + event.getTitle());
            java.io.InputStream stream = getClass().getResourceAsStream(imagePath);
            if (stream == null) {
                System.err.println("Image not found: " + imagePath);
                imagePath = "/images/placeholder.png";
                stream = getClass().getResourceAsStream(imagePath);
                if (stream == null) {
                    System.err.println("Placeholder image not found: " + imagePath);
                    img = new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=");
                } else {
                    img = new Image(stream);
                }
            } else {
                img = new Image(stream);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath + " for event: " + event.getTitle() + ". Error: " + e.getMessage());
            img = new Image(getClass().getResourceAsStream("/images/placeholder.png"));
        }

        ImageView imageView = new ImageView(img);
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        Label title = new Label(event.getTitle() != null ? event.getTitle() : "Untitled Event");
        title.getStyleClass().add("event-title");

        Label date = new Label(event.getStartDate() != null ? event.getStartDate().toString() : "No Date");
        date.getStyleClass().add("event-date");

        Label category = new Label("Category: " + (event.getCategory() != null ? event.getCategory() : "None"));
        category.getStyleClass().add("event-category");

        Label description = new Label(event.getDescription() != null ? event.getDescription() : "No Description");
        description.getStyleClass().add("event-description");
        description.setWrapText(true);

        Button reserve = new Button("Reserve");
        reserve.getStyleClass().add("reserve-button");
        reserve.setOnAction(e -> handleReserve(event));

        card.getChildren().addAll(imageView, title, date, category, description, reserve);
        return card;
    }

    private void handleReserve(Events event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventDetails.fxml"));
            showNormal.getScene().setRoot(loader.load());
            EventDetailsController controller = loader.getController();
            if (controller == null) {
                throw new IllegalStateException("Controller for EventDetails.fxml is null. Check fx:controller attribute.");
            }
            controller.setEventId(event.getId());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load event details form: " + e.getMessage());
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