module com.example.demo{
    // JavaFX Modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;
    requires javafx.base;
    requires javafx.graphics;

    // Standard Java Modules
    requires java.sql;
    requires java.net.http;
    requires jdk.jsobject;

    // JavaMail API

    // Third-Party Libraries
    requires org.controlsfx.controls;
    requires org.fxmisc.richtext;
    requires de.jensd.fx.glyphs.fontawesome;
    requires vosk;
    requires jbcrypt;
    requires com.gluonhq.maps;
    requires com.google.gson;
    requires org.json;
    requires com.fasterxml.jackson.databind;
    requires okhttp3;
    requires org.apache.commons.io;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires com.github.librepdf.openpdf;

    // Google APIs
    requires com.google.api.client;
    requires com.google.api.client.auth;
    requires com.google.api.services.oauth2;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client.json.jackson2;
    requires google.api.client;
    requires jakarta.mail;
    requires java.desktop;
    requires java.prefs;


    // OPEN / EXPORT declarations
    opens com.example.demo to javafx.fxml;
    opens controllers to javafx.fxml;
    opens tn.knowlity.controller to javafx.fxml;
    opens tn.knowlity.entity to javafx.base;
    opens com.esprit.knowlity.Model to javafx.base;
    opens com.esprit.knowlity.view.student to javafx.fxml;
    opens com.esprit.knowlity.view.teacher to javafx.fxml;
    opens com.esprit.knowlity.controller.student to javafx.fxml, javafx.web;
    opens com.esprit.knowlity.controller.teacher to javafx.fxml;
    opens com.esprit.knowlity.controller to javafx.fxml;
    opens com.esprit.knowlity.Utils.Snippet to javafx.fxml;
    opens view to javafx.fxml, javafx.graphics, javafx.base;

    exports com.example.demo;
    exports controllers;
    exports tn.knowlity.controller;
    exports com.esprit.knowlity;
    exports view;
}
