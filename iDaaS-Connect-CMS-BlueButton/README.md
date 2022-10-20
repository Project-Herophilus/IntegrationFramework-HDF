# iDAAS Connect BlueButton
iDaaS-Connect-BlueButton enables the connecting to and processing of Medicare data of an authenticated beneficiary through the 
[Blue Button API](https://bluebutton.cms.gov/) and sends it to a Kafka topic. This application
serves as a webserver. User opens the served URL using a web browser and log into the Medicare database. 
The application will automatically fetch part A, B, C, and D data and sends it to a Kafka topic. Then other 
processors can subscribe to the topic to process the data.

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
1. Sign up for the blue button [developer sandbox](https://bluebutton.cms.gov/).
2. Create a new application with the following. Then write down the resulting Client ID and Client Secret
* OAuth - Client Type: confidential
* OAuth - Grant Type: authorization-code
* Callback URLS: http://localhost:8890/callback (or another url more appropriate)

All of these properties can be placed into the application.properties files in src/main/resouorces or
you can add a custom application.properties file location as you test this, the steps are levrage a custom
application.properties file can be found in the same content listed above in the General Pre-Requisite section.

```
bluebutton.callback.path=callback
bluebutton.callback.host=localhost
bluebutton.callback.port=8890
```
Note:
http://localhost:8890/callback is the callback URL you registered with bluebutton.cms.gov. http://localhost:8890/bluebutton will be the service URL for iDAAS-Connect-BlueButton.
Every asset has its own defined specific port, we have done this to ensure multiple solutions can be run simultaneously.

## Known Issues
As of the time of this content publication there are no known specific issues. The ONLY consistent
common issue is setting the application.properties before running the application.
 
## Implementation Data Flow Steps
* Download the [CSV file](https://bluebutton.cms.gov/synthetic_users_by_claim_count_full.csv) which contains 100 sample data with id, user name, and password.
* Build the project 
* Make sure the application.properties are configured for your specific parameters defined when you setup your CMS account
* Start the project
* In a web browser type http://localhost:8890/bluebutton (this is bluebutton.callback.port setting in application.properties)
* It will automatically redirect you to Blue Buttons's authentication page. Fill in the user name and password
* Patient, Coverage, and ExplanationOfBenifit data will be sent to Kafka topic named bluebutton.

Happy Coding!!!!