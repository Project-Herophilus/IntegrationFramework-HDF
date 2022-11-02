/*
 * Copyright 2019 Project-Herophilus
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
package io.connectedhealth_idaas.bluebutton;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

@Component
public class CmsInteropBlueButton extends RouteBuilder {
    //@Autowired

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

    @Override
    public void configure() throws Exception {

        /*
         * Rest Endpoint Implementation
         */
        restConfiguration()
                .component("netty-http")
                //.host(config.bluebuttoncallbackhostname)
                //.port(config.bluebuttoncallbackportnumber)
                .bindingMode(RestBindingMode.json);
        rest()
                .get("/bluebutton").to("direct:authorize");
                //.get("/" + config.bluebuttoncallbackpath).to("direct:callback");

        from("direct:authorize")
                .setHeader("Location", simple("https://sandbox.bluebutton.cms.gov/v1/o/authorize/?response_type=code&client_id={{idaas.bluebuttonclientid}}&redirect_uri=http://{{idaas.bluebuttoncallbackhostname}}:{{idaas.bluebuttoncallbackportnumber}}/{{idaas.bluebuttoncallbackpath}}&scope=patient/Patient.read patient/Coverage.read patient/ExplanationOfBenefit.read profile"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("302"));

        from("direct:callback")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        //String clientId = SimpleBuilder.simple(config.bluebuttonclientid).evaluate(exchange, String.class);
                        //String clientSecret = SimpleBuilder.simple(config.bluebuttonclientsecret).evaluate(exchange, String.class);
                        String code = exchange.getIn().getHeader("code", String.class);
                        String body = "code=" + code + "&grant_type=authorization_code";
                        //String auth = clientId + ":" + clientSecret;
                        //String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                        exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
                        //exchange.getIn().setHeader("Authorization", authHeader);
                        exchange.getIn().setHeader("Content-Type", "application/x-www-form-urlencoded");
                        exchange.getIn().setHeader("Content-Length", body.length());
                        exchange.getIn().setBody(body);
                    }
                })
                .to("https://sandbox.bluebutton.cms.gov/v1/o/token/?bridgeEndpoint=true")
                //.unmarshal(new JacksonDataFormat(ConfigProperties.class))
                .process(new Processor() {
                    @Override
                    public void process(final Exchange exchange) throws Exception {
                  //      final ConfigProperties payload = exchange.getIn().getBody(ConfigProperties.class);
                  //      exchange.getIn().setBody(payload.getAccess_token());
                    }
                })
                .removeHeader("*")
                .to("direct:start");

        from("direct:start")
                .setHeader("Authorization", simple("Bearer ${body}"))
                .transform().constant(null)
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                .to("https://sandbox.bluebutton.cms.gov/v1/connect/userinfo?bridgeEndpoint=true")
                .unmarshal(new JacksonDataFormat(Map.class))
                .process(new Processor() {
                    @Override
                    public void process(final Exchange exchange) throws Exception {
                        final Map payload = exchange.getIn().getBody(Map.class);
                        final String fhirId = payload.get("patient").toString();
                        exchange.getIn().setBody(fhirId);
                    }
                })
                .removeHeaders("*", "Authorization")
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                .multicast()
                .to("direct:patient", "direct:coverage", "direct:explanationOfBenefit")
                .transform().constant("Done");

        from("direct:kafka")
                .to("kafka:bluebutton?brokers=localhost:9092");

        from("direct:patient")
                .toD("https://sandbox.bluebutton.cms.gov/v1/fhir/Patient/${body}?bridgeEndpoint=true")
                .to("direct:kafka");

        from("direct:coverage")
                .toD("https://sandbox.bluebutton.cms.gov/v1/fhir/Coverage/?beneficiary=${body}&bridgeEndpoint=true")
                .to("direct:kafka");

        from("direct:explanationOfBenefit")
                .to("https://sandbox.bluebutton.cms.gov/v1/fhir/ExplanationOfBenefit?bridgeEndpoint=true")
                .to("direct:kafka");

    }

}