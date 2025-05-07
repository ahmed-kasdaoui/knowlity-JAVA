package controllers;

import com.esprit.knowlity.controller.teacher.TeacherController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import tn.esprit.models.Chapitre;
import tn.esprit.models.Cours;
import tn.esprit.services.ServiceCours;
import tn.esprit.services.ServiceChapitre;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CourseDetailsController {
    public Label teacherLbael;
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
    @FXML private Label nbr_chapitre;
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

    public CourseDetailsController() {
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

        // Calculate total duration and number of chapters
        List<Chapitre> chapitres = serviceCours.getChapitres(course);
        int totalDuration = chapitres.stream()
                .mapToInt(Chapitre::getDureeEstimee)
                .sum();
        dureeLabel.setText(totalDuration + " minutes");

        // Update number of chapters
        teacherLbael.setText(course.getEnseignant().getNom()+" "+course.getEnseignant().getPrenom());
        nbr_chapitre.setText(String.valueOf(chapitres.size()));

        prixLabel.setText(course.getPrix() == 0 ? "Gratuit" : course.getPrix() + " DT");

        Label langueGraphic = new Label(course.getLangue() != null ? course.getLangue().toUpperCase() : "UNKNOWN");
        langueGraphic.getStyleClass().addAll("badge", "bg-info");
        langueLabel.setGraphic(langueGraphic);

        descriptionLabel.setText(course.getDescription() != null ? course.getDescription() : "No Description");

        // Get actual favorites count
        try {
            int favCount = serviceCours.getAllFavoris(course.getId()).size();
            favoritesLabel.setText("Ce cours est dans les favoris de " + favCount + " étudiant(s)");
        } catch (Exception e) {
            System.err.println("Failed to get favorites count: " + e.getMessage());
            favoritesLabel.setText("Ce cours est dans les favoris de 0 étudiant(s)");
        }

        // Course image
        try {
            String imagePath = course.getUrlImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File("Uploads/" + imagePath);
                if (imageFile.exists()) {
                    courseImage.setImage(new Image(imageFile.toURI().toString()));
                } else {
                    courseImage.setImage(new Image("file:Uploads/default_course.jpg"));
                }
            } else {
                courseImage.setImage(new Image("file:Uploads/default_course.jpg"));
            }
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
        if (course.getEnseignant() != null) {
            teacherEmail.setText(course.getEnseignant().getEmail());
            loadImage(teacherImage,course.getEnseignant().getImage());
        } else {
            teacherEmail.setText("Information enseignant non disponible");
            teacherImage.setImage(new Image("file:Uploads/teacher-placeholder.jpg"));
        }

        // Populate chapters
        populateGrid(chaptersGrid, chapitres);
    }
    private void loadImage(ImageView imageView, String imagePath) {
        String path = imagePath != null && !imagePath.isEmpty() ? imagePath.trim() : "/images/placeholder.png";
        if (path != null && !path.isEmpty()) {
            path = path.substring(path.lastIndexOf("\\") + 1); // Handles backslashes
            path = path.substring(path.lastIndexOf("/") + 1);  // Handles forward slashes
        }

        if (!path.startsWith("/images/") && !path.equals("/images/placeholder.png")) {
            path = "/images/" + path;
        }

        try {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream == null) {
                System.err.println("Image not found: " + path);
                stream = getClass().getResourceAsStream("/images/placeholder.png");
            }
            imageView.setImage(new Image(stream));
        } catch (Exception e) {
            System.err.println("Error loading image: " + path + ". Error: " + e.getMessage());
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
        }
    }

    private void populateGrid(GridPane grid, List<Chapitre> chapitres) {
        grid.getChildren().clear();
        int row = 0, col = 0;

        // Add "New" card first
        VBox newCard = createNewCard("Chapitre");
        grid.add(newCard, col, row);
        col++;

        // Then add existing chapters
        if (chapitres != null && !chapitres.isEmpty()) {
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
        } else {
            // Show message when no chapters exist
            VBox alertBox = new VBox(10);
            alertBox.getStyleClass().addAll("alert", "alert-info");
            alertBox.setAlignment(Pos.CENTER);
            Label icon = new Label("⚠️");
            icon.getStyleClass().add("alert-icon");
            Label heading = new Label("Aucun chapitre trouvé");
            heading.getStyleClass().add("alert-heading");
            Label message = new Label("Commencez par ajouter un nouveau chapitre");
            alertBox.getChildren().addAll(icon, heading, message);
            grid.add(alertBox, col, row); // Add alert next to the "New" card
        }
    }

    private VBox createChapitreCard(Chapitre chapitre) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", "chapter-card");

        // Header with chapter number badge
        HBox header = new HBox();
        header.getStyleClass().add("header-row");
        header.setAlignment(Pos.CENTER_LEFT);

        Label order = new Label("Chapitre " + chapitre.getChapOrder());
        order.getStyleClass().add("chapter-badge");
        header.getChildren().add(order);

        // Title with icon
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label titleIcon = new Label("📚");
        Label title = new Label(chapitre.getTitle() != null ? chapitre.getTitle() : "Untitled");
        title.getStyleClass().add("card-title");
        titleBox.getChildren().addAll(titleIcon, title);

        // Chapter info
        VBox infoBox = new VBox(5);
        infoBox.getStyleClass().add("chapter-info");

        // Duration info
        HBox durationBox = new HBox(5);
        durationBox.setAlignment(Pos.CENTER_LEFT);
        Label clockIcon = new Label("⏱️");
        clockIcon.getStyleClass().add("chapter-icon");
        Label duration = new Label(chapitre.getDureeEstimee() + " minutes");
        duration.getStyleClass().add("chapter-duration");
        durationBox.getChildren().addAll(clockIcon, duration);

        // Views info
        HBox viewsBox = new HBox(5);
        viewsBox.setAlignment(Pos.CENTER_LEFT);
        Label eyeIcon = new Label("👁️");
        eyeIcon.getStyleClass().add("chapter-icon");
        Label views = new Label(chapitre.getNbrVues() + " vues");
        views.getStyleClass().add("chapter-duration");
        viewsBox.getChildren().addAll(eyeIcon, views);

        infoBox.getChildren().addAll(durationBox, viewsBox);

        // Buttons
        HBox buttons = new HBox(10);
        buttons.getStyleClass().add("button-container");
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button viewBtn = new Button("Voir le contenu");
        viewBtn.getStyleClass().addAll("btn", "btn-outline-primary", "btn-sm");
        viewBtn.setGraphic(new Label("👁️"));
        viewBtn.setOnAction(e -> handleChapitreView(chapitre));

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().addAll("btn", "btn-outline-warning", "btn-sm");
        editBtn.setGraphic(new Label("✏️"));
        editBtn.setOnAction(e -> handleChapitreEdit(chapitre));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().addAll("btn", "btn-outline-danger", "btn-sm");
        deleteBtn.setGraphic(new Label("🗑️"));
        deleteBtn.setStyle("-fx-text-fill: #dc3545; -fx-border-color: #dc3545; -fx-background-color: transparent; -fx-cursor: hand; -fx-border-radius: 5px;");
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-text-fill: white; -fx-background-color: #dc3545; -fx-border-color: #dc3545; -fx-cursor: hand; -fx-border-radius: 5px; -fx-background-radius: 5px;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-text-fill: #dc3545; -fx-border-color: #dc3545; -fx-background-color: transparent; -fx-cursor: hand; -fx-border-radius: 5px;"));
        deleteBtn.setOnAction(e -> handleChapitreDelete(chapitre));

        buttons.getChildren().addAll(viewBtn, editBtn, deleteBtn);

        // Add all components to the card
        card.getChildren().addAll(header, titleBox, infoBox, buttons);
        return card;
    }

    private VBox createNewCard(String type) {
        VBox card = new VBox();
        card.getStyleClass().addAll("card", "chapter-card");
        Button addBtn = new Button("Ajouter un " + type.toLowerCase());
        addBtn.getStyleClass().addAll("btn", "btn-outline-success", "btn-sm", "w-100", "h-100");
        addBtn.setGraphic(new Label("➕"));
        addBtn.setOnAction(e -> handleAddChapitre());
        card.getChildren().add(addBtn);
        return card;
    }

    private void handleChapitreView(Chapitre chapitre) {
        System.out.println("Viewing chapter: " + chapitre.getTitle());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChapitreDetails.fxml"));
            Parent root = loader.load();
            System.out.println(chapitre);
            ChapitreDetailsController controller = loader.getController();
            controller.setChapitre(chapitre,course);
            mainBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load EditCours.fxml: " + e.getMessage());
        }   }

    private void handleChapitreEdit(Chapitre chapitre) {
        System.out.println("Editing chapter: " + chapitre.getTitle());
        try {
            if (chapitre == null) {
                System.err.println("No chapter selected for editing");
                showAlert("Erreur", "Aucun chapitre sélectionné.", Alert.AlertType.ERROR);
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditChapitre.fxml"));
            Parent root = loader.load();
            System.out.println(chapitre);
            EditChapitreController controller = loader.getController();
            controller.setChapitre(chapitre);
            mainBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load EditChapitre.fxml: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger le formulaire d'édition.", Alert.AlertType.ERROR);
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
            Parent root = FXMLLoader.load(getClass().getResource("/ListeCours.fxml"));
            favoritesLabel.getScene().setRoot(root);


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
            Stage stage = (Stage) mainBox.getScene().getWindow(); // Adjust to your @FXML node
            Scene currentScene = stage.getScene();
            Scene newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            stage.setScene(newScene);
            stage.setTitle("Détails du Cours - " + course.getTitle());
            stage.show();



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
        loadScene("/ListeCours.fxml");
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
    void addChapitreAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditChapitre.fxml"));
            Parent root = loader.load();
            EditChapitreController controller = loader.getController();
            Chapitre chapitre = new Chapitre();
            chapitre.setCours(course);
            controller.setChapitre(chapitre);
            controller.setOnSaveCallback(() -> {
                try {
                    FXMLLoader courseLoader = new FXMLLoader(getClass().getResource("/CourseDetails.fxml"));
                    Parent courseRoot = courseLoader.load();
                    CourseDetailsController courseController = courseLoader.getController();
                    courseController.setCourse(course);
                    mainBox.getScene().setRoot(courseRoot);
                } catch (IOException e) {
                    System.err.println("Failed to load CourseDetails.fxml: " + e.getMessage());
                    showAlert("Erreur", "Impossible de retourner aux détails du cours.", Alert.AlertType.ERROR);
                }
            });
            mainBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load EditChapitre.fxml: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout.", Alert.AlertType.ERROR);
        }
    }

    private void handleChapitreDelete(Chapitre chapitre) {
        if (chapitre == null) {
            showAlert("Erreur", "Aucun chapitre sélectionné.", Alert.AlertType.ERROR);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer le chapitre '" + chapitre.getTitle() + "' ?");

        if (confirm.showAndWait().filter(response -> response == ButtonType.OK).isPresent()) {
            try {
                // Delete associated PDF file if it exists
                if (chapitre.getContenu() != null && !chapitre.getContenu().isEmpty()) {
                    File pdfFile = new File("Uploads/" + chapitre.getContenu());
                    if (pdfFile.exists()) {
                        pdfFile.delete();
                    }
                }

                // Delete the chapter from the database
                ServiceChapitre serviceChapitre = new ServiceChapitre();
                serviceChapitre.delete(chapitre);

                // Show success message
                showAlert("Succès", "Le chapitre a été supprimé avec succès.", Alert.AlertType.INFORMATION);

                // Refresh the course details view
                initializeUI();
            } catch (Exception e) {
                System.err.println("Failed to delete chapter: " + e.getMessage());
                showAlert("Erreur", "Une erreur est survenue lors de la suppression du chapitre.", Alert.AlertType.ERROR);
            }
        }
    }

    public void handleevaluation(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/teacher/teacher.fxml"));
            Parent root = loader.load();
            TeacherController controller = loader.getController();

            controller.setCourse(course);



            mainBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load EditChapitre.fxml: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout.", Alert.AlertType.ERROR);
        }
    }

    public void handleQuiz(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListQuiz.fxml"));
            Parent root = loader.load();
            ListQuizController controller = loader.getController();

            controller.setCourse(course);



            mainBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load EditChapitre.fxml: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout.", Alert.AlertType.ERROR);
        }
    }
}
