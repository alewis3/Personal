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
 * Created on 8-17-2019
 */
public class Vehicle implements Runnable {

	public final static Point2D DEFAULT_START = new Point2D.Double(-97.753438, 30.229688);
	public final static Point2D NOT_FOUND = new Point2D.Double(-1, -1);

	public final AtomicBoolean shouldRun;
	private boolean isAlive;
	private final Random random = new Random();
	private int id;
	private VehicleStatus status;
	private Point2D coordinates = new Point2D.Double();
	private List<Point2D> route = new ArrayList<Point2D>();
	private List<Point2D> destinationList = new ArrayList<Point2D>();
	private final long delay;
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
		fileToWrite = "test/default/vehiclesimulator" + id + "_" + dateFileFormat.format(getCurrentDateTime()) + ".txt";
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
		fileToWrite = "test/" + folderName + "/vehiclesimulator" + id + "_" + dateFileFormat.format(getCurrentDateTime()) + ".txt";
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "\n\n** Report date/time:" + dateToStringFormat.format(getCurrentDateTime()) + "\nVehicle ID #" + getId() + 
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

			fw.write("** Vehicle simulation started at: " + dateToStringFormat.format(getCurrentDateTime()) + " **\n");
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
						count = 0;
					}

					// grab the first coordinate in the route and remove it after that.
					coordinates = route.get(0);
					route.remove(0);

					// when the route is empty set the vehicle status to arrived
					if (route.isEmpty())
					{
						setStatus(VehicleStatus.ARRIVED);
						String address = RouteDispatcher.reverseGeocoding(getCoordinates());
						String arrivedUpdate = "\n** Vehicle #" + getId() + " arrived at " + address + " @ " + dateToStringFormat.format(getCurrentDateTime()) + " **";
						fw.write("\n" + arrivedUpdate);
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
						e.printStackTrace();
						System.exit(0);
					}
				}
				/*
				 * This sections pertains to when the vehicle does not have a route
				 */
				if (status == VehicleStatus.AVAILABLE || status == VehicleStatus.ARRIVED)
				{
					// otherwise print toString/status and make sure the status is available and not arrived
					fw.write(toString());
					setStatus(VehicleStatus.AVAILABLE);
					getNextRoute(fw);
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						isAlive = false;
						e.printStackTrace();
						System.exit(0);
					}

				} 
			} // end of shouldRun while loop

			/*
			 * Write shutting down date/time to file and close file writer
			 */
			fw.write("\n\n*** Vehicle #" + getId() + " shutting down at " + dateToStringFormat.format(getCurrentDateTime()) + " ***");
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
		this.id = id;
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
				routeString += route.get(r) + ",\n";
			} else 
			{
				routeString += route.get(r) + "\nRoute End.";
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
		Point2D startCoords = getRouteStartCoordinates();
		if (!startCoords.equals(NOT_FOUND))
		{
			retAddress = RouteDispatcher.reverseGeocoding(startCoords);
		}
		return retAddress;
	}

	public String getRouteEndAddress()
	{
		String retAddress = "No route.";
		Point2D endCoords = getRouteEndCoordinates();
		if (!endCoords.equals(NOT_FOUND))
		{
			retAddress = RouteDispatcher.reverseGeocoding(endCoords);
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

	private Vehicle setDestinationList(List<Point2D> list)
	{
		destinationList = list;
		return this;
	}

	public Vehicle addPointToDestinationList(Point2D newDest)
	{
		destinationList.add(newDest);
		return this;
	}

	public Vehicle addAddressToDestinationList(String address)
	{
		Point2D point = RouteDispatcher.forwardGeocoding(address);
		addPointToDestinationList(point);
		return this;
	}

	private void getNextRoute(FileWriter fw)
	{
		if(destinationList.size() > 0)
		{
			Point2D newDestination = getDestinationList().get(0);
			String address = RouteDispatcher.reverseGeocoding(newDestination);
			Point2D currentLocation = getCoordinates();
			setRoute(RouteDispatcher.getRouteFromCoordinates(currentLocation, newDestination));
			getDestinationList().remove(0);
			String departingUpdate = "\n** Vehicle #" + getId() + " departing to destination " + address + " @ " + dateToStringFormat.format(getCurrentDateTime()) + " **";
			try {
				fw.write("\n" + departingUpdate);
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
