package routing;

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
 * Created on 8-17-2019
 */
public class RouteDispatcher {

	private final static String MAPBOX_TOKEN = "pk.eyJ1IjoiYWxld2lzMyIsImEiOiJjanJsZnY3eG8wODZ6M3lyMjNkbjI0djBmIn0.G-d-49VqmS1MkmucwhzmJg";
	private final static String MAPBOX_GEOCODING_URL = "https://api.mapbox.com/geocoding/v5/mapbox.places/";
	private final static String MAPBOX_ROUTING_URL = "https://api.mapbox.com/directions/v5/mapbox/driving/";
	private final static String BBOX = "-98.10986697734825,30.0224906564967,-97.3622527256841,30.73727958749211";  //bbox around greater Austin area
	private final static Point2D NOT_FOUND = new Point2D.Double(-1, -1);
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
		List<Point2D> route = new ArrayList<Point2D>();
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
	 * Point2D starting: the starting coordinate of the route
	 * Point2D destination: the destination coordinate of the route
	 * 
	 * Returns: 
	 * The distance from the start to the destination
	 */
	public static Double getDistanceFromCoordinates(Point2D starting, Point2D destination)
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
		JSONArray legs = (JSONArray) ((JSONObject) ((JSONArray) jsonObject.get("routes")).get(0)).get("legs");
		double distance;
		if ((((JSONObject) legs.get(0)).get("distance")).getClass().equals(Long.class))
		{
			long distanceLong = (long) ((JSONObject) legs.get(0)).get("distance");
			distance = (double) distanceLong;
		}
		else
		{
			distance = (double) ((JSONObject) legs.get(0)).get("distance");
		}

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
		if (!features.isEmpty()) 
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
	 * The coordinates address
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
		JSONArray features = (JSONArray) jsonObject.get("features");
		String address = (String) ((JSONObject) features.get(0)).get("place_name");
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
}
