package Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseInitializer {
    
    public static void initializeDatabase() {
        try {
            // Get SQL script content
            InputStream is = DatabaseInitializer.class.getResourceAsStream("/sql/create_tables.sql");
            if (is == null) {
                throw new RuntimeException("Could not find create_tables.sql");
            }
            
            String sqlScript = new BufferedReader(new InputStreamReader(is))
                .lines()
                .collect(Collectors.joining("\n"));
            
            // Execute SQL script
            Connection conn = DataSource.getInstance().getCnx();
            Statement stmt = conn.createStatement();
            stmt.execute(sqlScript);
            
            System.out.println("Database tables initialized successfully!");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database: " + e.getMessage());
        }
    }
}
