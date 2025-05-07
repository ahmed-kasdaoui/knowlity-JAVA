package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.models.Quiz;
import tn.esprit.models.QuizQuestion;
import tn.esprit.services.ServiceQuizQuestion;
import tn.esprit.services.ServiceQuizResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListQuizQuestionController {

    @FXML
    private VBox questionContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Button clearSearchButton;

    @FXML
    private Button createButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button addResponsesButton;

    @FXML
    private Button viewResponsesButton;

    @FXML
    private Button takeQuizButton;

    private ServiceQuizQuestion serviceQuizQuestion;
    private ServiceQuizResponse serviceQuizResponse;
    private Quiz selectedQuiz;
    private QuizQuestion selectedQuestion;
    private List<QuizQuestion> allQuestions;

    @FXML
    void initialize() {
        serviceQuizQuestion = new ServiceQuizQuestion();
        serviceQuizResponse = new ServiceQuizResponse();

        editButton.setDisable(true);
        deleteButton.setDisable(true);
        addResponsesButton.setDisable(true);
        viewResponsesButton.setDisable(true);

        createButton.setOnMouseEntered(e -> createButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-size: 14; -fx-pref-width: 150; -fx-pref-height: 40; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));
        createButton.setOnMouseExited(e -> createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14; -fx-pref-width: 150; -fx-pref-height: 40; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));

        editButton.setOnMouseEntered(e -> editButton.setStyle("-fx-background-color: #e69500; -fx-text-fill: white; -fx-font-size: 14; -fx-pref-width: 150; -fx-pref-height: 40; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));
        editButton.setOnMouseExited(e -> editButton.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-size: 14; -fx-pref-width: 150; -fx-pref-height: 40; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));

        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-background-color: #da190b; -fx-text-fill: white; -fx-font-size: 14; -fx-pref-width: 150; -fx-pref-height: 40; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14; -fx-pref-width: 150; -fx-pref-height: 40; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));

        addResponsesButton.setOnMouseEntered(e -> addResponsesButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-size: 14; -fx-pref-width: 150; -fx-pref-height: 40; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));
        addResponsesButton.setOnMouseExited(e -> addResponsesButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14; -fx-pref-width: 150; -fx-pref-height: 40; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));

        viewResponsesButton.setOnMouseEntered(e -> viewResponsesButton.setStyle("-fx-background-color: #7B1FA2; -fx-text-fill: white; -fx-font-size: 14; -fx-pref-width: 150; -fx-pref-height: 40; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));
        viewResponsesButton.setOnMouseExited(e -> viewResponsesButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-size: 14; -fx-pref-width: 150; -fx-pref-height: 40; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));

        searchField.setOnMouseEntered(e -> searchField.setStyle(
                "-fx-pref-width: 400; -fx-background-radius: 20; -fx-border-radius: 20; " +
                        "-fx-background-color: #ffffff; -fx-border-color: #3b28cc; " +
                        "-fx-padding: 8 30 8 30; -fx-font-size: 14; -fx-background-insets: 0;"
        ));
        searchField.setOnMouseExited(e -> searchField.setStyle(
                "-fx-pref-width: 400; -fx-background-radius: 20; -fx-border-radius: 20; " +
                        "-fx-background-color: #ffffff; -fx-border-color: #cccccc; " +
                        "-fx-padding: 8 30 8 30; -fx-font-size: 14; -fx-background-insets: 0;"
        ));

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterQuestions(newValue);
            clearSearchButton.setVisible(!newValue.isEmpty());
        });

        refreshTable();
    }

    public void setQuiz(Quiz quiz) {
        this.selectedQuiz = quiz;
        refreshTable();
    }

    private void refreshTable() {
        allQuestions = serviceQuizQuestion.getAll();
        filterQuestions(searchField.getText());
    }

    private void filterQuestions(String searchText) {
        questionContainer.getChildren().clear();
        List<QuizQuestion> questions = allQuestions;

        if (selectedQuiz != null) {
            questions = questions.stream()
                    .filter(q -> q.getQuiz() != null && q.getQuiz().getId() == selectedQuiz.getId())
                    .collect(Collectors.toList());
        }

        if (searchText != null && !searchText.trim().isEmpty()) {
            String searchLower = searchText.toLowerCase();
            questions = questions.stream()
                    .filter(q ->
                            (q.getType() != null && q.getType().toLowerCase().contains(searchLower)) ||
                                    (String.valueOf(q.getPoints()).contains(searchLower)) ||
                                    (q.getTexte() != null && q.getTexte().toLowerCase().contains(searchLower)) ||
                                    (String.valueOf(q.getOrdre()).contains(searchLower)) ||
                                    (q.getQuiz() != null && q.getQuiz().getTitre() != null && q.getQuiz().getTitre().toLowerCase().contains(searchLower))
                    )
                    .collect(Collectors.toList());
        }

        for (QuizQuestion question : questions) {
            HBox card = createQuestionCard(question);
            questionContainer.getChildren().add(card);
        }

        if (selectedQuestion != null && !questions.contains(selectedQuestion)) {
            selectedQuestion = null;
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            addResponsesButton.setDisable(true);
            viewResponsesButton.setDisable(true);
        }
    }

    private HBox createQuestionCard(QuizQuestion question) {
        HBox card = new HBox();
        card.setSpacing(15);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-padding: 15;");

        card.prefWidthProperty().bind(questionContainer.widthProperty().subtract(40));

        VBox content = new VBox();
        content.setSpacing(5);
        Label typeLabel = new Label("Type : " + question.getType());
        typeLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #555555;");
        Label pointsLabel = new Label("Points : " + question.getPoints());
        pointsLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #555555;");
        Label texteLabel = new Label("Texte : " + question.getTexte());
        texteLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #555555;");
        texteLabel.setWrapText(true);
        Label ordreLabel = new Label("Ordre : " + question.getOrdre());
        ordreLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #555555;");
        Label quizLabel = new Label("Quiz : " + (question.getQuiz() != null ? question.getQuiz().getTitre() : "N/A"));
        quizLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #555555;");
        content.getChildren().addAll(typeLabel, pointsLabel, texteLabel, ordreLabel, quizLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(content, spacer);

        card.setOnMouseClicked(e -> {
            selectedQuestion = question;
            editButton.setDisable(false);
            deleteButton.setDisable(true);
            addResponsesButton.setDisable(false);
            viewResponsesButton.setDisable(false);
            questionContainer.getChildren().forEach(node -> node.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-padding: 15;"));
            card.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-padding: 15;");
        });

        return card;
    }

    @FXML
    private void clearSearch() {
        searchField.setText("");
        clearSearchButton.setVisible(false);
    }

    @FXML
    private void handleCreateQuestion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterQuizQuestion.fxml"));
            AnchorPane pane = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Créer une Question");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(questionContainer.getScene().getWindow());
            stage.setScene(new Scene(pane));
            stage.showAndWait();

            refreshTable();
        } catch (IOException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur de navigation");
            errorAlert.setHeaderText("Échec du chargement du formulaire de création");
            errorAlert.setContentText("Erreur : " + e.getMessage());
            errorAlert.show();
        }
    }

    @FXML
    private void handleEditQuestion() {
        if (selectedQuestion != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierQuizQuestion.fxml"));
                AnchorPane pane = loader.load();

                ModifierQuizQuestionController controller = loader.getController();
                controller.setQuestionToModify(selectedQuestion);

                Stage stage = new Stage();
                stage.setTitle("Modifier une Question");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(questionContainer.getScene().getWindow());
                stage.setScene(new Scene(pane));
                stage.showAndWait();

                refreshTable();
            } catch (IOException e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur de navigation");
                errorAlert.setHeaderText("Échec du chargement du formulaire de modification");
                errorAlert.setContentText("Erreur : " + e.getMessage());
                errorAlert.show();
            }
        }
    }

    @FXML
    private void handleDeleteQuestion() {
        if (selectedQuestion != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation de suppression");
            confirmationAlert.setHeaderText("Supprimer une question");
            confirmationAlert.setContentText("Êtes-vous sûr de vouloir supprimer la question : " + selectedQuestion.getTexte() + " ?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    serviceQuizQuestion.delete(selectedQuestion);
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Succès");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Question supprimée avec succès !");
                    successAlert.showAndWait();
                    refreshTable();
                    selectedQuestion = null;
                    editButton.setDisable(true);
                    deleteButton.setDisable(true);
                    addResponsesButton.setDisable(true);
                    viewResponsesButton.setDisable(true);
                } catch (Exception e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setHeaderText("Échec de la suppression");
                    errorAlert.setContentText("Erreur : " + e.getMessage());
                    errorAlert.show();
                }
            }
        }
    }

    @FXML
    private void handleAddResponses() {
        if (selectedQuestion != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterQuizResponse.fxml"));
                AnchorPane pane = loader.load();

                AjouterQuizResponseController controller = loader.getController();
                controller.setQuestion(selectedQuestion);

                Stage stage = new Stage();
                stage.setTitle("Ajouter des Réponses");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(questionContainer.getScene().getWindow());
                stage.setScene(new Scene(pane));
                stage.showAndWait();
            } catch (IOException e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur de navigation");
                errorAlert.setHeaderText("Échec du chargement du formulaire d'ajout de réponses");
                errorAlert.setContentText("Erreur : " + e.getMessage());
                errorAlert.show();
            }
        }
    }

    @FXML
    private void handleViewResponses() {
        if (selectedQuestion != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListQuizResponse.fxml"));
                AnchorPane pane = loader.load();

                ListQuizResponseController controller = loader.getController();
                controller.setQuestion(selectedQuestion);

                Stage stage = new Stage();
                stage.setTitle("Liste des Réponses");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(questionContainer.getScene().getWindow());
                stage.setScene(new Scene(pane));
                stage.showAndWait();
            } catch (IOException e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur de navigation");
                errorAlert.setHeaderText("Échec du chargement de la liste des réponses");
                errorAlert.setContentText("Erreur : " + e.getMessage());
                errorAlert.show();
            }
        }
    }

    @FXML
    private void handleTakeQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TakeQuiz.fxml"));
            AnchorPane pane = loader.load();

            TakeQuizController controller = loader.getController();
            controller.setQuiz(selectedQuiz);

            Stage stage = new Stage();
            stage.setTitle("Participer à un Quiz");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(questionContainer.getScene().getWindow());
            stage.setScene(new Scene(pane));
            stage.showAndWait();
        } catch (IOException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur de navigation");
            errorAlert.setHeaderText("Échec du chargement de la vue de participation au quiz");
            errorAlert.setContentText("Erreur : " + e.getMessage());
            errorAlert.show();
        }
    }
}