/*
 * Copyright 2019 Project-Herophilus

 */
package io.connectedhealth_idaas.edi;

//import javax.jms.ConnectionFactory;
//import org.springframework.jms.connection.JmsTransactionManager;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class EdiRouteBuilder extends RouteBuilder {
  public static final String EDI278_INBD_ROUTE_ID = "edi-278-inbound";

  @Override
  public void configure() throws Exception {

    onException(Exception.class)
            .handled(true)
            .log(LoggingLevel.ERROR, "${exception}")
            .to("micrometer:counter:numExceptionHandled");

    // Needs to be a REST Endpoint that can be processed against either a data simulator or Postman
    // Postman is easy button
    from("rest:post:/idaas/edi278")
            .routeId(EDI278_INBD_ROUTE_ID)
            .to("log:" + EDI278_INBD_ROUTE_ID + "?showAll=true")
            .to("kafka:SftpFiles?brokers={{idaas.kafka.brokers}}")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:numProcessedFiles")
            .to("kafka:{{idaas.edi278.topic.name}}?brokers={{idaas.kafka.brokers}}");
            //perform needed checks
            //respond with a 275

  }
}
