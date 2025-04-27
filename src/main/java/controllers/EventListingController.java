package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEvents;
import tn.esprit.services.ServiceUserEventPreference;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
    private VBox eventContainer;
    @FXML
    private Button moreButton;

    private ObservableList<Events> events = FXCollections.observableArrayList();
    private ObservableList<Events> recommendedEvents = FXCollections.observableArrayList();
    private ObservableList<Events> currentDisplayList = FXCollections.observableArrayList();
    private ServiceUserEventPreference userEventPreferenceService = new ServiceUserEventPreference();
    private ServiceEvents serviceEvents = new ServiceEvents();
    private int displayedCount = 0;
    private final int BATCH_SIZE = 4;

    // Color themes for cards
    private final List<String> cardThemes = Arrays.asList("blue", "red", "green", "yellow");

    public void initialize() {
        serviceEvents.checkEvents();
        eventContainer.getStyleClass().add("light");

        root.getStyleClass().add("light-theme");

        try {
            events.addAll(serviceEvents.getAll());
            System.out.println("Fetched " + events.size() + " events from the database.");
        } catch (Exception e) {
            System.err.println("Error fetching events: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            recommendedEvents.addAll(userEventPreferenceService.getRecommendedEvents(1, 20));
            System.out.println("Found " + recommendedEvents.size() + " recommended events for user ID: " + 1);
            if (recommendedEvents.isEmpty()) {
                recommendedEvents.addAll(events);
                System.out.println("No recommended events found, falling back to all events.");
            }
        } catch (Exception e) {
            System.err.println("Error fetching recommended events: " + e.getMessage());
            e.printStackTrace();
            recommendedEvents.addAll(events);
        }

        currentDisplayList = events;
        System.out.println("Current display list size: " + currentDisplayList.size());
        displayEventsBatch(true);

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
            eventContainer.getChildren().clear();
            displayedCount = 0;
        }

        int startIndex = displayedCount;
        int endIndex = Math.min(startIndex + BATCH_SIZE, currentDisplayList.size());

        if (currentDisplayList.isEmpty()) {
            Label noEventsLabel = new Label("No events found.");
            noEventsLabel.getStyleClass().add("no-events-label");
            eventContainer.getChildren().add(noEventsLabel);
        } else {
            for (int i = startIndex; i < endIndex; i++) {
                Events event = currentDisplayList.get(i);

                // Alternate the card themes
                String theme = cardThemes.get(i % cardThemes.size());

                // Create a postcard with alternating directions based on even/odd index
                HBox card = createPostcard(event, theme, i % 2 == 0);
                eventContainer.getChildren().add(card);
            }
        }

        displayedCount = endIndex;
        moreButton.setVisible(displayedCount < currentDisplayList.size());
    }

    private void loadMoreEvents() {
        displayEventsBatch(false);
    }

    private HBox createPostcard(Events event, String colorTheme, boolean isLeftToRight) {
        HBox postcard = new HBox();
        postcard.getStyleClass().addAll("postcard", "light", colorTheme);
        postcard.getStyleClass().add("postcard-bg-" + colorTheme);
        // Create image section
        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("postcard__img");

        // Load image
        String imagePath = event.getImage() != null ? event.getImage().trim() : "/images/placeholder.png";
        if (!imagePath.startsWith("/images/") && !imagePath.equals("/images/placeholder.png")) {
            imagePath = "/images/" + imagePath;
        }

        try {
            java.io.InputStream stream = getClass().getResourceAsStream(imagePath);
            if (stream == null) {
                imagePath = "/images/placeholder.png";
                stream = getClass().getResourceAsStream(imagePath);
                if (stream == null) {
                    imageView.setImage(new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="));
                } else {
                    imageView.setImage(new Image(stream));
                }
            } else {
                imageView.setImage(new Image(stream));
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
            } catch (Exception ex) {
                imageView.setImage(new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="));
            }
        }

        // Create clickable image container
        StackPane imageContainer = new StackPane(imageView);
        imageContainer.getStyleClass().add("postcard__img_link");

        // Create content section
        VBox textContent = new VBox(10);
        textContent.getStyleClass().addAll("postcard__text", "t-dark");
        textContent.setPadding(new Insets(30, 35, 30, 35));
        HBox.setHgrow(textContent, Priority.ALWAYS);

        // Event Title
        Hyperlink title = new Hyperlink(event.getTitle() != null ? event.getTitle() : "Untitled Event");
        title.getStyleClass().addAll("postcard__title", colorTheme);
        title.setWrapText(true);
        title.setOnAction(e -> handleReserve(event));

        // Event Date with icon
        HBox dateBox = new HBox(5);
        dateBox.getStyleClass().add("postcard__subtitle");
        dateBox.setAlignment(Pos.CENTER_LEFT);

        Label calendarIcon = new Label("\uf073"); // Font Awesome calendar icon
        calendarIcon.getStyleClass().add("fas");

        Label dateText = new Label(event.getStartDate() != null ?
                event.getStartDate().toString() : "Fri, Mar 14th 2025");
        dateText.getStyleClass().add("small");

        dateBox.getChildren().addAll(calendarIcon, dateText);

        // Decorative bar
        Region bar = new Region();
        bar.getStyleClass().add("postcard__bar");
        bar.setPrefSize(50, 10);

        // Event Description
        Label description = new Label(event.getDescription() != null ?
                event.getDescription() :
                "We are proud to announce that we are organizing a 2-day hybrid workshop on Explainable AI (XAI), bringing together students, experts, researchers, and professionals to explore the latest advancements in making AI systems more transparent, interpretable, a");
        description.getStyleClass().add("postcard__preview-txt");
        description.setWrapText(true);
        description.setTextAlignment(TextAlignment.JUSTIFY);

        // Tag items
        HBox tagBox = new HBox(10);
        tagBox.getStyleClass().add("postcard__tagbox");
        tagBox.setAlignment(Pos.CENTER_LEFT);

        // Category tag
        Label categoryTag = new Label(event.getCategory() != null ?
                event.getCategory() : "Conference");
        categoryTag.getStyleClass().add("tag__item");

        // Free tag
        Label freeTag = new Label("FREE");
        freeTag.getStyleClass().add("tag__item");

        // Reserve button
        Hyperlink reserveButton = new Hyperlink("Reserve");
        reserveButton.getStyleClass().addAll("tag__item", "play", colorTheme);
        reserveButton.setOnAction(e -> handleReserve(event));

        tagBox.getChildren().addAll(categoryTag, freeTag, reserveButton);

        // Add all elements to the text content
        textContent.getChildren().addAll(title, dateBox, bar, description, tagBox);

        // Add components to postcard based on direction
        if (isLeftToRight) {
            postcard.getChildren().addAll(imageContainer, textContent);
        } else {
            postcard.getChildren().addAll(textContent, imageContainer);
        }

        return postcard;
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
