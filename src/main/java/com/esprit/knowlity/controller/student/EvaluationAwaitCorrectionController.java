package com.esprit.knowlity.controller.student;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.stage.Stage;
import javafx.scene.Node;

public class EvaluationAwaitCorrectionController {
    @FXML
    public Button backToCoursesButton;

    @FXML
    private void initialize() {
    }

    @FXML
    private void handleBackToCourses(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
