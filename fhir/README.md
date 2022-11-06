# iDaaS Connect FHIR
This is the upstream for RedHat Healthcare's <a href="https://github.com/RedHat-Healthcare/iDaaS-Connect/tree/master/iDaaS-Connect-FHIR" target="_blank">iDaaS Connect FHIR</a>. iDAAS has several key components that provide many capabilities. iDAAS Connect is intended ONLY
to enable iDAAS connectivity. iDAAS-Connect-FHIR specifically ONLY deals with enabling 
iDAAS to process the healthcare industry standard FHIR based resources ONLY. Here is the 
<a href="https://www.hl7.org/fhir/resourcelist.html" target="_blank">current FHIR Resource List</a> 
It will process over 80+ of the currently available resources - around 40 clinical FHIR 
resources, all the financial public health and research/evidence based medicine/and quality
reporting and testing resources. You can also find a list of the 
<a href="http://connectedhealth-idaas.io/home/SupportedTransactions" target="_blank">platforms supported transactions</a> 

When designing anb building this capability we wanted to ensure that this was an enablement. This means that we focused 
on the ability to enable systems to process FHIR resource data and determine if they wanted to persist the specific FHIR 
resources to a FHIR server. If they did then we made sure the design pattern included supporting multiple FHIR servers. 
So, whatever your choice to process to a FHIR server or not, we enable all these behaviors with simple configuration so
you don't have to do anything but make simple configuration changes in the application.properties.

## FHIR Servers Supported
For this particular repository it has been tested and works with multiple FHIR server.
If you follow the implementation guide you can easily configure the FHIR settings to enable FHIR
processing and then the specific FHIR server/cloud service  you are trying to send data to. 
Which ever FHIR server you implement you will need to follow the specific install instructions/connectivity instructions
from each vendor. 

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
- Rest endpoint for FHIR Message processing.

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