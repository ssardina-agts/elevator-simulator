# Elevator Simulator #

### What is this repository for? ###

* Fork of [elevatorsim](http://sourceforge.net/projects/elevatorsim)
* Adapted original simulator to allow control over a network
* Forked from r162 (0.4+)
* Developed by final year RMIT CSIT students as part of their capstone project
under the supervision of Sebastian Sardina

### Contribution guidelines ###

* TBD

### Who do I talk to? ###

* Sebastian Sardina (project supervisor and sarlrmit admin)

## Building from Source ##

#### Using Eclipse ####

* Clone repository into Eclipse workspace
* Import existing project, select the elevator-sim directory in the workspace
* If there are errors:
    * Check the m2eclipse plugin is installed
    * Run the "Discover m2e connectors" quick fix if there is an error on the
    plugin tag in pom.xml
    * Check maven proxy settings

#### Using Maven ####

* Clone repository
* Change directory into 'elevator-sim'
* run `mvn compile` to create class files or `mvn package` to create a jar

## Running the networked simulator ##

* Execute jar or ElevatorSimualationApplication
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


# Previous Readme #

Elevator Simulator 0.4
Copyright 2004-2005 Neil McKellar and Chris Dailey
Licensed under the LGPL (Lesser GNU Public License)

Written by Neil and Chris

The Elevator Simulator is an experiment in paired-programming.
Development is done entirely in Java. The finished product will
provide a framework for additional simulations and some ability
to test different elevator behaviours.

The application can be built with Ant using the build.xml file in
the components directory.  The current source was developed with
JDK 1.4.2.  There are no external library dependencies.  The
resulting jar file in build/jars/ should automatically execute
org.intranet.elevator.ElevatorSimulationApplication.

The project page is located at:
http://sourceforge.net/projects/elevatorsim

Report bugs and feature requests through the project page.

New Features in 0.4:
====================
Added an application icon.
Added the ability to have more than one type of car controller.
Added a time factor to the user interface to control the speed of the
real-time simulation.
Updated formatting of clock display.
Added a capacity setting for elevator cars.
Added animation of people getting on and off cars.
Added sensor behaviour for car doors.
Added an averaging feature for multiple simulations.  This makes the
random seed setting more useful.
Added additional error dialogs when the application encounters errors.
Many, many bug fixes.


New Features in 0.3:
====================
An ability to run blocks of simulations so results can be compared.
Basically, the application will run simulations outside of the
real-time clock.  The simulation increments each value by the
specified step value until every combination is tested.  A table of
results is produced that can show comparisons with two of the
ranges of parameters.