package io.connectedhealth_idaas.hl7;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class RestFileRouteBuilder extends RouteBuilder {

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

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
        .handled(true)
        .log(LoggingLevel.ERROR,"${exception}")
        .to("micrometer:counter:num_exception_handled");

        restConfiguration()
        .component("servlet");

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
                .log("Request Received.")
                .bean(s3Bean,"extract")
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_XML_VALUE))
                .to("micrometer:counter:num_files_request")
            .end();

    }

}
