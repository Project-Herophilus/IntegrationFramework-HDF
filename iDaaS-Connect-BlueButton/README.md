# iDAAS-Connect-ThirdParty
This is the upstream community for or RedHat Healthcare's <a href="https://github.com/RedHat-Healthcare/iDaaS-Connect/tree/master/iDaaS-Connect-ThirdParty" target="_blank">iDaaS Connect ThirdParty</a>.<br/>
iDAAS has several key components that provide many capabilities. iDAAS Connect is intended ONLY
to enable iDAAS connectivity. iDAAS-Connect-ThirdParty specifically is ONLY intended to deal with enabling
iDAAS to all sorts of third party connectivity. For example: RDBMS, Kafka, Mainframe, Files, SFTP, etc.
plus dozens of others are supported.

## Add-Ons
This solution contains three supporting directories. The intent of these artifacts to enable
resources to work locally: <br/>
+ platform-ddl: DDL usecd within this accelerator (MySQL 8 based).
+ platform-scripts: support running kafka, creating/listing and deleting topics needed for this solution
and also building and packaging the solution as well. All the scripts are named to describe their capabilities <br/>
+ platform-testdata: sample transactions to leverage for using the platform. 

## Pre-Requisites
For all iDaaS design patterns it should be assumed that you will either install as part of this effort, or have the following:
1. An existing Kafka (or some flavor of it) up and running. Red Hat currently implements AMQ-Streams based on Apache Kafka; however, we
have implemented iDaaS with numerous Kafka implementations. Please see the following files we have included to try and help: <br/>
[Kafka](https://github.com/RedHat-Healthcare/iDaaS-Demos/blob/master/Kafka.md)<br/>
[KafkaWindows](https://github.com/RedHat-Healthcare/iDaaS-Demos/blob/master/KafkaWindows.md)<br/>
No matter the platform chosen it is important to know that the Kafka out of the box implementation might require some changes depending
upon your implementation needs. Here are a few we have made to ensure: <br/>
In <kafka>/config/consumer.properties file we will be enhancing the property of auto.offset.reset to earliest. This is intended to enable any new
system entering the group to read ALL the messages from the start. <br/>
auto.offset.reset=earliest <br/>
2. Some understanding of building, deploying Java artifacts and the commands associated. If using Maven commands then Maven would need to be intalled and runing for the environment you are using. More details about Maven can be found [here](https://maven.apache.org/install.html)<br/>
3. An internet connection with active internet connectivity, this is to ensure that if any Maven commands are
run and any libraries need to be pulled down they can.<br/>

We also leverage [Kafka Tools](https://kafkatool.com/) to help us show Kafka details and transactions; however, you can leverage
code or various other Kafka technologies ot view the topics.

# Scenario(s): Kafka Integration 
This repository follows a very common general implementation. The only connector currently in this code
base is a Kafka topic. The key sceanrio this can demonstrate is data being processed from a data science 
kafka topic.

## Integration Data Flow Steps
1. The Kafka client connects to a particular broker and topic and checks if there is any data to process. 
2. If there is data it will audit the transaction processing 
3. The transaction will be routed for processing within iDAAS KIC
    
# Start The Engine!!!
This section covers the running of the solution. There are several options to start the Engine Up!!!

## Step 1: Kafka Server To Connect To
In order for ANY processing to occur you must have a Kafka server running that this accelerator is configured to connect to.
Please see the following files we have included to try and help: <br/>
[Kafka](https://github.com/RedHat-Healthcare/iDaaS-Demos/blob/master/Kafka.md)<br/>
[KafkaWindows](https://github.com/RedHat-Healthcare/iDaaS-Demos/blob/master/KafkaWindows.md)<br/>

## Step 2: Running the App: Maven or Code Editor
This section covers how to get the application started.

### Design Pattern/Accelerator Configuration
All iDaaS Design Pattern/Accelelrators have application.properties files to enable some level of reusability of code and simplfying configurational enhancements.<br/>
In order to run multiple iDaaS integration applications we had to ensure the internal http ports that
the application uses. In order to do this we MUST set the server.port property otherwise it defaults to port 8080 and ANY additional
components will fail to start. iDaaS Connect HL7 uses 9980. You can change this, but you will have to ensure other applications are not
using the port you specify.

```properties
server.port=9980
```
Once built you can run the solution by executing `./platform-scripts/start-solution.sh`.
The script will startup Kafka and iDAAS server.

Alternatively, if you have a running instance of Kafka, you can start a solution with:
`./platform-scripts/start-solution-with-kafka-brokers.sh --idaas.kafkaBrokers=host1:port1,host2:port2`.
The script will startup iDAAS server.

It is possible to overwrite configuration by:
1. Providing parameters via command line e.g.
`./start-solution.sh --idaas.adtPort=10009`
2. Creating an application.properties next to the idaas-connect-hl7.jar in the target directory
3. Creating a properties file in a custom location `./start-solution.sh --spring.config.location=file:./config/application.properties`

Supported properties include:
```properties
# Server - Internal
server.host=9983
# Kafka
kafkaBrokers=localhost:9092
# Reporting Directory and File Name
#mandatory.reporting.directory=/MandatoryReporting
mandatory.reporting.directory=src/data/MandatoryReporting
mandatory.reporting.file=ReportingExample.csv
# Covid Directory and File Ext
covid.reporting.directory=src/data/CovidData
covid.reporting.extension=*.csv
# Reseach Data Directory and File Ext
research.data.directory=src/data/ResearchData
covid.reporting.extension=*.csv
# JDBC Database
spring.datasource.url=jdbc:mysql://localhost/idaas
                     #jdbc:postgresql://localhost:5432/idaas
spring.datasource.username=idaas
spring.datasource.password=@idaas123
#spring.database.driver-class-name=com.mysql.cj.jdbc.Driver
#org.postgresql.Driver
```

### Running the App: Maven
You can use Maven from the command line, you would need to go the specific directory where this code exists and has a pom.xml and then run the
command: mvn clean install

### Running the App: From Your Code Editor/IDE
You can right click on Application.java file and select Run in the src directory.

### Running the App from Command Line
You will need to have built the application into a jar.

1. You download the code
2. You build the code into a jar
3. Move the application.properties file from the \src\main\resources to where you would like it or just remember the location as it
it will be needed later
4. Make sure you have a Kafka instance up and running to process data with
5. Go to a command line and execute java -jar <jarlocation>/<jarname.jar> --spring.config.location=file: ./<directory>/application.properties`

Happy using and coding....

## Ongoing Enhancements
We maintain all enhancements within the Git Hub portal under the 
<a href="https://github.com/RedHat-Healthcare/iDAAS-Connect-ThirdParty/projects" target="_blank">projects tab</a>

## Defects/Bugs
All defects or bugs should be submitted through the Git Hub Portal under the 
<a href="https://github.com/RedHat-Healthcare/iDAAS-Connect-ThirdPartyt/issues" target="_blank">issues tab</a>

## Chat and Collaboration
You can always leverage <a href="https://redhathealthcare.zulipchat.com" target="_blank">Red Hat Healthcare's ZuilpChat area</a>
and find all the specific areas for iDAAS-Connect-ThirdParty. We look forward to any feedback!!

If you would like to contribute feel free to, contributions are always welcome!!!! 

Happy using and coding....
