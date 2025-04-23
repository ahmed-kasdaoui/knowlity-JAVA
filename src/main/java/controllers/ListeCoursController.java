package controllers;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.Cours;
import tn.esprit.services.ServiceCours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListeCoursController {

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
    @FXML
    private ChoiceBox<String> filterChoiceBox;

    @FXML
    private ChoiceBox<String> sortChoiceBox;

    @FXML
    private TextField searchField;

    private ServiceCours serviceCours = new ServiceCours();
    private int visibleCourses = 0;
    private final int COURSES_PER_LOAD = 6;
    private SequentialTransition cardsEntryAnimation;

    private List<Cours> allCourses = new ArrayList<>();
    private List<Cours> filteredCourses = new ArrayList<>();

    @FXML
    public void initialize() {
        // Configurer ScrollPane
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Préparer l'animation du contenu principal
        prepareMainContentAnimation();

        // Animation d'entrée pour le titre et le bouton de création
        animateHeader();

        // Initialiser les listes de cours
        allCourses = serviceCours.getAll();
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

    private void prepareMainContentAnimation() {
        // Animation d'ouverture pour tout le contenu principal
        mainBox.setOpacity(0);
        mainBox.setScaleX(0.9);
        mainBox.setScaleY(0.9);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), mainBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(800), mainBox);
        scaleUp.setFromX(0.9);
        scaleUp.setFromY(0.9);
        scaleUp.setToX(1);
        scaleUp.setToY(1);
        scaleUp.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition mainAnimation = new ParallelTransition(fadeIn, scaleUp);
        mainAnimation.play();
    }

    private void animateHeader() {
        // Trouver les éléments du header
        HBox headerBox = (HBox) mainBox.getChildren().get(0);
        Label titleLabel = (Label) headerBox.getChildren().get(0);
        Button createBtn = (Button) headerBox.getChildren().get(1);

        // Configurer l'état initial
        titleLabel.setOpacity(0);
        titleLabel.setTranslateY(-50);
        createBtn.setOpacity(0);
        createBtn.setTranslateY(-50);

        // Animation du titre
        Timeline titleAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(titleLabel.opacityProperty(), 0),
                        new KeyValue(titleLabel.translateYProperty(), -50)
                ),
                new KeyFrame(Duration.millis(800),
                        new KeyValue(titleLabel.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(titleLabel.translateYProperty(), 0, Interpolator.EASE_OUT)
                )
        );

        // Animation du bouton
        Timeline btnAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(createBtn.opacityProperty(), 0),
                        new KeyValue(createBtn.translateYProperty(), -50)
                ),
                new KeyFrame(Duration.millis(800),
                        new KeyValue(createBtn.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(createBtn.translateYProperty(), 0, Interpolator.EASE_OUT)
                )
        );

        // Démarrer les animations
        titleAnim.play();
        btnAnim.setDelay(Duration.millis(300));
        btnAnim.play();
    }

    private void initializeFilterChoices() {
        // Créer un Set pour stocker les matières uniques
        Set<String> matieres = new HashSet<>();
        matieres.add("Toutes les matières"); // Option par défaut
        
        // Ajouter toutes les matières des cours
        for (Cours cours : allCourses) {
            if (cours.getMatiere() != null && cours.getMatiere().getTitre() != null) {
                matieres.add(cours.getMatiere().getTitre());
            }
        }
        
        // Convertir le Set en List et trier alphabétiquement
        List<String> matieresList = new ArrayList<>(matieres);
        Collections.sort(matieresList.subList(1, matieresList.size())); // Trier tout sauf "Toutes les matières"
        
        // Mettre à jour le ChoiceBox
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
        // Réinitialiser la liste filtrée
        filteredCourses.clear();
        
        // Appliquer la recherche et le filtre
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
        
        // Appliquer le tri
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
        
        // Rafraîchir l'affichage
        refreshCoursesGrid();
    }

    private void refreshCoursesGrid() {
        // Effacer la grille existante
        coursesGrid.getChildren().clear();
        visibleCourses = 0;
        
        // Réinitialiser les rangées
        coursesGrid.getRowConstraints().clear();
        
        // Afficher le message "Aucun cours" si nécessaire
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
        
        // Déterminer l'index de départ et le nouveau nombre de cours visibles
        int startIndex = visibleCourses;
        visibleCourses = Math.min(visibleCourses + COURSES_PER_LOAD, totalCourses);
        
        // Créer une animation séquentielle pour les nouvelles cartes
        cardsEntryAnimation = new SequentialTransition();
        
        for (int i = startIndex; i < visibleCourses; i++) {
            var cours = filteredCourses.get(i);
            var card = createCourseCard(cours);
            
            // Configuration initiale pour l'animation
            card.setOpacity(0);
            card.setScaleX(0.3);
            card.setScaleY(0.3);
            card.setTranslateY(50);
            
            // Calcul de la position dans la grille
            int column = i % 3;
            int row = i / 3;
            coursesGrid.add(card, column, row);
            
            // Animation d'entrée
            ParallelTransition cardEntry = createCardEntryAnimation(card, i - startIndex);
            cardsEntryAnimation.getChildren().add(cardEntry);
        }
        
        // Gestion du bouton "Afficher plus"
        loadMoreButton.setVisible(visibleCourses < totalCourses);
        loadMoreButton.setManaged(visibleCourses < totalCourses);
        
        // Défilement automatique après ajout
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
        // Animation de fondu
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Animation d'échelle avec rebond
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(500), card);
        scaleUp.setFromX(0.3);
        scaleUp.setFromY(0.3);
        scaleUp.setToX(1.05); // Légèrement plus grand pour l'effet rebond
        scaleUp.setToY(1.05);
        scaleUp.setInterpolator(Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000)); // Effet rebond

        // Animation de remise à l'échelle normale après le rebond
        ScaleTransition scaleNormal = new ScaleTransition(Duration.millis(300), card);
        scaleNormal.setFromX(1.05);
        scaleNormal.setFromY(1.05);
        scaleNormal.setToX(1.0);
        scaleNormal.setToY(1.0);
        scaleNormal.setInterpolator(Interpolator.EASE_OUT);
        scaleNormal.setDelay(Duration.millis(500));

        // Animation de translation Y (montée)
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), card);
        slideUp.setFromY(50);
        slideUp.setToY(0);
        slideUp.setInterpolator(Interpolator.EASE_OUT);

        // Combinaison des animations
        ParallelTransition cardAnimation = new ParallelTransition(card, fadeIn, scaleUp, scaleNormal, slideUp);

        // Délai entre les cartes pour effet séquentiel plus prononcé
        cardAnimation.setDelay(Duration.millis(100 * index));

        return cardAnimation;
    }

    private VBox createCourseCard(Cours cours) {
        // Card
        VBox card = new VBox();
        card.getStyleClass().add("course-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(400);

        // Ajouter effet de survol avec animation
        card.setOnMouseEntered(e -> {
            // Annuler les animations en cours pour éviter les conflits
            if (cardsEntryAnimation != null && cardsEntryAnimation.getStatus() == Animation.Status.RUNNING) {
                return;
            }

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), card);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);

            // Effet d'élévation supplémentaire
            Timeline elevate = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(card.translateYProperty(), 0)
                    ),
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(card.translateYProperty(), -10, Interpolator.EASE_OUT)
                    )
            );

            // Jouer les animations en parallèle
            ParallelTransition hover = new ParallelTransition(scaleUp, elevate);
            hover.play();
        });

        card.setOnMouseExited(e -> {
            // Annuler les animations en cours pour éviter les conflits
            if (cardsEntryAnimation != null && cardsEntryAnimation.getStatus() == Animation.Status.RUNNING) {
                return;
            }

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), card);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);

            // Retour à la position d'origine
            Timeline descend = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(card.translateYProperty(), -10)
                    ),
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(card.translateYProperty(), 0, Interpolator.EASE_OUT)
                    )
            );

            // Jouer les animations en parallèle
            ParallelTransition exit = new ParallelTransition(scaleDown, descend);
            exit.play();
        });

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

        // Animation du badge
        badge.setScaleX(0);
        badge.setScaleY(0);

        // Animer le badge avec un léger délai
        ScaleTransition badgeAnim = new ScaleTransition(Duration.millis(300), badge);
        badgeAnim.setDelay(Duration.millis(600));
        badgeAnim.setFromX(0);
        badgeAnim.setFromY(0);
        badgeAnim.setToX(1);
        badgeAnim.setToY(1);
        badgeAnim.setInterpolator(Interpolator.EASE_OUT);

        // Déclencher l'animation quand la carte est ajoutée à la scène
        card.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                badgeAnim.play();
            }
        });

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

        // Animation du bouton au survol
        viewButton.setOnMouseEntered(e -> {
            // Effet de pulsation du bouton
            Timeline pulse = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(viewButton.scaleXProperty(), 1),
                            new KeyValue(viewButton.scaleYProperty(), 1)
                    ),
                    new KeyFrame(Duration.millis(150),
                            new KeyValue(viewButton.scaleXProperty(), 1.1),
                            new KeyValue(viewButton.scaleYProperty(), 1.1)
                    ),
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(viewButton.scaleXProperty(), 1),
                            new KeyValue(viewButton.scaleYProperty(), 1)
                    )
            );
            pulse.play();
        });

        footer.getChildren().add(viewButton);

        card.getChildren().addAll(header, body, footer);
        return card;
    }

    @FXML
    void createAction(ActionEvent event) {
        // Animation de transition avant de naviguer
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), mainBox);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutCours.fxml"));
                Scene scene = new Scene(loader.load(), 1000, 900);

                Stage stage = (Stage) mainBox.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Ajouter un cours");
                stage.show();

            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        });
        fadeOut.play();
    }


    @FXML
    void loadMoreAction(ActionEvent event) {
        loadCourses(); // Directly load more courses without any animations
    }
    private void viewCourse(Cours cours) {
        System.out.println(serviceCours.getChapitres(cours));

        // Animation de transition avant de naviguer
        // Animation "Zoom out" puis fondu
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(300), mainBox);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.8);
        scaleOut.setToY(0.8);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), mainBox);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        ParallelTransition exitTransition = new ParallelTransition(scaleOut, fadeOut);
        exitTransition.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CourseDetails.fxml"));
                Scene scene = new Scene(loader.load(), 1000, 900);
                CourseDetailsController controller = loader.getController();
                controller.setCourse(cours);
                Stage stage = (Stage) mainBox.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Détails du Cours - " + cours.getTitle());
                stage.show();
            } catch (IOException ex) {
                System.err.println("Error loading CourseDetails.fxml: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        exitTransition.play();
    }

    @FXML
    void handleListes(ActionEvent event) {
        System.out.println("handleListes clicked");

        // Animation de transition de page avec zoom-out et fondu
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(400), mainBox);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.8);
        scaleOut.setToY(0.8);
        scaleOut.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), mainBox);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        ParallelTransition transition = new ParallelTransition(scaleOut, fadeOut);
        transition.setOnFinished(e -> loadScene("/ListeCours.fxml"));
        transition.play();
    }

    private void loadScene(String fxmlPath) {
        try {
            // Charger le nouveau FXML
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

            // Configurer l'animation d'entrée
            root.setOpacity(0);
            root.setScaleX(1.2);
            root.setScaleY(1.2);

            // Obtenir la scène actuelle
            Stage stage = (Stage) mainBox.getScene().getWindow();

            // Créer une nouvelle scène avec le root chargé
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);

            // Animation d'entrée pour la nouvelle scène
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(500), root);
            scaleIn.setFromX(1.2);
            scaleIn.setFromY(1.2);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            scaleIn.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition entryTransition = new ParallelTransition(fadeIn, scaleIn);
            entryTransition.play();

            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
