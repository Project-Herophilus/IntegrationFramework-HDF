/*
 * Copyright 2019
 */
package io.connectedhealth_idaas.hl7;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hl7.HL7;
import org.apache.camel.component.hl7.HL7MLLPNettyDecoderFactory;
import org.apache.camel.component.hl7.HL7MLLPNettyEncoderFactory;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
//import org.springframework.jms.connection.JmsTransactionManager;
//import javax.jms.ConnectionFactory;
import org.springframework.stereotype.Component;
//iDaaS Event Builder
import io.connectedhealth_idaas.eventbuilder.converters.ccda.CdaConversionService;
//import io.github.linuxforhealth.hl7.HL7ToFHIRConverter;
//import io.connectedhealth_idaas.eventbuilder.events.platform.HL7TerminologyProcessorEvent;
//import io.connectedhealth_idaas.eventbuilder.events.platform.DeIdentificationEvent;

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
        mapping.addUrlMappings("/idaas/*");
        return mapping;
    }

    private String getKafkaTopicUri(String topic) {
        return "kafka:" + topic + "?brokers=" + config.getKafkaBrokers();
    }
    /*
     *   Called to return a specific MLLP server based connection string for usage
     *   Accepts a port value retrieved from the properties of the asset
     */
    private String getHL7Uri(int port) {
        String s = "mllp:0.0.0.0:" + port;
        //camel.dataformat.hl7.validate=false
        return s;
    }
    /*
     *   Calls to return a specific Directory based connection string for usage
     *   Accepts a directory value retrieved from the properties of the asset
     */
    private String getHL7UriDirectory(String dirName) {
        return "file:src/" + dirName + "?delete=true?";
    }
    private String getHL7CCDAUriDirectory(String dirName) {
        return "file:src/" + dirName + "?delete=true";
    }

    /*
     * Kafka implementation based upon https://camel.apache.org/components/latest/kafka-component.html
     *
     */
    @Override
    public void configure() throws Exception {

        /*
         *  Direct component within platform to ensure we can centralize logic
         *  There are some values we will need to set within every route
         *  We are doing this to ensure we dont need to build a series of beans
         *  and we keep the processing as lightweight as possible
         *
         *   Simple language reference
         *   https://camel.apache.org/components/latest/languages/simple-language.html
         *   HIDN
         *   HIDN - Health information Data Network
         *   Intended to enable simple movement of data aside from specific standards
         *   Common Use Cases are areas to support remote (iOT/Edge) and any other need for small footprints to larger
         *   footprints
         *
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
         * KIC
         *
         * Direct component within platform to ensure we can centralize logic
         * There are some values we will need to set within every route
         * We are doing this to ensure we dont need to build a series of beans
         * and we keep the processing as lightweight as possible
         *
         *   Simple language reference
         *   https://camel.apache.org/components/latest/languages/simple-language.html
         *
         */

        //Integration Based Auditing
        from("direct:auditing")
                .routeId("iDaaS-DataIntegration-KIC")
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

        // App Integration
        from("direct:transactionauditing")
                .routeId("iDaaS-AppIntegration-KIC")
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
                .setHeader("errorID").exchangeProperty("internalMsgID")
                .setHeader("errorData").exchangeProperty("bodyData")
                .setHeader("transactionCount").exchangeProperty("transactionCount")
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.appintegrationTopic}}"));
        /*
         *   General Output Logging
         */
        from("direct:logging")
                .routeId("logging")
                .log(LoggingLevel.INFO, log, "HL7 Message: [${body}]")
        ;

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

        /*
         *   Servlet endpoint for CCDA
         */
        from("servlet://ccda")
                .routeId("ccda-post")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7-CCDA")
                .setProperty("messagetrigger").constant("CCDA")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("CCDA document received")
                // iDAAS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.ccdaTopicName}}"))
                // Convert CCDA to FHIR
                .choice()
                .when(simple("{{idaas.convertCCDAtoFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("CCDA")
                .setProperty("messagetrigger").constant("")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("conversion")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                //Invocation of CCDA Conversion
                // Unmarshall from XML Doc against XSD - or Bean to encapsulate features
                .bean(CdaConversionService.class, "getFhirJsonFromCdaXMLString(${body})")
                .setProperty("auditdetails").constant("CCDA to FHIR conversion event called")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.ccdaTopicName}}"))
                .endChoice();
        ;


        /*
         *
         * HL7 File Based Implementations
         *  ------------------------------
         *  HL7 implementation based upon https://camel.apache.org/components/latest/dataformats/hl7-dataformat.html
         *  Much like the upstream effort this code is based on:
         *  The fictitious medical org.: MCTN
         *  The fictitious app: MMS
         *
         */

        // ADT
        from(getHL7UriDirectory(config.getHl7ADT_Directory()))
                .routeId("hl7FileBasedAdmissions")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ADT")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("ADT message received")
                // iDAAS KIC Event
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.adtTopicName}}"))
        ;
        // ORM
        from(getHL7UriDirectory(config.getHl7ORM_Directory()))
                .routeId("hl7FileBasedOrders")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORM")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("ORM message received")
                // iDAAS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.ormTopicName}}"))
        ;
        // ORU
        from(getHL7UriDirectory(config.getHl7ORU_Directory()))
                .routeId("hl7FileBasedResults")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORU")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("ORU message received")
                // iDAAS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.oruTopicName}}"))
        ;
        // MFN
        from(getHL7UriDirectory(config.getHl7MFN_Directory()))
                .routeId("hl7FileBasedMasterFiles")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MFN")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("MFN message received")
                // iDAAS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.mfnTopicName}}"))
        ;
        // MDM
        from(getHL7UriDirectory(config.getHl7MDM_Directory()))
                .routeId("hl7FileBasedMasterDocs")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MDM")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("MDM message received")
                // iDAAS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.mdmTopicName}}"))
        ;
        // RDE
        from(getHL7UriDirectory(config.getHl7RDE_Directory()))
                .routeId("hl7FilePharmacy")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("RDE")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("RDE message received")
                // iDAAS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.rdeTopicName}}"))
        ;
        // SCH
        from(getHL7UriDirectory(config.getHl7SCH_Directory()))
                .routeId("hl7FileSchedules")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("SCH")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("SCH message received")
                // iDAAS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.schTopicName}}"))
        ;
        // VXU
        from(getHL7UriDirectory(config.getHl7VXU_Directory()))
                .routeId("hl7FileVaccinations")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("VXU")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("VXU message received")
                // iDAAS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.vxuTopicName}}"))
        ;

        // CCDA
        from(getHL7CCDAUriDirectory(config.getHl7CCDA_Directory()))
                .routeId("ccdaProcessor")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7-CCDA")
                .setProperty("messagetrigger").constant("CCDA")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("CCDA document received")
                // iDAAS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.ccdaTopicName}}"))
                // Convert CCDA to FHIR
                .choice()
                .when(simple("{{idaas.convertCCDAtoFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("CCDA")
                .setProperty("messagetrigger").constant("")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("conversion")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                //Invocation of CCDA Conversion
                // Unmarshall from XML Doc against XSD - or Bean to encapsulate features
                .bean(CdaConversionService.class, "getFhirJsonFromCdaXMLString(${body})")
                .setProperty("auditdetails").constant("CCDA to FHIR conversion event called")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.ccdaTopicName}}"))
                .endChoice();
        ;


        /*
         * https://camel.apache.org/components/3.7.x/mllp-component.html
         * HL7 v2x Server Implementations
         *  ------------------------------
         *  HL7 implementation based upon https://camel.apache.org/components/latest/dataformats/hl7-dataformat.html
         *  Much like the upstream effort this code is based on:
         *  The fictitious medical org.: MCTN
         *  The fictitious app: MMS
         *
         * https://camel.apache.org/components/2.x/dataformats/hl7-dataformat.html
         */
        // ADT
        from(getHL7Uri(config.getAdtPort()))
                .routeId("hl7MLLPAdmissions")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ADT")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("ADT message received")
                // iDaaS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri(config.getadtTopicName()))
                //Process Terminologies
                .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ADT")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                //.bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("Event sent to Defined Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
                /*Convert to FHIR
                .choice()
                   .when(simple("{{idaas.convert2FHIR}}"))
                   // set Auditing Properties
                   .setProperty("processingtype").constant("data")
                   .setProperty("appname").constant("iDAAS-Connect-HL7")
                   .setProperty("industrystd").constant("HL7")
                   .setProperty("messagetrigger").constant("ADT")
                   .setProperty("component").simple("conversion-FHIR")
                   .setProperty("camelID").simple("${camelId}")
                   .setProperty("exchangeID").simple("${exchangeId}")
                   .setProperty("internalMsgID").simple("${id}")
                   // Conversion
                   //.bean(HL7ToFHIRConverter.class, "convert(${body})")
                   .setProperty("bodyData").simple("${body}")
                   .setProperty("auditdetails").constant("converted HL7 resource ${body}")
                   // iDAAS KIC - Auditing Processing
                   .to("direct:auditing")
                   // Persist
                   .convertBodyTo(String.class).to(getKafkaTopicUri("fhir_conversion"))*/
                .choice()
                // Acknowledgement must go last as we leverage the HL7.ack as the inbound stream thus changing
                // everything after it
                .when(simple("{{idaas.adtACKResponse}}"))
                .transform(HL7.ack())
                // This would enable persistence of the ACK
                .convertBodyTo(String.class)
                .setProperty("bodyData").simple("${body}")
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ADT")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("processname").constant("Input")
                .setProperty("auditdetails").constant("ADT ACK Processed")
                .setProperty("bodyData").simple("${body}")
                // iDaaS KIC Processing
                .wireTap("direct:auditing")
                .endChoice();

        // ORM
        from(getHL7Uri(config.getOrmPort()))
                .routeId("hl7MLLPOrders")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORM")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("ORM message received")
                // iDaaS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.ormTopicName}}"))
                //Response to HL7 Message Sent Built by platform
                .choice().when(simple("{{idaas.ormACKResponse}}"))
                .transform(HL7.ack())
                // This would enable persistence of the ACK
                .convertBodyTo(String.class)
                .setProperty("bodyData").simple("${body}")
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORM")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("processname").constant("Input")
                .setProperty("auditdetails").constant("ACK Processed")
                // iDaaS KIC Processing
                .wireTap("direct:auditing")
                .endChoice();

        // ORU
        from(getHL7Uri(config.getOruPort()))
                .routeId("hl7MLLPResults")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORU")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("ORU message received")
                // iDaaS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.oruTopicName}}"))
                //Response to HL7 Message Sent Built by platform
                .choice().when(simple("{{idaas.oruACKResponse}}"))
                .transform(HL7.ack())
                // This would enable persistence of the ACK
                .convertBodyTo(String.class)
                .setProperty("bodyData").simple("${body}")
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORU")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("processname").constant("Input")
                .setProperty("auditdetails").constant("ACK Processed")
                // iDaaS KIC Processing
                .wireTap("direct:auditing")
                .endChoice();

        // MFN
        from(getHL7Uri(config.getMfnPort()))
                .routeId("hl7MLLPMasterFiles")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MFN")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("MFN message received")
                // iDaaS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.mfnTopicName}}"))
                //Response to HL7 Message Sent Built by platform
                .choice().when(simple("{{idaas.mfnACKResponse}}"))
                .transform(HL7.ack())
                // This would enable persistence of the ACK
                .convertBodyTo(String.class)
                .setProperty("bodyData").simple("${body}")
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MFN")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("processname").constant("Input")
                .setProperty("auditdetails").constant("ACK Processed")
                // iDaaS KIC Processing
                .wireTap("direct:auditing")
                .endChoice();


        // MDM
        from(getHL7Uri(config.getMdmPort()))
                .routeId("hl7MLLPMasterDocs")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MDM")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant(("MDM message received"))
                //iDaaS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.mdmTopicName}}"))
                //Response to HL7 Message Sent Built by platform
                .choice().when(simple("{{idaas.mdmACKResponse}}"))
                .transform(HL7.ack())
                // This would enable persistence of the ACK
                .convertBodyTo(String.class)
                .setProperty("bodyData").simple("${body}")
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MDM")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("processname").constant("Input")
                .setProperty("auditdetails").constant("ACK Processed")
                // iDaaS KIC Processing
                .wireTap("direct:auditing")
                .endChoice();

        // RDE
        from(getHL7Uri(config.getRdePort()))
                .routeId("hl7MLLPPharmacy")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("RDE")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant(("RDE message received"))
                //iDaaS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.rdeTopicName}}"))
                //Response to HL7 Message Sent Built by platform
                .choice().when(simple("{{idaas.rdeACKResponse}}"))
                .transform(HL7.ack())
                // This would enable persistence of the ACK
                .convertBodyTo(String.class)
                .setProperty("bodyData").simple("${body}")
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("RDE")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("processname").constant("Input")
                .setProperty("auditdetails").constant("ACK Processed")
                // iDaaS KIC Processing
                .wireTap("direct:auditing")
                .endChoice();

        // SCH
        from(getHL7Uri(config.getSchPort()))
                .routeId("hl7MLLPSchedule")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("SCH")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant(("SCH message received"))
                //iDaaS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.schTopicName}}"))
                //Response to HL7 Message Sent Built by platform
                .choice().when(simple("{{idaas.schACKResponse}}"))
                .transform(HL7.ack())
                // This would enable persistence of the ACK
                .convertBodyTo(String.class)
                .setProperty("bodyData").simple("${body}")
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("SCH")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("processname").constant("Input")
                .setProperty("auditdetails").constant("ACK Processed")
                // iDaaS KIC Processing
                .wireTap("direct:auditing")
                .endChoice();

        // VXU
        from(getHL7Uri(config.getVxuPort()))
                .routeId("hl7MLLPVaccination")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("VXU")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant(("VXU message received"))
                //iDaaS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.vxuTopicName}}"))
                //Response to HL7 Message Sent Built by platform
                .choice().when(simple("{{idaas.vxuACKResponse}}"))
                .transform(HL7.ack())
                // This would enable persistence of the ACK
                .convertBodyTo(String.class)
                .setProperty("bodyData").simple("${body}")
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("VXU")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("processname").constant("Input")
                .setProperty("auditdetails").constant("ACK Processed")
                // iDaaS KIC Processing
                .wireTap("direct:auditing")
                .endChoice()
        ;

    }


}