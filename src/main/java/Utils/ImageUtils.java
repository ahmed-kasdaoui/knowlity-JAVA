package Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class ImageUtils {
    
    public static String encodeImage(String imagePath) {
        try {
            byte[] fileContent = Files.readAllBytes(new File(imagePath).toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            System.err.println("Error encoding image: " + e.getMessage());
            return null;
        }
    }

    public static String saveImage(String sourcePath) {
        return encodeImage(sourcePath);
    }

    public static String getImageUrl(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            return null;
        }
        return "data:image/png;base64," + base64Image;
    }
}
