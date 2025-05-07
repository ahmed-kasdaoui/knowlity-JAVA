package controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import tn.esprit.models.EventRegistration;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEventRegistration;
import tn.esprit.services.ServiceEvents;
import tn.knowlity.entity.User;
import tn.knowlity.service.userService;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.UUID;

public class ShowEventRegistrationController {

    @FXML
    public Button backToEventsRegistrationButton;
    @FXML
    private Label idLabel;
    @FXML
    private Label eventTitleLabel;
    @FXML
    private Label seatsAvailableLabel;
    @FXML
    private Label userIdLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label registrationDateLabel;
    @FXML
    private Label placesReservedLabel;
    @FXML
    private Label comingFromLabel;
    @FXML
    private Label disabledParkingLabel;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private Button updateStatusButton;

    private Events  event = new Events();
    private final ServiceEvents serviceEvents = new ServiceEvents();
    private final ServiceEventRegistration serviceEventRegistration;
    private EventRegistration currentRegistration;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    userService userService = new userService();
    private User user = null;

    public ShowEventRegistrationController() {
        this.serviceEventRegistration = new ServiceEventRegistration();
    }

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll("Pending", "Confirmed", "Canceled");

        eventTitleLabel.setOnMouseClicked(event -> navigateToEventDetails());

        updateStatusButton.setOnAction(event -> updateStatus());
        backToEventsRegistrationButton.setOnAction(event -> navigateToEventsRegistartion());
    }

    public void setRegistration(EventRegistration registration) {
        this.currentRegistration = registration;
        event=currentRegistration.getEvent();
        if (registration != null) {
            eventTitleLabel.setText(registration.getEvent() != null ? registration.getEvent().getTitle() : "N/A");
            seatsAvailableLabel.setText(registration.getEvent() != null ? String.valueOf(registration.getEvent().getSeatsAvailable()) : "N/A");
            userIdLabel.setText(registration.getName());
            statusLabel.setText(registration.getStatus() != null ? registration.getStatus() : "N/A");
            registrationDateLabel.setText(registration.getRegistrationDate() != null ? registration.getRegistrationDate().format(dateFormatter) : "N/A");
            placesReservedLabel.setText(registration.getPlacesReserved() != null ? String.valueOf(registration.getPlacesReserved()) : "N/A");
            comingFromLabel.setText(registration.getComingFrom() != null ? registration.getComingFrom() : "N/A");
            disabledParkingLabel.setText(registration.isDisabledParking() ? "Yes" : "No");
            statusCombo.setValue(registration.getStatus());
        }
    }

    private void updateStatus() {
        String newStatus = statusCombo.getValue();
        event=currentRegistration.getEvent();
        if (newStatus == null) {
            showAlert(Alert.AlertType.ERROR, "Invalid Status", "Please select a status.");
            return;
        }
        if (newStatus == currentRegistration.getStatus()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Status", "Status is already in use.");
            return;
        }

        if (currentRegistration != null) {
            int newSeatsAvailable;
            try {
                currentRegistration.setStatus(newStatus);
                if (newStatus=="Confirmed"){
                    String check_in_code = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
                    newSeatsAvailable = event.getSeatsAvailable()-currentRegistration.getPlacesReserved();
                    currentRegistration.setCheck_in_code(check_in_code);
                    serviceEventRegistration.update(currentRegistration);
                    serviceEvents.updateSeatsAvailable(event.getId(), newSeatsAvailable);
                    String qrFilePath=generateQRCodeForRegistration(currentRegistration, event);
                    sendEmailWithQRCode(currentRegistration, event, qrFilePath);
                }else {
                    newSeatsAvailable = event.getSeatsAvailable()+currentRegistration.getPlacesReserved();
                    serviceEvents.updateSeatsAvailable(event.getId(), newSeatsAvailable);
                    currentRegistration.setCheck_in_code(null);
                    serviceEventRegistration.update(currentRegistration);
                }
                seatsAvailableLabel.setText(String.valueOf(newSeatsAvailable));
                statusLabel.setText(newStatus);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Status updated successfully.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status: " + e.getMessage());
            }
        }
    }


    private String generateQRCodeForRegistration(EventRegistration registration, Events event) {
        if (!"Confirmed".equalsIgnoreCase(registration.getStatus())) {
            return null;
        }
        try {
            // Prepare QR code content
            String qrContent = String.format(
                    "Check-In Code: %s\nEvent: %s\nName: %s\nPlaces Reserved: %s\nDisabled Parking: %s\nLocation: %s",
                    registration.getCheck_in_code() != null ? registration.getCheck_in_code() : "N/A",
                    event.getTitle(),
                    registration.getName(),
                    registration.getPlacesReserved(),
                    registration.isDisabledParking() ? "YES" : "NO",
                    event.getLocation() != null ? event.getLocation() : "N/A"
            );

            // QR code settings
            int width = 300;
            int height = 300;
            String filePath = "src/main/resources/images/qrcodes/registration_" + registration.getId() + ".png";

            // Ensure the qrcodes directory exists
            File qrCodeDir = new File("qrcodes");
            if (!qrCodeDir.exists()) {
                qrCodeDir.mkdirs();
            }

            // Generate QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, width, height);

            // Save QR code as an image
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            showAlert(Alert.AlertType.INFORMATION, "Success", "QR code generated successfully .");
            return filePath;

        } catch (WriterException | IOException e) {
            showAlert(Alert.AlertType.ERROR, "QR Code Error", "Failed to generate QR code: " + e.getMessage());
            return null;
        }
    }


    private void sendEmailWithQRCode(EventRegistration registration, Events event, String qrFilePath) throws SQLException {
        final String username = "chamseddinedoula7@gmail.com";
        final String password = "xlvmkpnbcrjbrysu";
        User user = userService.recherparid(registration.getUserId());
        System.out.println(user);
        String mail = user.getEmail();

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

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
            message.setSubject("Event Registration Confirmation - " + event.getTitle());

            // Create the email body
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            String emailBody = String.format(
                    "Dear %s,\n\nYour registration for the event '%s' has been confirmed!\n\n" +
                            "Event Details:\n- Title: %s\n- Start Date: %s\n- Location: %s\n\n" +
                            "Please find your QR code attached. Present this QR code at the event for check-in.\n\n" +
                            "Thank you for registering!\nEvent Management Team",
                    registration.getName(),
                    event.getTitle(),
                    event.getTitle(),
                    event.getStartDate() != null ? event.getStartDate().format(dateFormatter) : "N/A",
                    event.getLocation() != null ? event.getLocation() : "N/A"
            );
            messageBodyPart.setText(emailBody);

            // Attach the QR code
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File(qrFilePath));

            // Create a multipart message to combine body and attachment
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            // Set the multipart content to the message
            message.setContent(multipart);

            // Send the email
            Transport.send(message);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Confirmation email sent to " + mail);

        } catch (MessagingException | IOException e) {
            System.err.println("Erreur détaillée : " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Email Error", "Failed to send email: " + e.getMessage());
        }
    }


    private void navigateToEventDetails() {
        if (currentRegistration == null || currentRegistration.getEvent() == null) {
            showAlert(Alert.AlertType.ERROR, "No Event", "No event associated with this registration.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ShowEvent.fxml"));
            eventTitleLabel.getScene().setRoot(loader.load());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load event details: " + e.getMessage());
        }
    }

    private void navigateToEventsRegistartion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventRegistrationList.fxml"));
            backToEventsRegistrationButton.getScene().setRoot(loader.load());
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