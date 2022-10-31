package io.connectedhealth_idaas.hl7;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
// HL7 to FHIR Conversion
import io.github.linuxforhealth.hl7.HL7ToFHIRConverter;
// CCDA to FHIR Conversion
import io.connectedhealth_idaas.eventbuilder.converters.ccda.CdaConversionService;

@Component
public class Hl7RouteBuilder extends RouteBuilder {

    @Autowired
    //private S3Bean s3Bean;

    public static final String Terminology_ROUTE_ID = "terminologies-direct";
    public static final String Deidentification_ROUTE_ID = "deidentification-direct";
    public static final String Empi_ROUTE_ID = "empi-direct";
    public static final String CCDAConversion_ROUTE_ID = "ccdaconversion-direct";
    public static final String HL7Conversion_ROUTE_ID = "hl7conversion-direct";
    public static final String PublicCloud_ROUTE_ID = "publiccloud-direct";
    public static final String ProcessACKs_ROUTE_ID = "processacks-direct";
    public static final String Sdoh_ROUTE_ID = "sdoh-direct";
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
            .choice()
                .when(simple("{{idaas.process.Terminologies}}"))
                    //.routeId("iDaaS-Terminologies")
                    //.convertBodyTo(String.class).to("kafka:{{idaas.terminologyTopic}}?brokers={{idaas.kafkaBrokers}}");
                    .routeId(Terminology_ROUTE_ID)
                    .to("log:" + Terminology_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("micrometer:counter:terminologyTransactions")
                    .to("kafka:{{idaas.terminology.topic.name}}?brokers={{idaas.kafka.brokers}}")
            .endChoice();

        from("direct:ccdafhirconversion")
                .routeId(CCDAConversion_ROUTE_ID)
                .to("log:" + CCDAConversion_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:ccdaConversionTransactions")
                // Invocation of CCDA Conversion
                // Unmarshall from XML Doc against XSD - or Bean to encapsulate features
                .bean(CdaConversionService.class, "getFhirJsonFromCdaXMLString(${body})")
                .to("kafka:{{idaas.ccdaconversion.topic.name}}?brokers={{idaas.kafka.brokers}}");

        from("direct:deidentification")
            .choice()
                .when(simple("{{idaas.process.Deidentification}}"))
                    .routeId(Deidentification_ROUTE_ID)
                    .to("log:" + Deidentification_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("micrometer:counter:deidentificationTransactions")
                    .to("kafka:{{idaas.deidentification.topic.name}}?brokers={{idaas.kafka.brokers}}")
                    // to the deidentification API
            .endChoice();

        from("direct:empi")
            .choice()
                .when(simple("{{idaas.process.Empi}}"))
                    .routeId(Empi_ROUTE_ID)
                    .to("log:" + Empi_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("micrometer:counter:deidentificationTransactions")
                    .to("kafka:{{idaas.deidentification.topic.name}}?brokers={{idaas.kafka.brokers}}")
                    // to the empi API
            .endChoice();

        from("direct:hl7fhirconversion")
             .choice()
                .when(simple("{{idaas.convert.HL7toFHIR}}"))
                    .routeId(HL7Conversion_ROUTE_ID)
                    .bean(HL7ToFHIRConverter.class, "convert(${body})")
                    .to("log:" + HL7Conversion_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("micrometer:counter:hl7ConversionTransactions")
                    // Conversion
                    .bean(HL7ToFHIRConverter.class, "convert(${body})")
                    .to("kafka:{{idaas.hl7conversion.topic.name}}?brokers={{idaas.kafka.brokers}}")
            .endChoice();

        from("direct:publiccloud")
             .choice()
                .when(simple("{{idaas.process.PublicCloud}}"))
                    .routeId(PublicCloud_ROUTE_ID)
                    .to("log:" + PublicCloud_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("micrometer:counter:publiccloudTransactions")
                    .to("kafka:{{idaas.publiccloud.topic.name}}?brokers={{idaas.kafka.brokers}}")
             .endChoice();

        from("direct:processacks")
                .choice()
                .when(simple("{{idaas.process.Acks}}"))
                    .routeId(ProcessACKs_ROUTE_ID)
                    .to("log:" + ProcessACKs_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("kafka:{{idaas.processacks.topic.name}}?brokers={{idaas.kafka.brokers}}")
             .endChoice();

        from("direct:sdoh")
            .choice()
                .when(simple("{{idaas.process.Sdoh}}"))
                    .routeId(Sdoh_ROUTE_ID)
                    .to("log:" + Sdoh_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("micrometer:counter:sdohTransactions")
                    .to("kafka:{{idaas.sdoh.topic.name}}?brokers={{idaas.kafka.brokers}}")
            .endChoice();

        /*
         *   Rest EndPoints
         */
        from("rest:post/idaas/ccda")
                .routeId(CCDAPost_ROUTE_ID)
                .to("log:" + CCDAPost_ROUTE_ID + "?showAll=true")
                .log("${exchangeId} fully processed")
                .to("micrometer:counter:ccdaPostedTransactions")
                .to("kafka:{{idaas.ccdapost.topic.name}}?brokers={{idaas.kafka.brokers}}")
                .multicast().parallelProcessing()
                    // Process Terminologies
                    .to("direct:terminologies")
                    // Convert CCDA to FHIR
                    .to("direct:ccdafhirconversion")
                    // Deidentification
                    .to("direct:deidentification")
                    // EMPI
                    .to("direct:empi")
                    // Public Cloud
                    .to("direct:publiccloud")
                    //SDOH
                    .to("direct:sdoh")
                .end();

        from("rest:post/idaas/hl7")
                .routeId(HL7Post_ROUTE_ID)
                .to("log:" + HL7Post_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:HL7PostTransactions")
                .to("kafka:{{idaas.hl7post.topic.name}}?brokers={{idaas.kafka.brokers}}")
                .multicast().parallelProcessing()
                    // Process Terminologies
                    .to("direct:terminologies")
                    // Convert HL7 to FHIR
                    .to("direct:hl7fhirconversion")
                    // Deidentification
                    .to("direct:deidentification")
                    // EMPI
                    .to("direct:empi")
                    // Public Cloud
                    .to("direct:publiccloud")
                    //SDOH
                    .to("direct:sdoh")
                .end();


        /*
         *   HL7 EndPoints - MLLP Protocol
         */
        // ADT
        from("mllp:0.0.0.0:{{idaas.port.adt}}")
                .routeId(HL7ADT_ROUTE_ID)
                .to("log:" + HL7ADT_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:HL7PostTransactions")
                .to("kafka:{{idaas.hl7adt.topic.name}}?brokers={{idaas.kafka.brokers}}")
                //.to("fluentd:")
                // This is to ensure that processes can run independently and if they transform data
                // it will not mess with any other processes
                .multicast().parallelProcessing()
                    // Process Terminologies
                    .to("direct:terminologies")
                    // Convert HL7 to FHIR
                    .to("direct:hl7fhirconversion")
                    // Deidentification
                    .to("direct:deidentification")
                    // EMPI
                    .to("direct:empi")
                    // Public Cloud
                    .to("direct:publiccloud")
                    //SDOH
                    .to("direct:sdoh")
                    //ACKs
                    // if we want to persist ACK generated by MLLP component
                    .to("direct:processacks")
                .end();

    }
}