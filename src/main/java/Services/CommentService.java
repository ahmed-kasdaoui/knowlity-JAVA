package Services;

import Entities.Comment;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentService {
    private Connection cnx;

    public CommentService() {
        cnx = DataSource.getInstance().getCnx();
    }

    public void add(Comment comment) throws SQLException {
        String sql = "INSERT INTO comment (content, username, blog_id, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, comment.getContent());
            stmt.setString(2, comment.getUsername());
            stmt.setInt(3, comment.getBlogId());
            stmt.setTimestamp(4, Timestamp.valueOf(comment.getCreatedAt()));
            stmt.executeUpdate();

            // Récupérer l'ID généré
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                comment.setId(rs.getInt(1));
            }
        }
    }

    public List<Comment> getByBlogId(int blogId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment WHERE blog_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, blogId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("id"));
                comment.setContent(rs.getString("content"));
                comment.setUsername(rs.getString("username"));
                comment.setBlogId(rs.getInt("blog_id"));
                comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                comments.add(comment);
            }
        }
        
        return comments;
    }

    public void delete(int commentId) throws SQLException {
        String sql = "DELETE FROM comment WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, commentId);
            stmt.executeUpdate();
        }
    }
}
