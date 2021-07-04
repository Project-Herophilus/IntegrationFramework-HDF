# iDAAS Connect BlueButton
This project fetches Medicare data of an authenticated beneficiary through the [Blue Button API](https://bluebutton.cms.gov/) and sends it to a Kafka topic. The fuse application serves as a webserver. User opens the served URL using a web browser and log into the Medicare database. The application will automatically fetch its part A, B, C, and D data and sends it to a Kafka topic. Then other processors can subscribe to the topic to process the data.

## Prerequisites
1. Sign up for the blue button [developer sandbox](https://bluebutton.cms.gov/).
2. Create a new application with the following. Then write down the resulting Client ID and Client Secret
* OAuth - Client Type: confidential
* OAuth - Grant Type: authorization-code
* Callback URLS: http://localhost:8890/callback (or another url more appropriate)

## Solution Pre-Requisities
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

### Design Pattern/Accelerator Configuration
All iDaaS Design Pattern/Accelelrators have application.properties files to enable some level of reusability of code and simplfying configurational enhancements. Configure src/main/resources/application.properties with the prerequisite data, for example,
```
bluebutton.callback.path=callback
bluebutton.callback.host=localhost
bluebutton.callback.port=8890
```
http://localhost:8890/callback is the callback URL you registered with bluebutton.cms.gov. http://localhost:8890/bluebutton will be the service URL for iDAAS-Connect-BlueButton.
Every asset has its own defined specific port, we have done this to ensure multiple solutions can be run simultaneously.

## Administrative Interface(s) Specifics
For all the URL links we have made them localhost based, simply change them to the server the solution is running on.

|<b> iDaaS Connect Asset | Port | Admin URL / JMX URL |
| :---        | :----   | :--- | 
|iDaaS Connect BlueButton| 9982| http://localhost:9982/actuator/hawtio/index.html / http://localhost:9982/actuator/jolokia/read/org.apache.camel:context=*,type=routes,name=*|  

If you would like to contribute feel free to, contributions are always welcome!!!!

Happy using and coding....
## Additional Information
* Download the [CSV file](https://bluebutton.cms.gov/synthetic_users_by_claim_count_full.csv) which contains 100 sample data with id, user name, and password.
* Build the project by running `platform-scripts/build-solution.sh`
* Start the project by running `platform-scripts/start-solution.sh`. It will start the Kafka cluster on port 9092 and the Fuse application which listens on port 8890.
* In a web browser type http://localhost:8890/bluebutton
* It will automatically redirect you to Blue Buttons's authentication page. Fill in the user name and password
* Patient, Coverage, and ExplanationOfBenifit data will be sent to Kafka topic `bluebutton`.

Happy Coding!!!!