module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.google.gson;
    requires java.desktop;
    requires jbcrypt;
    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.jackson2;
    requires com.google.api.services.oauth2;
    requires java.sql;
    requires java.mail;

    opens tn.knowlity.entity to javafx.base;
    opens com.example.demo to javafx.fxml;
    opens tn.knowlity.controller to javafx.fxml;
    exports com.example.demo;
}