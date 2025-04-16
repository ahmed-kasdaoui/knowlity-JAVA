package controllers;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import tn.esprit.models.Cours;
import tn.esprit.models.Matiere;
import tn.esprit.services.ServiceCours;
import tn.esprit.services.ServiceMatiere;

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
import java.text.Normalizer;
import java.util.Arrays;
import java.util.UUID;

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

    private static final String UPLOAD_DIR = "Uploads/";
    private static final String WATERMARK_PATH = "src/main/resources/watermark.png";
    private static final String[] VALID_LANGUAGES = {"fr", "en", "es", "de", "ar"};
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB (updated per your code)
    private static final String COURSE_URL_BASE = "http://localhost:8080/cours/";
    private static final String FACEBOOK_PAGE_ID = "535397399664579";
    private static final String FACEBOOK_ACCESS_TOKEN = "EAAq1jUXunxQBOxr3qXCxWKLKVarrhK90Je7GnHrKGY4QF2jghFYTgAzJZAuYFDRISY2rkMJduXxVWNeUZCNnWBw88VScGzySVY4GrlrJODmACZCFfMimoxNS7uHZBLPtZApUJhUMckALChavZBe8NWT8HizJU9yXjPmelMf3mFjsoanmQHTrgAzzGFWXCHKnuZB";

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
        ServiceMatiere serviceMatiere = new ServiceMatiere();
        matiereComboBox.getItems().addAll(serviceMatiere.getAll());
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
            ServiceCours serviceCours = new ServiceCours();
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
}