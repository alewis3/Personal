package vehicleManager;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import routing.RouteDispatcher;
import vehicle.Vehicle;

/*
 * @author Amanda Lewis
 * Created on 8-17-2019
 */
public class VehicleManager {

	private final static Point2D POINT_NOT_FOUND = new Point2D.Double(-1, -1);
	
	private List<Vehicle> vehicles;
	private int vehicleCount;

	public VehicleManager()
	{
		vehicles = new ArrayList<Vehicle>();
		vehicleCount = 0;
	}
	
	public int size()
	{
		return vehicleCount;
	}

	/*
	 * Get a vehicle object by index
	 */
	public Vehicle get(int index)
	{
		return vehicles.get(index);
	}

	/*
	 * Add a vehicle object
	 */
	public void add(Vehicle vehicle)
	{
		vehicles.add(vehicle);
		vehicleCount++;
	}
	
	/*
	 * Return a collection of the values in vehicles
	 */
	public Collection<Vehicle> values()
	{
		Collection<Vehicle> c = vehicles;
		return c;
	}
	
	/*
	 * Parameters: 
	 * String address: The address you would like to find the closest available vehicle to
	 * 
	 * Returns: 
	 * The index of the closest available vehicle
	 */
	public Integer getClosestAvailableFromAddress(String address)
	{
		Point2D point = RouteDispatcher.forwardGeocoding(address);
		if(point == POINT_NOT_FOUND)
		{
			return -1;
		}
		return getClosestAvailable(point);
	}

	/*
	 * Parameters: 
	 * Point2D point: the point you would like to find the closest available vehicle to
	 * 
	 * Returns: 
	 * The index of the closest available vehicle
	 */
	public Integer getClosestAvailable(Point2D point)
	{
		List<Integer> availableIndexList = getAvailableVehicles();
		int currentKey = -1;
		if (availableIndexList.size() > 0) 
		{
			Double currentMinDistance = RouteDispatcher.getDistanceFromCoordinates(vehicles.get(availableIndexList.get(0)).getCoordinates(), point);
			currentKey = availableIndexList.get(0);
			for (int v = 1; v < availableIndexList.size(); v++)
			{
				Double newVehiclesDistance = RouteDispatcher.getDistanceFromCoordinates(vehicles.get(availableIndexList.get(v)).getCoordinates(), point);
				if (currentMinDistance > newVehiclesDistance)
				{
					currentKey = v;
					currentMinDistance = newVehiclesDistance;
				}
			}
		} else
		{
			currentKey = getClosestFromRouteEndPoints(point);
		}
		return currentKey;

	}

	/*
	 * This function is for when no vehicles are available, but we still would like to optimize which vehicle the new route goes out to. 
	 * It works by starting to look at the vehicles that do not have a destination list/queue lined up already, then incrementing the 
	 * preffered dest list size by 1 if no vehicles have that preferred size. It will continue to look until it finds a suitable vehicle
	 * 
	 * Parameters: 
	 * Point2D point: the point you would like to find the closest available vehicle to
	 * 
	 * Returns: 
	 * The index of the closest vehicle based on where their route ends
	 */
	private Integer getClosestFromRouteEndPoints(Point2D point)
	{
		int currentKey = -1;
		int currentMinDistance = Integer.MAX_VALUE;
		int preferredDestListSize = 0;

		while (currentKey == -1)
		{
			for (int g = 0; g < vehicles.size(); g++)
			{
				Vehicle vehicle = vehicles.get(g);
				int destListSize = vehicle.getDestinationList().size();
				if (destListSize == preferredDestListSize)
				{
					Point2D end;
					if(destListSize == 0)
					{
						end = vehicle.getRouteEndCoordinates();
					} else
					{
						end = vehicle.getDestinationList().get(preferredDestListSize - 1);
					}
					Double vehicleDistance = RouteDispatcher.getDistanceFromCoordinates(end, point);
					if(vehicleDistance < currentMinDistance)
					{
						currentKey = g;
					}
				}
			}
			preferredDestListSize ++;
		}
		return currentKey;
	}

	/*
	 * This private function will return a list of the indices of the available vehicles being managed by this instance
	 */
	private List<Integer> getAvailableVehicles()
	{
		List<Integer> indexList = new ArrayList<Integer>(vehicles.size());
		for(int a = 0; a < vehicles.size(); a++) 
		{
			if (vehicles.get(a).getStatusString() == "AVAILABLE")
			{
				indexList.add(a);
			}
		}
		return indexList;
	}
}
