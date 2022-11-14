package io.connectedhealth_idaas.hl7;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.http.MediaType;
// HL7 to FHIR Conversion
import io.github.linuxforhealth.hl7.HL7ToFHIRConverter;
// CCDA to FHIR Conversion
import io.connectedhealth_idaas.eventbuilder.converters.ccda.CdaConversionService;

@Component
public class Hl7RouteBuilder extends RouteBuilder {

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

    @Autowired
    private S3Bean s3Bean;

    // Public Variables
    public static final String TERMINOLOGY_ROUTE_ID = "terminologies-direct";
    public static final String DEIDENTIFICATION_ROUTE_ID = "deidentification-direct";
    public static final String EMPI_ROUTE_ID = "empi-direct";
    public static final String CCDACONVERSION_ROUTE_ID = "ccdaconversion-direct";
    public static final String DATATIER_ROUTE_ID = "datatier-direct";
    public static final String HEDA_ROUTE_ID = "heda-direct";
    public static final String HL7CCONVERSION_ROUTE_ID = "hl7conversion-direct";
    public static final String PUBLICCLOUD_ROUTE_ID = "publiccloud-direct";
    public static final String PROCESSACKS_ROUTE_ID = "processacks-direct";
    public static final String SDOH_ROUTE_ID = "sdoh-direct";
    public static final String CCDAPOST_ROUTE_ID = "ccda-post-inbound";
    public static final String HL7POST_ROUTE_ID = "hl7-post-inbound";
    public static final String HL7ADT_ROUTE_ID = "hl7-adt-inbound";
    public static final String HL7MDM_ROUTE_ID = "hl7-mdm-inbound";
    public static final String HL7MFN_ROUTE_ID = "hl7-mfn-inbound";
    public static final String HL7ORM_ROUTE_ID = "hl7-orm-inbound";
    public static final String HL7ORU_ROUTE_ID = "hl7-oru-inbound";
    public static final String HL7RDE_ROUTE_ID = "hl7-rde-inbound";
    public static final String HL7SCH_ROUTE_ID = "hl7-sch-inbound";
    public static final String HL7VXU_ROUTE_ID = "hl7-vxu-inbound";

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .handled(true)
                .log(LoggingLevel.ERROR,"${exception}")
                .to("micrometer:counter:hl7_exception_handled");
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

        from("direct:ccdafhirconversion")
                .choice()
                .when(simple("{{idaas.convert.CCDAtoFHIR}}"))
                    .routeId(CCDACONVERSION_ROUTE_ID)
                    //.to("log:" + CCDACONVERSION_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("micrometer:counter:ccdaConversionTransactions")
                    // Invocation of CCDA Conversion
                    // Unmarshall from XML Doc against XSD - or Bean to encapsulate features
                    .bean(CdaConversionService.class, "getFhirJsonFromCdaXMLString(${body})")
                    .to("kafka:{{idaas.ccdaconversion.topic.name}}?brokers={{idaas.kafka.brokers}}")
                    // Adding support for sending CCDA Documents to other processes
                    //.to("direct:datatier")
                    //.to("direct:publiccloud");
                    .to("micrometer:counter:datatierTransactions")
                    .to("kafka:{{idaas.datatier.topic.name}}?brokers={{idaas.kafka.brokers}}")
                    .to("micrometer:counter:publiccloudTransactions")
                    .to("kafka:{{idaas.publiccloud.topic.name}}?brokers={{idaas.kafka.brokers}}")
                .endChoice();

        from("direct:datatier")
                .choice()
                .when(simple("{{idaas.process.DataTier}}"))
                    .routeId(DATATIER_ROUTE_ID)
                    //.to("log:" + DATATIER_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("micrometer:counter:datatierTransactions")
                    .to("kafka:{{idaas.datatier.topic.name}}?brokers={{idaas.kafka.brokers}}")
                    // to the deidentification API
                .endChoice();

        from("direct:deidentification")
            .choice()
                .when(simple("{{idaas.process.Deidentification}}"))
                    .routeId(DEIDENTIFICATION_ROUTE_ID)
                    //.to("log:" + DEIDENTIFICATION_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("micrometer:counter:deidentificationTransactions")
                    .to("kafka:{{idaas.deidentification.topic.name}}?brokers={{idaas.kafka.brokers}}")
                    // to the deidentification API
            .endChoice();

        from("direct:empi")
            .choice()
                .when(simple("{{idaas.process.Empi}}"))
                    .routeId(EMPI_ROUTE_ID)
                    //.to("log:" + EMPI_ROUTE_ID + "?showAll=true")
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

        from("direct:hl7fhirconversion")
             .choice()
                .when(simple("{{idaas.convert.HL7toFHIR}}"))
                    .routeId(HL7CCONVERSION_ROUTE_ID)
                    .bean(HL7ToFHIRConverter.class, "convert(${body})")
                    //.to("log:" + HL7CCONVERSION_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("micrometer:counter:hl7ConversionTransactions")
                    // Conversion
                    .bean(HL7ToFHIRConverter.class, "convert(${body})")
                    .to("kafka:{{idaas.hl7conversion.topic.name}}?brokers={{idaas.kafka.brokers}}")
                    // Adding support for sending FHIR Resources to other processes
                    .to("micrometer:counter:datatierTransactions")
                    .to("direct:datatier")
                    //.to("kafka:{{idaas.datatier.topic.name}}?brokers={{idaas.kafka.brokers}}")
                    .to("micrometer:counter:publiccloudTransactions")
                    .to("direct:publiccloud")
                    //.to("kafka:{{idaas.publiccloud.topic.name}}?brokers={{idaas.kafka.brokers}}")
            .endChoice();

        from("direct:publiccloud")
             .choice()
                .when(simple("{{idaas.process.PublicCloud}}"))
                    .routeId(PUBLICCLOUD_ROUTE_ID)
                    //.to("log:" + PUBLICCLOUD_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("micrometer:counter:publiccloudTransactions")
                    .to("kafka:{{idaas.publiccloud.topic.name}}?brokers={{idaas.kafka.brokers}}")
             .endChoice();

        from("direct:processacks")
                .choice()
                .when(simple("{{idaas.process.Acks}}"))
                    .routeId(PROCESSACKS_ROUTE_ID)
                    //.to("log:" + PROCESSACKS_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("kafka:{{idaas.processacks.topic.name}}?brokers={{idaas.kafka.brokers}}")
             .endChoice();

        from("direct:sdoh")
            .choice()
                .when(simple("{{idaas.process.Sdoh}}"))
                    .routeId(SDOH_ROUTE_ID)
                    //.to("log:" + SDOH_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("micrometer:counter:sdohTransactions")
                    .to("kafka:{{idaas.sdoh.topic.name}}?brokers={{idaas.kafka.brokers}}")
            .endChoice();

        /*
         *   Rest EndPoints
         */
        restConfiguration()
                .component("servlet");

        rest("/ccda")
            .post()
                .produces(MediaType.TEXT_PLAIN_VALUE)
                .route()
                    .routeId(CCDAPOST_ROUTE_ID)
                    .to("log:" + CCDAPOST_ROUTE_ID + "?showAll=true")
                    .log("${exchangeId} fully processed")
                    .to("micrometer:counter:ccdaPostedTransactions")
                    .to("kafka:{{idaas.ccdapost.topic.name}}?brokers={{idaas.kafka.brokers}}")
                    .multicast().parallelProcessing()
                        // Process Terminologies
                        .to("direct:terminologies")
                        // Convert CCDA to FHIR
                        .to("direct:ccdafhirconversion")
                        // Data Tier
                        .to("direct:datatier")
                        // Deidentification
                        .to("direct:deidentification")
                        // EMPI
                        .to("direct:empi")
                        // HEDA
                        .to("direct:heda")
                        // Public Cloud
                        .to("direct:publiccloud")
                        //SDOH
                        .to("direct:sdoh")
                    .end()
                    //.outputType('CCDA FHIR Conversion Completed');
        .endRest();

        rest("/hl7")
            .post()
                .produces(MediaType.TEXT_PLAIN_VALUE)
                .route()
                    .routeId(HL7POST_ROUTE_ID)
                    .to("log:" + HL7POST_ROUTE_ID + "?showAll=true")
                    //.log("${exchangeId} fully processed")
                    .to("micrometer:counter:HL7PostTransactions")
                    .to("kafka:{{idaas.hl7post.topic.name}}?brokers={{idaas.kafka.brokers}}")
                    .multicast().parallelProcessing()
                    // Process Terminologies
                    .to("direct:terminologies")
                    // Convert HL7 to FHIR
                    .to("direct:hl7fhirconversion")
                    // Data Tier
                    .to("direct:datatier")
                    // Deidentification
                    .to("direct:deidentification")
                    // EMPI
                    .to("direct:empi")
                    // HEDA
                    .to("direct:heda")
                    // Public Cloud
                    .to("direct:publiccloud")
                    //SDOH
                    .to("direct:sdoh")
        .endRest();

        /*
         *   HL7 EndPoints - MLLP Protocol
         */
        // ADT
        from("mllp:0.0.0.0:{{idaas.port.adt}}")
                .routeId(HL7ADT_ROUTE_ID)
                .to("log:" + HL7ADT_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:HL7-ADT-PostTransactions")
                .to("kafka:{{idaas.hl7adt.topic.name}}?brokers={{idaas.kafka.brokers}}")
                //.to("fluentd:")
                // This is to ensure that processes can run independently and if they transform data
                // it will not mess with any other processes
                .multicast().parallelProcessing()
                    // Process Terminologies
                    .to("direct:terminologies")
                    // Convert HL7 to FHIR
                    .to("direct:hl7fhirconversion")
                    // Data Tier
                    .to("direct:datatier")
                    // Deidentification
                    .to("direct:deidentification")
                    // EMPI
                    .to("direct:empi")
                    // HEDA
                    .to("direct:heda")
                    // Public Cloud
                    .to("direct:publiccloud")
                    //SDOH
                    .to("direct:sdoh")
                    //ACKs
                    // if we want to persist ACK generated by MLLP component
                    .to("direct:processacks")
                .end();

        from("mllp:0.0.0.0:{{idaas.port.mdm}}")
                .routeId(HL7MDM_ROUTE_ID)
                .to("log:" + HL7MDM_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:HL7-MDM-PostTransactions")
                .to("kafka:{{idaas.hl7mdm.topic.name}}?brokers={{idaas.kafka.brokers}}")
                //.to("fluentd:")
                // This is to ensure that processes can run independently and if they transform data
                // it will not mess with any other processes
                .multicast().parallelProcessing()
                // Process Terminologies
                .to("direct:terminologies")
                // Convert HL7 to FHIR
                .to("direct:hl7fhirconversion")
                // Data Tier
                .to("direct:datatier")
                // Deidentification
                .to("direct:deidentification")
                // EMPI
                .to("direct:empi")
                // HEDA
                .to("direct:heda")
                // Public Cloud
                .to("direct:publiccloud")
                //SDOH
                .to("direct:sdoh")
                //ACKs
                // if we want to persist ACK generated by MLLP component
                .to("direct:processacks")
        .end();

        from("mllp:0.0.0.0:{{idaas.port.mfn}}")
                .routeId(HL7MFN_ROUTE_ID)
                .to("log:" + HL7MFN_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:HL7-MFN-PostTransactions")
                .to("kafka:{{idaas.hl7mfn.topic.name}}?brokers={{idaas.kafka.brokers}}")
                //.to("fluentd:")
                // This is to ensure that processes can run independently and if they transform data
                // it will not mess with any other processes
                .multicast().parallelProcessing()
                // Process Terminologies
                .to("direct:terminologies")
                // Convert HL7 to FHIR
                .to("direct:hl7fhirconversion")
                // Data Tier
                .to("direct:datatier")
                // Deidentification
                .to("direct:deidentification")
                // EMPI
                .to("direct:empi")
                // HEDA
                .to("direct:heda")
                // Public Cloud
                .to("direct:publiccloud")
                //SDOH
                .to("direct:sdoh")
                //ACKs
                // if we want to persist ACK generated by MLLP component
                .to("direct:processacks")
        .end();

        from("mllp:0.0.0.0:{{idaas.port.orm}}")
                .routeId(HL7ORM_ROUTE_ID)
                .to("log:" + HL7ORM_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:HL7-ORM-PostTransactions")
                .to("kafka:{{idaas.hl7orm.topic.name}}?brokers={{idaas.kafka.brokers}}")
                //.to("fluentd:")
                // This is to ensure that processes can run independently and if they transform data
                // it will not mess with any other processes
                .multicast().parallelProcessing()
                // Process Terminologies
                .to("direct:terminologies")
                // Convert HL7 to FHIR
                .to("direct:hl7fhirconversion")
                // Data Tier
                .to("direct:datatier")
                // Deidentification
                .to("direct:deidentification")
                // EMPI
                .to("direct:empi")
                // HEDA
                .to("direct:heda")
                // Public Cloud
                .to("direct:publiccloud")
                //SDOH
                .to("direct:sdoh")
                //ACKs
                // if we want to persist ACK generated by MLLP component
                .to("direct:processacks")
        .end();

        from("mllp:0.0.0.0:{{idaas.port.oru}}")
                .routeId(HL7ORU_ROUTE_ID)
                .to("log:" + HL7ORU_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:HL7-ORU-PostTransactions")
                .to("kafka:{{idaas.hl7oru.topic.name}}?brokers={{idaas.kafka.brokers}}")
                //.to("fluentd:")
                // This is to ensure that processes can run independently and if they transform data
                // it will not mess with any other processes
                .multicast().parallelProcessing()
                // Process Terminologies
                .to("direct:terminologies")
                // Convert HL7 to FHIR
                .to("direct:hl7fhirconversion")
                // Data Tier
                .to("direct:datatier")
                // Deidentification
                .to("direct:deidentification")
                // EMPI
                .to("direct:empi")
                // HEDA
                .to("direct:heda")
                // Public Cloud
                .to("direct:publiccloud")
                //SDOH
                .to("direct:sdoh")
                //ACKs
                // if we want to persist ACK generated by MLLP component
                .to("direct:processacks")
        .end();

        from("mllp:0.0.0.0:{{idaas.port.rde}}")
                .routeId(HL7RDE_ROUTE_ID)
                .to("log:" + HL7RDE_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:HL7-RDE-PostTransactions")
                .to("kafka:{{idaas.hl7rde.topic.name}}?brokers={{idaas.kafka.brokers}}")
                //.to("fluentd:")
                // This is to ensure that processes can run independently and if they transform data
                // it will not mess with any other processes
                .multicast().parallelProcessing()
                // Process Terminologies
                .to("direct:terminologies")
                // Convert HL7 to FHIR
                .to("direct:hl7fhirconversion")
                // Data Tier
                .to("direct:datatier")
                // Deidentification
                .to("direct:deidentification")
                // EMPI
                .to("direct:empi")
                // HEDA
                .to("direct:heda")
                // Public Cloud
                .to("direct:publiccloud")
                //SDOH
                .to("direct:sdoh")
                //ACKs
                // if we want to persist ACK generated by MLLP component
                .to("direct:processacks")
        .end();

        from("mllp:0.0.0.0:{{idaas.port.sch}}")
                .routeId(HL7SCH_ROUTE_ID)
                .to("log:" + HL7SCH_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:HL7-SCH-PostTransactions")
                .to("kafka:{{idaas.hl7sch.topic.name}}?brokers={{idaas.kafka.brokers}}")
                //.to("fluentd:")
                // This is to ensure that processes can run independently and if they transform data
                // it will not mess with any other processes
                .multicast().parallelProcessing()
                // Process Terminologies
                .to("direct:terminologies")
                // Convert HL7 to FHIR
                .to("direct:hl7fhirconversion")
                // Data Tier
                .to("direct:datatier")
                // Deidentification
                .to("direct:deidentification")
                // EMPI
                .to("direct:empi")
                // HEDA
                .to("direct:heda")
                // Public Cloud
                .to("direct:publiccloud")
                //SDOH
                .to("direct:sdoh")
                //ACKs
                // if we want to persist ACK generated by MLLP component
                .to("direct:processacks")
        .end();

        from("mllp:0.0.0.0:{{idaas.port.vxu}}")
                .routeId(HL7VXU_ROUTE_ID)
                .to("log:" + HL7VXU_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:HL7-VXU-PostTransactions")
                .to("kafka:{{idaas.hl7vxu.topic.name}}?brokers={{idaas.kafka.brokers}}")
                //.to("fluentd:")
                // This is to ensure that processes can run independently and if they transform data
                // it will not mess with any other processes
                .multicast().parallelProcessing()
                // Process Terminologies
                .to("direct:terminologies")
                // Convert HL7 to FHIR
                .to("direct:hl7fhirconversion")
                // Data Tier
                .to("direct:datatier")
                // Deidentification
                .to("direct:deidentification")
                // EMPI
                .to("direct:empi")
                // HEDA
                .to("direct:heda")
                // Public Cloud
                .to("direct:publiccloud")
                //SDOH
                .to("direct:sdoh")
                //ACKs
                // if we want to persist ACK generated by MLLP component
                .to("direct:processacks")
        .end();
          /*from("rest:post/idaas/hl7")
                .routeId(HL7POST_ROUTE_ID)
                .to("log:" + HL7POST_ROUTE_ID + "?showAll=true")
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
                .end();*/

    }
}