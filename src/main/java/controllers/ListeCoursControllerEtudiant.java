package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.models.Cours;
import tn.esprit.services.ServiceCours;

import java.io.IOException;

public class ListeCoursControllerEtudiant {

    @FXML
    private AnchorPane root;

    @FXML
    private VBox mainBox;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox scrollContent;

    @FXML
    private GridPane coursesGrid;

    @FXML
    private Button createButton;

    @FXML
    private Button loadMoreButton;

    @FXML
    private VBox noCoursesBox;

    private ServiceCours serviceCours = new ServiceCours();
    private int visibleCourses = 0;
    private final int COURSES_PER_LOAD = 6;

    @FXML
    public void initialize() {
        // Configurer ScrollPane
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        loadCourses();
    }

    private void loadCourses() {
        coursesGrid.getChildren().clear();
        visibleCourses = Math.min(visibleCourses + COURSES_PER_LOAD, serviceCours.getAll().size());

        var coursList = serviceCours.getAll();
        if (coursList.isEmpty()) {
            noCoursesBox.setVisible(true);
            noCoursesBox.setManaged(true);
            loadMoreButton.setVisible(false);
            loadMoreButton.setManaged(false);
            return;
        }

        noCoursesBox.setVisible(false);
        noCoursesBox.setManaged(false);
        loadMoreButton.setVisible(visibleCourses < coursList.size());
        loadMoreButton.setManaged(visibleCourses < coursList.size());

        for (int i = 0; i < visibleCourses && i < coursList.size(); i++) {
            var cours = coursList.get(i);
            var card = createCourseCard(cours);
            coursesGrid.add(card, i % 3, i / 3);
        }

        // Ajuster la disposition
        scrollContent.requestLayout();
        // Défilement fluide vers le bas si nouveaux cours
        if (visibleCourses > COURSES_PER_LOAD) {
            scrollPane.setVvalue(1.0);
        }
    }

    private VBox createCourseCard(Cours cours) {
        // Card
        VBox card = new VBox();
        card.getStyleClass().add("course-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(400);

        // Header
        StackPane header = new StackPane();
        header.setPrefHeight(180.0);
        header.getStyleClass().add("card-header");

        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image("file:Uploads/" + cours.getUrlImage()));
        } catch (Exception e) {
            imageView.setImage(new Image("file:Uploads/default.jpg"));
        }
        imageView.setFitHeight(180.0);
        imageView.setFitWidth(300.0);
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("object-fit-cover");

        Region overlay = new Region();
        overlay.getStyleClass().add("image-overlay");

        Label badge = new Label(cours.getMatiere() != null ? cours.getMatiere().getTitre() : "Sans matière");
        badge.getStyleClass().add("badge");
        StackPane.setAlignment(badge, Pos.TOP_LEFT);
        StackPane.setMargin(badge, new Insets(10, 0, 0, 10));

        header.getChildren().addAll(imageView, overlay, badge);

        // Body
        VBox body = new VBox(20);
        body.getStyleClass().add("card-body");
        body.setAlignment(Pos.CENTER);
        body.setPadding(new Insets(20, 10, 10, 10));

        Label title = new Label(cours.getTitle());
        title.getStyleClass().add("course-title");
        title.setWrapText(true);

        Label teacher = new Label("Enseignant: Chamseddine");
        teacher.getStyleClass().add("teacher-name");

        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER);

        VBox chapters = new VBox(5);
        chapters.setAlignment(Pos.CENTER);
        Label chaptersCount = new Label("10");
        chaptersCount.getStyleClass().add("stat-count");
        Label chaptersLabel = new Label("Chapitres");
        chaptersLabel.getStyleClass().add("text-muted");
        chapters.getChildren().addAll(chaptersCount, chaptersLabel);

        VBox students = new VBox(5);
        students.setAlignment(Pos.CENTER);
        Label studentsCount = new Label("10");
        studentsCount.getStyleClass().add("stat-count");
        Label studentsLabel = new Label("Apprenants");
        studentsLabel.getStyleClass().add("text-muted");
        students.getChildren().addAll(studentsCount, studentsLabel);

        stats.getChildren().addAll(chapters, students);

        body.getChildren().addAll(title, teacher, stats);

        // Footer
        HBox footer = new HBox();
        footer.getStyleClass().add("card-footer");
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(10));

        Button viewButton = new Button("Voir le cours");
        viewButton.getStyleClass().addAll("btn", "btn-outline-primary", "btn-hover");
        viewButton.setOnAction(e -> viewCourse(cours));
        footer.getChildren().add(viewButton);

        card.getChildren().addAll(header, body, footer);
        return card;
    }

    @FXML
    void createAction(ActionEvent event) {
        // TODO: Naviguer vers le formulaire de création
        System.out.println("Naviguer vers création de cours");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AjoutCours.fxml"));
            mainBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void loadMoreAction(ActionEvent event) {
        loadCourses();
    }

    private void viewCourse(Cours cours) {
        System.out.println(serviceCours.getChapitres(cours));


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CourseDetailsEtudiant.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 900); // Match CourseDetails.fxml dimensions
            CourseDetailsControllerEtudiant controller = loader.getController();
            controller.setCourse(cours); // Pass the Cours object
            Stage stage = (Stage) mainBox.getScene().getWindow(); // Adjust to your @FXML node
            stage.setScene(scene);
            stage.setTitle("Détails du Cours - " + cours.getTitle());
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading CourseDetails.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    void handleMesOffres(ActionEvent event) {
        System.out.println("handleMesOffres clicked");
        loadScene("/ListeCours.fxml");
    }

    @FXML
    void handleListes(ActionEvent event) {
        System.out.println("handleListes clicked");
        loadScene("/ListeCoursEtudiant.fxml");
    }

    @FXML
    void handleAutresOffres(ActionEvent event) {
        System.out.println("handleAutresOffres clicked");
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
}