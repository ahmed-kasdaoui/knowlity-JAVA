package tn.esprit.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEvents;

import java.time.LocalDateTime;

public class TestEventList extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Mock data
        ServiceEvents service = new ServiceEvents();
        Events event1 = new Events();
        event1.setId(1);
        event1.setTitle("Tech Conference");
        event1.setDescription("Annual tech event");
        event1.setStartDate(LocalDateTime.now().plusDays(1));
        event1.setEndDate(LocalDateTime.now().plusDays(7));
        event1.setType("On-Site");
        event1.setMaxParticipants(100);
        event1.setSeatsAvailable(50);
        event1.setLocation("Tunis");
        event1.setCreatedAt(LocalDateTime.now());
        event1.setImage("/images/test.jpg");
        event1.setCategory("Cultural");
        event1.setOrganizerId(1);


        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventRegistrationList.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("Event List");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}