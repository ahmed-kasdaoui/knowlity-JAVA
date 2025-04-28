package tn.esprit.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEvents;

import java.util.List;

public class EventStatisticsChart extends Application {

    @Override
    public void start(Stage primaryStage) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Event Title");
        yAxis.setLabel("Participants");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Event Participation Overview");
        barChart.setLegendVisible(true);

        XYChart.Series<String, Number> maxParticipantsSeries = new XYChart.Series<>();
        maxParticipantsSeries.setName("Max Participants");

        XYChart.Series<String, Number> seatsAvailableSeries = new XYChart.Series<>();
        seatsAvailableSeries.setName("Seats Available");

        ServiceEvents serviceEvents = new ServiceEvents();
        List<Events> eventList = serviceEvents.getAll();

        for (Events event : eventList) {
            String title = event.getTitle();
            Integer maxParticipants = event.getMaxParticipants();
            Integer seatsAvailable = event.getSeatsAvailable();

            if (maxParticipants != null) {
                maxParticipantsSeries.getData().add(new XYChart.Data<>(title, maxParticipants));
            }
            if (seatsAvailable != null) {
                seatsAvailableSeries.getData().add(new XYChart.Data<>(title, seatsAvailable));
            }
        }

        barChart.getData().addAll(maxParticipantsSeries, seatsAvailableSeries);

        VBox root = new VBox(barChart);
        Scene scene = new Scene(root, 1000, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Event Statistics");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
