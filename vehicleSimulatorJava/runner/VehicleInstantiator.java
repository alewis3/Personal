package runner;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vehicle.Vehicle;
import vehicleManager.VehicleManager;

/*
 * @author Amanda Lewis
 * Created on 8-17-2019
 */
public class VehicleInstantiator {

	public static void main(String[] args) {

		VehicleManager vm = new VehicleManager();
		ExecutorService vehicleExecutor = Executors.newCachedThreadPool();
		Scanner sc = new Scanner(System.in);

		System.out.println("*** Thank you for starting the Java Vehicle Simulator. \nCreated by Amanda Lewis 2019. ***");

		int choice = 0;
		while (choice != 3)
		{
			printMenu();
			choice = sc.nextInt();
			sc.nextLine();
			if (choice < 1 || choice > 3)
			{
				System.out.println("Choice must be between 1 and 3!");
			}
			else
			{
				if (choice == 1)
				{
					System.out.println("Adding vehicle #" + vm.size());
					Vehicle vehicle = new Vehicle(vm.size(), (new Point2D.Double(-97.7437,30.2711)), 8*1000);
					vm.add(vehicle);
					vehicleExecutor.execute(vehicle);
				}
				if (choice == 2)
				{
					System.out.println("Please enter the address in Austin where you need a vehicle sent.");
					String address = sc.nextLine();
					int vehicleIndex = vm.getClosestAvailableFromAddress(address);
					if (vehicleIndex == -1)
					{
						System.out.println("Your address could not be found, possibly because it was misspelled or not in Austin. Please try again.");
					}
					else
					{
						System.out.println("Directing vehicle #" + vm.get(vehicleIndex).getId() + " to " + address + ".");
						vm.get(vehicleIndex).addAddressToDestinationList(address);
					}
				}
			}

		}
		sc.close();
		vm.values().forEach(vehicleRunnable -> vehicleRunnable.shouldRun.set(false));
		System.out.println("Vehicles shutting down");
		vehicleExecutor.shutdown();
	}

	public static void printMenu()
	{
		System.out.println("\nPlease pick an option from 1 to 3: \n1) Add a vehicle \n2) Direct a vehicle to an address \n3) Exit\n");
	}
}
