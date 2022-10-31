/*
 * Copyright 2019 Project-Herophilus
 */
package io.connectedhealth_idaas.hl7;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
// HL7 to FHIR Conversion
import io.github.linuxforhealth.hl7.HL7ToFHIRConverter;
// CCDA to FHIR Conversion
import io.connectedhealth_idaas.eventbuilder.converters.ccda.CdaConversionService;

//@Component
public class CamelConfiguration extends RouteBuilder {
    private static final Logger log = LoggerFactory.getLogger(CamelConfiguration.class);

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

    /*
     * Kafka implementation based upon https://camel.apache.org/components/latest/kafka-component.html
     *
     */
    @Override
    public void configure() throws Exception {

        /*
         * KIC
         *
         * Direct component within platform to ensure we can centralize logic
         * There are some values we will need to set within every route
         * We are doing this to ensure we dont need to build a series of beans
         * and we keep the processing as lightweight as possible
         *   Simple language reference
         *   https://camel.apache.org/components/latest/languages/simple-language.html
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
                .convertBodyTo(String.class).to("kafka:{{idaas.integrationTopic}}?brokers={{idaas.kafkaBrokers}}");

        /*
         *   General Output Logging
         */
        from("direct:logging")
                .routeId("logging")
                .log(LoggingLevel.INFO, log, "Message: [${body}]")
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
                .convertBodyTo(String.class).to("kafka:{{idaas.terminologyTopic}}?brokers={{idaas.kafkaBrokers}}");

        from("direct:ccdafhirconversion")
                .routeId("iDaaS-CCDAFHIRConversion")
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
                .convertBodyTo(String.class).to("kafka:{{idaas.fhirConversionTopic}}?brokers={{idaas.kafkaBrokers}}");

        from("direct:publiccloud")
                .routeId("iDaaS-Connect-HL7-PublicCloud")
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
                .convertBodyTo(String.class).to("kafka:{{idaas.cloudTopic}}?brokers={{idaas.kafkaBrokers}}");


        // CCDA
        // Directory Processing
        from("file:{{idaas.hl7.ccda.dir}}?delete=true")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.ccdaTopicName}}?brokers={{idaas.kafkaBrokers}}")
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
                .to("direct:ccdafhirconversion")
             .endChoice();
        ;
        // Servlet Endpoint
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
             .convertBodyTo(String.class).to("kafka:{{idaas.ccdaTopicName}}?brokers={{idaas.kafkaBrokers}}")
             // Public Cloud - Original Message
             .choice()
                .when(simple("{{idaas.processPublicCloud}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("CCDA")
                .setProperty("messagetrigger").constant("CCDA")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("conversion")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").simple("CCDA message ${exchangeId}")
                .to("direct:auditing")
                .to("direct:publiccloud")
             // Convert CCDA to FHIR
             .choice()
                .when(simple("{{idaas.convertCCDAtoFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("CCDA")
                .setProperty("messagetrigger").constant("CCDA")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("conversion")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                //Invocation of CCDA Conversion
                // Unmarshall from XML Doc against XSD - or Bean to encapsulate features
                .bean(CdaConversionService.class, "getFhirJsonFromCdaXMLString(${body})")
                .setProperty("auditdetails").simple("Converted CCDA to FHIR Resource Bundle for message ${exchangeId}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:ccdafhirconversion")
                .choice()
                    .when(simple("{{idaas.processPublicCloud}}"))
                    // set Auditing Properties
                    .setProperty("processingtype").constant("data")
                    .setProperty("appname").constant("iDAAS-Connect-HL7")
                    .setProperty("industrystd").constant("CCDA")
                    .setProperty("messagetrigger").constant("CCDA")
                    .setProperty("component").simple("${routeId}")
                    .setProperty("processname").constant("conversion")
                    .setProperty("camelID").simple("${camelId}")
                    .setProperty("exchangeID").simple("${exchangeId}")
                    .setProperty("internalMsgID").simple("${id}")
                    .setProperty("bodyData").simple("${body}")
                    .setProperty("auditdetails").simple("CCDA message ${exchangeId}")
                    .setProperty("auditdetails").simple("Converted CCDA to FHIR Resource Bundle for message ${exchangeId}")
                    .to("direct:auditing")
                    .to("direct:publiccloud")
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

        // HTTP
        // Servlet Endpoint
        from("servlet://hl7")
                .routeId("hl7-post")
                .convertBodyTo(String.class)
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7-HTTP")
                .setProperty("messagetrigger").constant("HL7")
                .setProperty("componentname").simple("${routeId}")
                .setProperty("processname").constant("Input")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("HL7 over HTTP document received")
                // iDAAS KIC Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to("kafka:{{idaas.hl7HTTPTopicName}}?brokers={{idaas.kafkaBrokers}}")
                // Convert HL7 to FHIR
                .choice()
                    .when(simple("{{idaas.convertHL7toFHIR}}"))
                    // set Auditing Properties
                    .setProperty("processingtype").constant("data")
                    .setProperty("appname").constant("iDAAS-Connect-HL7")
                    .setProperty("industrystd").constant("HL7")
                    .setProperty("messagetrigger").constant("HTTP")
                    .setProperty("component").simple("conversion-FHIR")
                    .setProperty("camelID").simple("${camelId}")
                    .setProperty("exchangeID").simple("${exchangeId}")
                    .setProperty("internalMsgID").simple("${id}")
                    // Conversion
                    .bean(HL7ToFHIRConverter.class, "convert(${body})")
                    .setProperty("bodyData").simple("${body}")
                    .setProperty("auditdetails").simple("Converted HL7 Admission to FHIR Resource Bundle for message ${exchangeId}")
                    // iDAAS KIC - Auditing Processing
                    .to("direct:auditing")
                    // Persist
                    .to("direct:ccdafhirconversion")
                .endChoice();
        ;
        // ADT
        // Directory for File Based HL7
        from("file:{{idaas.hl7.adt.dir}}?delete=true")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.adtTopicName}}?brokers={{idaas.kafkaBrokers}}")
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
                .setProperty("auditdetails").constant("HL7 Admission Event sent topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
             /*Convert to FHIR */
             .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
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
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("Converted HL7 Admission to FHIR Resource")
                .setProperty("auditdetails").simple("Converted HL7 Admission to FHIR Resource Bundle for message ${exchangeId}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .to("direct:ccdafhirconversion")
             .endChoice()
        ;
        // MLLP
        from("mllp:0.0.0.0:{{idaas.adtPort}}")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.adtTopicName}}?brokers={{idaas.kafkaBrokers}}")
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
                 .setProperty("auditdetails").constant("HL7 Admission Event sent topic for terminology processing")
                 // iDAAS KIC - Auditing Processing
                 .to("direct:auditing")
                 // Write Parsed FHIR Terminology Transactions to Topic
                 .to("direct:terminologies")
             /*Convert to FHIR */
             .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ADT")
                .setProperty("component").simple("conversion-FHIR")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("processname").constant("conversion")
                .setProperty("internalMsgID").simple("${id}")
                // Conversion
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").simple("Converted HL7 Admission to FHIR Resource Bundle for message ${exchangeId}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .to("direct:ccdafhirconversion")
             /*.choice()
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
                 .wireTap("direct:auditing") */
             .endChoice();
        // ORM
        // HL7 Directory for Procesing Data
        from("file:{{idaas.hl7.orm.dir}}?delete=true")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.ormTopicName}}?brokers={{idaas.kafkaBrokers}}")
             //Process Terminologies
             .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORM")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                .setProperty("auditdetails").constant("HL7 Order sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
                /*Convert to FHIR */
             .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORM")
                .setProperty("component").simple("conversion-FHIR")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                // Conversion
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("Converted HL7 Order to FHIR Resource ${body}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .to("direct:ccdafhirconversion")
             .endChoice()
        ;
        // MLLP
        from("mllp:0.0.0.0:{{idaas.ormPort}}")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.ormTopicName}}?brokers={{idaas.kafkaBrokers}}")
             //Process Terminologies
             .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORM")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                .setProperty("auditdetails").constant("HL7 Order sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
             /*Convert to FHIR */
             .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORM")
                .setProperty("component").simple("conversion-FHIR")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                // Conversion
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("Converted HL7 Order to FHIR Resource ${body}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .to("direct:ccdafhirconversion")
                /*
                Response to HL7 Message Sent Built by platform
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
                .wireTap("direct:auditing")*/
                .endChoice();

        // ORU
        // HL7 directory for Processing Data
        from("file:{{idaas.hl7.oru.dir}}?delete=true")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.oruTopicName}}?brokers={{idaas.kafkaBrokers}}")
             //Process Terminologies
             .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORU")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                //.bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("HL7 Result sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
             /*Convert to FHIR */
             .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORU")
                .setProperty("component").simple("conversion-FHIR")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                // Conversion
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("Converted HL7 Result to FHIR resource ${body}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .to("direct:ccdafhirconversion")
             .endChoice()
        ;
        // MLLP
        from("mllp:0.0.0.0:{{idaas.oruPort}}")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.oruTopicName}}?brokers={{idaas.kafkaBrokers}}")
             //Process Terminologies
             .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORU")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                //.bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("HL7 Result sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
             /*Convert to FHIR */
             .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("ORU")
                .setProperty("component").simple("conversion-FHIR")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                // Conversion
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("Converted HL7 Result to FHIR resource ${body}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .to("direct:ccdafhirconversion")
                /*
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
                .wireTap("direct:auditing")*/
             .endChoice();

        // MFN
        // Directory for Processing HL7
        from("file:{{idaas.hl7.mfn.dir}}?delete=true")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.mfnTopicName}}?brokers={{idaas.kafkaBrokers}}")
             // Terminologies
             .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MFN")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                //.bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("HL7 Master File sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
             /*Convert to FHIR */
             /* Not supported in upstream library
             .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MFN")
                .setProperty("component").simple("conversion-FHIR")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                // Conversion
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("Converted HL7 Master File to FHIR resource ${body}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .convertBodyTo(String.class).to(getKafkaTopicUri("fhir_conversion"))
                */
             .endChoice()
        ;
        // MLLP
        from("mllp:0.0.0.0:{{idaas.mfnPort}}")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.mfnTopicName}}?brokers={{idaas.kafkaBrokers}}")
             // Terminologies
             .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MFN")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                //.bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("HL7 Master File sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
             /*Convert to FHIR */
             /* Not supported in upstream library
             .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MFN")
                .setProperty("component").simple("conversion-FHIR")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                // Conversion
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("Converted HL7 Master File to FHIR resource ${body}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .convertBodyTo(String.class).to(getKafkaTopicUri("fhir_conversion"))
                */
             //Response to HL7 Message Sent Built by platform
             /*
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
                .wireTap("direct:auditing")*/
             .endChoice();


        // MDM
        // Directory for Processing HL7 Files
        from("file:{{idaas.hl7.mdm.dir}}?delete=true")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.mdmTopicName}}?brokers={{idaas.kafkaBrokers}}")
             // Terminologies
             .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MDM")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                //.bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("HL7 Master Doc sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
            /*Convert to FHIR */
            /* Not supported in upstream library
            .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MDM")
                .setProperty("component").simple("conversion-FHIR")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                // Conversion
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("Converted HL7 Document to FHIR resource ${body}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .convertBodyTo(String.class).to(getKafkaTopicUri("fhir_conversion"))
                */
             .endChoice()
        ;
        // MLLP
        from("mllp:0.0.0.0:{{idaas.mdmPort}}")
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
              .convertBodyTo(String.class).to("kafka:{{idaas.mdmTopicName}}?brokers={{idaas.kafkaBrokers}}")
              // Terminologies
              .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MDM")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                //.bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("HL7 Master Doc sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
                /*Convert to FHIR */
                /* Not supported in upstream library
                .choice()
                   .when(simple("{{idaas.convertHL7toFHIR}}"))
                   // set Auditing Properties
                   .setProperty("processingtype").constant("data")
                   .setProperty("appname").constant("iDAAS-Connect-HL7")
                   .setProperty("industrystd").constant("HL7")
                   .setProperty("messagetrigger").constant("MDM")
                   .setProperty("component").simple("conversion-FHIR")
                   .setProperty("camelID").simple("${camelId}")
                   .setProperty("exchangeID").simple("${exchangeId}")
                   .setProperty("internalMsgID").simple("${id}")
                   // Conversion
                   .bean(HL7ToFHIRConverter.class, "convert(${body})")
                   .setProperty("bodyData").simple("${body}")
                   .setProperty("auditdetails").constant("Converted HL7 Document to FHIR resource ${body}")
                   // iDAAS KIC - Auditing Processing
                   .to("direct:auditing")
                   // Persist
                   .convertBodyTo(String.class).to(getKafkaTopicUri("fhir_conversion"))
                */
               /*
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
                .wireTap("direct:auditing")*/
              .endChoice();

        // RDE
        // Directory Processing of HL7
        from("file:{{idaas.hl7.rde.dir}}?delete=true")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.rdeTopicName}}?brokers={{idaas.kafkaBrokers}}")
             // Terminologies
             .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("RDE")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                //.bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("HL7 RDE Pharmacy sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
             /*Convert to FHIR */
             /* Not completely supported in upstream library */
             .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MFN")
                .setProperty("component").simple("conversion-FHIR")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                // Conversion
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("Converted HL7 Pharmacy to FHIR resource ${body}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .to("direct:ccdafhirconversion")
             .endChoice()
        ;
        // MLLP
        from("mllp:0.0.0.0:{{idaas.rdePort}}")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.rdeTopicName}}?brokers={{idaas.kafkaBrokers}}")
             // Terminologies
             .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("RDE")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                //.bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("HL7 RDE Pharmacy sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
             /*Convert to FHIR */
             /* Not completely supported in upstream library */
             .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("MFN")
                .setProperty("component").simple("conversion-FHIR")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                // Conversion
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("Converted HL7 Pharmacy to FHIR resource ${body}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .to("direct:ccdafhirconversion")
             //Response to HL7 Message Sent Built by platform
             /*
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
                .wireTap("direct:auditing")*/
             .endChoice();

        // SCH
        // Directory Based Processing
        from("file:{{idaas.hl7.sch.dir}}?delete=true")
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
            .convertBodyTo(String.class).to("kafka:{{idaas.schTopicName}}?brokers={{idaas.kafkaBrokers}}")
            // Terminologies
            .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("SCH")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                //.bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("HL7 Schedule sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
            /*Convert to FHIR */
            /* Not completely supported in upstream library */
            .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("SCH")
                .setProperty("component").simple("conversion-FHIR")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                // Conversion
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("Converted HL7 Schedule to FHIR resource ${body}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .to("direct:ccdafhirconversion")
                .endChoice()
            ;
        // MLLP
        from("mllp:0.0.0.0:{{idaas.schPort}}")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.schTopicName}}?brokers={{idaas.kafkaBrokers}}")
             // Terminologies
             .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("SCH")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                //.bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("HL7 Schedule sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
                /*Convert to FHIR */
                /* Not completely supported in upstream library */
             .choice()
                 .when(simple("{{idaas.convertHL7toFHIR}}"))
                 // set Auditing Properties
                 .setProperty("processingtype").constant("data")
                 .setProperty("appname").constant("iDAAS-Connect-HL7")
                 .setProperty("industrystd").constant("HL7")
                 .setProperty("messagetrigger").constant("SCH")
                 .setProperty("component").simple("conversion-FHIR")
                 .setProperty("camelID").simple("${camelId}")
                 .setProperty("exchangeID").simple("${exchangeId}")
                 .setProperty("internalMsgID").simple("${id}")
                 // Conversion
                 .bean(HL7ToFHIRConverter.class, "convert(${body})")
                 .setProperty("bodyData").simple("${body}")
                 .setProperty("auditdetails").constant("Converted HL7 Schedule to FHIR resource ${body}")
                 // iDAAS KIC - Auditing Processing
                 .to("direct:auditing")
                 // Persist
                .to("direct:ccdafhirconversion")
             //Response to HL7 Message Sent Built by platform
             /*
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
                .wireTap("direct:auditing")*/
             .endChoice();

        // VXU
        // Directory Based File Processing
        from("file:{{idaas.hl7.vxu.dir}}?delete=true")
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
            .convertBodyTo(String.class).to("kafka:{{idaas.vxuTopicName}}?brokers={{idaas.kafkaBrokers}}")
            // Terminologies
            .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("VXU")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                //.bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("HL7 Vaccination sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
            /* Convert to FHIR */
            /* Not completely supported in upstream library */
            .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("VXU")
                .setProperty("component").simple("conversion-FHIR")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                // Conversion
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("Converted HL7 Vaccination to FHIR resource ${body}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .to("direct:ccdafhirconversion")
            .endChoice()
        ;
        // MLLP
        from("mllp:0.0.0.0:{{idaas.vxuPort}}")
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
             .convertBodyTo(String.class).to("kafka:{{idaas.vxuTopicName}}?brokers={{idaas.kafkaBrokers}}")
             // Terminologies
             .choice()
                .when(simple("{{idaas.processTerminologies}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("VXU")
                .setProperty("component").simple("${routeId}")
                .setProperty("processname").constant("terminologies")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                .setProperty("bodyData").simple("${body}")
                // Persist Data To Defined Topic for other processing
                //.bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("HL7 Vaccination sent to Topic for terminology processing")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
                /*Convert to FHIR */
                /* Not completely supported in upstream library */
             .choice()
                .when(simple("{{idaas.convertHL7toFHIR}}"))
                // set Auditing Properties
                .setProperty("processingtype").constant("data")
                .setProperty("appname").constant("iDAAS-Connect-HL7")
                .setProperty("industrystd").constant("HL7")
                .setProperty("messagetrigger").constant("VXU")
                .setProperty("component").simple("conversion-FHIR")
                .setProperty("camelID").simple("${camelId}")
                .setProperty("exchangeID").simple("${exchangeId}")
                .setProperty("internalMsgID").simple("${id}")
                // Conversion
                .bean(HL7ToFHIRConverter.class, "convert(${body})")
                .setProperty("bodyData").simple("${body}")
                .setProperty("auditdetails").constant("Converted HL7 Vaccination to FHIR resource ${body}")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Persist
                .to("direct:ccdafhirconversion")
             //Response to HL7 Message Sent Built by platform
             /*
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
                .wireTap("direct:auditing")*/
             .endChoice()
        ;

    }


}