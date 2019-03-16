package remote.server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.util.ByteString;
import drtalgo.City;
import drtalgo.CityFactory;
import drtalgo.Vehicle;
import javafx.util.Pair;
import scala.Int;

import java.sql.*;
import java.util.*;

/**
 * Special handler of connected clients
 *
 * @author Kalinin Anton
 */

public class SimplisticHandler extends AbstractActor {

    //---------------------------------------- The HashMap of users -------------------------------------------//

    // Passengers
    private static HashMap<String, ActorRef> clientSystem = new HashMap<>();

    // Passenger -> stop from
    private static HashMap<String, String> passengersStops = new HashMap<>();

    // Drivers
    private static HashMap<String, ActorRef> driverSystem = new HashMap<>();

    // Drivers ID
    private static HashMap<String, Integer> driversId = new HashMap<>();

    //---------------------------------------------------------------------------------------------------------//


    //----------------------------------------- Algorithm fields ----------------------------------------------//

    // City creation factory
    private static CityFactory cityFactory;

    // Current city
    private static City city;

    //---------------------------------------------------------------------------------------------------------//


    //-------------------------------------------- User fields ------------------------------------------------//

    // User name (from database)
    private String userName;

    // Was the user taken (for passengers)
    private boolean taken = false;

    // Was the user entered to the system
    private boolean entered = false;

    private static int idKey = 0;

    //---------------------------------------------------------------------------------------------------------//

    /*
      Static constructor
     */
    static {
        cityFactory = Server.cityFactory;
        city = cityFactory.getCity();
    }

    /**
     * Get information about current users
     */
    static private void getInformation(){
        System.out.println("\n####################");
        System.out.println("Clients " + clientSystem.size());
        for(Map.Entry<String, ActorRef> user : clientSystem.entrySet())
            System.out.println("\t" + user.getKey());
        System.out.println("Drivers " + driverSystem.size());
        for(Map.Entry<String, ActorRef> user : driverSystem.entrySet())
            System.out.println("\t" + user.getKey());
        System.out.println("####################\n");
    }

    static private String getName(int id){
        for(HashMap.Entry<String, Integer> el : driversId.entrySet()) {
            if(el.getValue().equals(id))
                return el.getKey();
        }

        return null;
    }

    /**
     * Simplistic Handler class prop
     * @return handler's props
     */
    static Props props() {
        return Props.create(SimplisticHandler.class);
    }



    /**
     * Handler's response to accepting and receiving messages from a specific user
     *
     * @return receive builder
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Tcp.Received.class, msg -> {
                    String[] userInfo;
                    String strInfo = msg.data().decodeString("UTF-8");
                    userInfo = strInfo.split(" ", Integer.MAX_VALUE);
                    int configuration = Integer.valueOf(userInfo[0]);
                    String message = "";
                    switch (configuration) {
                        case Configurations.REGISTRATION: {
                            try {
                                if (!UsersDatabaseHandler.addUser(
                                        userInfo[1],  /* Name */
                                        userInfo[2],  /* Password */
                                        userInfo[3],  /* Full name */
                                        userInfo[4],  /* Phone number */
                                        userInfo[5])) /* Status */ {

                                    message = String.format("%d %s %s",
                                            Configurations.REGISTRATION, "false", "This&user&is&already&exist");

                                } else {
                                    message = String.format("%d %s %s",
                                            Configurations.REGISTRATION, "true", "You've&been&registered");
                                }
                            } catch (SQLException ex) {
                                System.out.println("SQL Exception");
                                message = String.format("%d %s",
                                        Configurations.EXCEPTION, "SQL&exception&in&server");
                            }
                            break;
                        }
                        case Configurations.LOGIN: {
                            try{
                                if(!UsersDatabaseHandler.login(
                                        userInfo[1],  /* Name */
                                        userInfo[2],  /* Phone number */
                                        userInfo[3])) /* Password */ {

                                    message = String.format("%d %s %s",
                                            Configurations.LOGIN, "false", "Incorrect&name&or&password");
                                } else {

                                    // Set user status
                                    if (userInfo[4].equals("d")) {
                                        driverSystem.put(userInfo[1], getSender());
                                        driversId.put(userInfo[1], idKey);
                                        idKey++;
                                    } else if(!userInfo[4].equals("u")) {
                                        System.out.println("Invalid client");
                                        message = String.format("%d %s %s",
                                                Configurations.LOGIN, "false", "Incorrect&user&status");

                                        getSelf().tell(message, getSender());
                                        return;
                                    }

                                    message = String.format("%d %s %s",
                                            Configurations.LOGIN, "true", "You've&been&entered");
                                    userName = userInfo[1];
                                    entered = true;
                                    System.out.println(String.format("[%s]: Client've been entered", userInfo[1]));

                                }
                            } catch (SQLException ex) {
                                System.out.println("SQL Exception");
                                message = String.format("%d %s",
                                        Configurations.EXCEPTION, "SQL&exception&in&server");

                            }

                            break;
                        }
                        case Configurations.SET_DRIVER_STOP: {
                            String busStopName = BusStopsDatabaseHandler.getBusStopName(userInfo[2]);
                            cityFactory.addVehicle(Integer.valueOf(userInfo[1]),
                                    busStopName);
                            message = String.format("%d %s", Configurations.SET_DRIVER_STOP, "You&are&here:&" + busStopName);
                            break;
                        }
                        case Configurations.ARRIVAL: {

                            break;
                        }

                        case Configurations.SELECTION: {
                            if(!clientSystem.containsKey(userName)) {
                                String from = BusStopsDatabaseHandler.getBusStopName(userInfo[1]);
                                String to = BusStopsDatabaseHandler.getBusStopName(userInfo[2]);

                                clientSystem.put(userName, getSender());
                                passengersStops.put(userName,from);

                                cityFactory.addPassenger(from,to,userName);
                                city.chooseWorkingVehicles();

                                for (Vehicle vehicle : city.getVehicles()) {
                                    System.out.println(vehicle.toString());
                                }

                                message = String.format("%d %s",
                                        Configurations.SELECTION, "Wait&for&the&bus" );


                                taken = true;

                                int i = 0;
                                for(Vehicle vehicle : city.getVehicles()){
                                    if (from != null && from.equals(vehicle.getCurstop().getName()))
                                        message = String.format("%d %s",
                                                Configurations.SELECTION, "Bus&is&here.&Id:&" + i);
                                    ++i;
                                }

                                i = 0;
                                for(Map.Entry<String, ActorRef> el : driverSystem.entrySet()) {
                                    Pair<String, Double> nextStop = city.getVehicles().get(i).
                                            getNextStopAndDistance();

                                    if(nextStop == null)
                                        getSelf().tell(String.format("%d %s",
                                            Configurations.SET_DRIVER_STOP, "You've&not&next&station"), el.getValue());
                                    else
                                        getSelf().tell(String.format("%d %s",
                                                Configurations.SET_DRIVER_STOP, "Next&station&" + nextStop.getKey()), el.getValue());
                                    ++i;
                                }

                            }
                            else {
                                message = String.format("%d %s",
                                        Configurations.SELECTION, "You've&been&select&bus&stop");
                                taken = true;
                            }
                            break;
                        }
                        case Configurations.EXCEPTION: {

                            break;
                        }
                        case Configurations.DRIVER_ARRIVE: {

                            ArrayList<String> passengers =
                                    cityFactory.getCity().getVehicles().
                                            get(driversId.get(userName)).getOffPassengerAndMoveNext();

                            city.chooseWorkingVehicles();

                            for (Vehicle vehicle : city.getVehicles()) {
                                System.out.println(vehicle.toString());
                            }

                            String currentStop = city.getVehicles().get(driversId.get(userName)).getCurstop().getName();

                            for(Map.Entry<String, String> passenger : passengersStops.entrySet()){
                                if(passenger.getValue().equals(currentStop))
                                    getSelf().tell(String.format("%d %s",
                                            Configurations.SELECTION, "Driver&is&here.&Id:&" + driversId.get(userName)),
                                            clientSystem.get(passenger.getKey()));
                            }

                            Pair<String, Double> nextStop = city.getVehicles().get(driversId.get(userName)).
                                    getNextStopAndDistance();
                            String busStopMessage;

                            if(nextStop == null) busStopMessage = "It&was&the&last&station";
                            else busStopMessage = "Next&bus&stop&is&"+nextStop.getKey();




                            if(passengers == null || passengers.size() == 0) {
                                message = String.format("%d %s", Configurations.DRIVER_ARRIVE,
                                        "No&one&to&get&off.&" + busStopMessage);
                                break;
                            }




                            for(String passenger : passengers) {
                                clientSystem.get(passenger).tell(
                                        TcpMessage.write(ByteString.fromString(
                                                String.format("%d %s", Configurations.ARRIVAL, "You've&been&arrived"))),
                                        getSelf());
                                clientSystem.remove(passenger);
                                passengersStops.remove(passenger);
                                taken = false;
                            }

                            message = String.format("%d %s", Configurations.DRIVER_ARRIVE,
                                    "You&get&off&passengers.&" + busStopMessage);
                            break;
                        }
                        default: {
                            System.out.println("Incorrect message from user: " + getSender());
                        }
                    }

                    getSelf().tell(message, getSender());

                    getInformation();
                })
                .match(Tcp.ConnectionClosed.class, msg -> {
                    System.out.println("Client closed");
                    driverSystem.remove(userName);
                    getInformation();
                    getContext().stop(getSelf());
                })
                .match(ByteString.class, msg -> {
                    for(Map.Entry<String, ActorRef> actorRef :
                        clientSystem.entrySet()) {
                        actorRef.getValue().tell(TcpMessage.write(msg), getSelf());
                    }
                })
                .match(String.class, str -> {
                    getSender().tell(TcpMessage.write(ByteString.fromString(str)), getSelf());
                })
                .build();
    }
}
