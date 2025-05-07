package Services;

import Entities.Rating;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatingService {
    private Connection conn;

    public RatingService() {
        conn = DataSource.getInstance().getCnx();
    }

    public void addRating(Rating rating) {
        // Vérifier d'abord si l'utilisateur a déjà noté ce blog
        if (hasUserRatedBlog(rating.getUserId(), rating.getBlogId())) {
            throw new RuntimeException("Vous avez déjà noté ce blog");
        }

        String query = "INSERT INTO blog_ratings (blog_id, user_id, rating, created_at) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, rating.getBlogId());
            pst.setInt(2, rating.getUserId());
            pst.setInt(3, rating.getRating());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la note : " + e.getMessage());
        }
    }

    public boolean hasUserRatedBlog(int userId, int blogId) {
        String query = "SELECT COUNT(*) as count FROM blog_ratings WHERE user_id = ? AND blog_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.setInt(2, blogId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification de la note : " + e.getMessage());
        }
        return false;
    }

    public double getAverageRating(int blogId) {
        String query = "SELECT COALESCE(AVG(rating), 0) as avg_rating FROM blog_ratings WHERE blog_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, blogId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de la moyenne : " + e.getMessage());
        }
        return 0.0;
    }

    public int getTotalRatings(int blogId) {
        String query = "SELECT COUNT(*) as total FROM blog_ratings WHERE blog_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, blogId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des notes : " + e.getMessage());
        }
        return 0;
    }

    public Map<Integer, Long> getRatingDistribution(int blogId) {
        Map<Integer, Long> distribution = new HashMap<>();
        String query = "SELECT rating, COUNT(*) as count FROM blog_ratings WHERE blog_id = ? GROUP BY rating";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, blogId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                distribution.put(rs.getInt("rating"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de la distribution : " + e.getMessage());
        }
        return distribution;
    }

    public List<Rating> getBlogRatings(int blogId) {
        List<Rating> ratings = new ArrayList<>();
        String query = "SELECT * FROM blog_ratings WHERE blog_id = ? ORDER BY created_at DESC";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, blogId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Rating rating = new Rating();
                rating.setId(rs.getInt("id"));
                rating.setBlogId(rs.getInt("blog_id"));
                rating.setRating(rs.getInt("rating"));
                rating.setCreatedAt(rs.getTimestamp("created_at"));
                ratings.add(rating);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des notes : " + e.getMessage());
        }
        return ratings;
    }

    public boolean hasUserRated(int blogId) {
        String query = "SELECT COUNT(*) as count FROM blog_ratings WHERE blog_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, blogId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification de la note : " + e.getMessage());
        }
        return false;
    }
}
