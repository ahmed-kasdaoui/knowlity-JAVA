package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import tn.esprit.models.OffreCovoiturage;
import tn.esprit.services.ServiceOffreCovoiturage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AjouterOffreController {

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField departField;

    @FXML
    private TextField destinationField;

    @FXML
    private Label fileLabel;

    @FXML
    private TextField matriculeField;

    @FXML
    private TextField placesField;

    @FXML
    private TextField prixField;

    @FXML
    private Button submitButton;

    @FXML
    private TextField timeField;

    @FXML
    private Button uploadButton;

    @FXML
    void ajouterOffreAction(ActionEvent event) {
        try {
            String depart = departField.getText();
            String destination = destinationField.getText();
            int matricule = Integer.parseInt(matriculeField.getText());
            int places = Integer.parseInt(placesField.getText());
            float prix = Float.parseFloat(prixField.getText());

            // Combine date and time into LocalDateTime
            LocalDate date = datePicker.getValue();
            LocalTime time = LocalTime.parse(timeField.getText()); // Make sure format is HH:mm
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            String img = fileLabel.getText(); // Just for now

            OffreCovoiturage offre = new OffreCovoiturage(
                    depart,
                    1, // conducteurId placeholder
                    destination,
                    matricule,
                    places,
                    dateTime,
                    "active",
                    prix,
                    img.isEmpty() ? null : img
            );

            ServiceOffreCovoiturage sf = new ServiceOffreCovoiturage();
            sf.add(offre);
            System.out.print(offre);// Use the instance you created

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Offre ajoutée avec succès !");
            alert.show();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailPersonne.fxml"));
            // loader.load(); // if needed to load the next scene

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec de l'ajout de l'offre");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }
}
