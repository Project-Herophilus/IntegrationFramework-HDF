# iDAAS-Connect-ThirdParty
iDAAS-Connect-ThirdParty is ONLY intended to deal with enabling connectivity to all sorts of different types of
data systems. For example: RDBMS, Kafka, Mainframe, Files, SFTP, etc. plus over one hundred other types of connectivity  
are supported.

# Focus on Improving
We are focusing on continuing to improve. With the numerous implementation and partner implementations we
have focused on success overall and as we progress forward the intent is to focus on success while being consistent.
Please find details on how to help us [here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/OngoingEnhancements.md).

# Pre-Requisites
For any repository to be implemented there are two types of requirements, overall general requirements
and then there are specific submodule requirements. We try and maintain as close to a current detailed list
as we can, for those specifics please check [here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/PreRequisites.md).

# Connectivity and Scenarios Provided within the Code
Within this module the following connectivity scenarios and examples are provided.

## Converters and Processes
- Each endpoint connectivity has configurable processes built in for empi, heda (healthcare event
  driven architecture), datatier, deidentificaton, public cloud, SDOH and terminologies.

## Endpoint Connectivity
- Rest endpoint for IoT data.
- SFTP File retrieval.
- Rest endpoint created to provide a file list, this example uses AWS S3 but could use any connectable protocol.
- Rest endpoint that enables retrieval of files, it works off the same AWS S3 source the file list does.
- Rest endpoint that queries a database table and provides a data driven response.

# Implementating this Module
The following section is designed to cover the details around implementing.

## Implementation Guides to Help
For the specifics around one or more specific implementations for this module please feel free to look
[here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/Platform-Content/ImplementationGuides/intro.md).

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
As of the time of this content publication there are no known implementation centric issues that have been reported.