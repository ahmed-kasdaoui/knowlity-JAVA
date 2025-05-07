package view;

import Entities.Blog;
import Entities.Rating;
import Services.BlogServices;
import Services.RatingService;
import Services.ReportService;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import tn.knowlity.tools.UserSessionManager;
import view.BlogFormController;
import view.BlogDetailController;
import tn.knowlity.entity.User;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Hyperlink;
import javafx.scene.effect.DropShadow;
import Entities.Creator;
import Services.CreatorService;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.sql.SQLException;
import java.util.stream.Collectors;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import java.net.URL;
import java.io.File;

public class BlogListController {

    private BlogServices blogService;
    private RatingService ratingService;
    private boolean isAdmin;
    private User currentUser = UserSessionManager.getInstance().getCurrentUser();
    private final int DEFAULT_USER_ID = currentUser.getId();

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private GridPane blogContainer;

    @FXML
    private Button newBlogButton;

    @FXML
    private TextField searchField;

    @FXML
    private Button adminLogoutButton;
    @FXML
    private Button userLogoutButton;
    @FXML
    private HBox userNavbar;

    @FXML
    private HBox adminNavbar;

    @FXML
    private Button notificationButton;

    private ReportService reportService;

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
            ratingService = new RatingService();
            reportService = new ReportService();
            blogs = FXCollections.observableArrayList();

            // Configure scroll pane
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

            // Configure blog container
            blogContainer.setAlignment(Pos.CENTER);
            blogContainer.setHgap(30);
            blogContainer.setVgap(30);
            blogContainer.setPadding(new Insets(40));
            blogContainer.setMaxWidth(1200); // Limite la largeur maximale
            blogContainer.setStyle("-fx-background-color: transparent;");

            // Center the container
            StackPane centeringPane = new StackPane(blogContainer);
            centeringPane.setStyle("-fx-background-color: transparent;");
            scrollPane.setContent(centeringPane);

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                refreshBlogsAndHandleErrors();
            });

            // Setup new blog button
            if (newBlogButton != null) {
                newBlogButton.setVisible(isAdmin);
                newBlogButton.setOnAction(e -> handleNewBlog());
                addButtonHoverEffect(newBlogButton);
            }

            // Setup logout buttons
            if (adminLogoutButton != null) {
                adminLogoutButton.setOnAction(e -> handleLogout());
                addButtonHoverEffect(adminLogoutButton);
            }
            if (userLogoutButton != null) {
                userLogoutButton.setOnAction(e -> handleLogout());
                addButtonHoverEffect(userLogoutButton);
            }

            // Setup notification button for admin
            setupNotificationButton();

            // Load initial blogs
            refreshBlogsAndHandleErrors();

            // Add scene listener to apply CSS when scene is available
            scrollPane.sceneProperty().addListener((observable, oldValue, newScene) -> {
                if (newScene != null) {
                    applyStyles(newScene);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Could not initialize application", e.getMessage());
        }
    }

    private void applyStyles(Scene scene) {
        try {
            String cssPath = BlogListController.class.getResource("/styles/blog-list.css").toExternalForm();
            if (cssPath == null) {
                System.err.println("Could not find CSS file at /styles/blog-list.css");
                return;
            }
            scene.getStylesheets().add(cssPath);
            System.out.println("Successfully loaded CSS from: " + cssPath);
        } catch (Exception e) {
            System.err.println("Failed to load CSS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupNotificationButton() {
        if (notificationButton != null) {
            FontAwesomeIconView bellIcon = new FontAwesomeIconView(FontAwesomeIcon.BELL);
            bellIcon.setFill(Color.WHITE);
            notificationButton.setGraphic(bellIcon);
            
            notificationButton.setOnAction(e -> showReportedBlogs());
            notificationButton.setVisible(false);
            
            // Check for reported blogs periodically
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> checkReportedBlogs()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }
    }

    private void checkReportedBlogs() {
        if (!isAdmin) return;

        List<Blog> reportedBlogs = blogService.getAll().stream()
            .filter(blog -> {
                try {
                    return reportService.countUniqueReports(blog.getId()) > 0;
                } catch (SQLException ex) {
                    return false;
                }
            })
            .collect(Collectors.toList());

        Platform.runLater(() -> {
            if (notificationButton != null) {
                notificationButton.setVisible(!reportedBlogs.isEmpty());
                if (!reportedBlogs.isEmpty()) {
                    FontAwesomeIconView bellIcon = (FontAwesomeIconView) notificationButton.getGraphic();
                    bellIcon.setFill(Color.RED);
                }
            }
        });
    }

    private void showReportedBlogs() {
        List<Blog> reportedBlogs = blogService.getAll().stream()
            .filter(blog -> {
                try {
                    return reportService.countUniqueReports(blog.getId()) > 0;
                } catch (SQLException ex) {
                    return false;
                }
            })
            .collect(Collectors.toList());

        if (reportedBlogs.isEmpty()) {
            showInfo("Aucun signalement", "Il n'y a aucun blog signalé pour le moment.");
            return;
        }

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        for (Blog blog : reportedBlogs) {
            try {
                int reportCount = reportService.countUniqueReports(blog.getId());
                HBox blogBox = new HBox(10);
                blogBox.setAlignment(Pos.CENTER_LEFT);

                Label titleLabel = new Label(blog.getTitle());
                Label reportCountLabel = new Label(reportCount + " signalement(s)");
                reportCountLabel.setTextFill(Color.RED);

                Button viewButton = new Button("Voir");
                viewButton.setOnAction(e -> openBlogDetail(blog));

                blogBox.getChildren().addAll(titleLabel, reportCountLabel, viewButton);
                content.getChildren().add(blogBox);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Blogs signalés");
        dialog.setScene(new Scene(scrollPane));
        dialog.showAndWait();

    }

    public void setAdminMode(boolean isAdmin) {
        this.isAdmin = isAdmin;
        if (newBlogButton != null) {
            newBlogButton.setVisible(isAdmin);
        }
        if (notificationButton != null) {
            notificationButton.setVisible(isAdmin);
            checkReportedBlogs();
        }
        if (adminNavbar != null && userNavbar != null) {
            adminNavbar.setVisible(isAdmin);
            adminNavbar.setManaged(isAdmin);
            userNavbar.setVisible(!isAdmin);
            userNavbar.setManaged(!isAdmin);
        }
        // Refresh blogs to update admin controls
        refreshBlogsAndHandleErrors();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
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
            // Wrap refreshBlogs in a lambda to handle SQLException
            controller.setOnSaveCallback(() -> {
                try {
                    refreshBlogs();
                } catch (SQLException e) {
                    showError("Erreur", "Impossible de rafraîchir les blogs", e.getMessage());
                }
            });
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();
            refreshBlogsAndHandleErrors();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de créer un nouveau blog", e.getMessage());
        }
    }

    @FXML
    private void handleCreatorsNav(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/creator_list.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la liste des créateurs");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/role_selection.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) (isAdmin ? adminLogoutButton.getScene().getWindow() : userLogoutButton.getScene().getWindow());
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de retourner à la sélection des rôles");
        }
    }

    private void refreshBlogsAndHandleErrors() {
        try {
            refreshBlogs();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de mettre à jour la liste des blogs", e.getMessage());
        }
    }

    private void refreshBlogs() throws SQLException {
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
            Node card = createBlogCard(blog);

            // Add fade-in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), card);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            blogContainer.add(card, col, row);

            col++;
            if (col == 2) { // 2 columns
                col = 0;
                row++;
            }
        }
    }

    private ImageView loadBlogImage(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            return createDefaultImageView();
        }

        try {
            Image image = null;
            
            // Si c'est une chaîne base64, on la décode
            if (imageData.startsWith("data:")) {
                try {
                    int commaIndex = imageData.indexOf(",");
                    if (commaIndex > -1) {
                        imageData = imageData.substring(commaIndex + 1);
                    }
                    byte[] bytes = Base64.getDecoder().decode(imageData);
                    image = new Image(new ByteArrayInputStream(bytes), 500, 200, true, true);
                } catch (Exception e) {
                    System.err.println("Failed to decode base64 image: " + e.getMessage());
                }
            } else {
                // Sinon, on essaie de charger comme une ressource ou un fichier
                try {
                    String imagePath = "/images/" + imageData;
                    URL imageUrl = getClass().getResource(imagePath);
                    if (imageUrl != null) {
                        image = new Image(imageUrl.toExternalForm(), 500, 200, true, true);
                    } else {
                        // Si ce n'est pas une ressource, essayer comme chemin de fichier direct
                        File file = new File(imageData);
                        if (file.exists()) {
                            image = new Image(file.toURI().toString(), 500, 200, true, true);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load image from path: " + e.getMessage());
                }
            }

            if (image != null && !image.isError()) {
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(500);
                imageView.setFitHeight(200);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setCache(true);
                return imageView;
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
        
        return createDefaultImageView();
    }

    private ImageView createDefaultImageView() {
        Rectangle defaultBg = new Rectangle(500, 200);
        defaultBg.setFill(Color.valueOf("#f0f0f0"));
        ImageView imageView = new ImageView();
        imageView.setFitWidth(500);
        imageView.setFitHeight(200);
        return imageView;
    }

    private void addDefaultBackground(StackPane imageContainer) {
        Rectangle defaultBg = new Rectangle(500, 200);
        defaultBg.setFill(Color.valueOf("#f0f0f0"));
        
        // Add a placeholder icon
        FontAwesomeIconView imageIcon = new FontAwesomeIconView(FontAwesomeIcon.IMAGE);
        imageIcon.setFill(Color.valueOf("#bdc3c7"));
        imageIcon.setSize("50");
        
        imageContainer.getChildren().addAll(defaultBg, imageIcon);
    }

    private Node createBlogCard(Blog blog) {
        VBox card = new VBox(15);
        card.getStyleClass().add("blog-card");
        card.setPrefWidth(500);
        
        // Add scale transition for hover effect
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), card);
        card.setOnMouseEntered(e -> {
            scaleTransition.setToX(1.03);
            scaleTransition.setToY(1.03);
            scaleTransition.play();
            card.getStyleClass().add("blog-card-hover");
        });
        card.setOnMouseExited(e -> {
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
            card.getStyleClass().remove("blog-card-hover");
        });

        // Add fade-in animation when card is created
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        // Image container with gradient overlay
        StackPane imageContainer = new StackPane();
        imageContainer.setMinHeight(200);
        imageContainer.getStyleClass().add("blog-image-container");
        
        if (blog.getBlogImage() != null && !blog.getBlogImage().isEmpty()) {
            try {
                ImageView imageView = loadBlogImage(blog.getBlogImage());
                if (imageView != null) {
                    imageView.getStyleClass().add("blog-image");
                    imageContainer.getChildren().add(imageView);
                } else {
                    addDefaultBackground(imageContainer);
                }
            } catch (Exception e) {
                System.err.println("Failed to load blog image: " + e.getMessage());
                addDefaultBackground(imageContainer);
            }
        } else {
            addDefaultBackground(imageContainer);
        }
        
        // Content container
        VBox contentBox = new VBox(15);
        contentBox.getStyleClass().add("blog-content");
        contentBox.setPadding(new Insets(20));
        
        // Title with hover effect
        Label titleLabel = new Label(blog.getTitle());
        titleLabel.getStyleClass().add("blog-title");
        titleLabel.setWrapText(true);
        
        // Creator info with avatar
        HBox creatorBox = new HBox(10);
        creatorBox.getStyleClass().add("creator-box");
        creatorBox.setAlignment(Pos.CENTER_LEFT);
        
        Circle avatarCircle = new Circle(20);
        avatarCircle.getStyleClass().add("creator-avatar");
        
        VBox creatorInfo = new VBox(5);
        Label creatorName = new Label(blog.getCreatorName() != null ? blog.getCreatorName() : "Anonymous");
        creatorName.getStyleClass().add("creator-name");
        
        Label dateLabel = new Label(blog.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dateLabel.getStyleClass().add("blog-date");
        
        creatorInfo.getChildren().addAll(creatorName, dateLabel);
        creatorBox.getChildren().addAll(avatarCircle, creatorInfo);
        
        // Preview text with gradient fade
        Label previewLabel = new Label(truncateContent(blog.getContent()));
        previewLabel.getStyleClass().add("blog-preview");
        previewLabel.setWrapText(true);
        
        // Rating display
        HBox ratingBox = new HBox(5);
        ratingBox.getStyleClass().add("rating-container");
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        
        try {
            double avgRating = ratingService.getAverageRating(blog.getId());
            Label ratingLabel = new Label(String.format("%.1f", avgRating));
            ratingLabel.getStyleClass().add("rating-score");
            
            HBox starsBox = new HBox(2);
            for (int i = 1; i <= 5; i++) {
                Label star = new Label("★");
                star.getStyleClass().add(i <= avgRating ? "star-filled" : "star-empty");
                starsBox.getChildren().add(star);
            }
            
            ratingBox.getChildren().addAll(ratingLabel, starsBox);
        } catch (Exception e) {
            Label noRating = new Label("Pas encore d'évaluation");
            noRating.getStyleClass().add("no-rating");
            ratingBox.getChildren().add(noRating);
        }
        
        // Action buttons with hover effects
        HBox actionButtons = new HBox(10);
        actionButtons.getStyleClass().add("action-buttons");
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        
        if (isAdmin) {
            Button editButton = new Button("", new FontAwesomeIconView(FontAwesomeIcon.EDIT));
            editButton.getStyleClass().addAll("action-button", "edit-button");
            
            Button deleteButton = new Button("", new FontAwesomeIconView(FontAwesomeIcon.TRASH));
            deleteButton.getStyleClass().addAll("action-button", "delete-button");
            
            editButton.setOnAction(e -> handleEditBlog(blog));
            deleteButton.setOnAction(e -> handleDeleteBlog(blog));
            
            actionButtons.getChildren().addAll(editButton, deleteButton);
        }
        
        // Add all elements to content box
        contentBox.getChildren().addAll(
            titleLabel,
            creatorBox,
            previewLabel,
            ratingBox
        );
        
        if (!actionButtons.getChildren().isEmpty()) {
            contentBox.getChildren().add(actionButtons);
        }
        
        // Add all containers to card
        card.getChildren().addAll(imageContainer, contentBox);
        
        // Make card clickable
        card.setOnMouseClicked(e -> openBlogDetail(blog));
        
        return card;
    }

    private String truncateContent(String content) {
        final int maxLength = 150;
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }

    private void handleEditBlog(Blog blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/blog_form.fxml"));
            Parent root = loader.load();

            BlogFormController controller = loader.getController();
            controller.setBlog(blog);
            // Wrap refreshBlogs in a lambda to handle SQLException
            controller.setOnSaveCallback(() -> {
                try {
                    refreshBlogs();
                } catch (SQLException e) {
                    showError("Erreur", "Impossible de rafraîchir les blogs", e.getMessage());
                }
            });

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Blog");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            // Set the dialog stage in the controller
            controller.setDialogStage(dialogStage);

            dialogStage.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire de modification", e.getMessage());
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
                showError("Erreur", "Impossible de supprimer le blog", e.getMessage());
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

    private void showCreatorProfile(String creatorName) {
        try {
            CreatorService creatorService = new CreatorService();
            Optional<Creator> creatorOpt = creatorService.getAll().stream()
                    .filter(c -> c.getName().equals(creatorName))
                    .findFirst();

            if (creatorOpt.isPresent()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/creator_profile.fxml"));
                Parent root = loader.load();

                CreatorProfileController controller = loader.getController();
                controller.setCreator(creatorOpt.get());

                Stage stage = new Stage();
                stage.setTitle("Profil de " + creatorName);
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                showError("Erreur", "Créateur non trouvé");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'afficher le profil du créateur");
        }
    }

    private void showError(String title, String content) {
        showError(title, content, null);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openBlogDetail(Blog blog) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BlogDetailView.fxml"));
            Parent root = loader.load();

            BlogDetailController controller = loader.getController();
            Stage detailStage = new Stage();
            controller.setStage(detailStage);
            controller.setAdminMode(isAdmin);
            controller.setCurrentUser(currentUser);
            controller.setBlog(blog);

            Scene scene = new Scene(root);
            detailStage.setTitle(blog.getTitle());
            detailStage.setScene(scene);
            detailStage.setMinWidth(800);
            detailStage.setMinHeight(600);
            detailStage.show();

            // Refresh the list when the detail window is closed
            detailStage.setOnHidden(event -> refreshBlogsAndHandleErrors());
        } catch (IOException ex) {
            ex.printStackTrace();
            showError("Erreur", "Impossible d'afficher les détails du blog: " + ex.getMessage());
        }
    }
}