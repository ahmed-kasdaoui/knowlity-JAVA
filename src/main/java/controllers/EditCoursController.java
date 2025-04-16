
        package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import tn.esprit.models.Cours;
import tn.esprit.models.Matiere;
import tn.esprit.services.ServiceCours;
import tn.esprit.services.ServiceMatiere;

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

public class EditCoursController {

    @FXML private TextField titleField;
    @FXML private ComboBox<String> langueComboBox;
    @FXML private TextArea descriptionField;
    @FXML private TextField prixField;
    @FXML private Label fileLabel;
    @FXML private ComboBox<Matiere> matiereComboBox;
    @FXML private TextField lienDePaimentField;
    @FXML private Button uploadButton;
    @FXML private Button submitButton;
    @FXML private ImageView imagePreview;

    private Cours course;
    private static final String UPLOAD_DIR = "src/main/resources/Uploads/";
    private static final String WATERMARK_PATH = "src/main/resources/watermark.png";
    private static final String[] VALID_LANGUAGES = {"fr", "en", "es", "de", "ar"};
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String COURSE_URL_BASE = "http://localhost:8080/cours/";
    private static final String FACEBOOK_PAGE_ID = "535397399664579";
    private static final String FACEBOOK_ACCESS_TOKEN = "EAAq1jUXunxQBOxr3qXCxWKLKVarrhK90Je7GnHrKGY4QF2jghFYTgAzJZAuYFDRISY2rkMJduXxVWNeUZCNnWBw88VScGzySVY4GrlrJODmACZCFfMimoxNS7uHZBLPtZApUJhUMckALChavZBe8NWT8HizJU9yXjPmelMf3mFjsoanmQHTrgAzzGFWXCHKnuZB";

    @FXML
    public void initialize() {
        System.out.println("Initializing EditCoursController");
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

        // Populate matiereComboBox
        ServiceMatiere serviceMatiere = new ServiceMatiere();
        matiereComboBox.getItems().addAll(serviceMatiere.getAll());

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

    public void setCourse(Cours course) {
        this.course = course;
        if (course != null) {
            titleField.setText(course.getTitle());
            langueComboBox.setValue(course.getLangue());
            descriptionField.setText(course.getDescription());
            prixField.setText(String.valueOf(course.getPrix()));
            lienDePaimentField.setText(course.getLienDePaiment() != null ? course.getLienDePaiment() : "");
            matiereComboBox.setValue(course.getMatiere());
            fileLabel.setText(course.getUrlImage() != null ? course.getUrlImage() : "Aucune image");
            if (course.getUrlImage() != null && !course.getUrlImage().isEmpty()) {
                try {
                    imagePreview.setImage(new Image("file:" + UPLOAD_DIR + course.getUrlImage()));
                    imagePreview.setVisible(true);
                    imagePreview.setManaged(true);
                } catch (Exception e) {
                    System.err.println("Failed to load course image: " + e.getMessage());
                    fileLabel.setText("Erreur d'image");
                }
            }
        }
    }

    @FXML
    void uploadButtonAction(ActionEvent event) {
        try {
            if (uploadButton.getScene() == null || uploadButton.getScene().getWindow() == null) {
                throw new IllegalStateException("Scene or window not initialized");
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner une image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.webp")
            );

            File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
            if (selectedFile == null) {
                return;
            }

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

            // Copy file
            Files.copy(selectedFile.toPath(), destination);

            // Apply watermark (using JavaFX Image if possible, fallback to AWT)
            // Note: For simplicity, keeping AWT as per original, but recommend JavaFX Image processing
            File watermarkFile = new File(WATERMARK_PATH);
            if (watermarkFile.exists()) {
                addWatermark(destination.toString(), WATERMARK_PATH);
            }

            // Update UI
            fileLabel.setText(newFilename);
            imagePreview.setImage(new Image("file:" + destination.toString()));
            imagePreview.setVisible(true);
            imagePreview.setManaged(true);

        } catch (Exception e) {
            System.err.println("Upload error: " + e.getMessage());
            showErrorAlert("Erreur d'upload", e.getMessage());
        }
    }

    @FXML
    void updateCoursAction(ActionEvent event) {
        try {
            if (course == null) {
                throw new IllegalStateException("Aucun cours sélectionné pour modification.");
            }

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
            if (description.length() > 1000) throw new IllegalArgumentException("La description ne peut pas dépasser 1000 caractères.");
            if (langue == null) throw new IllegalArgumentException("La langue est obligatoire.");
            if (!Arrays.asList(VALID_LANGUAGES).contains(langue)) throw new IllegalArgumentException("Langue non valide.");
            if (matiere == null) throw new IllegalArgumentException("La matière ne peut pas être vide.");
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
            if (imagePath == null || imagePath.equals("Aucune image") || imagePath.equals("Erreur d'image")) {
                throw new IllegalArgumentException("L'image du cours est obligatoire.");
            }

            // Update Cours
            course.setTitle(title);
            course.setDescription(description);
            course.setUrlImage(imagePath);
            course.setMatiere(matiere);
            course.setLangue(langue);
            course.setPrix(prix);
            course.setLienDePaiment(lienDePaiment.isEmpty() ? null : lienDePaiment);

            // Save to database
            ServiceCours serviceCours = new ServiceCours();
            serviceCours.update(course);

            // Share on Facebook
            shareOnFacebook(course);

            // Show success
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Cours modifié avec succès !");
            alert.show();

            // Navigate back
            retourAuxCours(null);

        } catch (IllegalArgumentException e) {
            showErrorAlert("Erreur de saisie", e.getMessage());
        } catch (Exception e) {
            System.err.println("Error updating cours: " + e.getMessage());
            showErrorAlert("Erreur", "Échec de la modification du cours : " + e.getMessage());
        }
    }

    @FXML
    void retourAuxCours(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CourseDetails.fxml"));
            Parent root = loader.load();
            CourseDetailsController controller = loader.getController();
            controller.setCourse(course);
            titleField.getScene().setRoot(root);
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

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.show();
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
        // Note: Using AWT as per original, but consider JavaFX Image processing
        System.out.println("Adding watermark to: " + imagePath);
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(new File(imagePath));
        java.awt.image.BufferedImage watermark = javax.imageio.ImageIO.read(new File(watermarkPath));

        // Resize watermark to 20% of original size
        int wWidth = (int) (watermark.getWidth() * 0.2);
        int wHeight = (int) (watermark.getHeight() * 0.2);
        java.awt.image.BufferedImage scaledWatermark = new java.awt.image.BufferedImage(wWidth, wHeight, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = scaledWatermark.createGraphics();
        g2d.drawImage(watermark.getScaledInstance(wWidth, wHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();

        // Position watermark (bottom-right, 2px margin)
        int x = image.getWidth() - wWidth - 2;
        int y = image.getHeight() - wHeight - 2;

        // Apply watermark with 50% opacity
        g2d = image.createGraphics();
        g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f));
        g2d.drawImage(scaledWatermark, x, y, null);
        g2d.dispose();

        // Save image
        javax.imageio.ImageIO.write(image, getFileExtension(imagePath), new File(imagePath));
    }

    private void shareOnFacebook(Cours cours) {
        try {
            if (cours.getId() <= 0) {
                System.out.println("Course ID not set, skipping Facebook share");
                return;
            }

            HttpClient client = HttpClient.newHttpClient();
            String message = String.format(
                    "🎓 Cours mis à jour !\n\n" +
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
            if (response.statusCode() != 200) {
                showErrorAlert("Avertissement", "Cours modifié, mais échec du partage Facebook.");
            }
        } catch (Exception e) {
            System.err.println("Facebook sharing error: " + e.getMessage());
            showErrorAlert("Erreur", "Erreur lors du partage Facebook : " + e.getMessage());
        }
    }
}
