package com.esprit.knowlity.controller.student;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.Node;

import java.io.IOException;
import tn.esprit.models.Cours;

public class EvaluationAwaitCorrectionController {
    @FXML
    public Button backToCoursesButton;

    private Cours course; // Store the course for back navigation

    @FXML
    private void initialize() {
        backToCoursesButton.setOnAction(this::goBackToEvaluationSelection);
    }

    public void setCourse(Cours course) {
        this.course = course;
    }

    @FXML
    private void goBackToEvaluationSelection(ActionEvent event) {
        try {
            // Verify the resource URL
            java.net.URL fxmlUrl = getClass().getResource("/com/esprit/knowlity/view/student/evaluation_select.fxml");
            if (fxmlUrl == null) {
                System.err.println("FXML resource not found: /com/esprit/knowlity/view/student/evaluation_select.fxml");
                return;
            }

            // Load FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent evalRoot = loader.load();

            // Get controller and set course
            EvaluationSelectController evalCtrl = loader.getController();
            evalCtrl.setCourse(course);
            evalCtrl.setEvaluations(new com.esprit.knowlity.Service.EvaluationService().getEvaluationsByCoursId(course.getId()));
            
            // Get the current scene and set the new root
            Node sourceNode = (Node) event.getSource();
            if (sourceNode != null && sourceNode.getScene() != null) {
                sourceNode.getScene().setRoot(evalRoot);
            } else {
                System.err.println("Unable to set scene root: source node or scene is null");
            }
        } catch (IOException e) {
            System.err.println("Failed to load EvaluationSelect.fxml: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error in goBackToEvaluationSelection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
