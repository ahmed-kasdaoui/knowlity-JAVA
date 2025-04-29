package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
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
            seriesMaxParticipants.getData().add(new XYChart.Data<>(event.getTitle().substring(0,5).concat("..."), event.getMaxParticipants()));
            seriesPlacesDisponibles.getData().add(new XYChart.Data<>(event.getTitle().substring(0,5).concat("..."), event.getSeatsAvailable()));
        }

        barChart.getData().addAll(seriesMaxParticipants, seriesPlacesDisponibles);
        barChart.setCategoryGap(20);
        barChart.setBarGap(10);

    }
}
