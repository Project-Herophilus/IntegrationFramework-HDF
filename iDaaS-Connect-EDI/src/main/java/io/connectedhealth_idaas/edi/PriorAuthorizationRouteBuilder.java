package io.connectedhealth_idaas.edi;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component
public class PriorAuthorizationRouteBuilder extends RouteBuilder {

    @Autowired
    private S3Bean s3Bean;

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
        .handled(true)
        .log(LoggingLevel.ERROR,"${exception}")
        .to("micrometer:counter:prior_authorization_exception_handled");

        from("kafka:Topic278?brokers={{idaas.kafka.brokers}}&groupId=hl7-278&autoOffsetReset=earliest")
        .log("Content received: ${body}")
        .setHeader("file-name", constant("{{s3.file275.key}}"))
        .bean(s3Bean,"extract")
        .log("275 Content: ${body}")
        .to("kafka:Topic275?brokers={{idaas.kafka.brokers}}")
        .to("micrometer:counter:files_converted");

    }
}
