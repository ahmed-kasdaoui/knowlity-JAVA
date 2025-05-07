package view;

import Entities.Creator;
import Services.CreatorService;
import Utils.ImageUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.net.URL;

public class CreatorFormController {
    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private TextField profileField;
    @FXML private TextArea achievementsArea;
    @FXML private TextField imageField;
    @FXML private ImageView imagePreview;
    
    @FXML private Label nameError;
    @FXML private Label profileError;
    @FXML private Label achievementsError;
    @FXML private Label imageError;

    private CreatorService creatorService;
    private Creator creator;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        creatorService = new CreatorService();
        
        // Charger l'image par défaut
        try {
            URL defaultImageUrl = getClass().getResource("/images/default-avatar.png");
            if (defaultImageUrl != null) {
                imagePreview.setImage(new Image(defaultImageUrl.toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image par défaut: " + e.getMessage());
        }

        // Configuration de la validation en temps réel
        setupValidation();

        // Masquer les messages d'erreur initialement
        hideAllErrors();
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
        this.isEditMode = true;
        formTitle.setText("Modifier Créateur");
        
        nameField.setText(creator.getName());
        profileField.setText(creator.getProfile());
        achievementsArea.setText(creator.getAchievements());
        imageField.setText(creator.getImage());
        
        try {
            File imageFile = new File(creator.getImage());
            if (imageFile.exists()) {
                imagePreview.setImage(new Image(imageFile.toURI().toString()));
            } else {
                loadDefaultImage();
            }
        } catch (Exception e) {
            loadDefaultImage();
        }
    }

    private void loadDefaultImage() {
        try {
            URL defaultImageUrl = getClass().getResource("/images/default-avatar.png");
            if (defaultImageUrl != null) {
                imagePreview.setImage(new Image(defaultImageUrl.toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image par défaut: " + e.getMessage());
        }
    }

    @FXML
    private void handleImageSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(imageField.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String imagePath = ImageUtils.saveImage(selectedFile);
                imageField.setText(imagePath);
                imagePreview.setImage(new Image(new File(imagePath).toURI().toString()));
            } catch (Exception e) {
                showError("Erreur", "Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        Creator newCreator = new Creator(
            nameField.getText(),
            profileField.getText(),
            achievementsArea.getText(),
            imageField.getText()
        );

        try {
            if (isEditMode) {
                newCreator.setId(creator.getId());
                creatorService.update(newCreator);
            } else {
                creatorService.add(newCreator);
            }
            closeWindow();
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de l'enregistrement du créateur");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void setupValidation() {
        // Validation du nom
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = !newValue.trim().isEmpty() && newValue.length() >= 3;
            updateFieldValidation(nameField, nameError, isValid,
                    "Le nom doit contenir au moins 3 caractères");
        });

        // Validation du profil
        profileField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = !newValue.trim().isEmpty() && newValue.length() >= 10;
            updateFieldValidation(profileField, profileError, isValid,
                    "Le profil doit contenir au moins 10 caractères");
        });

        // Validation des réalisations
        achievementsArea.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = !newValue.trim().isEmpty() && newValue.length() >= 20;
            updateFieldValidation(achievementsArea, achievementsError, isValid,
                    "Les réalisations doivent contenir au moins 20 caractères");
        });

        // Validation de l'image
        imageField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = !newValue.trim().isEmpty();
            updateFieldValidation(imageField, imageError, isValid,
                    "Une image est requise");
        });
    }

    private void updateFieldValidation(TextInputControl field, Label errorLabel, boolean isValid, String errorMessage) {
        if (!isValid) {
            field.getStyleClass().remove("valid-field");
            field.getStyleClass().add("error-field");
            errorLabel.setText(errorMessage);
            errorLabel.getStyleClass().add("visible");
        } else {
            field.getStyleClass().remove("error-field");
            field.getStyleClass().add("valid-field");
            errorLabel.getStyleClass().remove("visible");
        }
    }

    private void hideAllErrors() {
        nameError.getStyleClass().remove("visible");
        profileError.getStyleClass().remove("visible");
        achievementsError.getStyleClass().remove("visible");
        imageError.getStyleClass().remove("visible");
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validation du nom
        if (nameField.getText().trim().isEmpty() || nameField.getText().length() < 3) {
            updateFieldValidation(nameField, nameError, false,
                    "Le nom doit contenir au moins 3 caractères");
            isValid = false;
        }

        // Validation du profil
        if (profileField.getText().trim().isEmpty() || profileField.getText().length() < 10) {
            updateFieldValidation(profileField, profileError, false,
                    "Le profil doit contenir au moins 10 caractères");
            isValid = false;
        }

        // Validation des réalisations
        if (achievementsArea.getText().trim().isEmpty() || achievementsArea.getText().length() < 20) {
            updateFieldValidation(achievementsArea, achievementsError, false,
                    "Les réalisations doivent contenir au moins 20 caractères");
            isValid = false;
        }

        // Validation de l'image
        if (imageField.getText().trim().isEmpty()) {
            updateFieldValidation(imageField, imageError, false,
                    "Une image est requise");
            isValid = false;
        }

        return isValid;
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
