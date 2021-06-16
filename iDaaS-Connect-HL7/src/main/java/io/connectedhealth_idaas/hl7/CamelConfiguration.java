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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CamelConfiguration extends RouteBuilder {
    private static final Logger log = LoggerFactory.getLogger(CamelConfiguration.class);

    @Autowired
    private ConfigProperties config;

    @Bean
    private HL7MLLPNettyEncoderFactory hl7Encoder() {
        HL7MLLPNettyEncoderFactory encoder = new HL7MLLPNettyEncoderFactory();
        encoder.setCharset("iso-8859-1");
        //encoder.setConvertLFtoCR(true);
        return encoder;
    }

    @Bean
    private HL7MLLPNettyDecoderFactory hl7Decoder() {
        HL7MLLPNettyDecoderFactory decoder = new HL7MLLPNettyDecoderFactory();
        decoder.setCharset("iso-8859-1");
        return decoder;
    }

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

    private String getKafkaTopicUri(String topic) {
        return "kafka:" + topic +
               "?brokers=" +
               config.getKafkaBrokers();
    }

    private String getHL7Uri(int port) {
        return "netty:tcp://0.0.0.0:" + port + "?sync=true&decoders=#hl7Decoder&encoders=#hl7Encoder";
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
         *
         *   Simple language reference
         *   https://camel.apache.org/components/latest/languages/simple-language.html
         *
         */
        from("direct:auditing")
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
         *  Logging
         */
        from("direct:logging")
            .log(LoggingLevel.INFO, log, "HL7 Message: [${body}]")
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
        /*
         *
         * HL7 v2x Server Implementations
         *  ------------------------------
         *  HL7 implementation based upon https://camel.apache.org/components/latest/dataformats/hl7-dataformat.html
         *  For leveraging HL7 based files:
         *  from("file:src/data-in/hl7v2/adt?delete=true?noop=true")
         *
         *  Much like the upstream effort this code is based on:
         *  The fictitious medical org.: MCTN
         *  The fictitious app: MMS
         *
         */
        // ADT
        //from(getHL7Uri(config.getAdtPort()))
        from(getHL7Uri(config.getAdtPort()))
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
             // iDAAS DataHub Processing
             .wireTap("direct:auditing")
             // Send to Topic
             .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_adt"))
             //Response to HL7 Message Sent Built by platform
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
             .setProperty("auditdetails").constant("ACK Processed")
             // iDAAS DataHub Processing
             .wireTap("direct:auditing");
        
        // ORM
        //from(getHL7Uri(config.getAdtPort()))
        from(getHL7Uri(config.getOrmPort()))
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
            // iDAAS DataHub Processing
            .wireTap("direct:auditing")
            // Send to Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_orm"))
            //Response to HL7 Message Sent Built by platform
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
            // iDAAS DataHub Processing
            .wireTap("direct:auditing");

        // ORU
        //from(getHL7Uri(config.getAdtPort()))
        from(getHL7Uri(config.getOruPort()))
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
            .setProperty("auditdetails").constant("ORM message received")
            // iDAAS DataHub Processing
            .wireTap("direct:auditing")
            // Send to Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_oru"))
            //Response to HL7 Message Sent Built by platform
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
            // iDAAS DataHub Processing
            .wireTap("direct:auditing");

        // MFN
        //from(getHL7Uri(config.getAdtPort()))
        from(getHL7Uri(config.getMfnPort()))
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
            // iDAAS DataHub Processing
            .wireTap("direct:auditing")
            // Send to Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_mfn"))
            //Response to HL7 Message Sent Built by platform
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
            // iDAAS DataHub Processing
            .wireTap("direct:auditing");

        // MDM
        from(getHL7Uri(config.getMdmPort()))
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
            //iDAAS DataHub Processing
            .wireTap("direct:auditing")
            // Send to Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_mdm"))
            //Response to HL7 Message Sent Built by platform
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
            // iDAAS DataHub Processing
            .wireTap("direct:auditing");

        // RDE
        from(getHL7Uri(config.getRdePort()))
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
                //iDAAS DataHub Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_rde"))
                //Response to HL7 Message Sent Built by platform
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
                // iDAAS DataHub Processing
                .wireTap("direct:auditing");

        // SCH
        from(getHL7Uri(config.getSchPort()))
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
                //iDAAS DataHub Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_sch"))
                //Response to HL7 Message Sent Built by platform
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
                // iDAAS DataHub Processing
                .wireTap("direct:auditing");

        // VXU
        from(getHL7Uri(config.getVxuPort()))
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
                //iDAAS DataHub Processing
                .wireTap("direct:auditing")
                // Send to Topic
                .convertBodyTo(String.class).to(getKafkaTopicUri("mctn_mms_vxu"))
                //Response to HL7 Message Sent Built by platform
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
                // iDAAS DataHub Processing
                .wireTap("direct:auditing");
    }

}
