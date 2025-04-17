package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.Categorie;
import tn.esprit.services.ServiceCategorie;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

public class AjouterCategorieController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField motsClesField;

    @FXML
    private ComboBox<String> publicCibleComboBox;

    @FXML
    private TextArea descrptionField;

    @FXML
    private Button uploadButton;

    @FXML
    private Label fileLabel;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Button saveButton;

    @FXML
    private Label nameError;

    @FXML
    private Label motsClesError;

    @FXML
    private Label publicCibleError;

    @FXML
    private Label descrptionError;

    @FXML
    private Label brochureError;

    private ServiceCategorie serviceCategorie = new ServiceCategorie();
    private static final String UPLOAD_DIR = "Uploads/";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final String[] PUBLIC_CIBLE_OPTIONS = {"élèves", "étudiants", "adultes", "professionnels"};
    private static final int TARGET_SIZE = 200; // 200x200 pixels

    @FXML
    public void initialize() {
        // Ensure upload directory exists
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            System.out.println("Uploads directory creation: " + (created ? "Success" : "Failed") + " at " + uploadDir.getAbsolutePath());
        }
        if (!uploadDir.canWrite()) {
            System.err.println("Uploads directory is not writable: " + uploadDir.getAbsolutePath());
        }

        // Populate publicCibleComboBox
        publicCibleComboBox.getItems().addAll(PUBLIC_CIBLE_OPTIONS);
        System.out.println("publicCibleComboBox populated with: " + Arrays.toString(PUBLIC_CIBLE_OPTIONS));
    }

    @FXML
    void uploadButtonAction(ActionEvent event) {
        try {
            System.out.println("Upload button clicked");

            // Verify stage
            if (uploadButton.getScene() == null || uploadButton.getScene().getWindow() == null) {
                throw new IllegalStateException("Scene or window not initialized");
            }
            System.out.println("Stage verified");

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner une image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png")
            );
            File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
            if (selectedFile == null) {
                System.out.println("No file selected (dialog canceled)");
                fileLabel.setText("Aucune image");
                fileLabel.getStyleClass().removeAll("text-success", "text-danger");
                fileLabel.getStyleClass().add("text-muted");
                imagePreview.setVisible(false);
                imagePreview.setManaged(false);
                return;
            }
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());

            // Validate file
            if (!selectedFile.exists()) {
                throw new IllegalArgumentException("Fichier introuvable.");
            }
            if (selectedFile.length() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("Fichier trop volumineux (max 2 Mo).");
            }
            String extension = getFileExtension(selectedFile.getName()).toLowerCase();
            if (!extension.matches("jpg|png")) {
                throw new IllegalArgumentException("Seuls les fichiers JPG et PNG sont acceptés.");
            }
            System.out.println("File validated: size=" + selectedFile.length() + ", extension=" + extension);

            // Generate unique filename
            String newFilename = "category-" + UUID.randomUUID() + ".jpg";
            Path destination = Paths.get(UPLOAD_DIR, newFilename);
            System.out.println("Destination: " + destination);

            // Read image
            BufferedImage originalImage = ImageIO.read(selectedFile);
            if (originalImage == null) {
                throw new IllegalArgumentException("Impossible de lire l'image. Format non supporté ou fichier corrompu.");
            }
            System.out.println("Original image read: width=" + originalImage.getWidth() + ", height=" + originalImage.getHeight());

            // Resize and crop to 200x200
            BufferedImage resizedImage = resizeAndCropImage(originalImage, TARGET_SIZE, TARGET_SIZE);
            System.out.println("Image resized: width=" + resizedImage.getWidth() + ", height=" + resizedImage.getHeight());

            // Save as JPEG
            File outputFile = destination.toFile();
            boolean written = ImageIO.write(resizedImage, "jpg", outputFile);
            if (!written) {
                throw new IllegalArgumentException("Échec de la conversion en JPEG.");
            }
            System.out.println("Image saved as JPEG: " + destination);

            // Verify file exists and is readable
            if (!outputFile.exists() || !outputFile.canRead()) {
                throw new IllegalArgumentException("Le fichier enregistré est inaccessible.");
            }
            System.out.println("Saved file verified: exists=" + outputFile.exists() + ", readable=" + outputFile.canRead());

            // Update fileLabel
            fileLabel.setText(newFilename);
            fileLabel.getStyleClass().removeAll("text-muted", "text-danger");
            fileLabel.getStyleClass().add("text-success");
            System.out.println("fileLabel updated: " + newFilename);

            // Update image preview
            try {
                Image fxImage = new Image(outputFile.toURI().toString(), true);
                imagePreview.setImage(fxImage);
                imagePreview.setVisible(true);
                imagePreview.setManaged(true);
                System.out.println("Image preview set: " + outputFile.toURI());
            } catch (Exception e) {
                System.err.println("Failed to load image for preview: " + e.getMessage());
                fileLabel.setText("Erreur: Aperçu non disponible");
                fileLabel.getStyleClass().removeAll("text-muted", "text-success");
                fileLabel.getStyleClass().add("text-danger");
            }

        } catch (Exception e) {
            System.err.println("Upload error: " + e.getMessage());
            e.printStackTrace();
            fileLabel.setText("Erreur: " + e.getMessage());
            fileLabel.getStyleClass().removeAll("text-muted", "text-success");
            fileLabel.getStyleClass().add("text-danger");
            showAlert(Alert.AlertType.ERROR, "Erreur d'upload", e.getMessage());
            imagePreview.setVisible(false);
            imagePreview.setManaged(false);
        }
    }

    // Updated method to resize and crop image with bicubic interpolation
    private BufferedImage resizeAndCropImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        // Validate inputs
        if (originalImage == null) {
            throw new IllegalArgumentException("L'image originale est null.");
        }
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException("Dimensions cibles invalides: " + targetWidth + "x" + targetHeight);
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        if (originalWidth <= 0 || originalHeight <= 0) {
            throw new IllegalArgumentException("Dimensions de l'image originale invalides: " + originalWidth + "x" + originalHeight);
        }
        System.out.println("Resizing image: original=" + originalWidth + "x" + originalHeight + ", target=" + targetWidth + "x" + targetHeight);

        // Step 1: Calculate scaling to fit target dimensions
        double aspectRatio = (double) originalWidth / originalHeight;
        int scaledWidth, scaledHeight;

        if (aspectRatio > 1) {
            // Wider than tall
            scaledHeight = targetHeight;
            scaledWidth = (int) (targetHeight * aspectRatio);
        } else {
            // Taller than wide or square
            scaledWidth = targetWidth;
            scaledHeight = (int) (targetWidth / aspectRatio);
        }

        // Ensure scaled dimensions are positive
        scaledWidth = Math.max(1, scaledWidth);
        scaledHeight = Math.max(1, scaledHeight);
        System.out.println("Scaled dimensions: " + scaledWidth + "x" + scaledHeight);

        // Step 2: Create scaled image
        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC); // Smoother than bilinear
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);        } finally {
            g2d.dispose();
        }

        // Step 3: Crop to exactly targetWidth x targetHeight
        int x = (scaledWidth - targetWidth) / 2;
        int y = (scaledHeight - targetHeight) / 2;
        // Ensure crop coordinates are valid
        x = Math.max(0, x);
        y = Math.max(0, y);
        int cropWidth = Math.min(targetWidth, scaledWidth);
        int cropHeight = Math.min(targetHeight, scaledHeight);
        System.out.println("Cropping: x=" + x + ", y=" + y + ", width=" + cropWidth + ", height=" + cropHeight);

        BufferedImage croppedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        g2d = croppedImage.createGraphics();
        try {
            g2d.drawImage(scaledImage, 0, 0, targetWidth, targetHeight, x, y, x + cropWidth, y + cropHeight, null);
        } finally {
            g2d.dispose();
        }

        return croppedImage;
    }


    @FXML
    void RetourAction(ActionEvent event) {

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListeCategories.fxml"));
            nameField.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    @FXML
    void saveAction(ActionEvent event) {
        try {
            // Reset validation
            resetValidation();

            // Retrieve inputs
            String name = nameField.getText().trim();
            String motsCles = motsClesField.getText().trim();
            String publicCible = publicCibleComboBox.getValue();
            String descrption = descrptionField.getText().trim();
            String icone = fileLabel.getText();

            // Validate
            boolean hasError = false;
            if (name.isEmpty()) {
                nameError.setText("Le nom est requis.");
                nameError.setVisible(true);
                nameError.setManaged(true);
                nameField.getStyleClass().add("error");
                hasError = true;
            } else if (name.length() < 3) {
                nameError.setText("Minimum 3 caractères.");
                nameError.setVisible(true);
                nameError.setManaged(true);
                nameField.getStyleClass().add("error");
                hasError = true;
            } else if (name.length() > 255) {
                nameError.setText("Maximum 255 caractères.");
                nameError.setVisible(true);
                nameError.setManaged(true);
                nameField.getStyleClass().add("error");
                hasError = true;
            }

            if (motsCles.isEmpty()) {
                motsClesError.setText("Les mots-clés sont requis.");
                motsClesError.setVisible(true);
                motsClesError.setManaged(true);
                motsClesField.getStyleClass().add("error");
                hasError = true;
            } else if (motsCles.length() > 255) {
                motsClesError.setText("Maximum 255 caractères.");
                motsClesError.setVisible(true);
                motsClesError.setManaged(true);
                motsClesField.getStyleClass().add("error");
                hasError = true;
            }

            if (publicCible == null) {
                publicCibleError.setText("Le public cible est requis.");
                publicCibleError.setVisible(true);
                publicCibleError.setManaged(true);
                publicCibleComboBox.getStyleClass().add("error");
                hasError = true;
            } else if (!Arrays.asList(PUBLIC_CIBLE_OPTIONS).contains(publicCible)) {
                publicCibleError.setText("Choix invalide.");
                publicCibleError.setVisible(true);
                publicCibleError.setManaged(true);
                publicCibleComboBox.getStyleClass().add("error");
                hasError = true;
            }

            if (descrption.isEmpty()) {
                descrptionError.setText("La description est requise.");
                descrptionError.setVisible(true);
                descrptionError.setManaged(true);
                descrptionField.getStyleClass().add("error");
                hasError = true;
            } else if (descrption.length() < 10) {
                descrptionError.setText("Minimum 10 caractères.");
                descrptionError.setVisible(true);
                descrptionError.setManaged(true);
                descrptionField.getStyleClass().add("error");
                hasError = true;
            } else if (descrption.length() > 255) {
                descrptionError.setText("Maximum 255 caractères.");
                descrptionError.setVisible(true);
                descrptionError.setManaged(true);
                descrptionField.getStyleClass().add("error");
                hasError = true;
            }

            if (icone.equals("Aucune image") || icone.startsWith("Erreur")) {
                brochureError.setText("L’image est requise.");
                brochureError.setVisible(true);
                brochureError.setManaged(true);
                fileLabel.getStyleClass().removeAll("text-muted", "text-success");
                fileLabel.getStyleClass().add("text-danger");
                hasError = true;
            }

            if (hasError) {
                return;
            }

            // Create and save categorie
            Categorie categorie = new Categorie();
            categorie.setName(name);
            categorie.setMotsCles(motsCles);
            categorie.setPublicCible(publicCible);
            categorie.setDescrption(descrption);
            categorie.setIcone(icone);

            serviceCategorie.add(categorie);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie enregistrée.");
            // Navigate to category list (implement as needed)

        } catch (Exception e) {
            System.err.println("Save error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void resetValidation() {
        nameError.setVisible(false);
        nameError.setManaged(false);
        motsClesError.setVisible(false);
        motsClesError.setManaged(false);
        publicCibleError.setVisible(false);
        publicCibleError.setManaged(false);
        descrptionError.setVisible(false);
        descrptionError.setManaged(false);
        brochureError.setVisible(false);
        brochureError.setManaged(false);

        nameField.getStyleClass().remove("error");
        motsClesField.getStyleClass().remove("error");
        publicCibleComboBox.getStyleClass().remove("error");
        descrptionField.getStyleClass().remove("error");
        fileLabel.getStyleClass().removeAll("text-success", "text-danger");
        fileLabel.getStyleClass().add("text-muted");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex == -1 ? "" : filename.substring(dotIndex + 1);
    }
    @FXML
    void handleListes(ActionEvent event) {
        System.out.println("handleListes clicked");
        loadScene("/ListeCategories.fxml");
    }
    private void loadScene(String fxmlPath) {
        try {
            // Load the new FXML
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Get the current stage from a known node
            Stage stage = (Stage) nameField.getScene().getWindow();
            // Create a new scene with the loaded root
            Scene scene = new Scene(root, 1000, 700); // Match FXML dimensions
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }

    }

}