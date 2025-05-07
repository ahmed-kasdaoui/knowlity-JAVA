package view;

import Entities.Creator;
import Services.CreatorService;
import Utils.ImageUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CreatorListController {
    @FXML private TextField searchField;
    @FXML private VBox creatorContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private Button blogsButton;
    @FXML private Button logoutButton;
    @FXML private HBox adminNavbar;

    private CreatorService creatorService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        creatorService = new CreatorService();
        loadCreators();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCreators(newValue);
        });
    }

    private void loadCreators() {
        List<Creator> creators = creatorService.getAll();
        creatorContainer.getChildren().clear();
        creators.forEach(this::createCreatorCard);
    }

    private void filterCreators(String searchText) {
        creatorContainer.getChildren().clear();
        creatorService.getAll().stream()
                .filter(creator -> creator.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                        creator.getProfile().toLowerCase().contains(searchText.toLowerCase()))
                .forEach(this::createCreatorCard);
    }

    private void createCreatorCard(Creator creator) {
        VBox card = new VBox(10);
        card.getStyleClass().add("blog-card");

        // En-tête avec image et nom
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(60);
        imageView.setFitHeight(60);
        imageView.setPreserveRatio(true);
        
        try {
            Image image = new Image(creator.getImage());
            imageView.setImage(image);
        } catch (Exception e) {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-avatar.png"));
            imageView.setImage(defaultImage);
        }

        VBox titleBox = new VBox(5);
        Label nameLabel = new Label(creator.getName());
        nameLabel.getStyleClass().add("blog-title");
        Label profileLabel = new Label(creator.getProfile());
        profileLabel.getStyleClass().add("blog-description");
        titleBox.getChildren().addAll(nameLabel, profileLabel);

        header.getChildren().addAll(imageView, titleBox);

        // Contenu
        Label achievementsLabel = new Label(creator.getAchievements());
        achievementsLabel.setWrapText(true);
        achievementsLabel.getStyleClass().add("blog-description");

        // Dates et actions
        HBox footer = new HBox(15);
        footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        VBox datesBox = new VBox(5);
        Label createdLabel = new Label("Créé le: " + creator.getCreatedAt().toLocalDateTime().format(DATE_FORMATTER));
        Label updatedLabel = new Label("Modifié le: " + creator.getUpdatedAt().toLocalDateTime().format(DATE_FORMATTER));
        createdLabel.getStyleClass().add("date-label");
        updatedLabel.getStyleClass().add("date-label");
        datesBox.getChildren().addAll(createdLabel, updatedLabel);

        HBox actionsBox = new HBox(10);
        Button editButton = createIconButton("fas-edit", "edit-button", e -> handleEdit(creator));
        Button deleteButton = createIconButton("fas-trash-alt", "delete-button", e -> handleDelete(creator));
        actionsBox.getChildren().addAll(editButton, deleteButton);

        footer.getChildren().addAll(datesBox, actionsBox);

        card.getChildren().addAll(header, achievementsLabel, footer);
        creatorContainer.getChildren().add(card);

        // Ajouter un gestionnaire de clic pour ouvrir le profil
        card.setOnMouseClicked(event -> showCreatorProfile(creator));
    }

    private Button createIconButton(String iconLiteral, String styleClass, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button();
        button.getStyleClass().add(styleClass);
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.valueOf(iconLiteral.replace("fas-", "")));
        button.setGraphic(icon);
        button.setOnAction(handler);
        return button;
    }

    @FXML
    private void handleNewCreator() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/creator_form.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nouveau Créateur");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadCreators();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire de création");
        }
    }

    private void handleEdit(Creator creator) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/creator_form.fxml"));
            Parent root = loader.load();
            CreatorFormController controller = loader.getController();
            controller.setCreator(creator);
            Stage stage = new Stage();
            stage.setTitle("Modifier Créateur");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadCreators();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire de modification");
        }
    }

    private void handleDelete(Creator creator) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le créateur");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + creator.getName() + " ?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            creatorService.delete(creator.getId());
            loadCreators();
        }
    }

    @FXML
    private void handleBlogsNav(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/blog_list.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            
            // Configurer le mode admin
            BlogListController controller = loader.getController();
            controller.setAdminMode(true);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la liste des blogs");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/role_selection.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de se déconnecter");
        }
    }

    private void showCreatorProfile(Creator creator) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/creator_profile.fxml"));
            Parent root = loader.load();
            
            CreatorProfileController controller = loader.getController();
            controller.setCreator(creator);
            
            Stage stage = new Stage();
            stage.setTitle("Profil de " + creator.getName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'afficher le profil du créateur");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
