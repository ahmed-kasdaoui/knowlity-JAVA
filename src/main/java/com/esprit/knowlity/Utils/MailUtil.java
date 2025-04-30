package com.esprit.knowlity.Utils;

import java.util.Properties;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class MailUtil {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String USERNAME = "amennahali8@gmail.com";
    private static final String PASSWORD = "cvag jquw wvfj ptgm";

    public static void sendHtmlMail(String to, String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(htmlContent, "text/html; charset=utf-8");

        Transport.send(message);
    }

    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // HTML template for teacher notification
    public static String getTeacherHtmlEmail(String studentName, String evalName, String questionTitle, String answer) {
        return "<div style='background:#f6f8fb;padding:32px 0;'>"
                + "<div style='max-width:520px;margin:0 auto;background:#fff;border-radius:18px;box-shadow:0 6px 32px rgba(44,62,80,0.09);overflow:hidden;border:1px solid #e3e7ee;'>"
                + "<div style='background:#d32f2f;padding:24px 0 16px 0;text-align:center;'>"
                + "<img src='https://i.imgur.com/9v3K2hD.png' alt='Knowlity' style='width:56px;height:56px;border-radius:12px;box-shadow:0 2px 8px #b71c1c33;margin-bottom:10px;'>"
                + "<div style='font-size:26px;font-weight:700;color:#fff;letter-spacing:1px;'>Inappropriate Answer Alert</div>"
                + "</div>"
                + "<div style='padding:30px 32px 18px 32px;'>"
                + "<div style='font-size:17px;color:#222;font-weight:600;margin-bottom:8px;'>Hello Teacher,</div>"
                + "<div style='font-size:15px;color:#333;margin-bottom:18px;'>A student submitted an answer that was flagged as <span style='color:#d32f2f;font-weight:bold;'>inappropriate</span> and automatically graded <span style='color:#d32f2f;font-weight:bold;'>0</span>.</div>"
                + "<div style='background:#f8d7da;border-left:5px solid #d32f2f;border-radius:8px;padding:13px 18px;margin-bottom:18px;'>"
                + "<div style='margin-bottom:3px;'><b>Student:</b> <span style='color:#1976d2;'>" + escapeHtml(studentName) + "</span></div>"
                + "<div style='margin-bottom:3px;'><b>Evaluation:</b> <span style='color:#1976d2;'>" + escapeHtml(evalName) + "</span></div>"
                + "<div style='margin-bottom:3px;'><b>Question:</b> <span style='color:#1976d2;'>" + escapeHtml(questionTitle) + "</span></div>"
                + "<div style='margin-bottom:3px;'><b>Submitted Answer:</b></div>"
                + "<div style='background:#fff3cd;border-radius:6px;padding:9px 13px 8px 13px;font-family:monospace;color:#b71c1c;font-size:15px;margin-top:3px;'>" + escapeHtml(answer) + "</div>"
                + "</div>"
                + "<div style='color:#444;font-size:14px;margin-bottom:12px;'>Please review this answer and take any necessary action to uphold academic integrity.</div>"
                + "</div>"
                + "<div style='background:#f6f8fb;text-align:center;padding:14px 0 9px 0;color:#888;font-size:13px;border-top:1px solid #e3e7ee;'>"
                + "<span style='font-weight:bold;color:#1976d2;'>Knowlity</span> – Academic Integrity Notification"
                + "</div>"
                + "</div>"
                + "</div>";
    }

    // HTML template for student notification
    public static String getStudentHtmlEmail(String studentName, String evalName, String questionTitle, String answer) {
        return "<div style='background:#f6f8fb;padding:32px 0;'>"
                + "<div style='max-width:520px;margin:0 auto;background:#fff;border-radius:18px;box-shadow:0 6px 32px rgba(44,62,80,0.09);overflow:hidden;border:1px solid #e3e7ee;'>"
                + "<div style='background:#1976d2;padding:24px 0 16px 0;text-align:center;'>"
                + "<img src='https://i.imgur.com/9v3K2hD.png' alt='Knowlity' style='width:56px;height:56px;border-radius:12px;box-shadow:0 2px 8px #1976d233;margin-bottom:10px;'>"
                + "<div style='font-size:26px;font-weight:700;color:#fff;letter-spacing:1px;'>Inappropriate Answer Detected</div>"
                + "</div>"
                + "<div style='padding:30px 32px 18px 32px;'>"
                + "<div style='font-size:17px;color:#222;font-weight:600;margin-bottom:8px;'>Dear " + escapeHtml(studentName) + ",</div>"
                + "<div style='font-size:15px;color:#333;margin-bottom:18px;'>Your answer in evaluation <b>" + escapeHtml(evalName) + "</b> was detected as <span style='color:#d32f2f;font-weight:bold;'>inappropriate</span> and you will receive a <b style='color:#d32f2f;'>0</b> for this question.</div>"
                + "<div style='background:#f8d7da;border-left:5px solid #d32f2f;border-radius:8px;padding:13px 18px;margin-bottom:18px;'>"
                + "<div style='margin-bottom:3px;'><b>Question:</b> <span style='color:#1976d2;'>" + escapeHtml(questionTitle) + "</span></div>"
                + "<div style='margin-bottom:3px;'><b>Your Submitted Answer:</b></div>"
                + "<div style='background:#fff3cd;border-radius:6px;padding:9px 13px 8px 13px;font-family:monospace;color:#b71c1c;font-size:15px;margin-top:3px;'>" + escapeHtml(answer) + "</div>"
                + "</div>"
                + "<div style='background:#e3f2fd;border-left:5px solid #1976d2;border-radius:8px;padding:10px 18px;margin-bottom:18px;color:#1976d2;'>"
                + "Please avoid using inappropriate language in future submissions."
                + "</div>"
                + "</div>"
                + "<div style='background:#f6f8fb;text-align:center;padding:14px 0 9px 0;color:#888;font-size:13px;border-top:1px solid #e3e7ee;'>"
                + "<span style='font-weight:bold;color:#1976d2;'>Knowlity</span> – Academic Integrity Notification"
                + "</div>"
                + "</div>"
                + "</div>";
    }

}
