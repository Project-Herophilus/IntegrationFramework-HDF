/*
 * Copyright 2019 Project-Herophilus

 */
package io.connectedhealth_idaas.cmsinteroperability;

import java.util.ArrayList;
import java.util.List;

//import javax.jms.ConnectionFactory;
//import org.springframework.jms.connection.JmsTransactionManager;
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
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

/*
 *
 * General Links
 * https://camel.apache.org/components/latest/eips/split-eip.html
 * Basic Links for Implementations
 * All non healthcare industry and non public cloud connectors can be
 * leveraged.
 * https://camel.apache.org/components/3.16.x/index.html
 *
 */

//@Component
public class CamelConfiguration extends RouteBuilder {
  private static final Logger log = LoggerFactory.getLogger(CamelConfiguration.class);

  @Autowired
  private ConfigProperties config;

private String getKafkaTopicUri(String topic)
{
  return "kafka:" + topic + "?brokers=" + config.getKafkaBrokers();
}

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



  @Override
  public void configure() throws Exception {

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
