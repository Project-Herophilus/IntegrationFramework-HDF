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
package io.connectedhealth_idaas.edi;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaComponent;
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
//import sun.util.calendar.BaseCalendar;


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

  /*
   * Kafka implementation based upon https://camel.apache.org/components/latest/kafka-component.html
   *
   */
  @Override
  public void configure() throws Exception {
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

    // EDI from files
    from("file:{{270.inputdirectory}}/")
            .routeId("270-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("edi_270"))
            .to("file:{{output.directory}}/");

    from("file:{{271.inputdirectory}}/")
            .routeId("271-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("edi_271"))
            .to("file:{{output.directory}}/");

    from("file:{{276.inputdirectory}}/")
            .routeId("276-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("edi_276"))
            .to("file:{{output.directory}}/");

    from("file:{{277.inputdirectory}}/")
            .routeId("277-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("edi_277"))
            .to("file:{{output.directory}}/");

    from("file:{{834.inputdirectory}}/")
            .routeId("834-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("edi_834"))
            .to("file:{{output.directory}}/");

    from("file:{{835.inputdirectory}}/")
            .routeId("835-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("edi_835"))
            .to("file:{{output.directory}}/");

    from("file:{{837.inputdirectory}}/")
            .routeId("837-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("edi_837"))
            .to("file:{{output.directory}}/");

  }
}