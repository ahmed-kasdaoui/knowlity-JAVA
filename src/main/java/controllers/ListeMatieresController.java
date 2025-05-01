package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.Matiere;
import tn.esprit.services.ServiceMatiere;

import java.io.IOException;
import java.util.List;

import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;

public class ListeMatieresController {

    @FXML
    private AnchorPane root;

    @FXML
    private VBox mainBox;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private FlowPane matieresFlowPane;

    @FXML
    private TextField searchField;

    @FXML
    private Button createButton;

    private ServiceMatiere serviceMatiere = new ServiceMatiere();

    // Définition des tailles uniformes pour les cartes
    private final double CARD_WIDTH = 320;
    private final double CARD_HEIGHT = 280;

    @FXML
    public void initialize() {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Configuration du FlowPane
        matieresFlowPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        matieresFlowPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        matieresFlowPane.setHgap(25);
        matieresFlowPane.setVgap(25);
        matieresFlowPane.setPadding(new Insets(25));

        // Chargement des données
        loadMatieres();

        // Configuration de la recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            matieresFlowPane.getChildren().clear();
            List<Matiere> filteredMatieres = serviceMatiere.getAll().stream()
                    .filter(m -> m.getTitre().toLowerCase().contains(newValue.toLowerCase()) ||
                            (m.getCategorie() != null && m.getCategorie().getName().toLowerCase().contains(newValue.toLowerCase())) ||
                            m.getDescription().toLowerCase().contains(newValue.toLowerCase()))
                    .toList();

            if (filteredMatieres.isEmpty()) {
                Label noResults = new Label("Aucune matière ne correspond à votre recherche");
                noResults.getStyleClass().add("placeholder-text");
                matieresFlowPane.getChildren().add(noResults);
            } else {
                for (int i = 0; i < filteredMatieres.size(); i++) {
                    createMatiereCard(filteredMatieres.get(i), i);
                }
            }
        });
    }

    private void loadMatieres() {
        matieresFlowPane.getChildren().clear();
        List<Matiere> matieres = serviceMatiere.getAll();

        if (matieres.isEmpty()) {
            Label noResults = new Label("Aucune matière disponible");
            noResults.getStyleClass().add("placeholder-text");
            matieresFlowPane.getChildren().add(noResults);
        } else {
            // Ajouter les cartes avec un délai pour l'animation
            for (int i = 0; i < matieres.size(); i++) {
                createMatiereCard(matieres.get(i), i);
            }
        }
    }

    private void createMatiereCard(Matiere matiere, int index) {
        // Création de la carte avec taille fixe
        StackPane cardContainer = new StackPane();
        cardContainer.setPrefSize(CARD_WIDTH, CARD_HEIGHT);

        VBox card = new VBox(12);
        card.getStyleClass().add("matiere-card");
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setPadding(new Insets(20));

        // Traitement de la couleur du thème
        String themeColor = matiere.getCouleurTheme();
        Color baseColor;

        if (themeColor != null && !themeColor.isEmpty()) {
            try {
                baseColor = Color.web(themeColor);
            } catch (Exception e) {
                // Couleur par défaut si la couleur est invalide
                baseColor = Color.web("#6366f1");
            }
        } else {
            // Couleur par défaut si pas de couleur définie
            baseColor = Color.web("#6366f1");
        }

        // Créer un dégradé de couleurs pour le fond de la carte
        Color lighterColor = baseColor.deriveColor(0, 0.7, 1.3, 1.0);
        Color darkerColor = baseColor.deriveColor(0, 1.2, 0.7, 1.0);

        // Convertir les couleurs en format de chaîne hexadécimale
        String baseColorHex = String.format("#%02X%02X%02X",
                (int)(baseColor.getRed() * 255),
                (int)(baseColor.getGreen() * 255),
                (int)(baseColor.getBlue() * 255));

        String lighterColorHex = String.format("#%02X%02X%02X",
                (int)(lighterColor.getRed() * 255),
                (int)(lighterColor.getGreen() * 255),
                (int)(lighterColor.getBlue() * 255));

        String darkerColorHex = String.format("#%02X%02X%02X",
                (int)(darkerColor.getRed() * 255),
                (int)(darkerColor.getGreen() * 255),
                (int)(darkerColor.getBlue() * 255));

        // Appliquer le dégradé et les styles à la carte
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15px;" +
                        "-fx-border-width: 0;" +
                        "-fx-border-radius: 15px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(" +
                        (int)(baseColor.getRed() * 255) + "," +
                        (int)(baseColor.getGreen() * 255) + "," +
                        (int)(baseColor.getBlue() * 255) + ", 0.4), 15, 0, 0, 5);"
        );

        // Créer un élément visuel pour la couleur (barre supérieure)
        Rectangle colorBar = new Rectangle(CARD_WIDTH, 8);
        colorBar.setFill(Color.web(baseColorHex));
        colorBar.setArcWidth(15);
        colorBar.setArcHeight(15);

        // Créer les labels avec une meilleure présentation
        Label titreLabel = new Label(matiere.getTitre());
        titreLabel.getStyleClass().add("card-title");
        titreLabel.setStyle("-fx-text-fill: " + darkerColorHex + ";");

        // Affichage de la catégorie avec un style distinctif
        HBox categorieBox = new HBox(5);
        categorieBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Rectangle categoryIndicator = new Rectangle(8, 8);
        categoryIndicator.setFill(Color.web(baseColorHex));
        categoryIndicator.setArcWidth(2);
        categoryIndicator.setArcHeight(2);

        Label categorieLabel = new Label(
                matiere.getCategorie() != null ? matiere.getCategorie().getName() : "Non définie");
        categorieLabel.getStyleClass().add("card-category");
        categorieLabel.setStyle("-fx-text-fill: " + baseColorHex + ";");

        categorieBox.getChildren().addAll(categoryIndicator, categorieLabel);

        // Description avec un style adapté
        Label descriptionLabel = new Label(matiere.getDescription().length() > 120 ?
                matiere.getDescription().substring(0, 117) + "..." : matiere.getDescription());
        descriptionLabel.getStyleClass().add("card-description");
        descriptionLabel.setWrapText(true);

        // Prérequis avec une présentation plus claire
        HBox prerequisBox = new HBox(5);
        prerequisBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label prerequisTitleLabel = new Label("Prérequis:");
        prerequisTitleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");

        Label prerequisValueLabel = new Label(
                matiere.getPrerequis() != null && !matiere.getPrerequis().isEmpty() ?
                        matiere.getPrerequis() : "Aucun");
        prerequisValueLabel.getStyleClass().add("card-prerequis");

        prerequisBox.getChildren().addAll(prerequisTitleLabel, prerequisValueLabel);

        // Créer les boutons d'action avec un style moderne
        HBox actionsBox = new HBox(12);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);
        actionsBox.setPadding(new Insets(10, 0, 0, 0));

        Button detailButton = new Button("👁️ Détails");
        detailButton.getStyleClass().addAll("btn", "btn-info", "btn-sm");
        detailButton.setStyle("-fx-background-color: " + baseColorHex + ";");
        detailButton.setOnAction(e -> showDetailsAction(matiere));

        Button editButton = new Button("✏️ Modifier");
        editButton.getStyleClass().addAll("btn", "btn-primary", "btn-sm");
        editButton.setStyle("-fx-background-color: " + lighterColorHex + ";");
        editButton.setOnAction(e -> editAction(matiere));

        Button deleteButton = new Button("🗑️ Supprimer");
        deleteButton.getStyleClass().addAll("btn", "btn-danger", "btn-sm");
        deleteButton.setOnAction(e -> deleteAction(matiere));

        actionsBox.getChildren().addAll(detailButton, editButton, deleteButton);

        // Ajouter un espace flexible pour pousser les boutons vers le bas
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(colorBar, titreLabel, categorieBox, descriptionLabel,
                prerequisBox, spacer, actionsBox);

        // Ajouter la carte au conteneur
        cardContainer.getChildren().add(card);

        // Ajouter des animations au survol
        setupCardAnimations(card);

        // Ajouter le conteneur au FlowPane avec animation d'entrée
        matieresFlowPane.getChildren().add(cardContainer);
        animateCardAppearance(card, index);
    }

    private void setupCardAnimations(VBox card) {
        // Animation au survol
        card.setOnMouseEntered(event -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), card);
            scaleTransition.setToX(1.03);
            scaleTransition.setToY(1.03);
            scaleTransition.play();

            // Effet de lueur pour un look moderne
            Glow glow = new Glow();
            glow.setLevel(0.2);
            card.setEffect(glow);

            // Animation de translation vers le haut
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), card);
            translateTransition.setToY(-5);
            translateTransition.play();
        });

        card.setOnMouseExited(event -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), card);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();

            // Restaurer l'effet d'origine
            card.setEffect(null);

            // Animation de translation vers sa position d'origine
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), card);
            translateTransition.setToY(0);
            translateTransition.play();
        });

        // Animation au clic
        card.setOnMousePressed(event -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), card);
            scaleTransition.setToX(0.98);
            scaleTransition.setToY(0.98);
            scaleTransition.play();
        });

        card.setOnMouseReleased(event -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), card);
            scaleTransition.setToX(1.03);
            scaleTransition.setToY(1.03);
            scaleTransition.play();
        });
    }

    // Animation pour l'apparition des cartes lors du chargement initial
    private void animateCardAppearance(VBox card, int index) {
        card.setOpacity(0);
        card.setTranslateY(30);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(400), card);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setDelay(Duration.millis(70 * index));

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), card);
        translateTransition.setFromY(30);
        translateTransition.setToY(0);
        translateTransition.setDelay(Duration.millis(70 * index));

        fadeTransition.play();
        translateTransition.play();
    }

    @FXML
    void createAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterMatiere.fxml"));
            Scene scene = new Scene(loader.load(), 800, 800);

            Stage stage = (Stage) matieresFlowPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Ajouter une Matière");
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire d'ajout", e.getMessage());
        }
    }

    private void showDetailsAction(Matiere matiere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsMatiere.fxml"));
            Scene scene = new Scene(loader.load(), 800, 700);

            DetailsMatiereController controller = loader.getController();
            controller.setMatiere(matiere);

            Stage stage = (Stage) matieresFlowPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Détails de la Matière: " + matiere.getTitre());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les détails",
                    "Une erreur est survenue lors de l'affichage des détails: " + e.getMessage());
        }
    }

    private void editAction(Matiere matiere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditMatiere.fxml"));
            Scene scene = new Scene(loader.load(), 800, 800);

            EditMatiereController controller = loader.getController();
            controller.setMatiere(matiere);

            Stage stage = (Stage) matieresFlowPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Modifier une Matière");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire de modification.", e.getMessage());
        }
    }

    private void deleteAction(Matiere matiere) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer la matière " + matiere.getTitre() + " ?");
        if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            try {
                serviceMatiere.delete(matiere);
                loadMatieres(); // Recharger toutes les matières après suppression
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Suppression réussie", "La matière a été supprimée avec succès.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Échec de la suppression", e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void handleListes(ActionEvent event) {
        System.out.println("handleListes clicked");
        loadScene("/ListeCategories.fxml");
    }

    private void loadScene(String fxmlPath) {
        try {
            // Load the new FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 1000, 700);
            // Get the current stage from a known node
            Stage stage = (Stage) matieresFlowPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
