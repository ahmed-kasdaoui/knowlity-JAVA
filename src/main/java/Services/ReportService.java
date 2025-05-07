package Services;

import Entities.Report;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportService {
    private Connection conn;

    public ReportService() {
        conn = DataSource.getInstance().getCnx();
    }

    public int countUniqueReports(int blogId) throws SQLException {
        String query = "SELECT COUNT(DISTINCT user_id) FROM reports WHERE blog_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, blogId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public void addReport(Report report) throws SQLException {
        // Vérifier d'abord si l'utilisateur n'a pas déjà signalé ce blog
        if (!hasUserReportedBlog(report.getUserId(), report.getBlogId())) {
            String query = "INSERT INTO reports (blog_id, user_id, reason, report_date, status) VALUES (?, ?, ?, NOW(), ?)";
            try (PreparedStatement pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pst.setInt(1, report.getBlogId());
                pst.setInt(2, report.getUserId());
                pst.setString(3, report.getReason());
                pst.setString(4, report.getStatus());
                pst.executeUpdate();

                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    report.setId(rs.getInt(1));
                }

                // Vérifier le nombre de signalements uniques
                int uniqueReports = countUniqueReports(report.getBlogId());
                if (uniqueReports >= 3) {
                    // Supprimer le blog
                    BlogServices blogServices = new BlogServices();
                    blogServices.deleteById(report.getBlogId());
                }
            }
        }
    }

    public boolean hasUserReportedBlog(int userId, int blogId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reports WHERE user_id = ? AND blog_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.setInt(2, blogId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public List<Report> getReportsByBlog(int blogId) throws SQLException {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM reports WHERE blog_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, blogId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Report report = new Report();
                report.setId(rs.getInt("id"));
                report.setBlogId(rs.getInt("blog_id"));
                report.setUserId(rs.getInt("user_id"));
                report.setReason(rs.getString("reason"));
                report.setReportDate(rs.getTimestamp("report_date"));
                report.setStatus(rs.getString("status"));
                reports.add(report);
            }
        }
        return reports;
    }

    public void updateReportStatus(int reportId, String status) throws SQLException {
        String query = "UPDATE reports SET status = ? WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, status);
            pst.setInt(2, reportId);
            pst.executeUpdate();
        }
    }
}
