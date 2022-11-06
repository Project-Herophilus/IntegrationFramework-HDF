# iDaaS Connect HL7
iDAAS-Connect-HL7 ONLY deals with enabling
connecting to/from healthcare industry standard HL7. It also can process data for CCDA based transactions ONLY.
The following transactions are supported:
- HL7 messages (ADT, ORM, ORU, MFN, MDM, PHA, SCH and VXU) from any vendor and any version of HL7 v2.
- CCDA Messages

For ease of use and implementation this design pattern has built in support for the following protocols to support data processing:
- MLLP (Minimal Lower Layer Protocol - HL7 v2 Protocol)
- HTTP(s) endpoints for CCDA and HL7 as needed

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
- Configurable Converters from HL7v2 and CCDA to FHIR.
- Each endpoint connectivity has configurable processes built in for empi, heda (healthcare event
driven architecture), datatier, deidentificaton, public cloud, SDOH and terminologies.

## Endpoint Connectivity
- Rest endpoint for HL7
- Rest edpoint for CCDA
- HL7 MLLP connectivity for ADT, MDM, MFN, ORM, ORU, SCH, RDE and VXU.

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



