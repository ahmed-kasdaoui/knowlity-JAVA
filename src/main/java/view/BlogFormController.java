package view;

import Entities.Blog;
import Services.BlogServices;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.shape.SVGPath;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.time.LocalDateTime;

public class BlogFormController {
    @FXML
    private Text formTitle;
    
    @FXML
    private TextField titleField;
    
    @FXML
    private TextField creatorField;
    
    @FXML
    private TextArea contentArea;
    
    @FXML
    private TextField userImageField;
    
    @FXML
    private TextField blogImageField;
    
    @FXML
    private Button uploadImageButton;
    
    @FXML
    private Label imageNameLabel;
    
    @FXML
    private ImageView previewImage;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private BlogServices blogService;
    private Stage dialogStage;
    private Blog blog;
    private Label titleErrorLabel;
    private Label contentErrorLabel;
    private Label creatorErrorLabel;
    private Runnable onSaveCallback;
    
    @FXML
    private void initialize() {
        blogService = new BlogServices();
        
        // Créer les labels d'erreur
        titleErrorLabel = new Label();
        titleErrorLabel.getStyleClass().add("error-label");
        titleErrorLabel.setManaged(false);
        titleErrorLabel.setVisible(false);
        
        contentErrorLabel = new Label();
        contentErrorLabel.getStyleClass().add("error-label");
        contentErrorLabel.setManaged(false);
        contentErrorLabel.setVisible(false);
        
        creatorErrorLabel = new Label();
        creatorErrorLabel.getStyleClass().add("error-label");
        creatorErrorLabel.setManaged(false);
        creatorErrorLabel.setVisible(false);
        
        // Ajouter les labels après chaque champ
        VBox titleContainer = (VBox) titleField.getParent();
        titleContainer.getChildren().add(titleErrorLabel);
        
        VBox contentContainer = (VBox) contentArea.getParent();
        contentContainer.getChildren().add(contentErrorLabel);
        
        VBox creatorContainer = (VBox) creatorField.getParent();
        creatorContainer.getChildren().add(creatorErrorLabel);
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
        
        if (blog != null) {
            formTitle.setText("Modifier le Blog");
            titleField.setText(blog.getTitle());
            contentArea.setText(blog.getContent());
            creatorField.setText(blog.getCreatorName());
            userImageField.setText(blog.getUserImage());
            blogImageField.setText(blog.getBlogImage());
            
            if (blog.getBlogImage() != null && !blog.getBlogImage().isEmpty()) {
                imageNameLabel.setText("Image existante");
                updatePreviewImage(blog.getBlogImage());
            }
        } else {
            formTitle.setText("Nouveau Blog");
        }
    }
    
    private void showValidationDialog(String title, String message) {
        Stage dialogStage = new Stage(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(this.dialogStage);

        VBox dialogVbox = new VBox(10);
        dialogVbox.getStyleClass().add("validation-dialog");
        
        // Icône d'erreur (utilisation d'un SVG pour une meilleure qualité)
        SVGPath errorIcon = new SVGPath();
        errorIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z");
        errorIcon.getStyleClass().add("error-icon");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("header");
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("content");
        messageLabel.setWrapText(true);
        
        Button okButton = new Button("OK");
        okButton.getStyleClass().add("ok-button");
        okButton.setOnAction(e -> dialogStage.close());
        
        HBox buttonBox = new HBox();
        buttonBox.getStyleClass().add("button-container");
        buttonBox.getChildren().add(okButton);
        
        dialogVbox.getChildren().addAll(errorIcon, titleLabel, messageLabel, buttonBox);
        
        Scene dialogScene = new Scene(dialogVbox);
        dialogScene.setFill(null);
        dialogScene.getStylesheets().add(getClass().getResource("/styles/validation-dialog.css").toExternalForm());
        
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }
    
    private void showFieldError(TextField field, Label errorLabel, String message) {
        field.getStyleClass().add("error-field");
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }
    
    private void clearFieldError(TextField field, Label errorLabel) {
        field.getStyleClass().remove("error-field");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }
    
    private void showTextAreaError(TextArea area, Label errorLabel, String message) {
        area.getStyleClass().add("error-field");
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }
    
    private void clearTextAreaError(TextArea area, Label errorLabel) {
        area.getStyleClass().remove("error-field");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        // Réinitialiser tous les états d'erreur
        clearFieldError(titleField, titleErrorLabel);
        clearTextAreaError(contentArea, contentErrorLabel);
        clearFieldError(creatorField, creatorErrorLabel);
        
        String title = titleField.getText();
        String content = contentArea.getText();
        String creator = creatorField.getText();

        // Nettoyage des espaces
        title = title != null ? title.trim() : "";
        content = content != null ? content.trim() : "";
        creator = creator != null ? creator.trim() : "";

        StringBuilder errorMessage = new StringBuilder();

        if (title.length() < 3) {
            showFieldError(titleField, titleErrorLabel, 
                "Le titre doit contenir au moins 3 caractères (actuel: " + title.length() + ")");
            errorMessage.append("• Le titre doit contenir au moins 3 caractères\n");
            isValid = false;
        }
        
        if (content.length() < 10) {
            showTextAreaError(contentArea, contentErrorLabel,
                "Le contenu doit contenir au moins 10 caractères (actuel: " + content.length() + ")");
            errorMessage.append("• Le contenu doit contenir au moins 10 caractères\n");
            isValid = false;
        }
        
        if (creator.isEmpty()) {
            showFieldError(creatorField, creatorErrorLabel,
                "Le nom du créateur est obligatoire");
            errorMessage.append("• Le nom du créateur est obligatoire\n");
            isValid = false;
        }

        if (!isValid) {
            showValidationDialog("Erreur de Validation", 
                "Veuillez corriger les erreurs suivantes :\n\n" + errorMessage.toString());
        }
        
        return isValid;
    }
    
    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }
        
        try {
            if (blog == null) {
                blog = new Blog();
                LocalDateTime now = LocalDateTime.now();
                blog.setCreatedAt(now);
                blog.setUpdatedAt(now);
            } else {
                blog.setUpdatedAt(LocalDateTime.now());
            }
            
            blog.setTitle(titleField.getText().trim());
            blog.setContent(contentArea.getText().trim());
            blog.setCreatorName(creatorField.getText().trim());
            blog.setUserImage(userImageField.getText());
            blog.setBlogImage(blogImageField.getText());
            
            if (blog.getId() == 0) {
                blogService.add(blog);
            } else {
                blogService.update(blog);
            }
            
            // Run the callback before closing
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            
            // Close the dialog
            dialogStage.close();
        } catch (Exception e) {
            showValidationDialog("Erreur", "Impossible de sauvegarder le blog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                String base64 = Base64.getEncoder().encodeToString(fileContent);
                String extension = getFileExtension(file).toLowerCase();
                String mimeType = switch (extension) {
                    case "png" -> "image/png";
                    case "gif" -> "image/gif";
                    default -> "image/jpeg";
                };
                String imageUrl = "data:" + mimeType + ";base64," + base64;
                
                blogImageField.setText(imageUrl);
                imageNameLabel.setText(file.getName());
                
                updatePreviewImage(imageUrl);
                
            } catch (IOException e) {
                showValidationDialog("Erreur", "Impossible de charger l'image: " + e.getMessage());
            }
        }
    }
    
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf + 1);
    }
    
    private void updatePreviewImage(String imageUrl) {
        try {
            Image image = new Image(imageUrl);
            previewImage.setImage(image);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
        }
    }
}
