package tn.knowlity.controller;

import com.example.demo.HelloApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.knowlity.tools.UserSessionManager;

import java.io.IOException;

public class FrontController {
@FXML
ImageView test;

    @FXML
    private TextField email;
    public void pageUpdate() throws IOException {
        Stage currentStage = (Stage) test.getScene().getWindow();
        navbarController.changeScene("/User/editFront.fxml", currentStage);
    }

    public void logout() throws IOException {
        UserSessionManager.getInstance().logout();
        String file = "/User/loginPage.fxml";

        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(file));
        Scene scene = new Scene(fxmlLoader.load(), 1500, 800);

        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();


        Stage currentStage = (Stage) test.getScene().getWindow();
        currentStage.close();

    }
}
