# iDaaS Connect HL7
This is the upstream for RedHat Healthcare's <a href="https://github.com/RedHat-Healthcare/iDaaS-Connect/tree/master/iDaaS-Connect-HL7" target="_blank">iDaaS Connect HL7</a>. iDaaS Connect HL7 is designed to simplify HL7 integration platform development and delivery.
iDAAS has several key components that provide many capabilities. iDAAS Connect is intended ONLY
to enable iDAAS connectivity. iDAAS-Connect-HL7 specifically ONLY deals with enabling
iDAAS to process the healthcare industry standard HL7 and CCDA based transactions ONLY.
The following transactions are supported:
- HL7 messages (ADT, ORM, ORU, MFN, MDM, PHA, SCH and VXU) from any vendor and any version of HL7 v2.
- CCDA Events 

For ease of use and implementation this design pattern has built in support for the following protocols to support data processing:
- MLLP (Minimal Lower Layer Protocol - the HL7 Protocol)
- Files
- HTTP(s) endpoint for CCDA

## Add-Ons
To support this design pattern/reference architecture there are a subset of assets avaiable to assist in the implementation : <br/>

|Add-On | Description |
| ------------ | ----------- |
| [Diagrams](https://github.com/Project-Herophilus/Project-Herophilus-Assets/tree/main/Platform/Draw.IO)| Various Draw.IO diagrams that cover iDaaS  |
| [Test Data](https://github.com/Project-Herophilus/Project-Herophilus-Assets/tree/main/Platform/Testdata)| Test data for all healthcare industry standards work  |
| [Synthetic Data](https://github.com/Project-Herophilus/DataSynthesis)| Synthetic Data Tools and Data  |
| [Data Simulators](https://github.com/Project-Herophilus/iDaaS-AddOns)| Simulators to help in implementation and testing  |

# Specific Instructions
The following are special instructions developed based on specific implementations. While some of these might be documented
for specific OSes that the overall issue is key to understand and resolve.

## General Issues: HL7
1. IPV6 needs to be disabled and IPv4 needs to be enabled.

### Red Hat Enterprise Linux

1. Specific to iDaaS Connect HL7 design patterns/reference architecture IPv4 must be enabled at the OS level, IPv6 will cause connectivity issues and in many cases outright failure of the components to function.<br/>
   https://access.redhat.com/solutions/8709
   But here's the current specifics: <br/>
   Disabling IPv6 in NetworkManager
   For all systems that run NetworkManager, IPv6 must be disabled on each interface with the option ipv6.method set to ignore (RHEL7) or disabled (RHEL8+). This step must be done in addition to IPv6 being disabled using the methods below.
   For RHEL 8 and later: <br/>
```
nmcli connection modify <Connection Name> ipv6.method "disabled" <br/>
(Replace <Connection Name> with interface)
```
AND <br/>
```
Create a new file named /etc/sysctl.d/ipv6.conf and add the following options:

# First, disable for all interfaces
net.ipv6.conf.all.disable_ipv6 = 1
net.ipv6.conf.default.disable_ipv6 = 1
net.ipv6.conf.lo.disable_ipv6 = 1
# If using the sysctl method, the protocol must be disabled all specific interfaces as well.
net.ipv6.conf.<interface>.disable_ipv6 = 1
The new settings would then need to be reloaded with the following command line command:

# sysctl -p /etc/sysctl.d/ipv6.conf
```

## Pre-Requisites
For all iDaaS design patterns it should be assumed that you will either install as part of this effort, or have the following:

1. An existing Kafka (or some flavor of it) up and running. Please see the following files we have included to try and help: <br/>
[Kafka](https://github.com/RedHat-Healthcare/iDaaS-Demos/blob/master/Kafka.md)<br/>
[KafkaWindows](https://github.com/RedHat-Healthcare/iDaaS-Demos/blob/master/KafkaWindows.md)<br/>
No matter the platform chosen it is important to know that the Kafka out of the box implementation might require some changes depending upon your implementation needs. Here are a few we have made to ensure: <br/>
In /config/consumer.properties file we will be enhancing the property of auto.offset.reset to earliest. This is intended to enable any new system entering the group to read ALL the messages from the start. <br/>
auto.offset.reset=earliest <br/>
2. Some understanding of building, deploying Java artifacts and the commands associated. If using Maven commands then Maven would need to be intalled and runing for the environment you are using. More details about Maven can be found [here](https://maven.apache.org/install.html)<br/>
3. An internet connection with active internet connectivity, this is to ensure that if any Maven commands are
run and any libraries need to be pulled down they can.<br/>

# Scenario(s)
This section is intended to cover any scenarios covered within this demo.

## Basic HL7 Message Integration 
This repository follows a very common general clinical care implementation pattern. The implementation pattern involves one system sending data to 
another system via the HL7/MLLP message standard. 

|Identifier | Description |
| ------------ | ----------- |
| Healthcare Facility| MCTN |
| Sending EMR/EHR | MMS |
| HL7 Message Events | ADT (Admissions, Discharge and Transfers),ORM (Orders),ORU (Results), MDM (Master Document Management), MFN (Master Files Notification), RDE (Pharmacy), SCH (Schedules), VXU (Vaccinations) |
| CCDA Events | CCDA v3 compliant transactions |

<br/>
It is important to know that for every HL7 Message Type/Event there is a specifically defined, and dedicated, HL7 socket server endpoint.

### Integration Data Flow Steps
Here is a general visual intended to show the general data flow and how the accelerator design pattern is intended to work. <br/>
 <img src="https://github.com/RedHat-Healthcare/iDAAS/blob/master/content/images/iDAAS-Platform/DataFlow-HL7.png" width="800" height="600">

1. Any external connecting system will use an HL7 client (external to this application) will connect to the specifically defined HL7
Server socket (one socket per datatype) and typically stay connected.
2. The HL7 client will send a single HL7 based transaction to the HL7 server.
3. iDAAS Connect HL7 will do the following actions:<br/>
    a. Receive the HL7 message. Internally, it will audit the data it received to a specifically defined topic.<br/>
    b. The HL7 message will then be processed to a specifically defined topic for this implementation. There is a 
    specific topic pattern -  for the facility and application each data type has a specific topic define for it.
    For example: Admissions: MCTN_MMS_ADT, Orders: MCTN_MMS_ORM, Results: MCTN_MMS_ORU, etc.. <br/>
    c. An acknowledgement will then be sent back to the hl7 client (this tells the client he can send the next message,
    if the client does not get this in a timely manner it will resend the same message again until he receives an ACK).<br/>
    d. The acknowledgement is also sent to the auditing topic location.<br/>


## Step 2: Running the App: Maven Commands or Code Editor
This section covers how to get the application started.
+ Maven: go to the directory of where you have this code. Specifically, you want to be at the same level as the POM.xml file and execute the
following command: <br/>
```
mvn clean install
 ```
You can run the individual efforts with a specific command, it is always recommended you run the mvn clean install first.
Here is the command to run the design pattern from the command line: <br/>
```
mvn spring-boot:run
 ```
Depending upon if you have every run this code before and what libraries you have already in your local Maven instance it could take a few minutes.
+ Code Editor: You can right click on the Application.java in the /src/<application namespace> and select Run

# Running the Java JAR
If you don't run the code from an editor or from the maven commands above. You can compile the code through the maven
commands above to build a jar file. Then, go to the /target directory and run the following command: <br/>
```
java -jar <jarfile>.jar 
 ```

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

Supported properties include (for this accelerator there is a block per message type that follows the same pattern):
```properties
server.port=9980
# Kafka Configuration - use comma if multiple kafka servers are needed
idaas.kafkaBrokers=localhost:9092
# Basics on properties
idaas.hl7ADT_Directory=data/adt
idaas.adtPort=10001
idaas.adtACKResponse=true
idaas.adtTopicName=mctn_mms_adt
idaas.hl7ORM_Directory=data/orm
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
|iDaaS Connect HL7 | 9980| http://localhost:9980/actuator/hawtio/index.html / http://localhost:9980/actuator/jolokia/read/org.apache.camel:context=*,type=routes,name=* | 

If you would like to contribute feel free to, contributions are always welcome!!!! 

Happy using and coding....

