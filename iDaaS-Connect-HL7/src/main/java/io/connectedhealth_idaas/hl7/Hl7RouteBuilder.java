package io.connectedhealth_idaas.hl7;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Hl7RouteBuilder extends RouteBuilder {

    @Autowired
    //private S3Bean s3Bean;

    public static final String Terminology_ROUTE_ID = "terminologies-direct";
    public static final String CCDAConversion_ROUTE_ID = "ccdaconversion-direct";
    public static final String HL7Conversion_ROUTE_ID = "hl7conversion-direct";
    public static final String PublicCloud_ROUTE_ID = "publiccloud-direct";
    public static final String CCDAPost_ROUTE_ID = "ccda-post-inbound";
    public static final String HL7Post_ROUTE_ID = "ccda-post-inbound";
    public static final String HL7ADT_ROUTE_ID = "hl7-adt-inbound";

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .handled(true)
                .log(LoggingLevel.ERROR,"${exception}")
                .to("micrometer:counter:hl7_exception_handled");

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
                //.routeId("iDaaS-Terminologies")
                //.convertBodyTo(String.class).to("kafka:{{idaas.terminologyTopic}}?brokers={{idaas.kafkaBrokers}}");
                .routeId(Terminology_ROUTE_ID)
                .to("log:" + Terminology_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:terminologyTransactions")
                .to("kafka:{{idaas.terminology.topic.name}}?brokers={{idaas.kafka.brokers}}");

        from("direct:ccdafhirconversion")
                .routeId(CCDAConversion_ROUTE_ID)
                .to("log:" + CCDAConversion_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:ccdaConversionTransactions")
                .to("kafka:{{idaas.ccdaconversion.topic.name}}?brokers={{idaas.kafka.brokers}}");

        from("direct:hl7fhirconversion")
                .routeId(HL7Conversion_ROUTE_ID)
                .to("log:" + HL7Conversion_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:hl7ConversionTransactions")
                .to("kafka:{{idaas.hl7conversion.topic.name}}?brokers={{idaas.kafka.brokers}}");

        from("direct:publiccloud")
                .routeId(PublicCloud_ROUTE_ID)
                .to("log:" + PublicCloud_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:publiccloudTransactions")
                .to("kafka:{{idaas.publiccloud.topic.name}}?brokers={{idaas.kafka.brokers}}");

        /*
         *   Rest EndPoints
         */
        from("rest:post/idaas/ccda")
                .routeId(CCDAPost_ROUTE_ID)
                .to("log:" + CCDAPost_ROUTE_ID + "?showAll=true")
                .log("${exchangeId} fully processed")
                .to("micrometer:counter:ccdaPostedTransactions")
                .to("kafka:{{idaas.ccdapost.topic.name}}?brokers={{idaas.kafka.brokers}}");
                // Public Cloud
                .choice()
                    .when(simple("{{idaas.process.PublicCloud}}"))
                        .to("direct:publiccloud")
                // Convert CCDA to FHIR
                .choice()
                .when(simple("{{idaas.convert.CCDAtoFHIR}}"))
                    //Invocation of CCDA Conversion
                    // Unmarshall from XML Doc against XSD - or Bean to encapsulate features
                        .bean(CdaConversionService.class, "getFhirJsonFromCdaXMLString(${body})")
                        // Write Parsed FHIR Terminology Transactions to Topic
                        .to("direct:ccdafhirconversion")
                .endChoice();
        ;

        from("rest:post/idaas/hl7")
                .routeId(HL7Post_ROUTE_ID)
                .to("log:" + HL7Post_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:HL7PostTransactions")
                .to("kafka:{{idaas.hl7post.topic.name}}?brokers={{idaas.kafka.brokers}}");
                // Convert HL7 to FHIR
                .choice()
                    .when(simple("{{idaas.convert.HL7toFHIR}}"))
                        // Conversion
                        .bean(HL7ToFHIRConverter.class, "convert(${body})")
                        .to("direct:hl7fhirconversion")
                .endChoice();
                 .choice()
                    .when(simple("{{idaas.process.PublicCloud}}"))
                        .to("direct:publiccloud")
                .endChoice();
        ;

        /*
         *   HL7 EndPoints
         */
        // MLLP
        from("mllp:0.0.0.0:{{idaas.port.adt}}")
                .routeId(HL7ADT_ROUTE_ID)
                .to("log:" + HL7ADT_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:HL7PostTransactions")
                .to("kafka:{{idaas.hl7adt.topic.name}}?brokers={{idaas.kafka.brokers}}");
                .choice()
                // Process Terminologies
                .when(simple("{{idaas.process.Terminologies}}"))
                    .to("direct:terminologies")
                .endChoice();
                // Convert HL7 to FHIR
                .choice()
                    .when(simple("{{idaas.convert.HL7toFHIR}}"))
                        .bean(HL7ToFHIRConverter.class, "convert(${body})")
                        .to("direct:hl7fhirconversion")
                .endChoice();
                // Public Cloud
                .choice()
                    .when(simple("{{idaas.process.PublicCloud}}"))
                    .to("direct:publiccloud")
                .endChoice();
                // Process ACK
                // if we want to persist ACK generated by MLLP component


    }
}