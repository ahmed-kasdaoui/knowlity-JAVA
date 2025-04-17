package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import tn.esprit.models.Matiere;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class DetailsMatiereController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label titreValueLabel;

    @FXML
    private Label categorieValueLabel;

    @FXML
    private Label prerequisValueLabel;

    @FXML
    private Label descriptionValueLabel;

    @FXML
    private StackPane couleurThemeContainer;

    @FXML
    private Label createdAtValueLabel;

    @FXML
    private Label updatedAtValueLabel;

    @FXML
    private Button backButton;

    @FXML
    private Button editButton;

    private Matiere matiere;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
        displayMatiereDetails();
    }

    private void displayMatiereDetails() {
        if (matiere == null) return;

        // Mettre à jour le titre de la page
        titleLabel.setText("Détails de la matière : " + matiere.getTitre());

        // Remplir les informations de la matière
        titreValueLabel.setText(matiere.getTitre());

        if (matiere.getCategorie() != null) {
            categorieValueLabel.setText(matiere.getCategorie().getName());
        } else {
            categorieValueLabel.setText("Non définie");
        }

        prerequisValueLabel.setText(matiere.getPrerequis() != null ? matiere.getPrerequis() : "Aucun prérequis spécifié");

        descriptionValueLabel.setText(matiere.getDescription() != null ? matiere.getDescription() : "Aucune description disponible");

        // Afficher la couleur du thème comme un rectangle coloré
        if (matiere.getCouleurTheme() != null && !matiere.getCouleurTheme().isEmpty()) {
            try {
                Rectangle colorRect = new Rectangle(100, 50);
                colorRect.setFill(Color.web(matiere.getCouleurTheme()));
                colorRect.setStroke(Color.BLACK);
                colorRect.setStrokeWidth(1);
                colorRect.setArcWidth(10);
                colorRect.setArcHeight(10);

                Label colorLabel = new Label(matiere.getCouleurTheme());
                colorLabel.setTextFill(Color.web("#333333"));
                colorLabel.setTranslateY(70);

                couleurThemeContainer.getChildren().clear();
                couleurThemeContainer.getChildren().addAll(colorRect, colorLabel);
            } catch (Exception e) {
                couleurThemeContainer.getChildren().clear();
                couleurThemeContainer.getChildren().add(new Label("Couleur invalide: " + matiere.getCouleurTheme()));
            }
        } else {
            couleurThemeContainer.getChildren().clear();
            couleurThemeContainer.getChildren().add(new Label("Non définie"));
        }

        // Afficher les dates
        if (matiere.getCreatedAt() != null) {
            createdAtValueLabel.setText(matiere.getCreatedAt().format(dateFormatter));
        }

        if (matiere.getUpdatedAt() != null) {
            updatedAtValueLabel.setText(matiere.getUpdatedAt().format(dateFormatter));
        }
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        try {
            // Charger la vue de liste
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeMatiere.fxml"));
            Scene scene = new Scene(loader.load());

            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Définir la nouvelle scène
            stage.setScene(scene);
            stage.setTitle("Liste des Matières");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la liste des matières: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditButton(ActionEvent event) {
        try {
            // Charger la vue d'édition
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditMatiere.fxml"));
            Scene scene = new Scene(loader.load());

            // Obtenir le contrôleur et définir la matière à modifier
            EditMatiereController controller = loader.getController();
            controller.setMatiere(matiere);

            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Définir la nouvelle scène
            stage.setScene(scene);
            stage.setTitle("Modifier une Matière");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du formulaire d'édition: " + e.getMessage());
        }
    }
    void handleListes(ActionEvent event) {
        System.out.println("handleListes clicked");
        loadScene("/ListeCategories.fxml");
    }
    private void loadScene(String fxmlPath) {
        try {
            // Load the new FXML
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Get the current stage from a known node
            Stage stage = (Stage) editButton.getScene().getWindow();
            // Create a new scene with the loaded root
            Scene scene = new Scene(root, 1000, 700); // Match FXML dimensions
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }

    }
}
