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
  public static final String EDI270_INBD_ROUTE_ID = "edi-270-inbound";
  public static final String EDI276_INBD_ROUTE_ID = "edi-277-inbound";
  public static final String EDI278_INBD_ROUTE_ID = "edi-278-inbound";
  public static final String EDI834_INBD_ROUTE_ID = "edi-834-inbound";
  public static final String EDI835_INBD_ROUTE_ID = "edi-835-inbound";
  public static final String EDI837_INBD_ROUTE_ID = "edi-837-inbound";

  @Override
  public void configure() throws Exception {

    onException(Exception.class)
            .handled(true)
            .log(LoggingLevel.ERROR, "${exception}")
            .to("micrometer:counter:numExceptionHandled");

    // 270 - Eligibility Inquiry
    // Will Respond with a 271 - Eligibility Response
    from("rest:post:/idaas/edi270")
            .routeId(EDI270_INBD_ROUTE_ID)
            .to("log:" + EDI270_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.edi270.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed checks
            //respond with a 271
    // 276 - Claim Status Inquiry
    // Will Respond with a 277 - HC Claim Status Response
    from("rest:post:/idaas/edi276")
            .routeId(EDI276_INBD_ROUTE_ID)
            .to("log:" + EDI276_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.edi276.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed checks
            //respond with a 277
    // 278 - Auth Inquiry
    // Will Respond with a 275 - Inbound Patient Info
    from("rest:post:/idaas/edi278")
            .routeId(EDI278_INBD_ROUTE_ID)
            .to("log:" + EDI278_INBD_ROUTE_ID + "?showAll=true")
            .to("kafka:SftpFiles?brokers={{idaas.kafka.brokers}}")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.edi278.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed checks
            //respond with a 275
    // 834 - Benefit Enrollment
    from("rest:post:/idaas/edi834")
            .routeId(EDI834_INBD_ROUTE_ID)
            .to("log:" + EDI834_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.edi834.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed checks
    // 835 - Electornic Remittence
    from("rest:post:/idaas/edi835")
            .routeId(EDI835_INBD_ROUTE_ID)
            .to("log:" + EDI835_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.edi835.topic.name}}?brokers={{idaas.kafka.brokers}}");
    // 837 - Billing and Services
    from("rest:post:/idaas/edi837")
            .routeId(EDI837_INBD_ROUTE_ID)
            .to("kafka:SftpFiles?brokers={{idaas.kafka.brokers}}")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.edi837.topic.name}}?brokers={{idaas.kafka.brokers}}");

  }
}
