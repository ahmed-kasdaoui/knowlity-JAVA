package controllers;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
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
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Labeled;
import java.util.prefs.Preferences;

import java.io.IOException;
import java.util.*;

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

    @FXML
    private TextField searchField;

    @FXML
    private ChoiceBox<String> filterChoiceBox;

    @FXML
    private ChoiceBox<String> sortChoiceBox;

    @FXML
    private StackPane guideTooltip;

    @FXML
    private Label guideContent;

    @FXML
    private Button guidePrevButton;

    @FXML
    private Button guideNextButton;

    @FXML
    private Button guideCloseButton;

    @FXML
    private Button reopenGuideButton;

    private ServiceCours serviceCours = new ServiceCours();
    private int visibleCourses = 0;
    private final int COURSES_PER_LOAD = 6;
    private SequentialTransition cardsEntryAnimation;

    private List<Cours> allCourses = new ArrayList<>();
    private List<Cours> filteredCourses = new ArrayList<>();

    private SimpleIntegerProperty currentStep = new SimpleIntegerProperty(0);
    private List<GuideStep> guideSteps;

    private static class GuideStep {
        final String message;
        final String targetId;
        final boolean showPrevious;

        GuideStep(String message, String targetId, boolean showPrevious) {
            this.message = message;
            this.targetId = targetId;
            this.showPrevious = showPrevious;
        }
    }

    @FXML
    public void initialize() {
        // Configurer ScrollPane
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

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

        // Créer et configurer le bouton de réouverture du guide
        reopenGuideButton = new Button("Ouvrir le Guide");
        reopenGuideButton.getStyleClass().add("guide-reopen-button");
        reopenGuideButton.setOnAction(e -> restartGuide());
        
        // Ajouter le bouton en haut à droite du mainBox
        HBox headerBox = (HBox) mainBox.getChildren().get(0);
        headerBox.getChildren().add(reopenGuideButton);

        // Charger les cours initiaux
        refreshCoursesGrid();

        // Initialiser les étapes du guide
        initializeGuideSteps();

        // Démarrer le guide après un court délai
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(e -> {
            if (!Boolean.parseBoolean(getPreference("courseGuideCompleted", "false"))) {
                startGuide();
            }
        });
        delay.play();
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
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);
        scaleUp.setInterpolator(Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000));

        // Animation de remise à l'échelle normale
        ScaleTransition scaleNormal = new ScaleTransition(Duration.millis(300), card);
        scaleNormal.setFromX(1.05);
        scaleNormal.setFromY(1.05);
        scaleNormal.setToX(1.0);
        scaleNormal.setToY(1.0);
        scaleNormal.setInterpolator(Interpolator.EASE_OUT);
        scaleNormal.setDelay(Duration.millis(500));

        // Animation de translation
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), card);
        slideUp.setFromY(50);
        slideUp.setToY(0);
        slideUp.setInterpolator(Interpolator.EASE_OUT);

        // Combinaison des animations
        ParallelTransition cardAnimation = new ParallelTransition(card, fadeIn, scaleUp, scaleNormal, slideUp);
        cardAnimation.setDelay(Duration.millis(100 * index));

        return cardAnimation;
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
            Parent root = loader.load();
            CourseDetailsControllerEtudiant controller = loader.getController();
            controller.setCourse(cours); // Pass the Cours object
            Stage stage = (Stage) mainBox.getScene().getWindow();
            
            // Garder les dimensions actuelles de la fenêtre
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

    private void initializeGuideSteps() {
        guideSteps = Arrays.asList(
            new GuideStep("Bienvenue dans la liste des cours ! Je vais vous guider à travers les fonctionnalités principales.", null, false),
            new GuideStep("Utilisez la barre de recherche pour trouver rapidement un cours par son titre ou sa description.", "searchField", true),
            new GuideStep("Filtrez les cours par matière pour afficher uniquement ceux qui vous intéressent.", "filterChoiceBox", true),
            new GuideStep("Triez les cours selon différents critères pour les organiser comme vous le souhaitez.", "sortChoiceBox", true),
            new GuideStep("Les cours sont affichés ici sous forme de cartes. Cliquez sur 'Voir le cours' pour plus de détails.", "coursesGrid", true),
            new GuideStep("Si vous ne trouvez pas ce que vous cherchez, utilisez le bouton 'Afficher plus' pour voir plus de cours.", "loadMoreButton", true),
            new GuideStep("Vous pouvez maintenant explorer les cours ! N'hésitez pas à utiliser ces fonctionnalités pour trouver les cours qui vous intéressent.", null, true)
        );
    }

    private void startGuide() {
        currentStep.set(0);
        showCurrentStep();
    }

    @FXML
    private void nextGuideStep() {
        if (currentStep.get() < guideSteps.size() - 1) {
            currentStep.set(currentStep.get() + 1);
            showCurrentStep();
        }
    }

    @FXML
    private void previousGuideStep() {
        if (currentStep.get() > 0) {
            currentStep.set(currentStep.get() - 1);
            showCurrentStep();
        }
    }

    @FXML
    private void closeGuide() {
        guideTooltip.setVisible(false);
        guideTooltip.setManaged(false);
        removeAllHighlights();
        setPreference("courseGuideCompleted", "true");
    }

    private void showCurrentStep() {
        GuideStep step = guideSteps.get(currentStep.get());
        
        // Mettre à jour le contenu
        guideContent.setText(step.message);
        
        // Gérer les boutons
        guidePrevButton.setVisible(step.showPrevious);
        guidePrevButton.setManaged(step.showPrevious);
        guideNextButton.setText(currentStep.get() == guideSteps.size() - 1 ? "Terminer" : "Suivant");
        
        // Supprimer les surlignages précédents
        removeAllHighlights();
        
        // Positionner le tooltip et surligner l'élément cible
        if (step.targetId != null) {
            Node targetNode = root.lookup("#" + step.targetId);
            if (targetNode != null) {
                highlightNode(targetNode);
                positionTooltipNearNode(targetNode);
            }
        } else {
            // Centrer le tooltip pour les étapes sans cible
            centerTooltip();
        }
        
        // Afficher le tooltip
        guideTooltip.setVisible(true);
        guideTooltip.setManaged(true);
    }

    private void removeAllHighlights() {
        root.lookupAll(".guide-highlight").forEach(node -> 
            node.getStyleClass().remove("guide-highlight")
        );
    }

    private void highlightNode(Node node) {
        node.getStyleClass().add("guide-highlight");
    }

    private void positionTooltipNearNode(Node targetNode) {
        // Obtenir les coordonnées de l'élément cible
        Bounds bounds = targetNode.localToScene(targetNode.getBoundsInLocal());
        
        // Calculer la position du tooltip
        double tooltipX = bounds.getMinX() + bounds.getWidth() / 2 - guideTooltip.getWidth() / 2;
        double tooltipY = bounds.getMaxY() + 10;
        
        // Ajuster si le tooltip dépasse les bords
        if (tooltipX < 0) tooltipX = 10;
        if (tooltipX + guideTooltip.getWidth() > root.getWidth()) {
            tooltipX = root.getWidth() - guideTooltip.getWidth() - 10;
        }
        
        // Positionner au-dessus si pas assez d'espace en dessous
        if (tooltipY + guideTooltip.getHeight() > root.getHeight()) {
            tooltipY = bounds.getMinY() - guideTooltip.getHeight() - 10;
            guideTooltip.setStyle("--arrow-top: auto; --arrow-bottom: -6px;");
        } else {
            guideTooltip.setStyle("--arrow-top: -6px; --arrow-bottom: auto;");
        }
        
        // Appliquer la position
        guideTooltip.setTranslateX(tooltipX);
        guideTooltip.setTranslateY(tooltipY);
    }

    private void centerTooltip() {
        guideTooltip.setTranslateX((root.getWidth() - guideTooltip.getWidth()) / 2);
        guideTooltip.setTranslateY((root.getHeight() - guideTooltip.getHeight()) / 2);
        guideTooltip.setStyle("--arrow-top: auto; --arrow-bottom: auto;");
    }

    private String getPreference(String key, String defaultValue) {
        Preferences prefs = Preferences.userNodeForPackage(ListeCoursControllerEtudiant.class);
        return prefs.get(key, defaultValue);
    }

    private void setPreference(String key, String value) {
        Preferences prefs = Preferences.userNodeForPackage(ListeCoursControllerEtudiant.class);
        prefs.put(key, value);
    }

    @FXML
    private void restartGuide() {
        // Réinitialiser la préférence du guide
        setPreference("courseGuideCompleted", "false");
        
        // Réinitialiser et redémarrer le guide
        currentStep.set(0);
        showCurrentStep();
    }
}