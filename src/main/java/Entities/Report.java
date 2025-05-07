package Entities;

import java.sql.Timestamp;

public class Report {
    private int id;
    private int blogId;
    private int userId;
    private String reason;
    private Timestamp reportDate;
    private String status; // "PENDING", "REVIEWED", "DISMISSED"

    public Report() {
    }

    public Report(int blogId, int userId, String reason) {
        this.blogId = blogId;
        this.userId = userId;
        this.reason = reason;
        this.status = "PENDING";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBlogId() {
        return blogId;
    }

    public void setBlogId(int blogId) {
        this.blogId = blogId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getReportDate() {
        return reportDate;
    }

    public void setReportDate(Timestamp reportDate) {
        this.reportDate = reportDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
