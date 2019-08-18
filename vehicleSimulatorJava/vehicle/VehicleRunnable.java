package vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.awt.geom.Point2D;
import routing.RouteDispatcher;

/*
 * Created by Amanda Lewis on 8-17-2019
 */
public class VehicleRunnable implements Runnable {

	private final Point2D DEFAULT_START = new Point2D.Double(-97.753438, 30.229688);

	public final AtomicBoolean shouldRun;
	private final Random random = new Random();
	private int id;
	private VehicleStatus status;
	private Point2D coordinates = new Point2D.Double();
	private List<Point2D> route = new ArrayList<Point2D>();
	private List<Point2D> destinationList = new ArrayList<Point2D>();
	private final long delay;

	/*
	 * Default Constructor
	 */
	public VehicleRunnable()
	{
		shouldRun = new AtomicBoolean(true);
		id = random.nextInt();
		status = VehicleStatus.AVAILABLE;
		coordinates = DEFAULT_START;
		delay = 5*1000;
	}

	/*
	 * Second constructor
	 * 
	 * Parameters: 
	 * int id: the id of the vehicle
	 * Point2D coordinates: the starting coordinates of the vehicle
	 * long delay: the amount of time in milliseconds that the vehicle goes without reporting
	 */
	public VehicleRunnable(int id, Point2D coordinates, long delay)
	{
		shouldRun = new AtomicBoolean(true);
		this.id = id;
		status = VehicleStatus.AVAILABLE;
		this.coordinates = coordinates;
		this.delay = delay;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "** Vehicle ID #" + this.getId() + 
				"\nVehicle Status: " + this.getStatusString() + 
				"\nVehicle Coordinates: " + this.getPrintedCoordinates() + "**";
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		while (shouldRun.get())
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
					System.out.println(this.toString());
					count = 0;
				}
				
				// grab the first coordinate in the route and remove it after that.
				coordinates = route.get(0);
				route.remove(0);
				
				// when the route is empty set the vehicle status to arrived
				if (route.isEmpty())
				{
					setStatus(VehicleStatus.ARRIVED);
				}
				try {
					// sleep one second and add to count same number of seconds
					Thread.sleep(1000);
					count += 1000;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			/*
			 * This sections pertains to wen the vehicle does not have a route
			 */
			if (status == VehicleStatus.AVAILABLE || status == VehicleStatus.ARRIVED)
			{
				// if the destination list has more items in it, get the next route
				if(destinationList.size() > 0)
				{
					getNextRoute();
				}
				else
				{
					// otherwise print toString/status and make sure the status is available and not arrived
					System.out.println(this.toString());
					setStatus(VehicleStatus.AVAILABLE);
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} 
		} // end of shouldRun while loop
		
		System.out.println("Vehicle #" + getId() + " exiting...");
	}

	public int getId()
	{
		return this.id;
	}

	public VehicleRunnable setId(int id) 
	{
		this.id = id;
		return this;
	}

	public VehicleStatus getStatus()
	{
		return this.status;
	}

	public VehicleRunnable setStatus(VehicleStatus status) 
	{
		this.status = status;
		return this;
	}

	public String getStatusString()
	{
		return this.status.toString();
	}

	public Point2D getCoordinates()
	{
		return this.coordinates;
	}

	public VehicleRunnable setCoordinates(Point2D coordinates) 
	{
		this.coordinates = coordinates;
		return this;
	}

	public String getPrintedCoordinates()
	{
		return "(" + this.coordinates.getX() + ", " + this.coordinates.getY() + ")";
	}

	public List<Point2D> getRoute()
	{
		return this.route;
	}

	public VehicleRunnable setRoute(List<Point2D> route) 
	{
		this.route = route;
		return this;
	}

	public String getRouteString()
	{
		String routeString = "Vehicle #" + this.getId() + " Route Start: \n";
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
		Point2D coords = new Point2D.Double();
		if (route.size() > 0)
		{
			coords = route.get(0);
		}
		return coords;
	}

	public Point2D getRouteEndCoordinates()
	{
		Point2D coords = new Point2D.Double();
		if (route.size() > 0)
		{
			coords = route.get((route.size() - 1));
		}
		return coords;
	}

	public String getRouteStartAndEndAddress() 
	{
		return "Vehicle #" + this.getId() + 
				"\nRoute Start: " + RouteDispatcher.reverseGeocoding(this.getRouteStartCoordinates()) + 
				"\nRoute End: " + RouteDispatcher.reverseGeocoding(this.getRouteEndCoordinates());
	}

	public List<Point2D> getDestinationList() 
	{
		return destinationList;
	}

	public VehicleRunnable setDestinationList(List<Point2D> list)
	{
		destinationList = list;
		return this;
	}

	public VehicleRunnable addPointToDestinationList(Point2D newDest)
	{
		destinationList.add(newDest);
		return this;
	}
	
	public VehicleRunnable addAddressToDestinationList(String address)
	{
		Point2D point = RouteDispatcher.forwardGeocoding(address);
		addPointToDestinationList(point);
		return this;
	}

	public void getNextRoute()
	{
		if(destinationList.size() > 0)
		{
			Point2D newDestination = getDestinationList().get(0);
			Point2D currentLocation = getCoordinates();
			route = RouteDispatcher.getRouteFromCoordinates(currentLocation, newDestination);
			getDestinationList().remove(0);
			setStatus(VehicleStatus.IN_TRANSIT);
		}
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleRunnable that = (VehicleRunnable) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
