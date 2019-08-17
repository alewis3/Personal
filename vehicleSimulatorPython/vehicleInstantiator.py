# Author: Amanda Lewis
# Program: vehicleInstantiator.py
# The purpose of this program is to make a call to the supply side to receive all vehicle info 
# from the database and then sort each vehicle into their respective fleets and start a new 
# thread for each vehicle to update the supply side
# this program uses the theading package and daemon threads

from fleet import Fleet
from vehicle import Vehicle
from fleetManager import FleetManager
import threading
import threadManager
import requests

API_CALL = "https://team22.supply.softwareengineeringii.com/supply/instantiateVehicles"

fm = FleetManager()

# make the request
def getVehicleInfo():
    response = requests.post(API_CALL).json()
    #print(str(response)) # this should be a json object with the keys as the indicies and the values are another object with vehicle info in it
    return response

def main():
    
    try: 

        jsonDictionary = getVehicleInfo() # this returns a dictionary with the keys as indicies 0-n and values are vehicle objects
            # with keys vID, vLatitude, vLongitude, fleetID, and vState
        count = 0
        isRunning = True

        try: 
            while jsonDictionary[str(count)] != None:
                vehicleObj = jsonDictionary[str(count)]
                vID = vehicleObj["vID"]
                vLat = vehicleObj["vLatitude"]
                vLong = vehicleObj["vLongitude"]
                fleetID = vehicleObj["fleetID"]
                vState = vehicleObj["vState"]
                vLatFloat = float(vLat)
                vLongFloat = float(vLong)
                v1 = Vehicle(ID=vID, latitude=vLatFloat, longitude=vLongFloat, status=vState) # create new vehicles
                print("New Vehicle created with id ", str(vID))

                if fm.doesIDExist(fleetID): # if the fleet id is already in the fleet manager
                    fleet = fm.getFleetByID(fleetID) 
                    fleet.add(v1) # add the vehicle to the existing fleet
                    print("Vehicle added to fleet with fleetID: ", str(fleetID))
                else: 
                    newFleet = Fleet(fleetID) # otherwise create a new fleet w/ that fleetID
                    fm.add(newFleet)
                    newFleet.add(v1)
                    print("New fleet created for vehicle with fleetID: ", str(fleetID))

                v1.start()

                count += 1
        except KeyError as e:
            print("Key could not be found.")

        answer = 0
        print("All Vehicles instantiated")
        while isRunning:
            validAnswer = False
            print("What would you like to do?\n1. Exit ")
            try:
                answer = int(input())
                if answer == 1:
                    validAnswer = True
                else:
                    print("Please select '1' if you would like to exit")
            except ValueError as e:
                print("Error: " + str(e))
            if validAnswer:
                isRunning = False
                killAllThreads(fm)
    # the except catches all keyboard exceptions and shuts down the threads
    except KeyboardInterrupt as e:
        isRunning = False
        killAllThreads(fm)
        print("\n^C received,...")
    finally: 
        print("Shutting down the vehicle sim...")
    
def killAllThreads(fleetManager):
    for fIndex in range (0, fleetManager.getSize()):
        fleet = fleetManager.get(fIndex)
        for vIndex in range (0, fleet.getSize()):
            currentVehicle = fleet.get(vIndex)
            currentVehicle.kill()
            
    for fIndex in range (0, fleetManager.getSize()):
        fleet = fleetManager.get(fIndex)
        for vIndex in range (0, fleet.getSize()):
            currentVehicle = fleet.get(vIndex)
            currentVehicle.join()

main()