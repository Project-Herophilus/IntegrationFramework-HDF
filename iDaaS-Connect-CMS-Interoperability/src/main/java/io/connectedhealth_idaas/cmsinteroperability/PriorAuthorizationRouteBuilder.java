package io.connectedhealth_idaas.cmsinteroperability;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

//@Component
public class PriorAuthorizationRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
        .handled(true)
        .log(LoggingLevel.ERROR,"${exception}")
        .to("micrometer:counter:prior_authorization_exception_handled")
        .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN_VALUE))
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
        .setBody(simple("${exception}"));

        rest("/priorAuthorization")
        .post()
        .consumes(MediaType.TEXT_PLAIN_VALUE)
        .produces(MediaType.TEXT_PLAIN_VALUE)
        .route()
        .routeId("priorAuthorization")
        .convertBodyTo(String.class)
        .log("Content received: ${body}")
        .to("kafka:Topic278?brokers={{idaas.kafka.brokers}}")
        .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN_VALUE))
        .setBody(constant("file published"))
        .to("micrometer:counter:files_278_received");

    }

}
