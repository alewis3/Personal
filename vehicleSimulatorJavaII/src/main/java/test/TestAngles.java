package vehicle;

import routing.RouteDispatcher;

public class TestAngles {

    public static void main(String[] args)
    {
        String starting = "3001 S. Congress Ave. Austin TX, 78704";
        String ending1 = "1913 W. 40th St. Austin TX 78731";
        String ending2 = "6600 Delmonico Dr. Austin TX 78759";

        double angle = RouteDispatcher.getAngleBetweenTwoAddressPairs(starting, ending1, ending2);
        System.out.println("The angle between " + ending1 + " and " + ending2 + " from " + starting + " is: \n" + angle);
    }
}
