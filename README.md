# iDaaS-Connect Background
Complete Repository for all iDaaS-Connect reference architecture/design pattern/accelerator(s). iDaaS Connect is based
upon Apache Camel.

Intelligent DaaS (Data as a Service) is intended to be a tier of reusable and extensible capabilities.
As such, it is all about enabling a consistent design pattern/accelerator based mindset to help healthcare
organizations innovate in a new and comprehensive manner. Our focus has been, and will continue to be, how
we simplify data access within healthcare and focus on ensuring <b>data is the asset</b>.

Here are a few key things to know as you read this:
* The basis of all the efforts for Intelligent DaaS is that <b>Data is the Asset</b> and this set of repositories are
  intended to help anyone connect and build innovative platforms for usage with a variety of data: healthcare
  and beyond.
* Intelligent DaaS (Data as a Service), or any of its components, ARE PROVIDED UNDER Apache-2 Open Source licenses AS IS. 
THEY ARE NOT PRODUCTS AND WILL NOT BECOME A PRODUCTS. We work very hard to ensure they continue to drive enabling
capabilities in a consitent manner, as a design pattern/accelerator. This specific repository is focused around providing
comprehensive healthcare connectivity and routing of data. 
* iDaaS-Connect is all about enabling connectivity to data. For ease of use and scale we have isolated each
  specific type of connectivity into its own specific solution. This repository is all about showcasing
  capabilities through software and is a value add.

# Reference Architecture/Design Pattern Based Platform 
Just to provide a few visuals to how to visualize it as we talk about each submodule within each part of the platform.
Below please find a visual that does visualize the entire iDaaS capabilities set. The key thing to note is while each specific iDaaS capability is purpose built and designed for any type of customer public or hybrid cloud our focus is on meeting data where it is securely and at scale.

Here is a detailed and Cloud Agnostic visual:<br/>
![iDaaS Cloud Agnostic Data Flow.png](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/images/iDaaS-Platform/Implementations/Implementations-Gen-CloudAgnostic.png)
<br/>

Here is a high level visual:<br/>
![iDaaS Data Flow - Detailed.png](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/images/iDaaS-Platform/iDAAS-DataFlow.png)
<br/>

# iDaaS-Connect is all about Integration 
Like most of the repositories provided for usage, The iDaaS Connect repository consists of numerous modules within it.
The reason for this approach is because iDaaS Connect focus is about providing a wide variety of connectivity options based
on specific needs. Here are the modules that are in iDaaS-Connect.

| Identifier                                         | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|----------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Blue Button](iDaaS-Connect-BlueButton/README.md)  | BlueButton is intended to be a very specific implementation to support puling of data to support several defined and specific government initiatives.                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| [Cloud](iDaaS-Connect-Cloud/README.md)             | Cloud is intended to be a very specific set of connectivity for the three major public cloud vendors - AWS, Azure and GCP.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| [EDI](iDaaS-Connect-EDI/README.md)                 | EDI has been a standard around for decades, this repository does not introduce capabilities that compete with capabilities vailable for claims processing or other EDI very specific needs. The intent of this repository it to enable the processing of EDI data such as claims and Supply chain.                                                                                                                                                                                                                                                                                                                 |
| [FHIR](iDaaS-Connect-FHIR/README.md)               | FHIR is a modern based integration standard that has been adopted by the government to assist them in addressing new federal mandates such as the Interoperability and Patient Access Rule. The iDaaS-Connect-FHIR component fully supports integrating to multiple  external vendor FHIR servers in a consistent design pattern manner. |
| [HL7](iDaaS-Connect-HL7/README.md)                 | HL7 v2 is a very legacy based client/server socket protocol that has been out for decades and has thousands of unique implementations across healthcare vendors. Additionally, CCDA was developed for web services based XML based document exchanges of clinical data.                                                                                                                                                                                                                                                                                                                                            |
| [SAP](iDaaS-Connect-SAP/README.md)                 | This iDaaS Connect accelerator is specifically designed to receive data from the various SAP supported connectors. This accelerator apart from handling the needed specific connectivity also does a minimal initial routing of data and has complete auditing integrated.                                                                                                                                                                                                                                                                                                                                         |
| [Third Party](iDaaS-Connect-ThirdParty/README.md)  | This iDaaS Connect accelerator is specifically designed to receive data from several dozens connectors. The connectorsinclude JDBC (any jdbc compliant data source with a jar), Kafka, FTP/sFTP and sFTP, AS400, HTTP(s), REST and many more. Since this accelerator is built atop the upstream of Apache Camel this accelerator can leverage any <a href="https://camel.apache.org/components/latest/index.html" target="_blank">supported components</a>. This accelerator apart from handling the needed specific connectivity also does a minimal initial routing of data and has complete auditing integrated. |


# Add-Ons
To support any iDaaS branded artifact there are a subset of assets avaiable to assist in the implementation : <br/>

| Add-On                                                                                                                             | Description                                                                                                                                                                                                                                                                                                               |
|------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Project Herophilus Technical Content](https://github.com/Project-Herophilus/Project-Herophilus-Assets/tree/main/Platform/Content) | Variety of content specific to iDaaS - PowerPoints, Word Docs, and Draw.IO diagrams                                                                                                                                                                                                                                       |
| [Test Data](https://github.com/Project-Herophilus/Project-Herophilus-Assets/tree/main/Testing)                                     | Test data for all healthcare industry standards work                                                                                                                                                                                                                                                                      |
| [Synthetic Data](https://github.com/Project-Herophilus/DataSynthesis)                                                              | When it comes to building or testing assets having lots of data to resemble you production data is critical, it also helps drive innovation. We have open sourced a synthetic data platform and continue to enahnce it based on feedback. It currently contains over 18 billion specific data attributes that can be used |
| [Data Simulators](https://github.com/Project-Herophilus/iDaaS-DataSimulators)                                                      | Having data is one aspect, the other is having tooling that can quickly be configured and used to test components. We have developed a data-simulators for our core components, Simulators to help in implementation and testing                                                                                          |

# Additional Add-Ons
To support any iDaaS branded artifact there are a subset of assets avaiable to assist in the implementation : <br/>

|Add-On | Description |
| ------------ | ----------- |
| API - Testing|Leverage the published [PostmanCollections](https://universal-capsule-967150.postman.co/users/3200250), they are named by community and specific reference architecture they support.|

# Administering the Platform - Management and Insight of Components
Administering and seeing what the plaform is doing is also critical. Within each specific repository there is an administrative user interface that allows for monitoring and insight into the connectivity of any endpoint. Additionally, there is also the implementation to enable implementations to build there own by exposing the metadata. The data is exposed and can be used in numerous very common tools like Data Dog, Prometheus and so forth.
This capability to enable would require a few additional properties to be set.

Below is a generic visual of how this looks (the visual below is specific to iDaaS Connect HL7): <br/>

![iDaaS Platform - Visuals - iDaaS Data Flow - Detailed](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/images/iDaaS-Platform/iDaaS-Mgmt-UI.png)

Every asset has its own defined specific port, we have done this to ensure multiple solutions can be run simultaneously.

## Administrative Interface(s) Specifics
For all the URL links we have made them localhost based, simply change them to the server the solution is running on.

| iDaaS Connect Asset       | Port | Admin URL   | JMX URL|                                                                                   
|---------------------------| ----   |--------------------------------------------------|------------------------------------------------------------------------------------------| 
| iDaaS Connect HL7         | 9980| http://localhost:9980/actuator/hawtio/index.html                                                                                           | http://localhost:9980/actuator/jolokia/read/org.apache.camel:context=*,type=routes,name=* | 
| iDaaS Connect FHIR        | 9981| http://localhost:9981/actuator/hawtio/index.html                                                                                           | http://localhost:9981/actuator/jolokia/read/org.apache.camel:context=*,type=routes,name=* |  
| iDaaS Connect BlueButton  | 9982| http://localhost:9982/actuator/hawtio/index.html                                                                                           | http://localhost:9982/actuator/jolokia/read/org.apache.camel:context=*,type=routes,name=* |  
| iDaaS Connect Third Party | 9983| http://localhost:9983/actuator/hawtio/index.html                                                                                           | http://localhost:9983/actuator/jolokia/read/org.apache.camel:context=*,type=routes,name=* |  
| iDaaS Connect EDI         | 9984| http://localhost:9984/actuator/hawtio/index.html                                                                                           | http://localhost:9984/actuator/jolokia/read/org.apache.camel:context=*,type=routes,name=* |  
| iDaaS Connect Cloud       | 9985| http://localhost:9985/actuator/hawtio/index.html                                                                                           | http://localhost:9985/actuator/jolokia/read/org.apache.camel:context=*,type=routes,name=* |  
| iDaaS Connect SAP         | 9986| http://localhost:9986/actuator/hawtio/index.html | http://localhost:9986/actuator/jolokia/read/org.apache.camel:context=*,type=routes,name=* |  

# Platform General Pre-Requisites

For all iDaaS design patterns it should be assumed that you will either install as part of this effort or need the [following](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/PreRequisites.md) on any environment you choose to implement the platfom on.
