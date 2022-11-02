/*
 * Copyright 2019 Project-Herophilus

 */
package io.connectedhealth_idaas.cmsinteroperability;

//import javax.jms.ConnectionFactory;
//import org.springframework.jms.connection.JmsTransactionManager;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class CmsInteroperabilityRouteBuilder extends RouteBuilder {
  public static final String XDS_INBD_ROUTE_ID = "xds-inbound";
  public static final String PIXAddUpdate_INBD_ROUTE_ID = "pixaddupdate-inbound";
  public static final String ProvideReqDocs_INBD_ROUTE_ID = "providerreqdocs-inbound";
  public static final String Davinci_ClinDex_INBD_ROUTE_ID = "davinci-clindex-inbound";
  public static final String PriorAuth_INBD_ROUTE_ID = "priorauth-inbound";
  public static final String DEQM_INBD_ROUTE_ID = "deqm-inbound";
  public static final String PDEX_INBD_ROUTE_ID = "pdex-inbound";
  public static final String EPDX_INBD_ROUTE_ID = "epdx-inbound";
  @Override
  public void configure() throws Exception {

    onException(Exception.class)
            .handled(true)
            .log(LoggingLevel.ERROR, "${exception}")
            .to("micrometer:counter:numExceptionHandled");

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
    from("rest:post:/idaas/priorauth")
            .routeId(PriorAuth_INBD_ROUTE_ID)
            .to("log:" + PriorAuth_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.priorauth.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed Actions to built the correct response
            //respond back with content

    // Data Exchange Quality Metrics
    from("rest:post:/idaas/deqm")
            .routeId(DEQM_INBD_ROUTE_ID)
            .to("log:" + DEQM_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.deqm.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed Actions to built the correct response
            //respond back with content
    // Clinical Data Exchange
    from("rest:post:/idaas/deqm")
            .routeId(DEQM_INBD_ROUTE_ID)
            .to("log:" + DEQM_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.deqm.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed Actions to built the correct response
            //respond back with content
    // Payer Data Exchange
    from("rest:post:/idaas/pdex")
            .routeId(PDEX_INBD_ROUTE_ID)
            .to("log:" + PDEX_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.pdex.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed Actions to built the correct response
            //respond back with content
    // Pt Data Exchange

    // Performing Lab Reporting
    from("rest:post:/idaas/epdx")
            .routeId(EPDX_INBD_ROUTE_ID)
            .to("log:" + EPDX_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.epdx.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed Actions to built the correct response
            //respond back with content


  }
}
