# iDaaS Connect HL7
iDAAS-Connect-HL7 ONLY deals with enabling
connecting to/from healthcare industry standard HL7. It also can process data for CCDA based transactions ONLY.
The following transactions are supported:
- HL7 messages (ADT, ORM, ORU, MFN, MDM, PHA, SCH and VXU) from any vendor and any version of HL7 v2.
- CCDA Events 

For ease of use and implementation this design pattern has built in support for the following protocols to support data processing:
- MLLP (Minimal Lower Layer Protocol - HL7 v2 Protocol)
- Files
- HTTP(s) endpoint for CCDA

# Focus on Improving
We are focusing on continuing to improve. With the numerous implementation and partner implementations we
have focused on success overall and as we progress forward the intent is to focus on success while being consistent.
Please find details on how to help us [here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/OngoingEnhancements.md).

# Pre-Requisites
For any repository to be implemented there are two types of requirements, overall general requirements
and then there are specific submodule requirements.

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

## Specific Implementation Configuration Details  
There are several key details you must make decisions on and also configure. For all of these you will
need to update the application.properties in accordance with these decisions.
- For every HL7 based connection you can specify a specific directory, port and whether or not 
you want to process the ACK/NAK responses.
- CCDA. Key setting is whether you want to automatically convert the data with the setting
  idaas.ccdaConvert=true
- processTerminologies - if you want to process terminologies based on the data dlowing through the
  HL7 and CCDA transactions. If idaas.processTerminologies=true then all transactions will go to a specifically
  defined component for another set of assets to process.
- convertToFHIR - is about specifically converting only HL7 messages to FHIR automatically with the
setting idaas.convert2FHIR=true
- Coming soon are the ability to automatically anonymize or deidentify data. with the settings 
  idaas.deidentify=false and idaas.anonymize=false
```
# Basics on properties
idaas.hl7ADT_Directory=data/adt
idaas.adtPort=10001
idaas.adtACKResponse=true
idaas.adtTopicName=mctn_mms_adt2
idaas.hl7ORM_Directory=data/orm
idaas.ormPort=10002
idaas.ormACKResponse=true
idaas.ormTopicName=mctn_mms_orm
idaas.hl7ORU_Directory=data/oru
idaas.oruPort=10003
idaas.oruACKResponse=true
idaas.oruTopicName=mctn_mms_oru
idaas.hl7RDE_Directory=data/rde
idaas.rdePort=10004
idaas.rdeACKResponse=true
idaas.rdeTopicName=mctn_mms_rde
idaas.hl7MFN_Directory=data/mfn
idaas.mfnPort=10005
idaas.mfnACKResponse=true
idaas.mfnTopicName=mctn_mms_mfn
idaas.hl7MDM_Directory=data/mdm
idaas.mdmPort=10006
idaas.mdmACKResponse=true
idaas.mdmTopicName=mctn_mms_mdm
idaas.hl7SCH_Directory=data/sch
idaas.schPort=10007
idaas.schACKResponse=true
idaas.schTopicName=mctn_mms_sch
idaas.hl7VXU_Directory=data/vxu
idaas.vxuPort=10008
idaas.vxuACKResponse=true
idaas.vxuTopicName=mctn_mms_vxu
# CCDA
idaas.hl7ccda_Directory=data/ccda
idaas.ccdaTopicName=mctn_mms_ccda
idaas.ccdaConvert=true
# FHIR bundle
idaas.fhirBundleTopicName=mctn_mms_ccda
# Other Settings
idaas.processTerminologies=false
idaas.convert2FHIR=false
idaas.deidentify=false
idaas.anonymize=false

```

# Specific Implementation Details: HL7 Message Integration
This repository follows a very common general facility based implementation. The implementation
is of a facility, we have named MCTN for an application we have named MMS. This implementation
specifically defines one FHIR endpoint per FHIR resource.

## Known Issues
As of the time of this content publication there are some specifically known issues with 
iDaaS Connect HL7 and how OS'es can allocate and leverage ports with hostnames. This is most
often seen because IPV6 is enabled.

### General Issues: HL7
1. IPV6 needs to be disabled and IPv4 needs to be enabled.

#### Red Hat Enterprise Linux

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
### Implementation Data Flow Specifics 
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

### Implementation Data Flow Steps: HL7
Here is a general visual intended to show the general data flow and how the accelerator design pattern is intended to work. <br/>
 <img src="https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/images/iDaaS-Platform/DataFlow-HL7.png" width="800" height="600">

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


Happy using and coding....

