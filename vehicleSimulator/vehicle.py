# Author: Amanda Lewis
# Program: Vehicle.py
# Purpose: A class of vehicle objects

import random
import requests
import time
import json
from enum import Enum, auto
import ast
from threading import Thread


DEFAULT_ID = 99
DEFAULT_LAT = 30.22968822927 # St. Edward's lat
DEFAULT_LONG = -97.753438393843 # St. Edward's Long

DEFAULT_ROUTE = []
API_CALL = "https://team22.supply.softwareengineeringii.com/supply"

class Status(Enum):
    AVAILABLE = "AVAILABLE"
    IN_TRANSIT = "IN_TRANSIT"
    ARRIVED ="ARRIVED"
    IN_MAINENTENANCE = "IN_MAINENTENANCE"

DEFAULT_STATUS = Status.AVAILABLE


minLat = 29.542674611627803             # bbox
maxLat = 30.922331067658433             # around
minLong = -98.57987255420727             # Austin
maxLong = -96.68435437706239             # TX

# Vehicle class to simulate a vehicle object
class Vehicle(Thread):


    def __init__(self, group=None, target=None, name=None, args=(), kwargs=None, 
            daemon=False, ID=DEFAULT_ID, latitude=DEFAULT_LAT, longitude=DEFAULT_LONG, 
            status=DEFAULT_STATUS.value):

        self.setVehicleID(ID)
        self.setCoords(latitude, longitude)
        self.status = status
        self.setRoute(DEFAULT_ROUTE)
        super(Vehicle, self).__init__(group=group, target=target, name=name,args=args, kwargs=kwargs, daemon=daemon)
        self.args = args
        self.kwargs = kwargs


    # overriding hash method
    def __hash__(self):
        return hash("vehicle"*self.getVehicleID())

    # overriding equals method
    def __eq__(self, other):
        """

        :type other: Vehicle
        """
        retVal = True
        try:
            if self.getVehicleID() != other.getVehicleID():
                retVal = False
            elif type(self) != type(other):
                retVal = False
        except AttributeError as e:
            print("Error: " + str(e))
            retVal = False
        return retVal

    # overriding str method
    def __str__(self):
        temp = "Vehicle ID: " + str(self.getVehicleID()) + " Current coordinates: (" + str(self.getLatitude()) + "," + str(self.getLongitude()) + ") Status: " + str(self.getStatus())
        return temp

    # a function to set the vehicleID
    def setVehicleID(self, ID):
        if ID > 0:
            self.vID = ID
        else:
            self.vID = DEFAULT_ID

    # a function to update the coordinates of the vehicle. takes an array of lat/long coordinates
    def setCoords(self, latitude, longitude):
        if latitude > maxLat or latitude < minLat or longitude > maxLong or longitude < minLong:
            self.lat = DEFAULT_LAT
            self.long = DEFAULT_LONG
        else:
            self.lat = latitude
            self.long = longitude

    # a function to set the status of the vehicle. vehicles can have 1 of 4 statuses: available, in transit, arrived, or in maintenance
    # available is the default status and statuses are in the status enum
    def setStatus(self, status):
        # This if statement makes sure status is one of the four in the Status enum
        if status == Status.AVAILABLE or status == Status.IN_TRANSIT or status == Status.ARRIVED or status == Status.IN_MAINENTENANCE:
            self.status = status.value
        else:
            self.status = Status.AVAILABLE.value

    # a function to set the route attribute of a vehicle. routes are lists of lists of coordinates
    def setRoute(self, myRoute):
        if type(myRoute) == type(DEFAULT_ROUTE):
            self.route = myRoute
        else:
            self.route = DEFAULT_ROUTE

    # a function to get the vehicle ID of this vehicle
    def getVehicleID(self):
        return self.vID

    # a function to return the current coordinates of the vehicle
    def getCoords(self):
        return [self.getLongitude(), self.getLatitude()]

    # a function to get just the latitude of the vehicle
    def getLatitude(self):
        return self.lat

    # a function to get just the longitude of the vehicle
    def getLongitude(self):
        return self.long

    # a function to return the status at a given moment
    def getStatus(self):
        return self.status

    # a function to return the attribute of routeInformation, "route"
    def getRoute(self):
        return self.route

    # a function that receives nothing and returns the starting coordinates of the route
    def getRouteStartCoordinates(self):
        if len(self.getRoute()) > 0:
            retVal = self.getRoute()[0]
        else:
            retVal = [0,0]
        return retVal

    # a function that receives nothing and returns the ending coordinates of the route,
    def getRouteEndCoordinates(self):
        if len(self.getRoute()) > 0:
            retVal = self.getRoute()[len(self.getRoute()) - 1]
        else:
            retVal = [0, 0]
        return retVal

    # a function that receives nothing and starts the vehicle on its path
    # and updates the vehicle's location every 2 seconds
    # This function also makes a request to the supply side API every 10 seconds
    # updating the vehicle's attributes in the database
    def run(self):
        self.shouldRun = True
        while self.shouldRun:
            if self.getStatus() == Status.ARRIVED.value:
                self.setStatus(Status.AVAILABLE)
            # update supply side will run while the vehicle is available
            self.updateSupplySide()

            # if the vehicle has gone from available to in transit, it also has been given a route
            
            # a counter to count to 10 because the coordinates increment every second
            counter = 0  
            while isEmpty(self.getRoute()) == False:
                currentCoords = self.getRoute()[0]
                # a check to make sure the current coordinates is an empty list
                if len(currentCoords) != 0:
                    self.setCoords(currentCoords[1],currentCoords[0])
                    self.getRoute().remove(self.getCoords())
                    counter += 1
                    if counter == 10:  # when counter reaches 10, 10 seconds have passed and it is time to send an update
                        counter = 0
                        self.makeRequest()
                    time.sleep(1)
                else:
                    self.getRoute().remove([])
            if self.getStatus() == Status.IN_TRANSIT.value:
                # set status to arrived to let the supply side know the route is over
                self.setStatus(Status.ARRIVED)
                self.makeRequest()
                time.sleep(10)
            self.setStatus(Status.AVAILABLE)
            self.makeRequest()
        print("Vehicle " + str(self.getVehicleID()) + " shutting down...")

    def kill(self):
        self.shouldRun = False


    # updateCoords() receives nothing and will call the helper method getInc() and add or subtract
    # the amount from the selected latitude or longitude and update the coordinates of the vehicle that was sent in
    def updateCoords(self):
        currentCoords = self.getCoords()  # get current coordinates
        arr = getInc()  # get array
        amt = arr[0]   # get float amt
        index = arr[1]  # get index
        posOrNeg = arr[2]  # get positive or negative bit
        if posOrNeg == 0:  # increment or decrement according to the pos or neg bit
            currentCoords[index] = currentCoords[index] + amt
        else:
            currentCoords[index] = currentCoords[index] - amt

        # these if/elif stmts make sure the vehicle is within the bounding box around austin.
        if currentCoords[0] > maxLong:
            currentCoords[0] = maxLong
        elif currentCoords[0] < minLong:
            currentCoords[0] = minLong
        elif currentCoords[1] > maxLat:
            currentCoords[1] = maxLat
        elif currentCoords[1] < minLat:
            currentCoords[1] = minLat

        self.setCoords(currentCoords[1], currentCoords[0])

    # This function receives nothing and updates the coordinates of the vehicle periodically and also makes
    # a post request to the supply side API every 10 seconds until the status of the vehicle is no longer available
    def updateSupplySide(self):

        while self.getStatus() == Status.AVAILABLE.value and self.shouldRun:
            self.makeRequest()
            time.sleep(10)

    # make request will assemble data about the vehicle ad make a request to the supply side
    def makeRequest(self):
        data = "{\"vehicleID\": " + str(self.getVehicleID()) + ",\"coordinate\": " + str(self.getCoords()) + ",\"status\":\"" + str(self.getStatus()) + "\"}"

        jsonObj = json.loads(data)
        url = API_CALL + "/vehicleUpdate"
        try: 
            response = requests.post(url, json=jsonObj).json()

            if response["received"] == "ROUTE" and self.getStatus() == Status.AVAILABLE.value:
                myRoute = ast.literal_eval(response["route"])
                self.setRoute(myRoute)
                self.setStatus(Status.IN_TRANSIT)
            print("Request Made with json obj" + str(jsonObj))
        except requests.ConnectionError as e: 
            print("Error: could not connect to server. Msg:" + str(e))



# getInc() is a helper function to the updateCoordsMethod in the vehicleClassrecieves nothing and gets a random float to increment by, a random bit (0 or 1) for the index, and a random bit
# to determine if the float should be added or subtracted.
def getInc():
    num = random.random() / 25  # get a random float to add to the lat or long and divide by 10 to make it small
    index = random.randint(0, 1)  # get the index to add num to lat or long
    positive = random.randint(0,1)  # get a random number to determine if the num will be added or subtracted
    return [num, index, positive]  # (positive = 0, negative = 1)

# a function to check an array for a specific item
def contains(array, item):
    retFound = False
    for arrayItem in array:
        if arrayItem == item:
            retFound = True
            break
    return retFound

# a function that receives an array and checks if it is empty
def isEmpty(array):
    if len(array) == 0:
        retVal = True
    else:
        retVal = False
    return retVal
