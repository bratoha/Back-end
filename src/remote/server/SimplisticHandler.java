package remote.server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.util.ByteString;
import com.server.ServerInformation;
import drtalgo.BusStop;
import drtalgo.City;
import drtalgo.CityFactory;
import drtalgo.Vehicle;


import java.sql.*;
import java.util.*;


public class SimplisticHandler extends AbstractActor {

    private static HashSet<ActorRef> clientSystem = new HashSet<>();
    private static HashSet<ActorRef> driverSystem = new HashSet<>();
    private static CityFactory cityFactory;
    private static City city;
    private String userName;
    private boolean taken = false;


    static {
        cityFactory = Server.cityFactory;
        city = cityFactory.getCity();
    }

    public SimplisticHandler() {

    }

    static private void getInformation(){
        System.out.println("\n####################");
        System.out.println("Clients " + clientSystem.size());
        System.out.println("Drivers " + driverSystem.size());
        System.out.println("####################\n");
    }

    static Props props() {
        return Props.create(SimplisticHandler.class);
    }

    // Реакция сервера на отправку сообщений клиентам
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Tcp.Received.class, msg -> {

                    // 0 - name, 1 - password, 2 - fullname, 3 - phoneNumber, 4 - status, 5 - registration?
                    String strInfo = msg.data().decodeString("UTF-8");
                    String[] userInfo = strInfo.split(" ");
                    if(!clientSystem.contains(getSender()) &&  !driverSystem.contains(getSender())) {
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
                                clientSystem.add(getSender());
                                break;
                            }
                            case "d": {
                                driverSystem.add(getSender());
                                break;
                            }
                            default:
                                System.out.println("Invalid client");
                        }

                        getInformation();
                        userName = UsersDatabaseHandler.getUserName(userInfo[0], userInfo[1]);
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
                            vehicle.getTrip().getTrip().toString();

                          //  vehicle.setCurstop(stop);
                            System.out.println(vehicle.toString());

                    }

//                    for (Vehicle vehicle : city.getVehicles()) {
//                        System.out.println(vehicle.toString());
//                        vehicle.getTrip().getTrip().remove(0);
//                        System.out.println(vehicle.toString());
//                    }


                    getSelf().tell(new ServerInformation(true, "You have been taken", false), getSender());
                })
                .match(Tcp.ConnectionClosed.class, msg -> {
                    System.out.println("Client closed");
                    clientSystem.remove(getSender());
                    driverSystem.remove(getSender());
                    getInformation();
                    getContext().stop(getSelf());
                })
                .match(ByteString.class, msg -> {
                    for(ActorRef actorRef :
                        clientSystem) {
                        actorRef.tell(TcpMessage.write(msg), getSelf());
                    }
                })
                .match(ServerInformation.class, info -> {
                    getSender().tell(TcpMessage.write(ByteString.fromArray(info.serializeSelf())), getSelf());
                })
                .build();
    }
}
