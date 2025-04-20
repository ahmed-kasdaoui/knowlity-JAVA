package view;

import Entities.Blog;
import Services.BlogServices;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import view.BlogFormController;
import view.BlogDetailController;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class BlogListController {
    private BlogServices blogService;
    private boolean isAdmin;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private GridPane blogContainer;

    @FXML
    private Button newBlogButton;

    @FXML
    private TextField searchField;

    @FXML
    private Button logoutButton;

    private ObservableList<Blog> blogs;
    private FilteredList<Blog> filteredBlogs;

    private void addButtonHoverEffect(Button button) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(10);

        button.setOnMouseEntered(e -> button.setEffect(shadow));
        button.setOnMouseExited(e -> button.setEffect(null));
    }

    @FXML
    private void initialize() {
        try {
            blogService = new BlogServices();
            blogs = FXCollections.observableArrayList();

            // Configure grid
            blogContainer.setHgap(20);
            blogContainer.setVgap(20);
            blogContainer.setPadding(new Insets(20));

            // Setup search functionality
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                refreshBlogs();
            });

            // Setup new blog button
            if (newBlogButton != null) {
                newBlogButton.setVisible(isAdmin);
                newBlogButton.setOnAction(e -> handleNewBlog());
                addButtonHoverEffect(newBlogButton);
            }

            // Setup logout button
            if (logoutButton != null) {
                logoutButton.setOnAction(e -> handleLogout());
                addButtonHoverEffect(logoutButton);
            }

            // Load initial blogs
            refreshBlogs();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Could not initialize application", e.getMessage());
        }
    }

    public void setAdminMode(boolean isAdmin) {
        System.out.println("Setting admin mode to: " + isAdmin);
        this.isAdmin = isAdmin;

        if (newBlogButton != null) {
            newBlogButton.setVisible(isAdmin);
            newBlogButton.setManaged(isAdmin);
        }

        // Refresh the blog list to update cards with admin controls
        if (blogContainer != null) {
            System.out.println("Refreshing blog list with admin mode: " + this.isAdmin);
            refreshBlogs();
        }
    }

    @FXML
    private void handleNewBlog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/blog_form.fxml"));
            VBox blogForm = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nouveau Blog");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            
            Scene scene = new Scene(blogForm);
            scene.getStylesheets().add(getClass().getResource("/styles/blog.css").toExternalForm());
            dialogStage.setScene(scene);

            BlogFormController controller = loader.getController();
            controller.setBlog(null); // Indicate this is a new blog
            controller.setOnSaveCallback(this::refreshBlogs);
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();
            refreshBlogs();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de créer un nouveau blog: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/role_selection.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Error", "Could not logout", e.getMessage());
        }
    }

    private void refreshBlogs() {
        blogs.clear();
        List<Blog> loadedBlogs = blogService.getAll();
        blogs.addAll(loadedBlogs);

        // Filter blogs based on search
        String searchText = searchField.getText().toLowerCase();
        List<Blog> filteredList = blogs.filtered(blog ->
            blog.getTitle().toLowerCase().contains(searchText) ||
            blog.getContent().toLowerCase().contains(searchText)
        );

        // Clear existing grid
        blogContainer.getChildren().clear();

        // Add blogs to grid with animation
        int col = 0;
        int row = 0;
        for (Blog blog : filteredList) {
            VBox blogCard = createBlogCard(blog);

            // Add fade-in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), blogCard);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            blogContainer.add(blogCard, col, row);

            col++;
            if (col == 2) { // 2 columns
                col = 0;
                row++;
            }
        }
    }

    private VBox createBlogCard(Blog blog) {
        VBox card = new VBox(15);
        card.getStyleClass().add("blog-card");
        card.setPrefWidth(300);

        // Add click handler with debug print
        card.setOnMouseClicked(e -> {
            System.out.println("Blog clicked: " + blog.getTitle());
            System.out.println("Blog image data length: " + (blog.getBlogImage() != null ? blog.getBlogImage().length() : "null"));
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BlogDetailView.fxml"));
                Parent root = loader.load();

                BlogDetailController controller = loader.getController();
                Stage detailStage = new Stage();
                controller.setStage(detailStage);
                controller.setBlog(blog);

                Scene scene = new Scene(root);
                detailStage.setTitle(blog.getTitle());
                detailStage.setScene(scene);
                detailStage.setMinWidth(800);
                detailStage.setMinHeight(600);
                detailStage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
                showError("Erreur", "Impossible d'afficher les détails du blog: " + ex.getMessage());
            }
        });

        // Blog Image
        if (blog.getBlogImage() != null && !blog.getBlogImage().isEmpty()) {
            try {
                String imageData = blog.getBlogImage();
                if (imageData.contains(",")) {
                    imageData = imageData.split(",")[1];
                }
                byte[] bytes = Base64.getDecoder().decode(imageData.trim());
                Image image = new Image(new ByteArrayInputStream(bytes));

                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(300);
                imageView.setFitHeight(200);
                imageView.setPreserveRatio(true);

                card.getChildren().add(imageView);
            } catch (Exception e) {
                System.err.println("Error loading image for blog card: " + e.getMessage());
            }
        }

        VBox contentBox = new VBox(10);
        // Title
        Label titleLabel = new Label(blog.getTitle());
        titleLabel.getStyleClass().add("blog-detail-title");
        titleLabel.setWrapText(true);

        // Creator info
        // Actions buttons (Edit/Delete)
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(0, 10, 0, 10));

        if (isAdmin) {
            Button editButton = new Button();
            editButton.getStyleClass().addAll("action-button", "edit-button");
            FontIcon editIcon = new FontIcon(FontAwesomeSolid.EDIT);
            editIcon.setIconColor(Color.WHITE);
            editButton.setGraphic(editIcon);
            editButton.setOnAction(e -> {
                e.consume(); // Prevent event bubbling to card click
                handleEditBlog(blog);
            });

            Button deleteButton = new Button();
            deleteButton.getStyleClass().addAll("action-button", "delete-button");
            FontIcon deleteIcon = new FontIcon(FontAwesomeSolid.TRASH);
            deleteIcon.setIconColor(Color.WHITE);
            deleteButton.setGraphic(deleteIcon);
            deleteButton.setOnAction(e -> {
                e.consume(); // Prevent event bubbling to card click
                handleDeleteBlog(blog);
            });

            actionBox.getChildren().addAll(editButton, deleteButton);
        }

        // Creator info
        HBox creatorBox = new HBox(10);
        creatorBox.getStyleClass().add("creator-box");
        creatorBox.setAlignment(Pos.CENTER_LEFT);


        // Creator avatar
        ImageView avatar = new ImageView();
        if (blog.getUserImage() != null && !blog.getUserImage().isEmpty()) {
            try {
                avatar.setImage(new Image(blog.getUserImage()));
            } catch (Exception e) {
                avatar.setImage(new Image(getClass().getResource("/images/default-avatar.png").toExternalForm()));
            }
        } else {
            avatar.setImage(new Image(getClass().getResource("/images/default-avatar.png").toExternalForm()));
        }
        avatar.setFitWidth(30);
        avatar.setFitHeight(30);
        avatar.setPreserveRatio(true);
        avatar.getStyleClass().add("creator-avatar");

        VBox creatorInfo = new VBox(2);
        Label creatorLabel = new Label(blog.getCreatorName());
        creatorLabel.getStyleClass().add("creator-name");

        Label dateLabel = new Label(blog.getCreatedAt() != null ?
            blog.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "");
        dateLabel.getStyleClass().add("blog-date");

        creatorInfo.getChildren().addAll(creatorLabel, dateLabel);
        creatorBox.getChildren().addAll(avatar, creatorInfo);

        // Content preview
        Label contentLabel = new Label(truncateContent(blog.getContent()));
        contentLabel.getStyleClass().add("blog-preview");
        contentLabel.setWrapText(true);

        // Create title row with action buttons
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Action buttons for admin
        System.out.println("Creating blog card with admin mode: " + isAdmin);
        if (isAdmin) {
            System.out.println("Adding admin buttons to blog card");
            HBox actions = new HBox(10);
            actions.getStyleClass().add("action-buttons");
            actions.setAlignment(Pos.CENTER_RIGHT);

            Button editButton = new Button();
            editButton.getStyleClass().addAll("action-button", "edit-button");
            FontIcon editIcon = new FontIcon(FontAwesomeSolid.EDIT);
            editIcon.setIconSize(16);
            editIcon.setIconColor(javafx.scene.paint.Color.WHITE);
            editButton.setGraphic(editIcon);
            editButton.setTooltip(new Tooltip("Modifier"));

            Button deleteButton = new Button();
            deleteButton.getStyleClass().addAll("action-button", "delete-button");
            FontIcon deleteIcon = new FontIcon(FontAwesomeSolid.TRASH);
            deleteIcon.setIconSize(16);
            deleteIcon.setIconColor(javafx.scene.paint.Color.WHITE);
            deleteButton.setGraphic(deleteIcon);
            deleteButton.setTooltip(new Tooltip("Supprimer"));

            editButton.setOnAction(e -> handleEditBlog(blog));
            deleteButton.setOnAction(e -> handleDeleteBlog(blog));

            actions.getChildren().addAll(editButton, deleteButton);
            titleRow.getChildren().addAll(titleLabel, actions);
        } else {
            titleRow.getChildren().add(titleLabel);
        }

        contentBox.getChildren().addAll(titleRow, creatorBox, contentLabel);

        card.getChildren().add(contentBox);

        // Add hover effect
        card.setOnMouseEntered(e -> card.getStyleClass().add("blog-card-hover"));
        card.setOnMouseExited(e -> card.getStyleClass().remove("blog-card-hover"));

        return card;
    }

    private String truncateContent(String content) {
        final int maxLength = 100;
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }

    private void handleEditBlog(Blog blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/blog_form.fxml"));
            Parent root = loader.load();

            BlogFormController controller = loader.getController();
            controller.setBlog(blog);
            controller.setOnSaveCallback(this::refreshBlogs);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Blog");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            
            // Set the dialog stage in the controller
            controller.setDialogStage(dialogStage);
            
            dialogStage.show();
        } catch (IOException e) {
            showError("Error", "Could not open edit form", e.getMessage());
        }
    }

    private void handleDeleteBlog(Blog blog) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce blog ?");
        alert.setContentText("Cette action ne peut pas être annulée.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                blogService.delete(blog);
                refreshBlogs();
            } catch (Exception e) {
                showError("Error", "Could not delete blog", e.getMessage());
            }
        }
    }

    private void showError(String title, String content, String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.setContentText(errorMessage != null ? errorMessage : "");
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        showError(title, content, null);
    }

    private void showBlogDetail(Blog blog) {
        try {
            // Create a dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Détail du Blog");
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles/blog.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("blog-detail-dialog");

            // Create the content
            VBox content = new VBox();
            content.getStyleClass().add("blog-detail-content-container");
            content.setPrefWidth(600);
            content.setMaxWidth(600);

            // Blog image
            if (blog.getBlogImage() != null && !blog.getBlogImage().isEmpty()) {
                try {
                    ImageView blogImage = new ImageView();
                    if (blog.getBlogImage().startsWith("data:image")) {
                        // Handle Base64 image
                        String base64Image = blog.getBlogImage().split(",")[1];
                        byte[] imageData = Base64.getDecoder().decode(base64Image);
                        blogImage.setImage(new Image(new ByteArrayInputStream(imageData)));
                    } else {
                        // Handle URL image
                        blogImage.setImage(new Image(blog.getBlogImage()));
                    }
                    blogImage.setFitWidth(600);
                    blogImage.setPreserveRatio(true);
                    blogImage.getStyleClass().add("blog-detail-image");
                    content.getChildren().add(blogImage);
                } catch (Exception e) {
                    System.err.println("Error loading blog image: " + e.getMessage());
                }
            }

            // Title
            Label titleLabel = new Label(blog.getTitle());
            titleLabel.getStyleClass().add("blog-detail-title");
            titleLabel.setWrapText(true);

            // Creator info
            HBox creatorBox = new HBox(10);
            creatorBox.getStyleClass().add("creator-box");
            creatorBox.setAlignment(Pos.CENTER_LEFT);

            ImageView avatar = new ImageView();
            if (blog.getUserImage() != null && !blog.getUserImage().isEmpty()) {
                try {
                    if (blog.getUserImage().startsWith("data:image")) {
                        // Handle Base64 image
                        String base64Image = blog.getUserImage().split(",")[1];
                        byte[] imageData = Base64.getDecoder().decode(base64Image);
                        avatar.setImage(new Image(new ByteArrayInputStream(imageData)));
                    } else {
                        // Handle URL image
                        avatar.setImage(new Image(blog.getUserImage()));

                    }
                } catch (Exception e) {
                    avatar.setImage(new Image(getClass().getResource("/images/default-avatar.png").toExternalForm()));
                }
            } else {
                avatar.setImage(new Image(getClass().getResource("/images/default-avatar.png").toExternalForm()));
            }
            avatar.setFitWidth(40);
            avatar.setFitHeight(40);
            avatar.setPreserveRatio(true);
            avatar.getStyleClass().add("creator-avatar");

            VBox creatorInfo = new VBox(2);
            Label creatorLabel = new Label(blog.getCreatorName());
            creatorLabel.getStyleClass().add("creator-name");

            Label dateLabel = new Label(blog.getCreatedAt() != null ?
                blog.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) : "");
            dateLabel.getStyleClass().add("blog-date");

            creatorInfo.getChildren().addAll(creatorLabel, dateLabel);
            creatorBox.getChildren().addAll(avatar, creatorInfo);

            // Content in ScrollPane
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefViewportHeight(300);
            scrollPane.getStyleClass().add("blog-detail-scroll");

            Label contentLabel = new Label(blog.getContent());
            contentLabel.getStyleClass().add("blog-detail-content");
            contentLabel.setWrapText(true);

            scrollPane.setContent(contentLabel);

            // Add all to content
            content.getChildren().addAll(titleLabel, creatorBox, scrollPane);

            // Set dialog content
            dialog.getDialogPane().setContent(content);

            // Add close button
            ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(closeButton);

            // Add CSS class to close button
            Button closeButtonNode = (Button) dialog.getDialogPane().lookupButton(closeButton);
            closeButtonNode.getStyleClass().add("close-button");

            // Show dialog
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'afficher les détails du blog");
        }
    }

    private void handleBlogClick(Blog blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BlogDetailView.fxml"));
            Parent root = loader.load();

            BlogDetailController controller = loader.getController();
            Stage detailStage = new Stage();
            controller.setStage(detailStage);
            controller.setBlog(blog);

            Scene scene = new Scene(root);
            detailStage.setTitle(blog.getTitle());
            detailStage.setScene(scene);
            detailStage.setMinWidth(800);
            detailStage.setMinHeight(600);
            detailStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'afficher les détails du blog");
        }
    }
}
