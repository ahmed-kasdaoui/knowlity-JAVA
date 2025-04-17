package controllers;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MatiereListController {

    @FXML
    private TableView<Matiere> matiereTable;
    @FXML
    private TableColumn<Matiere, Number> idColumn;
    @FXML
    private TableColumn<Matiere, String> titreColumn;
    @FXML
    private TableColumn<Matiere, String> createdAtColumn;
    @FXML
    private TableColumn<Matiere, String> updatedAtColumn;
    @FXML
    private TableColumn<Matiere, Void> actionsColumn;
    @FXML
    private Button newMatiereButton;

    private final ObservableList<Matiere> matiereList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Initialize table columns
        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));
        titreColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitre()));
        createdAtColumn.setCellValueFactory(cellData -> {
            LocalDateTime createdAt = cellData.getValue().getCreatedAt();
            return new SimpleStringProperty(createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        });
        updatedAtColumn.setCellValueFactory(cellData -> {
            LocalDateTime updatedAt = cellData.getValue().getUpdatedAt();
            return new SimpleStringProperty(updatedAt != null ? updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        });

        // Set up actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("Voir");
            private final Button editButton = new Button("Modifier");

            {
                viewButton.getStyleClass().addAll("btn", "info");
                editButton.getStyleClass().addAll("btn", "warning");
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
                    HBox buttons = new HBox(5, viewButton, editButton);
                    setGraphic(buttons);
                }
            }
        });

        // Load sample data
        loadSampleData();
        matiereTable.setItems(matiereList);

        // Handle empty table
        matiereTable.setPlaceholder(new Label("Aucune matière trouvée"));

        // Set up new matiere button action
        newMatiereButton.setOnAction(event -> handleNewMatiereAction());
    }

    private void loadSampleData() {
        matiereList.add(new Matiere(1, "Mathématiques", LocalDateTime.now().minusDays(5), LocalDateTime.now()));
        matiereList.add(new Matiere(2, "Physique", LocalDateTime.now().minusDays(3), null));
        matiereList.add(new Matiere(3, "Chimie", LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1)));
    }

    private void handleViewAction(Matiere matiere) {
        showAlert("Voir", "Affichage de la matière: " + matiere.getTitre());
    }

    private void handleEditAction(Matiere matiere) {
        showAlert("Modifier", "Modification de la matière: " + matiere.getTitre());
    }

    private void handleNewMatiereAction() {
        showAlert("Créer", "Création d'une nouvelle matière");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

class Matiere {
    private final int id;
    private final String titre;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Matiere(int id, String titre, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.titre = titre;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}