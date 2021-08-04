/*
 * Copyright 2019 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.connectedhealth_idaas.fhir;

import ca.uhn.fhir.store.IAuditDataStore;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.MultipleConsumersSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

//import org.springframework.jms.connection.JmsTransactionManager;
//import javax.jms.ConnectionFactory;
import org.springframework.stereotype.Component;
//import sun.util.calendar.BaseCalendar;
import java.time.LocalDate;

@Component
public class CamelConfiguration extends RouteBuilder {
  private static final Logger log = LoggerFactory.getLogger(CamelConfiguration.class);

  @Autowired
  private ConfigProperties config;

  @Bean
  private KafkaEndpoint kafkaEndpoint(){
    KafkaEndpoint kafkaEndpoint = new KafkaEndpoint();
    return kafkaEndpoint;
  }
  @Bean
  private KafkaComponent kafkaComponent(KafkaEndpoint kafkaEndpoint){
    KafkaComponent kafka = new KafkaComponent();
    return kafka;
  }

  @Bean
  ServletRegistrationBean camelServlet() {
    // use a @Bean to register the Camel servlet which we need to do
    // because we want to use the camel-servlet component for the Camel REST service
    ServletRegistrationBean mapping = new ServletRegistrationBean();
    mapping.setName("CamelServlet");
    mapping.setLoadOnStartup(1);
    mapping.setServlet(new CamelHttpTransportServlet());
    mapping.addUrlMappings("/projherophilus/*");
    return mapping;
  }

  private String getKafkaTopicUri(String topic) {
    return "kafka:" + topic +
            "?brokers=" +
            config.getKafkaBrokers();
  }

  private String getFHIRServerUri(String fhirResource) {
    String fhirServerVendor = config.getFhirVendor();
    String fhirServerURI = null;
    if (fhirServerVendor.equals("ibm"))
    {
      //.to("jetty:http://localhost:8090/fhir-server/api/v4/AdverseEvents?bridgeEndpoint=true&exchangePattern=InOut")
      fhirServerURI = "http:"+config.getIbmURI()+fhirResource+"?bridgeEndpoint=true";
    }
    if (fhirServerVendor.equals("hapi"))
    {
      fhirServerURI = "http:"+config.getHapiURI()+fhirResource+"?bridgeEndpoint=true";
    }
    if (fhirServerVendor.equals("microsoft"))
    {
      fhirServerURI = "http:"+config.getMicrosoftURI()+fhirResource+"?bridgeEndpoint=true";
    }
    return fhirServerURI;
  }

  /*
   * Kafka implementation based upon https://camel.apache.org/components/latest/kafka-component.html
   *
   */
  @Override
  public void configure() throws Exception {
    // https://tomd.xyz/camel-rest/
    // Rest Configuration
    // Define the implementing component - and accept the default host and port
    restConfiguration().component("servlet")
        .host("0.0.0.0").port(String.valueOf(simple("{{server.port}}")));

    /*
     *   HIDN
     *   HIDN - Health information Data Network
     *   Intended to enable simple movement of data aside from specific standards
     *   Common Use Cases are areas to support remote (iOT/Edge) and any other need for small footprints to larger
     *   footprints
     * : Unstructured data, st
     */
    from("direct:hidn")
         .routeId("HIDN-Endpoint")
         .setHeader("messageprocesseddate").simple("${date:now:yyyy-MM-dd}")
         .setHeader("messageprocessedtime").simple("${date:now:HH:mm:ss:SSS}")
         .setHeader("eventdate").simple("eventdate")
         .setHeader("eventtime").simple("eventtime")
         .setHeader("processingtype").exchangeProperty("processingtype")
         .setHeader("industrystd").exchangeProperty("industrystd")
         .setHeader("component").exchangeProperty("componentname")
         .setHeader("processname").exchangeProperty("processname")
         .setHeader("organization").exchangeProperty("organization")
         .setHeader("careentity").exchangeProperty("careentity")
         .setHeader("customattribute1").exchangeProperty("customattribute1")
         .setHeader("customattribute2").exchangeProperty("customattribute2")
         .setHeader("customattribute3").exchangeProperty("customattribute3")
         .setHeader("camelID").exchangeProperty("camelID")
         .setHeader("exchangeID").exchangeProperty("exchangeID")
         .setHeader("internalMsgID").exchangeProperty("internalMsgID")
         .setHeader("bodyData").exchangeProperty("bodyData")
         .setHeader("bodySize").exchangeProperty("bodySize")
         .convertBodyTo(String.class).to(getKafkaTopicUri("hidn"))
    ;
    /*
     * Audit
     *
     * Direct component within platform to ensure we can centralize logic
     * There are some values we will need to set within every route
     * We are doing this to ensure we dont need to build a series of beans
     * and we keep the processing as lightweight as possible
     *
     */
    from("direct:auditing")
        .routeId("iDaaS-KIC")
        .setHeader("messageprocesseddate").simple("${date:now:yyyy-MM-dd}")
        .setHeader("messageprocessedtime").simple("${date:now:HH:mm:ss:SSS}")
        .setHeader("processingtype").exchangeProperty("processingtype")
        .setHeader("industrystd").exchangeProperty("industrystd")
        .setHeader("component").exchangeProperty("componentname")
        .setHeader("messagetrigger").exchangeProperty("messagetrigger")
        .setHeader("processname").exchangeProperty("processname")
        .setHeader("auditdetails").exchangeProperty("auditdetails")
        .setHeader("camelID").exchangeProperty("camelID")
        .setHeader("exchangeID").exchangeProperty("exchangeID")
        .setHeader("internalMsgID").exchangeProperty("internalMsgID")
        .setHeader("bodyData").exchangeProperty("bodyData")
        .convertBodyTo(String.class).to(getKafkaTopicUri("opsmgmt_platformtransactions"))
    ;
    /*
    *  Logging
    */
    from("direct:logging")
        .routeId("Logging")
        .log(LoggingLevel.INFO, log, "FHIR Message: [${body}]")
        //To invoke Logging
        //.to("direct:logging")
    ;

    /*
    *   General iDaaS Platform
    */
    rest("/api/hidn")
        .post()
        .route()
        .routeId("API-HIDN")
         // Data Parsing and Conversions
        // Normal Processing
        .convertBodyTo(String.class)
        .setHeader("messageprocesseddate").simple("${date:now:yyyy-MM-dd}")
        .setHeader("messageprocessedtime").simple("${date:now:HH:mm:ss:SSS}")
        .setHeader("eventdate").simple("eventdate")
        .setHeader("eventtime").simple("eventtime")
        .setHeader("processingtype").exchangeProperty("processingtype")
        .setHeader("industrystd").exchangeProperty("industrystd")
        .setHeader("component").exchangeProperty("componentname")
        .setHeader("processname").exchangeProperty("processname")
        .setHeader("organization").exchangeProperty("organization")
        .setHeader("careentity").exchangeProperty("careentity")
        .setHeader("customattribute1").exchangeProperty("customattribute1")
        .setHeader("customattribute2").exchangeProperty("customattribute2")
        .setHeader("customattribute3").exchangeProperty("customattribute3")
        .setHeader("camelID").exchangeProperty("camelID")
        .setHeader("exchangeID").exchangeProperty("exchangeID")
        .setHeader("internalMsgID").exchangeProperty("internalMsgID")
        .setHeader("bodyData").exchangeProperty("bodyData")
        .setHeader("bodySize").exchangeProperty("bodySize")
        .wireTap("direct:hidn")
    ;

    from("servlet://hidn")
        .routeId("HIDN")
         // Data Parsing and Conversions
         // Normal Processing
        .convertBodyTo(String.class)
        .setHeader("messageprocesseddate").simple("${date:now:yyyy-MM-dd}")
        .setHeader("messageprocessedtime").simple("${date:now:HH:mm:ss:SSS}")
        .setHeader("eventdate").simple("eventdate")
        .setHeader("eventtime").simple("eventtime")
        .setHeader("processingtype").exchangeProperty("processingtype")
        .setHeader("industrystd").exchangeProperty("industrystd")
        .setHeader("component").exchangeProperty("componentname")
        .setHeader("processname").exchangeProperty("processname")
        .setHeader("organization").exchangeProperty("organization")
        .setHeader("careentity").exchangeProperty("careentity")
        .setHeader("customattribute1").exchangeProperty("customattribute1")
        .setHeader("customattribute2").exchangeProperty("customattribute2")
        .setHeader("customattribute3").exchangeProperty("customattribute3")
        .setHeader("camelID").exchangeProperty("camelID")
        .setHeader("exchangeID").exchangeProperty("exchangeID")
        .setHeader("internalMsgID").exchangeProperty("internalMsgID")
        .setHeader("bodyData").exchangeProperty("bodyData")
        .setHeader("bodySize").exchangeProperty("bodySize")
        .wireTap("direct:hidn")
    ;

    /*
     *  Any endpoint defined would be managed by access http(s)://<servername>:<serverport>/<resource name>
     */
    /*
     *  Bundle Processing
    */
        from("servlet://bundle").noAutoStartup()
        .routeId("FHIRBundles")
        .convertBodyTo(String.class)
        // set Auditing Properties
        .setProperty("processingtype").constant("data")
        .setProperty("appname").constant("iDAAS-Connect-FHIR")
        .setProperty("messagetrigger").constant("Bundles")
        .setProperty("component").simple("${routeId}")
        .setProperty("camelID").simple("${camelId}")
        .setProperty("exchangeID").simple("${exchangeId}")
        .setProperty("internalMsgID").simple("${id}")
        .setProperty("bodyData").simple("${body}")
        .setProperty("processname").constant("Input")
        .setProperty("auditdetails").constant("Bundle resource/bundle received")
        // iDAAS DataHub Processing
        .wireTap("direct:auditing")
        // Send to FHIR Server
        .choice().when(simple("{{idaas.processToFHIR}}"))
            .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
            // This needs to deserialize the event and build individual resources
            // as a Bean
            .convertBodyTo(String.class)
            // set Auditing Properties – will be inside a loop one per defined resource
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Bundles")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Response")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("Bundles response resource/bundle received")
            // iDAAS DataHub Processing
            .wireTap("direct:auditing")// Invoke External FHIR Server
        .endChoice();
        ;
    /*
     *  Genomics - Molecular Research

    from("servlet://molecularresearch").noAutoStartup()
            .routeId("FHIRMolecularResearch")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("messagetrigger").constant("MolecularResearch")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("MolecularResearch resource/bundle received")
            // iDAAS DataHub Processing
            .wireTap("direct:auditing")
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
                .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
                .to(getFHIRServerUri("MolecularResearch"))
                //Process Response
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-FHIR")
                .setProperty("industrystd").constant("FHIR")
                .setProperty("messagetrigger").constant("MolecularResearch")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("Response")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("MolecularResearch response resource/bundle received")
                // iDAAS DataHub Processing
                .wireTap("direct:auditing")// Invoke External FHIR Server
            .endChoice();
    ;
*/
    /*
     *  Clinical FHIR
     */
    from("servlet://adverseevent")
            .routeId("FHIRAdverseEvent")
            // set Auditing Properties
            .convertBodyTo(String.class)
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("AdverseEvent")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Adverse Event message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_adverseevent"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
                .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
                .to(getFHIRServerUri("AdverseEvent"))
                // Process Response
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-FHIR")
                .setProperty("industrystd").constant("FHIR")
                .setProperty("messagetrigger").constant("adverseevent")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("Response")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("adverseevent FHIR response message received")
                // iDAAS KIC - Auditing Processing
                .wireTap("direct:auditing")
            .endChoice();
    ;
    from("servlet://allergyintolerance")
            .routeId("FHIRAllergyIntolerance")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("messagetrigger").constant("AllergyIntolerance")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Allergy Intolerance message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_allergyintolerance"))
            .choice().when(simple("{{idaas.processToFHIR}}"))
                //Send to FHIR Server
                .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
                .to(getFHIRServerUri("AllergyIntolerance"))
                //Process Response
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-FHIR")
                .setProperty("industrystd").constant("FHIR")
                .setProperty("messagetrigger").constant("allergyintolerance")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("Response")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("allergyintolerance FHIR response message received")
                // iDAAS KIC - Auditing Processing
                .wireTap("direct:auditing")
            .endChoice();

    from("servlet://appointment")
            .routeId("FHIRAppointment")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Appointment")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Appointment message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_appointment"))
            .choice().when(simple("{{idaas.processToFHIR}}"))
              //Send to FHIR Server
              //.setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Appointment"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("appointment")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("appointment FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")// Invoke External FHIR Server
            .endChoice();

    from("servlet://appointmentresponse")
            .routeId("FHIRAppointmentResponse")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("AppointmentResponse")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Appointment Response message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_appointmentresponse"))
            .choice().when(simple("{{idaas.processToFHIR}}"))
              //Send to FHIR Server
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("AppointmentResponse"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("appointmentresponse")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("appointmentresponse FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")// Invoke External FHIR Server
            .endChoice();

    from("servlet://careplan")
            .routeId("FHIRCarePlan")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("CarePlan")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("CarePlan message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_careplan"))
            .choice().when(simple("{{idaas.processToFHIR}}"))
              //Send to FHIR Server
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("CarePlan"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("careplan")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("careplan FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice();

    from("servlet://careteam")
            .routeId("FHIRCareTeam")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("CareTeam")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("CareTeam message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_careteam"))
            // Send To FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("CareTeam"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("careteam")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("careteam FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice();
    ;
    from("servlet://clincialimpression")
            .routeId("FHIRClinicalImpression")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("ClinicalImpression")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("ClinicalImpression message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_clinicalimpression"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("ClinicalImpression"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("clinicalimpression")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("clinicalimpression FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://codesystem")
            .routeId("FHIRCodeSystem")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("CodeSystem")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("CodeSystem message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_codesystem"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("CodeSystem"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("codesystem")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("codesystem FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://consent")
            .routeId("FHIRConsent")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Consent")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Consent message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_consent"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Consent"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("consent")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("consent FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://communication")
            .routeId("FHIRCommunication")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Communication")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Communication message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_communication"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Communication"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("communication")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("communication FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://condition")
            .routeId("FHIRCondition")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Condition")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Condition message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_condition"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Condition"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("condition")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("condition FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;

    from("servlet://detectedissue")
            .routeId("FHIRDetectedIssue")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("DetectedIssue")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Detected Issue message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_detectedissue"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("DetectedIssue"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("detectedissue")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("detectedissue FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://device")
            .routeId("FHIRDevice")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Device")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Device message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_device"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Device"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("device")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("device FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://devicedefinition")
            .routeId("FHIRDeviceDefinition")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("DeviceDefinition")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Device Definition message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_devicedefinition"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("DeviceDefinition"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("devicedefinition")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("Device Definition FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://devicerequest")
            .routeId("FHIRDeviceRequest")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("DeviceRequest")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Device Request message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_devicerequest"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("DeviceRequest"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("devicerequest")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("devicerequest FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://deviceusestatement")
            .routeId("FHIRDeviceUseStatement")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("DeviceUseStatement")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Device Use Statement message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_deviceusestatement"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("DeviceUseStatement"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("deviceusestatement")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("deviceusestatement FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://diagnosticreport")
            .routeId("FHIRDiagnosticReport")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("DiagnosticReport")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Diagnostic Report message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_diagnosticreport"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("DiagnosticReport"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("DiagnosticReport")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("Diagnostic Report FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://documentreference")
            .routeId("FHIRDocumentReference")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("DocumentReference")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("DocumentReference message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_documentreference"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("DocumentReference"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("documentreference")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("documentreference FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://encounter")
            .routeId("FHIREncounter")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Encounter")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Encounter message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_encounter"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Encounter"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("encounter")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("encounter FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://episodeofcare")
            .routeId("FHIREpisodeOfCare")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("EpisodeOfCare")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("EpisodeOfCare message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_episodeofcare"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("EpisodeOfCare"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("episodeofcare")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("episodeofcare FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://eventdefinition")
            .routeId("FHIREventDefinition")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("EventDefinition")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("EventDefinition message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_eventdefinition"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("EventDefinition"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("eventdefinition")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("EventDefinition FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://familymemberhistory")
            .routeId("FHIRMemberHistory")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Family Member History")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Family Member History message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_familymemberhistory"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("FamilyMemberHistory"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("Family Member History")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("Family Member History FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://goal")
            .routeId("FHIRGoal")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Goal")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Goal message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_goal"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Goal"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("goal")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("goal FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://healthcareservice")
            .routeId("FHIRHealthcareService")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("HealthcareService")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("HealthcareService message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_healthcareservice"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("HealthcareService"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("healthcareservice")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("healthcareservice FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://imagingstudy")
            .routeId("FHIRImagingStudy")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("ImagingStudy")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Imaging Study message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("ImagingStudy"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("imagingstudy")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("imagingstudy FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://immunization")
            .routeId("FHIRImmunization")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Immunization")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Immunization message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_immunization"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("HealthcareService"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("Immunization")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("Immunization FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://location")
            .routeId("FHIRLocation")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Location")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Location message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_location"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Location"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("location")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("location FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://medication")
            .routeId("FHIRMedication")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Medication")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Medication message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_medication"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Medication"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("Medication")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("Medication FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://medicationadministration")
            .routeId("FHIRMedicationAdministration")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("MedicationAdministration")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Medication Admin message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_medicationadmin"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("MedicationAdministration"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("medicationadministration")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("Medication Admin FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://medicationdispense")
            .routeId("FHIRMedicationDispense")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("MedicationDispense")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Medication Dispense message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_medicationdispense"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("MedicationDispense"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("medicationdispense")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("Medication Dispense response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://medicationproduct")
            .routeId("FHIRMedicationProduct")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("MedicationProduct")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Medication Product message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_medicationproduct"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
            .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
            .to(getFHIRServerUri("MedicationProduct"))
            //Process Response
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("medicationproduct")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Response")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("medicationproduct FHIR response message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://medicationproductauthorization")
            .routeId("FHIRMedicationProductAuthorization")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("MedicationProductAuthorization")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Medication Product Auth message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_medicationproductauth"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
            .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
            .to(getFHIRServerUri("MedicationProductAuthorization"))
            //Process Response
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("medicationproductauth")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Response")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("medicationproduct auth FHIR response message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://medicationproductcentralind")
            .routeId("FHIRMedicationProductCentralInd")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("MedicationProductCentralInd")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Medication Product Central Ind message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_medicationproductcentralind"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
            .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
            .to(getFHIRServerUri("MedicationProductCentralIndication"))
            //Process Response
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("medicationproductcentralind")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Response")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("medicationproduct central ind FHIR response message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://medicationrequest")
            .routeId("FHIRMedicationRequest")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("MedicationRequest")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Medication Request message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_medicationrequest"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("MedicationRequest"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("medicationrequest")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("meedicationrequest FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://medicationstatement")
            .routeId("FHIRMedicationStatement")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("MedicationStatement")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Medication Statement message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_medicationstatement"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("MedicationStatement"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("medicationstatement")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("Medication Statement FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://namingsystem")
            .routeId("FHIRNamingSystem")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("NamingSystem")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Naming System message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_namingsystem"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("NamingSystem"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("NamingSystem")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("Naming System FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://observation")
            .routeId("FHIRObservation")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Observation")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Observation message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_observation"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Observation"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("observation")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("observation FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://observationdefinition")
            .routeId("FHIRObservationDefinition")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("ObservationDefinition")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Observation Definition message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_observationdefinition"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("ObservationDefinition"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("ObservationDefinition")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("Observation Definition FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://operationoutcome")
            .routeId("FHIROperationOutcome")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("OperationOutcome")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Operation Outcome message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_operationoutcome"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("OperationOutcome"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("OperationOutcome")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("Operation Outcome FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://organization")
            .routeId("FHIROrganization")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Organization")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Organization message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_organization"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
            .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
            .to(getFHIRServerUri("Organization"))
            // Process Response
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("organization")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Response")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("organization FHIR response message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://organizationaffiliation")
            .routeId("FHIROrganizationAffiliation")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("OrganizationAffiliation")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Organization Affiliation message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_organizationaffiliation"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("OrganizationAffiliation"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("organizationaffiliation")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("organizationaffiliation FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://patient")
            .routeId("FHIRPatient")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Patient")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Patient message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_patient"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Patient"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("patient")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("patient FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://person")
            .routeId("FHIRPerson")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Person")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Person message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_person"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Person"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("person")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("person FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://practitioner")
            .routeId("FHIRPractitioner")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Practitioner")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("Practitioner message received")
            .setProperty("bodyData").simple("${body}")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_practitioner"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Practitioner"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("practitioner")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("practitioner FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://practitionerrole")
            .routeId("FHIRPractitionerRole")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("PractitionerRole")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("Practitioner Role message received")
            .setProperty("bodyData").simple("${body}")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_practitionerrole"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("PractitionerRole"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("practitionerrole")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("practitioner role response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://procedure")
            .routeId("FHIRProcedure")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Procedure")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Procedure message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_procedure"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Procedure"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("Procedure")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("procedure FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://questionaire")
            .routeId("FHIRQuestionaire")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Questionaire")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Questionaire message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_questionnaire"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Questionnaire"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("Questionaire")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("questionaire FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://questionaireresponse")
            .routeId("FHIRQuestionaireResponse")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("QuestionaireResponse")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Questionaire Response message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_questionnaireresponse"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("QuestionnaireResponse"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("questionaireresponse")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("questionaireresponse FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://schedule")
            .routeId("FHIRSchedule")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Schedule")
            .setProperty("component").simple("${routeId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Schedule message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_schedule"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Schedule"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("schedule")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("schedule FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://servicerequest")
            .routeId("FHIRServiceRequest")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("ServiceRequest")
            .setProperty("component").simple("${routeId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Service Request message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_servicerequest"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("ServiceRequest"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("servicerequest")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("servicerequest FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://specimen")
            .routeId("FHIRSpecimen")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Specimen")
            .setProperty("component").simple("${routeId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Specimen message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_specimen"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Specimen"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("specimen")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("specimen FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://substance")
            .routeId("FHIRSubstance")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Substance")
            .setProperty("component").simple("${routeId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Substance message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_substance"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Substance"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("Substance")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("substance FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://supplydelivery")
            .routeId("FHIRSupplyDelivery")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("SupplyDelivery")
            .setProperty("component").simple("${routeId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Supply Delivery message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_supplydelivery"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("SupplyDelivery"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("SupplyDelivery")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("supplydelivery FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://supplyrequest")
            .routeId("FHIRSupplyRequest")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("SupplyRequest")
            .setProperty("component").simple("${routeId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Supply Request message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_supplyrequest"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("SupplyRequest"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("SupplyRequest")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("supplyrequest FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://testreport")
            .routeId("FHIRTestReport")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("TestReport")
            .setProperty("component").simple("${routeId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Test Report message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_testreport"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("TestReport"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("TestReport")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("testreport FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://verificationresult")
            .routeId("FHIRVerificationResult")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("VerificationResult")
            .setProperty("component").simple("${routeId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Verification Result message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_verificationresult"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("VerificationResult"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("VerificationResult")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("verificationresult FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    /*
     *  FHIR: Financial
     */
    from("servlet://account")
            .routeId("FHIRAccount")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Account")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("account message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_account"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Account"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("Account")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Input")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("account FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://chargeitem")
            .routeId("FHIRChargeItem")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("ChargeItem")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("charge item message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_chargeitem"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("ChargeItem"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("ChargeItem")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Input")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("chargeitem FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://chargeitemdefinition")
            .routeId("FHIRChargeItemDefintion")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("ChargeItemDefinition")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("charge item definition message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_chargeitemdefinition"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("ChargeItemDefinition"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("ChargeItemDefintion")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Input")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("chargeitemdefintion FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://claim")
            .routeId("FHIRClaim")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Claim")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("claim message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_claim"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Claim"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("Claim")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Input")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("claim FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://claimresponse")
            .routeId("FHIRClaimResponse")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("ClaimResponse")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("claim response message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_claimresponse"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("ClaimResponse"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("ClaimResponse")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Input")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("claim response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://contract")
            .routeId("FHIRContract")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Contract")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("contract message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_contract"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Contract"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("Contract")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("contract FHIR message response received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://coverage")
            .routeId("FHIRCoverage")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Coverage")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("coverage message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_coverage"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Coverage"))// Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("Coverage")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("coverage FHIR message response received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet:/coverageeligibilityrequest")
            .routeId("FHIRCoverageEligibilityRequest")
            // set Auditing Properties
            .convertBodyTo(String.class)
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("CoveragEligibilityRequest")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("coverageeligibilityrequest message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_coverageeligibilityrequest"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("CoverageEligibilityRequest"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("CoverageEligibilityRequest")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("coverage eligibility request FHIR Response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://coverageeligibilityresponse")
            .routeId("FHIRCoverageEligibilityResponse")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("CoverageEligibilityResponse")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("coverageeligibilityresponse message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_coverageeligibilityresponse"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("CoverageEligibilityResponse"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("CoverageEligibilityResponse")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("coverage eligibility response response received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://enrollmentrequest")
            .routeId("FHIREnrollmentrequest")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("EnrollmentRequest")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("Enrollment Request message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_enrollmentrequest"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("EnrollmentRequest"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("EnrollmentRequest")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("enrollment request message  received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://enrollmentresponse")
            .routeId("FHIREnrollmentresponse")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("EnrollmentResponse")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("Enroll Response message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_enrollmentresponse"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("EnrollmentResponse"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("enrollmentresponse")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("enrollment response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://explanationofbenefits")
            .routeId("FHIRExplanationofbenefits")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("ExplanationOfBenefit")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("explanationofbenefits message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_explanationofbenefit"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("ExplanationOfBenefit"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("ExplanationOfBenefits")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("explanationofbenefits response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://insuranceplan")
            .routeId("FHIRInsuranceplan")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("InsurancePlan")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("insuranceplan message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_insuranceplan"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("InsurancePlan"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("InsurancePlan")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("insuranceplan response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://invoice")
            .routeId("FHIRInvoice")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Invoice")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("invoice message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_invoice"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Invoice"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("Invoice")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("invoice FHIR message response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://paymentnotice")
            .routeId("FHIRPaymentNotice")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("PaymentNotice")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("paymentnotice message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_paymentnotice"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("PaymentNotice"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("PaymentNotice")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("paymentnotice FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet:paymentreconciliation")
            .routeId("FHIRPaymentreconciliation")
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("PaymentReconciliation")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("paymentreconciliation message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_paymentreconciliation"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("PaymentReconciliation"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("PaymentReconciliation")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("paymentreconciliation FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    /*
     *  FHIR: Public Health
     */
    from("servlet://researchstudy")
            .routeId("FHIRResearchStudy")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("ResearchStudy")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Research Study message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_researchstudy"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("ResearchStudy"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("ResearchStudy")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("researchstudy response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://researchsubject")
            .routeId("FHIRResearchSubject")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("ResearchSubject")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Research Subject message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_researchstudy"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("ResearchStudy"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("ResearchSubject")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("researchsubject FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    /*
     *  FHIR: Evidence Based Medicine
     */
    from("servlet://researchdefinition")
            .routeId("FHIRResearchDefinition")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("ResearchDefinition")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Research Definition message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_researchdefinition"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("ResearchDefinition"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("ResearchDefinition")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("researchdefinition FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://researchelementdefinition")
            .routeId("FHIRResearchElementDefinition")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("ResearchElementDefinition")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Research Element Definition message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_researchelementdefintion"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("ResearchElementDefinition"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("ResearchElementDefinition")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("researchelementdefinition FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://evidence")
            .routeId("FHIREvidence")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Evidence")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Evidence message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_evidence"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Evidence"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("Evidence")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("evidence FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://evidencevariable")
            .routeId("FHIREvidenceVariable")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("EvidenceVariable")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Evidence Variable message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_evidencevariable"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("EviodenceVariable"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("EvidenceVariable")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("evidencevariable FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://effectevidencesynthesis")
            .routeId("FHIREffectEvidenceSynthesis")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("EffectEvidenceSynthesis")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Effect Evidence Synthesis message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_effectiveevidencesynthesis"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("EffectiveEvidenceSynthesis"))
              //Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("EffectEvidenceSynthesis")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("effectevidencesynthesis FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://riskevidencesynthesis")
            .routeId("FHIRRiskEvidenceSynthesis")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("RiskEvidenceSynthesis")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Risk Evidence Synthesis message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_riskevidencesynthesis"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("RiskEvidenceSynthesis"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("riskevidencesynthesis")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("riskevidencesynthesis FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    /*
     *  FHIR: Quality Reporting
     */
    from("servlet://measure")
            .routeId("FHIRMeasure")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("Measure")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Measure message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_measure"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("Measure"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("Measure")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("measure FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
    from("servlet://measurereport")
            .routeId("FHIRMeasureReport")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("MeasureReport")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Measure Report message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_measurereport"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("MeasureReport"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("MeasureReport")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("measurereport FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .choice().when(simple("{{idaas.processToFHIR}}"))
    ;
    from("servlet://testscript")
            .routeId("FHIRTestScript")
            .convertBodyTo(String.class)
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-FHIR")
            .setProperty("industrystd").constant("FHIR")
            .setProperty("messagetrigger").constant("TestScript")
            .setProperty("component").simple("${routeId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("processname").constant("Input")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("Test Script message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("fhirsvr_testscript"))
            // Send to FHIR Server
            .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              .to(getFHIRServerUri("TestScript"))
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").constant("TestScript")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Response")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("testscript FHIR response message received")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
            .endChoice()
    ;
  }
}