package view;

import Entities.Creator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;

public class CreatorProfileController {
    @FXML private ImageView creatorImage;
    @FXML private Label nameLabel;
    @FXML private Label profileLabel;
    @FXML private Label achievementsLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label updatedAtLabel;
    @FXML private Button backButton;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setCreator(Creator creator) {
        // Configurer l'image
        try {
            Image image = new Image(creator.getImage());
            creatorImage.setImage(image);
        } catch (Exception e) {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-avatar.png"));
            creatorImage.setImage(defaultImage);
        }

        // Configurer les informations
        nameLabel.setText(creator.getName());
        profileLabel.setText(creator.getProfile());
        achievementsLabel.setText(creator.getAchievements());
        
        // Formater les dates
        createdAtLabel.setText(creator.getCreatedAt().toLocalDateTime().format(DATE_FORMATTER));
        updatedAtLabel.setText(creator.getUpdatedAt().toLocalDateTime().format(DATE_FORMATTER));
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}
