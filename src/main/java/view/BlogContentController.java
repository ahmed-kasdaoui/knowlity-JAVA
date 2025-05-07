package view;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class BlogContentController {
    @FXML
    private Text titleText;
    
    @FXML
    private Text contentText;
    
    @FXML
    private Button closeButton;
    
    public void setBlogContent(String title, String content) {
        titleText.setText(title);
        contentText.setText(content);
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
