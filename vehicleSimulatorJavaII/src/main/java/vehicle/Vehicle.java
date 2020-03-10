package vehicle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import routing.RouteDispatcher;

/*
 * @author Amanda Lewis
 * Refactored on 2-11-2020
 */
public class Vehicle implements Runnable {

	public final static int DEFAULT_ID = 9999;
	public final static Point2D DEFAULT_START = new Point2D.Double(-97.753438, 30.229688);
	public final static Point2D NOT_FOUND = new Point2D.Double(-1, -1);
	public final AtomicBoolean shouldRun;
	
	private boolean isAlive;
	private final Random random = new Random();
	private int id;
	private VehicleStatus status;
	private Point2D coordinates;
	private List<Point2D> route = new ArrayList<>();
	private List<Point2D> destinationList = new ArrayList<>();
	private List<String> addressList = new ArrayList<>();
	private final long delay; // in milliseconds (1 second == 1000 milliseconds)
	private final String fileToWrite;
	private final DateFormat dateFileFormat = new SimpleDateFormat("MM-dd-yyyy-HH_mm_ss");
	private final DateFormat dateToStringFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	/*
	 * Default Constructor
	 * Assigns a random id, available status, coordinates at St. Edward's, 5 second delay, and default fileFormat
	 */
	public Vehicle()
	{
		shouldRun = new AtomicBoolean(true);
		isAlive = false;
		id = random.nextInt();
		status = VehicleStatus.AVAILABLE;
		coordinates = DEFAULT_START;
		delay = 5*1000;
		fileToWrite = "test/default/vehiclesimulator" + id + "_" + getCurrentDateTimeFile() + ".txt";
	}

	/*
	 * Second constructor
	 * 
	 * Parameters: 
	 * int id: the id of the vehicle
	 * Point2D coordinates: the starting coordinates of the vehicle
	 * long delay: the amount of time in milliseconds that the vehicle goes without reporting
	 * String folderName: the name of the folder to store this vehicle's output in. Send in format 'folderName' with no slashes
	 */
	public Vehicle(int id, Point2D coordinates, long delay, String folderName)
	{
		shouldRun = new AtomicBoolean(true);
		isAlive = false;
		this.id = id;
		status = VehicleStatus.AVAILABLE;
		this.coordinates = coordinates;
		this.delay = delay;
		fileToWrite = "test/" + folderName + "/vehiclesimulator" + id + "_" + getCurrentDateTimeFile() + ".txt";
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "\n\n** Report date/time:" + getCurrentDateTimeString() + "\nVehicle ID #" + getId() +
				"\nVehicle Status: " + getStatusString() + 
				"\nVehicle Coordinates: " + getPrintedCoordinates() + 
				"\nVehicle Destination: " + getRouteEndAddress() + " **";
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		try {
			isAlive = true;
			File file = new File(fileToWrite);
			file.getParentFile().mkdirs();
			FileWriter fw = new FileWriter(file, true);

			fw.write("** Vehicle simulation started at: " + getCurrentDateTimeString() + " **\n");
			fw.flush();

			// This while loop will run until the shutdown command is given
			while (shouldRun.get() && !Thread.interrupted())
			{
				/*
				 * This section of the code pertains to when the vehicle is IN_TRANSIT and has a route to follow
				 */
				long count = 0;
				while (status == VehicleStatus.IN_TRANSIT && route.size() > 0)
				{
					/*
					 * If the count has become greater than or equal to the delay, print toString and status
					 */
					if(count >= delay)
					{
						fw.write(toString());
						fw.flush();
						count = 0;
					}

					// grab the first coordinate in the route and remove it after that.
					setCoordinates(route.get(0));
					route.remove(0);

					// when the route is empty set the vehicle status to arrived
					if (route.isEmpty())
					{
						setStatus(VehicleStatus.ARRIVED);
						String address = getAddressList().get(0);
						String arrivedUpdate = "\n** Vehicle #" + getId() + " arrived at " + address + " @ " + getCurrentDateTimeString() + " **";
						fw.write("\n" + arrivedUpdate);
						fw.flush();
						System.out.println(arrivedUpdate);
						// if the destination list has more items in it, get the next route
						getNextRoute(fw);
					}
					try {
						// sleep one second and add to count same number of seconds
						Thread.sleep(1000);
						count += 1000;
					} catch (InterruptedException e) {
						isAlive = false;
						shouldRun.set(false);
						e.printStackTrace();
					}
				}
				/*
				 * This sections pertains to when the vehicle does not have a route
				 */
				if (status == VehicleStatus.AVAILABLE || status == VehicleStatus.ARRIVED)
				{
					// otherwise print toString/status and make sure the status is available and not arrived
					fw.write(toString());
					fw.flush();
					setStatus(VehicleStatus.AVAILABLE);
					getNextRoute(fw);
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						isAlive = false;
						shouldRun.set(false);
						e.printStackTrace();
					}

				} 
			} // end of shouldRun while loop

			/*
			 * Write shutting down date/time to file and close file writer
			 */
			String shutDownStmt = "\n*** Vehicle #" + getId() + " shutting down at " + getCurrentDateTimeString() + " ***";
			fw.write("\n" + shutDownStmt);
			System.out.println(shutDownStmt);
			fw.flush();
			fw.close();
			isAlive = false;
		} catch (IOException e) {
			isAlive = false;
			e.printStackTrace();
			System.exit(0);
		}

	}

	public int getId()
	{
		return id;
	}

	public Vehicle setId(int id) 
	{
		if (this.id > 0) {
			this.id = id;
		}
		else {
			this.id = DEFAULT_ID;
		}
		return this;
	}

	public VehicleStatus getStatus()
	{
		return status;
	}

	public Vehicle setStatus(VehicleStatus status) 
	{
		this.status = status;
		return this;
	}

	public String getStatusString()
	{
		return status.toString();
	}

	public Point2D getCoordinates()
	{
		return coordinates;
	}

	public Vehicle setCoordinates(Point2D coordinates) 
	{
		this.coordinates = coordinates;
		return this;
	}

	public String getPrintedCoordinates()
	{
		return "(" + coordinates.getX() + ", " + coordinates.getY() + ")";
	}

	public static String getCoordinatesInStringFormat(Point2D coords)
	{
		return "(" + coords.getX() + ", " + coords.getY() + ")";
	}

	public List<Point2D> getRoute()
	{
		return route;
	}

	private Vehicle setRoute(List<Point2D> route) 
	{
		this.route = route;
		return this;
	}

	public String getRouteString()
	{
		String routeString = "Vehicle #" + getId() + " Route Start: \n";
		for(int r = 0; r < route.size(); r++) {
			if (r != (route.size() - 1)) 
			{
				routeString += getCoordinatesInStringFormat(route.get(r)) + ",\n";
			} else 
			{
				routeString += getCoordinatesInStringFormat(route.get(r)) + "\nRoute End.";
			}
		}
		return routeString;
	}

	public Point2D getRouteStartCoordinates()
	{
		Point2D coords = NOT_FOUND;
		if (route.size() > 0)
		{
			coords = route.get(0);
		}
		return coords;
	}

	public Point2D getRouteEndCoordinates()
	{
		Point2D coords = NOT_FOUND;
		if (route.size() > 0)
		{
			coords = route.get((route.size() - 1));
		}
		return coords;
	}

	public String getRouteStartAddress()
	{
		String retAddress = "No route.";
		Point2D startCoords = getCoordinates();
		if (!startCoords.equals(NOT_FOUND))
		{
			retAddress = RouteDispatcher.reverseGeocoding(startCoords);
		}
		return retAddress;
	}

	public String getRouteEndAddress()
	{
		String retAddress = "No route.";
		if (getAddressList().size() > 0)
		{
			retAddress = getAddressList().get(0);
		}
		return retAddress;
	}

	public String getRouteStartAndEndAddress() 
	{
		return "Vehicle #" + getId() + 
				"\nRoute Start: " + getRouteStartAddress() + 
				"\nRoute End: " + getRouteEndAddress();
	}

	public List<Point2D> getDestinationList() 
	{
		return destinationList;
	}

	public List<String> getAddressList() { return addressList; }

	private Vehicle setDestinationList(List<Point2D> list)
	{
		destinationList = list;
		return this;
	}

	private Vehicle setAddressList(List<String> list)
	{
		addressList = list;
		return this;
	}

	public Vehicle addPointToDestinationList(Point2D newDest)
	{
		destinationList.add(newDest);
		return this;
	}

	public Vehicle addAddressToAddressList(String address)
	{
		addressList.add(address);
		return this;
	}

	public Vehicle addAddressToDestinationList(String address)
	{
		Point2D point = RouteDispatcher.forwardGeocoding(address);
		addPointToDestinationList(point);
		addAddressToAddressList(address);
		return this;
	}

	private void getNextRoute(FileWriter fw)
	{
		if(destinationList.size() > 0 && addressList.size() > 0)
		{
			// remove the previous destination
			// by doing this all you need to do to get the address is
			// get the address in addressList at position 0
			// so no more reverse geocoding each time to print the address.
			getDestinationList().remove(0);
			getAddressList().remove(0);

			Point2D newDestination = getDestinationList().get(0);
			String address = getAddressList().get(0);
			Point2D currentLocation = getCoordinates();
			setRoute(RouteDispatcher.getRouteFromCoordinates(currentLocation, newDestination));
			String departingUpdate = "\n** Vehicle #" + getId() + " departing to destination " + address + " @ " + dateToStringFormat.format(getCurrentDateTime()) + " **";
			try {
				fw.write("\n" + departingUpdate);
				fw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(departingUpdate);
			setStatus(VehicleStatus.IN_TRANSIT);
		}
	}

	public Date getCurrentDateTime()
	{
		return new Date();
	}

	public String getCurrentDateTimeString()
	{
		return dateToStringFormat.format(getCurrentDateTime());
	}

	public String getCurrentDateTimeFile()
	{
		return dateFileFormat.format(getCurrentDateTime());
	}

	public boolean isStillRunning()
	{
		return isAlive;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vehicle that = (Vehicle) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
