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

/**
 * The server actor that handles connections
 *
 * @author Kalinin Anton
 */
public class Server extends AbstractActor {

    // The number of pending connections the queue will hold
    private final int backlog;

    // The current address of server
    private final InetSocketAddress address;

    // City creation factory
    final static CityFactory cityFactory = new CityFactory();



    /**
     * Server constructor
     * @param address the current address of server
     * @param backlog The number of pending connections the queue will hold
     */
    public Server(InetSocketAddress address, int backlog) {
        this.backlog = backlog;
        this.address = address;
    }

    /**
     * Server class prop
     * @param address the current address of server
     * @param backlog the number of pending connections the queue will hold
     * @return server's props
     */
    static Props props(InetSocketAddress address, int backlog) {
        return Props.create(Server.class, address, backlog);
    }

    /**
     * Bind server to ip
     * and add stop data to algorithm from database
     */
    @Override
    public void preStart() {
        final ActorRef tcp = Tcp.get(getContext().getSystem()).manager();
        tcp.tell(TcpMessage.bind(getSelf(),
                address, backlog), getSelf());

        // Setting preferences from the database
        try {
            BusStopsDatabaseHandler.getBusStops(cityFactory);
            BusStopsDatabaseHandler.getDistance(cityFactory);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        cityFactory.countDistances();

    }


    /**
     *  Server reaction
     */
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

                    getSender().tell(TcpMessage.write(ByteString.fromString(
                            String.format("%d %s %s", Configurations.CONNECTION,
                                    "true", "You've&been&connected")
                    )), getSelf());
                })
                .build();
    }

}

class ServerMain {
    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("MySystem");
        ActorRef server = system.actorOf(Server.props(
                new InetSocketAddress("192.168.43.154", 8080),
                100),
                "Server");



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

