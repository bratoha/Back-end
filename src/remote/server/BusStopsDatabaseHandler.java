package remote.server;

import drtalgo.CityFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handler of the city stops and information about which is in the databases
 *
 * @author Kalinin Anton
 */

class BusStopsDatabaseHandler extends DatabaseHandler {

    /**
     * Getting information about bus stops and setting them into city factory
     * @param cityFactory city factory
     * @throws SQLException
     */
    static void getBusStops(CityFactory cityFactory) throws SQLException {
        preparedStatement = connection.prepareStatement("select * from BusStops");

        ResultSet resultSet = preparedStatement.executeQuery();

        while(resultSet.next()) {
            cityFactory.addBusStop(resultSet.getString(2));
        }
    }

    /**
     * Getting bus stops distances and setting them into the city factory
     * @param cityFactory city factory
     * @throws SQLException
     */
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


    /**
     * Getting bus stop name.
     * If the stop does not exist, returns null
     * @param id bus stop id
     * @return the name of bus stop
     * @throws SQLException
     */
    static String getBusStopName(String id) throws SQLException {
        preparedStatement = connection.prepareStatement("select * from BusStops where id = ?");
        preparedStatement.setString(1,id);

        ResultSet resultSet = preparedStatement.executeQuery();

        if(resultSet.next()) return resultSet.getString(2);
        return null;
    }
}
