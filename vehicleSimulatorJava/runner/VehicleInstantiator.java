package runner;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import vehicle.VehicleRunnable;

public class VehicleInstantiator {

	public static int vehicleCount = 0;
	
	public static void main(String[] args) {
		
		List<VehicleRunnable> vehicles = new ArrayList<>();
		ExecutorService vehicleExecutor = Executors.newCachedThreadPool();
		
		System.out.println("*** Thank you for starting the Java Vehicle Simulator. \nCreated by Amanda Lewis 2019. ***");
		
		int choice = 0;
		while (choice != 3)
		{
			printMenu();
			if (choice == 1)
			{
				vehicleCount++;
				VehicleRunnable vehicle = new VehicleRunnable(vehicleCount, (new Point2D.Double(-97.7437,30.2711)), 8*1000);
				vehicles.add(vehicle);
				vehicleExecutor.execute(vehicle);
			}
			if (choice == 2)
			{
				
			}
		}
		
		
		
		
		
		vehicles.add(new VehicleRunnable(1, (new Point2D.Double(-97.753438, 30.229688)), 8*1000));

		vehicleExecutor.execute(vehicles.get(0));

        //kindly request vehicles to shut down (will not complete until after all routes are done)
        vehicles.values().forEach(vehicleRunnable -> vehicleRunnable.shouldRun.set(false));
        System.out.println("Vehicles shutting down");
        vehicleExecutor.shutdown();
	}

	public static void printMenu()
	{
		System.out.println("Please pick an option from 1 to 3: \n1) Add a vehicle \n2) Direct a vehicle to an address \n3) Exit");
	}
}
