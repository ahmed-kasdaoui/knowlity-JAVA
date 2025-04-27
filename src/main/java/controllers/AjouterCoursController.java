package controllers;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.models.Cours;
import tn.esprit.models.Matiere;
import tn.esprit.services.ServiceCours;
import tn.esprit.services.ServiceMatiere;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.LibVosk;
import javax.sound.sampled.*;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.vosk.LogLevel;

public class AjouterCoursController {

    @FXML
    private TextField titleField;

    @FXML
    private ComboBox<String> langueComboBox;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField prixField;

    @FXML
    private Label fileLabel;

    @FXML
    private ComboBox<Matiere> matiereComboBox;

    @FXML
    private TextField lienDePaimentField;

    @FXML
    private Button uploadButton;

    @FXML
    private Button submitButton;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Button voiceButton;

    private static final String UPLOAD_DIR = "Uploads/";
    private static final String WATERMARK_PATH = "src/main/resources/watermark.png";
    private static final String[] VALID_LANGUAGES = {"fr", "en", "es", "de", "ar"};
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB (updated per your code)
    private static final String COURSE_URL_BASE = "http://localhost:8080/cours/";
    private static final String FACEBOOK_PAGE_ID = "535397399664579";
    private static final String FACEBOOK_ACCESS_TOKEN = "EAAq1jUXunxQBOxr3qXCxWKLKVarrhK90Je7GnHrKGY4QF2jghFYTgAzJZAuYFDRISY2rkMJduXxVWNeUZCNnWBw88VScGzySVY4GrlrJODmACZCFfMimoxNS7uHZBLPtZApUJhUMckALChavZBe8NWT8HizJU9yXjPmelMf3mFjsoanmQHTrgAzzGFWXCHKnuZB";

    private File selectedFile;
    private final ServiceCours serviceCours;
    private final ServiceMatiere serviceMatiere;
    private final Object lock = new Object();
    private volatile boolean listening = false;
    private Thread recognitionThread;
    private volatile TargetDataLine currentLine;
    private volatile Model currentModel;
    private volatile Recognizer currentRecognizer;

    public AjouterCoursController() {
        this.serviceCours = new ServiceCours();
        this.serviceMatiere = new ServiceMatiere();
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing AjouterCoursController");
        System.out.println("Working directory: " + System.getProperty("user.dir"));

        // Create uploads directory
        File uploadDir = new File(UPLOAD_DIR);
        try {
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                System.out.println("Uploads directory creation: " + (created ? "Success" : "Failed") + " at " + uploadDir.getAbsolutePath());
            }
            if (!uploadDir.canWrite()) {
                throw new IOException("Uploads directory is not writable: " + uploadDir.getAbsolutePath());
            }
            System.out.println("Uploads directory writable: " + uploadDir.canWrite());
        } catch (Exception e) {
            System.err.println("Error initializing uploads directory: " + e.getMessage());
        }

        // Verify watermark file
        File watermarkFile = new File(WATERMARK_PATH);
        if (!watermarkFile.exists()) {
            System.err.println("Watermark file missing at: " + WATERMARK_PATH);
        }

        // Populate langueComboBox
        langueComboBox.getItems().addAll(VALID_LANGUAGES);
        System.out.println("langueComboBox populated with: " + Arrays.toString(VALID_LANGUAGES));

        // Populate matiereComboBox
        List<Matiere> matieres = serviceMatiere.getAll();
        matiereComboBox.getItems().addAll(matieres);
        System.out.println("matiereComboBox populated with " + matiereComboBox.getItems().size() + " items");

        // Display only titre in matiereComboBox
        matiereComboBox.setConverter(new StringConverter<Matiere>() {
            @Override
            public String toString(Matiere matiere) {
                return matiere != null ? matiere.getTitre() : "";
            }

            @Override
            public Matiere fromString(String string) {
                return null;
            }
        });

        // Set button factory for selected matiere display
        matiereComboBox.setButtonCell(new ListCell<Matiere>() {
            @Override
            protected void updateItem(Matiere item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitre());
                }
            }
        });

        // Add voice recognition button
        voiceButton = new Button("🎙️ Dicter la description");
        voiceButton.setOnAction(e -> toggleVoiceRecognition());
        VBox descriptionContainer = (VBox) descriptionField.getParent();
        descriptionContainer.getChildren().add(voiceButton);
    }

    private void toggleVoiceRecognition() {
        if (!listening) {
            startVoiceRecognition();
        } else {
            stopVoiceRecognition();
        }
    }

    private void startVoiceRecognition() {
        synchronized (lock) {
            if (listening) {
                System.out.println("La reconnaissance est déjà en cours");
                return;
            }

            try {
                listening = true;
                voiceButton.setText("⏹️ Arrêter la dictée");
                voiceButton.setStyle("-fx-background-color: linear-gradient(to right, #ff416c, #ff4b2b); -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 25px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 0); -fx-padding: 10 20 10 20; -fx-cursor: hand; -fx-transition: all 0.3s ease;");
                
                // Ajouter une animation au bouton
                voiceButton.setOnMouseEntered(e -> voiceButton.setStyle("-fx-background-color: linear-gradient(to right, #ff4b2b, #ff416c); -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 25px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 15, 0, 0, 0); -fx-padding: 10 20 10 20; -fx-cursor: hand; -fx-transition: all 0.3s ease;"));
                voiceButton.setOnMouseExited(e -> voiceButton.setStyle("-fx-background-color: linear-gradient(to right, #ff416c, #ff4b2b); -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 25px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 0); -fx-padding: 10 20 10 20; -fx-cursor: hand; -fx-transition: all 0.3s ease;"));

                // Initialisation du modèle
                String workingDir = System.getProperty("user.dir");
                String absoluteModelPath = new File(workingDir, "models/fr").getAbsolutePath();
                System.out.println("Chargement du modèle depuis: " + absoluteModelPath);
                
                currentModel = new Model(absoluteModelPath);
                System.out.println("Modèle chargé avec succès");

                // Configuration du microphone
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    throw new LineUnavailableException("Format audio non supporté");
                }

                currentLine = (TargetDataLine) AudioSystem.getLine(info);
                currentLine.open(format);
                System.out.println("Microphone ouvert avec format: " + format);

                // Configuration du recognizer
                currentRecognizer = new Recognizer(currentModel, 16000);
                System.out.println("Recognizer créé");

                // Démarrage de la capture
                currentLine.start();
                System.out.println("Capture audio démarrée");

                recognitionThread = new Thread(() -> {
                    try {
                        byte[] buffer = new byte[4096];
                        System.out.println("Début de la reconnaissance vocale");
                        System.out.println("Parlez maintenant...");

                        while (listening && currentLine != null && currentLine.isOpen()) {
                            int bytesRead = currentLine.read(buffer, 0, buffer.length);
                            
                            if (bytesRead > 0) {
                                // Calcul du niveau audio
                                double level = calculateAudioLevel(buffer, bytesRead);
                                if (level > 0.01) {
                                    System.out.print("█");
                                }

                                // Reconnaissance
                                if (currentRecognizer != null && currentRecognizer.acceptWaveForm(buffer, bytesRead)) {
                                    String result = currentRecognizer.getResult();
                                    System.out.println("\nRésultat: " + result);
                                    
                                    // Extraction du texte sans accolades
                                    String text = result.replaceAll("[{}]", "").replaceAll(".*\"text\"\\s*:\\s*\"([^\"]*)\".*", "$1").trim();
                                    if (!text.isEmpty()) {
                                        final String finalText = text;
                                        javafx.application.Platform.runLater(() -> {
                                            String currentText = descriptionField.getText();
                                            descriptionField.setText(currentText + (currentText.isEmpty() ? "" : " ") + finalText);
                                        });
                                    }
                                } else if (currentRecognizer != null) {
                                    // Affichage des résultats partiels
                                    String partial = currentRecognizer.getPartialResult();
                                    String partialText = partial.replaceAll("[{}]", "").replaceAll(".*\"partial\"\\s*:\\s*\"([^\"]*)\".*", "$1").trim();
                                    if (!partialText.isEmpty()) {
                                        System.out.println("\nPartiel: " + partialText);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur pendant la reconnaissance: " + e.getMessage());
                        e.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            showAlert("Erreur", "Erreur pendant la reconnaissance: " + e.getMessage(), Alert.AlertType.ERROR);
                        });
                    } finally {
                        cleanup();
                    }
                });

                recognitionThread.start();
                System.out.println("Thread de reconnaissance démarré");

            } catch (Exception e) {
                System.err.println("Erreur au démarrage: " + e.getMessage());
                e.printStackTrace();
                showAlert("Erreur", "Erreur au démarrage de la reconnaissance: " + e.getMessage(), Alert.AlertType.ERROR);
                cleanup();
            }
        }
    }

    private void cleanup() {
        synchronized (lock) {
            System.out.println("Début du nettoyage des ressources...");
            listening = false;

            // Attendre que le thread de reconnaissance se termine
            if (recognitionThread != null && recognitionThread.isAlive()) {
                try {
                    System.out.println("Attente de la fin du thread de reconnaissance...");
                    recognitionThread.join(1000); // Attendre max 1 seconde
                } catch (InterruptedException e) {
                    System.err.println("Interruption pendant l'attente de fin du thread");
                }
            }
            
            // Fermer la ligne audio
            if (currentLine != null) {
                try {
                    System.out.println("Fermeture de la ligne audio...");
                    if (currentLine.isActive()) {
                        currentLine.stop();
                    }
                    if (currentLine.isOpen()) {
                        currentLine.close();
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la fermeture de la ligne audio: " + e.getMessage());
                } finally {
                    currentLine = null;
                }
            }

            // Fermer le recognizer
            if (currentRecognizer != null) {
                try {
                    System.out.println("Fermeture du recognizer...");
                    currentRecognizer.close();
                } catch (Exception e) {
                    System.err.println("Erreur lors de la fermeture du recognizer: " + e.getMessage());
                } finally {
                    currentRecognizer = null;
                }
            }

            // Fermer le modèle
            if (currentModel != null) {
                try {
                    System.out.println("Fermeture du modèle...");
                    currentModel.close();
                } catch (Exception e) {
                    System.err.println("Erreur lors de la fermeture du modèle: " + e.getMessage());
                } finally {
                    currentModel = null;
                }
            }

            System.out.println("Nettoyage terminé");
        }

        // Mettre à jour l'UI sur le thread JavaFX
        javafx.application.Platform.runLater(() -> {
            voiceButton.setText("🎙️ Dicter la description");
            voiceButton.setStyle("");
        });
    }

    private void stopVoiceRecognition() {
        System.out.println("Demande d'arrêt de la reconnaissance vocale");
        cleanup();
    }

    private double calculateAudioLevel(byte[] buffer, int bytesRead) {
        long sum = 0;
        // Traiter les échantillons 16-bit
        for (int i = 0; i < bytesRead - 1; i += 2) {
            short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xff));
            sum += Math.abs(sample);
        }
        // Calculer le niveau moyen
        return sum / (bytesRead / 2.0) / 32768.0;
    }

    private TargetDataLine getMicrophone() throws LineUnavailableException {
        System.out.println("Configuration du microphone...");
        
        // Essayer différents formats audio
        AudioFormat[] formats = {
            new AudioFormat(16000, 16, 1, true, false),  // Format Vosk préféré
            new AudioFormat(44100, 16, 1, true, false),  // Format standard
            new AudioFormat(48000, 16, 1, true, false)   // Format haute qualité
        };
        
        // Lister et chercher le microphone AMD
        System.out.println("\nRecherche du microphone...");
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        
        // D'abord essayer le microphone par défaut avec le format Vosk
        AudioFormat voskFormat = formats[0];
        DataLine.Info defaultInfo = new DataLine.Info(TargetDataLine.class, voskFormat);
        
        if (AudioSystem.isLineSupported(defaultInfo)) {
            try {
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(defaultInfo);
                line.open(voskFormat);
                System.out.println("Microphone configuré avec le format Vosk");
                return configureLineSettings(line);
            } catch (Exception e) {
                System.out.println("Échec du format Vosk: " + e.getMessage());
            }
        }

        // Si le format Vosk échoue, essayer tous les formats disponibles
        for (AudioFormat format : formats) {
            try {
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                if (AudioSystem.isLineSupported(info)) {
                    TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                    line.open(format);
                    System.out.println("Microphone configuré avec format alternatif: " + format);
                    return configureLineSettings(line);
                }
            } catch (Exception e) {
                System.out.println("Échec du format " + format.getSampleRate() + "Hz: " + e.getMessage());
            }
        }

        throw new LineUnavailableException("Aucun format audio compatible trouvé");
    }
    
    private TargetDataLine configureLineSettings(TargetDataLine line) {
        try {
            // Vérifier si le contrôle de volume est supporté
            if (line.isControlSupported(FloatControl.Type.VOLUME)) {
                FloatControl volume = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
                volume.setValue(volume.getMaximum()); // Mettre le volume au maximum
                System.out.println("Volume du microphone réglé au maximum: " + volume.getValue());
            }
            
            // Vérifier si le gain est supporté
            if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(gain.getMaximum()); // Mettre le gain au maximum
                System.out.println("Gain du microphone réglé au maximum: " + gain.getValue());
            }
            
            System.out.println("Microphone configuré avec succès");
            System.out.println("Format: " + line.getFormat());
            System.out.println("Buffer size: " + line.getBufferSize() + " bytes");
            
            return line;
        } catch (Exception e) {
            System.err.println("Attention: Impossible de configurer les contrôles audio: " + e.getMessage());
            return line; // Retourner la ligne même si la configuration a échoué
        }
    }

    @FXML
    void uploadButtonAction(ActionEvent event) {
        System.out.println("Upload button clicked");
        try {
            // Verify stage
            if (uploadButton.getScene() == null || uploadButton.getScene().getWindow() == null) {
                throw new IllegalStateException("Scene or window not initialized");
            }
            System.out.println("Stage verified");

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner une image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.webp")
            );
            System.out.println("FileChooser initialized");

            File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
            if (selectedFile == null) {
                System.out.println("No file selected (dialog canceled)");
                return;
            }
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());

            // Validate file
            if (!selectedFile.exists()) {
                throw new IOException("Fichier introuvable.");
            }
            if (selectedFile.length() > MAX_FILE_SIZE) {
                throw new IOException("Fichier trop volumineux (max 10 Mo).");
            }
            String extension = getFileExtension(selectedFile.getName()).toLowerCase();
            if (!extension.matches("jpg|png|webp")) {
                throw new IOException("Format non supporté. Utilisez JPG, PNG ou WEBP.");
            }

            // Generate unique filename
            String originalName = selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf('.'));
            String slug = slugify(originalName);
            String newFilename = slug + "-" + UUID.randomUUID() + "." + extension;
            Path destination = Paths.get(UPLOAD_DIR, newFilename);
            System.out.println("Destination: " + destination);

            // Verify upload directory
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists() || !uploadDir.isDirectory()) {
                throw new IOException("Répertoire Uploads/ n'existe pas.");
            }
            if (!uploadDir.canWrite()) {
                throw new IOException("Répertoire Uploads/ non accessible en écriture.");
            }

            // Copy file
            Files.copy(selectedFile.toPath(), destination);
            System.out.println("File copied: " + destination);

            // Apply watermark
            File watermarkFile = new File(WATERMARK_PATH);
            if (!watermarkFile.exists()) {
                throw new IOException("Fichier de watermark introuvable à : " + WATERMARK_PATH);
            }
            addWatermark(destination.toString(), WATERMARK_PATH);
            System.out.println("Watermark applied to: " + destination);

            // Update fileLabel
            fileLabel.setText(newFilename);
            System.out.println("fileLabel updated: " + fileLabel.getText());

            // Preview image
            try {
                Image image = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setVisible(true);
                imagePreview.setManaged(true);
            } catch (Exception e) {
                System.err.println("Error loading image preview: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Upload error: " + e.getMessage());
            showErrorAlert("Erreur d'upload", e.getMessage());
        }
    }

    @FXML
    void ajouterCoursAction(ActionEvent event) {
        try {
            // Retrieve inputs
            String title = titleField.getText().trim();
            String langue = langueComboBox.getValue();
            String description = descriptionField.getText().trim();
            String prixText = prixField.getText().trim();
            Matiere matiere = matiereComboBox.getValue();
            String lienDePaiment = lienDePaimentField.getText().trim();
            String imagePath = fileLabel.getText();

            // Validation
            if (title.isEmpty()) throw new IllegalArgumentException("Le titre ne peut pas être vide.");
            if (title.length() > 255) throw new IllegalArgumentException("Le titre ne peut pas dépasser 255 caractères.");
            if (description.isEmpty()) throw new IllegalArgumentException("La description ne peut pas être vide.");
            if (description.length() > 255) throw new IllegalArgumentException("La description ne peut pas dépasser 255 caractères.");
            if (langue == null) throw new IllegalArgumentException("La langue est obligatoire.");
            if (!Arrays.asList(VALID_LANGUAGES).contains(langue)) throw new IllegalArgumentException("Langue non valide.");
            if (matiere == null) throw new IllegalArgumentException("La matière ne peut pas être vide.");
            if (prixText.isEmpty()) throw new IllegalArgumentException("Le prix est obligatoire.");
            int prix;
            try {
                prix = Integer.parseInt(prixText);
                if (prix < 0) throw new IllegalArgumentException("Le prix ne peut pas être négatif.");
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Le prix doit être un nombre entier.");
            }
            if (prix != 0 && lienDePaiment.isEmpty()) {
                throw new IllegalArgumentException("Le lien de paiement est requis si le prix est non nul.");
            }
            if (!lienDePaiment.isEmpty() && lienDePaiment.length() > 255) {
                throw new IllegalArgumentException("Le lien de paiement ne peut pas dépasser 255 caractères.");
            }
            if (imagePath == null || imagePath.equals("Aucun fichier choisi")) {
                throw new IllegalArgumentException("L'image du cours est obligatoire.");
            }

            // Create Cours
            Cours cours = new Cours();
            cours.setTitle(title);
            cours.setDescription(description);
            cours.setUrlImage(imagePath);
            cours.setMatiere(matiere);
            cours.setLangue(langue);
            cours.setPrix(prix);
            cours.setLienDePaiment(lienDePaiment.isEmpty() ? null : lienDePaiment);

            // Save to database
            serviceCours.add(cours);
            System.out.println("Cours saved: " + cours);

            // Share on Facebook
            shareOnFacebook(cours);

            // Show success
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Cours ajouté avec succès !");
            alert.show();

            // Clear form
            clearForm();

        } catch (IllegalArgumentException e) {
            showErrorAlert("Erreur de saisie", e.getMessage());
        } catch (Exception e) {
            System.err.println("Error saving cours: " + e.getMessage());
            showErrorAlert("Erreur", "Échec de l'ajout du cours : " + e.getMessage());
        }
    }

    private void clearForm() {
        titleField.clear();
        langueComboBox.setValue(null);
        descriptionField.clear();
        prixField.clear();
        matiereComboBox.setValue(null);
        lienDePaimentField.clear();
        fileLabel.setText("Aucun fichier choisi");
        System.out.println("Form cleared");
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.show();
        System.out.println("Error alert: " + title + " - " + message);
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replaceAll("[^\\w\\s-]", "")
                .replaceAll("[\\s+]", "-")
                .toLowerCase();
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
    }

    private void addWatermark(String imagePath, String watermarkPath) throws IOException {
        System.out.println("Adding watermark to: " + imagePath);
        BufferedImage image = ImageIO.read(new File(imagePath));
        BufferedImage watermark = ImageIO.read(new File(watermarkPath));

        // Resize watermark to 20% of original size
        int wWidth = (int) (watermark.getWidth() * 0.2);
        int wHeight = (int) (watermark.getHeight() * 0.2);
        BufferedImage scaledWatermark = new BufferedImage(wWidth, wHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledWatermark.createGraphics();
        g2d.drawImage(watermark.getScaledInstance(wWidth, wHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();

        // Position watermark (bottom-right, 2px margin)
        int x = image.getWidth() - wWidth - 2;
        int y = image.getHeight() - wHeight - 2;

        // Apply watermark with 50% opacity
        g2d = image.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.drawImage(scaledWatermark, x, y, null);
        g2d.dispose();

        // Save image
        ImageIO.write(image, getFileExtension(imagePath), new File(imagePath));
        System.out.println("Watermark saved");
    }

    private void shareOnFacebook(Cours cours) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String message = String.format(
                    "🎓 Nouveau cours disponible !\n\n" +
                            "📚 Titre : %s\n" +
                            "📝 Description : %s\n" +
                            "💵 Prix : %s\n" +
                            "🌐 Langue : %s\n\n" +
                            "👉 Découvrez-le maintenant : %s",
                    cours.getTitle(),
                    cours.getDescription(),
                    cours.getPrix() > 0 ? cours.getPrix() + " DT" : "Gratuit",
                    cours.getLangue().toUpperCase(),
                    COURSE_URL_BASE + cours.getId()
            );

            String url = String.format("https://graph.facebook.com/v22.0/%s/feed", FACEBOOK_PAGE_ID);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "message=" + URLEncoder.encode(message, "UTF-8") +
                                    "&access_token=" + URLEncoder.encode(FACEBOOK_ACCESS_TOKEN, "UTF-8")
                    ))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Facebook response: " + response.body());

            if (response.statusCode() != 200) {
                showErrorAlert("Avertissement", "Cours créé, mais échec du partage Facebook.");
            } else {
                System.out.println("Facebook post successful");
            }

        } catch (Exception e) {
            System.err.println("Facebook sharing error: " + e.getMessage());
            showErrorAlert("Erreur", "Erreur lors du partage Facebook : " + e.getMessage());
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListeCours.fxml"));
            matiereComboBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void retourAuxCours(Event event){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListeCours.fxml"));
            matiereComboBox.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void handleListes(ActionEvent event) {
        System.out.println("handleListes clicked");
        loadScene("/ListeCours.fxml");
    }

    private void loadScene(String fxmlPath) {
        try {
            // Load the new FXML
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Get the current stage from a known node
            Stage stage = (Stage) langueComboBox.getScene().getWindow();
            // Create a new scene with the loaded root
            Scene scene = new Scene(root, 1000, 700); // Match FXML dimensions
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}