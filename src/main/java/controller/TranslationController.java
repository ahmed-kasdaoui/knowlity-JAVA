package controller;

import api.TranslationService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class TranslationController {
    private final TranslationService translationService;
    private final Map<String, String> languageCodes;
    private ProgressIndicator progressIndicator;

    public TranslationController() {
        this.translationService = new TranslationService();
        this.languageCodes = new HashMap<>();
        initializeLanguageCodes();
    }

    private void initializeLanguageCodes() {
        languageCodes.put("Auto-détection", "auto");
        languageCodes.put("English", "en");
        languageCodes.put("Français", "fr");
        languageCodes.put("Español", "es");
        languageCodes.put("Deutsch", "de");
        languageCodes.put("Italiano", "it");
        languageCodes.put("Português", "pt");
        languageCodes.put("Nederlands", "nl");
        languageCodes.put("Polski", "pl");
        languageCodes.put("Русский", "ru");
        languageCodes.put("中文", "zh");
        languageCodes.put("한국어", "ko");
        languageCodes.put("日本語", "ja");
        languageCodes.put("Arabic", "ar");
        languageCodes.put("Hindi", "hi");
    }

    public void setupLanguageSelector(ComboBox<String> languageComboBox) {
        // Ajouter les langues supportées
        languageComboBox.getItems().addAll(languageCodes.keySet());
        
        // Sélectionner l'auto-détection par défaut
        languageComboBox.setValue("Auto-détection");
    }

    public void setupTranslationUI(VBox container, TextArea translatedTextArea) {
        // Créer et configurer l'indicateur de progression
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(20, 20);
        
        // Ajouter l'indicateur au conteneur
        container.getChildren().add(progressIndicator);
    }

    public void translateContent(String content, String targetLang, TextArea targetTextArea) {
        if (content == null || content.trim().isEmpty()) {
            showError("Erreur", "Le contenu à traduire est vide");
            return;
        }

        // Afficher l'indicateur de progression
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }

        Task<String> translationTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                String langCode = languageCodes.get(targetLang);
                if (langCode == null) {
                    throw new IllegalArgumentException("Langue non supportée: " + targetLang);
                }

                return translationService.translateText(content, "auto", langCode);
            }
        };

        translationTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                targetTextArea.setText(translationTask.getValue());
                if (progressIndicator != null) {
                    progressIndicator.setVisible(false);
                }
            });
        });

        translationTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                if (progressIndicator != null) {
                    progressIndicator.setVisible(false);
                }
                showError("Erreur de traduction", 
                    translationTask.getException().getMessage());
            });
        });

        // Démarrer la tâche de traduction dans un thread séparé
        new Thread(translationTask).start();
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
