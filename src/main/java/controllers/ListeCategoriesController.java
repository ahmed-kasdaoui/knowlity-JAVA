package controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.models.Categorie;
import tn.esprit.services.ServiceCategorie;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ListeCategoriesController {

    @FXML
    private FlowPane cardsContainer;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private StackPane emptyState;
    
    private final ServiceCategorie serviceCategorie = new ServiceCategorie();
    private FilteredList<Categorie> filteredCategories;

    @FXML
    public void initialize() {
        loadCategories();
        setupSearch();
    }

    private void loadCategories() {
        List<Categorie> categories = serviceCategorie.getAll();
        filteredCategories = new FilteredList<>(FXCollections.observableArrayList(categories));
        updateCardsDisplay();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCategories.setPredicate(categorie -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return categorie.getName().toLowerCase().contains(lowerCaseFilter) ||
                       categorie.getDescrption().toLowerCase().contains(lowerCaseFilter);
            });
            updateCardsDisplay();
        });
    }

    private void updateCardsDisplay() {
        cardsContainer.getChildren().clear();
        
        if (filteredCategories.isEmpty()) {
            emptyState.setVisible(true);
            cardsContainer.setVisible(false);
        } else {
            emptyState.setVisible(false);
            cardsContainer.setVisible(true);
            
            for (Categorie categorie : filteredCategories) {
                cardsContainer.getChildren().add(createCategoryCard(categorie));
            }
        }
    }

    private VBox createCategoryCard(Categorie categorie) {
        VBox card = new VBox(10);
        card.getStyleClass().add("category-card");
        card.setAlignment(Pos.CENTER);
        
        // Image
        ImageView imageView = new ImageView();
        try {
            File imageFile = new File("Uploads/" + categorie.getIcone());
            if (imageFile.exists()) {
                imageView.setImage(new Image(imageFile.toURI().toString()));
            } else {
                FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.TAGS);
                iconView.getStyleClass().add("category-icon");
                card.getChildren().add(iconView);
            }
        } catch (Exception e) {
            FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.TAGS);
            iconView.getStyleClass().add("category-icon");
            card.getChildren().add(iconView);
        }
        
        if (imageView.getImage() != null) {
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
            card.getChildren().add(imageView);
        }
        
        // Title
        Label titleLabel = new Label(categorie.getName());
        titleLabel.getStyleClass().add("category-title");
        card.getChildren().add(titleLabel);
        
        // Description
        Label descLabel = new Label(categorie.getDescrption());
        descLabel.getStyleClass().add("category-description");
        descLabel.setWrapText(true);
        card.getChildren().add(descLabel);
        
        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        
        Button editButton = createActionButton(FontAwesomeIcon.EDIT, "Modifier");
        editButton.setOnAction(e -> editAction(categorie));
        
        Button deleteButton = createActionButton(FontAwesomeIcon.TRASH, "Supprimer");
        deleteButton.setOnAction(e -> deleteAction(categorie));
        
        actions.getChildren().addAll(editButton, deleteButton);
        card.getChildren().add(actions);
        
        return card;
    }

    private Button createActionButton(FontAwesomeIcon icon, String tooltip) {
        Button button = new Button();
        button.getStyleClass().add("action-button");
        
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        button.setGraphic(iconView);
        
        Tooltip.install(button, new Tooltip(tooltip));
        
        return button;
    }

    @FXML
    void createAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterCategorie.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Créer une Catégorie");
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Get the controller
            AjouterCategorieController controller = loader.getController();
            
            // Set up a callback for when the operation is complete
            controller.setOnSaveCallback(() -> {
                loadCategories(); // Refresh the list
                stage.close(); // Close the window
            });
            
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    private void editAction(Categorie categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditCategorie.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Éditer une Catégorie");
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Get the controller and set the category
            EditCategorieController controller = loader.getController();
            controller.setCategorie(categorie);
            
            // Set up a callback for when the operation is complete
            controller.setOnSaveCallback(() -> {
                loadCategories(); // Refresh the list
                stage.close(); // Close the window
            });
            
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire d'édition");
        }
    }

    private void deleteAction(Categorie categorie) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer la catégorie " + categorie.getName() + " ?");
        
        if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            try {
                serviceCategorie.delete(categorie);
                loadCategories();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "La catégorie a été supprimée avec succès");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la catégorie");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}