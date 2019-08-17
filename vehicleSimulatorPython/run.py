# Author: Amanda Lewis
# Program: run.py
# Purpose: to test and simulate real vehicles traveling routes.
# this program uses multithreading and the ThreadPoolExecutor to manage threads

import vehicle
from vehicle import Vehicle
from vehicle import Status
import json
import fleet
import threading

# instantiate a fleet
vehicleList = fleet.Fleet(1)

def main():
    
    try: 
        answer = 0
        
        print("Welcome to the terminal tester for the vehicle simulator")
        while answer != 2:
            validAnswer = False
            print("What would you like to do?\n1. Create a vehicle \n2. Exit ")
            try:
                answer = int(input())
                if answer >= 1 and answer <= 2:
                    validAnswer = True
                else:
                    print("Please pick a number between 1 and 3")
            except ValueError as e:
                print("Error: " + str(e))
            if validAnswer:
                if answer == 1:
                    createVehicle()

    # the except catches all keyboard exceptions and shuts down the threads
    except KeyboardInterrupt as e:
        killAllThreads()
        print("\n^C received,...")
    finally: 
        print("Shutting down the vehicle sim...")

# this function asks for user input for an ID # and then creates a vehicle and adds it to a vehicle list
# it also creates a new thread with the vehicle's updating supply method
def createVehicle():
    print("Would you like to specify the ID of the vehicle? IDs must be unique to each vehicle you create.")
    valid = False
    id = 0
    # a while loop with a try catch to validate the user input until they enter something correct.
    while valid == False:
        try:
            id = int(input())
            valid = True
        except Valuerror as e:
            print("Error: You did not enter a number" + str(e))
    v1 = vehicle.Vehicle(ID=id, status=Status.AVAILABLE)
    vehicleList.add(v1)
    v1.start()

# this function gives a vehicle a static route to iterate over
def travelARoute():
    if vehicleList.getSize() > 0:
        print("Which vehicle would you like to choose?")
        # print the list of vehicles
        vehicleList.printVehicles()
        # validate user input, let them choose a vehicle.
        valid = False
        while valid == False:
            try:
                position = int(input())
                if position >= 0 and position < vehicleList.getSize():
                    valid = True
                else:
                    print("Not a valid number. Please pick a number between 0 and " + str(vehicleList.getSize() - 1))
            except ValueError as e:
                print("Error: You did not enter a number" + str(e))
        picked: Vehicle = vehicleList.get(position)
        print("Giving " + str(picked) + " a route now...")
        route = [[-97.783527,30.276797],[-97.781113,30.274913],[-97.781471,30.274012],[-97.784092,30.272505],[-97.783703,30.271474],[-97.780266,30.269469],[-97.777566,30.268892],[-97.787723,30.264493],[-97.803211,30.2554],[-97.805906,30.25183],[-97.806332,30.25012],[-97.805868,30.247941],[-97.802777,30.245269],[-97.797066,30.23447],[-97.784981,30.228409],[-97.771467,30.227306],[-97.769153,30.226241],[-97.764211,30.22249],[-97.758166,30.232976],[-97.757113,30.231396],[-97.75399,30.229393],[-97.753351,30.229637]]
        picked.setRoute(route)
    else:
        print("No vehicles in fleet!")

def killAllThreads():
    for index in range (0, len(vehicleList)):
        vehicleList.get(index).kill()
        vehicleList.get(index).join()
main()