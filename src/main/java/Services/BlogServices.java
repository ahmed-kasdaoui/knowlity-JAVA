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
        createOrUpdateTable();
    }

    private void createOrUpdateTable() {
        try {
            // Vérifier si la table existe
            DatabaseMetaData metaData = cnx.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "blog", null);
            
            if (!tables.next()) {
                // Créer la table si elle n'existe pas
                String createTable = """
                    CREATE TABLE blog (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        title VARCHAR(255) NOT NULL,
                        content TEXT NOT NULL,
                        creator_name VARCHAR(255) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        user_image VARCHAR(1000),
                        blog_image VARCHAR(1000)
                    )
                """;
                try (Statement stmt = cnx.createStatement()) {
                    stmt.execute(createTable);
                }
            } else {
                // Modifier la table si elle existe
                String alterTable = """
                    ALTER TABLE blog 
                    MODIFY COLUMN user_image VARCHAR(1000),
                    MODIFY COLUMN blog_image VARCHAR(1000)
                """;
                try (Statement stmt = cnx.createStatement()) {
                    stmt.execute(alterTable);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création/modification de la table blog: " + e.getMessage());
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

    public void update(Blog blog) throws SQLException {
        String sql = "UPDATE blog SET title = ?, content = ?, creator_name = ?, updated_at = ?, blog_image = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, blog.getTitle());
            stmt.setString(2, blog.getContent());
            stmt.setString(3, blog.getCreatorName());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(5, blog.getBlogImage());
            stmt.setInt(6, blog.getId());

            stmt.executeUpdate();
        }
    }

    public void deleteById(int blogId) throws SQLException {
        String req = "DELETE FROM blog WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, blogId);
            pst.executeUpdate();
        }
    }

    public void delete(Blog blog) {
        try {
            deleteById(blog.getId());
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

                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    blog.setCreatedAt(createdAt.toLocalDateTime());
                }

                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                    blog.setUpdatedAt(updatedAt.toLocalDateTime());
                }

                // Charger les images
                String userImage = rs.getString("user_image");
                String blogImage = rs.getString("blog_image");
                blog.setUserImage(userImage);
                blog.setBlogImage(blogImage);
                blogs.add(blog);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            throw new RuntimeException("Erreur lors du chargement des blogs: " + ex.getMessage());
        }
        return blogs;
    }

    public Blog getBlogById(int id) throws SQLException {
        String sql = "SELECT * FROM blog WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Blog blog = new Blog(
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("creator_name")
                );
                blog.setId(rs.getInt("id"));

                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    blog.setCreatedAt(createdAt.toLocalDateTime());
                }

                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                    blog.setUpdatedAt(updatedAt.toLocalDateTime());
                }

                // Charger les images
                String userImage = rs.getString("user_image");
                String blogImage = rs.getString("blog_image");
                blog.setUserImage(userImage);
                blog.setBlogImage(blogImage);

                return blog;
            }
            return null;
        }
    }
}
