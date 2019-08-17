package runner;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import vehicle.VehicleRunnable;

public class VehicleInstantiator {

	public static void main(String[] args) {
		
		HashMap<Integer, VehicleRunnable> vehicles = new HashMap<>(8);
		ExecutorService vehicleExecutor = Executors.newCachedThreadPool();
		
		vehicles.put(1, new VehicleRunnable(1, (new Point2D.Double(-97.753438, 30.229688)), 4*1000));
		vehicles.put(2, new VehicleRunnable(2, (new Point2D.Double(-97.753438, 30.229688)), 3*1000));
		vehicles.put(3, new VehicleRunnable(3, (new Point2D.Double(-97.753438, 30.229688)), 5*1000));
		vehicles.put(4, new VehicleRunnable(4, (new Point2D.Double(-97.753438, 30.229688)), 2*1000));
		vehicles.put(5, new VehicleRunnable(5, (new Point2D.Double(-97.753438, 30.229688)), 6*1000));
		vehicles.put(6, new VehicleRunnable(6, (new Point2D.Double(-97.753438, 30.229688)), 1*1000));
		vehicles.put(7, new VehicleRunnable(7, (new Point2D.Double(-97.753438, 30.229688)), 8*1000));
		vehicles.put(8, new VehicleRunnable(8, (new Point2D.Double(-97.753438, 30.229688)), 7*1000));

		vehicles.values().forEach(vehicleExecutor::execute);


        //kindly request vehicles to shut down (will not complete until after all routes are done)
        vehicles.values().forEach(vehicleRunnable -> vehicleRunnable.shouldRun.set(false));
        System.out.println("Vehicles shutting down");
        vehicleExecutor.shutdown();
	}

}
