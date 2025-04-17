package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tn.esprit.interfaces.IService;
import tn.esprit.models.Events;
import tn.esprit.services.ServiceEvents;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddEvent {

    @FXML
    private TextField LatitudeTF;

    @FXML
    private ChoiceBox<String> category;

    @FXML
    private Button clearbtn;

    @FXML
    private TextArea descriptionTF;

    @FXML
    private DatePicker startdate;

    @FXML
    private DatePicker enddate;

    @FXML
    private TextField imageTF;

    @FXML
    private TextField locationTF;

    @FXML
    private TextField longitudeTF;

    @FXML
    private TextField max_participants;

    @FXML
    private TextField titleTF;

    @FXML
    private ChoiceBox<String> typeTF;

    @FXML
    private void initialize() {
        category.getItems().addAll("workshop","conference", "cultural", "Art", "Sports");
        typeTF.getItems().addAll("On-ligne","On-Site");

        category.setValue("workshop");
        typeTF.setValue("On-site");
    }

    @FXML
    void clear(ActionEvent event) {
        titleTF.clear();
        descriptionTF.clear();
        startdate.setValue(null);
        enddate.setValue(null);
        typeTF.setValue(null);
        max_participants.clear();
        locationTF.clear();
        imageTF.clear();
        LatitudeTF.clear();
        longitudeTF.clear();
        category.setValue(null); // or set default like category.setValue("Technology");
    }

    private final IService<Events> se = new ServiceEvents();
    @FXML
    void save(ActionEvent event) {
        LocalDateTime startDate = startdate.getValue().atTime(LocalTime.now());
        LocalDateTime endDate = enddate.getValue().atTime(LocalTime.now());
        try {
            Events event1 = new Events(titleTF.getText(), descriptionTF.getText(),startDate, endDate, typeTF.getValue(), Integer.parseInt(max_participants.getText()), Integer.parseInt(max_participants.getText()), locationTF.getText(), imageTF.getText(), category.getValue(),Float.parseFloat(longitudeTF.getText()),Float.parseFloat(LatitudeTF.getText()));
            event1.setOrganizerId(1);
            se.add(event1);
            System.out.println("Event saved successfully!");
        } catch (Exception ex) {
            System.err.println("An error occurred while saving the event: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    @FXML
    void display(ActionEvent event) {

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/EventList.fxml"));
            titleTF.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }


}
