package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tn.esprit.models.Categorie;
import tn.esprit.models.Matiere;
import tn.esprit.services.ServiceCategorie;
import tn.esprit.services.ServiceMatiere;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class EditMatiereController {

    @FXML
    private AnchorPane root;

    @FXML
    private TextField titreField;

    @FXML
    private Label titreError;

    @FXML
    private ComboBox<Categorie> categorieComboBox;

    @FXML
    private Label categorieError;

    @FXML
    private TextField prerequisField;

    @FXML
    private Label prerequisError;

    @FXML
    private TextArea descriptionField;

    @FXML
    private Label descriptionError;

    @FXML
    private ColorPicker couleurThemePicker;

    @FXML
    private Label couleurThemeError;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private ServiceMatiere serviceMatiere = new ServiceMatiere();
    private ServiceCategorie serviceCategorie = new ServiceCategorie();
    private Matiere matiere;

    @FXML
    public void initialize() {
        // Configuration du ColorPicker avec une couleur par défaut
        couleurThemePicker.setValue(Color.web("#3498db"));

        // Chargement des catégories dans le ComboBox
        loadCategories();

        // Ajout des listeners pour la validation en temps réel
        setupValidationListeners();
    }

    private void loadCategories() {
        List<Categorie> categories = serviceCategorie.getAll();
        categorieComboBox.setItems(FXCollections.observableArrayList(categories));

        // Configuration de l'affichage des items dans le ComboBox
        categorieComboBox.setCellFactory(lv -> new ListCell<Categorie>() {
            @Override
            protected void updateItem(Categorie categorie, boolean empty) {
                super.updateItem(categorie, empty);
                if (empty || categorie == null) {
                    setText(null);
                } else {
                    setText(categorie.getName());
                }
            }
        });

        // Configuration de l'affichage de l'item sélectionné
        categorieComboBox.setButtonCell(new ListCell<Categorie>() {
            @Override
            protected void updateItem(Categorie categorie, boolean empty) {
                super.updateItem(categorie, empty);
                if (empty || categorie == null) {
                    setText(null);
                } else {
                    setText(categorie.getName());
                }
            }
        });
    }

    private void setupValidationListeners() {
        // Validation du titre lors de la saisie
        titreField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTitre(newValue);
        });

        // Validation de la catégorie lors de la sélection
        categorieComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateCategorie(newValue);
        });

        // Validation des prérequis lors de la saisie
        prerequisField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePrerequis(newValue);
        });

        // Validation de la description lors de la saisie
        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateDescription(newValue);
        });
    }

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;

        // Remplissage des champs avec les données de la matière
        titreField.setText(matiere.getTitre());
        prerequisField.setText(matiere.getPrerequis());
        descriptionField.setText(matiere.getDescription());

        if (matiere.getCouleurTheme() != null && !matiere.getCouleurTheme().isEmpty()) {
            try {
                couleurThemePicker.setValue(Color.web(matiere.getCouleurTheme()));
            } catch (Exception e) {
                couleurThemePicker.setValue(Color.web("#3498db")); // Couleur par défaut en cas d'erreur
            }
        }

        if (matiere.getCategorie() != null) {
            categorieComboBox.getItems().forEach(categorie -> {
                if (categorie.getId() == matiere.getCategorie().getId()) {
                    categorieComboBox.setValue(categorie);
                }
            });
        }
    }

    @FXML
    void saveAction(ActionEvent event) {
        // Validation de tous les champs avant la sauvegarde
        boolean isValid = validateAllFields();

        if (isValid) {
            // Mise à jour des données de la matière
            matiere.setTitre(titreField.getText().trim());
            matiere.setCategorie(categorieComboBox.getValue());
            matiere.setPrerequis(prerequisField.getText().trim());
            matiere.setDescription(descriptionField.getText().trim());
            matiere.setCouleurTheme(toHexString(couleurThemePicker.getValue()));
            matiere.setUpdatedAt(LocalDateTime.now());

            try {
                // Mise à jour dans la base de données
                serviceMatiere.update(matiere);

                // Affichage d'un message de succès
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Mise à jour réussie",
                        "La matière \"" + matiere.getTitre() + "\" a été mise à jour avec succès.");

                // Retour à la liste des matières
                backToList(event);

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour",
                        "Une erreur est survenue lors de la mise à jour de la matière: " + e.getMessage());
            }
        }
    }

    @FXML
    void cancelAction(ActionEvent event) {
        // Confirmation avant d'annuler
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Annuler les modifications");
        alert.setContentText("Êtes-vous sûr de vouloir annuler les modifications? Les changements non sauvegardés seront perdus.");

        if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            backToList(event);
        }
    }

    private void backToList(ActionEvent event) {
        // Fermer la fenêtre actuelle
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }

    private boolean validateAllFields() {
        boolean titreValid = validateTitre(titreField.getText());
        boolean categorieValid = validateCategorie(categorieComboBox.getValue());
        boolean prerequisValid = validatePrerequis(prerequisField.getText());
        boolean descriptionValid = validateDescription(descriptionField.getText());

        return titreValid && categorieValid && prerequisValid && descriptionValid;
    }

    private boolean validateTitre(String titre) {
        boolean isValid = true;

        if (titre == null || titre.trim().isEmpty()) {
            showError(titreError, "Le titre est obligatoire");
            isValid = false;
        } else if (titre.trim().length() < 3) {
            showError(titreError, "Le titre doit contenir au moins 3 caractères");
            isValid = false;
        } else if (titre.trim().length() > 255) {
            showError(titreError, "Le titre ne doit pas dépasser 255 caractères");
            isValid = false;
        } else {
            hideError(titreError);
        }

        return isValid;
    }

    private boolean validateCategorie(Categorie categorie) {
        boolean isValid = true;

        if (categorie == null) {
            showError(categorieError, "La catégorie est obligatoire");
            isValid = false;
        } else {
            hideError(categorieError);
        }

        return isValid;
    }

    private boolean validatePrerequis(String prerequis) {
        boolean isValid = true;

        if (prerequis == null || prerequis.trim().isEmpty()) {
            showError(prerequisError, "Les prérequis sont obligatoires");
            isValid = false;
        } else if (prerequis.trim().length() > 255) {
            showError(prerequisError, "Les prérequis ne doivent pas dépasser 255 caractères");
            isValid = false;
        } else {
            hideError(prerequisError);
        }

        return isValid;
    }

    private boolean validateDescription(String description) {
        boolean isValid = true;

        if (description == null || description.trim().isEmpty()) {
            showError(descriptionError, "La description est obligatoire");
            isValid = false;
        } else if (description.trim().length() < 10) {
            showError(descriptionError, "La description doit contenir au moins 10 caractères");
            isValid = false;
        } else if (description.trim().length() > 255) {
            showError(descriptionError, "La description ne doit pas dépasser 255 caractères");
            isValid = false;
        } else {
            hideError(descriptionError);
        }

        return isValid;
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
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
            Stage stage = (Stage) cancelButton.getScene().getWindow();
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
