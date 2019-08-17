# Vehicle-Simulator
---
##Attributes of a vehicle: 
* *vehicleID*
* *vehicleLatitude*
* *vehicleLongitude*
* *vehicleStatus*
* *vehicleRoute*
---
##Constructor will take:
* *vehicleID*, or set to default 99
* *vehicleLatitude* or set to default St. Edward's Lat
* *vehicleLongitude* or set to default St. Edward's Long
* *vehicleStatus* or set to default status **AVAILABLE**
* *vehicleRoute* or set to default empty list of lists
---
##Methods:
* `setVehicleID(ID)`
* `setCoords(latitude, longitude)`
* `setStatus(status)`
* `setRoute(route)`
---
* `getVehicleID()`
* `getCoords()`
* `getLatitude()`
* `getLongitude()`
* `getStatus()`
* `getRoute()`
* `getRouteStartCoordinates()`
* `getRouteEndCoordinates()`
---
**Other functions:**
* `customerDone()`
* `go()`
* `makeRequest()`
* `updateCoords()`
* `updateSupplySide()`
---
**API Calls**
* */vehicleUpdate* will be called by the vehicle periodically to let the supply side know where it is at and its status. The supply side will respond with one of three options: 
* "*received*" : "*OK*" to be sent when the vehicle is **AVAILABLE** or **IN_TRANSIT** to let them know the request was received successfully. 
* "*received*" : "*ROUTE*" to be sent when the vehicle is **AVAILABLE** to let them know a route is being delivered to them in the "*route*" attribute of the json object.
* "*received*" : "*DONE*" to be sent when the vehicle is **ARRIVED** and the customer is done so the vehicle can change its status to **AVAILABLE** and continue on its path.