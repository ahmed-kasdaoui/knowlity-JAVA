package view;

import Entities.*;
import Services.BlogServices;
import Services.RatingService;
import Services.CommentService;
import Services.ReportService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import tn.knowlity.entity.User;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.Base64;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public class BlogDetailController {
    private User currentUser;
    @FXML
    private Label titleLabel;

    @FXML
    private Label creatorLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label averageRatingLabel;

    @FXML
    private ImageView blogImage;

    @FXML
    private TextFlow contentFlow;

    @FXML
    private VBox imageContainer;

    @FXML
    private Slider ratingSlider;

    @FXML
    private Button submitRatingButton;

    @FXML
    private VBox ratingContainer;

    @FXML
    private VBox statsContainer;

    @FXML
    private VBox commentsContainer;

    @FXML
    private TextArea commentInput;

    @FXML
    private Button submitCommentButton;

    private Stage stage;
    private Blog currentBlog;
    private RatingService ratingService;
    private BlogServices blogService;
    private CommentService commentService;
    private ReportService reportService;
    @FXML
    private Button reportButton;
    private boolean isAdmin;

    public BlogDetailController() {
        ratingService = new RatingService();
        commentService = new CommentService();
        reportService = new ReportService();
    }

    @FXML
    public void initialize() {
        ratingService = new RatingService();
        blogService = new BlogServices();
        commentService = new CommentService();

        // Configurer le bouton de soumission des commentaires
        if (submitCommentButton != null) {
            submitCommentButton.setOnAction(e -> {
                try {
                    handleSubmitComment();
                } catch (SQLException ex) {
                    showError("Erreur", "Une erreur est survenue : " + ex.getMessage());
                }
            });
        }
    }

    private void handleSubmitComment() throws SQLException {
        if (currentUser == null) {
            showError("Erreur", "Vous devez être connecté pour commenter");
            return;
        }

        String content = commentInput.getText().trim();
        if (content.isEmpty()) {
            showError("Erreur", "Le commentaire ne peut pas être vide");
            return;
        }

        try {
            Comment comment = new Comment(content, currentUser.getNom() + " " + currentUser.getPrenom(), currentBlog.getId());
            commentService.add(comment);
            commentInput.clear();
            loadComments(); // Recharger les commentaires
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'ajouter le commentaire : " + e.getMessage());
        }
    }

    private void loadComments() throws SQLException {
        try {
            List<Comment> comments = commentService.getByBlogId(currentBlog.getId());
            commentsContainer.getChildren().clear();

            for (Comment comment : comments) {
                VBox commentBox = createCommentBox(comment);
                commentsContainer.getChildren().add(commentBox);
            }
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les commentaires : " + e.getMessage());
        }
    }

    private VBox createCommentBox(Comment comment) {
        VBox commentBox = new VBox(5);
        commentBox.getStyleClass().add("comment-box");
        commentBox.setPadding(new Insets(10));
        commentBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // En-tête du commentaire (utilisateur et date)
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar de l'utilisateur (icône par défaut)
        FontAwesomeIconView userIcon = new FontAwesomeIconView(FontAwesomeIcon.USER_CIRCLE);
        userIcon.setFill(Color.web("#3498DB"));
        userIcon.setSize("24");

        VBox userInfo = new VBox(2);
        Label usernameLabel = new Label(comment.getUsername());
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50; -fx-font-size: 14px;");

        Label dateLabel = new Label(comment.getCreatedAt().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        ));
        dateLabel.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 12px;");

        userInfo.getChildren().addAll(usernameLabel, dateLabel);
        header.getChildren().addAll(userIcon, userInfo);

        // Contenu du commentaire
        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: #34495E; -fx-font-size: 14px; -fx-padding: 5 0 0 0;");

        VBox contentBox = new VBox(5);
        contentBox.getChildren().add(contentLabel);
        contentBox.setPadding(new Insets(5, 0, 0, 34)); // Aligner avec l'avatar

        commentBox.getChildren().addAll(header, contentBox);

        // Bouton de suppression pour l'admin
        if (isAdmin) {
            Button deleteButton = new Button();
            deleteButton.getStyleClass().add("delete-comment-button");
            deleteButton.setStyle("-fx-background-color: transparent;");

            FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
            deleteIcon.setFill(Color.web("#E74C3C"));
            deleteIcon.setSize("16");
            deleteButton.setGraphic(deleteIcon);

            deleteButton.setOnAction(e -> {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("Supprimer le commentaire");
                alert.setContentText("Voulez-vous vraiment supprimer ce commentaire ?");

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            handleDeleteComment(comment.getId());
                        } catch (SQLException ex) {
                            showError("Erreur", "Impossible de supprimer le commentaire : " + ex.getMessage());
                        }
                    }
                });
            });

            HBox buttonBox = new HBox(deleteButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(5, 0, 0, 0));
            commentBox.getChildren().add(buttonBox);
        }

        return commentBox;
    }

    private void handleDeleteComment(int commentId) throws SQLException {
        try {
            commentService.delete(commentId);
            loadComments(); // Recharger les commentaires
        } catch (SQLException e) {
            showError("Erreur", "Impossible de supprimer le commentaire : " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadStats() throws SQLException {
        // Load average rating
        double averageRating = ratingService.getAverageRating(currentBlog.getId());
        int totalRatings = ratingService.getTotalRatings(currentBlog.getId());
        
        if (totalRatings > 0) {
            averageRatingLabel.setText(String.format("%.1f/5 (%d avis)", averageRating, totalRatings));
        } else {
            averageRatingLabel.setText("Aucun avis");
        }

        // Add click handler to show detailed statistics
        statsContainer.setOnMouseClicked(e -> showDetailedStatistics());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setAdminMode(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setBlog(Blog blog) {
        this.currentBlog = blog;

        // Mettre à jour l'UI d'abord
        titleLabel.setText(blog.getTitle());
        creatorLabel.setText("Par " + blog.getCreatorName());

        // Afficher le contenu du blog
        Text contentText = new Text(blog.getContent());
        contentFlow.getChildren().clear();
        contentFlow.getChildren().add(contentText);

        // Charger l'image
        loadBlogImage(blog);

        try {
            // Configurer la section de notation et mettre à jour l'affichage
            setupRatingSection();
            updateAverageRating();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger la section de notation : " + e.getMessage());
        }

        // Afficher les statistiques si admin
        if (isAdmin) {
            showDetailedStatistics();
        }

        // Configuration du bouton de signalement
        setupReportButton();
    }

    private void setupRatingSection() throws SQLException {
        if (ratingContainer == null || ratingSlider == null || submitRatingButton == null || currentUser == null) return;

        if (isAdmin) {
            // Cacher complètement la section de notation pour les admins
            ratingContainer.setVisible(false);
            ratingContainer.setManaged(false);
            return;
        }

        // Configuration pour les utilisateurs normaux
        ratingContainer.setVisible(true);
        ratingContainer.setManaged(true);

        // Configuration du slider
        ratingSlider.setMin(1);
        ratingSlider.setMax(5);
        ratingSlider.setValue(3);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setBlockIncrement(1);

        // Style et visibilité
        ratingSlider.setStyle("-fx-pref-width: 300;");
        submitRatingButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10px 20px; " +
                "-fx-background-radius: 20px; -fx-cursor: hand;");

        try {
            // Vérifier si l'utilisateur a déjà noté
            if (ratingService.hasUserRatedBlog(currentUser.getId(), currentBlog.getId())) {
                ratingSlider.setDisable(true);
                submitRatingButton.setDisable(true);
                submitRatingButton.setText("Déjà noté");
            } else {
                // Activer les contrôles
                ratingSlider.setDisable(false);
                submitRatingButton.setDisable(false);
                submitRatingButton.setText("Donner mon avis");

                // Configuration du bouton
                submitRatingButton.setOnAction(e -> {
                    try {
                        submitRating();
                    } catch (SQLException ex) {
                        showError("Erreur", "Impossible d'enregistrer votre note : " + ex.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Impossible de vérifier votre note");
        }
    }

    private void submitRating() throws SQLException {
        if (currentBlog == null || currentUser == null || isAdmin) return;

        try {
            // Vérifier si l'utilisateur n'a pas déjà noté
            if (ratingService.hasUserRatedBlog(currentUser.getId(), currentBlog.getId())) {
                showAlert(AlertType.WARNING, "Déjà noté",
                        "Vous avez déjà noté ce blog.");
                return;
            }

            // Créer et enregistrer la nouvelle note
            int rating = (int) ratingSlider.getValue();
            Rating newRating = new Rating();
            newRating.setBlogId(currentBlog.getId());
            newRating.setUserId(currentUser.getId());
            newRating.setRating(rating);
            ratingService.addRating(newRating);

            // Mettre à jour l'interface
            updateAverageRating();

            // Désactiver les contrôles de notation
            ratingSlider.setDisable(true);
            submitRatingButton.setDisable(true);
            submitRatingButton.setText("Déjà noté");

            // Afficher un message de confirmation
            showAlert(AlertType.INFORMATION, "Note enregistrée",
                    "Votre note a été enregistrée avec succès.");

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur",
                    "Impossible d'enregistrer votre note : " + e.getMessage());
        }
    }

    private void updateAverageRating() throws SQLException {
        if (currentBlog == null || averageRatingLabel == null) return;

        try {
            double avgRating = ratingService.getAverageRating(currentBlog.getId());
            int totalRatings = ratingService.getTotalRatings(currentBlog.getId());

            // Créer la boîte principale pour le rating
            VBox ratingBox = new VBox(10);
            ratingBox.setAlignment(Pos.CENTER);
            ratingBox.setPadding(new Insets(20));
            ratingBox.getStyleClass().add("rating-detail-box");

            // Score numérique
            Label scoreLabel = new Label(String.format("%.1f", avgRating));
            scoreLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

            // Étoiles
            HBox starsBox = new HBox(5);
            starsBox.setAlignment(Pos.CENTER);

            for (int i = 1; i <= 5; i++) {
                FontAwesomeIconView starIcon = new FontAwesomeIconView(FontAwesomeIcon.STAR);
                starIcon.setSize("24");

                if (i <= avgRating) {
                    starIcon.setFill(Color.web("#f1c40f")); // Étoile pleine
                } else if (i - avgRating < 1) {
                    starIcon.setOpacity(0.5); // Étoile semi-remplie
                } else {
                    starIcon.setFill(Color.web("#bdc3c7")); // Étoile vide
                }

                starsBox.getChildren().add(starIcon);
            }

            // Nombre total d'avis
            Label totalLabel = new Label(String.format("%d avis", totalRatings));
            totalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

            ratingBox.getChildren().addAll(scoreLabel, starsBox, totalLabel);

            // Remplacer le contenu du label par la boîte de rating
            if (averageRatingLabel.getParent() instanceof Pane) {
                Pane parent = (Pane) averageRatingLabel.getParent();
                int index = parent.getChildren().indexOf(averageRatingLabel);
                parent.getChildren().remove(averageRatingLabel);
                parent.getChildren().add(index, ratingBox);
            }

        } catch (Exception e) {
            averageRatingLabel.setText("Aucune évaluation");
            averageRatingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            e.printStackTrace();
        }
    }

    private void showDetailedStatistics() {
        // Vérifier si l'utilisateur est admin
        if (!isAdmin) {
            statsContainer.setVisible(false);
            statsContainer.setManaged(false);
            return;
        }

        // Afficher les statistiques uniquement pour les admins
        if (currentBlog == null || statsContainer == null) return;

        try {
            statsContainer.getChildren().clear();
            int totalRatings = ratingService.getTotalRatings(currentBlog.getId());
            Map<Integer, Long> distribution = ratingService.getRatingDistribution(currentBlog.getId());

            try {
                // Charger les statistiques
                loadStats();

                // Charger les commentaires
                if (commentsContainer != null) {
                    loadComments();
                }
            } catch (SQLException e) {
                showError("Erreur", "Une erreur est survenue lors du chargement : " + e.getMessage());
            }

            // En-tête des statistiques
            Label headerLabel = new Label("Statistiques détaillées");
            headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
            statsContainer.getChildren().add(headerLabel);

            // Nombre total d'évaluations
            Label totalLabel = new Label(String.format("Nombre total d'évaluations : %d", totalRatings));
            totalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495E;");
            statsContainer.getChildren().add(totalLabel);

            // Distribution des notes
            for (int i = 5; i >= 1; i--) {
                long count = distribution.getOrDefault(i, 0L);
                double percentage = totalRatings > 0 ? (count * 100.0 / totalRatings) : 0;

                HBox ratingRow = new HBox(10);
                ratingRow.setAlignment(Pos.CENTER_LEFT);

                Label starLabel = new Label(String.format("%d étoile%s", i, i > 1 ? "s" : ""));
                starLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C3E50;");

                Label countLabel = new Label(String.format("%d (%.1f%%)", count, percentage));
                countLabel.setStyle("-fx-text-fill: #7F8C8D;");

                ratingRow.getChildren().addAll(starLabel, countLabel);
                statsContainer.getChildren().add(ratingRow);
            }
        } catch (Exception e) {
            Label errorLabel = new Label("Impossible de charger les statistiques");
            errorLabel.setStyle("-fx-text-fill: #E74C3C;");
            statsContainer.getChildren().setAll(errorLabel);
        }
    }

    private void loadBlogImage(Blog blog) {
        if (blog == null) {
            System.out.println("Blog est null");
            imageContainer.setVisible(false);
            imageContainer.setManaged(false);
            return;
        }

        String blogImage = blog.getBlogImage();
        if (blogImage == null || blogImage.isEmpty()) {
            System.out.println("Image du blog est null ou vide");
            imageContainer.setVisible(false);
            imageContainer.setManaged(false);
            return;
        }

        try {
            System.out.println("Tentative de chargement de l'image: " + blogImage.substring(0, Math.min(50, blogImage.length())) + "...");

            String imageData = blogImage;
            // Si l'image est au format data URL (base64)
            if (imageData.startsWith("data:")) {
                int commaIndex = imageData.indexOf(",");
                if (commaIndex > -1) {
                    imageData = imageData.substring(commaIndex + 1);
                }
            }

            byte[] bytes = Base64.getDecoder().decode(imageData.trim());
            Image image = new Image(new ByteArrayInputStream(bytes));

            if (image.isError()) {
                throw new Exception("Erreur lors du chargement de l'image: " + image.getException().getMessage());
            }

            this.blogImage.setImage(image);
            this.blogImage.setFitWidth(600);
            this.blogImage.setPreserveRatio(true);

            imageContainer.setVisible(true);
            imageContainer.setManaged(true);
            System.out.println("Image chargée avec succès");

        } catch (IllegalArgumentException e) {
            System.err.println("Erreur de décodage base64: " + e.getMessage());
            showAlert(AlertType.WARNING, "Erreur", "Format d'image invalide");
            imageContainer.setVisible(false);
            imageContainer.setManaged(false);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            showAlert(AlertType.WARNING, "Erreur", "Impossible de charger l'image du blog");
            imageContainer.setVisible(false);
            imageContainer.setManaged(false);
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleClose() {
        stage.close();
    }

    private void setupReportButton() {
        if (currentUser == null || isAdmin || reportButton == null) {
            if (reportButton != null) {
                reportButton.setVisible(false);
                reportButton.setManaged(false);
            }
            return;
        }

        try {
            boolean hasReported = reportService.hasUserReportedBlog(currentUser.getId(), currentBlog.getId());
            if (hasReported) {
                reportButton.setDisable(true);
                reportButton.setText("Blog déjà signalé");
            } else {
                reportButton.setDisable(false);
                reportButton.setText("Signaler ce blog");
                reportButton.setOnAction(e -> handleReportBlog());
            }
        } catch (SQLException e) {
            showError("Erreur", "Impossible de vérifier le statut du signalement : " + e.getMessage());
        }
    }

    private void handleReportBlog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Signaler le blog");
        dialog.setHeaderText("Pourquoi souhaitez-vous signaler ce blog ?");

        // Créer la zone de texte pour la raison
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Entrez la raison du signalement...");
        reasonArea.setPrefRowCount(3);

        // Ajouter les boutons
        ButtonType submitButtonType = new ButtonType("Signaler", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        // Activer/désactiver le bouton de soumission selon si une raison est fournie
        Node submitButton = dialog.getDialogPane().lookupButton(submitButtonType);
        submitButton.setDisable(true);
        reasonArea.textProperty().addListener((observable, oldValue, newValue) -> {
            submitButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(reasonArea);

        // Convertir le résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return reasonArea.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reason -> {
            try {
                Report report = new Report(currentBlog.getId(), currentUser.getId(), reason);
                reportService.addReport(report);

                // Vérifier si le blog a été supprimé automatiquement
                if (reportService.countUniqueReports(currentBlog.getId()) >= 3) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Blog supprimé");
                    alert.setHeaderText(null);
                    alert.setContentText("Le blog a été supprimé automatiquement car il a reçu 3 signalements de différents utilisateurs.");
                    alert.showAndWait();

                    // Fermer la fenêtre de détail
                    stage.close();
                    return;
                }

                showAlert(AlertType.INFORMATION, "Blog signalé",
                        "Votre signalement a été enregistré et sera examiné par nos modérateurs.");

                // Mettre à jour l'interface
                reportButton.setDisable(true);
                reportButton.setText("Blog déjà signalé");
            } catch (SQLException e) {
                showError("Erreur", "Impossible d'enregistrer le signalement : " + e.getMessage());
            }
        });
    }
}
