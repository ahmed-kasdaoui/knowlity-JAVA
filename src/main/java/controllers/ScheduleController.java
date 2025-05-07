package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import tn.esprit.models.Cours;
import tn.esprit.models.Chapitre;
import tn.esprit.services.ServiceInscription;
import tn.knowlity.tools.UserSessionManager;
import tn.knowlity.entity.User;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class ScheduleController {

    private static final String API_URL = "";
    private static final String BEARER_TOKEN = "";
    private final User user = UserSessionManager.getInstance().getCurrentUser();

    @FXML
    private VBox root;

    @FXML
    private Label titleLabel;

    @FXML
    private VBox scheduleContainer;

    @FXML
    private void initialize() {
        // Update title with username
        if (user != null) {
            titleLabel.setText("Monthly Course Schedule for " + user.getNom());
        } else {
            titleLabel.setText("Monthly Course Schedule");
        }

        // Fetch and display schedule
        String scheduleData = fetchScheduleFromAPI();
        displaySchedule(scheduleData);
    }

    private void displaySchedule(String scheduleData) {
        scheduleContainer.getChildren().clear();
        
        // Nettoyer les données reçues
        scheduleData = scheduleData.trim();
        
        // Supprimer les tirets et les lignes vides au début
        scheduleData = scheduleData.replaceAll("^---\\s*", "");
        
        // Diviser par semaines
        String[] weeks = scheduleData.split("(?=Week \\d+:)");
        
        for (String weekData : weeks) {
            weekData = weekData.trim();
            if (!weekData.isEmpty() && weekData.startsWith("Week")) {
                VBox weekCard = createWeekCard(weekData);
                scheduleContainer.getChildren().add(weekCard);
                
                // Ajouter un espacement entre les cartes
                Region spacer = new Region();
                spacer.setPrefHeight(20);
                scheduleContainer.getChildren().add(spacer);
            }
        }
    }

    private VBox createWeekCard(String weekContent) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");
        card.setSpacing(15);
        
        // Ajouter l'effet d'ombre
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        shadow.setSpread(0.1);
        card.setEffect(shadow);

        // En-tête de la semaine
        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setSpacing(10);

        // Extraire le numéro de la semaine
        Pattern weekPattern = Pattern.compile("Week (\\d+):");
        Matcher weekMatcher = weekPattern.matcher(weekContent);

        if (weekMatcher.find()) {
            Label weekLabel = new Label("Semaine " + weekMatcher.group(1));
            weekLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");
            header.getChildren().add(weekLabel);
        }

        // Extraire le total des heures
        Pattern totalPattern = Pattern.compile("Total hours: ([\\d.]+)h");
        Matcher totalMatcher = totalPattern.matcher(weekContent);
        
        if (totalMatcher.find()) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            Label totalLabel = new Label(String.format("Total: %.1f heures", 
                Double.parseDouble(totalMatcher.group(1).replace(",", "."))));
            totalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
            
            header.getChildren().addAll(spacer, totalLabel);
        }

        card.getChildren().add(header);

        // Liste des cours
        VBox courseList = new VBox();
        courseList.setSpacing(8);

        // Extraire les cours et leurs heures
        Pattern coursePattern = Pattern.compile("([^:\\n]+):\\s*([\\d.,]+)h");
        Matcher courseMatcher = coursePattern.matcher(weekContent);

        while (courseMatcher.find()) {
            String courseName = courseMatcher.group(1).trim();
            String hours = courseMatcher.group(2).replace(",", ".");

            HBox courseItem = new HBox();
            courseItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            courseItem.setSpacing(10);
            courseItem.setStyle("-fx-padding: 12; -fx-background-color: #f1f5f9; -fx-background-radius: 8;");

            Label courseLabel = new Label("• " + courseName);
            courseLabel.setStyle("-fx-text-fill: #1e293b;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Label hoursLabel = new Label(hours + " heures");
            hoursLabel.setStyle("-fx-text-fill: #64748b;");

            courseItem.getChildren().addAll(courseLabel, spacer, hoursLabel);
            courseList.getChildren().add(courseItem);
        }

        card.getChildren().add(courseList);
        return card;
    }

    // Method to fetch schedule from API
    private String fetchScheduleFromAPI() {
        if (user == null) {
            return "Error: No user is currently logged in.";
        }

        try {
            // Fetch enrolled courses
            ServiceInscription serviceInscription = new ServiceInscription();
            List<Cours> enrolledCourses = serviceInscription.getCoursInscrits(user.getId());
            if (enrolledCourses == null || enrolledCourses.isEmpty()) {
                return "No enrolled courses found for the user.";
            }

            // Build course list for API payload
            StringBuilder courseList = new StringBuilder();
            for (Cours course : enrolledCourses) {
                int totalDuration = course.getChapitres().stream()
                        .mapToInt(Chapitre::getDureeEstimee)
                        .sum();
                double durationInHours = totalDuration / 60.0;
                if (Double.isNaN(durationInHours) || Double.isInfinite(durationInHours) || durationInHours <= 0) {
                    continue;
                }
                String cleanTitle = course.getTitle() != null ?
                        course.getTitle()
                                .replace("\n", " ")
                                .replace("\"", "'")
                                .replace("\\", "")
                                .replace("\r", "")
                                .replace("\t", " ") : "Untitled Course";
                courseList.append(String.format("%s %.1fh\n", cleanTitle, durationInHours));
            }

            if (courseList.length() == 0) {
                return "Error: No valid courses with positive duration found.";
            }

            // Build JSON payload with improved prompt for creative scheduling
            JSONObject payload = new JSONObject();
            String inputs = String.format(
                    "You are an innovative educational scheduling assistant tasked with creating an engaging and effective learning plan.\n\n" +
                    "Create a dynamic 4-week schedule for the following courses that optimizes learning effectiveness. Consider these principles:\n" +
                    "1. Progressive Learning: Start with lighter hours and gradually increase intensity\n" +
                    "2. Focus Periods: Allocate more hours to complex subjects when student attention is typically higher\n" +
                    "3. Learning Peaks: Include intensive learning periods balanced with lighter sessions\n" +
                    "4. Practical Distribution: Vary daily hours based on typical student energy levels\n\n" +
                    "For each week:\n" +
                    "- Distribute hours creatively but ensure the total course hours are completed by the end\n" +
                    "- Consider course complexity and optimal learning times\n" +
                    "- Create a rhythm that maintains student engagement\n\n" +
                    "Course List with Total Hours:\n%s\n" +
                    "Format each week as:\n" +
                    "Week X:\n" +
                    "[Course]: [Hours]h\n" +
                    "Total hours: [Sum]h",
                    courseList.toString()
            );
            payload.put("inputs", inputs);
            JSONObject parameters = new JSONObject();
            parameters.put("max_new_tokens", 800);
            parameters.put("temperature", 0.7); // Increased temperature for more creativity
            parameters.put("return_full_text", false);
            payload.put("parameters", parameters);

            String jsonPayload = payload.toString();

            // Log the payload for debugging
            System.out.println("API Payload: " + jsonPayload);

            // Validate JSON
            if (!isValidJson(jsonPayload)) {
                return "Error: Invalid JSON payload.";
            }

            // Create HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // Build HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + BEARER_TOKEN)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            // Send request and get response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Log response for debugging
            System.out.println("HTTP Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            // Check if response is successful
            if (response.statusCode() == 200) {
                return extractGeneratedText(response.body());
            } else {
                return "Error: Failed to fetch schedule (HTTP " + response.statusCode() + ")\nResponse: " + response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Unable to fetch schedule - " + e.getMessage();
        }
    }

    // Method to extract generated_text from JSON response
    private String extractGeneratedText(String jsonResponse) {
        try {
            String generatedText = "";
            
            // Check if response is a JSON array
            if (jsonResponse.trim().startsWith("[")) {
                JSONArray jsonArray = new JSONArray(jsonResponse);
                if (jsonArray.length() > 0) {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    if (jsonObject.has("generated_text")) {
                        generatedText = jsonObject.getString("generated_text");
                    }
                }
            } else {
                // Try parsing as a single JSON object
                JSONObject jsonObject = new JSONObject(jsonResponse);
                if (jsonObject.has("generated_text")) {
                    generatedText = jsonObject.getString("generated_text");
                }
            }
            
            if (generatedText.isEmpty()) {
                return "Error: No generated text found in response";
            }

            // Clean up the generated text
            generatedText = generatedText
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .trim();
            
            System.out.println("Extracted text: " + generatedText); // Debug log
            return generatedText;
            
        } catch (JSONException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            return "Error parsing API response: " + e.getMessage();
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            return "Error parsing API response: " + e.getMessage();
        }
    }

    // Basic JSON validation
    private boolean isValidJson(String json) {
        try {
            new JSONObject(json);
            return true;
        } catch (JSONException e) {
            System.err.println("JSON Validation Error: " + e.getMessage());
            return false;
        }
    }
}
