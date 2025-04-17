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
        titleField.setPromptText("Entrez le titre");
        titleField.getStyleClass().add("form-field");

        Label contentLabel = new Label("Contenu");
        contentArea = new TextArea();
        contentArea.setPromptText("Entrez le contenu");
        contentArea.getStyleClass().add("form-field");
        contentArea.setPrefRowCount(5);

        Label creatorLabel = new Label("Nom du Créateur");
        creatorNameField = new TextField();
        creatorNameField.setPromptText("Entrez votre nom");
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
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        String creatorName = creatorNameField.getText().trim();

        if (title.isEmpty() || content.isEmpty() || creatorName.isEmpty()) {
            showAlert("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        Blog blog = new Blog(title, content, creatorName);
        blog.setCreatedAt(LocalDateTime.now());

        if (selectedUserImage != null) {
            blog.setUserImage(ImageUtils.saveImage(selectedUserImage));
        }
        if (selectedBlogImage != null) {
            blog.setBlogImage(ImageUtils.saveImage(selectedBlogImage));
        }

        controller.addBlog(blog);
        onSaveCallback.run();
        clearForm();
        stage.close();
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

    public void show() {
        stage.show();
    }

    public void setCurrentBlog(Blog blog) {
        this.currentBlog = blog;
        titleField.setText(blog.getTitle());
        contentArea.setText(blog.getContent());
        creatorNameField.setText(blog.getCreatorName());
        selectedUserImage = blog.getUserImage();
        selectedBlogImage = blog.getBlogImage();
    }
}
