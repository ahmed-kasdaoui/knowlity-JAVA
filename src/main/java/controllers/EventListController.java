package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEvents;
import tn.esprit.services.ServiceEventRegistration;
import tn.esprit.models.EventRegistration;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

public class EventListController {

    @FXML
    private TextField searchField;
    @FXML
    private Button Registrationsbtn;
    @FXML
    private Button addButton;
    @FXML
    private GridPane eventsGrid;

    private final ServiceEvents serviceEvents;
    private final ServiceEventRegistration serviceEventRegistration;
    private List<EventRegistration> eventRegistration;
    private ObservableList<Events> eventsList;
    private ObservableList<Events> filteredEventsList;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EventListController() {
        this.serviceEventRegistration = new ServiceEventRegistration();
        this.serviceEvents = new ServiceEvents();
    }

    @FXML
    public void initialize() {
        // Initialize events lists
        eventsList = FXCollections.observableArrayList();
        filteredEventsList = FXCollections.observableArrayList();
        serviceEvents.checkEvents();

        // Load events
        loadEvents();

        // Set up search
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterEvents(newValue));

        // Set up buttons
        Registrationsbtn.setOnAction(event -> navigateToRegistration());
        addButton.setOnAction(event -> navigateToAdd());
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
        eventRegistration=serviceEventRegistration.getByEvent(event.getId());
        int confirmed=0;
        for (EventRegistration registration : eventRegistration){
            if (registration.getStatus().equalsIgnoreCase("confirmed")){
                confirmed++;
            }
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to delete this event?");
        confirmAlert.setContentText("Event: " + event.getTitle() + "\nThis event have "+eventRegistration.size()+" registrations( "+confirmed+" Confirmed) .");
        confirmAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceEvents.delete(event);
                    for (EventRegistration registration : eventRegistration){
                        serviceEventRegistration.delete(registration);
                        if (registration.getStatus().equalsIgnoreCase("confirmed")){
                            sendEmail(registration,event);
                        }
                    }
                    eventsList.remove(event);
                    filterEvents(searchField.getText());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event deleted successfully.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete event: " + e.getMessage());
                }
            }
        });
    }


    private void sendEmail(EventRegistration registration, Events event) {
        final String username = "najd.rahmani1@gmail.com";
        final String password = "mdbr dblk wjjb wfot";
        String mail="najd.rahmani1@gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Create a session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create a new email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail));
            message.setSubject("Subject: Cancellation Notice for " + event.getTitle());

            // Create the email body
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            String emailBody = String.format(
                            "Dear %s,\n\n" +
                            "We regret to inform you that the event '%s' scheduled for %s at %s has been cancelled.\n\n" +
                            "We apologize for any inconvenience this may cause. If you have any questions or need further assistance, please contact our Event Management Team.\n\n" +
                            "Thank you for your understanding.\n\n" +
                            "Best regards,\nEvent Management Team",
                    registration.getName(),
                    event.getTitle(),
                    event.getStartDate() != null ? event.getStartDate().format(dateFormatter) : "N/A",
                    event.getLocation() != null ? event.getLocation() : "N/A"
            );
            messageBodyPart.setText(emailBody);


            // Create a multipart message to combine body and attachment
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Set the multipart content to the message
            message.setContent(multipart);

            // Send the email
            Transport.send(message);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Confirmation email sent to " + "najd.rahmani1@gmail.com");

        } catch (MessagingException e) {
            showAlert(Alert.AlertType.ERROR, "Email Error", "Failed to send email: " + e.getMessage());
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

    @FXML
    private void afficherStats() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/StatsEvents.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Events Statistics");
            stage.setResizable(false);
            stage.setScene(new Scene(root, 600, 500));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Email Error", "Failed to send email: " + e.getMessage());
        }
    }
}