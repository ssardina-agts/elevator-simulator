# RMIT Elevator Simulator #

## What is this repository for? 

* Fork of [elevatorsim](http://sourceforge.net/projects/elevatorsim)
* Adapted original simulator to allow control over a network
* Forked from r162 (0.4+)
* Developed by final year RMIT CSIT students as part of their capstone project
under the supervision of A/Prof. Sebastian Sardina

### Changes from r162 (0.4+) version 

* Network connectivity to simulation; allows controllers to run outside simulator.
* Allows to change car destination while car is traveling.
* People repress floor button if unable to enter car due to lack of capacity.
* Generate special event when car passes through each floor while traveling.
* Many bugs fixed.



## Building from Source

### Via Eclipse 

* Clone repository into Eclipse workspace
* Import existing project, select the elevator-sim directory in the workspace
* You can now run **ElevatorSimulationApplication** from Eclipse
* If there are errors:
    * Check the `m2eclipse` plugin is installed
    * Run the "Discover m2e connectors" quick fix if there is an error on the plugin tag in `pom.xml`
    * Check maven proxy settings

### Via Maven 

* Clone repository
* Change directory into `elevator-sim`
* Run `mvn package`.
    * This will install the normal JAR file and one with all dependencies.
* Can also run `mvn install` to create a JAR file and:
    * maven will copy a JAR with the json dependency to `target/`
    * Make sure the json JAR is in the same directory when executing `rmit-elevator-simulator\*.jar`


## Running the networked simulator ##

* Execute JAR file or `ElevatorSimualationApplication` class.
    * e.g., `java -jar target/rmit-elevator-sim-1.0-jar-with-dependencies.jar`
* Select file > new`
* Select 'Random Rider Insertion' and click 'Real time'
* Configure simulation settings to your liking or leave them at their defaults
* Select 'NetworkWrapperController' under the 'Controller' option
* Click apply
* Run a client to connect and control the elevators.
* Click 'Go, Dude!' once the client is connected and the bottom left panel is showing statistics
* Adjust the time factor in the bottom right to speed up / slow down the simulation
* It is up to the client to control the elevator cars


## Configuration 

* The application will generate `elsimsettings.json` in the working directory if it does not already exist
* `port` is the port the server will listen for a connection on
* `timeout` is the time in seconds the server will wait for communication from a client before trying to throwing an error
* set `enableOldControllers` to true to enable MetaController and SimpleController which do not run over the network
* set `enableHiddenSimulators` to true to enable some old simulators from the original project that were created for development purposes


## Launch Options ##

The following arguments can be given to the program:

* `-speed <speed factor>`: Sets the simulation speed to <speed factor> when a simulation is created
* `-filestats <csv file>`: Outputs csv statistics to `<csv file>`. One line will be appended to the file for every simulation that is run. The same csv file should not be used across different application versions.


----------------------------------------------

## PROJECT CONTRIBUTORS 

* Sebastian Sardina (Project leader & contact- ssardina@gmail.com)
* Matthew McNally
* Joshua Richards


## LICENSE 

This project is using the GPLv3 for open source licensing for information and the license visit GNU website (https://www.gnu.org/licenses/gpl-3.0.en.html).

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.