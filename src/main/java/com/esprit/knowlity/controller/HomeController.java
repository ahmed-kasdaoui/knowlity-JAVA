package com.esprit.knowlity.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

public class HomeController {
    @FXML private Button studentButton;
    @FXML private Button teacherButton;

    @FXML
    public void initialize() {
        addButtonEffects(studentButton);
        addButtonEffects(teacherButton);

        studentButton.setOnAction(e -> openStudentView());
        teacherButton.setOnAction(e -> openTeacherView());
    }

    private void addButtonEffects(Button button) {
        button.setOnMouseEntered(e -> animateButton(button, 1.08));
        button.setOnMouseExited(e -> animateButton(button, 1.0));
    }

    private void animateButton(Button button, double scale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(180), button);
        st.setToX(scale);
        st.setToY(scale);
        st.play();
    }

    private void openStudentView() {
        openView("/com/esprit/knowlity/view/student/student.fxml", "Student Dashboard");
    }

    private void openTeacherView() {
        openView("/com/esprit/knowlity/view/teacher/teacher.fxml", "Teacher Back Office");
    }

    private void openView(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) studentButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
