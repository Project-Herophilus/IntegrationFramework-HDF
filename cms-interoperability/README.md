# iDAAS Connect CMS-Interoperability
iDAAS Connect for Processing EDI data - This effort has started to support the HIPAA Compliant 5010 EDI transactions
but will be moving into Supply Chain transactions. This design pattern is intended to parse, build and enable
the usage of 5010 EDI and Supply Chain data.

# Focus on Improving
We are focusing on continuing to improve. With the numerous implementation and partner implementations we
have focused on success overall and as we progress forward the intent is to focus on success while being consistent.
Please find details on how to help us [here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/OngoingEnhancements.md).

# Pre-Requisites
For any repository to be implemented there are two types of requirements, overall general requirements
and then there are specific submodule requirements. We try and maintain as close to a current detailed list
as we can, for those specifics please check [here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/PreRequisites.md).

It is important to understand that this design pattern is intended to simplify the ability to connect to data and 
wire it to key internal systems within an implementation. We want to ensure implementors understand
we stand behind the framework to provide the ability to process data <b>BUT</b> in no way is advertising that we do 
these industy mandated processing needs out of the box, every vendor will need to connect to and customize based
on implementation needs.

# Connectivity and Scenarios Provided within the Code
Within this module the following connectivity scenarios and examples are provided.

## Converters and Processes
- Each endpoint connectivity has configurable processes built in for empi, heda (healthcare event
  driven architecture), datatier, deidentificaton, public cloud, SDOH and terminologies.

## Endpoint Connectivity
- Rest endpoints for xdsrequest, pixaddupdate and providerreqdocs.
- Rest endpoint for Prior Authorization.
- Rest endpoints for Davinci: Data Exchange Quality Matrix, Clinical Data Exchange, Payer Data Exchange, and 
Performing Lab Reporting.

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
As of the time of this content publication there are no known specific issues.

