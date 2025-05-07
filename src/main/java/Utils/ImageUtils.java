package Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

public class ImageUtils {
    private static final String UPLOAD_DIR = "src/main/resources/uploads/";
    
    public static String saveImage(File sourceFile) {
        try {
            // Créer le répertoire s'il n'existe pas
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Générer un nom de fichier unique
            String fileName = UUID.randomUUID().toString() + getFileExtension(sourceFile.getName());
            Path targetPath = Paths.get(UPLOAD_DIR + fileName);

            // Copier le fichier
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Retourner le chemin relatif pour le stockage en base de données
            return "/uploads/" + fileName;
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            return null;
        }
    }

    private static String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ".png"; // Extension par défaut
        }
        return fileName.substring(lastIndexOf);
    }

    public static String encodeImage(String imagePath) {
        try {
            byte[] fileContent = Files.readAllBytes(new File(imagePath).toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            System.err.println("Error encoding image: " + e.getMessage());
            return null;
        }
    }

    public static String getImageUrl(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            return null;
        }
        return "data:image/png;base64," + base64Image;
    }
}
