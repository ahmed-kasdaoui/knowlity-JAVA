package Services;

import Entities.Blog;
import Utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BlogServices {
    private Connection cnx;

    public BlogServices() {
        cnx = DataSource.getInstance().getCnx();
        if (cnx == null) {
            throw new RuntimeException("Impossible de se connecter à la base de données");
        }
    }

    public void add(Blog blog) {
        try {
            String req = "INSERT INTO blog (title, content, creator_name, created_at, updated_at, user_image, blog_image) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setString(1, blog.getTitle());
            pst.setString(2, blog.getContent());
            pst.setString(3, blog.getCreatorName());
            pst.setTimestamp(4, Timestamp.valueOf(blog.getCreatedAt()));
            pst.setTimestamp(5, Timestamp.valueOf(blog.getUpdatedAt()));
            pst.setString(6, blog.getUserImage());
            pst.setString(7, blog.getBlogImage());
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            throw new RuntimeException("Erreur lors de l'ajout du blog: " + ex.getMessage());
        }
    }

    public void update(Blog blog) {
        try {
            String req = "UPDATE blog SET title=?, content=?, creator_name=?, updated_at=?, user_image=?, blog_image=? WHERE id=?";
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setString(1, blog.getTitle());
            pst.setString(2, blog.getContent());
            pst.setString(3, blog.getCreatorName());
            pst.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now())); // Update the updated_at timestamp
            pst.setString(5, blog.getUserImage());
            pst.setString(6, blog.getBlogImage());
            pst.setInt(7, blog.getId());
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour du blog: " + ex.getMessage());
        }
    }

    public void delete(Blog blog) {
        try {
            String req = "DELETE FROM blog WHERE id = ?";
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, blog.getId());
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            throw new RuntimeException("Erreur lors de la suppression du blog: " + ex.getMessage());
        }
    }

    public List<Blog> getAll() {
        List<Blog> blogs = new ArrayList<>();
        try {
            String req = "SELECT * FROM blog ORDER BY created_at DESC";
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Blog blog = new Blog(
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("creator_name")
                );
                blog.setId(rs.getInt("id"));
                
                // Handle potentially null timestamps
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    blog.setCreatedAt(createdAt.toLocalDateTime());
                }
                
                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                    blog.setUpdatedAt(updatedAt.toLocalDateTime());
                }
                
                blog.setUserImage(rs.getString("user_image"));
                blog.setBlogImage(rs.getString("blog_image"));
                blogs.add(blog);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            throw new RuntimeException("Erreur lors du chargement des blogs: " + ex.getMessage());
        }
        return blogs;
    }
}
