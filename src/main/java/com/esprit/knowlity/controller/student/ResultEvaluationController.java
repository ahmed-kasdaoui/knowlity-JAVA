package com.esprit.knowlity.controller.student;

import com.esprit.knowlity.Model.Evaluation;
import com.esprit.knowlity.Model.Question;
import com.esprit.knowlity.Model.Reponse;
import com.esprit.knowlity.Service.EvaluationService;
import com.esprit.knowlity.Service.QuestionService;
import com.esprit.knowlity.Service.ReponseService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import com.esprit.knowlity.Utils.UploadcareUtil;
import tn.esprit.models.Cours;
import tn.esprit.services.ServiceCours;
import tn.knowlity.entity.User;
import tn.knowlity.tools.UserSessionManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ResultEvaluationController implements Initializable {
    @FXML
    private HBox scoreSummaryBox;
    @FXML
    private ImageView scoreIconImageView;
    @FXML
    private Label scoreSummaryLabel;
    private Runnable backCallback;

    @FXML
    private Text titleText;
    @FXML
    private Text descText;
    @FXML
    private VBox resultsContainer;
    @FXML
    private HBox badgeInlineBox;
    @FXML
    private ImageView badgeImage;
    @FXML
    private Label badgeTitle;
    @FXML
    private Button exportPdfButton;
    @FXML
    private Label noResultLabel;
    @FXML
    private ImageView qrCodeImage;

    private Evaluation evaluation;
    private int userId;

    private User user = UserSessionManager.getInstance().getCurrentUser();
    private final int DEFAULT_USER_ID = user.getId();

    private final ReponseService reponseService = new ReponseService();
    private final QuestionService questionService = new QuestionService();

    @FXML
    private Button btnBack;

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
        this.userId = DEFAULT_USER_ID;
        loadResults();
    }

    @FXML
    private void handleBackAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/evaluation_select.fxml"));
            Parent root = loader.load();
            
            // Obtenir le contrôleur et initialiser les données
            EvaluationSelectController controller = loader.getController();
            
            // Récupérer les évaluations pour le cours actuel
            if (evaluation != null) {
                EvaluationService evaluationService = new EvaluationService();
                ServiceCours serviceCours = new ServiceCours();
                
                // Récupérer le cours et les évaluations
                Cours cours = serviceCours.getCoursById(evaluation.getCoursId());
                List<Evaluation> evals = evaluationService.getEvaluationsByCoursId(evaluation.getCoursId());
                
                // Configurer le contrôleur
                controller.setCourse(cours);
                controller.setEvaluations(evals);
                
                // Configurer le bouton retour du EvaluationSelectController
                controller.setOnBack(event -> {
                    try {
                        FXMLLoader studentLoader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/student.fxml"));
                        Parent studentRoot = studentLoader.load();
                        StudentController studentController = studentLoader.getController();
                        studentController.setCourse(cours);
                        btnBack.getScene().setRoot(studentRoot);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showErrorDialog("Erreur de navigation", 
                            "Impossible de retourner à l'écran principal: " + e.getMessage());
                    }
                });
            }
            
            // Changer la scène
            btnBack.getScene().setRoot(root);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorDialog("Erreur de navigation", 
                "Impossible de retourner à la page précédente: " + ex.getMessage());
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (exportPdfButton != null) {
            exportPdfButton.setOnAction(e -> exportPdf());
        }
    }

    private void exportPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(exportPdfButton.getScene().getWindow());
        if (file != null) {
            try {
                List<Question> questions = questionService.getQuestionsByEvaluationId(evaluation.getId());
                List<Reponse> reponses = reponseService.getReponsesByEvaluationIdAndUserId(evaluation.getId(), DEFAULT_USER_ID);
                DateTimeFormatter certFormatter = DateTimeFormatter.ofPattern("yyMMddHHmm");
                String dt = LocalDateTime.now().format(certFormatter);
                String certId = "CER-" + DEFAULT_USER_ID + (evaluation != null ? evaluation.getId() : "0") + dt;

                String uploadedUrl = generateAndUploadPdf(file, questions, reponses, certId);
                if (uploadedUrl != null) {
                    generateAndUploadQRCode(uploadedUrl);
                }

                com.esprit.knowlity.controller.CustomDialogController.showDialog(
                        "Export Successful",
                        "PDF exported successfully!",
                        com.esprit.knowlity.controller.CustomDialogController.DialogType.SUCCESS
                );
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to export PDF: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private String generateAndUploadPdf(File file, List<Question> questions, List<Reponse> reponses, String certId) throws Exception {
        boolean hasBadWord = hasCensoredWord(reponses);
        String uploadedUrl = null;

        if (hasBadWord) {
            generateBadWordPdf(file);
            uploadedUrl = UploadcareUtil.uploadToUploadcare(file, certId + "-avertissement");
        } else if (evaluation.getBadgeThreshold() != null && evaluation.getBadgeImage() != null) {
            generatePdf(file, certId);
            uploadedUrl = UploadcareUtil.uploadToUploadcare(file, certId);
        } else {
            int totalScore = 0, maxScore = 0;
            for (Question q : questions) {
                maxScore += q.getPoint();
            }
            generateCorrectionPdf(file, questions, reponses, totalScore, maxScore);
            uploadedUrl = UploadcareUtil.uploadToUploadcare(file, certId);
        }
        return uploadedUrl;
    }

    private void generateAndUploadQRCode(String url) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 256, 256);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            Image qrImage = new Image(new ByteArrayInputStream(pngOutputStream.toByteArray()));
            qrCodeImage.setImage(qrImage);
            qrCodeImage.setVisible(true);
            qrCodeImage.setManaged(true);
            qrCodeImage.setOnMouseEntered(e -> {
                qrCodeImage.setScaleX(1.12);
                qrCodeImage.setScaleY(1.12);
            });
            qrCodeImage.setOnMouseExited(e -> {
                qrCodeImage.setScaleX(1.0);
                qrCodeImage.setScaleY(1.0);
            });
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("QR Code Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to generate QR code: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void generatePdf(File file, String certId) throws Exception {
        // Student details
        String studentNom = user.getNom();
        String studentPrenom = user.getPrenom();
        Document document = new Document(new Rectangle(800, 600), 36, 36, 36, 36);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        PdfContentByte cb = writer.getDirectContentUnder();
        cb.setColorFill(new java.awt.Color(245, 249, 252));
        cb.rectangle(0, 0, 800, 600);
        cb.fill();
        cb.setLineWidth(5f);
        cb.setColorStroke(new java.awt.Color(67, 206, 162));
        cb.rectangle(10, 10, 780, 580);
        cb.stroke();
        cb.setLineWidth(2f);
        cb.setColorStroke(new java.awt.Color(24, 90, 157));
        cb.rectangle(20, 20, 760, 560);
        cb.stroke();
        cb.saveState();
        cb.beginText();
        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, false), 60);
        cb.setColorFill(new java.awt.Color(200, 220, 240, 40));
        cb.showTextAligned(Element.ALIGN_CENTER, "Knowlity", 400, 300, 45);
        cb.endText();
        cb.restoreState();
        Paragraph certPara = new Paragraph("Certificat de Réussite", new Font(Font.HELVETICA, 32, Font.BOLD, new java.awt.Color(67, 206, 162)));
        certPara.setAlignment(Element.ALIGN_CENTER);
        document.add(certPara);

        // Add extra spacing
        document.add(new Paragraph(" ")); // Empty paragraph for extra vertical space
        document.add(new Paragraph(" ")); // Another empty paragraph

        String userName = studentNom + " " + studentPrenom;
        String evalTitle = evaluation != null ? evaluation.getTitle() : "[Evaluation]";
        Paragraph congrats = new Paragraph("This is to certify that " + userName + "\n\nhas successfully passed the evaluation of " + evalTitle, new Font(Font.HELVETICA, 20, Font.BOLD, new java.awt.Color(24, 90, 157)));
        congrats.setAlignment(Element.ALIGN_CENTER);
        congrats.setSpacingAfter(18);
        document.add(congrats);
        String badgeName = badgeTitle.getText();
        String badgeImg = badgeImage.getImage() != null ? badgeImage.getImage().getUrl() : null;
        Paragraph badgeHeader = new Paragraph("Badge Awarded", new Font(Font.HELVETICA, 19, Font.BOLD, new java.awt.Color(67, 206, 162)));
        badgeHeader.setAlignment(Element.ALIGN_CENTER);
        badgeHeader.setSpacingBefore(8);
        document.add(badgeHeader);
        System.out.println("Badge Image Path: " + badgeImg);
        if (badgeImg != null && !badgeImg.isEmpty()) {
            // Extract filename from the URL or path
            String fileName;
            if (badgeImg.startsWith("file:")) {
                fileName = badgeImg.substring(badgeImg.lastIndexOf('/') + 1);
            } else {
                fileName = badgeImg;
            }
            
            // Create a new image from the badge image path
            File badgeImageFile = new File("C:\\xampp\\htdocs\\knowlity\\" + fileName);
            System.out.println("Badge Image Filename: " + fileName);
            System.out.println("Badge Image File: " + badgeImageFile.getAbsolutePath());
            System.out.println("Badge Image File Exists: " + badgeImageFile.exists());
            
            if (badgeImageFile.exists()) {
                try {
                    com.lowagie.text.Image badgeImgInstance = com.lowagie.text.Image.getInstance(badgeImageFile.getAbsolutePath());
                    badgeImgInstance.setAlignment(Element.ALIGN_CENTER);
                    badgeImgInstance.scaleToFit(80, 80); // Adjust size as needed
                    document.add(badgeImgInstance);
                    System.out.println("Badge Image Added Successfully");
                } catch (Exception e) {
                    System.err.println("Error adding badge image: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Badge image file does not exist");
            }
        } else {
            System.out.println("No badge image path provided");
        }
        Paragraph badgeNamePara = new Paragraph(badgeName, new Font(Font.HELVETICA, 22, Font.BOLD, new java.awt.Color(24, 90, 157)));
        badgeNamePara.setAlignment(Element.ALIGN_CENTER);
        document.add(badgeNamePara);
        Paragraph certified = new Paragraph("\n\nCertified by Knowlity", new Font(Font.HELVETICA, 16, Font.BOLD, new java.awt.Color(24, 90, 157)));
        certified.setAlignment(Element.ALIGN_CENTER);
        document.add(certified);
        Paragraph signature = new Paragraph("\n________________________", new Font(Font.HELVETICA, 14, Font.NORMAL, new java.awt.Color(120, 120, 120)));
        signature.setAlignment(Element.ALIGN_CENTER);
        document.add(signature);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String currentDateTime = LocalDateTime.now().format(formatter);
        Paragraph datePara = new Paragraph("Exported on: " + currentDateTime + "\nCertificate ID: " + certId, new Font(Font.HELVETICA, 12, Font.ITALIC, new java.awt.Color(160, 160, 160)));
        datePara.setAlignment(Element.ALIGN_CENTER);
        document.add(datePara);
        document.close();
    }

    private void generateCorrectionPdf(File file, List<Question> questions, List<Reponse> reponses, int totalScore, int maxScore) throws Exception {
        // Student details
        String studentNom = user.getNom();
        String studentPrenom = user.getPrenom();
        String studentEmail = user.getEmail();
        Document document = new Document(new Rectangle(800, 700), 36, 36, 36, 36);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Header Bar with color and logo
        PdfContentByte cb = writer.getDirectContentUnder();
        float headerBarHeight = 50f;
        cb.setColorFill(new java.awt.Color(67, 206, 162));
        cb.rectangle(0, 700 - headerBarHeight, 800, headerBarHeight);
        cb.fill();
        try {
            java.io.InputStream logoStream = getClass().getResourceAsStream("/images/logo.png");
            if (logoStream != null) {
                com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(org.apache.commons.io.IOUtils.toByteArray(logoStream));
                float logoHeight = 36f;
                float logoWidth = 72f;
                float logoX = (800f - logoWidth) / 2f;
                float logoY = 700f - headerBarHeight + (headerBarHeight - logoHeight) / 2f;
                logo.setAbsolutePosition(logoX, logoY);
                logo.scaleToFit(logoWidth, logoHeight);
                document.add(logo);

            // Knowlity Watermark
            cb.beginText();
            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
            cb.setColorStroke(new java.awt.Color(200, 200, 200, 50)); // Light gray with transparency
            cb.setFontAndSize(baseFont, 80);
            cb.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_STROKE);
            cb.showTextAligned(Element.ALIGN_CENTER, "Knowlity", 400, 300, 45);
            cb.endText();
            }
        } catch (Exception ignored) {
        }

        // Title
        Font titleFont = new Font(Font.HELVETICA, 32, Font.BOLD, new java.awt.Color(78, 67, 118));
        Paragraph title = new Paragraph("Correction de l'évaluation", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(6);
        title.setSpacingBefore(18);
        document.add(title);

        // Evaluation name
        Font subFont = new Font(Font.HELVETICA, 18, Font.BOLD, new java.awt.Color(67, 206, 162));
        Paragraph evalTitle = new Paragraph(evaluation.getTitle(), subFont);
        evalTitle.setAlignment(Element.ALIGN_CENTER);
        evalTitle.setSpacingAfter(2);
        document.add(evalTitle);

        // Student details in a styled card
        PdfPTable studentTable = new PdfPTable(1);
        studentTable.setWidthPercentage(60);
        studentTable.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell studentCell = new PdfPCell();
        studentCell.setBorderColor(new java.awt.Color(67, 206, 162));
        studentCell.setBorderWidth(1f);
        studentCell.setPadding(15);
        studentCell.setBackgroundColor(new java.awt.Color(245, 249, 252));

        // Name and Prenom
        Font nameFont = new Font(Font.HELVETICA, 18, Font.BOLD, new java.awt.Color(24, 90, 157));
        Paragraph namePara = new Paragraph(studentPrenom + " " + studentNom, nameFont);
        namePara.setAlignment(Element.ALIGN_CENTER);
        namePara.setSpacingAfter(8);
        studentCell.addElement(namePara);

        // Email
        Font emailFont = new Font(Font.HELVETICA, 14, Font.ITALIC, new java.awt.Color(120, 120, 120));
        Paragraph emailPara = new Paragraph(studentEmail, emailFont);
        emailPara.setAlignment(Element.ALIGN_CENTER);
        emailPara.setSpacingAfter(8);
        studentCell.addElement(emailPara);

        // Score with trophy icon (moved inside the student card)
        Font scoreFont = new Font(Font.HELVETICA, 16, Font.BOLD, new java.awt.Color(67, 206, 162));
        Paragraph scorePara = new Paragraph("Note: " + totalScore + " / " + maxScore + "  \uD83C\uDFC6", scoreFont);
        scorePara.setAlignment(Element.ALIGN_CENTER);
        scorePara.setSpacingAfter(8);
        studentCell.addElement(scorePara);

        studentTable.addCell(studentCell);
        studentTable.setSpacingBefore(15);
        studentTable.setSpacingAfter(10); // Reduced spacing to minimize the gap

        document.add(studentTable);

        // Questions/Answers as cards
        Font qFont = new Font(Font.HELVETICA, 15, Font.BOLD, new java.awt.Color(78, 67, 118));
        Font aFont = new Font(Font.HELVETICA, 13, Font.NORMAL, java.awt.Color.DARK_GRAY);
        Font nFont = new Font(Font.HELVETICA, 13, Font.BOLD, new java.awt.Color(67, 206, 162));
        Font commentFont = new Font(Font.HELVETICA, 12, Font.ITALIC, new java.awt.Color(255, 81, 47));

        for (Question q : questions) {
            com.lowagie.text.pdf.PdfPTable card = new com.lowagie.text.pdf.PdfPTable(1);
            card.setWidthPercentage(90);
            card.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell();
            cell.setBorderColor(new java.awt.Color(67, 206, 162));
            cell.setBorderWidth(1.5f);
            cell.setPadding(12);
            cell.setBackgroundColor(new java.awt.Color(255, 255, 255, 240));

            Paragraph qPara = new Paragraph("Q: " + q.getTitle(), qFont);
            qPara.setSpacingAfter(2);
            cell.addElement(qPara);

            Paragraph enonce = new Paragraph(q.getEnonce(), new Font(Font.HELVETICA, 12, Font.ITALIC, new java.awt.Color(120, 120, 120)));
            enonce.setSpacingAfter(2);
            cell.addElement(enonce);

            Reponse r = reponses.stream().filter(resp -> resp.getQuestionId() == q.getId()).findFirst().orElse(null);
            if (r != null) {
                String answerText = (r.getText() != null && !r.getText().isEmpty()) ? r.getText() : "[Aucune réponse]";
                Paragraph ansPara = new Paragraph("Réponse: " + answerText, aFont);
                ansPara.setSpacingAfter(2);
                cell.addElement(ansPara);

                String noteText = r.getNote() != null ? ("Note: " + r.getNote() + " / " + q.getPoint()) : "Note: -";
                Paragraph notePara = new Paragraph(noteText, nFont);
                cell.addElement(notePara);

                if (r.getCommentaire() != null && !r.getCommentaire().isEmpty()) {
                    Paragraph commentPara = new Paragraph("Commentaire: " + r.getCommentaire(), commentFont);
                    cell.addElement(commentPara);
                }
            } else {
                Paragraph ansPara = new Paragraph("Réponse: [Aucune réponse]", aFont);
                cell.addElement(ansPara);
                Paragraph notePara = new Paragraph("Note: -", nFont);
                cell.addElement(notePara);
            }
            card.addCell(cell);
            document.add(com.lowagie.text.Chunk.NEWLINE);
            document.add(card);
            document.add(com.lowagie.text.Chunk.NEWLINE);
        }

        // Footer
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String currentDateTime = LocalDateTime.now().format(formatter);
        Font footerFont = new Font(Font.HELVETICA, 12, Font.ITALIC, new java.awt.Color(160, 160, 160));
        Paragraph datePara = new Paragraph("Exporté le: " + currentDateTime, footerFont);
        datePara.setAlignment(Element.ALIGN_CENTER);
        datePara.setSpacingBefore(18);
        document.add(datePara);

        document.close();
    }

    private boolean hasCensoredWord(List<Reponse> reponses) {
        for (Reponse r : reponses) {
            if (r.getText() != null && r.getText().contains("****")) {
                return true;
            }
        }
        return false;
    }

    private void generateBadWordPdf(File file) throws Exception {
        // Student details
        String studentNom = user.getNom();
        String studentPrenom = user.getPrenom();
        Document document = new Document(new Rectangle(800, 700), 36, 36, 36, 36);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Header Bar
        PdfContentByte cb = writer.getDirectContentUnder();
        // Watermark
        cb.saveState();
        BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
        cb.beginText();
        cb.setFontAndSize(baseFont, 60);
        cb.setColorFill(new java.awt.Color(200, 200, 200, 60)); // Light gray, semi-transparent
        cb.showTextAligned(Element.ALIGN_CENTER, "Knowlity", 400, 300, 45);
        cb.endText();
        cb.restoreState();
        float headerBarHeight = 50f;
        cb.setColorFill(new java.awt.Color(255, 81, 47)); // Red bar
        cb.rectangle(0, 700 - headerBarHeight, 800, headerBarHeight);
        cb.fill();

        // --- Image Avertissement  ---
        try {
            java.io.InputStream imgStream = getClass().getResourceAsStream("/images/avertissement.png");
            if (imgStream != null) {
                // iText needs a file or byte array, so copy to temp file
                java.io.File tempImg = java.io.File.createTempFile("avertissement", ".png");
                java.nio.file.Files.copy(imgStream, tempImg.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(tempImg.getAbsolutePath());
                img.scaleToFit(90f, 90f);
                img.setAlignment(com.lowagie.text.Image.ALIGN_CENTER);
                img.setSpacingBefore(18f);
                img.setSpacingAfter(8f);
                document.add(img);
                tempImg.deleteOnExit();
            }
        } catch (Exception ex) {
            // If image not found, skip gracefully
        }

        // --- Main Warning Title ---
        Font titleFont = new Font(Font.HELVETICA, 32, Font.BOLD, new java.awt.Color(255, 81, 47));
        Paragraph title = new Paragraph("Avertissement d'évaluation", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(12);
        title.setSpacingBefore(12);
        document.add(title);

        // --- Student Name ---
        Font studentFont = new Font(Font.HELVETICA, 20, Font.NORMAL, new java.awt.Color(78, 67, 118));
        Paragraph studentName = new Paragraph(studentPrenom + " " + studentNom, studentFont);
        studentName.setAlignment(Element.ALIGN_CENTER);
        studentName.setSpacingAfter(12);
        document.add(studentName);

        // --- Main Message ---
        Font msgFont = new Font(Font.HELVETICA, 20, Font.BOLD, new java.awt.Color(78, 67, 118));
        String evalName = evaluation != null ? evaluation.getTitle() : "[Nom de l'évaluation]";
        Paragraph msg = new Paragraph("Vous avez obtenu 0 à l'évaluation " + evalName + " car vous avez utilisé des mots inappropriés.\nNe recommencez pas.", msgFont);
        msg.setAlignment(Element.ALIGN_CENTER);
        msg.setSpacingBefore(10);
        msg.setSpacingAfter(24);
        document.add(msg);

        // --- Note: 0 ---
        Font noteFont = new Font(Font.HELVETICA, 28, Font.BOLD, new java.awt.Color(255, 81, 47));
        Paragraph note = new Paragraph("Note : 0", noteFont);
        note.setAlignment(Element.ALIGN_CENTER);
        note.setSpacingBefore(10);
        note.setSpacingAfter(18);
        document.add(note);

        // --- Motivational Message ---
        Font motivFont = new Font(Font.HELVETICA, 18, Font.ITALIC, new java.awt.Color(67, 206, 162));
        Paragraph motiv = new Paragraph("L'erreur est humaine. L'important c'est d'apprendre et de s'améliorer.", motivFont);
        motiv.setAlignment(Element.ALIGN_CENTER);
        motiv.setSpacingBefore(10);
        motiv.setSpacingAfter(30);
        document.add(motiv);

        // --- Footer ---
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String currentDateTime = LocalDateTime.now().format(formatter);
        Font footerFont = new Font(Font.HELVETICA, 12, Font.ITALIC, new java.awt.Color(160, 160, 160));
        Paragraph datePara = new Paragraph("Exporté le: " + currentDateTime, footerFont);
        datePara.setAlignment(Element.ALIGN_CENTER);
        datePara.setSpacingBefore(10);
        document.add(datePara);

        document.close();
    }

    private void loadResults() {
        if (scoreSummaryBox != null) {
            scoreSummaryBox.setVisible(false);
            scoreSummaryBox.setManaged(false);
        }
        if (evaluation == null) return;
        titleText.setText(evaluation.getTitle());
        descText.setText(evaluation.getDescription());
        resultsContainer.getChildren().clear();
        List<Question> questions = questionService.getQuestionsByEvaluationId(evaluation.getId());
        List<Reponse> reponses = reponseService.getReponsesByEvaluationIdAndUserId(evaluation.getId(), DEFAULT_USER_ID);
        boolean hasBadWord = hasCensoredWord(reponses);
        if (hasBadWord) {
            // Show warning indicator in resultsContainer
            Label warningLabel = new Label("⚠ Mots inappropriés détectés dans vos réponses. Vous n'êtes pas éligible au certificat.");
            warningLabel.setStyle("-fx-font-size: 16px; -fx-font-family: 'Segoe UI Semibold'; -fx-text-fill: #ff512f; -fx-background-color: #fff4f0; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-font-weight: bold; -fx-alignment: center;");
            resultsContainer.getChildren().add(warningLabel);
        }
        if (reponses == null || reponses.isEmpty()) {
            if (scoreSummaryBox != null) {
                scoreSummaryBox.setVisible(false);
                scoreSummaryBox.setManaged(false);
            }
            noResultLabel.setVisible(true);
            resultsContainer.setVisible(false);
            badgeInlineBox.setVisible(false);
            badgeInlineBox.setManaged(false);
            return;
        }
        noResultLabel.setVisible(false);
        resultsContainer.setVisible(true);
        badgeInlineBox.setVisible(false);
        badgeInlineBox.setManaged(false);
        boolean allCorrected = true;
        int totalScore = 0;
        int maxScore = 0;

        for (Question q : questions) {
            maxScore += q.getPoint();
            Reponse r = reponses.stream().filter(resp -> resp.getQuestionId() == q.getId()).findFirst().orElse(null);
            String borderColor = "#ff512f"; // red by default
            if (r != null && r.getNote() != null) {
                if (r.getNote() >= 0.99 * q.getPoint()) borderColor = "#43cea2"; // green for full
                else borderColor = "#ffa500"; // orange for partial
            }
            HBox card = new HBox(24);
            card.setMinHeight(110);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 18; -fx-padding: 18 24 18 24; " +
                    "-fx-effect: dropshadow(gaussian, #185a9d33, 8, 0.13, 0, 2);" +
                    "-fx-border-width: 0 0 0 7; -fx-border-color: transparent transparent transparent " + borderColor + ";" +
                    "-fx-cursor: hand; -fx-transition: box-shadow 0.3s, border-color 0.3s;");
            card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-effect: dropshadow(gaussian, #185a9d88, 14, 0.18, 0, 4);"));
            card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-effect: dropshadow(gaussian, #185a9d88, 14, 0.18, 0, 4);", "")));

            VBox left = new VBox(6);
            left.setPrefWidth(450);
            Text qTitle = new Text(q.getTitle());
            qTitle.setStyle("-fx-font-size: 18px; -fx-font-family: 'Segoe UI Black'; -fx-fill: #185a9d; -fx-effect: dropshadow(gaussian, #43cea2, 1, 0.12, 0, 1);");
            Text qEnonce = new Text(q.getEnonce());
            qEnonce.setStyle("-fx-font-size: 15px; -fx-fill: #444; -fx-font-family: 'Segoe UI'; -fx-font-style: italic;");
            left.getChildren().addAll(qTitle, qEnonce);

            VBox right = new VBox(10);
            right.setPrefWidth(370);
            right.setStyle("-fx-alignment: CENTER_LEFT;");
            if (r != null) {
                Label answer = new Label((r.getText() != null && !r.getText().isEmpty()) ? r.getText() : "[No Answer]");
                answer.setStyle("-fx-font-size: 16px; -fx-font-family: 'Segoe UI Semibold'; -fx-text-fill: #222; -fx-background-color: #f2f7fa; -fx-background-radius: 8; -fx-padding: 5 10 5 10;");
                HBox noteBox = new HBox(7);
                Label noteIcon = new Label("\uD83D\uDCDD");
                noteIcon.setStyle("-fx-font-size: 17px; -fx-text-fill: #43cea2;");
                Label note = new Label(r.getNote() != null ? ("Note: " + r.getNote() + " / " + q.getPoint()) : "Note: -");
                note.setStyle("-fx-font-size: 15px; -fx-font-family: 'Segoe UI Semibold'; -fx-text-fill: #43cea2;");
                noteBox.getChildren().addAll(noteIcon, note);
                HBox commentBox = new HBox(7);
                if (r.getCommentaire() != null && !r.getCommentaire().isEmpty()) {
                    Label commentIcon = new Label("\uD83D\uDCAC");
                    commentIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: #ff512f;");
                    Label comment = new Label(r.getCommentaire());
                    comment.setStyle("-fx-font-size: 14px; -fx-font-family: 'Segoe UI'; -fx-text-fill: #ff512f; -fx-background-color: #fff4f0; -fx-background-radius: 8; -fx-padding: 4 9 4 9;");
                    commentBox.getChildren().addAll(commentIcon, comment);
                }
                right.getChildren().add(answer);
                right.getChildren().add(noteBox);
                if (!commentBox.getChildren().isEmpty()) right.getChildren().add(commentBox);
                if (r.getNote() != null) totalScore += r.getNote();
                if (r.getNote() == null) allCorrected = false;
            } else {
                Label noResp = new Label("No response.");
                noResp.setStyle("-fx-font-size: 16px; -fx-font-family: 'Segoe UI Semibold'; -fx-text-fill: #ff512f; -fx-background-color: #fff4f0; -fx-background-radius: 8; -fx-padding: 5 10 5 10;");
                right.getChildren().add(noResp);
                allCorrected = false;
            }
            card.getChildren().addAll(left, right);
            resultsContainer.getChildren().add(card);
        }
        // --- Score Summary with Icon ---
        if (scoreSummaryBox != null) {
            scoreSummaryBox.setVisible(true);
            scoreSummaryBox.setManaged(true);
            double percent = maxScore > 0 ? (totalScore * 100.0 / maxScore) : 0;
            String iconPath;
            if (percent >= 90) iconPath = "/images/excellent.png";
            else if (percent >= 75) iconPath = "/images/good.png";
            else if (percent >= 50) iconPath = "/images/average.png";
            else iconPath = "/images/poor.png";
            if (scoreIconImageView != null) {
                scoreIconImageView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream(iconPath)));
            }
            scoreSummaryLabel.setText("Vous avez obtenu " + totalScore + " sur " + maxScore + " points");
        }
        // --- Badge & QR code logic ---
        if (allCorrected && evaluation.getBadgeThreshold() != null && evaluation.getBadgeImage() != null) {
            // Set badge image
            File badgeImageFile = new File("C:\\xampp\\htdocs\\knowlity\\" + evaluation.getBadgeImage());
            if (badgeImageFile.exists()) {
                badgeImage.setImage(new Image(badgeImageFile.toURI().toString()));
                badgeImage.setFitWidth(80);
                badgeImage.setFitHeight(80);
                badgeImage.setPreserveRatio(true);
            }
            
            badgeTitle.setText(evaluation.getBadgeTitle() != null ? evaluation.getBadgeTitle() : "Badge");
            badgeInlineBox.setVisible(true);
            badgeInlineBox.setManaged(true);
            // --- Auto-generate, upload PDF, and show QR code ---
            try {
                DateTimeFormatter certFormatter = DateTimeFormatter.ofPattern("yyMMddHHmm");
                String dt = LocalDateTime.now().format(certFormatter);
                String certId = "CER-" + DEFAULT_USER_ID + (evaluation != null ? evaluation.getId() : "0") + dt;
                // Use a temp file for auto-generation
                File tempFile = File.createTempFile(certId, ".pdf");
                generatePdf(tempFile, certId);
                String fileUrl = UploadcareUtil.uploadToUploadcare(tempFile, certId);
                generateAndUploadQRCode(fileUrl);
                // Add a tooltip or label for the QR code
                qrCodeImage.setVisible(true);
                qrCodeImage.setManaged(true);
                qrCodeImage.setPickOnBounds(true);
                qrCodeImage.setOnMouseEntered(e -> {
                    qrCodeImage.setStyle("-fx-effect: dropshadow(gaussian, #43cea2, 8, 0.18, 0, 2);");
                });
                qrCodeImage.setOnMouseExited(e -> {
                    qrCodeImage.setStyle("");
                });
                qrCodeImage.setOnMouseClicked(e -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("QR Code Info");
                    alert.setHeaderText(null);
                    alert.setContentText("Scan this QR code with your mobile to download your certificate PDF.");
                    alert.showAndWait();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Certificate Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to auto-generate/upload certificate: " + e.getMessage());
                alert.showAndWait();
            }
        } else if (allCorrected) {
            // No badge case: show QR, export, but hide badge image/title
            badgeInlineBox.setVisible(true);
            badgeInlineBox.setManaged(true);
            badgeImage.setVisible(false);
            badgeTitle.setVisible(false);
            try {
                DateTimeFormatter certFormatter = DateTimeFormatter.ofPattern("yyMMddHHmm");
                String dt = LocalDateTime.now().format(certFormatter);
                String certId = "CORR-" + DEFAULT_USER_ID + (evaluation != null ? evaluation.getId() : "0") + dt;
                File tempFile = File.createTempFile(certId, ".pdf");
                generateCorrectionPdf(tempFile, questions, reponses, totalScore, maxScore);
                String fileUrl = UploadcareUtil.uploadToUploadcare(tempFile, certId);
                generateAndUploadQRCode(fileUrl);
                qrCodeImage.setVisible(true);
                qrCodeImage.setManaged(true);
                qrCodeImage.setPickOnBounds(true);
                qrCodeImage.setOnMouseEntered(e -> qrCodeImage.setStyle("-fx-effect: dropshadow(gaussian, #43cea2, 8, 0.18, 0, 2);"));
                qrCodeImage.setOnMouseExited(e -> qrCodeImage.setStyle(""));
                qrCodeImage.setOnMouseClicked(e -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("QR Code Info");
                    alert.setHeaderText(null);
                    alert.setContentText("Scan this QR code with your mobile to download your correction PDF.");
                    alert.showAndWait();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Correction PDF Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to generate/upload correction PDF: " + e.getMessage());
                alert.showAndWait();
            }
        } else {
            badgeInlineBox.setVisible(false);
            badgeInlineBox.setManaged(false);
            qrCodeImage.setVisible(false);
            qrCodeImage.setManaged(false);
        }
    }
}