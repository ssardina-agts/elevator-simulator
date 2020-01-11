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



## Building & Running

You can run the simulation via command line, Maven, or inside an IDE like Eclipse.

The main class is `org.intranet.elevator.ElevatorSimulationApplication`

The easiest way is to just obtain the latest JAR file from the [release](https://github.com/ssardina-agts/elevator-simulator/releases) section and just run:

```bash
java -jar elevator-simulator-1.0-jar-with-dependencies.jar
```

Alternatively, you can first produce the JAR file via Maven via `mvn clean package `, which will leave the JAR file under `target/`

Also, if you have cloned the source, you can just run the system via `mvn exec:java`

Once the applicatio is up and running:

1. Select **File > New**.
2. Select **Random Rider Insertion** and click **Real time**.
3. Configure simulation settings to your liking or leave them at their defaults.
4. Select **NetworkWrapperController** under the **Controller** option.
5. Click **Apply**.
6. Run a client system (e.g., Java, Python, or SARL based) to connect and control the elevators.
7. Click **Go, Dude!** once the client is connected and the bottom left panel is showing statistics.
8. Adjust the time factor in the bottom right to speed up / slow down the simulation.
9. It is up to the client to control the elevator cars.


### Configuration 

* The application will generate `elsimsettings.json` in the working directory if it does not already exist
* `port` is the port the server will listen for a connection on
* `timeout` is the time in seconds the server will wait for communication from a client before trying to throwing an error
* set `enableOldControllers` to true to enable MetaController and SimpleController which do not run over the network
* set `enableHiddenSimulators` to true to enable some old simulators from the original project that were created for development purposes


### Launch Options 

The following arguments can be given to the program:

* `-speed <speed factor>`: Sets the simulation speed to <speed factor> when a simulation is created
* `-filestats <csv file>`: Outputs csv statistics to `<csv file>`. One line will be appended to the file for every simulation that is run. The same csv file should not be used across different application versions.


----------------------------------------------

## PROJECT CONTRIBUTORS 

* Sebastian Sardina (Project leader & contact - ssardina@gmail.com)
* Matthew McNally
* Joshua Richards
* Abhijeet Anand.

## LICENSE 

This project is using the GPLv3 for open source licensing for information and the license visit GNU website (https://www.gnu.org/licenses/gpl-3.0.en.html).

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.