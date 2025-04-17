package view;

import Entities.Blog;
import Services.BlogServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tooltip;

public class BlogListController {
    private BlogServices blogService;
    private boolean isAdmin;
    private GridPane blogContainer;
    
    @FXML
    private ScrollPane scrollPane;
    
    @FXML
    private Button newBlogButton;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button logoutButton;

    @FXML
    private void initialize() {
        try {
            // Initialize blog service
            blogService = new BlogServices();
            
            // Initialize the blog container as a GridPane
            blogContainer = new GridPane();
            blogContainer.setId("blogContainer");
            
            // Make the scroll pane transparent and add the grid
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            scrollPane.setContent(blogContainer);
            scrollPane.setFitToWidth(true);
            
            // Add CSS
            String css = getClass().getResource("/styles/blog.css").toExternalForm();
            blogContainer.getStylesheets().add(css);
            
            // Add search field listener
            if (searchField != null) {
                searchField.textProperty().addListener((obs, oldText, newText) -> refreshBlogGrid());
            }
            
            // Load blogs
            refreshBlogGrid();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'initialiser l'interface: " + e.getMessage());
        }
    }
    
    public void setAdminMode(boolean isAdmin) {
        this.isAdmin = isAdmin;
        System.out.println("Admin mode set to: " + isAdmin);
        
        if (newBlogButton != null) {
            newBlogButton.setVisible(isAdmin);
            newBlogButton.setManaged(isAdmin);
        }
        
        refreshBlogGrid();
    }

    @FXML
    private void handleNewBlog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/blog_form.fxml"));
            VBox blogForm = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nouveau Blog");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            
            Scene scene = new Scene(blogForm);
            dialogStage.setScene(scene);
            
            BlogFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
            
            refreshBlogGrid();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de créer un nouveau blog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/role_selection.fxml"));
            VBox roleSelection = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(roleSelection));
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de se déconnecter: " + e.getMessage());
        }
    }

    private void refreshBlogGrid() {
        try {
            GridPane blogContainer = (GridPane) scrollPane.getContent();
            blogContainer.getChildren().clear();
            
            List<Blog> blogs = blogService.getAll();
            
            int column = 0;
            int row = 0;
            int maxColumns = 3; // Nombre de colonnes dans la grille
            
            for (Blog blog : blogs) {
                if (matchesSearch(blog)) {
                    VBox card = createBlogCard(blog);
                    
                    // Add fade-in animation
                    card.setOpacity(0);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), card);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.setDelay(Duration.millis((column + row * maxColumns) * 100)); // Délai progressif
                    fadeIn.play();
                    
                    blogContainer.add(card, column, row);
                    
                    column++;
                    if (column >= maxColumns) {
                        column = 0;
                        row++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la liste des blogs: " + e.getMessage());
        }
    }
    
    private boolean matchesSearch(Blog blog) {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) return true;
        
        return blog.getTitle().toLowerCase().contains(searchText) ||
               blog.getContent().toLowerCase().contains(searchText) ||
               blog.getCreatorName().toLowerCase().contains(searchText);
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
        contentBox.getStyleClass().add("blog-content-box");
        contentBox.setPadding(new Insets(15));
        
        // Title
        Label titleLabel = new Label(blog.getTitle());
        titleLabel.getStyleClass().add("blog-title");
        titleLabel.setWrapText(true);
        
        // Creator info with avatar
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
        
        contentBox.getChildren().addAll(titleLabel, creatorBox, contentLabel);
        
        // Action buttons for admin
        if (isAdmin) {
            HBox actions = new HBox(10);
            actions.getStyleClass().add("action-buttons");
            actions.setAlignment(Pos.CENTER_RIGHT);
            
            Button editButton = new Button();
            editButton.getStyleClass().addAll("icon-button", "edit-button");
            FontIcon editIcon = new FontIcon(FontAwesomeSolid.EDIT);
            editIcon.setIconSize(16);
            editIcon.setIconColor(javafx.scene.paint.Color.WHITE);
            editButton.setGraphic(editIcon);
            editButton.setTooltip(new Tooltip("Modifier"));
            
            Button deleteButton = new Button();
            deleteButton.getStyleClass().addAll("icon-button", "delete-button");
            FontIcon deleteIcon = new FontIcon(FontAwesomeSolid.TRASH);
            deleteIcon.setIconSize(16);
            deleteIcon.setIconColor(javafx.scene.paint.Color.WHITE);
            deleteButton.setGraphic(deleteIcon);
            deleteButton.setTooltip(new Tooltip("Supprimer"));
            
            editButton.setOnAction(e -> handleEditBlog(blog));
            deleteButton.setOnAction(e -> handleDeleteBlog(blog));
            
            actions.getChildren().addAll(editButton, deleteButton);
            contentBox.getChildren().add(actions);
        }
        
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
            VBox blogForm = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Blog");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            
            Scene scene = new Scene(blogForm);
            dialogStage.setScene(scene);
            
            BlogFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setBlog(blog);
            
            dialogStage.showAndWait();
            
            refreshBlogGrid();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de modifier le blog: " + e.getMessage());
        }
    }
    
    private void handleDeleteBlog(Blog blog) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le blog");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce blog ?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                blogService.delete(blog);
                refreshBlogGrid();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de supprimer le blog: " + e.getMessage());
            }
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
