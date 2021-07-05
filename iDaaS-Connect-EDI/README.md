# iDAAS Connect EDI
iDAAS Connect for Processing EDI data - This effort has started to support the HIPAA Compliant 5010 EDI transactions
but will be moving into Supply Chain transactions. This design pattern is intended to parse, build and enable
the usage of 5010 EDI and Supply Chain data.

The intent of these artifacts to enable resources to work locally: <br/>
+ platform-scripts: support running kafka, creating/listing and deleting topics needed for this solution
   and also building and packaging the solution as well. All the scripts are named to describe their capabilities <br/>
+ platform-testdata: sample transactions to leverage for using the platform. <br/>

## Scenario: EDI Data Processing
This repository follows a very common general implementation of processing a file from a filesystem. The intent is to pick
up the file and process it and then leverage the existing iDaaS-EventBuilder library to show it being processed and manipulated.

### Integration Data Flow Steps

1. Every 1 minute the defined directory is looked at for any .edi file, if found the file is processed into a matching structure.
2. The data structure is then persisted into a kafka topic.

## Step 1: Kafka Server To Connect To
In order for ANY processing to occur you must have a Kafka server running that this accelerator is configured to connect to.
Please see the following files we have included to try and help: <br/>
[Kafka](https://github.com/RedHat-Healthcare/iDaaS-Demos/blob/master/Kafka.md)<br/>
[KafkaWindows](https://github.com/RedHat-Healthcare/iDaaS-Demos/blob/master/KafkaWindows.md)<br/>

## Step 2: Running the App: Maven Commands or Code Editor
This section covers how to get the application started.
+ Maven: The following steps are needed to run the code. Either through your favorite IDE or command line
```
git clone <repo name>
For example:
git clone https://github.com/RedHat-Healthcare/iDaaS-Connect.git
 ```
You can either compile at the base directory or go to the specific iDaaS-Connect acceelerator. Specifically, you want to
be at the same level as the POM.xml file and execute the following command: <br/>
```
mvn clean install
```
You can run the individual efforts with a specific command, it is always recommended you run the mvn clean install first.
Here is the command to run the design pattern from the command line: <br/>
```
mvn spring-boot:run
 ```
Depending upon if you have every run this code before and what libraries you have already in your local Maven instance
it could take a few minutes.
+ Code Editor: You can right click on the Application.java in the /src/<application namespace> and select Run

# Running the Java JAR
If you don't run the code from an editor or from the maven commands above. You can compile the code through the maven
commands above to build a jar file. Then, go to the /target directory and run the following command: <br/>
```
java -jar <jarfile>.jar 
 ```

## Design Pattern/Accelerator Configuration
Each design pattern/accelerator has a unique and specific application.properties for its usage and benefit. Please make
sure to look at these as there is a lot of power in these and the goal is to minimize hard coded anything.
Leverage the respective application.properties file in the correct location to ensure the properties are properly set
and use a custom location. You can compile the code through the maven commands above to build a jar file. Then, go
to the /target directory and run the following command: <br/>
```
java -jar <jarfile>.jar --spring.config.location=file:./config/application.properties
 ```
# Admin Interface - Management and Insight of Components
Within each specific repository there is an administrative user interface that allows for monitoring and insight into the
connectivity of any endpoint. Additionally, there is also the implementation to enable implementations to build there own
by exposing the metadata. The data is exposed and can be used in numerous very common tools like Data Dog, Prometheus and so forth.
This capability to enable would require a few additional properties to be set.

Below is a generic visual of how this looks (the visual below is specific to iDaaS Connect HL7): <br/>

![iDaaS Platform - Visuals - iDaaS Data Flow - Detailed.png](https://github.com/RedHat-Healthcare/iDAAS/blob/master/images/iDAAS-Platform/iDaaS-Mgmt-UI.png)

Every asset has its own defined specific port, we have done this to ensure multiple solutions can be run simultaneously.

## Administrative Interface(s) Specifics
For all the URL links we have made them localhost based, simply change them to the server the solution is running on.

|<b> iDaaS Connect Asset | Port | Admin URL / JMX URL |
| :---        | :----   | :--- |
|iDaaS Connect EDI | 9984| http://localhost:9984/actuator/hawtio/index.html / http://localhost:9984/actuator/jolokia/read/org.apache.camel:context=*,type=routes,name=*|

### Design Pattern/Accelerator Configuration

If you would like to contribute feel free to, contributions are always welcome!!!!

Happy using and coding....
