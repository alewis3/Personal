package runner;

import java.awt.geom.Point2D;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vehicle.Vehicle;
import vehicleManager.VehicleManager;

/*
 * @author Amanda Lewis
 * Created on 8-17-2019
 */
public class VehicleInstantiator {

	public static void main(String[] args) {

		/*
		 * Instantiate vehicle manager and vehicle executor to handle the vehicles/threads
		 */
		VehicleManager vm = new VehicleManager();
		ExecutorService vehicleExecutor = Executors.newCachedThreadPool();

		Scanner sc = new Scanner(System.in);

		/*
		 * This regex will be used to verify the file name. 
		 * It can contain alphanumeric characters and dashes and underscores
		 */
		String regex = "^[a-zA-Z0-9-_]+$";
		Pattern pattern = Pattern.compile(regex);

		System.out.println("*** Thank you for starting the Java Vehicle Simulator. \nCreated by Amanda Lewis 2019. ***");

		/*
		 * Grab a filename for the user to use for each of the vehicles created in this run
		 */
		System.out.println("Please enter a folder name for all the output files of the vehicle for this run. \nThe directory will be /Users/patricklewis/VehicleTestOutput/. \nEnter only alphanumeric characters as well as dashes (-) and underscores (_).");
		String fileName = null;
		while (fileName == null)
		{
			fileName = sc.nextLine();
			Matcher matcher = pattern.matcher(fileName);
			if (!matcher.matches())
			{
				System.out.println("The file name must be alphanumeric, dashes, or underscores!");
				fileName = null;
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				System.out.println("Shutdown hook enabled");
				sc.close();
				vm.values().forEach(vehicle -> vehicle.shouldRun.set(false));
				vehicleExecutor.shutdown();
			}
		});

		/*
		 * Here we will grab the user's input and keep printing the menu until they enter something valid.
		 */
		int choice = 0;
		while (choice != 3)
		{
			while (choice == 0)
			{
				printMenu();
				String input = sc.nextLine();
				try
				{
					choice = Integer.parseInt(input);
					if (choice < 1 || choice > 3)
					{
						System.out.println("Choice must be between 1 and 3!");
						choice = 0;
					}
				}
				catch (NumberFormatException e)
				{
					System.out.println("Error: Choice must be an integer!");
					choice = 0;
				}

			}

			/*
			 * Switch to handle the option the user chose
			 */
			switch (choice)
			{
			/* 
			 * if the option is 1, we want to add a vehicle with default coordinates, 
			 * a delay of 10 seconds, and the filename specified
			 */
			case 1:
				try
				{
					System.out.println("Adding vehicle #" + vm.size());
					Vehicle vehicle = new Vehicle(vm.size(), (new Point2D.Double(-97.7437,30.2711)), 10*1000, fileName);
					vm.add(vehicle);
					vehicleExecutor.execute(vehicle);
					choice = 0;
					break;
				} catch (Exception e)
				{
					System.out.println("Could not add vehicle due to: " + e.getClass().getName() + " error");
					e.printStackTrace();
					System.exit(0);
				}
				/* 
				 * if the option is 2 we need to retrieve an address from the user and find the closest 
				 * available vehicle to that address. If there are no available vehicles, we will try 
				 * to find the closest vehicle with the shortest destination list that can take the job
				 */
			case 2:
				System.out.println("Please enter the address in Austin where you need a vehicle sent. \nEnter a valid address with more than three characters and no semicolons.");
				String address = "";
				while (address.isEmpty())
				{
					address = sc.nextLine();
					if (address.length() <= 3)
					{
						address = "";
						System.out.println("Address must be more than 3 characters!\nPlease enter the address in Austin where you need a vehicle sent. \nEnter a valid address with more than three characters and no semicolons.");
					}
				}

				int vehicleIndex = vm.getClosestAvailableFromAddress(address);
				if (vehicleIndex == -1)
				{
					System.out.println("Your address could not be found, possibly because it was misspelled or not in Austin. Please try again.");
				}
				else
				{
					System.out.println("Directing vehicle #" + vm.get(vehicleIndex).getId() + " to " + address + " now.");
					vm.get(vehicleIndex).addAddressToDestinationList(address);
				}
				choice = 0;
				break;
			case 3: 
				break;
			default:
				System.out.println("Invalid Choice.");
				break;
			}
		}
		sc.close();
		vm.values().forEach(vehicle -> vehicle.shouldRun.set(false));
		System.out.println("Vehicles shutting down");
		vehicleExecutor.shutdown();

		/*
		 * Check if the program is still running.
		 */
		boolean programRunning = true;
		while (programRunning)
		{
			int finishedCount = 0;
			for(int h = 0; h < vm.size(); h++)
			{
				if (!vm.get(h).isStillRunning())
				{
					finishedCount++;
				}
			}
			if (finishedCount == vm.size())
			{
				programRunning = false;
			}
		}
		System.out.println("All vehicles shut down");
	}

	public static void printMenu()
	{
		System.out.println("\nPlease pick an option from 1 to 3: \n1) Add a vehicle \n2) Direct a vehicle to an address \n3) Exit\n");
	}
}
