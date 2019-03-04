package remote.server;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersDatabaseHandler extends DatabaseHandler{

    private static boolean haveUser(String name) throws SQLException {
        preparedStatement =
                connection.prepareStatement("select * from Users where name = ?");
        preparedStatement.setString(1, name);

        ResultSet resultSet = preparedStatement.executeQuery();

        return resultSet.next();
    }

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

    static void removeUser(String name) throws SQLException {
        preparedStatement = connection.prepareStatement("delete from Users where name = ?");
        preparedStatement.setString(1,name);
        preparedStatement.executeUpdate();
    }

    static boolean login(String name, String phoneNumber, String password) throws SQLException {
        preparedStatement = connection.prepareStatement("select * from Users where (name = ? or phoneNumber = ?) and password = ?");
        preparedStatement.setString(1,name);
        preparedStatement.setString(2,phoneNumber);
        preparedStatement.setString(3,password);

        ResultSet resultSet = preparedStatement.executeQuery();

        return resultSet.next();
    }

    static String getUserName(String name, String password) throws SQLException {
        preparedStatement = connection.prepareStatement("select * from Users where name = ? and password = ?");
        preparedStatement.setString(1,name);
        preparedStatement.setString(2,password);

        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()) return resultSet.getString(2);

        return null;

    }
}