# iDAAS-Connect-ThirdParty
This is the upstream community for or RedHat Healthcare's <a href="https://github.com/RedHat-Healthcare/iDaaS-Connect/tree/master/iDaaS-Connect-ThirdParty" target="_blank">iDaaS Connect ThirdParty</a>.<br/>
iDAAS has several key components that provide many capabilities. iDAAS Connect is intended ONLY
to enable iDAAS connectivity. iDAAS-Connect-ThirdParty specifically is ONLY intended to deal with enabling
iDAAS to all sorts of third party connectivity. For example: RDBMS, Kafka, Mainframe, Files, SFTP, etc.
plus dozens of others are supported.

# Focus on Improving
We are focusing on continuing to improve. With the numerous implementation and partner implementations we
have focused on success overall and as we progress forward the intent is to focus on success while being consistent.
Please find details on how to help us [here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/OngoingEnhancements.md).

# Pre-Requisites
For any repository to be implemented there are two types of requirements, overall general requirements
and then there are specific submodule requirements.

## General Pre-Requisites
For all iDaaS Connect branded solutions there are some general content which can be looked at
here in [detail](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/CloningBuildingRunningSolution.md)

## Specific Implementation Pre-Requisites
This solution contains three supporting directories. The intent of these artifacts to enable
resources to work locally: <br/>
+ platform-ddl: DDL usecd within this accelerator.
+ platform-testdata: sample transactions to leverage for using the platform. 

# Administrative Console
Within each implementation there is a management console, the management console provides the same
interface and capabilities no matter what implementation you are working within. Specifics on the
Admin/Mgmt interface can be found
[here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/AdministeringPlatform.md).

# Specific Implementation Details
The following section is intended to cover specific implementation known issues, challenges and potential implementation
details.
- Make sure you specify the specific directories correctly
- Make sure you specify the database you are connecting to correctly
```
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

## Known Issues
As of the time of this content publication there are no known specific issues. The ONLY consistent
common issue is setting the application.properties before running the application.

# Implementation Scenario(s): Kafka Integration 
This repository follows a very common general implementation. The only connector currently in this code
base is a Kafka topic. The key sceanrio this can demonstrate is data being processed from a data science 
kafka topic.

## Implementation Data Flow Steps
1. The Kafka client connects to a particular broker and topic and checks if there is any data to process. 
2. If there is data it will audit the transaction processing 
3. The transaction will be routed for processing within iDAAS KIC
    
Happy using and coding....
