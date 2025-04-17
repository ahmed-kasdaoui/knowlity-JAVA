package view;

import Entities.Blog;
import Services.BlogServices;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
    
    @FXML
    private void initialize() {
        blogService = new BlogServices();
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setBlog(Blog blog) {
        this.blog = blog;
        
        if (blog != null) {
            // Editing existing blog
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
            // Creating new blog
            formTitle.setText("Nouveau Blog");
        }
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
            
            blog.setTitle(titleField.getText());
            blog.setContent(contentArea.getText());
            blog.setCreatorName(creatorField.getText());
            blog.setUserImage(userImageField.getText());
            blog.setBlogImage(blogImageField.getText());
            
            if (blog.getId() == 0) {
                // Nouveau blog
                blogService.add(blog);
            } else {
                // Blog existant
                blogService.update(blog);
            }
            
            dialogStage.close();
            
        } catch (Exception e) {
            showError("Erreur", "Impossible de sauvegarder le blog: " + e.getMessage());
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
                // Convertir directement le fichier en Base64
                byte[] fileContent = Files.readAllBytes(file.toPath());
                String base64 = Base64.getEncoder().encodeToString(fileContent);
                String extension = getFileExtension(file).toLowerCase();
                String mimeType = switch (extension) {
                    case "png" -> "image/png";
                    case "gif" -> "image/gif";
                    default -> "image/jpeg";
                };
                String imageUrl = "data:" + mimeType + ";base64," + base64;
                
                // Update fields
                blogImageField.setText(imageUrl);
                imageNameLabel.setText(file.getName());
                
                // Update preview
                updatePreviewImage(imageUrl);
                
            } catch (IOException e) {
                showError("Erreur", "Impossible de charger l'image: " + e.getMessage());
            }
        }
    }
    
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // Empty extension
        }
        return name.substring(lastIndexOf + 1);
    }
    
    private boolean validateForm() {
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            showError("Erreur de validation", "Le titre est obligatoire");
            return false;
        }
        
        if (contentArea.getText() == null || contentArea.getText().trim().isEmpty()) {
            showError("Erreur de validation", "Le contenu est obligatoire");
            return false;
        }
        
        if (creatorField.getText() == null || creatorField.getText().trim().isEmpty()) {
            showError("Erreur de validation", "Le nom du créateur est obligatoire");
            return false;
        }
        
        return true;
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void updatePreviewImage(String imageUrl) {
        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (imageUrl.startsWith("data:image")) {
                    String base64Image = imageUrl.split(",")[1];
                    byte[] imageData = Base64.getDecoder().decode(base64Image);
                    Image image = new Image(new ByteArrayInputStream(imageData));
                    previewImage.setImage(image);
                } else {
                    previewImage.setImage(new Image(imageUrl));
                }
                previewImage.setFitWidth(200);
                previewImage.setFitHeight(150);
                previewImage.setPreserveRatio(true);
            }
        } catch (Exception e) {
            System.err.println("Error updating preview image: " + e.getMessage());
        }
    }
}
