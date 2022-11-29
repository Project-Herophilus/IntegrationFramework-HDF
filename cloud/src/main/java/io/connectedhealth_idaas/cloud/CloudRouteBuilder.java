package io.connectedhealth_idaas.cloud;

import io.connectedhealth_idaas.eventbuilder.converters.ccda.CdaConversionService;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class CloudRouteBuilder extends RouteBuilder {
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
    public static final String DATATIER_ROUTE_ID = "datatier-direct";
    public static final String HEDA_ROUTE_ID = "heda-direct";
    public static final String PUBLICCLOUD_ROUTE_ID = "publiccloud-direct";
    public static final String SDOH_ROUTE_ID = "sdoh-direct";

    public static final String IOT_ROUTE_ID = "iot-inbound";
    public static final String HL7_ROUTE_ID = "hl7-third-party";
    public static final String DQAAS_ROUTE_ID = "dqass-third-party";

    public static final String AWSS3_ROUTE_ID = "aws-s3-inbound";
    public static final String AWSMQQUEUE_ROUTE_ID = "aws-mqqueue-inbound";
    public static final String AWSMQTOPIC_ROUTE_ID = "aws-mqtopic-inbound";
    public static final String AWSSQS_ROUTE_ID = "aws-sqs-inbound";
    public static final String AWSKINESIS_ROUTE_ID = "aws-kinesis-inbound";

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .handled(true)
                .log(LoggingLevel.ERROR,"${exception}")
                .to("micrometer:counter:Cloud_Exception")
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN_VALUE))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(simple("${exception}"));
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
                .to("micrometer:counter:Terminology_Inbd_ProcessedEvent")
                .to("kafka:{{idaas.terminology.topic.name}}?brokers={{idaas.kafka.brokers}}")
                .endChoice();

        from("direct:datatier")
                .choice()
                .when(simple("{{idaas.process.DataTier}}"))
                .routeId(DATATIER_ROUTE_ID)
                .to("log:" + DATATIER_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:DataTier_Inbd_ProcessedEvent")
                .to("kafka:{{idaas.datatier.topic.name}}?brokers={{idaas.kafka.brokers}}")
                // to the deidentification API
                .endChoice();

        from("direct:deidentification")
                .choice()
                .when(simple("{{idaas.process.Deidentification}}"))
                .routeId(DEIDENTIFICATION_ROUTE_ID)
                .to("log:" + DEIDENTIFICATION_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:Deidentification_Inbd_ProcessedEvent")
                .to("kafka:{{idaas.deidentification.topic.name}}?brokers={{idaas.kafka.brokers}}")
                // to the deidentification API
                .endChoice();

        from("direct:empi")
                .choice()
                .when(simple("{{idaas.process.Empi}}"))
                .routeId(EMPI_ROUTE_ID)
                .to("log:" + EMPI_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:EMPI_Inbd_ProcessedEvent")
                .to("kafka:{{idaas.deidentification.topic.name}}?brokers={{idaas.kafka.brokers}}")
                // to the empi API
                .endChoice();

        from("direct:heda")
                .choice()
                .when(simple("{{idaas.process.HEDA}}"))
                .routeId(HEDA_ROUTE_ID)
                .to("log:" + HEDA_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:HEDA_Inbd_ProcessedEvent")
                .to("kafka:{{idaas.heda.topic.name}}?brokers={{idaas.kafka.brokers}}")
                .endChoice();

        from("direct:publiccloud")
                .choice()
                .when(simple("{{idaas.process.PublicCloud}}"))
                .routeId(PUBLICCLOUD_ROUTE_ID)
                .to("log:" + PUBLICCLOUD_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:PublicCloud_Inbd_ProcessedEvent")
                .to("kafka:{{idaas.publiccloud.topic.name}}?brokers={{idaas.kafka.brokers}}")
                .endChoice();

        from("direct:sdoh")
                .choice()
                .when(simple("{{idaas.process.Sdoh}}"))
                .routeId(SDOH_ROUTE_ID)
                .to("log:" + SDOH_ROUTE_ID + "?showAll=true")
                //.log("${exchangeId} fully processed")
                .to("micrometer:counter:SDOH_Inbd_ProcessedEvent")
                .to("kafka:{{idaas.sdoh.topic.name}}?brokers={{idaas.kafka.brokers}}")
                .endChoice();

        // Routes
        restConfiguration()
                .component("servlet");

        rest("/s3-file")
                .get("/list-objects")
                .produces(MediaType.TEXT_PLAIN_VALUE)
                .route()
                .routeId("s3-ObjectList")
                .log("Request Received.")
                .to("aws-s3://public-idaas?accessKey={{aws.access.key}}&secretKey={{aws.secret.key}}&region={{aws.region}}&operation=listObjects")
                .bean(s3Bean,"list")
                .endRest()
                .get("/list-buckets")
                .produces(MediaType.TEXT_PLAIN_VALUE)
                .route()
                .routeId("s3-ListBuckets")
                .log("Request Received.")
                .to("aws-s3://public-idaas?accessKey={{aws.access.key}}&secretKey={{aws.secret.key}}&region={{aws.region}}&operation=listBuckets")
                .bean(s3Bean,"list")
                .endRest()
                .get("/delete-buckets")
                .produces(MediaType.TEXT_PLAIN_VALUE)
                .route()
                .routeId("s3-DeleteBuckets")
                .log("Request Received.")
                .to("aws-s3://public-idaas?accessKey={{aws.access.key}}&secretKey={{aws.secret.key}}&region={{aws.region}}&operation=deleteBucket")
                .bean(s3Bean,"list")
                .endRest()
                .get("/delete-objects")
                .produces(MediaType.TEXT_PLAIN_VALUE)
                .route()
                .routeId("s3-DeleteObjects")
                .log("Request Received.")
                .to("aws-s3://public-idaas?accessKey={{aws.access.key}}&secretKey={{aws.secret.key}}&region={{aws.region}}&operation=deleteObject")
                .bean(s3Bean,"list")
                .endRest()
                .get("/{objectname}")
                .produces(MediaType.APPLICATION_XML_VALUE)
                .route()
                .routeId("ExtractFile")
                .log("Request received for file ${header.file-name}.")
                .bean(s3Bean,"extract")
                .to("kafka:S3Files?brokers={{idaas.kafka.brokers}}")
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_XML_VALUE))
                .to("micrometer:counter:num_objects_request")
                .end();

        //https://camel.apache.org/components/3.18.x/aws2-sqs-component.html
        /*
        // Need to add correct aws2-sqs reference into master pom.xml with version and then in specific pom just reference
        /
        from("aws2-sqs://camel-1?accessKey={{aws.access.key}}&secretKey={{aws.secret.key}}&region={{aws.region}}")
        .choice().when(simple("{{idaas.aws.SQS}}"))
                // SQS Specifics
                .routeId(AWSSQS_ROUTE_ID)
                .to("log:" + AWSSQS_ROUTE_ID + "?showAll=true")
                .log("${exchangeId} fully processed")
                .to("micrometer:counter:awsSQSProcess_Inbd_ProcessedEvent")
                .to("kafka:{{idaas.awssqs.topic.name}}?brokers={{idaas.kafka.brokers}}")
       .endChoice();
        /*
        /*
        // Kinesis
        // .choice().when(simple("{{idaas.aws.Kinesis}}"))
        // Kinesis
            .setHeader(KinesisConstants.PARTITION_KEY,simple("Shard1"))
                //.to(getAWSConfig("aws2-kinesis://testhealthcarekinesisstream?"))
                .from("aws2-kinesis://testhealthcarekinesisstream?exchangePattern=OutOnly")
                .endChoice()
        */
    }
}
