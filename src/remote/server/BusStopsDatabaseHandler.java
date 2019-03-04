package remote.server;

import drtalgo.CityFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

class BusStopsDatabaseHandler extends DatabaseHandler {

    static void getBusStops(CityFactory cityFactory) throws SQLException {
        preparedStatement = connection.prepareStatement("select * from BusStops");

        ResultSet resultSet = preparedStatement.executeQuery();

        while(resultSet.next()) {
            cityFactory.addBusStop(resultSet.getString(2));
        }
    }

    static void getDistance(CityFactory cityFactory) throws SQLException {
        preparedStatement = connection.prepareStatement("select * from Paths");

        ResultSet resultSet = preparedStatement.executeQuery();

        while(resultSet.next()) {
            String from;
            String to;

            preparedStatement = connection.prepareStatement("select  * from BusStops where id = ? ");
            preparedStatement.setString(1, resultSet.getString(1));

            ResultSet outRes = preparedStatement.executeQuery();
            outRes.next();
            from = outRes.getString(2);


            preparedStatement = connection.prepareStatement("select  * from BusStops where id = ? ");
            preparedStatement.setString(1, resultSet.getString(2));

            outRes = preparedStatement.executeQuery();
            outRes.next();
            to = outRes.getString(2);

            double distance = Double.valueOf(resultSet.getString(3));

            cityFactory.addRoad(from, to, distance);
        }
    }

    static String getBusStopName(String id) throws SQLException {
        preparedStatement = connection.prepareStatement("select * from BusStops where id = ?");
        preparedStatement.setString(1,id);

        ResultSet resultSet = preparedStatement.executeQuery();

        if(resultSet.next()) return resultSet.getString(2);
        return null;
    }
}
