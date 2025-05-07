package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.models.Cours;
import tn.esprit.models.Quiz;
import tn.esprit.services.ServiceQuiz;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListQuizController {

    @FXML
    private VBox quizContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Button clearSearchButton;

    @FXML
    private Button createButton;

    @FXML
    private Button backButton;

    private ServiceQuiz serviceQuiz;
    private List<Quiz> allQuizzes;
    private Cours cours;

    @FXML
    void initialize() {
        serviceQuiz = new ServiceQuiz();

        // Hover effects
        createButton.setOnMouseEntered(e -> createButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-pref-width: 150; -fx-padding: 5;"));
        createButton.setOnMouseExited(e -> createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 150; -fx-padding: 5;"));
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: #da190b; -fx-text-fill: white; -fx-pref-width: 150; -fx-padding: 5;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-pref-width: 150; -fx-padding: 5;"));

        // Search field hover
        searchField.setOnMouseEntered(e -> searchField.setStyle(
                "-fx-pref-width: 400; -fx-background-radius: 20; -fx-border-radius: 20;" +
                        "-fx-background-color: #ffffff; -fx-border-color: #3b28cc;" +
                        "-fx-padding: 8 30 8 30; -fx-font-size: 14; -fx-background-insets: 0;"
        ));
        searchField.setOnMouseExited(e -> searchField.setStyle(
                "-fx-pref-width: 400; -fx-background-radius: 20; -fx-border-radius: 20;" +
                        "-fx-background-color: #ffffff; -fx-border-color: #cccccc;" +
                        "-fx-padding: 8 30 8 30; -fx-font-size: 14; -fx-background-insets: 0;"
        ));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterQuizzes(newVal);
            clearSearchButton.setVisible(!newVal.isEmpty());
        });

        refreshQuizList();
    }

    private void refreshQuizList() {
        if (cours != null) {
            allQuizzes = serviceQuiz.getAll().stream()
                    .filter(q -> q.getCours_id() == cours.getId())
                    .collect(Collectors.toList());
        } else {
            allQuizzes = List.of(); // liste vide si aucun cours n'est défini
        }
        filterQuizzes(searchField.getText());
    }

    private void filterQuizzes(String searchText) {
        quizContainer.getChildren().clear();
        List<Quiz> quizzes = allQuizzes;

        if (searchText != null && !searchText.trim().isEmpty()) {
            String lower = searchText.toLowerCase();
            quizzes = quizzes.stream()
                    .filter(q ->
                            (q.getTitre() != null && q.getTitre().toLowerCase().contains(lower)) ||
                                    (q.getDescription() != null && q.getDescription().toLowerCase().contains(lower)) ||
                                    (String.valueOf(q.getScoreMax()).contains(lower)) ||
                                    (q.getDateLimite() != null && q.getDateLimite().toString().toLowerCase().contains(lower))
                    )
                    .collect(Collectors.toList());
        }

        for (Quiz quiz : quizzes) {
            HBox card = createQuizCard(quiz);
            quizContainer.getChildren().add(card);
        }
    }

    private HBox createQuizCard(Quiz quiz) {
        HBox card = new HBox();
        card.setSpacing(15);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-padding: 15;");
        card.prefWidthProperty().bind(quizContainer.widthProperty().subtract(40));

        VBox content = new VBox();
        content.setSpacing(5);
        Label titleLabel = new Label("Titre : " + quiz.getTitre());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #3b28cc;");
        Label descLabel = new Label("Description : " + quiz.getDescription());
        descLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #555555;");
        Label scoreMaxLabel = new Label("Score Maximum : " + quiz.getScoreMax());
        scoreMaxLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #555555;");
        Label dateLimiteLabel = new Label("Date Limite : " + (quiz.getDateLimite() != null ? quiz.getDateLimite().toString() : "N/A"));
        dateLimiteLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #555555;");
        content.getChildren().addAll(titleLabel, descLabel, scoreMaxLabel, dateLimiteLabel);

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);

        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-pref-width: 100; -fx-padding: 5;");
        editButton.setOnAction(e -> handleEditQuiz(quiz));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-pref-width: 100; -fx-padding: 5;");
        deleteButton.setOnAction(e -> handleDeleteQuiz(quiz));

        Button questionsButton = new Button("Questions");
        questionsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-pref-width: 100; -fx-padding: 5;");
        questionsButton.setOnAction(e -> handleViewQuestions(quiz));

        buttonBox.getChildren().addAll(editButton, deleteButton, questionsButton);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(content, spacer, buttonBox);
        return card;
    }

    @FXML
    private void clearSearch() {
        searchField.setText("");
        clearSearchButton.setVisible(false);
    }

    @FXML
    private void handleCreateQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterQuiz.fxml"));
            AnchorPane pane = loader.load();

            AjouterQuizController controller = loader.getController();
            controller.setCours(cours); // injecte le cours pour associer le quiz

            Scene scene = new Scene(pane);
            Stage stage = new Stage();
            stage.setTitle("Créer un Quiz");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(quizContainer.getScene().getWindow());
            stage.setScene(scene);
            stage.showAndWait();

            refreshQuizList();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement du formulaire de création : " + e.getMessage());
        }
    }

    private void handleEditQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierQuiz.fxml"));
            AnchorPane pane = loader.load();

            ModifierQuizController controller = loader.getController();
            controller.setQuizToModify(quiz);

            Scene scene = new Scene(pane);
            Stage stage = new Stage();
            stage.setTitle("Modifier un Quiz");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(quizContainer.getScene().getWindow());
            stage.setScene(scene);
            stage.showAndWait();

            refreshQuizList();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement du formulaire de modification : " + e.getMessage());
        }
    }

    private void handleDeleteQuiz(Quiz quiz) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation de suppression");
        confirmationAlert.setHeaderText("Supprimer un quiz");
        confirmationAlert.setContentText("Êtes-vous sûr de vouloir supprimer le quiz : " + quiz.getTitre() + " ?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceQuiz.delete(quiz);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Quiz supprimé avec succès !");
                refreshQuizList();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression : " + e.getMessage());
            }
        }
    }

    private void handleViewQuestions(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListQuizQuestion.fxml"));
            AnchorPane pane = loader.load();

            ListQuizQuestionController controller = loader.getController();
            controller.setQuiz(quiz);

            Stage stage = new Stage();
            stage.setTitle("Questions du Quiz : " + quiz.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(quizContainer.getScene().getWindow());
            stage.setScene(new Scene(pane));
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement de la liste des questions : " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CourseDetails.fxml"));
            Parent root = loader.load();
            CourseDetailsController controller = loader.getController();
            controller.setCourse(cours);
            backButton.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load CourseDetails.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner aux détails du cours.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setCourse(Cours course) {
        this.cours = course;
        refreshQuizList(); // recharge les quiz dès que le cours est défini
    }
}
