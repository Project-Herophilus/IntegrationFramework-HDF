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
package io.connectedhealth_idaas.sap;

//import javax.jms.ConnectionFactory;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
//import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/*
 *  Kafka implementation based on https://camel.apache.org/components/latest/kafka-component.html
 *  JDBC implementation based on https://camel.apache.org/components/latest/dataformats/hl7-dataformat.html
 */
@Component
public class SapRouteBuilder extends RouteBuilder {
  private static final Logger log = LoggerFactory.getLogger(SapRouteBuilder.class);

  //@Autowired


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

  // Public Variables
  public static final String TERMINOLOGY_ROUTE_ID = "terminologies-direct";
  public static final String DEIDENTIFICATION_ROUTE_ID = "deidentification-direct";
  public static final String EMPI_ROUTE_ID = "empi-direct";
  public static final String DATATIER_ROUTE_ID = "datatier-direct";
  public static final String HEDA_ROUTE_ID = "heda-direct";
  public static final String PUBLICCLOUD_ROUTE_ID = "publiccloud-direct";
  public static final String SDOH_ROUTE_ID = "sdoh-direct";
  public static final String IDOCS_ROUTE_ID = "idocs";

  @Override
  public void configure() throws Exception {

    /*
     *  Direct actions used across platform
     *
     */

    onException(Exception.class)
            .handled(true)
            .log(LoggingLevel.ERROR,"${exception}")
            .to("micrometer:counter:rest_exception_handled")
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

    // Routes
    restConfiguration()
            .component("servlet");

    rest("/idocs")
            .post()
            .produces(MediaType.TEXT_PLAIN_VALUE)
            .route()
            .routeId(IDOCS_ROUTE_ID)
            .to("log:"+ IDOCS_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:idocsEventReceived")
            .to("kafka:{{idaas.iot.integration.topic}}?brokers={{idaas.kafka.brokers}}")
            .endRest();




  }
}
