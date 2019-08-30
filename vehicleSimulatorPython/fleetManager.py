# Author: Amanda Lewis 
# Program: fleetManager.py simulates a list of fleets by storing them in an array

import json
import requests
from vehicle import Vehicle
from fleet import Fleet

class FleetManager(object):

    # default constructor that takes no arguments
    def __init__(self):
        self.size = 0
        self.fleetManager = []

    # overriding iter method
    def __iter__(self):
        yield self.size
        yield self.fleetManager

    def printFleets(self):
        for fleet in self.fleetManager:
            print(str(self.find(fleet)) + ") " + str(fleet))

    # getSize returns the current size of the fleet manager
    def getSize(self):
        return self.size

    # getFleetManager returns the whole list of fleets
    def getFleetManager(self):
        return self.fleetManager

    # add will add a fleet to the fleet manager, provided it is not already in the fleet
    def add(self, fleet : Fleet):
        if self.contains(fleet):
            print("Fleet could not be added due to duplicate fleet")
        else:
            self.getFleetManager().append(fleet)
            self.size += 1

    # contains will check to see if the fleet sent in is in the fleet manager
    # and will return true if found, false if not
    def contains(self, fleet):
        """

        :type vehicle: fleet
        """
        retVal = False
        for item in self.getFleetManager():
            if item == fleet:
                retVal = True
                break
        return retVal

    # get will take in a position and will return none if the position is invalid
    # and the vehicle at that position if it is valid
    def get(self, position):
        if(position > self.getSize() or position < 0):
            retVal = None
        else:
            count = 0
            for item in self.getFleetManager():
                if count == position:
                    retVal = item
                count += 1
        return retVal

    def doesIDExist(self, fID):
        retVal = False
        for item in self.getFleetManager():
            if fID == item.getFleetID():
                retVal = True
                break
        return retVal

    def getFleetByID(self, fID):
        retVal = None
        if self.doesIDExist(fID):
            for item in self.getFleetManager():
                if fID == item.getFleetID():
                    retVal = item
                    break
        return retVal

    # find will take in a fleet and will return the position it is found at, 
    # and -1 if it is not found
    def find(self, fleet):
        if self.contains(fleet):
            count = 0
            for item in self.getFleetManager():
                if item == fleet:
                    retVal = count
                    break
                count += 1
        else:
            retVal = -1
        return retVal

    # remove will take a fleet and remove that fleet in the list if it is found
    # and none if it is not found
    def remove(self, fleet):
        if self.find(fleet) == -1:
            retVal = None
            print("Fleet could not be removed because it was not found in the Fleet Manager")
        else:
            retVal = self.get(self.find(fleet))
            self.getFleetManager().remove(fleet)
            self.size -= 1
        return retVal
