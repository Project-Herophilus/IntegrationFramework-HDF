package io.connectedhealth_idaas.thirdparty;

import org.apache.camel.Exchange;
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
public class ThirdPartyRouteBuilder extends RouteBuilder {
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

    public static final String IOT_ROUTE_ID = "iot-inbound";
    public static final String HL7_ROUTE_ID = "hl7-third-party";
    public static final String DQAAS_ROUTE_ID = "dqass-third-party";

    @Override
    public void configure() throws Exception {

/*
        onException(Exception.class)
                .log(LoggingLevel.ERROR,"${exception}")
                .to("micrometer:counter:sftp_exception_handled");

*/

        onException(Exception.class)
                .handled(true)
                .log(LoggingLevel.ERROR,"${exception}")
                .to("micrometer:counter:rest_exception_handled")
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN_VALUE))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(simple("${exception}"));

        from("sftp:{{sftp.host}}:{{sftp.port}}/{{sftp.hl7.dir}}?username={{sftp.username}}&password={{sftp.password}}&move={{sftp.dir.processed}}&moveFailed={{sftp.dir.error}}&include=^.*\\.(dat|hl7)$")
                .routeId(HL7_ROUTE_ID)
                .to("log:"+ HL7_ROUTE_ID + "?showAll=true")
                .to("kafka:SftpFiles?brokers={{idaas.kafka.brokers}}")
                .log("${exchangeId} fully processed")
                .to("micrometer:counter:num_processed_files");

        from("sftp:{{sftp.host}}:{{sftp.port}}/{{sftp.dqaas.dir}}?username={{sftp.username}}&password={{sftp.password}}&move={{sftp.dir.processed}}&moveFailed={{sftp.dir.error}}")
                .routeId(DQAAS_ROUTE_ID)
                .to("log:"+ DQAAS_ROUTE_ID + "?showAll=true")
                .to("sftp:{{sftp.host}}:{{sftp.port}}/{{sftp.ct.dir}}?username={{sftp.username}}&password={{sftp.password}}")
                .log("${exchangeId} fully processed")
                .to("micrometer:counter:num_processed_files");

        restConfiguration()
                .component("servlet");

        rest("/iot")
            .post()
                .produces(MediaType.TEXT_PLAIN_VALUE)
                .route()
                .routeId(IOT_ROUTE_ID)
                .to("log:"+ IOT_ROUTE_ID + "?showAll=true")
                .log("${exchangeId} fully processed")
                .to("micrometer:counter:iotEventReceived")
                .to("kafka:{{idaas.iot.integration.topic}}?brokers={{idaas.kafka.brokers}}")
        .endRest();

        rest("/file")
            .get()
                .produces(MediaType.TEXT_PLAIN_VALUE)
                .route()
                .routeId("FileList")
                .log("Request Received.")
                .to("aws-s3://public-idaas?accessKey={{aws.access.key}}&secretKey={{aws.secret.key}}&region={{aws.region}}&operation=listObjects")
                .bean(s3Bean,"list")
            .endRest()
            .get("/{file-name}")
                .produces(MediaType.APPLICATION_XML_VALUE)
                .route()
                .routeId("ExtractFile")
                .log("Request received for file ${header.file-name}.")
                .bean(s3Bean,"extract")
                .to("kafka:S3Files?brokers={{idaas.kafka.brokers}}")
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_XML_VALUE))
                .to("micrometer:counter:num_files_request")
        .end();

        rest("/db2")
            .get("/membership")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .route()
                .routeId("MembershipGetAll")
                .log("Request for MEMBERSHIP received.")
                .to("sql:select * from MEMBERSHIP")
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                .marshal().json(JsonLibrary.Jackson)
                .to("micrometer:counter:processed_membership_queries")
            .endRest()
            .get("/claims")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .route()
                .routeId("ClaimsGetAll")
                .log("Request for CLAIMS received.")
                .to("sql:select * from CLAIMS")
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                .marshal().json(JsonLibrary.Jackson)
                .to("micrometer:counter:processed_claims_queries")
        .end();

        /*
         *  Mandatory Reporting
         *  Sample: Topic to Postgres
         *
         */

        /*
        from(getKafkaTopicUri("MandatoryReporting")).unmarshal(new JacksonDataFormat(ReportingOutput.class))
        .process(new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
            final ReportingOutput payload = exchange.getIn().getBody(ReportingOutput.class);
           }
        })
        .routeId("DBProcessing-MandatoryReporting")
        .log(LoggingLevel.INFO, log, "Transaction Message: [${body}]")
        .to("sql:insert into etl_mandatoryreporting (organizationid,patientaccountnumber, patientlastname, patientfirstname, zipcode, roombed, " +
            "age, gender, admissiondate) values( :#${body.organizationId},:#${body.patientAccount},:#${body.patientLastName}," +
            ":#${body.patientFirstName},:#${body.zipCode},:#${body.roomBed},:#${body.age},:#${body.gender},:#${body.admissionDate})");

        from("file:{{covid.reporting.directory}}/")
                .routeId("FileProcessing-CovidReporting")
                .choice()
                .when(simple("${file:ext} == 'csv'"))
                //.when(simple("${file:ext} == ${covid.reporting.extension}"))
                .split(body().tokenize("\n")).streaming()
                .unmarshal(new BindyCsvDataFormat(CovidJohnHopkinsUSDailyData.class))
                //.marshal(new JacksonDataFormat(CovidJohnHopkinsUSDailyData.class))
                .to("kafka:CovidDailyData?brokers={{idaas.kafka.brokers}}")
                .endChoice();
         */
    }
}
