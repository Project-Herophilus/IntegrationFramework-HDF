# iDAAS-Connect-ThirdParty
iDAAS-Connect-ThirdParty is ONLY intended to deal with enabling connectivity to all sorts of different types of
data systems. For example: RDBMS, Kafka, Mainframe, Files, SFTP, etc. plus over one hundred other types of connectivity  
are supported.

# Focus on Improving
We are focusing on continuing to improve. With the numerous implementation and partner implementations we
have focused on success overall and as we progress forward the intent is to focus on success while being consistent.
Please find details on how to help us [here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/OngoingEnhancements.md).

# Implementation
The following section is designed to cover the details around implementing.

## How To Get, Build and Run iDaaS-Connect Assets
Within each submodule/design pattern/reference architecture in this repository there is a specific README.md. It is
intended to follow a specific format that covers a solution definition, how we look to continually improve, pre-requisities,
implementation details including specialized configuration, known issues and their potential resolutions.
However, there are a lot of individual capabilities, we have tried to keep content relevant and specific to
cover specific topics.
- For cloning, building and running of assets that content can be found
  [here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/CloningBuildingRunningSolution.md).
- Within each implementation there is a management console, the management console provides the same
  interface and capabilities no matter what implementation you are working within, some specifics and
  and details can be found [here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/AdministeringPlatform.md).

## Known Issues
As of the time of this content publication there are no known specific issues. The ONLY consistent
common issue is setting the application.properties before running the application.

## Specific Implementation Pre-Requisites
This solution contains three supporting directories. The intent of these artifacts to enable
resources to work locally: <br/>
+ DDLs: DDL used within this specific design pattern/accelerator.

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


## How to deploy using maven-plugin

    mvn clean oc:deploy -P openshift -Djkube.generator.from=openshift/fuse7-java11-openshift:1.10 -Djkube.generator.fromMode=istag

## How to run in Dev Mode

    mvn spring-boot:run -Dspring-boot.run.profiles=dev
