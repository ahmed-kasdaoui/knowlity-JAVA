package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private static DataSource instance;
    private Connection cnx;
    
    private final String URL = "jdbc:mysql://localhost:3306/knowlity";
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    
    private DataSource() {
        try {
            // Load MySQL driver explicitly
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Try to connect
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connected to database successfully!");
        } catch (SQLException ex) {
            System.err.println("SQL Error: " + ex.getMessage());
            throw new RuntimeException("Erreur de connexion à la base de données: " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.err.println("Driver Error: " + ex.getMessage());
            throw new RuntimeException("MySQL driver non trouvé: " + ex.getMessage());
        }
    }
    
    public static DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }
    
    public Connection getCnx() {
        try {
            // Check if connection is still valid
            if (cnx == null || !cnx.isValid(1)) {
                // Try to reconnect
                cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException ex) {
            System.err.println("Connection Error: " + ex.getMessage());
            throw new RuntimeException("Erreur de connexion à la base de données: " + ex.getMessage());
        }
        return cnx;
    }
}
