package drtalgo;

/**
 * Class CityFactory
 * Use for creating an instance of class City
 */
public class CityFactory {

    /** fields **/

    // city that is creating
    private City city;
    private static int index = 1;

    /** constructor **/
    public CityFactory(){
        city = new City();
    }

    public City getCity(){return city;}

    /**
     * Making an example of city
     */
    public void makeCityExample1(){
        BusStop stop1 = new BusStop("Stop1");
        BusStop stop2 = new BusStop("Stop2");
        BusStop stop3 = new BusStop("Stop3");
        BusStop stop4 = new BusStop("Stop4");
        BusStop stop5 = new BusStop("Stop5");


        city.addStop(stop1);
        city.addStop(stop2);
        city.addStop(stop3);
        city.addStop(stop4);
        city.addStop(stop5);

        city.addRoad(stop1, stop2, 5);
        city.addRoad(stop2, stop4, 8);
        city.addRoad(stop1, stop3, 6);
        city.addRoad(stop3, stop4, 7);
        city.addRoad(stop4, stop5, 9);

        city.countDistances();

        Passenger pass1 = new Passenger(stop1, stop2, "Passenger1");
        Passenger pass2 = new Passenger(stop1, stop2, "Passenger2");
        Passenger pass3 = new Passenger(stop4, stop5, "Passenger3");
        Passenger pass4 = new Passenger(stop4, stop5, "Passenger4");

        city.addPassenger(pass1);
        city.addPassenger(pass2);
        city.addPassenger(pass3);
        city.addPassenger(pass4);


        Vehicle vehicle1 = new Vehicle(city, 20, 1, stop1);
        Vehicle vehicle2 = new Vehicle(city, 20, 2, stop4);
        index = 3;


        city.addVehicle(vehicle1);
        city.addVehicle(vehicle2);

    }

    public void addBusStop(String name) {
        city.addStop(new BusStop(name));
    }

    public void addRoad(String from, String to, double distance){
        city.addRoad(city.getStop(from), city.getStop(to), distance);
    }

    public void addPassenger(String from, String to, String name) {
        city.addPassenger(new Passenger(city.getStop(from), city.getStop(to), name));
    }

    public void addVehicle(int capacity, String name) {
        city.addVehicle(new Vehicle(city, capacity, index++, city.getStop(name)));
    }

    public void countDistances(){
        city.countDistances();
    }

}
