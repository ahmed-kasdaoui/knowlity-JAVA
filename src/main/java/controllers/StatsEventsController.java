package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.Node;
import tn.esprit.services.ServiceEvents;
import tn.esprit.models.Events;

import java.sql.SQLException;
import java.util.List;

public class StatsEventsController {

    @FXML
    private BarChart<String, Number> barChart;

    private final ServiceEvents eventServices = new ServiceEvents();

    @FXML
    public void initialize() {
        chargerStats();
    }

    private void chargerStats() {
        barChart.getData().clear();
        List<Events> events = eventServices.getAll();

        XYChart.Series<String, Number> seriesMaxParticipants = new XYChart.Series<>();
        seriesMaxParticipants.setName("Max Participants");

        XYChart.Series<String, Number> seriesPlacesDisponibles = new XYChart.Series<>();
        seriesPlacesDisponibles.setName("Available Places");

        for (Events event : events) {
            String truncatedTitle = event.getTitle().substring(0, Math.min(5, event.getTitle().length())).concat("...");
            seriesMaxParticipants.getData().add(new XYChart.Data<>(truncatedTitle, event.getMaxParticipants()));
            seriesPlacesDisponibles.getData().add(new XYChart.Data<>(truncatedTitle, event.getSeatsAvailable()));
        }

        barChart.getData().addAll(seriesMaxParticipants, seriesPlacesDisponibles);
        barChart.setCategoryGap(20);
        barChart.setBarGap(10);

        // Defer tooltip setup until the chart is rendered
        Platform.runLater(() -> {
            for (XYChart.Series<String, Number> series : barChart.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        String fullTitle = events.get(barChart.getData().indexOf(series) * series.getData().size() + series.getData().indexOf(data)).getTitle();
                        Tooltip tooltip = new Tooltip(fullTitle);
                        Tooltip.install(node, tooltip);
                    }
                }
            }
        });
    }
}