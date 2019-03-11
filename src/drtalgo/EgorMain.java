package drtalgo;

import java.sql.SQLOutput;

public class EgorMain {
    public static void main(String[] args) {
        CityFactory ct = new CityFactory();
        City city = ct.getCity();
        ct.makeCityExample1();
        Vehicle veh = city.getVehicles().get(0);
        city.chooseWorkingVehicles();
        System.out.println("********************");
        System.out.println(veh.toString());
        System.out.println("********************");
        System.out.println(veh.getNextStopAndDistance().getKey() + " " + veh.getNextStopAndDistance().getValue());
        System.out.println("********************");

        veh.getOffPassengerAndMoveNext();
        System.out.println(veh.getCurstop().name);
        System.out.println("********************");
        System.out.println(veh.toString());
        System.out.println("********************");
        ct.addPassenger("Stop1", "Stop2", "EGOR");
        city.chooseWorkingVehicles();
        veh.getOffPassengerAndMoveNext();
        city.chooseWorkingVehicles();
        System.out.println(veh.toString());
    }
}
