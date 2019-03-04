package remote.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

abstract class DatabaseHandler {
    private static final String name = "root";
    private static final String password = "135-toha-0135";
    private static final String url = "jdbc:mysql://localhost:3306/usersInfo?useSSL=false";
    static Connection connection;
    static PreparedStatement preparedStatement;

    static {
        try {
            connection = DriverManager.getConnection(url, name, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
