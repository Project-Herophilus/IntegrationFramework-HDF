  /*
   * Copyright 2019 Project-Herophilus
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
  import org.apache.camel.*;
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
// Event Builder
  import io.connectedhealth_idaas.eventbuilder.events.platform.FHIRTerminologyProcessorEvent;
  import io.connectedhealth_idaas.eventbuilder.parsers.fhir.FHIRBundleParser;
//import org.springframework.jms.connection.JmsTransactionManager;
//import javax.jms.ConnectionFactory;
//import sun.util.calendar.BaseCalendar;
// Unused
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
    ServletRegistrationBean camelServlet() {
      // use a @Bean to register the Camel servlet which we need to do
      // because we want to use the camel-servlet component for the Camel REST service
      ServletRegistrationBean mapping = new ServletRegistrationBean();
      mapping.setName("CamelServlet");
      mapping.setLoadOnStartup(1);
      mapping.setServlet(new CamelHttpTransportServlet());
      mapping.addUrlMappings("/idaas/*");
      return mapping;
    }

    private String getKafkaTopicUri(String inputParam) {
      return "kafka:" + inputParam+
              "?brokers=" +
              config.getKafkaBrokers();
    }

    private String getFHIRServerUri(String fhirResource) {
      String fhirServerVendor = config.getFhirVendor();
      //String fhirEndPoint = "simple(${headers.resourcename})";
      String fhirServerURI = null;
      if (fhirServerVendor.equals("ibm"))
      {
        //.to("jetty:http://localhost:8090/fhir-server/api/v4/AdverseEvents?bridgeEndpoint=true&exchangePattern=InOut")
        //fhirServerURI = "http:"+config.getHapiURI()+fhirResource+"?bridgeEndpoint=true";
        fhirServerURI = config.getIbmURI()+fhirResource;
        fhirServerURI = fhirServerURI+"?bridgeEndpoint=true";
      }
      if (fhirServerVendor.equals("hapi"))
      {
        //fhirServerURI = "http:"+config.getHapiURI()+fhirEndPoint+"?bridgeEndpoint=true";
        //fhirServerURI = config.getHapiURI()+fhirEndPoint;
        fhirServerURI = config.getHapiURI()+fhirResource;
        fhirServerURI = fhirServerURI+"?bridgeEndpoint=true";
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
          .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.integrationTopic}}"))
      ;

      from("direct:terminologiesprocessing")
           .routeId("iDaaS-Terminologies")
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
           .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.terminologyTopic}}"))
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
       *  FHIR Processing
       */
      //https://camel.apache.org/components/3.16.x/eips/multicast-eip.html
      from("servlet://fhirendpoint")
           .routeId("FHIRProcessing")
           .multicast().parallelProcessing()
             .to("direct:generalprocessing")
             .to("direct:fhirmessaging")
             .to("direct:terminologies")
      .end();

      // General Data Integration
      from("direct:generalprocessing")
           .routeId("FHIRResourceProcessing")
           // Setup Params
           .log("Exchange Header: ${headers}")
           .log("Exchange Header - ResourceName: ${headers.resourcename}")
           //.convertBodyTo(String.class)
           .setProperty("processingtype").constant("data")
           .setProperty("appname").constant("iDAAS-Connect-FHIR")
           .setProperty("industrystd").constant("FHIR")
           .setProperty("messagetrigger").simple("${headers.resourcename}")
           .setProperty("component").simple("${routeId}")
           .setProperty("camelID").simple("${camelId}")
           .setProperty("exchangeID").simple("${exchangeId}")
           .setProperty("internalMsgID").simple("${id}")
           .setProperty("bodyData").simple("${body}")
           .setProperty("processname").constant("Input")
           .setProperty("auditdetails").simple("${headers.resourcename} Resource message received")
           // iDAAS KIC - Auditing Processing
           .wireTap("direct:auditing")
           // Send To Topic
           .toD(getKafkaTopicUri("fhir_${headers.resourcename}"))
      ;

      // Send to FHIR Server
      from("direct:fhirmessaging")
           .routeId("FHIRMessaging")
           .choice().when(simple("{{idaas.processToFHIR}}"))
              .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
              //.toD(getFHIRServerUri("AllergyIntolerance"))
              // http://simple%7BAdverseEvent%7D?bridgeEndpoint=true
              //.toD(getFHIRServerUri(String.valueOf(simple("${headers.resourcename}"))))
              .setBody(simple("${body}"))
              .toD(getFHIRServerUri("${headers.resourcename}"))
              // Process Response
              .convertBodyTo(String.class)
               // set Auditing Properties
               .setProperty("processingtype").constant("data")
               .setProperty("appname").constant("iDAAS-Connect-FHIR")
               .setProperty("industrystd").constant("FHIR")
               .setProperty("messagetrigger").simple("${headers.resourcename}")
               .setProperty("component").simple("${routeId}")
               .setProperty("processname").constant("Response")
               .setProperty("camelID").simple("${camelId}")
               .setProperty("exchangeID").simple("${exchangeId}")
               .setProperty("internalMsgID").simple("${id}")
               .setProperty("bodyData").simple("${body}")
               .setProperty("auditdetails").constant("${headers.resourcename} FHIR Server response message received")
               // iDAAS KIC - Auditing Processing
               .wireTap("direct:auditing")
               // Populate Params
               //.to("file://data/")
           .endChoice();

      // Send to Terminology Processes
      from("direct:terminologies")
           .routeId("TerminologyProcessor")
           .choice().when(simple("{{idaas.processTerminologies}}"))
              .setBody(simple("${body}"))
              .wireTap("direct:terminologiesprocessing")
              // Process Response
              .convertBodyTo(String.class)
              // set Auditing Properties
              .setProperty("processingtype").constant("data")
              .setProperty("appname").constant("iDAAS-Connect-FHIR")
              .setProperty("industrystd").constant("FHIR")
              .setProperty("messagetrigger").simple("${headers.resourcename}")
              .setProperty("component").simple("${routeId}")
              .setProperty("processname").constant("Request")
              .setProperty("camelID").simple("${camelId}")
              .setProperty("exchangeID").simple("${exchangeId}")
              .setProperty("internalMsgID").simple("${id}")
              .setProperty("bodyData").simple("${body}")
              .setProperty("auditdetails").constant("${headers.resourcename} FHIR Resource sent to terminology topic")
              // iDAAS KIC - Auditing Processing
              .wireTap("direct:auditing")
              // Populate Params
              //.to("file://data/")
           .endChoice();

      from("servlet://AdverseEvent")
           .routeId("FHIRProcessing-Test")
           .convertBodyTo(String.class)
           .setProperty("processingtype").constant("data")
           .setProperty("appname").constant("iDAAS-Connect-FHIR")
           .setProperty("industrystd").constant("FHIR")
           .setProperty("messagetrigger").simple("AdverseEvent")
           .setProperty("component").simple("${routeId}")
           .setProperty("camelID").simple("${camelId}")
           .setProperty("exchangeID").simple("${exchangeId}")
           .setProperty("internalMsgID").simple("${id}")
           .setProperty("bodyData").simple("${body}")
           .setProperty("processname").constant("Input")
           .setProperty("auditdetails").simple("Resource message received")
           // iDAAS KIC - Auditing Processing
           .wireTap("direct:auditing")
           // Send To Topic
           .toD(getKafkaTopicUri("fhir_testoutput"))
      ;

    }
  }