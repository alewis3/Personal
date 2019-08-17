# Author: Amanda Lewis 
# Program: Fleet.py simulates a fleet of vehicles by storing them in an array

import json
import requests
from vehicle import Vehicle

class Fleet(object):

    # default constructor that takes no arguments
    def __init__(self, fID):
        self.size = 0
        self.fleet = []
        self.fleetID = fID

    # overriding len method
    def __len__(self):
        return self.size

    # overriding iter method
    def __iter__(self):
        yield self.size
        yield self.fleet

    # overriding equals method
    def __eq__(self, other):
        """

        :type other: Fleet
        """
        retVal = True
        try:
            if type(self) != type(other):
                retVal = False
            elif self.getFleetID() != other.getFleetID():
                retVal = False
        except AttributeError as e:
            print("Error: " + str(e))
            retVal = False
        return retVal

    def setFleetID(self, fID):
        self.fleetID = fID

    def getFleetID(self):
        return self.fleetID

    def printVehicles(self):
        for vehicle in self.fleet:
            print(str(self.find(vehicle)) + ") " + str(vehicle))

    # getSize returns the current size of the fleet
    def getSize(self):
        return self.size

    def getFleet(self):
        return self.fleet

    def add(self, vehicle):
        if self.contains(vehicle):
            print("Vehicle could not be added due to duplicate vehicle")
        else:
            self.getFleet().append(vehicle)
            self.size += 1

    def contains(self, vehicle):
        """

        :type vehicle: Vehicle
        """
        retVal = False
        for item in self.getFleet():
            if item == vehicle:
                retVal = True
                break
        return retVal

    def get(self, position):
        if(position > self.getSize() or position < 0):
            retVal = None
        else:
            count = 0
            for item in self.getFleet():
                if count == position:
                    retVal = item
                count += 1
        return retVal

    def find(self, vehicle):
        if self.contains(vehicle):
            count = 0
            for item in self.getFleet():
                if item == vehicle:
                    retVal = count
                    break
                count += 1
        else:
            retVal = -1
        return retVal

    def remove(self, vehicle):
        if self.find(vehicle) == -1:
            retVal = None
            print("Vehicle could not be removed because it was not found in the fleet")
        else:
            retVal = self.get(self.find(vehicle))
            self.getFleet().remove(vehicle)
            self.size -= 1
        return retVal
