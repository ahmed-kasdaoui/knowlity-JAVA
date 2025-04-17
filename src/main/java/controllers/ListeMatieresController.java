package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.esprit.models.Matiere;
import tn.esprit.services.ServiceMatiere;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ListeMatieresController {

    @FXML
    private AnchorPane root;

    @FXML
    private VBox mainBox;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TableView<Matiere> matieresTable;

    @FXML
    private TableColumn<Matiere, String> titreColumn;

    @FXML
    private TableColumn<Matiere, String> categorieColumn;

    @FXML
    private TableColumn<Matiere, String> prerequisColumn;

    @FXML
    private TableColumn<Matiere, String> descriptionColumn;

    @FXML
    private TableColumn<Matiere, Void> couleurThemeColumn;

    @FXML
    private TableColumn<Matiere, Void> actionsColumn;

    @FXML
    private Button createButton;

    private ServiceMatiere serviceMatiere = new ServiceMatiere();

    @FXML
    public void initialize() {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Configuration des colonnes
        titreColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitre()));
        categorieColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCategorie() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategorie().getName());
            } else {
                return new javafx.beans.property.SimpleStringProperty("Non définie");
            }
        });
        prerequisColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPrerequis()));
        descriptionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));

        // Configuration de la colonne couleurTheme pour afficher la couleur
        couleurThemeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Matiere matiere = getTableRow().getItem();
                    if (matiere.getCouleurTheme() != null && !matiere.getCouleurTheme().isEmpty()) {
                        try {
                            Rectangle colorRect = new Rectangle(80, 30);
                            colorRect.setFill(Color.web(matiere.getCouleurTheme()));
                            colorRect.setStroke(Color.BLACK);
                            colorRect.setStrokeWidth(1);
                            setGraphic(colorRect);
                        } catch (Exception e) {
                            setGraphic(new Label("Couleur invalide"));
                        }
                    } else {
                        setGraphic(new Label("Non définie"));
                    }
                }
                setAlignment(Pos.CENTER);
            }
        });

        // Configuration de la colonne actions
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Matiere matiere = getTableRow().getItem();
                    HBox buttons = new HBox(10);
                    buttons.setAlignment(Pos.CENTER);

                    Button detailButton = new Button("Détails");
                    detailButton.getStyleClass().addAll("btn", "btn-info", "btn-sm");
                    detailButton.setOnAction(e -> showDetailsAction(matiere));

                    Button editButton = new Button("Modifier");
                    editButton.getStyleClass().addAll("btn", "btn-primary", "btn-sm");
                    editButton.setOnAction(e -> editAction(matiere));

                    Button deleteButton = new Button("Supprimer");
                    deleteButton.getStyleClass().addAll("btn", "btn-danger", "btn-sm");
                    deleteButton.setOnAction(e -> deleteAction(matiere));

                    buttons.getChildren().addAll(detailButton, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });

        // Chargement des données
        matieresTable.getItems().setAll(serviceMatiere.getAll());
    }

    @FXML
    void createAction(ActionEvent event) {
        try {FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterMatiere.fxml"));
            Scene scene = new Scene(loader.load(), 800, 800); // Match CourseDetails.fxml dimensions

            Stage stage = (Stage) matieresTable.getScene().getWindow(); // Adjust to your @FXML node
            stage.setScene(scene);
            stage.setTitle("Liste des Matieres ");
            stage.show();


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void showDetailsAction(Matiere matiere) {
        try {
            // Charger le FXML pour les détails
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsMatiere.fxml"));
            Scene scene = new Scene(loader.load(), 800, 700);

            // Obtenir le contrôleur et lui passer la matière à afficher
            DetailsMatiereController controller = loader.getController();
            controller.setMatiere(matiere);

            // Afficher la nouvelle scène
            Stage stage = (Stage) matieresTable.getScene().getWindow();
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

            // Assurez-vous que le chemin est correct et que le FXML est associé à EditMatiereController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditMatiere.fxml"));
            Scene scene = new Scene(loader.load(), 800, 800);

            // Obtenez le contrôleur correct
            EditMatiereController controller = loader.getController();
            controller.setMatiere(matiere);

            Stage stage = (Stage) matieresTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Modifier une Matière");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire de modification.", e.getMessage());
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
                matieresTable.getItems().remove(matiere);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Suppression réussie", "La matière a été supprimée avec succès.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression", e.getMessage());
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
    void handleListes(ActionEvent event) {
        System.out.println("handleListes clicked");
        loadScene("/ListeCategories.fxml");
    }
    private void loadScene(String fxmlPath) {
        try {
            // Load the new FXML
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Get the current stage from a known node
            Stage stage = (Stage) matieresTable.getScene().getWindow();
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
