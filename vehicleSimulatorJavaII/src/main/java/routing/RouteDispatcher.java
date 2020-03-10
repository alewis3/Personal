package routing;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/*
 * @author Amanda Lewis
 * Refactored on 2-11-2020
 */
public class RouteDispatcher {

	private final static String MAPBOX_TOKEN = "pk.eyJ1IjoiYWxld2lzMyIsImEiOiJjanJsZnY3eG8wODZ6M3lyMjNkbjI0djBmIn0.G-d-49VqmS1MkmucwhzmJg";
	private final static String MAPBOX_GEOCODING_URL = "https://api.mapbox.com/geocoding/v5/mapbox.places/";
	private final static String MAPBOX_ROUTING_URL = "https://api.mapbox.com/directions/v5/mapbox/driving/";
	private final static String MAPBOX_OPTIMIZATION_URL = "https://api.mapbox.com/optimized-trips/v1/mapbox/driving/";
	private final static String BBOX = "-98.10986697734825,30.0224906564967,-97.3622527256841,30.73727958749211";  //bbox around greater Austin area
	private final static Point2D NOT_FOUND = new Point2D.Double(-1, -1);

	/*
	 * Parameters:
	 * String starting
	 * String ending1
	 * String ending2
	 *
	 * Returns:
	 * The angle difference between the ending addresses
	 */
	public static double getAngleBetweenTwoAddressPairs(String starting, String ending1, String ending2)
	{
		Point2D startingPoint = forwardGeocoding(starting);
		Point2D endingPoint1 = forwardGeocoding(ending1);
		Point2D endingPoint2 = forwardGeocoding(ending2);
		if (startingPoint == NOT_FOUND || endingPoint1 == NOT_FOUND || endingPoint2 == NOT_FOUND)
		{
			return -1;
		}
		else
		{
			return getAngleBetweenTwoCoordinatePairs(startingPoint, endingPoint1, endingPoint2);
		}
	}

	/*
	 * Parameters:
	 * Point2D starting
	 * Point2D ending1
	 * Point2D ending2
	 *
	 * Returns:
	 * The angle difference between the ending points
	 */
	public static double getAngleBetweenTwoCoordinatePairs(Point2D starting , Point2D ending1, Point2D ending2)
	{
		Point2D direction1 = getDirectionVector(starting, ending1);
		Point2D direction2 = getDirectionVector(starting, ending2);
		return getAngleBetweenTwoVectors(direction1, direction2);
	}

	/*
	 * Parameters:
	 * Point2D starting
	 * Point2D ending
	 *
	 * Returns:
	 * A Unit vector representing the direction from starting to ending.
	 */
	public static Point2D getDirectionVector(Point2D starting, Point2D ending)
	{
		Double distance = getDistanceFromCoordinates(starting, ending);
		Point2D vector = new Point2D.Double((ending.getX() - starting.getX()), (ending.getY() - starting.getY()));
		Point2D directionVector = new Point2D.Double(vector.getX()/distance, vector.getY()/distance);
		return directionVector;
	}

	/*
	 * Parameters:
	 * Point2D vector1
	 * Point2D vector2
	 *
	 * Returns:
	 * a double representing the angle between the two vectors.
	 */
	public static double getAngleBetweenTwoVectors(Point2D vector1, Point2D vector2)
	{
		double magnitude1 = getMagnitudeOfAVector(vector1);
		double magnitude2 = getMagnitudeOfAVector(vector2);
		double angle = Math.acos(((vector1.getX() * vector2.getX()) + (vector1.getY() * vector2.getY())) / (magnitude1 * magnitude2));
		double deg = Math.toDegrees(angle);
		return deg;
	}

	/*
	 * Parameters:
	 * Point2D vector
	 *
	 * Returns:
	 * The magnitude (length) of the vector
	 */
	public static double getMagnitudeOfAVector(Point2D vector)
	{
		return Math.sqrt(Math.pow(vector.getX(), 2) + Math.pow(vector.getY(), 2));
	}

	/*
	 * Parameters: 
	 * String starting: the starting address of the route
	 * String destination: the destination address of the route
	 * 
	 * Returns: 
	 * A route from starting to destination
	 */
	public static List<Point2D> getRouteFromAddresses(String starting, String destination)
	{
		Point2D startingPoint = forwardGeocoding(starting);
		Point2D endingPoint = forwardGeocoding(destination);

		if (startingPoint == NOT_FOUND || endingPoint == NOT_FOUND)
		{
			return new ArrayList<Point2D>();
		}
		return getRouteFromCoordinates(startingPoint, endingPoint);
	}

	/*
	 * Parameters: 
	 * Point2D starting: the starting coordinate of the route
	 * Point2D destination: the destination coordinate of the route
	 * 
	 * Returns: 
	 * A route from starting to destination
	 */
	public static List<Point2D> getRouteFromCoordinates(Point2D starting, Point2D destination)
	{
		String url = buildRoutingURL(starting, destination);
		String response = getRequest(url);
		JSONParser p = new JSONParser();
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = (JSONObject) p.parse(response);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		List<Point2D> route = new ArrayList<>();
		JSONArray steps = (JSONArray) ((JSONObject) ((JSONArray) ((JSONObject) ((JSONArray) jsonObject.get("routes")).get(0)).get("legs")).get(0)).get("steps");
		for(int j = 0; j < steps.size(); j++)
		{
			JSONObject obj = (JSONObject) steps.get(j);
			JSONArray coordinates = (JSONArray) ((JSONObject) obj.get("geometry")).get("coordinates");
			for(int x = 0; x < coordinates.size(); x++)
			{
				JSONArray coordinate = (JSONArray) coordinates.get(x);
				Point2D point = new Point2D.Double((Double) coordinate.get(0), (Double) coordinate.get(1));
				route.add(point);
			}

		}
		return route;
	}

	/*
	 * Parameters:
	 * List<String> addressList
	 *
	 * Returns:
	 * A list of points representing a route with all the coordinates on it.
	 */
	public static List<Point2D> getOptimizedRouteFromAddresses(List<String> addressList)
	{
		List<Point2D> pointList = getPointListFromAddresses(addressList);
		if (pointList.contains(NOT_FOUND))
		{
			return new ArrayList<Point2D>();
		}
		List<Point2D> route = getOptimizedRouteFromCoordinates(pointList);
		return route;
	}

	/*
	 * Parameters:
	 * List<Point2D> destinationList
	 *
	 * Returns:
	 * An optimized route that includes all the points in the list
	 *
	 * Throws:
	 * RuntimeException if the destinationList.size() > 12
	 */
	public static List<Point2D> getOptimizedRouteFromCoordinates(List<Point2D> destinationList)
	{
		if (destinationList.size() > 12)
		{
			throw new RuntimeException("List cannot have more than 12 coordinates.");
		}
		String url = buildOptimizationURL(destinationList);
		System.out.println(url);
		String response = getRequest(url);
		JSONParser p = new JSONParser();
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = (JSONObject) p.parse(response);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		List<Point2D> route = new ArrayList<>();
		JSONArray steps = (JSONArray) ((JSONObject) ((JSONArray) ((JSONObject) ((JSONArray) jsonObject.get("trips")).get(0)).get("legs")).get(0)).get("steps");
		for(int j = 0; j < steps.size(); j++)
		{
			JSONObject obj = (JSONObject) steps.get(j);
			JSONArray coordinates = (JSONArray) ((JSONObject) obj.get("geometry")).get("coordinates");
			for(int x = 0; x < coordinates.size(); x++)
			{
				JSONArray coordinate = (JSONArray) coordinates.get(x);
				Point2D point = new Point2D.Double((Double) coordinate.get(0), (Double) coordinate.get(1));
				route.add(point);
			}

		}
		return route;
	}

	/*
	 * Parameters: 
	 * Point2D starting: the starting coordinate of the route
	 * Point2D destination: the destination coordinate of the route
	 * 
	 * Returns: 
	 * The distance from the start to the destination
	 */
	public static Double getDistanceFromCoordinates(Point2D starting, Point2D destination)
	{
		// calculate using the distance formula
		// sqrt((x_2 - x_1)^2 + (y_2 - y_1)^2)
		Double distance = Math.sqrt(Math.pow((destination.getX() - starting.getX()), 2) + Math.pow((destination.getY() - starting.getY()), 2));

		return distance;
	}

	/*
	 * Parameters: 
	 * String address: the address to convert to coordinates
	 * 
	 * Returns: 
	 * The address's location represented by (long, lat) coordinates
	 */
	public static Point2D forwardGeocoding(String address)
	{
		String url = buildGeocodingURLFromAddress(address);
		String response = getRequest(url);
		JSONParser p = new JSONParser();
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = (JSONObject) p.parse(response);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		JSONArray features = (JSONArray) jsonObject.get("features");
		if (features != null && !features.isEmpty()) 
		{
			JSONArray center = (JSONArray) ((JSONObject) features.get(0)).get("center");
			return new Point2D.Double((Double) center.get(0), (Double) center.get(1));
		}
		else
		{
			return new Point2D.Double(-1, -1);
		}
	}

	/*
	 * Parameters: 
	 * Point2D coordinates: the coordinates to convert to address in (long, lat) format
	 * 
	 * Returns: 
	 * The coordinates address, or null if it could not be found.
	 */
	public static String reverseGeocoding(Point2D coordinates)
	{
		String url = buildGeocodingURLFromPoint(coordinates);
		String response = getRequest(url);
		JSONParser p = new JSONParser();
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = (JSONObject) p.parse(response);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String address;
		JSONArray features = (JSONArray) jsonObject.get("features");
		if (features.size() > 0) {
			address = (String) ((JSONObject) features.get(0)).get("place_name");
		}
		else {
			address = null;
		}
		return address;

	}

	/*
	 * Parameters: 
	 * String url: the full url to make a get request to including parameters
	 * 
	 * Returns: 
	 * A string containing the response body
	 * 
	 * Throws: 
	 * Runtime Exception if the response code is not 200
	 */
	private static String getRequest(String url)
	{
		StringBuffer response = new StringBuffer();
		try {
			URL obj = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

			connection.setRequestMethod("GET");
			connection.connect();

			int responseCode = connection.getResponseCode();

			if (responseCode == 200)
			{
				BufferedReader in = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}
			else
			{
				throw new RuntimeException("Error: Response code is " + responseCode + " for request " + url);
			}
		} catch (IOException e) {
			System.out.println("Error messages: " + e.getMessage());
		}
		return response.toString();
	}

	/*
	 * Parameters:
	 * List<String> addressList
	 *
	 * Returns:
	 * a list of points forward geocoded from the addresses received.
	 */
	private static List<Point2D> getPointListFromAddresses(List<String> addressList)
	{
		List<Point2D> pointList = new ArrayList<>(addressList.size());
		for (int i = 0; i < addressList.size(); i++)
		{
			Point2D point = forwardGeocoding(addressList.get(i));
			pointList.add(point);
		}
		return pointList;
	}

	/*
	 * Parameters: 
	 * Point2D point: the point to include in the geocoding URL
	 * 
	 * Returns: 
	 * The request string with specified coordinates
	 */
	private static String buildGeocodingURLFromPoint(Point2D point) 
	{
		return MAPBOX_GEOCODING_URL + point.getX() + "," + point.getY() +
				".json?access_token=" + MAPBOX_TOKEN;
	}

	/*
	 * Parameters: 
	 * String address: the address to include in the geocoding URL
	 * 
	 * Returns: 
	 * The request string with specified address prepared with prepareAddress (See below)
	 * Note: this url will include a bbox around the greater Austin, Texas area so vehicles don't roam too far
	 */
	private static String buildGeocodingURLFromAddress(String address)
	{
		String preparedAddress = prepareAddress(address);
		return MAPBOX_GEOCODING_URL + preparedAddress + ".json?bbox=" + BBOX + "&access_token=" + MAPBOX_TOKEN;
	}

	/*
	 * Parameters: 
	 * String address: The address to prepare for an API call
	 * 
	 * Returns: 
	 * The specified address with semicolons removed and spaces converted to "%20"
	 */
	private static String prepareAddress(String address)
	{
		char semicolon = ";".charAt(0);
		String temp = "";
		for(int l = 0; l < address.length(); l++)
		{
			String charReplace = "";
			char ch = address.charAt(l);
			if (Character.isWhitespace(ch))
			{
				charReplace = "%20";
			}
			else if (ch != semicolon)
			{
				charReplace = Character.toString(ch);
			}
			temp += charReplace;
		}
		return temp;
	}

	/*
	 * Parameters: 
	 * Point2D starting: the starting point to include in the geocoding URL	 
	 * Point2D destination: the destination point to include in the geocoding URL
	 * 
	 * Returns: 
	 * The request string with specified coordinates
	 */
	private static String buildRoutingURL(Point2D starting, Point2D destination) 
	{
		return MAPBOX_ROUTING_URL + starting.getX() + "," + starting.getY() +
				";" + destination.getX() + "," + destination.getY() +
				"?geometries=geojson&steps=true&access_token=" + MAPBOX_TOKEN;
	}

	private static String buildOptimizationURL(List<Point2D> destinationList)
	{
		String retVal = "";
		if (destinationList.size() > 12)
		{
			retVal = null;
		}
		retVal += MAPBOX_OPTIMIZATION_URL;

		for(int i = 0; i < destinationList.size(); i++)
		{
			Point2D point = destinationList.get(i);
			retVal += point.getX() + "," + point.getY();
			if (i != destinationList.size() - 1)
			{
				retVal += ";";
			}
		}
		retVal += "?geometries=geojson&steps=true&access_token=" + MAPBOX_TOKEN;
		return retVal;
	}
}
