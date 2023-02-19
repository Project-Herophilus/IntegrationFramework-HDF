# Integration Framework for HDF (Healthcare Data Foundation) Background
the Integration Framework for HDF (Healthcare Data Foundation) is intended to be a tier of reusable and extensible capabilities.
As such, it is all about enabling a consistent design pattern/accelerator based mindset to help healthcare
organizations innovate in a new and comprehensive manner. Our focus has been, and will continue to be, how
we simplify data access within healthcare and focus on ensuring <b>data is the asset</b>.

Here are a few key things to know as you read this:
* All the Integration Framework for HDF assets, like all assets within Project-Herophilus,  are available and provided "as-is" under
  the [Apache 2 license](https://www.apache.org/licenses/LICENSE-2.0) model.
* The basis of all the efforts for Intelligent DaaS is that <b>Data is the Asset</b> and this set of repositories are
  intended to help anyone connect and build innovative platforms for usage with a variety of data: healthcare
  and beyond.
* Intelligent DaaS (Data as a Service), or any of its components, ARE PROVIDED UNDER Apache-2 Open Source licenses AS IS. 
THEY ARE NOT PRODUCTS AND WILL NOT BECOME A PRODUCTS. We work very hard to ensure they continue to drive enabling
capabilities in a consitent manner, as a design pattern/accelerator. This specific repository is focused around providing
comprehensive healthcare connectivity and routing of data. 
* Integration Framework for HDF is all about enabling connectivity to data. For ease of use and scale we have isolated each
  specific type of connectivity into its own specific solution. This repository is all about showcasing
  capabilities through software and is a value add.
* This platform has everything of other integration platforms such as management interfaces. There is also the capability 
to quickly and easily customize where you want logs to end up with numerous industry leading technologies such as Data Dog, Splunk, 
etc. In addition, the platform enables implementations to also build highly customized visualizd dashboards as well
simply with Grafana.

# Integration Framework for HDF (Healthcare Data Foundation) - A Proven Reference Architecture/Design Pattern Based Platform
Just to provide a few visuals to how to visualize it as we talk about each submodule within each part of the platform.
Below please find a visual that does visualize the entire iDaaS capabilities set. The key thing to note is while each
specific iDaaS capability is purpose built and designed for any type of customer, without regard for implementation
type, to ensure our focus is on meeting data where it is securely and at scale.

## Processors and Converters
Each Integration Framework for HDF component/module has a consistent pattern within it, this approach consists of having processes
and converters. By doing this you can fully customize the experience needed for the most common tasks.
This design enables the simplist to most complex implementations to be delivered consistently.

### Processors
Processors, as hopefully depicted by the name, are intended to be for specific processing needs for data enablement
capabilities.

| Processor                                   | Description                                                         |
|---------------------------------------------|---------------------------------------------------------------------|
| Cloud                                       | Processing for public cloud components                              |                                                                                                                                                                                                                                                                                                                                                                                                                              |
| Deidentification                            | Processing for deidentification                                     |
| EMPI                                        | Processing for EMPI (Enteprise Master Person Index)                 |
| HEDA (Healthcare Event Driven Architecture) | Processing for built a specific healthcare event driven architcture |
| SDOH                                        | Processing for SDOH (Social Determinants) data                      |
| Terminologies                               | Processing for terminologies                                        |

### Converters
Converters are designed with ONLY one function, to convert data from one format to another. As of Integration Framework for HDF v4 there
is ONLY code based conversions available. However, there has been some work to start enabling mapping based conversions.

| Processor  | Description                              |
|------------|------------------------------------------|
| CCDAtoFHIR | Converting from CCDA Data format to FHIR |
| HL7toFHIR  | Converting from HL7v2 to FHIR            |

## Statistics
Stats are now included for all components that are leveraged. Statistics have a naming convention
for ease of use.

| Processor             | Description                      | Sample Naming Convention                         |
|-----------------------|----------------------------------|--------------------------------------------------|
| Errors                | Any exception/error handling     | (module name)_exception                          |
| Processing Components | Common processing components     | (Procesing Component Name)_Inbd_ProcessedEvent   |
| Converters            | Conversion components            | (converter)Conversion_(Direction)_ProcessedEvent |
| General               | Any component within any modules | (Protocol)_(TransactionType)_Inbd_ProcessedEvent |

# Integration Framework for HDF is all about Connecting and Data Integration
Like most of the repositories provided for usage, The iDaaS Connect repository consists of numerous modules within it.
The reason for this approach is because iDaaS Connect focus is about providing a wide variety of connectivity options based
on specific needs or industry centric specific connectivity needs.

Here is a detailed and Cloud Agnostic visual:<br/><br/>
![iDaaS Cloud Agnostic Data Flow.png](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/images/iDaaS-Platform/Implementations-Gen-CloudAgnostic.png)
<br/>

Here are the modules that are in Integration Framework for HDF.

| Identifier                                             | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|--------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Blue Button](cms-blue-button/README.md)               | BlueButton is intended to be a very specific implementation to support puling of data to support several defined and specific government initiatives.                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| [Cloud](cloud/README.md)                               | Cloud is intended to be a very specific set of connectivity for the three major public cloud vendors - AWS, Azure and GCP.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| [CMS Interoperability](cms-interoperability/README.md) | CMS Interoperability is intended to be a very specific project to implement the wide variety CMS Interoperability use cases.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| [EDI](edi/README.md)                                   | EDI has been a standard around for decades, this repository does not introduce capabilities that compete with capabilities vailable for claims processing or other EDI very specific needs. The intent of this repository it to enable the processing of EDI data such as claims and Supply chain.                                                                                                                                                                                                                                                                                                                 |
| [FHIR](fhir/README.md)                                 | FHIR is a modern based integration standard that has been adopted by the government to assist them in addressing new federal mandates such as the Interoperability and Patient Access Rule. The Integration Framework for HDF-FHIR component fully supports integrating to multiple  external vendor FHIR servers in a consistent design pattern manner.                                                                                                                                                                                                                                                                           |
| [HL7](hl7/README.md)                                   | HL7 v2 is a very legacy based client/server socket protocol that has been out for decades and has thousands of unique implementations across healthcare vendors. Additionally, CCDA was developed for web services based XML based document exchanges of clinical data.                                                                                                                                                                                                                                                                                                                                            |
| [NCPDP](ncpdp/README.md)                               | This iDaaS Connect accelerator is all about providing the capability to process NCPDP data. This accelerator apart from handling the needed specific connectivity also does a minimal initial routing of data and has complete auditing integrated.                                                                                                                                                                                                                                                                                                                                            |
| [SAP](sap/README.md)                                   | This iDaaS Connect accelerator is specifically designed to receive data from the various SAP supported connectors. This accelerator apart from handling the needed specific connectivity also does a minimal initial routing of data and has complete auditing integrated.                                                                                                                                                                                                                                                                                                                                         |
| [Third Party](thirdparty/README.md)                    | This iDaaS Connect accelerator is specifically designed to receive data from several dozens connectors. The connectorsinclude JDBC (any jdbc compliant data source with a jar), Kafka, FTP/sFTP and sFTP, AS400, HTTP(s), REST and many more. Since this accelerator is built atop the upstream of Apache Camel this accelerator can leverage any <a href="https://camel.apache.org/components/latest/index.html" target="_blank">supported components</a>. This accelerator apart from handling the needed specific connectivity also does a minimal initial routing of data and has complete auditing integrated. |

## Community Add-Ons
To support any iDaaS branded artifact there are a subset of assets avaiable to assist in the implementation : <br/>

| Add-On                                                                                                                             | Description                                                                                                                                                                                                                                                                                                               |
|------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Project Herophilus Technical Content](https://github.com/Project-Herophilus/Project-Herophilus-Assets/tree/main/Platform/Content) | Variety of content specific to iDaaS - PowerPoints, Word Docs, and Draw.IO diagrams                                                                                                                                                                                                                                       |
| [Test Data](https://github.com/Project-Herophilus/Project-Herophilus-Assets/tree/main/Testing)                                     | Test data for all healthcare industry standards work                                                                                                                                                                                                                                                                      |
| [Synthetic Data](https://github.com/Project-Herophilus/DataSynthesis)                                                              | When it comes to building or testing assets having lots of data to resemble you production data is critical, it also helps drive innovation. We have open sourced a synthetic data platform and continue to enahnce it based on feedback. It currently contains over 18 billion specific data attributes that can be used |
| [Data Simulators](https://github.com/Project-Herophilus/iDaaS-DataSimulators)                                                      | Having data is one aspect, the other is having tooling that can quickly be configured and used to test components. We have developed a data-simulators for our core components, Simulators to help in implementation and testing                                                                                          |

## Additional Add-Ons
To support any iDaaS branded artifact there are a subset of assets avaiable to assist in the implementation : <br/>

|Add-On | Description |
| ------------ | ----------- |
| API - Testing|Leverage the published [PostmanCollections](https://universal-capsule-967150.postman.co/users/3200250), they are named by community and specific reference architecture they support.|


# Administering the Platform - Management and Insight of Components
Administering and seeing what the plaform is doing is also critical. Within each specific repository there is an administrative user interface that allows for monitoring and insight into the connectivity of any endpoint. Additionally, there is also the implementation to enable implementations to build there own by exposing the metadata. The data is exposed and can be used in numerous very common tools like Data Dog, Prometheus and so forth.
This capability to enable would require a few additional properties to be set.

Below is a generic visual of how this looks (the visual below is specific to iDaaS Connect HL7): <br/>

![Integration Framework for HDF Data Flow - Detailed](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/images/iDaaS-Platform/iDaaS-Mgmt-UI.png)

For more specific details around the management and administrative capabilities you can go [here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/AdministeringPlatform.md).


# Platform General Pre-Requisites

For all iDaaS design patterns it should be assumed that you will either install as part of this effort or need the [following](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/PreRequisites.md) on any environment you choose to implement the platfom on.

