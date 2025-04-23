package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Chapitre;
import tn.esprit.models.Cours;
import tn.esprit.services.ServiceCours;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CourseDetailsControllerEtudiant {
    @FXML private AnchorPane root;
    @FXML private VBox mainBox;
    @FXML private Button backButton;
    @FXML private Button editButton;
    @FXML private ImageView courseImage;
    @FXML private Label courseTitle;
    @FXML private Label matiereBadge;
    @FXML private Label categorieBadge;
    @FXML private Label dureeLabel;
    @FXML private Label prixLabel;
    @FXML private Label langueLabel;
    @FXML private VBox paymentBox;
    @FXML private Label paymentLink;
    @FXML private Label descriptionLabel;
    @FXML private ImageView teacherImage;
    @FXML private Label teacherEmail;
    @FXML private Label favoritesLabel;
    @FXML private Button statsButton;
    @FXML private VBox chaptersContainer;

    private Cours course;
    private final ServiceCours serviceCours;

    public CourseDetailsControllerEtudiant() {
        this.serviceCours = new ServiceCours();
    }

    public void setCourse(Cours course) {
        this.course = course;
        initializeUI();
    }

    private void initializeUI() {
        if (course == null) {
            return;
        }

        // Set course details
        courseTitle.setText(course.getTitle());
        descriptionLabel.setText(course.getDescription());
        
        // Set category and subject badges
        if (course.getMatiere() != null) {
            matiereBadge.setText(course.getMatiere().getTitre());
            if (course.getMatiere().getCategorie() != null) {
                categorieBadge.setText(course.getMatiere().getCategorie().getName());
            }
        }
        
        // Calculate total duration from chapters
        int totalDuration = course.getChapitres().stream()
                           .mapToInt(Chapitre::getDureeEstimee)
                           .sum();
        dureeLabel.setText(totalDuration + " minutes");
        
        // Set other details
        prixLabel.setText(course.getPrix() + " DT");
        langueLabel.setText(course.getLangue());
        favoritesLabel.setText("0"); // Default value since favorites are not implemented
        
        // Load course image
        try {
            Image image = new Image("file:Uploads/" + course.getUrlImage());
            courseImage.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading course image: " + e.getMessage());
            // Set a default image
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-course.png"));
                courseImage.setImage(defaultImage);
            } catch (Exception ex) {
                System.err.println("Error loading default image: " + ex.getMessage());
            }
        }

        // Load teacher image
        try {
            Image teacherImageFile = new Image(getClass().getResourceAsStream("/images/teacher-avatar.png"));
            teacherImage.setImage(teacherImageFile);
        } catch (Exception e) {
            System.err.println("Error loading teacher avatar: " + e.getMessage());
        }

        // Set teacher email (temporarily disabled as we don't have access to teacher info)
        teacherEmail.setText("Enseignant");

        // Load chapters
        loadChapters();
    }

    private void loadChapters() {
        chaptersContainer.getChildren().clear();
        List<Chapitre> chapters = course.getChapitres();
        
        for (Chapitre chapitre : chapters) {
            VBox chapterCard = createChapterCard(chapitre);
            chaptersContainer.getChildren().add(chapterCard);
        }
    }

    private VBox createChapterCard(Chapitre chapitre) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("chapter-card");
        card.setMaxWidth(Double.MAX_VALUE);
        
        // En-tête du chapitre
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Numéro du chapitre avec style
        Label chapterNumber = new Label(String.format("%02d", chapitre.getChapOrder()));
        chapterNumber.getStyleClass().add("chapter-number");
        
        VBox titleBox = new VBox(5);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        
        // Titre du chapitre
        Label title = new Label(chapitre.getTitle());
        title.getStyleClass().add("chapter-title");
        title.setWrapText(true);
        
        // Description
        Label description = new Label(chapitre.getContenu());
        description.getStyleClass().add("chapter-description");
        description.setWrapText(true);
        
        titleBox.getChildren().addAll(title, description);
        
        // Informations du chapitre (durée et vues)
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Durée
        HBox durationBox = new HBox(5);
        durationBox.setAlignment(Pos.CENTER_RIGHT);
        
        FontAwesomeIconView clockIcon = new FontAwesomeIconView(FontAwesomeIcon.CLOCK_ALT);
        clockIcon.getStyleClass().add("chapter-icon");
        
        Label duration = new Label(chapitre.getDureeEstimee() + " min");
        duration.getStyleClass().add("chapter-duration");
        
        durationBox.getChildren().addAll(clockIcon, duration);
        
        // Nombre de vues
        HBox viewsBox = new HBox(5);
        viewsBox.setAlignment(Pos.CENTER_RIGHT);
        
        FontAwesomeIconView eyeIcon = new FontAwesomeIconView(FontAwesomeIcon.EYE);
        eyeIcon.getStyleClass().add("chapter-icon");
        
        Label views = new Label(chapitre.getNbrVues() + " vues");
        views.getStyleClass().add("chapter-views");
        
        viewsBox.getChildren().addAll(eyeIcon, views);
        
        infoBox.getChildren().addAll(durationBox, viewsBox);
        
        // Assemblage de l'en-tête
        header.getChildren().addAll(chapterNumber, titleBox);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        
        // Statut du chapitre (verrouillé/déverrouillé)
        FontAwesomeIconView lockIcon = new FontAwesomeIconView(FontAwesomeIcon.LOCK);
        lockIcon.getStyleClass().addAll("chapter-icon", "lock-icon");
        header.getChildren().add(lockIcon);
        
        // Assemblage final de la carte
        card.getChildren().addAll(header, infoBox);
        
        // Ajouter l'effet de survol
        card.setOnMouseEntered(e -> {
            card.getStyleClass().add("chapter-card-hover");
        });
        
        card.setOnMouseExited(e -> {
            card.getStyleClass().remove("chapter-card-hover");
        });
        
        // Ajouter l'action de clic
        card.setOnMouseClicked(event -> viewChapter(chapitre));
        
        return card;
    }

    private void viewChapter(Chapitre chapitre) {
        System.out.println("Viewing chapter: " + chapitre.getTitle());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChapitreDetailsEtudiant.fxml"));
            Parent root = loader.load();
            System.out.println(chapitre);
            ChapitreDetailsControllerEtudiant controller = loader.getController();
            controller.setChapitre(chapitre, course);
            mainBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load EditCours.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListeCoursEtudiant.fxml"));
            mainBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Error loading ListeCoursEtudiant.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditAction() {
        try {
            if (course == null) {
                System.err.println("No course selected for editing");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditCours.fxml"));
            Parent root = loader.load();
            EditCoursController controller = loader.getController();
            controller.setCourse(course);
            mainBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load EditCours.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void handleStatsAction() {
        System.out.println("Viewing stats for course: " + (course != null ? course.getTitle() : "null"));
        // TODO: Implement stats navigation
    }

    @FXML
    private void handleDeleteAction() {
        if (course == null) {
            System.err.println("No course selected for deletion");
            return;
        }

        // Confirm deletion
        boolean confirmed = showConfirmationDialog("Supprimer le cours", "Êtes-vous sûr de vouloir supprimer ce cours ?");
        if (!confirmed) {
            return;
        }

        try {
            // Call the service to delete the course
            serviceCours.delete(course);

            // Show a success message
            showAlert("Succès", "Le cours a été supprimé avec succès.", Alert.AlertType.INFORMATION);

            // Navigate back to the list of courses
            handleBack();
        } catch (Exception e) {
            System.err.println("Failed to delete course: " + e.getMessage());
            showAlert("Erreur", "Une erreur est survenue lors de la suppression du cours.", Alert.AlertType.ERROR);
        }
    }

    // Utility method to show a confirmation dialog
    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType okButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType cancelButton = new ButtonType("Non", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(okButton, cancelButton);

        return alert.showAndWait().filter(response -> response == okButton).isPresent();
    }

    // Utility method to show an alert dialog
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleListes(ActionEvent event) {
        System.out.println("handleListes clicked");
        loadScene("/ListeCoursEtudiant.fxml");
    }

    private void loadScene(String fxmlPath) {
        try {
            // Load the new FXML
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Get the current stage from a known node
            Stage stage = (Stage) mainBox.getScene().getWindow();
            // Create a new scene with the loaded root
            Scene scene = new Scene(root, 1000, 700); // Match FXML dimensions
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEnrollAction() {
        // Cette méthode sera implémentée plus tard pour gérer l'inscription
        System.out.println("Tentative d'inscription au cours: " + course.getTitle());
    }
}
