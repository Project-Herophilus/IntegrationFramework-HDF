/*
 * Copyright 2019 Project-Herophilus

 */
package io.connectedhealth_idaas.cmsinteroperability;

//import javax.jms.ConnectionFactory;
//import org.springframework.jms.connection.JmsTransactionManager;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class CmsInteroperabilityRouteBuilder extends RouteBuilder {
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
  public static final String XDS_INBD_ROUTE_ID = "xds-inbound";
  public static final String PIXAddUpdate_INBD_ROUTE_ID = "pixaddupdate-inbound";
  public static final String ProvideReqDocs_INBD_ROUTE_ID = "providerreqdocs-inbound";
  public static final String CEDX_INBD_ROUTE_ID = "davinci-clindex-inbound";
  public static final String PRIORAUTH_INBD_ROUTE_ID = "priorauth-inbound";
  public static final String DEQM_INBD_ROUTE_ID = "deqm-inbound";
  public static final String PDEX_INBD_ROUTE_ID = "pdex-inbound";
  public static final String EPDX_INBD_ROUTE_ID = "epdx-inbound";
  public static final String TERMINOLOGY_ROUTE_ID = "terminologies-direct";
  public static final String DEIDENTIFICATION_ROUTE_ID = "deidentification-direct";
  public static final String EMPI_ROUTE_ID = "empi-direct";
  public static final String DATATIER_ROUTE_ID = "datatier-direct";
  public static final String HEDA_ROUTE_ID = "heda-direct";
  public static final String PUBLICCLOUD_ROUTE_ID = "publiccloud-direct";
  public static final String SDOH_ROUTE_ID = "sdoh-direct";

  @Override
  public void configure() throws Exception {

    onException(Exception.class)
            .handled(true)
            .log(LoggingLevel.ERROR, "${exception}")
            .to("micrometer:counter:cmsInterop_exception_handled")
            .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN_VALUE))
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
            .setBody(simple("${exception}"));

    /*
     *   Direct Internal Processing
     */
    from("direct:terminologies")
            .choice()
            .when(simple("{{idaas.process.Terminologies}}"))
            //.routeId("iDaaS-Terminologies")
            //.convertBodyTo(String.class).to("kafka:{{idaas.terminologyTopic}}?brokers={{idaas.kafkaBrokers}}");
            .routeId(TERMINOLOGY_ROUTE_ID)
            .to("log:" + TERMINOLOGY_ROUTE_ID + "?showAll=true")
            //.log("${exchangeId} fully processed")
            .to("micrometer:counter:terminologyTransactions")
            .to("kafka:{{idaas.terminology.topic.name}}?brokers={{idaas.kafka.brokers}}")
            .endChoice();

    from("direct:datatier")
            .choice()
            .when(simple("{{idaas.process.DataTier}}"))
            .routeId(DATATIER_ROUTE_ID)
            .to("log:" + DATATIER_ROUTE_ID + "?showAll=true")
            //.log("${exchangeId} fully processed")
            .to("micrometer:counter:datatierTransactions")
            .to("kafka:{{idaas.datatier.topic.name}}?brokers={{idaas.kafka.brokers}}")
            // to the deidentification API
            .endChoice();

    from("direct:deidentification")
            .choice()
            .when(simple("{{idaas.process.Deidentification}}"))
            .routeId(DEIDENTIFICATION_ROUTE_ID)
            .to("log:" + DEIDENTIFICATION_ROUTE_ID + "?showAll=true")
            //.log("${exchangeId} fully processed")
            .to("micrometer:counter:deidentificationTransactions")
            .to("kafka:{{idaas.deidentification.topic.name}}?brokers={{idaas.kafka.brokers}}")
            // to the deidentification API
            .endChoice();

    from("direct:empi")
            .choice()
            .when(simple("{{idaas.process.Empi}}"))
            .routeId(EMPI_ROUTE_ID)
            .to("log:" + EMPI_ROUTE_ID + "?showAll=true")
            //.log("${exchangeId} fully processed")
            .to("micrometer:counter:deidentificationTransactions")
            .to("kafka:{{idaas.deidentification.topic.name}}?brokers={{idaas.kafka.brokers}}")
            // to the empi API
            .endChoice();

    from("direct:heda")
            .choice()
            .when(simple("{{idaas.process.HEDA}}"))
            .routeId(HEDA_ROUTE_ID)
            .to("log:" + HEDA_ROUTE_ID + "?showAll=true")
            //.log("${exchangeId} fully processed")
            .to("micrometer:counter:hedaTransactions")
            .to("kafka:{{idaas.heda.topic.name}}?brokers={{idaas.kafka.brokers}}")
            .endChoice();

    from("direct:publiccloud")
            .choice()
            .when(simple("{{idaas.process.PublicCloud}}"))
            .routeId(PUBLICCLOUD_ROUTE_ID)
            .to("log:" + PUBLICCLOUD_ROUTE_ID + "?showAll=true")
            //.log("${exchangeId} fully processed")
            .to("micrometer:counter:publiccloudTransactions")
            .to("kafka:{{idaas.publiccloud.topic.name}}?brokers={{idaas.kafka.brokers}}")
            .endChoice();

    from("direct:sdoh")
            .choice()
            .when(simple("{{idaas.process.Sdoh}}"))
            .routeId(SDOH_ROUTE_ID)
            .to("log:" + SDOH_ROUTE_ID + "?showAll=true")
            //.log("${exchangeId} fully processed")
            .to("micrometer:counter:sdohTransactions")
            .to("kafka:{{idaas.sdoh.topic.name}}?brokers={{idaas.kafka.brokers}}")
            .endChoice();

    restConfiguration()
            .component("servlet");
    // XDS.b
    // General
    // https://wiki.ihe.net/index.php/XDS.b_Implementation
    // https://wiki.ohie.org/display/SUB/XDS.b+Interface+Module+Design
    // Implementations
    // https://oehf.github.io/ipf/ipf-tutorials-xds/index.html

    // HIE
    // XDS.b Request-Response
    from("rest:post:/idaas/xdsrequest")
            .routeId(XDS_INBD_ROUTE_ID)
            .to("log:" + XDS_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.xds.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed XDS Lookup
            //respond with a XDS Response
    // PIX Add-Update
    from("rest:post:/idaas/pixaddupdate")
            .routeId(PIXAddUpdate_INBD_ROUTE_ID)
            .to("log:" + PIXAddUpdate_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.pixaddupdate.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed Actions to persist PIX Documents to a XDS Repository/DataStore
    // Provide Required Docs
    from("rest:post:/idaas/providereqdocs")
            .routeId(ProvideReqDocs_INBD_ROUTE_ID)
            .to("log:" + ProvideReqDocs_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.providerreqdocs.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed Actions to persist Provide Docs to a XDS Repository/DataStore

    // Davinci
    // General
    // https://confluence.hl7.org/display/DVP/Da+Vinci+Implementation+Guide+Dashboard
    // https://confluence.hl7.org/pages/viewpage.action?pageId=120753118
    // PAS (Prior Authorizations):              https://build.fhir.org/ig/HL7/davinci-pas/
    //                                          https://build.fhir.org/ig/HL7/davinci-pas/usecases.html
    // DEQM (Data Exchange Quality Measures) :  https://build.fhir.org/ig/HL7/davinci-deqm/
    // CDEX (Clinical Data Exchange)         :  http://hl7.org/fhir/us/davinci-cdex/
    // PDEX (Payer Data Exchange)            :  https://hl7.org/fhir/us/davinci-pdex/
    // EPDX (Performing Lab Reporting)       :  http://hl7.org/fhir/us/core/StructureDefinition-us-core-diagnosticreport-lab.html


    // Prior Auth
    rest("/priorauth")
            .post()
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .routeId(PRIORAUTH_INBD_ROUTE_ID)
            .to("log:" + PRIORAUTH_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.priorauth.topic.name}}?brokers={{idaas.kafka.brokers}}")
            //perform needed Actions to built the correct response
            //respond back with content
            .multicast().parallelProcessing()
              // Process Terminologies
              .to("direct:terminologies")
              // Data Tier
              .to("direct:datatier")
              // Deidentification
              .to("direct:deidentification")
              // EMPI
              .to("direct:empi")
              // HEDA
              .to("direct:heda")
              // Public Cloud
              .to("direct:publiccloud")
              //SDOH
              .to("direct:sdoh")
    .endRest();
    // Data Exchange Quality Metrics
    rest("/deqm")
            .post()
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .routeId(DEQM_INBD_ROUTE_ID)
            .to("log:" + DEQM_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.deqm.topic.name}}?brokers={{idaas.kafka.brokers}}")
            //perform needed Actions to built the correct response
            //respond back with content
            .multicast().parallelProcessing()
              // Process Terminologies
              .to("direct:terminologies")
              // Data Tier
              .to("direct:datatier")
              // Deidentification
              .to("direct:deidentification")
              // EMPI
              .to("direct:empi")
              // HEDA
              .to("direct:heda")
              // Public Cloud
              .to("direct:publiccloud")
              //SDOH
              .to("direct:sdoh")
    .endRest();
    // Clinical Data Exchange
    rest("/cdex")
            .post()
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .routeId(CEDX_INBD_ROUTE_ID)
            .to("log:" + CEDX_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.cedx.topic.name}}?brokers={{idaas.kafka.brokers}}")
            //perform needed Actions to built the correct response
            //respond back with content
            .multicast().parallelProcessing()
              // Process Terminologies
              .to("direct:terminologies")
              // Data Tier
              .to("direct:datatier")
              // Deidentification
              .to("direct:deidentification")
              // EMPI
              .to("direct:empi")
              // HEDA
              .to("direct:heda")
              // Public Cloud
              .to("direct:publiccloud")
              //SDOH
              .to("direct:sdoh")
    .endRest();
    // Payer Data Exchange
    rest("/pdex")
            .post()
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .routeId(PDEX_INBD_ROUTE_ID)
            .to("log:" + PDEX_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.pdex.topic.name}}?brokers={{idaas.kafka.brokers}}")
            //perform needed Actions to built the correct response
            //respond back with content
            .multicast().parallelProcessing()
              // Process Terminologies
              .to("direct:terminologies")
              // Data Tier
              .to("direct:datatier")
              // Deidentification
              .to("direct:deidentification")
              // EMPI
              .to("direct:empi")
              // HEDA
              .to("direct:heda")
              // Public Cloud
              .to("direct:publiccloud")
              //SDOH
              .to("direct:sdoh")
    .endRest();
    // Pt Data Exchange
    // Complete later
    // Performing Lab Reporting
    rest("/pdex")
            .post()
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .routeId(EPDX_INBD_ROUTE_ID)
            .to("log:" + EPDX_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.epdx.topic.name}}?brokers={{idaas.kafka.brokers}}")
            //perform needed Actions to built the correct response
            //respond back with content
            .multicast().parallelProcessing()
              // Process Terminologies
              .to("direct:terminologies")
              // Data Tier
              .to("direct:datatier")
              // Deidentification
              .to("direct:deidentification")
              // EMPI
              .to("direct:empi")
              // HEDA
              .to("direct:heda")
              // Public Cloud
              .to("direct:publiccloud")
              //SDOH
              .to("direct:sdoh")
   .endRest();
    // Prior Auth
   /* rest("/priorAuthorization")
            .post()
            .consumes(MediaType.TEXT_PLAIN_VALUE)
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .routeId("priorAuthorization")
            .convertBodyTo(String.class)
            .log("Content received: ${body}")
            .to("kafka:Topic278?brokers={{idaas.kafka.brokers}}")
            .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN_VALUE))
            .setBody(constant("file published"))
            .to("micrometer:counter:files_278_received");*/
  }
}
