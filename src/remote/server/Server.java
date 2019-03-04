package remote.server;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Scanner;

import akka.actor.*;
import akka.io.*;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.Connected;
import akka.util.ByteString;
import com.server.ServerInformation;
import drtalgo.CityFactory;

public class Server extends AbstractActor {

    private final int backlog;
    final static CityFactory cityFactory = new CityFactory();
    public Server(int backlog) {
        this.backlog = backlog;

    }

    static Props props(int backlog) {
        return Props.create(Server.class, backlog);
    }

    // Привязка сервера к ip
    @Override
    public void preStart() {
        final ActorRef tcp = Tcp.get(getContext().getSystem()).manager();
        tcp.tell(TcpMessage.bind(getSelf(),
                new InetSocketAddress("localhost", 8080), backlog), getSelf());


        // Тут работа с БД, чтобы не париться с MySQL, добавляй сам остановки
        // Главное не юзай DatabaseHandler'ы

        /*try {
            BusStopsDatabaseHandler.getBusStops(cityFactory);
            BusStopsDatabaseHandler.getDistance(cityFactory);
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
        cityFactory.countDistances();

        //Вот здесь добавляй пассажиров и водил
        cityFactory.addVehicle(20,"CentralSquare");
        cityFactory.addVehicle(20, "Zoo");
    }


    // Реакция сервера на работу сети и подключения
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Tcp.Bound.class, msg -> {
                    System.out.println("Server was launched on " + msg.localAddress().toString());
                })
                .match(CommandFailed.class, msg -> {
                    getContext().stop(getSelf());
                })
                .match(Connected.class, conn -> {
                    System.out.println("Connected client " + conn.remoteAddress().toString());

                    // Привязываем SimplisticHandler к клиенту
                    final ActorRef handler = getContext().actorOf(
                           SimplisticHandler.props());

                    getSender().tell(TcpMessage.register(handler,
                            true, // <-- keepOpenOnPeerClosed flag
                            true), getSelf());

                    getSender().tell(TcpMessage.write(
                            ByteString.fromArray(new ServerInformation(
                                    false,
                                    "You've been connected",
                                    true)
                                    .serializeSelf())),
                                    getSelf());
                })
                .build();
    }

}

class ServerMain {
    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("MySystem");
        ActorRef server = system.actorOf(Server.props(100), "Server");
        Thread.sleep(1000);
        Scanner in = new Scanner(System.in);
        String message = in.nextLine();



        while(!message.equals("close")) {
            server.tell(new ServerInformation(false, message, true), ActorRef.noSender());

            Thread.sleep(10);
        }

        system.terminate();

    }

}

