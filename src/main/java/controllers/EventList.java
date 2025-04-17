package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.models.*;
import tn.esprit.services.ServiceEvents;

import java.time.LocalDateTime;
import java.util.List;

public class EventList {

    @FXML
    private TableColumn<Events, String> description;

    @FXML
    private TableColumn<Events, LocalDateTime> enddate;

    @FXML
    private TableColumn<Events, Integer> seats;

    @FXML
    private TableColumn<Events, LocalDateTime> startdate;

    @FXML
    private TableColumn<Events, String> title;

    @FXML
    private TableColumn<Events, String> type;

    @FXML
    private TableView<Events> tableEvent;

    ServiceEvents se = new ServiceEvents();

    @FXML
    public void initialize() {

        try {
            List<Events> events = se.getAll();

            ObservableList<Events> observableList = FXCollections.observableList(events);
            tableEvent.setItems(observableList);

            title.setCellValueFactory(new PropertyValueFactory<>("title"));
            description.setCellValueFactory(new PropertyValueFactory<>("description"));
            seats.setCellValueFactory(new PropertyValueFactory<>("maxParticipants"));
            startdate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
            enddate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
            type.setCellValueFactory(new PropertyValueFactory<>("type"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
