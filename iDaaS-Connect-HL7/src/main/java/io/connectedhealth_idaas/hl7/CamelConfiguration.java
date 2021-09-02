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
import org.springframework.stereotype.Component;
//iDaaS Event Builder
import io.connectedhealth_idaas.eventbuilder.converters.ccda.CdaConversionService;
//import io.connectedhealth_idaas.eventbuilder.events.platform.DeIdentificationEvent;
import io.connectedhealth_idaas.eventbuilder.events.platform.HL7TerminologyProcessorEvent;

@Component
public class CamelConfiguration extends RouteBuilder {
    private static final Logger log = LoggerFactory.getLogger(CamelConfiguration.class);

    @Autowired
    private ConfigProperties config;

    /*
     *   Established KafkaEndpoint for usage
     */
    @Bean
    private KafkaEndpoint kafkaEndpoint() {
        KafkaEndpoint kafkaEndpoint = new KafkaEndpoint();
        return kafkaEndpoint;
    }

    @Bean
    private KafkaComponent kafkaComponent(KafkaEndpoint kafkaEndpoint) {
        KafkaComponent kafka = new KafkaComponent();
        return kafka;
    }

    /*
     *   Setups the configuration for a servlet to be used. This can be used to call a Web Service and/or REST API
     */
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

   /*
    *  Beans to be used for any stream related processes
    */

 /*    @Bean
   public DeIdentificationEvent deidentificationEventParser() {
       return new DeIdentificationEvent();
   }

      @Bean
    private CCDATransformer ccdaTransformer(String cdaDocument){
        CCDATransformer ccdaTransformer = new CCDATransformer();
        return ccdaTransformer;
    }

    @Bean
    private CCDAValidator ccdaValidator(String cdaDocument){
        CCDATransformer ccdaValidator = new CCDAValidator();
        return ccdaTransformer;
    }*/


    /*
     *   Called to return a specific Kafka URI string based connection string for usage
     *   Accepts a topic value retrieved from the properties of the asset
     */
    private String getKafkaTopicUri(String topic) {
        return "kafka:" + topic +
                "?brokers=" +
                config.getKafkaBrokers();
    }

    /*
     *   Called to return a specific MLLP server based connection string for usage
     *   Accepts a port value retrieved from the properties of the asset
     */
    private String getHL7Uri2(int port) {
        String s = "mllp:0.0.0.0:" + port;
        //camel.dataformat.hl7.validate=false
        return s;
    }

    /*
     *   Called to return a specific Directory based connection string for usage
     *   Accepts a directory value retrieved from the properties of the asset
     */
    private String getHL7UriDirectory(String dirName) {
        return "file:src/" + dirName + "?delete=true";
    }
    /*
     *   Called to return a specific Directory based connection string for usage
     *   Accepts a directory value retrieved from the properties of the asset
     */
    private String getHL7CCDAUriDirectory(String dirName) {
        return "file:src/" + dirName + "?delete=true";
    }

    /*
     * Code to execute when the application starts and runs
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
         * Transactional Audit
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
        from("direct:auditing")
            .routeId("iDaaS-Transactions-KIC")
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
            .convertBodyTo(String.class).to(getKafkaTopicUri("opsmgmt_platformtransactions"));

        /*
         * Transactional Audit
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
        from("direct:transactionauditing")
            .routeId("iDaaS-App-KIC")
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
            .convertBodyTo(String.class).to(getKafkaTopicUri("opsmgmt_appplatformtransactions"));


        /*
         *  Logging
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
                .convertBodyTo(String.class).to(getKafkaTopicUri("terminologies-hl7"));

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
            // Unmarshall from XML Doc against XSD - or Bean to encapsulate features
            // Send to Topic
           .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.vxuTopicName}}"))
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
        from(getHL7Uri2(config.getAdtPort()))
             .routeId("hl7Admissions")
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
             //Response to HL7 Message Sent Built by platform
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
                //Invocation of HL7 Terminology Parsing
                .bean(HL7TerminologyProcessorEvent.class, "hl7BuildTermsForProcessingToJSON('AllergyIntolerence', ${body})")
                .setProperty("auditdetails").constant("allergyintolerance terminology event called")
                // iDAAS KIC - Auditing Processing
                .to("direct:auditing")
                // Write Parsed FHIR Terminology Transactions to Topic
                .to("direct:terminologies")
            .endChoice();

        // ORM
        from(getHL7Uri2(config.getOrmPort()))
             .routeId("hl7Orders")
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
            .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_orm"))
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
        from(getHL7Uri2(config.getOruPort()))
            .routeId("hl7Results")
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
            .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_oru"))
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
        from(getHL7Uri2(config.getMfnPort()))
            .routeId("hl7MasterFiles")
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
            .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_mfn"))
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
        from(getHL7Uri2(config.getMdmPort()))
            .routeId("hl7MasterDocs")
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
            .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_mdm"))
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
        from(getHL7Uri2(config.getRdePort()))
            .routeId("hl7Pharmacy")
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
            .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_rde"))
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
        from(getHL7Uri2(config.getSchPort()))
            .routeId("hl7Schedule")
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
            .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_sch"))
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
        from(getHL7Uri2(config.getVxuPort()))
            .routeId("hl7Vaccination")
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
            .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_vxu"))
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
            .endChoice();

    }

}
