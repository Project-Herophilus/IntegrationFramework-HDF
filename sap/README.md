# iDAAS-Connect-SAP
iDAAS-Connect-SAP is ONLY intended to help with enabling connecting to/from all SAP data.

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

# Specific Implementation Details
The following section is intended to cover specific implementation known issues, challenges and potential implementation
details.

## Implementation Example(s): Kafka Integration 
This repository follows a very common general implementation. The only connector currently in this code
base is a Kafka topic. The key sceanrio this can demonstrate is data being processed from a data science 
kafka topic.

## Implementation Data Flow Steps
1. The Kafka client connects to a particular broker and topic and checks if there is any data to process. 
2. If there is data it will audit the transaction processing 
3. The transaction will be routed for processing within iDAAS KIC
    
Supported properties include:
```properties
# Server - Internal
server.host=9983
# Kafka
kafkaBrokers=localhost:9092
# JDBC Database
spring.datasource.url=jdbc:mysql://localhost/idaas
                     #jdbc:postgresql://localhost:5432/idaas
spring.datasource.username=idaas
spring.datasource.password=@idaas123
#spring.database.driver-class-name=com.mysql.cj.jdbc.Driver
#org.postgresql.Driver
```

Happy using and coding....

