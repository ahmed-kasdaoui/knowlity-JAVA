package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.Categorie;
import tn.esprit.services.ServiceCategorie;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class EditCategorieController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField motsClesField;

    @FXML
    private ComboBox<String> publicCibleComboBox;

    @FXML
    private TextArea descrptionField;

    @FXML
    private Button uploadButton;

    @FXML
    private Label fileLabel;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Button saveButton;

    @FXML
    private Label nameError;

    @FXML
    private Label motsClesError;

    @FXML
    private Label publicCibleError;

    @FXML
    private Label descrptionError;

    @FXML
    private Label brochureError;

    private ServiceCategorie serviceCategorie = new ServiceCategorie();
    private static final String UPLOAD_DIR = "Uploads/";
    private static final String[] PUBLIC_CIBLE_OPTIONS = {"élèves", "étudiants", "adultes", "professionnels"};
    private Categorie currentCategorie;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        // Remplir les options du ComboBox
        publicCibleComboBox.getItems().addAll(PUBLIC_CIBLE_OPTIONS);

        // Si une catégorie est définie, remplir les champs avec ses valeurs
        if (currentCategorie != null) {
            populateFields(currentCategorie);
        }
    }

    public void setCategorie(Categorie categorie) {
        this.currentCategorie = categorie;
        populateFields(categorie);
    }

    private void populateFields(Categorie categorie) {
        nameField.setText(categorie.getName());
        motsClesField.setText(categorie.getMotsCles());
        publicCibleComboBox.setValue(categorie.getPublicCible());
        descrptionField.setText(categorie.getDescrption());
        fileLabel.setText(categorie.getIcone());

        if (categorie.getIcone() != null && !categorie.getIcone().isEmpty()) {
            File file = new File(UPLOAD_DIR + categorie.getIcone());
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setVisible(true);
            }
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    void saveAction(ActionEvent event) {
        try {
            resetValidation();

            String name = nameField.getText().trim();
            String motsCles = motsClesField.getText().trim();
            String publicCible = publicCibleComboBox.getValue();
            String descrption = descrptionField.getText().trim();
            String icone = fileLabel.getText();

            boolean hasError = false;

            if (name.isEmpty()) {
                nameError.setText("Le nom est requis.");
                nameError.setVisible(true);
                hasError = true;
            }

            if (motsCles.isEmpty()) {
                motsClesError.setText("Les mots clés sont requis.");
                motsClesError.setVisible(true);
                hasError = true;
            }

            if (publicCible == null || publicCible.isEmpty()) {
                publicCibleError.setText("Le public cible est requis.");
                publicCibleError.setVisible(true);
                hasError = true;
            }

            if (descrption.isEmpty()) {
                descrptionError.setText("La description est requise.");
                descrptionError.setVisible(true);
                hasError = true;
            }

            if (icone.isEmpty()) {
                brochureError.setText("L'image est requise.");
                brochureError.setVisible(true);
                hasError = true;
            }

            if (hasError) {
                return;
            }

            // Mise à jour de la catégorie
            currentCategorie.setName(name);
            currentCategorie.setMotsCles(motsCles);
            currentCategorie.setPublicCible(publicCible);
            currentCategorie.setDescrption(descrption);
            currentCategorie.setIcone(icone);

            serviceCategorie.update(currentCategorie);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie mise à jour avec succès");
            
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    void cancelAction(ActionEvent event) {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void resetValidation() {
        nameError.setVisible(false);
        motsClesError.setVisible(false);
        publicCibleError.setVisible(false);
        descrptionError.setVisible(false);
        brochureError.setVisible(false);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void RetourAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListeCategories.fxml"));
            nameField.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    @FXML
    void uploadButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                String uniqueFileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                File destination = new File(UPLOAD_DIR + uniqueFileName);

                Files.copy(selectedFile.toPath(), destination.toPath());

                fileLabel.setText(uniqueFileName);
                imagePreview.setImage(new Image(destination.toURI().toString()));
                imagePreview.setVisible(true);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Image téléchargée avec succès.");

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de télécharger l'image : " + e.getMessage());
            }
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
            Stage stage = (Stage) nameField.getScene().getWindow();
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
