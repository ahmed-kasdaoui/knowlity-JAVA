package tn.esprit.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.models.EventRegistration;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEventRegistration;

import java.time.LocalDateTime;

public class TestEventRegistrationList extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Mock data
        Events event = new Events();
        event.setId(1);
        event.setTitle("Tech Conference");

        EventRegistration registration = new EventRegistration(
                1, 1, event, LocalDateTime.now(), "Pending", true, "Online", "John Doe", 2
        );

        ServiceEventRegistration service = new ServiceEventRegistration();
        service.add(registration);

        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NewEventRegistration.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("Event Registration List");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}