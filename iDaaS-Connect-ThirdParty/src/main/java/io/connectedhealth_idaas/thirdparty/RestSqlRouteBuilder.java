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

@Component
public class RestSqlRouteBuilder extends RouteBuilder {

    @Bean
    ServletRegistrationBean camelServlet() {
        // use a @Bean to register the Camel servlet which we need to do
        // because we want to use the camel-servlet component for the Camel REST service
        ServletRegistrationBean mapping = new ServletRegistrationBean();
        mapping.setName("CamelServlet");
        mapping.setLoadOnStartup(1);
        mapping.setServlet(new CamelHttpTransportServlet());
        mapping.addUrlMappings("/idaas/db2/*");
        return mapping;
    }

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
        .handled(true)
        .log(LoggingLevel.ERROR,"${exception}")
        .to("micrometer:counter:num_exception_handled")
        .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.TEXT_PLAIN_VALUE))
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
        .setBody(simple("${exception}"));

        restConfiguration()
        .component("servlet");

        rest()
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

    }
}
