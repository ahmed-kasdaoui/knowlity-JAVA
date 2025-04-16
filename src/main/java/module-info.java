module com.esprit.knowlity {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.esprit.knowlity.Model to javafx.base;

    opens com.esprit.knowlity.view.student to javafx.fxml;
    opens com.esprit.knowlity.view.teacher to javafx.fxml;

    opens com.esprit.knowlity.controller.student to javafx.fxml;
    opens com.esprit.knowlity.controller.teacher to javafx.fxml;

    exports com.esprit.knowlity;
    opens com.esprit.knowlity.controller to javafx.fxml;
}