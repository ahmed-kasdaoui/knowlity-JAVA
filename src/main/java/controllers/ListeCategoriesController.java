package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import tn.esprit.models.Categorie;
import tn.esprit.services.ServiceCategorie;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class ListeCategoriesController {

    @FXML
    private AnchorPane root;

    @FXML
    private VBox mainBox;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TableView<Categorie> categoriesTable;

    @FXML
    private TableColumn<Categorie, String> nameColumn;

    @FXML
    private TableColumn<Categorie, String> descriptionColumn;

    @FXML
    private TableColumn<Categorie, Void> iconeColumn;

    @FXML
    private TableColumn<Categorie, Void> actionsColumn;

    @FXML
    private Button createButton;

    private ServiceCategorie serviceCategorie = new ServiceCategorie();

    @FXML
    public void initialize() {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        descriptionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescrption()));

        iconeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Categorie categorie = getTableRow().getItem();
                    if (categorie.getIcone() != null && !categorie.getIcone().isEmpty()) {
                        ImageView imageView = new ImageView();
                        try {
                            imageView.setImage(new Image("file:Uploads/" + categorie.getIcone()));
                        } catch (Exception e) {
                            imageView.setImage(new Image("file:Uploads/default.jpg"));
                        }
                        imageView.setFitWidth(87);
                        imageView.setFitHeight(87);
                        setGraphic(imageView);
                    } else {
                        setGraphic(new Label("No icon"));
                    }
                }
                setAlignment(Pos.CENTER);
            }
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Categorie categorie = getTableRow().getItem();
                    HBox buttons = new HBox(10);
                    buttons.setAlignment(Pos.CENTER);


                    Button editButton = new Button("Edit");
                    editButton.getStyleClass().addAll("btn", "btn-primary", "btn-sm");
                    editButton.setOnAction(e -> editAction(categorie));

                    Button deleteButton = new Button("Delete");
                    deleteButton.getStyleClass().addAll("btn", "btn-danger", "btn-sm");
                    deleteButton.setOnAction(e -> deleteAction(categorie));

                    buttons.getChildren().addAll( editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });

        categoriesTable.getItems().setAll(serviceCategorie.getAll());
    }

    @FXML
    void createAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditCategorie.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            Stage stage = (Stage) createButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Créer une Catégorie");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAction(Integer id) {
        System.out.println("Afficher catégorie ID: " + id);
    }

    private void editAction(Categorie categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditCategorie.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);
            EditCategorieController controller = loader.getController();
            controller.setCategorie(categorie);
            Stage stage = (Stage) categoriesTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Éditer une Catégorie");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteAction(Categorie categorie) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer la catégorie " + categorie.getName() + " ?");
        if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            serviceCategorie.delete(categorie);
            categoriesTable.getItems().remove(categorie);
        }
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