package remote.server;

import akka.actor.AbstractActor;
import akka.actor.Props;
import drtalgo.CityFactory;
import drtalgo.Passenger;

import java.sql.PreparedStatement;

/**
 * @author Kalinin Anton
 */
public class AlgorithmHandler extends AbstractActor {

    private final CityFactory cityFactory;

    public AlgorithmHandler(CityFactory cityFactory) {
        this.cityFactory = cityFactory;
    }

    static Props props(CityFactory cityFactory) {
        return Props.create(CityFactory.class, cityFactory);
    }


    static class SimplePassenger {

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Passenger.class, passenger -> {
                   // cityFactory.addPassenger(passenger);
                })
                .build();
    }
}
