package remote.server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.util.ByteString;
import com.server.ServerInformation;
import drtalgo.City;
import drtalgo.CityFactory;
import drtalgo.Vehicle;

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

    // Drivers
    private static HashMap<String, ActorRef> driverSystem = new HashMap<>();

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

                    String strInfo = msg.data().decodeString("UTF-8");
                    String[] userInfo = strInfo.split(" ");
                    if(!entered) {
                        // 0 - name, 1 - password, 2 - fullname, 3 - phoneNumber, 4 - status, 5 - registration
                        try {
                            // Вход/регистрация клиента
                            switch (userInfo[5]){
                                case "y": {
                                    if(!UsersDatabaseHandler.addUser
                                            (userInfo[0], userInfo[1], userInfo[2], userInfo[3], userInfo[4])) {
                                        getSelf().tell(new ServerInformation(
                                                        false, "This user already exists", true),
                                                getSender());
                                    } else {

                                        getSelf().tell(new ServerInformation(
                                                        false, "You have been registered", false),
                                                getSender());
                                    }
                                    return;
                                }
                                case "n": {
                                    if(!UsersDatabaseHandler.login(userInfo[0],userInfo[3], userInfo[1])) {
                                        getSelf().tell(new ServerInformation(
                                                        false, "Incorrect name or password", true),
                                                getSender());
                                        return;
                                    }

                                    getSelf().tell(new ServerInformation(
                                                    true, "You have been entered", false),
                                            getSender());
                                }
                            }

                        } catch (ClassCastException ex) {
                            System.out.println("Wrong message form client");
                            getSelf().tell(new ServerInformation(
                                            false, "Wrong message", true),
                                    getSender());
                            return;
                        } catch (SQLException ex) {
                            System.out.println("SQL Exception");
                            getSelf().tell(new ServerInformation(
                                            false, "SQL Exception", true),
                                    getSender());
                            return;
                        }

                        switch (userInfo[4]) {
                            case "u": {
                                clientSystem.put(userInfo[0], getSender());
                                break;
                            }
                            case "d": {
                                driverSystem.put(userInfo[0], getSender());
                                break;
                            }
                            default:
                                System.out.println("Invalid client");
                        }

                        getInformation();
                        userName = UsersDatabaseHandler.getUserName(userInfo[0], userInfo[1]);
                        entered = true;
                        return;
                    }

                    if(!taken) {
                        String from = BusStopsDatabaseHandler.getBusStopName(userInfo[0]);
                        String to = BusStopsDatabaseHandler.getBusStopName(userInfo[1]);

                        cityFactory.addPassenger(from,to,userName);

                        try {
                            city.chooseWorkingVehicles();
                        } catch (NullPointerException ex) {
                            System.out.println("Null");
                            return;
                        }

                        for (Vehicle vehicle : city.getVehicles()) {
                            System.out.println(vehicle.toString());
                        }

                        getSelf().tell(new ServerInformation(true, "All OK", false), getSender());
                        taken = true;
                        return;
                    }


                    for (Vehicle vehicle : city.getVehicles()) {
                        System.out.println(vehicle.toString());
                    }


                    getSelf().tell(new ServerInformation(true, "You have been taken", false), getSender());
                })
                .match(Tcp.ConnectionClosed.class, msg -> {
                    System.out.println("Client closed");
                    clientSystem.remove(userName);
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
                .match(ServerInformation.class, info -> {
                    getSender().tell(TcpMessage.write(ByteString.fromArray(info.serializeSelf())), getSelf());
                })
                .build();
    }
}
