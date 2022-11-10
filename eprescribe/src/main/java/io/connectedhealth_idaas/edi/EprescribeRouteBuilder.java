/*
 * Copyright 2019 Project-Herophilus

 */
package io.connectedhealth_idaas.eprescribe;

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
public class EprescribeRouteBuilder extends RouteBuilder {
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
  public static final String RXFILL_INBD_ROUTE_ID = "rxfill-inbound";


  @Override
  public void configure() throws Exception {

    onException(Exception.class)
            .handled(true)
            .log(LoggingLevel.ERROR, "${exception}")
            .to("micrometer:counter:eprescribeExceptionHandled");

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

    // http://www.stylusstudio.com/edi/ncpdp-standard.html
    rest("/rxfill")
            .post()
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .to("log:" + EDI270_INBD_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:rxfillProcessedEvents")
            .to("kafka:{{idaas.rxfill.topic.name}}?brokers={{idaas.kafka.brokers}}")
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

  }
}
