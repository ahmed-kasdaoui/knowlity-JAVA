package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.Modality;
import tn.esprit.services.ServiceMatiere;
import tn.esprit.models.Matiere;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.List;
import java.util.Arrays;
import java.util.Random;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import java.util.Optional;
import java.sql.SQLException;

import java.io.IOException;

public class MatiereListController {

    @FXML
    private TableView<Matiere> matieresTable;
    @FXML
    private TableColumn<Matiere, String> titreColumn;
    @FXML
    private TableColumn<Matiere, String> categorieColumn;
    @FXML
    private TableColumn<Matiere, String> descriptionColumn;
    @FXML
    private TableColumn<Matiere, String> prerequisColumn;
    @FXML
    private TableColumn<Matiere, String> couleurThemeColumn;
    @FXML
    private TableColumn<Matiere, Void> actionsColumn;
    @FXML
    private Button createButton;
    @FXML
    private TextField searchField;
    @FXML
    private FlowPane matieresFlowPane;
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private StackPane emptyState;

    private final ObservableList<Matiere> matiereList = FXCollections.observableArrayList();
    private final ServiceMatiere matiereService = new ServiceMatiere();
    private FilteredList<Matiere> filteredList;
    private static final FontAwesomeIcon[] ICONS = {
        FontAwesomeIcon.BOOK,
        FontAwesomeIcon.GRADUATION_CAP,
        FontAwesomeIcon.FLASK,
        FontAwesomeIcon.CALCULATOR,
        FontAwesomeIcon.CODE,
        FontAwesomeIcon.UNIVERSITY
    };
    private final int[] VARIANTS = {1, 2, 3};
    private Random random = new Random();

    @FXML
    public void initialize() {
        // Initialize table columns
        // titreColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitre()));
        // categorieColumn.setCellValueFactory(cellData -> {
        //     String categoryName = cellData.getValue().getCategorie() != null
        //             ? cellData.getValue().getCategorie().getName()
        //             : "Non spécifiée";
        //     return new SimpleStringProperty(categoryName);
        // });
        // descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        // prerequisColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrerequis()));
        // couleurThemeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCouleurTheme()));

        // Text wrapping for description and prerequis columns
        // descriptionColumn.setCellFactory(col -> new TableCell<Matiere, String>() {
        //     @Override
        //     protected void updateItem(String item, boolean empty) {
        //         super.updateItem(item, empty);
        //         setText(empty || item == null ? null : item);
        //         setWrapText(true);
        //         setStyle("-fx-padding: 8px; -fx-text-fill: #1e293b;");
        //     }
        // });
        // prerequisColumn.setCellFactory(col -> new TableCell<Matiere, String>() {
        //     @Override
        //     protected void updateItem(String item, boolean empty) {
        //         super.updateItem(item, empty);
        //         setText(empty || item == null ? null : item);
        //         setWrapText(true);
        //         setStyle("-fx-padding: 8px; -fx-text-fill: #1e293b;");
        //     }
        // });

        // Color theme as circles
        // couleurThemeColumn.setCellFactory(col -> new TableCell<Matiere, String>() {
        //     private final Circle colorCircle = new Circle(12);

        //     {
        //         colorCircle.getStyleClass().add("color-circle");
        //         setAlignment(Pos.CENTER);
        //     }

        //     @Override
        //     protected void updateItem(String item, boolean empty) {
        //         super.updateItem(item, empty);
        //         if (empty || item == null) {
        //             setGraphic(null);
        //         } else {
        //             colorCircle.setStyle("-fx-fill: " + item + ";");
        //             setGraphic(colorCircle);
        //         }
        //     }
        // });

        // Actions column with modern buttons
        // actionsColumn.setCellFactory(param -> new TableCell<Matiere, Void>() {
        //     private final Button viewButton = new Button("Voir");
        //     private final Button editButton = new Button("Modifier");

        //     {
        //         viewButton.getStyleClass().addAll("btn", "btn-info", "btn-sm");
        //         editButton.getStyleClass().addAll("btn", "btn-warning", "btn-sm");
        //         viewButton.setOnAction(event -> {
        //             Matiere matiere = getTableView().getItems().get(getIndex());
        //             handleViewAction(matiere);
        //         });
        //         editButton.setOnAction(event -> {
        //             Matiere matiere = getTableView().getItems().get(getIndex());
        //             handleEditAction(matiere);
        //         });
        //     }

        //     @Override
        //     protected void updateItem(Void item, boolean empty) {
        //         super.updateItem(item, empty);
        //         if (empty) {
        //             setGraphic(null);
        //         } else {
        //             HBox buttons = new HBox(10, viewButton, editButton);
        //             buttons.setAlignment(Pos.CENTER);
        //             setGraphic(buttons);
        //         }
        //     }
        // });

        // Set responsive column widths
        // matieresTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
        //     double totalWidth = newWidth.doubleValue();
        //     double minWidth = 80.0; // Minimum width for non-actions columns
        //     double actionsMinWidth = 100.0; // Minimum width for actions column
        //     double actionsWeight = 0.3; // Actions column takes 50% of available width

        //     // Calculate widths
        //     double actionsWidth = Math.max(actionsMinWidth, totalWidth * actionsWeight);
        //     double remainingWidth = totalWidth - actionsWidth;
        //     double otherColumnWidth = Math.max(minWidth, remainingWidth / 5); // Split remaining width among 5 columns

        //     // Set widths
        //     titreColumn.setPrefWidth(otherColumnWidth);
        //     categorieColumn.setPrefWidth(otherColumnWidth);
        //     descriptionColumn.setPrefWidth(otherColumnWidth);
        //     prerequisColumn.setPrefWidth(otherColumnWidth);
        //     couleurThemeColumn.setPrefWidth(otherColumnWidth);
        //     actionsColumn.setPrefWidth(actionsWidth);

        //     // Set minimum widths to prevent columns from becoming too narrow
        //     titreColumn.setMinWidth(minWidth);
        //     categorieColumn.setMinWidth(minWidth);
        //     descriptionColumn.setMinWidth(minWidth);
        //     prerequisColumn.setMinWidth(minWidth);
        //     couleurThemeColumn.setMinWidth(minWidth);
        //     actionsColumn.setMinWidth(actionsMinWidth);
        // });

        // Initialize filtered list for search
        filteredList = new FilteredList<>(matiereList, p -> true);
        // matieresTable.setItems(filteredList);

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(matiere -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return matiere.getTitre().toLowerCase().contains(lowerCaseFilter) ||
                        (matiere.getCategorie() != null && matiere.getCategorie().getName().toLowerCase().contains(lowerCaseFilter)) ||
                        matiere.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                        matiere.getPrerequis().toLowerCase().contains(lowerCaseFilter);
            });
            updateFlowPane();
        });

        // Load data
        loadMatieres();
        updateFlowPane();

        // Handle empty table
        // matieresTable.setPlaceholder(new Label("Aucune matière trouvée"));
    }

    private void loadMatieres() {
        matiereList.clear();
        matiereList.addAll(matiereService.getAll());
        cardsContainer.getChildren().clear();
        matiereList.forEach(this::createMatiereCard);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (emptyState != null) {
            boolean isEmpty = cardsContainer.getChildren().isEmpty();
            emptyState.setManaged(isEmpty);
            emptyState.setVisible(isEmpty);
        }
    }

    @FXML
    private void createAction(ActionEvent event) {
        try {
            // Charger le fichier FXML avec le bon chemin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterMatiere.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Matière");
            
            // Créer la scène avec des dimensions spécifiques
            Scene scene = new Scene(root, 800, 841);
            
            // Ajouter les styles si nécessaire
            scene.getStylesheets().add(getClass().getResource("/styles/forms2.css").toExternalForm());

            stage.setScene(scene);
            
            // Rendre la fenêtre modale
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner((Stage) ((Node) event.getSource()).getScene().getWindow());
            stage.setResizable(false);
            // Configurer le callback pour le retour
            stage.setOnHidden(e -> {
                loadMatieres();
                updateFlowPane();
            });

            // Afficher la fenêtre
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur de chargement: " + e.getMessage());
            showError("Erreur", "Impossible de charger la page d'ajout: " + e.getMessage());
        }
    }

    private void handleViewAction(Matiere matiere) {
        try {
            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsMatiere.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Détails de la Matière: " + matiere.getTitre());
            
            // Créer la scène
            Scene scene = new Scene(root, 800, 700);
            stage.setScene(scene);
            
            // Rendre la fenêtre modale
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(cardsContainer.getScene().getWindow());
            stage.setResizable(false);
            // Configurer le contrôleur
            DetailsMatiereController controller = loader.getController();
            controller.setMatiere(matiere);

            // Afficher la fenêtre
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'afficher les détails: " + e.getMessage());
        }
    }

    private void handleEditAction(Matiere matiere) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditMatiere.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur et initialiser les données
            EditMatiereController controller = loader.getController();
            controller.setMatiere(matiere);

            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Modifier la Matière: " + matiere.getTitre());
            
            // Créer la scène
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(false);
            // Rendre la fenêtre modale
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(cardsContainer.getScene().getWindow());

            // Configurer le callback pour le retour
            stage.setOnHidden(e -> {
                loadMatieres();
                updateFlowPane();
            });

            // Afficher la fenêtre
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page de modification: " + e.getMessage());
        }
    }

    private void updateFlowPane() {
        if (cardsContainer != null) {
            cardsContainer.getChildren().clear();
            for (Matiere matiere : filteredList) {
                createMatiereCard(matiere);
            }
            updateEmptyState();
        }
    }

    private void handleDeleteAction(Matiere matiere) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer la matière " + matiere.getTitre() + " ?");
        if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            try {
                matiereService.delete(matiere);
                matiereList.remove(matiere);
                updateFlowPane();
                showAlert("Succès", "La matière a été supprimée avec succès.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Échec de la suppression: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void createMatiereCard(Matiere matiere) {
        VBox card = new VBox();
        card.getStyleClass().addAll("matiere-card", "variant-" + VARIANTS[random.nextInt(VARIANTS.length)]);
        
        // Header with icon
        HBox header = new HBox();
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().add("card-icon-container");
        FontAwesomeIconView icon = new FontAwesomeIconView(ICONS[random.nextInt(ICONS.length)]);
        icon.getStyleClass().add("card-icon");
        iconContainer.getChildren().add(icon);
        
        Label title = new Label(matiere.getTitre());
        title.getStyleClass().add("card-title");
        HBox.setHgrow(title, Priority.ALWAYS);
        
        header.getChildren().addAll(iconContainer, title);
        
        // Content sections
        VBox content = new VBox();
        content.setSpacing(10);
        
        // Category section
        VBox categorySection = createSection("Catégorie", 
            matiere.getCategorie() != null ? matiere.getCategorie().getName() : "Non spécifiée", 
            FontAwesomeIcon.TH_LARGE);
        
        // Description section
        VBox descriptionSection = createSection("Description", 
            matiere.getDescription(), 
            FontAwesomeIcon.INFO_CIRCLE);
        
        // Prerequisites section
        VBox prerequisSection = createSection("Prérequis", 
            matiere.getPrerequis(), 
            FontAwesomeIcon.KEY);
        
        content.getChildren().addAll(categorySection, descriptionSection, prerequisSection);
        
        // Action buttons
        HBox actions = new HBox();
        actions.getStyleClass().add("card-actions");
        actions.setAlignment(Pos.CENTER);
        actions.setSpacing(10);
        
        Button viewBtn = createActionButton("Voir", FontAwesomeIcon.EYE, "view-button", 
            e -> handleViewAction(matiere));
        Button editBtn = createActionButton("Modifier", FontAwesomeIcon.PENCIL, "edit-button", 
            e -> handleEditAction(matiere));
        Button deleteBtn = createActionButton("Supprimer", FontAwesomeIcon.TRASH, "delete-button", 
            e -> handleDeleteAction(matiere));
        
        actions.getChildren().addAll(viewBtn, editBtn, deleteBtn);
        
        // Add all components to card
        card.getChildren().addAll(header, content, actions);
        cardsContainer.getChildren().add(card);
    }
    
    private VBox createSection(String title, String content, FontAwesomeIcon iconType) {
        VBox section = new VBox();
        section.getStyleClass().add("card-section");
        
        HBox titleContainer = new HBox();
        titleContainer.setSpacing(5);
        titleContainer.setAlignment(Pos.CENTER_LEFT);
        
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.getStyleClass().add("section-icon");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        
        titleContainer.getChildren().addAll(icon, titleLabel);
        
        Text contentText = new Text(content);
        contentText.getStyleClass().add("section-content");
        contentText.setWrappingWidth(280);
        
        section.getChildren().addAll(titleContainer, contentText);
        return section;
    }
    
    private Button createActionButton(String text, FontAwesomeIcon iconType, String styleClass, 
            javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button();
        button.getStyleClass().addAll("action-button", styleClass);
        
        HBox content = new HBox();
        content.setSpacing(5);
        content.setAlignment(Pos.CENTER);
        
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        Label label = new Label(text);
        
        content.getChildren().addAll(icon, label);
        button.setGraphic(content);
        button.setOnAction(handler);
        
        return button;
    }

    @FXML
    private void handleAddMatiere() {

    }
    
    private void handleViewMatiere(Matiere matiere) {
        // Implement view functionality
    }
    
    private void handleEditMatiere(Matiere matiere) {
        // Implement edit functionality
    }
    
    private void handleDeleteMatiere(Matiere matiere) {
        // Implement delete functionality
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}