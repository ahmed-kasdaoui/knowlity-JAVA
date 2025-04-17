package view;

import Entities.Blog;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.time.format.DateTimeFormatter;

public class BlogDetailController {
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label creatorLabel;
    
    @FXML
    private Label dateLabel;
    
    @FXML
    private ImageView blogImage;
    
    @FXML
    private TextFlow contentFlow;
    
    @FXML
    private VBox imageContainer;
    
    private Stage stage;
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setBlog(Blog blog) {
        System.out.println("Setting blog in detail view: " + blog.getTitle());
        
        // Set title
        titleLabel.setText(blog.getTitle());
        
        // Set creator info
        creatorLabel.setText("Par " + blog.getCreatorName());
        
        // Set date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        dateLabel.setText("Publié le " + blog.getCreatedAt().format(formatter));
        
        // Set content
        Text contentText = new Text(blog.getContent());
        contentFlow.getChildren().clear();
        contentFlow.getChildren().add(contentText);
        
        // Handle image
        String blogImage = blog.getBlogImage();
        if (blogImage != null && !blogImage.isEmpty()) {
            System.out.println("Blog has image data, length: " + blogImage.length());
            try {
                // Clean the Base64 string
                if (blogImage.contains(",")) {
                    blogImage = blogImage.split(",")[1];
                }
                
                byte[] imageData = Base64.getDecoder().decode(blogImage.trim());
                System.out.println("Decoded image data length: " + imageData.length);
                
                Image image = new Image(new ByteArrayInputStream(imageData));
                System.out.println("Image loaded: " + image.getWidth() + "x" + image.getHeight());
                
                this.blogImage.setImage(image);
                
                // Set a minimum size for the image
                this.blogImage.setFitWidth(600);
                this.blogImage.setPreserveRatio(true);
                
                imageContainer.setVisible(true);
                imageContainer.setManaged(true);
                System.out.println("Image container set to visible");
            } catch (Exception e) {
                System.err.println("Error loading blog image: " + e.getMessage());
                e.printStackTrace();
                imageContainer.setVisible(false);
                imageContainer.setManaged(false);
            }
        } else {
            System.out.println("No image data found for blog");
            imageContainer.setVisible(false);
            imageContainer.setManaged(false);
        }
    }
    
    @FXML
    private void handleClose() {
        stage.close();
    }
}
