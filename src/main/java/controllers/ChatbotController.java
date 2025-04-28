package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatbotController {

    @FXML private VBox chatHeader;
    @FXML private ImageView chatbotLogo;
    @FXML private Label logoText;
    @FXML private Button closeChatbot;
    @FXML private ScrollPane chatBody;
    @FXML private VBox messageContainer;
    @FXML private HBox chatFooter;
    @FXML private TextArea messageInput;
    @FXML private Button sendMessage;

    private Stage stage;
    private static final String API_KEY = "AIzaSyCJTX4kLgIWKyNqguBWdcZ20qHg9Uw7wgg";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=" + API_KEY;
    private List<ChatMessage> chatHistory;

    public void initialize() {
        chatHistory = new ArrayList<>();
        chatHistory.add(new ChatMessage("user", "Context: You are an AI assistant for 9arini. This website is a Tunisian online platform specializing in computer science and education. It offers a variety of features, including:" +
                "1. **Courses**: Users can take online courses related to computer science." +
                "2. **Tests and Quizzes**: Interactive assessments to evaluate knowledge and skills." +
                "3. **Certificates**: After completing courses or tests, users can receive official certificates." +
                "4. **Offers**: There are various offers available to users on courses and other services." +
                "5. **Events**: The website also offers different events related to computer science and education." +
                "6. **Forum**: A community forum where users can discuss topics and ask questions." +
                "7. **Complaints**: Users can submit complaints related to their experience on the website." +
                "Your job is to help users navigate these features and provide relevant information. If users ask unrelated questions, you can provide general knowledge, but always prioritize website-related queries."));

        addBotMessage("Hey there\nHow can I help you today?");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void closeChatbot() {
        stage.close();
    }

    @FXML
    private void handleSendMessage() {
        String userMessage = messageInput.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        addUserMessage(userMessage);
        messageInput.clear();

        HBox thinkingMessage = addBotMessage("...");
        thinkingMessage.getStyleClass().add("thinking");

        generateBotResponse(userMessage, thinkingMessage);
    }

    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        messageBox.setSpacing(10);
        messageBox.getStyleClass().add("user-message");

        VBox messageContent = new VBox(2);
        Label messageText = new Label(message);
        messageText.setWrapText(true);
        messageText.setMaxWidth(300);
        messageText.setStyle("-fx-background-color: #5a7ca3; -fx-text-fill: white; -fx-padding: 12 16 12 16; -fx-background-radius: 13 13 3 13;");

        Label timestamp = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timestamp.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");

        messageContent.getChildren().addAll(messageText, timestamp);
        messageBox.getChildren().add(messageContent);
        messageContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private HBox addBotMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        messageBox.setSpacing(10);
        messageBox.getStyleClass().add("bot-message");

        ImageView botAvatar = new ImageView(new Image(getClass().getResourceAsStream("/images/robotic.png")));
        botAvatar.setFitHeight(35);
        botAvatar.setFitWidth(35);

        VBox messageContent = new VBox(2);
        Label messageText = new Label(message);
        messageText.setWrapText(true);
        messageText.setMaxWidth(300);
        messageText.setStyle("-fx-background-color: rgba(0, 0, 0, 0.075); -fx-background-radius: 13 13 13 3; -fx-padding: 12 16 12 16;");

        Label timestamp = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timestamp.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");

        messageContent.getChildren().addAll(messageText, timestamp);
        messageBox.getChildren().addAll(botAvatar, messageContent);
        messageContainer.getChildren().add(messageBox);
        scrollToBottom();
        return messageBox;
    }

    private void scrollToBottom() {
        chatBody.setVvalue(1.0);
    }

    private void generateBotResponse(String userMessage, HBox thinkingMessage) {
        chatHistory.add(new ChatMessage("user", userMessage));

        String requestBody = buildRequestBody();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        String botResponse = extractBotResponse(response);
                        Platform.runLater(() -> {
                            messageContainer.getChildren().remove(thinkingMessage);
                            addBotMessage(botResponse);
                            chatHistory.add(new ChatMessage("model", botResponse));
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            messageContainer.getChildren().remove(thinkingMessage);
                            addBotMessage("Sorry, I'm having trouble responding. Please try again.");
                        });
                    }
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        messageContainer.getChildren().remove(thinkingMessage);
                        addBotMessage("Sorry, I'm having trouble responding. Please try again.");
                    });
                    return null;
                });
    }

    private String buildRequestBody() {
        StringBuilder json = new StringBuilder("{\"contents\": [");
        for (int i = 0; i < chatHistory.size(); i++) {
            ChatMessage msg = chatHistory.get(i);
            json.append("{\"role\": \"").append(msg.getRole()).append("\", \"parts\": [{\"text\": \"")
                    .append(msg.getText().replace("\"", "\\\"")).append("\"}]}");
            if (i < chatHistory.size() - 1) {
                json.append(",");
            }
        }
        json.append("]}");
        return json.toString();
    }

    private String extractBotResponse(String response) {
        int startIndex = response.indexOf("\"text\": \"") + 9;
        int endIndex = response.indexOf("\"", startIndex);
        return response.substring(startIndex, endIndex);
    }

    private static class ChatMessage {
        private String role;
        private String text;

        public ChatMessage(String role, String text) {
            this.role = role;
            this.text = text;
        }

        public String getRole() {
            return role;
        }

        public String getText() {
            return text;
        }
    }
}