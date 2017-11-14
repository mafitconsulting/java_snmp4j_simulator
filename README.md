# CMDB Desktop Application
Java swing application for windows desktop. The application connects to a back-end oracle database and 
retrieves CI data from custom table (viewAxisTable) which had been populated from an AXIS CMDB feed. 
Searches on ci name. Retrieves HW_STATUS and V_STATUS 

## Getting Started

To clone this repository:     

git clone https://github.com/mafitconsulting/CMDBDesktopApplication.git
        
Once the repository is cloned, there will be a requirement to customise the code as per users requirement
### Prerequisites
####Runtime
jre 1.6 or above
####Development
Java JDK 6 or above

Netbeans IDE 8.1 (optional)

Oracle Database 11g Release 2 JDBC Drivers ojdbc6_g.jar (package in the repository)

### Distributing and Running Standalone GUI Applications
As an example of how the GUI application for distribution runsfrom the command line:

1. Navigate to the project's dist folder in the location where you cloned the git repo

java -jar AxisQueryServer.jar 

You'll need to recompile all the classes when you've customised you main class.

## Built With
Netbeans 8.1

## Authors
Mark Fieldhouse - Mafitconsulting 

## License
This project is licensed under the MIT License 
