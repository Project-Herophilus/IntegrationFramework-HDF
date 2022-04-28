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
For this particular repository it has been tested and works with multiple FHIR servers. <br/>
<a href="https://github.com/hapifhir/hapi-fhir-jpaserver-starter" target="_blank">HAPI FHIR JPA Server</a><br/>
<a href="https://github.com/IBM/FHIR" target="_blank">IBM FHIR Server</a><br/>
<a href="https://github.com/microsoft/fhir-server" target="_blank">Microsoft Azure FHIR Server</a><br/>

Which ever FHIR server you implement you will need to follow the specific install instructions from each vendor.
While we have tested with all three of them there could be a need to reconfigure the connectivity details to it.

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
There are several key details you must make decisions on and also configure. For all of these you will 
need to update the application.properties in accordance with these decisions.

- processingToFHIR - if you are using a FHIR server or not. The setting of idaas.processToFHIR=false 
will bypass sending any data to any FHIR server. If set to true (idaas.processToFHIR=true) then
you will need to configure the specific FHIR server parameters.
- FHIR server (if using one needs to be defined within the specific URI). We DO NOT need the 
resource specific defined details as we build that based on industry standard implemntation based 
mandates/requirements.
- processTerminologies - if you want to process terminologies based on the data dlowing through the 
FHIR resources. If idaas.processTerminologies=true then all transactions will go to a specifically
defined component for another set of assets to process.
```
# fhirVendor can be ibm hapi or microsoft
idaas.fhirVendor=ibm
idaas.ibmURI=52.118.134.81:8090/fhir-server/api/v4/
idaas.hapiURI=localhost:8888/hapi-fhir-jpaserver/fhir/
idaas.msoftURI=localhost:0809/microsoftapi/api/v4/
# FHIR Specific
idaas.processToFHIR=false
idaas.processTerminologies=false
idaas.processBundles=false
```

# Specific Implementation Details: FHIR Resource Integration
This repository follows a very common general facility based implementation. The implementation
is of a facility, we have named MCTN for an application we have named MMS. This implementation 
specifically defines one FHIR endpoint per FHIR resource.

## Known Issues
As of the time of this content publication there are no known specific issues. The ONLY consistent
common issue is setting the application.properties before running the application.

## Implementation Data Flow Steps
 
1. This respository acts as an HTTP/HTTP(s) secure endpoint for processing FHIR Data. Each FHIR Resource has a 
specifically defined URL endpoint. It posts the transactions and gets a response back.
2. iDAAS Connect FHIR will do the following actions:<br/>
    a. Receive the FHIR message. Internally, it will audit the data it received to 
    a specifically defined topic.<br/>
    b. The FHIR message will then be processed to a specifically defined topic for this implementation. There is a 
    specific topic pattern -  for the facility and application each data type has a specific topic define for it.
    For example: FHIRSvr_AdverseEvent, FHIRSvr_Consent, etc. <br/>
    c. If the code is enabled then the FHIR resource data can be sent to an external FHIR server. If and external 
    FHIR server is configured the respomse from it will then be sent back to the FHIR client.<br/>
    d. The response is also sent to the auditing topic location.<br/>
    

Happy using and coding....

