package controllers;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tn.esprit.models.Categorie;
import tn.esprit.models.Matiere;
import tn.esprit.services.ServiceCategorie;
import tn.esprit.services.ServiceMatiere;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class AjouterMatiereController {

    @FXML
    private TextField titreField;

    @FXML
    private ComboBox<Categorie> categorieComboBox;

    @FXML
    private TextField prerequisField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ColorPicker couleurThemePicker;

    @FXML
    private Button saveButton;

    @FXML
    private Label titreError;

    @FXML
    private Label categorieError;

    @FXML
    private Label prerequisError;

    @FXML
    private Label descriptionError;

    @FXML
    private Label couleurThemeError;

    private ServiceMatiere serviceMatiere = new ServiceMatiere();
    private ServiceCategorie serviceCategorie = new ServiceCategorie();
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private Matiere matiereToEdit;
    private boolean isEditMode = false;
    private Runnable onReturnCallback;

    @FXML
    public void initialize() {
        // Populate categorieComboBox
        categorieComboBox.getItems().addAll(serviceCategorie.getAll());
        categorieComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Categorie item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        categorieComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Categorie item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        System.out.println("categorieComboBox populated with: " + categorieComboBox.getItems().size() + " categories");

        // Set default color
        couleurThemePicker.setValue(Color.web("#7c3aed"));
    }

    public void setOnReturnCallback(Runnable callback) {
        this.onReturnCallback = callback;
    }

    @FXML
    void saveAction(ActionEvent event) {
        try {
            // Reset validation
            resetValidation();

            // Retrieve inputs
            String titre = titreField.getText().trim();
            Categorie categorie = categorieComboBox.getValue();
            String prerequis = prerequisField.getText().trim();
            String description = descriptionField.getText().trim();
            Color couleurTheme = couleurThemePicker.getValue();

            // Validate
            boolean hasError = false;

            // Titre
            if (titre.isEmpty()) {
                titreError.setText("Le titre est requis.");
                titreError.setVisible(true);
                titreError.setManaged(true);
                titreField.getStyleClass().add("error");
                hasError = true;
            } else if (titre.length() > 255) {
                titreError.setText("Maximum 255 caractères.");
                titreError.setVisible(true);
                titreError.setManaged(true);
                titreField.getStyleClass().add("error");
                hasError = true;
            }

            // Catégorie
            if (categorie == null) {
                categorieError.setText("La catégorie est requise.");
                categorieError.setVisible(true);
                categorieError.setManaged(true);
                categorieComboBox.getStyleClass().add("error");
                hasError = true;
            }

            // Prérequis
            if (prerequis.isEmpty()) {
                prerequisError.setText("Les prérequis sont requis.");
                prerequisError.setVisible(true);
                prerequisError.setManaged(true);
                prerequisField.getStyleClass().add("error");
                hasError = true;
            } else if (prerequis.length() > 255) {
                prerequisError.setText("Maximum 255 caractères.");
                prerequisError.setVisible(true);
                prerequisError.setManaged(true);
                prerequisField.getStyleClass().add("error");
                hasError = true;
            }

            // Description
            if (description.isEmpty()) {
                descriptionError.setText("La description est requise.");
                descriptionError.setVisible(true);
                descriptionError.setManaged(true);
                descriptionField.getStyleClass().add("error");
                hasError = true;
            } else if (description.length() > 255) {
                descriptionError.setText("Maximum 255 caractères.");
                descriptionError.setVisible(true);
                descriptionError.setManaged(true);
                descriptionField.getStyleClass().add("error");
                hasError = true;
            }

            // Couleur du thème
            String hexColor = couleurTheme != null ? String.format("#%02X%02X%02X",
                    (int) (couleurTheme.getRed() * 255),
                    (int) (couleurTheme.getGreen() * 255),
                    (int) (couleurTheme.getBlue() * 255)) : null;
            if (hexColor == null || !HEX_COLOR_PATTERN.matcher(hexColor).matches()) {
                couleurThemeError.setText("Couleur invalide.");
                couleurThemeError.setVisible(true);
                couleurThemeError.setManaged(true);
                couleurThemePicker.getStyleClass().add("error");
                hasError = true;
            }

            if (hasError) {
                return;
            }

            // Create and save Matiere
            Matiere matiere = isEditMode ? matiereToEdit : new Matiere();
            matiere.setTitre(titre);
            matiere.setCategorie(categorie);
            matiere.setPrerequis(prerequis);
            matiere.setDescription(description);
            matiere.setCouleurTheme(hexColor);
            if (!isEditMode) {
                matiere.setCreatedAt(LocalDateTime.now());
            }
            matiere.setUpdatedAt(LocalDateTime.now());

            if (isEditMode) {
                serviceMatiere.update(matiere);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Matière modifiée.");
            } else {
                serviceMatiere.add(matiere);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Matière enregistrée.");
            }

            if (onReturnCallback != null) {
                onReturnCallback.run();
            }
            clearForm(); // Vider le formulaire après une soumission réussie
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            System.err.println("Save error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void retourAuxMatiere(Event event) {
        if (onReturnCallback != null) {
            onReturnCallback.run();
        }
    }

    /** Méthode pour vider le formulaire */
    private void clearForm() {
        // Vider les champs texte
        titreField.clear();
        prerequisField.clear();
        descriptionField.clear();

        // Réinitialiser la ComboBox
        categorieComboBox.getSelectionModel().clearSelection();

        // Réinitialiser le ColorPicker
        couleurThemePicker.setValue(Color.WHITE); // Ou une couleur par défaut comme Color.WHITE

        // Réinitialiser les états de validation
        resetValidation();

        // Optionnel : Remettre le focus sur le premier champ
        titreField.requestFocus();
    }

    private void resetValidation() {
        titreError.setVisible(false);
        titreError.setManaged(false);
        categorieError.setVisible(false);
        categorieError.setManaged(false);
        prerequisError.setVisible(false);
        prerequisError.setManaged(false);
        descriptionError.setVisible(false);
        descriptionError.setManaged(false);
        couleurThemeError.setVisible(false);
        couleurThemeError.setManaged(false);

        titreField.getStyleClass().remove("error");
        categorieComboBox.getStyleClass().remove("error");
        prerequisField.getStyleClass().remove("error");
        descriptionField.getStyleClass().remove("error");
        couleurThemePicker.getStyleClass().remove("error");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
            Stage stage = (Stage) titreField.getScene().getWindow();
            // Create a new scene with the loaded root
            Scene scene = new Scene(root, 1000, 700); // Match FXML dimensions
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setMatiere(Matiere matiere) {
        this.matiereToEdit = matiere;
        this.isEditMode = true;
        
        titreField.setText(matiere.getTitre());
        categorieComboBox.setValue(matiere.getCategorie());
        descriptionField.setText(matiere.getDescription());
        prerequisField.setText(matiere.getPrerequis());
        if (matiere.getCouleurTheme() != null && !matiere.getCouleurTheme().isEmpty()) {
            couleurThemePicker.setValue(Color.web(matiere.getCouleurTheme()));
        }
    }
}