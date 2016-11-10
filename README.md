# RMIT Elevator Simulator #

### What is this repository for? ###

* Fork of [elevatorsim](http://sourceforge.net/projects/elevatorsim)
* Adapted original simulator to allow control over a network
* Forked from r162 (0.4+)
* Developed by final year RMIT CSIT students as part of their capstone project
under the supervision of A/Prof. Sebastian Sardina

### Changes from r162 (0.4+) version ###

* Network connectivity to simulation; allows controllers to run outside simulator.
* Allows to change car destination while car is traveling.
* People repress floor button if unable to enter car due to lack of capacity.
* Generate special event when car passes through each floor while traveling.
* Many bugs fixed.

### Who do I talk to? ###

* A/Prof. Sebastian Sardina (sebastian.sardina@rmit.edu.au)
Project supervisor and SARLRMIT admin


The project page is located at:
https://bitbucket.org/sarlrmit/elevator-sim

Report bugs and feature requests through the project page (Issues).

## Building from Source ##

#### Using Eclipse ####

* Clone repository into Eclipse workspace
* Import existing project, select the elevator-sim directory in the workspace
* You can now run ElevatorSimulationApplication from Eclipse
* If there are errors:
    * Check the m2eclipse plugin is installed
    * Run the "Discover m2e connectors" quick fix if there is an error on the
    plugin tag in pom.xml
    * Check maven proxy settings

#### Using Maven ####

* Clone repository
* Change directory into 'elevator-sim'
* run `mvn install` to create a JAR file
* maven will copy a jar with the json dependency to target/
* Make sure the json jar is in the same directory when executing rmit-elevator-simulator\*.jar

## Running the networked simulator ##

* Execute JAR file or ElevatorSimualationApplication
* Select file > new
* Select 'Random Rider Insertion' and click 'Real time'
* Configure simulation settings to your liking or leave them at their defaults
* Select 'NetworkWrapperController' under the 'Controller' option
* Click apply
* Run a client (see sarlrmit/java-elsim-client or sarlrmit/SARL-CONTROLLER)
* Click 'Go, Dude!' once the client is connected and the bottom left panel
is showing statistics
* Adjust the time factor in the bottom right to speed up / slow down the simulation
* It is up to the client to control the elevator cars

## Configuration ##
* The application will generate elsimsettings.json in the working directory if it
does not already exist
* 'port' is the port the server will listen for a connection on
* 'timeout' is the time in seconds the server will wait for communication from a
client before trying to throwing an error
* set 'enableOldControllers' to true to enable MetaController and SimpleController
which do not run over the network
* set 'enableHiddenSimulators' to true to enable some old simulators from the original
project that were created for development purposes

## Launch Options ##

The following arguments can be given to the program:

* `-speed <speed factor>`: Sets the simulation speed to <speed factor> when a
simulation is created
* `-filestats <csv file>`: Outputs csv statistics to <csv file>. One line
will be appended to the file for every simulation that is run. The same csv file
should not be used across different application versions.