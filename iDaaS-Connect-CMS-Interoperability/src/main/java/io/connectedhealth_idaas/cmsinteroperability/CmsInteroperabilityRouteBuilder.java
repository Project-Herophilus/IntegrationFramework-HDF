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
public class EdiRouteBuilder extends RouteBuilder {
  public static final String XDS_INBD_ROUTE_ID = "xds-inbound";
  public static final String PIXAddUpdate_INBD_ROUTE_ID = "pixaddupdate-inbound";
  public static final String ProvideReqDocs_INBD_ROUTE_ID = "providerreqdocs-inbound";
  public static final String Davinci_ClinDex_INBD_ROUTE_ID = "davinci-clindex-inbound";
  public static final String EDI835_INBD_ROUTE_ID = "edi-835-inbound";
  public static final String EDI837_INBD_ROUTE_ID = "edi-837-inbound";

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
            .to("kafka:{{idaas.xds.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed Actions to persist PIX Documents to a XDS Repository/DataStore
    // Provide Required Docs
    from("rest:post:/idaas/providereqdocs")
            .routeId(ProvideReqDocs_INBD_ROUTE_ID)
            .to("log:" + ProvideReqDocs_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.xds.topic.name}}?brokers={{idaas.kafka.brokers}}");
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

    // Prior Auth

    // Data Exchange Quality Metrics

    // Clinical Data Exchange

    // Payer Data Exchange


    // Pt Data Exchange

    // Perfcorming Lab Reporting


  }
}
