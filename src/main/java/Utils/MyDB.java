package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDB {
    String url = "jdbc:mysql://localhost:3306/knowlity";
    String user = "root";
    String password = "";
    private Connection conn;
    private static MyDB instance;

    private MyDB() {
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public static MyDB getInstance() {
        if (instance == null) {
            instance = new MyDB();
        }
return instance;
    }
}
