package controllers;

import javafx.collections.ListChangeListener;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.models.QuizQuestion;
import tn.esprit.models.QuizResponse;
import tn.esprit.services.ServiceQuizResponse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class ListQuizResponseController {

    @FXML
    private Label questionLabel;

    @FXML
    private ListView<QuizResponse> responseList;

    @FXML
    private Button backButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    private QuizQuestion selectedQuestion;
    private ServiceQuizResponse serviceQuizResponse;

    @FXML
    void initialize() {
        serviceQuizResponse = new ServiceQuizResponse();

        // Configure the ListView to display custom cells
        responseList.setCellFactory(listView -> new ListCell<QuizResponse>() {
            @Override
            protected void updateItem(QuizResponse response, boolean empty) {
                super.updateItem(response, empty);
                if (empty || response == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create a custom layout for each response
                    VBox container = new VBox(8);
                    container.setStyle("-fx-padding: 10;");

                    HBox texteRow = new HBox(10);
                    Label texteLabel = new Label("Texte: " + response.getTexte());
                    texteLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #2D3748;");
                    texteLabel.setWrapText(true);
                    texteLabel.setMaxWidth(700);
                    texteRow.getChildren().add(texteLabel);

                    HBox correcteRow = new HBox(10);
                    Label correcteLabel = new Label("Correcte: " + (response.isEstCorrecte() ? "Oui" : "Non"));
                    correcteLabel.setStyle(response.isEstCorrecte() ?
                            "-fx-font-size: 15; -fx-text-fill: #2F855A; -fx-font-weight: bold;" :
                            "-fx-font-size: 15; -fx-text-fill: #E53E3E; -fx-font-weight: bold;");
                    correcteRow.getChildren().add(correcteLabel);

                    HBox questionRow = new HBox(10);
                    Label questionLabel = new Label("Question Associée: " + (response.getQuestion() != null ? response.getQuestion().getTexte() : "N/A"));
                    questionLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #2D3748;");
                    questionLabel.setWrapText(true);
                    questionLabel.setMaxWidth(700);
                    questionRow.getChildren().add(questionLabel);

                    container.getChildren().addAll(texteRow, correcteRow, questionRow);
                    setGraphic(container);
                }
            }
        });

        // Disable the edit and delete buttons initially
        editButton.setDisable(true);
        deleteButton.setDisable(true);

        // Add a ListChangeListener to enable/disable the buttons based on list contents
        responseList.getItems().addListener((ListChangeListener<QuizResponse>) change -> {
            boolean isEmpty = responseList.getItems().isEmpty();
            editButton.setDisable(isEmpty);
            deleteButton.setDisable(isEmpty);
        });
    }

    public void setQuestion(QuizQuestion question) {
        this.selectedQuestion = question;
        questionLabel.setText("Question : " + question.getTexte());

        // Load responses for the selected question
        loadResponses();
    }

    private void loadResponses() {
        if (selectedQuestion != null) {
            List<QuizResponse> responses = serviceQuizResponse.getAll().stream()
                    .filter(response -> response.getQuestion() != null && response.getQuestion().getId() == selectedQuestion.getId())
                    .toList();
            responseList.setItems(FXCollections.observableArrayList(responses));

            // Manually update the button states after setting items
            boolean isEmpty = responseList.getItems().isEmpty();
            editButton.setDisable(isEmpty);
            deleteButton.setDisable(isEmpty);
        }
    }

    @FXML
    private void handleEditResponses() {
        try {
            String absolutePath = "C:/Users/jouilq/OneDrive/Bureau/Pidev JAVA/PidevJava/src/main/resources/ModifierQuizResponse.fxml";
            File fxmlFile = new File(absolutePath);
            if (!fxmlFile.exists()) {
                throw new IOException("FXML file not found at: " + fxmlFile.getAbsolutePath());
            }
            URL fxmlUrl = fxmlFile.toURI().toURL();
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            AnchorPane pane = loader.load();

            ModifierQuizResponseController controller = loader.getController();
            controller.setQuestion(selectedQuestion);

            Stage stage = new Stage();
            stage.setTitle("Modifier les Réponses");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(responseList.getScene().getWindow());
            stage.setScene(new Scene(pane));
            stage.showAndWait();

            // Refresh the list after editing
            loadResponses();
        } catch (IOException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur de navigation");
            errorAlert.setHeaderText("Échec du chargement du formulaire de modification");
            errorAlert.setContentText("Erreur : " + e.getMessage());
            errorAlert.show();
        }
    }

    @FXML
    private void handleDeleteResponses() {
        // Show confirmation dialog
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation de suppression");
        confirmationAlert.setHeaderText("Supprimer les réponses");
        confirmationAlert.setContentText("Êtes-vous sûr de vouloir supprimer toutes les réponses de cette question ?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete each response individually
                List<QuizResponse> responses = responseList.getItems();
                for (QuizResponse response : responses) {
                    serviceQuizResponse.delete(response);
                }

                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Réponses supprimées avec succès !");
                successAlert.showAndWait();

                // Refresh the list
                loadResponses();
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText("Échec de la suppression");
                errorAlert.setContentText("Erreur : " + e.getMessage());
                errorAlert.show();
            }
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}