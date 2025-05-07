package view;

import Entities.Blog;
import Utils.ImageUtils;
import controller.BlogController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDateTime;

public class BlogFormView {
    private BlogController controller;
    private Stage stage;
    private TextField titleField;
    private TextArea contentArea;
    private TextField creatorNameField;
    private String selectedUserImage;
    private String selectedBlogImage;
    private Blog currentBlog;
    private Runnable onSaveCallback;

    public BlogFormView(BlogController controller, Runnable onSaveCallback) {
        this.controller = controller;
        this.onSaveCallback = onSaveCallback;
        createAndShowStage();
    }

    private void createAndShowStage() {
        stage = new Stage();
        stage.setTitle("Créer un Blog");

        VBox formLayout = createFormLayout();
        formLayout.getStyleClass().add("form-layout");
        formLayout.setPadding(new Insets(20));
        formLayout.setSpacing(15);
        formLayout.setAlignment(Pos.TOP_CENTER);
        formLayout.setMinWidth(400);

        Scene scene = new Scene(formLayout);
        scene.getStylesheets().add(getClass().getResource("/styles/blog.css").toExternalForm());
        
        stage.setScene(scene);
        stage.show();
    }

    private VBox createFormLayout() {
        VBox formLayout = new VBox(15);

        Label titleLabel = new Label("Titre du Blog");
        titleField = new TextField();
        titleField.setPromptText("Entrez le titre (minimum 3 caractères)");
        titleField.getStyleClass().add("form-field");

        Label contentLabel = new Label("Contenu");
        contentArea = new TextArea();
        contentArea.setPromptText("Entrez le contenu (minimum 10 caractères)");
        contentArea.getStyleClass().add("form-field");
        contentArea.setPrefRowCount(5);

        Label creatorLabel = new Label("Nom du Créateur");
        creatorNameField = new TextField();
        creatorNameField.setPromptText("Entrez votre nom (obligatoire)");
        creatorNameField.getStyleClass().add("form-field");

        Button userImageButton = new Button("Choisir Photo de Profil");
        userImageButton.getStyleClass().add("form-button");
        userImageButton.setOnAction(e -> selectUserImage());

        Button blogImageButton = new Button("Choisir Image du Blog");
        blogImageButton.getStyleClass().add("form-button");
        blogImageButton.setOnAction(e -> selectBlogImage());

        Button saveButton = new Button("Enregistrer");
        saveButton.getStyleClass().add("save-button");
        saveButton.setOnAction(e -> saveBlog());

        Button clearButton = new Button("Effacer");
        clearButton.getStyleClass().add("clear-button");
        clearButton.setOnAction(e -> clearForm());

        formLayout.getChildren().addAll(
            titleLabel, titleField,
            contentLabel, contentArea,
            creatorLabel, creatorNameField,
            userImageButton,
            blogImageButton,
            saveButton,
            clearButton
        );

        return formLayout;
    }

    private void selectUserImage() {
        File file = showImageFileChooser();
        if (file != null) {
            selectedUserImage = file.getAbsolutePath();
        }
    }

    private void selectBlogImage() {
        File file = showImageFileChooser();
        if (file != null) {
            selectedBlogImage = file.getAbsolutePath();
        }
    }

    private File showImageFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une Image");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        return fileChooser.showOpenDialog(stage);
    }

    private void saveBlog() {
        String title = titleField.getText();
        String content = contentArea.getText();
        String creatorName = creatorNameField.getText();

        System.out.println("Tentative de sauvegarde du blog:");
        System.out.println("Titre (avant trim): '" + title + "' longueur: " + (title != null ? title.length() : 0));
        System.out.println("Contenu (avant trim): '" + content + "' longueur: " + (content != null ? content.length() : 0));
        System.out.println("Créateur (avant trim): '" + creatorName + "'");

        // Nettoyage des espaces
        title = title != null ? title.trim() : "";
        content = content != null ? content.trim() : "";
        creatorName = creatorName != null ? creatorName.trim() : "";

        System.out.println("Après nettoyage:");
        System.out.println("Titre: '" + title + "' longueur: " + title.length());
        System.out.println("Contenu: '" + content + "' longueur: " + content.length());
        System.out.println("Créateur: '" + creatorName + "'");

        // Validation
        if (title.length() < 3) {
            String message = "Le titre doit contenir au moins 3 caractères (actuel: " + title.length() + ")";
            System.out.println("Erreur de validation: " + message);
            showAlert(message);
            titleField.requestFocus();
            return;
        }

        if (content.length() < 10) {
            String message = "Le contenu doit contenir au moins 10 caractères (actuel: " + content.length() + ")";
            System.out.println("Erreur de validation: " + message);
            showAlert(message);
            contentArea.requestFocus();
            return;
        }

        if (creatorName.isEmpty()) {
            String message = "Le nom du créateur est obligatoire";
            System.out.println("Erreur de validation: " + message);
            showAlert(message);
            creatorNameField.requestFocus();
            return;
        }

        System.out.println("Validation réussie, tentative de sauvegarde...");

        try {
            if (currentBlog != null) {
                // Mode modification
                currentBlog.setTitle(title);
                currentBlog.setContent(content);
                currentBlog.setCreatorName(creatorName);
                currentBlog.setUpdatedAt(LocalDateTime.now());
                
                if (selectedUserImage != null) {
                    File userImageFile = new File(selectedUserImage);
                    currentBlog.setUserImage(ImageUtils.saveImage(userImageFile));
                }
                if (selectedBlogImage != null) {
                    File blogImageFile = new File(selectedBlogImage);
                    currentBlog.setBlogImage(ImageUtils.saveImage(blogImageFile));
                }
                
                controller.updateBlog(currentBlog);
                showSuccessAlert("Blog mis à jour avec succès!");
            } else {
                // Mode création
                Blog newBlog = new Blog(title, content, creatorName);
                
                if (selectedUserImage != null) {
                    File userImageFile = new File(selectedUserImage);
                    newBlog.setUserImage(ImageUtils.saveImage(userImageFile));
                }
                if (selectedBlogImage != null) {
                    File blogImageFile = new File(selectedBlogImage);
                    newBlog.setBlogImage(ImageUtils.saveImage(blogImageFile));
                }
                
                controller.addBlog(newBlog);
                showSuccessAlert("Blog créé avec succès!");
            }

            System.out.println("Sauvegarde réussie!");
            onSaveCallback.run();
            clearForm();
            stage.close();
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde: " + e.getMessage());
            showAlert("Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    private void clearForm() {
        titleField.clear();
        contentArea.clear();
        creatorNameField.clear();
        selectedUserImage = null;
        selectedBlogImage = null;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void show() {
        stage.show();
    }

    public void setCurrentBlog(Blog blog) {
        this.currentBlog = blog;
        stage.setTitle("Modifier le Blog");
        titleField.setText(blog.getTitle());
        contentArea.setText(blog.getContent());
        creatorNameField.setText(blog.getCreatorName());
        selectedUserImage = blog.getUserImage();
        selectedBlogImage = blog.getBlogImage();
    }
}
