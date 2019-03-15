package remote.server;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Special handler of users in the database
 *
 * @author Kalinin Anton
 */

public class UsersDatabaseHandler extends DatabaseHandler{

    /**
     * Checks the database for the existence of such a user
     * @param name user name
     * @return Is there such a user
     * @throws SQLException
     */
    private static boolean haveUser(String name) throws SQLException {
        preparedStatement =
                connection.prepareStatement("select * from Users where name = ?");
        preparedStatement.setString(1, name);

        ResultSet resultSet = preparedStatement.executeQuery();

        return resultSet.next();
    }

    /**
     * Add new user to database
     * @param name user name
     * @param password hashed user password
     * @param fullname user full name
     * @param phoneNumber user phone number
     * @param status user status (driver or passenger)
     * @return Whether adding was successful
     * @throws SQLException
     */
    static boolean addUser(String name, String password, String fullname, String phoneNumber, String status)
            throws SQLException {
        if(!UsersDatabaseHandler.haveUser(name)) {
            preparedStatement = connection.prepareStatement(
                    "insert into Users (name, password, fullname, phoneNumber, status) values (?, ?, ?, ?, ?)");
            fullname = fullname.replace('&', ' ');
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, fullname);
            preparedStatement.setString(4, phoneNumber);
            preparedStatement.setString(5, status);
            preparedStatement.executeUpdate();
            return true;
        }
        return false;
    }

    /**
     * Remove user form database
     * @param name username
     * @throws SQLException
     */
    static void removeUser(String name) throws SQLException {
        preparedStatement = connection.prepareStatement("delete from Users where name = ?");
        preparedStatement.setString(1,name);
        preparedStatement.executeUpdate();
    }

    /**
     * Login to user account
     * @param name username
     * @param phoneNumber user phone number
     * @param password hashed user password
     * @return Was the entrance successful
     * @throws SQLException
     */
    static boolean login(String name, String phoneNumber, String password) throws SQLException {
        preparedStatement = connection.prepareStatement("select * from Users where (name = ? or phoneNumber = ?) and password = ?");
        preparedStatement.setString(1,name);
        preparedStatement.setString(2,phoneNumber);
        preparedStatement.setString(3,password);

        ResultSet resultSet = preparedStatement.executeQuery();

        return resultSet.next();
    }

    /**
     * Get user name from database
     * @param name user name
     * @param password hashed user password
     * @return user name from database
     * @throws SQLException
     */
    static String getUserName(String name, String password) throws SQLException {
        preparedStatement = connection.prepareStatement("select * from Users where name = ? and password = ?");
        preparedStatement.setString(1,name);
        preparedStatement.setString(2,password);

        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()) return resultSet.getString(2);

        return null;

    }
}