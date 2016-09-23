# README #

This README will help you you obtain and install a copy of the SARL Controller Client on your local machine,
for the Elevator Sim Server install instructions please see the README.md at (https://bitbucket.org/sarlrmit/elevator-sim)
This project was
### Software Prerequisites ###
1. Eclipse Platform (version xx minumum)
2. Java Runtime Environment (JRE)


### How to set up the Elevator Sim Server ###

1.  Download SarlEclipse: Sarl.io/download/
2.  Follow installation guide at sarl.io
3.  Clone SarlEclipse repository within Sarl Eclipse: bitbucket.org/sarlrmit/sarl-controller
4.  Import project from repository 
5.  pom.xml will appear with an error, hover over and click discover new m2e connector
6.  You will be prompted to install buildhelper, do so
7.  Go to Run -> Run Configurations and under Java Application select New_configuration
8.  In the Project field Browse for the project
9.  In the Main class field Search for io.janusproject.Boot
10. Under program arguments put the full name(io.sarl.elevatorsim.SimulatorProxyAgent)
11. Run and enjoy!

### Who do I talk to? ###

Matthew McNally (Project Manager & Sarl agent developer) - 
Joshua Richards (Java Elevator Sim Server developer) -
Joshua Beale (Sarl agent developer) -

### License ###
This project is using the GPL3 for open source licensing for information and the license visit GNU website (https://www.gnu.org/licenses/gpl-3.0.en.html)
