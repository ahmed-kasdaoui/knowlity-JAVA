import javafx.application.Application;
import javafx.stage.Stage;
import view.MainApplication;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        MainApplication app = new MainApplication();
        app.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
