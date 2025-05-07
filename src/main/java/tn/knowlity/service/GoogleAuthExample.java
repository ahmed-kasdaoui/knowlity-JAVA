package tn.knowlity.service;


import com.example.demo.HelloApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.json.JSONObject;
import tn.knowlity.entity.User;
import tn.knowlity.tools.UserSessionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.function.Consumer;

public class GoogleAuthExample extends Application {
    // OAuth 2.0 Configuration
    private static final Dotenv dotenv = Dotenv.load();
    private static final String CLIENT_ID = dotenv.get("GOOGLE_CLIENT_ID");
    private static final String CLIENT_SECRET = dotenv.get("GOOGLE_CLIENT_SECRET");
    private static final String REDIRECT_URI = dotenv.get("GOOGLE_REDIRECT_URI");
    private static final String GOOGLE_AUTH_URL = String.format(
            "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=openid%%20profile%%20email",
            CLIENT_ID, REDIRECT_URI
    );
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";

    private Label statusLabel;
    private Label userInfoLabel;
    private tn.knowlity.service.userService userService = new userService();







    private boolean isAuthenticated = false; // ajouter en haut de ta classe

    // test() ne retourne plus rien
    public void test(Stage primaryStage, Consumer<Boolean> callback) {
        primaryStage.setTitle("Google Authentication");

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ffffff;");
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("Google Authentication");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(GOOGLE_AUTH_URL);

        statusLabel = new Label("Not Connected");
        statusLabel.setStyle("-fx-text-fill: #ffffff;");

        userInfoLabel = new Label("No User Data");
        userInfoLabel.setStyle("-fx-text-fill: #ffffff;");

        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.contains("code=")) {
                String authCode = extractAuthCode(newValue);
                if (authCode != null) {
                    Platform.runLater(() -> {
                        try {
                            String accessToken = exchangeCodeForToken(authCode);
                            UserInfo userInfo = fetchUserInfo(accessToken);
                            User testuser= userService.recherparemail(userInfo.email);

                            if(testuser!=null ) {
                                callback.accept(true);
                                primaryStage.close();
                                User user = userService.recherparemail(userInfo.email);
                                UserSessionManager.getInstance().setCurrentUser(user);
                            }else{
                            updateUserInterface(userInfo);
                     String[] roles =  new String[]{"Etudiant"};
                            User user = new User(userInfo.familyName, userInfo.email,new Date(), 24705158,userInfo.id, "C:\\Users\\21694\\Downloads\\user.jpg","homme","tunis", userInfo.id, 0,"math",roles, userInfo.name);

                            userService.ajouterEtudiant(user);
                            System.out.println(user);
                            System.out.println("*************************************");
                                User user11 = userService.recherparemail(userInfo.email);
                                UserSessionManager.getInstance().setCurrentUser(user11);

                            callback.accept(true); // ➔ succès
                            primaryStage.close();
                        }} catch (Exception e) {
                            statusLabel.setText("Authentication Failed: " + e.getMessage());
                            callback.accept(false); // ➔ échec
                        }
                    });
                }
            }
        });

        root.getChildren().addAll(
                titleLabel,
                webView,
                statusLabel,
                userInfoLabel
        );

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }















    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Google Authentication");

        // Layout
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black;");
        root.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Google Authentication");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // WebView for authentication
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(GOOGLE_AUTH_URL);

        // Status and User Info Labels
        statusLabel = new Label("Not Connected");
        statusLabel.setStyle("-fx-text-fill: red;");

        userInfoLabel = new Label("No User Data");
        userInfoLabel.setStyle("-fx-text-fill: blue;");

        // Login Button


        // Track URL changes to capture authorization code
        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.contains("code=")) {
                String authCode = extractAuthCode(newValue);
                if (authCode != null) {
                    Platform.runLater(() -> {
                        try {
                            String accessToken = exchangeCodeForToken(authCode);
                            UserInfo userInfo = fetchUserInfo(accessToken);
                            System.out.println(userInfo);
                            updateUserInterface(userInfo);
                            System.out.println("ntastiwfil yser");

                            Stage stage = new Stage();
                            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("userteslogin.fxml"));
                            Scene scene = new Scene(fxmlLoader.load(), 1000, 800);
                            stage.setTitle("Hello!");
                            stage.setScene(scene);


                            stage.show();
                            primaryStage.close();
                        } catch (Exception e) {
                            statusLabel.setText("Authentication Failed: " + e.getMessage());
                        }
                    });
                }
            }
        });

        // Add components to layout
        root.getChildren().addAll(
                titleLabel,
                webView,

                statusLabel,
                userInfoLabel
        );

        // Scene configuration





        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // User data model
     private static class UserInfo {
        String id;
        String email;
        String name;
        String givenName;
        String familyName;
        String picture;
        String phoneNumber ;

        public String  getname(){
            return familyName;
        }
        public String  getprenom(){
            return name;
        }
        public String  getid(){
            return id;
        }
        public String  getemail(){
            return email;
        }
        public String  getpicture(){
            return picture;
        }




        @Override
        public String toString() {
            return String.format(
                    "User ID: %s\nEmail: %s\nName: %s\nGiven Name: %s\nFamily Name: %s \nPicture: %s\nPhone number: %s ",
                    id, email, name, givenName, familyName , picture, phoneNumber
            );
        }
    }

    // Extract authorization code from redirect URL
    private String extractAuthCode(String url) {
        String[] params = url.split("[?&]");
        for (String param : params) {
            if (param.startsWith("code=")) {
                return param.substring(5);
            }
        }
        return null;
    }

    // Exchange authorization code for access token
    private String exchangeCodeForToken(String authCode) throws IOException {
        URL url = new URL(TOKEN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        String postData = String.format(
                "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                authCode, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI
        );

        conn.getOutputStream().write(postData.getBytes());

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getString("access_token");
    }

    // Fetch user information using access token
    private UserInfo fetchUserInfo(String accessToken) throws IOException {
        URL url = new URL(USER_INFO_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        UserInfo userInfo = new UserInfo();
        userInfo.id = jsonResponse.optString("sub", "");
        userInfo.email = jsonResponse.optString("email", "");
        userInfo.name = jsonResponse.optString("name", "");
        userInfo.givenName = jsonResponse.optString("given_name", "");
        userInfo.familyName = jsonResponse.optString("family_name", "");
        userInfo.picture = jsonResponse.optString("picture", "");
        userInfo.phoneNumber = jsonResponse.optString("phone_number", "");


        return userInfo;
    }

    // Update UI with user information
    private void updateUserInterface(UserInfo userInfo) {
        statusLabel.setText("Connected");
        statusLabel.setStyle("-fx-text-fill: green;");
        userInfoLabel.setText(userInfo.toString());
    }

    public static void main(String[] args) {
        launch(args);
    }
}