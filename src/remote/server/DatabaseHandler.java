package remote.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract database handler
 * Only connection to storage
 *
 * @author Kalinin Anton
 */
abstract class DatabaseHandler {
    // Connection to database
    static Connection connection;

    // Database statement
    static PreparedStatement preparedStatement;

    /*
     * Static constructor
     */
    static {
        String filename = "/Users/aikalinin/info.txt";
        try {

            List<String> records = new ArrayList<>();

            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null)
            {
                records.add(line);
            }
            reader.close();

            connection = DriverManager.getConnection(records.get(2), records.get(0), records.get(1));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            System.err.format("Exception occurred trying to read '%s'", filename);
        }
    }
}
