/*
 * Copyright 2019

 */
package io.connectedhealth_idaas.edi;

import java.util.ArrayList;
import java.util.List;

import javax.jms.ConnectionFactory;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

/*
 *
 * General Links
 * https://camel.apache.org/components/latest/eips/split-eip.html
 * Basic Links for Implementations
 * Kafka implementation based on
 * https://camel.apache.org/components/latest/kafka-component.html JDBC
 * implementation based on
 * https://camel.apache.org/components/latest/dataformats/hl7-dataformat.html
 * JPA implementayion based on
 * https://camel.apache.org/components/latest/jpa-component.html File
 * implementation based on
 * https://camel.apache.org/components/latest/file-component.html FileWatch
 * implementation based on
 * https://camel.apache.org/components/latest/file-watch-component.html FTP/SFTP
 * and FTPS implementations based on
 * https://camel.apache.org/components/latest/ftp-component.html JMS
 * implementation based on
 * https://camel.apache.org/components/latest/jms-component.html JT400 (AS/400)
 * implementation based on
 * https://camel.apache.org/components/latest/jt400-component.html HTTP
 * implementation based on
 * https://camel.apache.org/components/latest/http-component.html HDFS
 * implementation based on
 * https://camel.apache.org/components/latest/hdfs-component.html jBPMN
 * implementation based on
 * https://camel.apache.org/components/latest/jbpm-component.html MongoDB
 * implementation based on
 * https://camel.apache.org/components/latest/mongodb-component.html RabbitMQ
 * implementation based on
 * https://camel.apache.org/components/latest/rabbitmq-component.html There are
 * lots of third party implementations to support cloud storage from Amazon AC2,
 * Box and so forth There are lots of third party implementations to support
 * cloud for Amazon Cloud Services Awaiting update to 3.1 for functionality
 * Apache Kudu implementation REST API implementations
 */

@Component
public class CamelConfiguration extends RouteBuilder {
  private static final Logger log = LoggerFactory.getLogger(CamelConfiguration.class);

  @Autowired
  private ConfigProperties config;

  @Bean
  private KafkaEndpoint kafkaEndpoint() {
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

  private String getKafkaTopicUri(String topic) {
    return "kafka:" + topic + "?brokers=" + config.getKafkaBrokers();
  }

  @Override
  public void configure() throws Exception {

    /*
     *   HIDN
     *   HIDN - Health information Data Network
     *   Intended to enable simple movement of data aside from specific standards
     *   Common Use Cases are areas to support remote (iOT/Edge) and any other need for small footprints to larger
     *   footprints
     *
     */
    from("direct:hidn")
            .routeId("HIDN Processing")
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
     * Direct actions used across platform
     *
     */
    from("direct:auditing")
            .routeId("KIC-KnowledgeInsightConformance")
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
            .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.integrationTopic}}"));
    /*
     * Direct Logging
     */
    from("direct:logging")
            .routeId("Logging")
            .log(LoggingLevel.INFO, log, "Transaction Message: [${body}]");

    /*
     *   Terminologies component for processing terminology events
     */
    from("direct:terminologies")
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
            .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.terminologyTopic}}"));

    /*
     *  Servlet common endpoint accessable to process transactions
     */
    from("servlet://hidn")
            .routeId("HIDN Servlet")
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

    /*
     *   HTTP Endpoint
     */
    from("servlet://edi_5010_270")
            .routeId("edi_5010_270")
            // set Auditing Properties
            .convertBodyTo(String.class)
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-270")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("EDI 5010-270 message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.topicName270}}"))
            //Process Terminologies
            .choice()
            .when(simple("{{idaas.processTerminologies}}"))
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-270")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("terminologies")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("Event sent to Defined Topic for terminology processing")
            // iDAAS KIC - Auditing Processing
            .to("direct:auditing")
            // Write Parsed FHIR Terminology Transactions to Topic
            .to("direct:terminologies")
            .endChoice();
    ;

    from("servlet://edi_5010_271")
            .routeId("edi_5010_271")
            // set Auditing Properties
            .convertBodyTo(String.class)
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-271")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("EDI 5010-271 message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.topicName271}}"))
            //Process Terminologies
            .choice()
            .when(simple("{{idaas.processTerminologies}}"))
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-271")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("terminologies")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("Event sent to Defined Topic for terminology processing")
            // iDAAS KIC - Auditing Processing
            .to("direct:auditing")
            // Write Parsed FHIR Terminology Transactions to Topic
            .to("direct:terminologies")
            .endChoice();
    ;

    from("servlet://edi_5010_276")
            .routeId("edi_5010_276")
            // set Auditing Properties
            .convertBodyTo(String.class)
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-276")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("EDI 5010-276 message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.topicName276}}"))
            //Process Terminologies
            .choice()
            .when(simple("{{idaas.processTerminologies}}"))
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-276")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("terminologies")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("Event sent to Defined Topic for terminology processing")
            // iDAAS KIC - Auditing Processing
            .to("direct:auditing")
            // Write Parsed FHIR Terminology Transactions to Topic
            .to("direct:terminologies")
            .endChoice();
    ;

    from("servlet://edi_5010_277")
            .routeId("edi_5010_277")
            // set Auditing Properties
            .convertBodyTo(String.class)
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-277")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("EDI 5010-277 message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.topicName277}}"))
            //Process Terminologies
            .choice()
            .when(simple("{{idaas.processTerminologies}}"))
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-277")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("terminologies")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("Event sent to Defined Topic for terminology processing")
            // iDAAS KIC - Auditing Processing
            .to("direct:auditing")
            // Write Parsed FHIR Terminology Transactions to Topic
            .to("direct:terminologies")
            .endChoice();
    ;

    from("servlet://edi_5010_834")
            .routeId("edi_5010_834")
            // set Auditing Properties
            .convertBodyTo(String.class)
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-834")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("EDI 5010-834 message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.topicName834}}"))
            //Process Terminologies
            .choice()
            .when(simple("{{idaas.processTerminologies}}"))
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-834")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("terminologies")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("Event sent to Defined Topic for terminology processing")
            // iDAAS KIC - Auditing Processing
            .to("direct:auditing")
            // Write Parsed FHIR Terminology Transactions to Topic
            .to("direct:terminologies")
            .endChoice();
    ;

    from("servlet://edi_5010_835")
            .routeId("edi_5010_835")
            // set Auditing Properties
            .convertBodyTo(String.class)
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-835")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("EDI 5010-835 message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.topicName835}}"))
            //Process Terminologies
            .choice()
            .when(simple("{{idaas.processTerminologies}}"))
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-835")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("terminologies")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("Event sent to Defined Topic for terminology processing")
            // iDAAS KIC - Auditing Processing
            .to("direct:auditing")
            // Write Parsed FHIR Terminology Transactions to Topic
            .to("direct:terminologies")
            .endChoice();
    ;

    from("servlet://edi_5010_837")
            .routeId("edi_5010_837")
            // set Auditing Properties
            .convertBodyTo(String.class)
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-837")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").constant("EDI 5010-837 message received")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.topicName837}}"))
            //Process Terminologies
            .choice()
            .when(simple("{{idaas.processTerminologies}}"))
            // set Auditing Properties
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-EDI")
            .setProperty("industrystd").constant("EDI")
            .setProperty("messagetrigger").constant("5010-837")
            .setProperty("component").simple("${routeId}")
            .setProperty("processname").constant("terminologies")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("auditdetails").constant("Event sent to Defined Topic for terminology processing")
            // iDAAS KIC - Auditing Processing
            .to("direct:auditing")
            // Write Parsed FHIR Terminology Transactions to Topic
            .to("direct:terminologies")
            .endChoice();
    ;

    /*
     *  File Processing for EDI
     */

    from("file:{{270.inputdirectory}}/")
            .routeId("270-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("{{idaas.topicName270}}"))
            .to("file:{{idaas.topicName270_Output}}/");

    from("file:{{271.inputdirectory}}/")
            .routeId("271-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("{{idaas.topicName271}}"))
            .to("file:{{idaas.topicName271_Output}}/");

    from("file:{{276.inputdirectory}}/")
            .routeId("276-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("{{idaas.topicName276}}"))
            .to("file:{{idaas.topicName276_Output}}/");

    from("file:{{277.inputdirectory}}/")
            .routeId("277-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("{{idaas.topicName277}}"))
            .to("file:{{idaas.topicName277_Output}}/");

    from("file:{{834.inputdirectory}}/")
            .routeId("834-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("{{idaas.topicName834}}"))
            .to("file:{{idaas.topicName834_Output}}/");

    from("file:{{835.inputdirectory}}/")
            .routeId("835-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("{{idaas.topicName835}}"))
            .to("file:{{idaas.topicName835_Output}}/");

    from("file:{{837.inputdirectory}}/")
            .routeId("837-EDI-File")
            .choice()
            .when(simple("${file:ext} == 'edi'"))
            .to(getKafkaTopicUri("{{idaas.topicName837}}"))
            .to("file:{{idaas.topicName837_Output}}/");
  }
}
