# iDAAS Connect EDI
iDAAS Connect for Processing EDI data - This effort has started to support the HIPAA Compliant 5010 EDI transactions
but will be moving into Supply Chain transactions. This design pattern is intended to parse, build and enable
the usage of 5010 EDI and Supply Chain data.

# Focus on Improving
We are focusing on continuing to improve. With the numerous implementation and partner implementations we
have focused on success overall and as we progress forward the intent is to focus on success while being consistent.
Please find details on how to help us [here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/OngoingEnhancements.md).

# Pre-Requisites
For any repository to be implemented there are two types of requirements, overall general requirements
and then there are specific submodule requirements.

## General Pre-Requisites
For all iDaaS Connect branded solutions there are some general content which can be looked at
here in [detail](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/CloningBuildingRunningSolution.md)

## Specific Implementation Pre-Requisites
As of this content release there are no specific prerequisites, all of them will be downloaded as
part of the build process (covered above in the General Pre-Requisites section).

# Administrative Console
Within each implementation there is a management console, the management console provides the same
interface and capabilities no matter what implementation you are working within. Specifics on the
Admin/Mgmt interface can be found
[here](https://github.com/Project-Herophilus/Project-Herophilus-Assets/blob/main/AdministeringPlatform.md).

# Specific Implementation Details
The following section is intended to cover specific implementation known issues, challenges and potential implementation
details.

## Known Issues
As of the time of this content publication there are no known specific issues. The ONLY consistent
common issue is setting the application.properties before running the application.

## Implementation Example(s): EDI Data Processing
This repository follows a very common general implementation of processing a file from a filesystem. The intent is to pick
up the file and process it and then leverage the existing iDaaS-EventBuilder library to show it being processed and manipulated.

### Implementation Data Flow Steps

1. Every 1 minute the defined directory is looked at for any .edi file, if found the file is processed into a matching structure.
2. The data structure is then persisted into a kafka topic.

Happy using and coding....
