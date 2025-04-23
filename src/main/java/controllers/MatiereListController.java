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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import tn.esprit.services.ServiceMatiere;
import tn.esprit.models.Matiere;

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

    private final ObservableList<Matiere> matiereList = FXCollections.observableArrayList();
    private final ServiceMatiere matiereService = new ServiceMatiere();
    private FilteredList<Matiere> filteredList;

    @FXML
    public void initialize() {
        // Initialize table columns
        titreColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitre()));
        categorieColumn.setCellValueFactory(cellData -> {
            String categoryName = cellData.getValue().getCategorie() != null
                    ? cellData.getValue().getCategorie().getName()
                    : "Non spécifiée";
            return new SimpleStringProperty(categoryName);
        });
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        prerequisColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrerequis()));
        couleurThemeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCouleurTheme()));

        // Text wrapping for description and prerequis columns
        descriptionColumn.setCellFactory(col -> new TableCell<Matiere, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setWrapText(true);
                setStyle("-fx-padding: 8px; -fx-text-fill: #1e293b;");
            }
        });
        prerequisColumn.setCellFactory(col -> new TableCell<Matiere, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setWrapText(true);
                setStyle("-fx-padding: 8px; -fx-text-fill: #1e293b;");
            }
        });

        // Color theme as circles
        couleurThemeColumn.setCellFactory(col -> new TableCell<Matiere, String>() {
            private final Circle colorCircle = new Circle(12);

            {
                colorCircle.getStyleClass().add("color-circle");
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    colorCircle.setStyle("-fx-fill: " + item + ";");
                    setGraphic(colorCircle);
                }
            }
        });

        // Actions column with modern buttons
        actionsColumn.setCellFactory(param -> new TableCell<Matiere, Void>() {
            private final Button viewButton = new Button("Voir");
            private final Button editButton = new Button("Modifier");

            {
                viewButton.getStyleClass().addAll("btn", "btn-info", "btn-sm");
                editButton.getStyleClass().addAll("btn", "btn-warning", "btn-sm");
                viewButton.setOnAction(event -> {
                    Matiere matiere = getTableView().getItems().get(getIndex());
                    handleViewAction(matiere);
                });
                editButton.setOnAction(event -> {
                    Matiere matiere = getTableView().getItems().get(getIndex());
                    handleEditAction(matiere);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(10, viewButton, editButton);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        // Set responsive column widths
        matieresTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue();
            double minWidth = 80.0; // Minimum width for non-actions columns
            double actionsMinWidth = 100.0; // Minimum width for actions column
            double actionsWeight = 0.3; // Actions column takes 50% of available width

            // Calculate widths
            double actionsWidth = Math.max(actionsMinWidth, totalWidth * actionsWeight);
            double remainingWidth = totalWidth - actionsWidth;
            double otherColumnWidth = Math.max(minWidth, remainingWidth / 5); // Split remaining width among 5 columns

            // Set widths
            titreColumn.setPrefWidth(otherColumnWidth);
            categorieColumn.setPrefWidth(otherColumnWidth);
            descriptionColumn.setPrefWidth(otherColumnWidth);
            prerequisColumn.setPrefWidth(otherColumnWidth);
            couleurThemeColumn.setPrefWidth(otherColumnWidth);
            actionsColumn.setPrefWidth(actionsWidth);

            // Set minimum widths to prevent columns from becoming too narrow
            titreColumn.setMinWidth(minWidth);
            categorieColumn.setMinWidth(minWidth);
            descriptionColumn.setMinWidth(minWidth);
            prerequisColumn.setMinWidth(minWidth);
            couleurThemeColumn.setMinWidth(minWidth);
            actionsColumn.setMinWidth(actionsMinWidth);
        });

        // Initialize filtered list for search
        filteredList = new FilteredList<>(matiereList, p -> true);
        matieresTable.setItems(filteredList);

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
        });

        // Load data
        loadMatieres();

        // Handle empty table
        matieresTable.setPlaceholder(new Label("Aucune matière trouvée"));
    }

    private void loadMatieres() {
        matiereList.clear();
        matiereList.addAll(matiereService.getAll());
    }
    @FXML
    private void createAction(ActionEvent event) {
        try {
            // Load the AjouterMatiere.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterMatiere.fxml"));
            Parent root = loader.load();

            // Create a new stage for the add page
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une Matière");
            stage.show();

            // Optionally, close the current window or keep it open
            // If you want to close the current window, uncomment the line below
            // ((Stage) createButton.getScene().getWindow()).close();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page d'ajout: " + e.getMessage());
        }
    }

    private void handleViewAction(Matiere matiere) {
        // Keeping as alert since no view FXML was specified
        showAlert("Voir", "Affichage de la matière: " + matiere.getTitre());
    }

    private void handleEditAction(Matiere matiere) {
        try {
            // Load the EditMatiere.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditMatiere.fxml"));
            Parent root = loader.load();

            // Get the controller for EditMatiere.fxml and pass the Matiere object
            Object controller = loader.getController();
            if (controller instanceof EditMatiereController) {
                ((EditMatiereController) controller).setMatiere(matiere);
            } else {
                showAlert("Erreur", "Le contrôleur de la page d'édition n'est pas compatible.");
                return;
            }

            // Create a new stage for the edit page
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier la Matière: " + matiere.getTitre());
            stage.show();

            // Optionally, close the current window or keep it open
            // If you want to close the current window, uncomment the line below
            // ((Stage) matieresTable.getScene().getWindow()).close();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page d'édition: " + e.getMessage());
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}