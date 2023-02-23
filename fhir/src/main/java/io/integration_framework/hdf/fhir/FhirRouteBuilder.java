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
 package io.integration_framework.hdf.fhir;

 import io.connectedhealth_idaas.eventbuilder.converters.ccda.CdaConversionService;
 import org.apache.camel.Exchange;
 import org.apache.camel.LoggingLevel;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.component.servlet.CamelHttpTransportServlet;
 import org.springframework.boot.web.servlet.ServletRegistrationBean;
 import org.springframework.context.annotation.Bean;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Component;

 @Component
 public class FhirRouteBuilder extends RouteBuilder {
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
     public static final String TERMINOLOGY_ROUTE_ID = "terminologies-direct";
     public static final String DATATIER_ROUTE_ID = "datatier-direct";
     public static final String DEIDENTIFICATION_ROUTE_ID = "deidentification-direct";
     public static final String EMPI_ROUTE_ID = "empi-direct";
     public static final String HEDA_ROUTE_ID = "heda-direct";
     public static final String PUBLICCLOUD_ROUTE_ID = "publiccloud-direct";
     public static final String SDOH_ROUTE_ID = "sdoh-direct";
     @Override
     public void configure() throws Exception {

         onException(Exception.class)
                 .handled(true)
                 .log(LoggingLevel.ERROR,"${exception}")
                 .to("micrometer:counter:fhir_Exception");

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
                     .log("${exchangeId} fully processed")
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

         // Send to FHIR Server
         from("direct:fhirmessaging")
                 .routeId("FHIRMessaging")
                 // we should test before even trying this because if there is no
                 // ${headers.resourcename} in the message it will never work
                 // https://camel.apache.org/components/3.18.x/languages/simple-language.html
                 .choice().when(simple("${headers.resourcename} != null"))
                 .to("micrometer:counter:fhirProcessing_Inbd_ProcessedEvent")
                 // testing for null
                 //simple("${header.baz} == null")
                 // testing for not null
                 //simple("${header.baz} != null")
                    .log("${headers.resourcename} fully processed")
                    // Persist to Topic
                    .to("micrometer:counter:fhir_Topic_ProcessedEvent")
                    .to("kafka:{{idaas.fhir.topic.name}}?brokers={{idaas.kafka.brokers}}")
                    // Specific Topic for Each FHIR Resource
                    .toD(String.valueOf("kafka:{{idaas.fhir.topic.name}}"+"_"+"${headers.resourcename}?brokers={{idaas.kafka.brokers}}"))
                    .choice().when(simple("{{idaas.process.FHIR}}"))
                        .to("micrometer:counter:REST_fhirServer_Inbd_ProcessedEvent")
                        .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
                        .setBody(simple("${body}"))
                         //.toD(getFHIRServerUri("AllergyIntolerance"))
                         //.toD(String.valueOf(simple("${headers.resourcename}")))
                         // .toD(("${idaas.fhirserverURI}"+"${headers.resourcename}?bridgeEndpoint=true"))
                         // Process Response
                    .endChoice()
                .endChoice();

         restConfiguration()
                 .component("servlet");
         /*
          *  FHIR Processing
          */
         //https://camel.apache.org/components/3.16.x/eips/multicast-eip.html
         rest("/fhirendpoint")
                 .post()
                 .produces(MediaType.TEXT_PLAIN_VALUE)
                 .route()
                 .routeId("FHIRProcessing")
                 .to("micrometer:counter:REST_fhirendpoint_Inbd_ProcessedEvent")
                 .multicast().parallelProcessing()
                    // FHIR Regular Processing
                    .to("direct:fhirmessaging")
                    // Process Terminologies
                    .to("direct:terminologies")
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

         // General Data Integration
         /*from("direct:generalprocessing")
              .toD(("fhir_${headers.resourcename}"))
        ;*/

     }
 }