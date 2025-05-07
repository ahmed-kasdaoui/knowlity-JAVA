package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BlogListView {
    private final boolean isAdmin;
    
    public BlogListView(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
    
    public void show(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/blog_list.fxml"));
        Scene scene = new Scene(loader.load());
        BlogListController controller = loader.getController();
        controller.setAdminMode(isAdmin);
        stage.setScene(scene);
    }
}
