package vehicleManager;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import routing.RouteDispatcher;
import vehicle.VehicleRunnable;

public class VehicleManager {

	private List<VehicleRunnable> vehicles;
	private int vehicleCount;
	
	public VehicleManager()
	{
		vehicles = new ArrayList<VehicleRunnable>();
		vehicleCount = 0;
	}
	
	public VehicleRunnable get(int index)
	{
		return vehicles.get(index);
	}
	
	public void add(VehicleRunnable vehicle)
	{
		vehicles.add(vehicle);
		vehicleCount++;
	}
	
	public Collection<VehicleRunnable> values()
	{
		Collection<VehicleRunnable> c = vehicles;
		return c;
	}
	
	public HashMap<Integer, VehicleRunnable> getClosestAvailable(Point2D point)
	{
		HashMap<Double,VehicleRunnable> closest = new HashMap<Double, VehicleRunnable>();
		closest.put(RouteDispatcher.getDistanceFromCoordinates(vehicles.get(0).getCoordinates(),  point), vehicles.get(0));
		
		
	}
}
