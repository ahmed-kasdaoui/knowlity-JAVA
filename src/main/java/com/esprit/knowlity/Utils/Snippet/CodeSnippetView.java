package com.esprit.knowlity.Utils.Snippet;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class CodeSnippetView extends VBox {
    private final CodeArea codeArea;

    public CodeSnippetView(String code, String language) {
        // --- HEADER ---
        HBox header = new HBox();
        header.setSpacing(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("code-header");
        // Language badge: Capitalize first letter, fallback to 'Code'
        String langDisplay = (language == null || language.trim().isEmpty()) ? "Code" : language.substring(0, 1).toUpperCase() + language.substring(1).toLowerCase();
        Label langBadge = new Label(langDisplay);
        langBadge.getStyleClass().add("lang-badge");
        // Copy button with icon and tooltip
        Label copyLabel = new Label("Copy");
        copyLabel.setTooltip(new javafx.scene.control.Tooltip("Copy code to clipboard"));
        copyLabel.setStyle("-fx-text-fill: #43cea2; -fx-font-size: 13px; -fx-font-family: 'Segoe UI', 'Arial'; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 16 0 6;");
        javafx.animation.PauseTransition copyResetTransition = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
        copyLabel.setOnMouseClicked(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(code);
            clipboard.setContent(content);
            copyLabel.setText("Copied!");
            copyResetTransition.stop();
            copyResetTransition.playFromStart();
        });
        copyResetTransition.setOnFinished(ev -> copyLabel.setText("Copy"));
        // Remove copy button from header
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(spacer);

        // --- CODE AREA ---

        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setEditable(false);
        codeArea.replaceText(0, 0, code);
        codeArea.setWrapText(true);
        codeArea.getStylesheets().add(getClass().getResource("/com/esprit/knowlity/view/student/codesnippet.css").toExternalForm());
        codeArea.getStyleClass().add("code-area");
        // Minimal syntax highlight for demo (extend for more languages)
        if ("java".equalsIgnoreCase(language)) {
            codeArea.setStyleSpans(0, JavaSyntaxHighlighter.computeHighlighting(code));
        } else if ("python".equalsIgnoreCase(language)) {
            codeArea.setStyleSpans(0, PythonSyntaxHighlighter.computeHighlighting(code));
        } else if ("javascript".equalsIgnoreCase(language)) {
            codeArea.setStyleSpans(0, JavaScriptSyntaxHighlighter.computeHighlighting(code));
        }
        // --- FOOTER ---
        HBox footer = new HBox();
        footer.setSpacing(16);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("code-footer");
        footer.setPadding(new javafx.geometry.Insets(0, 18, 8, 18)); // Ensure always inside card
        // Language icon and name
        Label langFooter = new Label("\uD83D\uDCDD " + langDisplay); // 📝
        langFooter.setStyle("-fx-text-fill: #43cea2; -fx-font-size: 13px; -fx-font-family: 'Segoe UI', 'Arial'; -fx-font-weight: bold; -fx-padding: 0 10 0 0;");
        // Line count icon and value
        int lines = code.split("\n").length;
        Label lineCount = new Label("\u23CE " + lines + " line" + (lines > 1 ? "s" : "")); // ⏎
        lineCount.setStyle("-fx-text-fill: #6ee7c6; -fx-font-size: 12px; -fx-font-family: 'Segoe UI', 'Arial'; -fx-padding: 0 8 0 0;");
        // Char count icon and value
        Label charCount = new Label("\u25A1 " + code.length() + " char" + (code.length() != 1 ? "s" : "")); // ◡
        charCount.setStyle("-fx-text-fill: #6ee7c6; -fx-font-size: 12px; -fx-font-family: 'Segoe UI', 'Arial';");
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        footer.getChildren().addAll(copyLabel, footerSpacer, langFooter, lineCount, charCount);

        VBox card = new VBox();
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #23272e 90%, #313d4e 100%); -fx-background-radius: 20; -fx-border-radius: 20; -fx-border-width: 2.5; -fx-border-color: #43cea2; -fx-padding: 0; -fx-spacing: 0;");
        card.setSpacing(0);
        card.getChildren().addAll(header, codeArea, footer);
        this.getChildren().add(card);
        this.setStyle("-fx-background-color: transparent; -fx-padding: 20 0 20 0;");
        this.setEffect(new javafx.scene.effect.DropShadow(24, javafx.scene.paint.Color.web("#23272e", 0.16)));
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(520), card);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

}