/*
 * Copyright 2019 Project-Herophilus

 */
package io.integration_framework.hdf.edi;

//import javax.jms.ConnectionFactory;
//import org.springframework.jms.connection.JmsTransactionManager;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class EdiRouteBuilder extends RouteBuilder {
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
  public static final String TERMINOLOGY_ROUTE_ID = "terminologies-direct";
  public static final String DATATIER_ROUTE_ID = "datatier-direct";
  public static final String DEIDENTIFICATION_ROUTE_ID = "deidentification-direct";
  public static final String EMPI_ROUTE_ID = "empi-direct";
  public static final String HEDA_ROUTE_ID = "heda-direct";
  public static final String PUBLICCLOUD_ROUTE_ID = "publiccloud-direct";
  public static final String SDOH_ROUTE_ID = "sdoh-direct";
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
            .to("micrometer:counter:edi_Exception");

    /*from("kafka:Topic278?brokers={{idaas.kafka.brokers}}&groupId=hl7-278&autoOffsetReset=earliest")
                .log("Content received: ${body}")
                .setHeader("file-name", constant("{{s3.file275.key}}"))
                .bean(s3Bean,"extract")
                .log("275 Content: ${body}")
                .to("kafka:Topic275?brokers={{idaas.kafka.brokers}}")
                .to("micrometer:counter:files_converted");*/

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
            .to("micrometer:counter:Terminology_Inbd_Transactions")
            .to("kafka:{{idaas.terminology.topic.name}}?brokers={{idaas.kafka.brokers}}")
            .endChoice();

    from("direct:datatier")
            .choice()
            .when(simple("{{idaas.process.DataTier}}"))
            .routeId(DATATIER_ROUTE_ID)
            .to("log:" + DATATIER_ROUTE_ID + "?showAll=true")
            //.log("${exchangeId} fully processed")
            .to("micrometer:counter:DataTier_Inbd_Transactions")
            .to("kafka:{{idaas.datatier.topic.name}}?brokers={{idaas.kafka.brokers}}")
            // to the deidentification API
            .endChoice();

    from("direct:deidentification")
            .choice()
            .when(simple("{{idaas.process.Deidentification}}"))
            .routeId(DEIDENTIFICATION_ROUTE_ID)
            .to("log:" + DEIDENTIFICATION_ROUTE_ID + "?showAll=true")
            //.log("${exchangeId} fully processed")
            .to("micrometer:counter:Deidentification_Inbd_Transactions")
            .to("kafka:{{idaas.deidentification.topic.name}}?brokers={{idaas.kafka.brokers}}")
            // to the deidentification API
            .endChoice();

    from("direct:empi")
            .choice()
            .when(simple("{{idaas.process.Empi}}"))
            .routeId(EMPI_ROUTE_ID)
            .to("log:" + EMPI_ROUTE_ID + "?showAll=true")
            //.log("${exchangeId} fully processed")
            .to("micrometer:counter:EMPI_Inbd_Transactions")
            .to("kafka:{{idaas.deidentification.topic.name}}?brokers={{idaas.kafka.brokers}}")
            // to the empi API
            .endChoice();

    from("direct:heda")
            .choice()
            .when(simple("{{idaas.process.HEDA}}"))
            .routeId(HEDA_ROUTE_ID)
            .to("log:" + HEDA_ROUTE_ID + "?showAll=true")
            //.log("${exchangeId} fully processed")
            .to("micrometer:counter:HEDA_Inbd_Transactions")
            .to("kafka:{{idaas.heda.topic.name}}?brokers={{idaas.kafka.brokers}}")
            .endChoice();

    from("direct:publiccloud")
            .choice()
            .when(simple("{{idaas.process.PublicCloud}}"))
            .routeId(PUBLICCLOUD_ROUTE_ID)
            .to("log:" + PUBLICCLOUD_ROUTE_ID + "?showAll=true")
            //.log("${exchangeId} fully processed")
            .to("micrometer:counter:PublicCloud_Inbd_Transactions")
            .to("kafka:{{idaas.publiccloud.topic.name}}?brokers={{idaas.kafka.brokers}}")
            .endChoice();

    from("direct:sdoh")
            .choice()
            .when(simple("{{idaas.process.Sdoh}}"))
            .routeId(SDOH_ROUTE_ID)
            .to("log:" + SDOH_ROUTE_ID + "?showAll=true")
            //.log("${exchangeId} fully processed")
            .to("micrometer:counter:SDOH_Inbd_Transactions")
            .to("kafka:{{idaas.sdoh.topic.name}}?brokers={{idaas.kafka.brokers}}")
            .endChoice();

    restConfiguration()
            .component("servlet");
    // 270 - Eligibility Inquiry
    // Will Respond with a 271 - Eligibility Response
    rest("/edi270")
            .post()
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .to("log:" + EDI270_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:REST_edi270_Inbd_ProcessedEvents")
            .to("kafka:{{idaas.edi270.topic.name}}?brokers={{idaas.kafka.brokers}}")
            //perform needed checks
            //respond with a 271
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
    // 276 - Claim Status Inquiry
    // Will Respond with a 277 - HC Claim Status Response
    rest("/edi276")
            .post()
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .to("log:" + EDI276_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:REST_edi276_Inbd_ProcessedEvents")
            .to("kafka:{{idaas.edi276.topic.name}}?brokers={{idaas.kafka.brokers}}")
            //perform needed checks
            //respond with a 277
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
    // 278 - Auth Inquiry
    // Will Respond with a 275 - Inbound Patient Info
    rest("/edi278")
            .post()
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .to("log:" + EDI278_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:REST_edi278_Inbd_ProcessedEvents")
            .to("kafka:{{idaas.edi278.topic.name}}?brokers={{idaas.kafka.brokers}}")
            //perform needed checks
            //respond with a 277
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
    // 834 - Benefit Enrollment
    rest("/edi834")
            .post()
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .to("log:" + EDI834_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:REST_edi834_Inbd_ProcessedEvents")
            .to("kafka:{{idaas.edi834.topic.name}}?brokers={{idaas.kafka.brokers}}")
            //perform needed checks
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
    // 835 - Electornic Remittence
    rest("/edi835")
            .post()
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .to("log:" + EDI835_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:REST_edi835_Inbd_ProcessedEvents")
            .to("kafka:{{idaas.edi835.topic.name}}?brokers={{idaas.kafka.brokers}}")
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
    // 837 - Billing and Services
    rest("/edi837")
            .post()
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .to("log:" + EDI837_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:REST_edi837_Inbd_ProcessedEvents")
            .to("kafka:{{idaas.edi837.topic.name}}?brokers={{idaas.kafka.brokers}}")
            //perform needed checks
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

  }
}
