package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

public class ListQuizEtudiantController {

    @FXML
    private VBox quizContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Button clearSearchButton;

    @FXML
    private Button backButton;

    private ServiceQuiz serviceQuiz;
    private List<Quiz> allQuizzes; // Store the full list for filtering
    private Cours cours;

    @FXML
    void initialize() {
        serviceQuiz = new ServiceQuiz();

        // Add hover effects for buttons
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: #da190b; -fx-text-fill: white; -fx-pref-width: 150; -fx-padding: 5;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-pref-width: 150; -fx-padding: 5;"));

        // Add hover effects for searchField
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

        // Add dynamic search functionality
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterQuizzes(newValue);
            clearSearchButton.setVisible(!newValue.isEmpty());
        });

        // Load all quizzes into the cards
        refreshQuizList();
    }

    private void refreshQuizList() {
        if (cours == null) {
            System.out.println("Aucun cours sélectionné");
            return;
        }
        
        // Récupérer uniquement les quiz du cours sélectionné
        allQuizzes = serviceQuiz.getQuizByCours(cours.getId());
        System.out.println("Refreshed quiz list for course " + cours.getTitle() + ". Total quizzes retrieved: " + allQuizzes.size());
        filterQuizzes(searchField.getText()); // Appliquer le filtre de recherche existant
    }

    private void filterQuizzes(String searchText) {
        quizContainer.getChildren().clear();
        List<Quiz> quizzes = allQuizzes;

        // Apply search filter if search text is present
        if (searchText != null && !searchText.trim().isEmpty()) {
            String searchLower = searchText.toLowerCase();
            quizzes = quizzes.stream()
                    .filter(q ->
                            (q.getTitre() != null && q.getTitre().toLowerCase().contains(searchLower)) ||
                                    (q.getDescription() != null && q.getDescription().toLowerCase().contains(searchLower)) ||
                                    (String.valueOf(q.getScoreMax()).contains(searchLower)) ||
                                    (q.getDateLimite() != null && q.getDateLimite().toString().toLowerCase().contains(searchLower))
                    )
                    .collect(Collectors.toList());
        }

        System.out.println("Filtered quizzes. Displaying " + quizzes.size() + " quizzes after filtering with search text: '" + searchText + "'");

        // Display the filtered quizzes
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
        Button takeQuizButton = new Button("Prendre Quiz");
        takeQuizButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-pref-width: 100; -fx-padding: 5;");
        takeQuizButton.setOnAction(e -> handleTakeQuiz(quiz));

        buttonBox.getChildren().addAll(takeQuizButton);

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

    private void handleEditQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierQuiz.fxml"));
            AnchorPane pane = loader.load();

            Scene scene = new Scene(pane);

            ModifierQuizController controller = loader.getController();
            controller.setQuizToModify(quiz);

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

            Scene scene = new Scene(pane);

            ListQuizQuestionController controller = loader.getController();
            controller.setQuiz(quiz);

            Stage stage = new Stage();
            stage.setTitle("Questions du Quiz : " + quiz.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(quizContainer.getScene().getWindow());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement de la liste des questions : " + e.getMessage());
        }
    }

    private void handleTakeQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TakeQuiz.fxml"));
            AnchorPane pane = loader.load();

            TakeQuizController controller = loader.getController();
            controller.setQuiz(quiz);

            Stage stage = new Stage();
            stage.setTitle("Participer à un Quiz : " + quiz.getTitre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(quizContainer.getScene().getWindow());
            stage.setScene(new Scene(pane));
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement de la vue de participation au quiz : " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
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
        // Rafraîchir la liste des quiz quand un cours est défini
        if (serviceQuiz != null) {
            refreshQuizList();
        }
    }
}