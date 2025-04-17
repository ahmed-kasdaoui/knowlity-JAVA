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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Chapitre;
import tn.esprit.models.Cours;
import tn.esprit.services.ServiceCours;

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
    @FXML private GridPane chaptersGrid;

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
            System.err.println("Course is null");
            courseTitle.setText("Error: No Course Data");
            return;
        }

        // Set course details
        courseTitle.setText(course.getTitle() != null ? course.getTitle() : "No Title");
        matiereBadge.setText(course.getMatiere() != null && course.getMatiere().getTitre() != null ? course.getMatiere().getTitre() : "Unknown");
        categorieBadge.setText(course.getMatiere() != null && course.getMatiere().getCategorie() != null ? course.getMatiere().getCategorie().getName() : "Unknown");
        dureeLabel.setText( "6 heures");
        prixLabel.setText(course.getPrix() == 0 ? "Gratuit" : course.getPrix() + " DT");
        Label langueGraphic = new Label(course.getLangue() != null ? course.getLangue().toUpperCase() : "UNKNOWN");
        langueGraphic.getStyleClass().addAll("badge", "bg-info");
        langueLabel.setGraphic(langueGraphic);
        descriptionLabel.setText(course.getDescription() != null ? course.getDescription() : "No Description");
        favoritesLabel.setText("Ce cours est dans les favoris de 5 étudiant(s)");

        // Course image
        try {
            courseImage.setImage(new Image("file:Uploads/" + (course.getUrlImage() != null ? course.getUrlImage() : "default_course.jpg")));
        } catch (Exception e) {
            System.err.println("Failed to load course image: " + e.getMessage());
            courseImage.setImage(new Image("file:Uploads/default_course.jpg"));
        }

        // Payment link
        if (course.getPrix() > 0 && course.getLienDePaiment() != null && !course.getLienDePaiment().isEmpty()) {
            paymentBox.setVisible(true);
            paymentBox.setManaged(true);
            paymentLink.setText(course.getLienDePaiment());
        } else {
            paymentBox.setVisible(false);
            paymentBox.setManaged(false);
        }

        // Teacher details
        teacherEmail.setText("chamseddine.doula@example.com");
        try {
            teacherImage.setImage(new Image("file:Uploads/teacher-placeholder.jpg"));
        } catch (Exception e) {
            System.err.println("Failed to load teacher image: " + e.getMessage());
        }

        // Populate chapters
        List<Chapitre> chapitres = serviceCours.getChapitres(course);
        System.out.println(chapitres);
        if (chapitres == null || chapitres.isEmpty()) {
            System.out.println("No chapters available for course: " + course.getTitle());
            VBox alertBox = new VBox(10);
            alertBox.getStyleClass().addAll("alert", "alert-info");
            alertBox.setAlignment(Pos.CENTER);
            Label icon = new Label("⚠️");
            icon.getStyleClass().add("alert-icon");
            Label heading = new Label("Aucun chapitre trouvé");
            heading.getStyleClass().add("alert-heading");
            Label message = new Label("Commencez par ajouter un nouveau chapitre");
            alertBox.getChildren().addAll(icon, heading, message);
            chaptersGrid.add(alertBox, 0, 0, 3, 1); // Span all columns
        } else {
            System.out.println("Populating " + chapitres.size() + " chapters");
            populateGrid(chaptersGrid, chapitres);
        }
    }

    private void populateGrid(GridPane grid, List<Chapitre> chapitres) {
        grid.getChildren().clear();
        int row = 0, col = 0;
        for (Chapitre chapitre : chapitres) {
            if (chapitre != null) {
                VBox card = createChapitreCard(chapitre);
                grid.add(card, col, row);
                col++;
                if (col > 2) {
                    col = 0;
                    row++;
                }
            }
        }
        // Add "New" card


    }

    private VBox createChapitreCard(Chapitre chapitre) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", "chapter-card");

        // Header: Badge and ID
        HBox header = new HBox();
        header.getStyleClass().add("header-row");
        Label order = new Label("#" + chapitre.getChapOrder());
        order.getStyleClass().addAll("badge", "bg-primary");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label idLabel = new Label("ID: " + chapitre.getId());
        idLabel.getStyleClass().add("text-muted");
        header.getChildren().addAll(order, spacer, idLabel);

        // Title
        Label title = new Label(chapitre.getTitle() != null ? chapitre.getTitle() : "Untitled");
        title.getStyleClass().add("card-title");

        // Buttons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        Button viewBtn = new Button("Voir");
        viewBtn.getStyleClass().addAll("btn", "btn-outline-primary", "btn-sm");
        viewBtn.setGraphic(new Label("👁️"));
        viewBtn.setOnAction(e -> handleChapitreView(chapitre));

        buttons.getChildren().addAll(viewBtn);

        card.getChildren().addAll(header, title, buttons);
        return card;
    }



    private void handleChapitreView(Chapitre chapitre) {
        System.out.println("Viewing chapter: " + chapitre.getTitle());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChapitreDetailsEtudiant.fxml"));
            Parent root = loader.load();
            System.out.println(chapitre);
            ChapitreDetailsControllerEtudiant controller = loader.getController();
            controller.setChapitre(chapitre,course);
            mainBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load EditCours.fxml: " + e.getMessage());
        }   }

    private void handleChapitreEdit(Chapitre chapitre) {
        System.out.println("Editing chapter: " + chapitre.getTitle());
        try {
            if (chapitre == null) {
                System.err.println("No course selected for adding a chapter");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditChapitre.fxml"));
            Parent root = loader.load();
            EditChapitreController controller = loader.getController();
            controller.setChapitre(chapitre);
            mainBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load EditCours.fxml: " + e.getMessage());
        }
    }

    private void handleAddChapitre() {
        try {
            if (course == null) {
                System.err.println("No course selected for adding a chapter");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterChapitre.fxml"));
            Parent root = loader.load();
            AjouterChapitreController controller = loader.getController();
            controller.setCourse(course);
            mainBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load EditCours.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeCoursEtudiant.fxml"));
            Scene scene = new Scene(loader.load(), 1150, 800); // Match CourseDetails.fxml dimensions

            Stage stage = (Stage) mainBox.getScene().getWindow(); // Adjust to your @FXML node
            stage.setScene(scene);
            stage.setTitle("Liste des Cours ");
            stage.show();

        } catch (IOException e) {
            System.err.println("Failed to load ListeCours.fxml: " + e.getMessage());
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
            handleBackAction();
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

}}
