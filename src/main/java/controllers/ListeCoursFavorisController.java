package controllers;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.Cours;
import tn.esprit.services.ServiceCours;
import tn.esprit.services.ServiceFavoris;
import tn.knowlity.tools.UserSessionManager;
import tn.knowlity.service.userService;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ListeCoursFavorisController {
    @FXML private AnchorPane root;
    @FXML private VBox mainBox;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox scrollContent;
    @FXML private GridPane coursesGrid;
    @FXML private Button loadMoreButton;
    @FXML private VBox noCoursesBox;
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> filterChoiceBox;
    @FXML private ChoiceBox<String> sortChoiceBox;
    @FXML private StackPane guideTooltip;
    @FXML private Label guideContent;
    @FXML private Button guidePrevButton;
    @FXML private Button guideNextButton;
    @FXML private Button guideCloseButton;
    @FXML private MenuItem mesOffresItem;
    @FXML private MenuItem autresOffresItem;

    private ServiceCours serviceCours = new ServiceCours();
    private userService userService = new userService();
    private int visibleCourses = 0;
    private final int COURSES_PER_LOAD = 6;
    private SequentialTransition cardsEntryAnimation;
    private List<Cours> allCourses = new ArrayList<>();
    private List<Cours> filteredCourses = new ArrayList<>();
    private final int userId = UserSessionManager.getInstance().getCurrentUser().getId();

    @FXML
    public void initialize() {
        // Configurer ScrollPane
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Initialiser les listes de cours favoris
        allCourses = serviceCours.getAllFavoris(userId);
        filteredCourses.addAll(allCourses);

        // Initialiser les matières dans le filterChoiceBox
        initializeFilterChoices();

        // Configurer les listeners pour la recherche, le filtrage et le tri
        setupSearchListener();
        setupFilterListener();
        setupSortListener();

        // Sélectionner les valeurs par défaut
        filterChoiceBox.getSelectionModel().select("Toutes les matières");
        sortChoiceBox.getSelectionModel().select("Trier par");

        // Charger les cours initiaux
        refreshCoursesGrid();
    }

    private void initializeFilterChoices() {
        Set<String> matieres = new HashSet<>();
        matieres.add("Toutes les matières");
        
        for (Cours cours : allCourses) {
            if (cours.getMatiere() != null && cours.getMatiere().getTitre() != null) {
                matieres.add(cours.getMatiere().getTitre());
            }
        }
        
        List<String> matieresList = new ArrayList<>(matieres);
        Collections.sort(matieresList.subList(1, matieresList.size()));
        filterChoiceBox.getItems().setAll(matieresList);
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndSortCourses();
        });
    }

    private void setupFilterListener() {
        filterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterAndSortCourses();
        });
    }

    private void setupSortListener() {
        sortChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterAndSortCourses();
        });
    }

    private void filterAndSortCourses() {
        filteredCourses.clear();
        String searchText = searchField.getText().toLowerCase();
        String selectedCategory = filterChoiceBox.getValue();
        
        for (Cours cours : allCourses) {
            boolean matchesSearch = searchText.isEmpty() || 
                                  cours.getTitle().toLowerCase().contains(searchText) ||
                                  cours.getDescription().toLowerCase().contains(searchText);
            
            boolean matchesFilter = selectedCategory == null || 
                                  selectedCategory.equals("Toutes les matières") ||
                                  (cours.getMatiere() != null && 
                                   cours.getMatiere().getTitre() != null && 
                                   cours.getMatiere().getTitre().equals(selectedCategory));
            
            if (matchesSearch && matchesFilter) {
                filteredCourses.add(cours);
            }
        }
        
        String sortOption = sortChoiceBox.getValue();
        if (sortOption != null) {
            switch (sortOption) {
                case "Nom (A-Z)":
                    filteredCourses.sort((c1, c2) -> c1.getTitle().compareToIgnoreCase(c2.getTitle()));
                    break;
                case "Nom (Z-A)":
                    filteredCourses.sort((c1, c2) -> c2.getTitle().compareToIgnoreCase(c1.getTitle()));
                    break;
                case "Date (Plus récent)":
                    filteredCourses.sort((c1, c2) -> Integer.compare(c2.getId(), c1.getId()));
                    break;
                case "Date (Plus ancien)":
                    filteredCourses.sort((c1, c2) -> Integer.compare(c1.getId(), c2.getId()));
                    break;
                case "Popularité":
                    filteredCourses.sort((c1, c2) -> Integer.compare(c2.getPrix(), c1.getPrix()));
                    break;
            }
        }
        
        refreshCoursesGrid();
    }

    private void refreshCoursesGrid() {
        coursesGrid.getChildren().clear();
        visibleCourses = 0;
        coursesGrid.getRowConstraints().clear();
        
        if (filteredCourses.isEmpty()) {
            noCoursesBox.setManaged(true);
            noCoursesBox.setVisible(true);
            loadMoreButton.setVisible(false);
            loadMoreButton.setManaged(false);
        } else {
            noCoursesBox.setManaged(false);
            noCoursesBox.setVisible(false);
            loadCourses();
        }
    }

    private void loadCourses() {
        int totalCourses = filteredCourses.size();
        int startIndex = visibleCourses;
        visibleCourses = Math.min(visibleCourses + COURSES_PER_LOAD, totalCourses);
        
        cardsEntryAnimation = new SequentialTransition();
        
        for (int i = startIndex; i < visibleCourses; i++) {
            var cours = filteredCourses.get(i);
            var card = createCourseCard(cours);
            
            card.setOpacity(0);
            card.setScaleX(0.3);
            card.setScaleY(0.3);
            card.setTranslateY(50);
            
            int column = i % 3;
            int row = i / 3;
            coursesGrid.add(card, column, row);
            
            ParallelTransition cardEntry = createCardEntryAnimation(card, i - startIndex);
            cardsEntryAnimation.getChildren().add(cardEntry);
        }
        
        loadMoreButton.setVisible(visibleCourses < totalCourses);
        loadMoreButton.setManaged(visibleCourses < totalCourses);
        
        if (startIndex > 0) {
            cardsEntryAnimation.setOnFinished(e -> {
                Timeline scrollAnim = new Timeline(
                    new KeyFrame(Duration.millis(800),
                        new KeyValue(scrollPane.vvalueProperty(), 1.0)
                    )
                );
                scrollAnim.play();
            });
        }
        
        cardsEntryAnimation.play();
    }

    private ParallelTransition createCardEntryAnimation(VBox card, int index) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(500), card);
        scaleUp.setFromX(0.3);
        scaleUp.setFromY(0.3);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);
        scaleUp.setInterpolator(Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000));

        ScaleTransition scaleNormal = new ScaleTransition(Duration.millis(300), card);
        scaleNormal.setFromX(1.05);
        scaleNormal.setFromY(1.05);
        scaleNormal.setToX(1.0);
        scaleNormal.setToY(1.0);
        scaleNormal.setInterpolator(Interpolator.EASE_OUT);
        scaleNormal.setDelay(Duration.millis(500));

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), card);
        slideUp.setFromY(50);
        slideUp.setToY(0);
        slideUp.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition cardAnimation = new ParallelTransition(card, fadeIn, scaleUp, scaleNormal, slideUp);
        cardAnimation.setDelay(Duration.millis(100 * index));

        return cardAnimation;
    }

    private VBox createCourseCard(Cours cours) {
        VBox card = new VBox();
        card.getStyleClass().add("course-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(400);

        StackPane header = new StackPane();
        header.setPrefHeight(180.0);
        header.getStyleClass().add("card-header");

        ImageView imageView = new ImageView();
        try {
            String imagePath = cours.getUrlImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("http")) {
                    // Si c'est une URL
                    Image image = new Image(imagePath, 300, 180, true, true);
                    imageView.setImage(image);
                } else {
                    // Si c'est un chemin local
                    File imageFile = new File("Uploads/" + imagePath);
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString(), 300, 180, true, true);
                        imageView.setImage(image);
                    } else {
                        throw new Exception("Image file not found: " + imagePath);
                    }
                }
            } else {
                throw new Exception("Image path is null or empty");
            }
        } catch (Exception e) {
            System.out.println("Error loading course image: " + e.getMessage());
            // Utiliser une image par défaut depuis les ressources
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-course.jpg"), 300, 180, true, true);
                imageView.setImage(defaultImage);
            } catch (Exception ex) {
                System.out.println("Error loading default image: " + ex.getMessage());
                // Créer un rectangle gris comme fallback ultime
                Rectangle placeholder = new Rectangle(300, 180, Color.LIGHTGRAY);
                header.getChildren().add(placeholder);
            }
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

        VBox body = new VBox(20);
        body.getStyleClass().add("card-body");
        body.setAlignment(Pos.CENTER);
        body.setPadding(new Insets(20, 10, 10, 10));

        Label title = new Label(cours.getTitle());
        title.getStyleClass().add("course-title");
        title.setWrapText(true);

        // Gestion sécurisée de l'affichage de l'enseignant
        String enseignantNom = "Non assigné";
        if (cours.getEnseignant() != null && cours.getEnseignant().getNom() != null) {
            enseignantNom = cours.getEnseignant().getNom();
        }
        Label teacher = new Label("Enseignant: " + enseignantNom);
        teacher.getStyleClass().add("teacher-name");

        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER);

        VBox chapters = new VBox(5);
        chapters.setAlignment(Pos.CENTER);
        Label chaptersCount = new Label(String.valueOf(serviceCours.getChapitres(cours).size()));
        chaptersCount.getStyleClass().add("stat-count");
        Label chaptersLabel = new Label("Chapitres");
        chaptersLabel.getStyleClass().add("text-muted");
        chapters.getChildren().addAll(chaptersCount, chaptersLabel);

        stats.getChildren().add(chapters);

        body.getChildren().addAll(title, teacher, stats);

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
    void loadMoreAction(ActionEvent event) {
        loadCourses();
    }

    private void viewCourse(Cours cours) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CourseDetailsEtudiant.fxml"));
            Parent root = loader.load();
            CourseDetailsControllerEtudiant controller = loader.getController();
            controller.setCourse(cours);
            Stage stage = (Stage) mainBox.getScene().getWindow();
            Scene currentScene = stage.getScene();
            Scene newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            stage.setScene(newScene);
            stage.setTitle("Détails du Cours - " + cours.getTitle());
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading CourseDetails.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleListes(ActionEvent event) {
        Object source = event.getSource();
        String fxmlPath;
        
        if (source == mesOffresItem) {
            fxmlPath = "/ListeCoursEtudiantFavoris.fxml";
        } else if (source == autresOffresItem) {
            fxmlPath = "/ListeCoursEtudiantInscrits.fxml";
        } else {
            fxmlPath = "/ListeCoursEtudiant.fxml";
        }
        
        loadScene(fxmlPath);
    }

    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) mainBox.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void genererPlanning(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ScheduleView.fxml"));
            Scene scene = new Scene(loader.load(), 800, 800);

            // Créer une nouvelle fenêtre
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Planning");
            newStage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void previousGuideStep() {
        // Implémentation du guide étape précédente
        int currentStep = Integer.parseInt(guideTooltip.getUserData().toString());
        showGuideStep(currentStep - 1);
    }

    @FXML
    private void nextGuideStep() {
        // Implémentation du guide étape suivante
        int currentStep = Integer.parseInt(guideTooltip.getUserData().toString());
        showGuideStep(currentStep + 1);
    }

    @FXML
    private void closeGuide() {
        guideTooltip.setVisible(false);
        guideTooltip.setManaged(false);
    }

    private void showGuideStep(int step) {
        guideTooltip.setUserData(String.valueOf(step));
        switch (step) {
            case 1:
                guideContent.setText("Bienvenue dans vos cours favoris ! Ici vous retrouverez tous les cours que vous avez marqués comme favoris.");
                guidePrevButton.setDisable(true);
                guideNextButton.setDisable(false);
                break;
            case 2:
                guideContent.setText("Utilisez la barre de recherche pour trouver rapidement un cours spécifique.");
                guidePrevButton.setDisable(false);
                guideNextButton.setDisable(false);
                break;
            case 3:
                guideContent.setText("Vous pouvez filtrer les cours par matière et les trier selon différents critères.");
                guidePrevButton.setDisable(false);
                guideNextButton.setDisable(true);
                break;
            default:
                closeGuide();
                break;
        }
    }
} 