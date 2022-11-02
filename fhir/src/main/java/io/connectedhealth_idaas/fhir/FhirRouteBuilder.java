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
 package io.connectedhealth_idaas.fhir;

 import io.connectedhealth_idaas.eventbuilder.converters.ccda.CdaConversionService;
 import org.apache.camel.Exchange;
 import org.apache.camel.LoggingLevel;
 import org.apache.camel.builder.RouteBuilder;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Component;

 @Component
 public class FhirRouteBuilder extends RouteBuilder {

     public static final String TERMINOLOGY_ROUTE_ID = "terminologies-direct";
     public static final String DEIDENTIFICATION_ROUTE_ID = "deidentification-direct";
     public static final String EMPI_ROUTE_ID = "empi-direct";
     public static final String PUBLICCLOUD_ROUTE_ID = "publiccloud-direct";
     public static final String SDOH_ROUTE_ID = "sdoh-direct";
     @Override
     public void configure() throws Exception {

         onException(Exception.class)
                 .handled(true)
                 .log(LoggingLevel.ERROR,"${exception}")
                 .to("micrometer:counter:fhir_exception_handled");

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

         from("direct:deidentification")
                 .choice()
                 .when(simple("{{idaas.process.Deidentification}}"))
                 .routeId(DEIDENTIFICATION_ROUTE_ID)
                 .to("log:" + DEIDENTIFICATION_ROUTE_ID + "?showAll=true")
                 //.log("${exchangeId} fully processed")
                 .to("micrometer:counter:deidentificationTransactions")
                 .to("kafka:{{idaas.deidentification.topic.name}}?brokers={{idaas.kafka.brokers}}")
                 // to the deidentification API
                 .endChoice();

         from("direct:empi")
                 .choice()
                 .when(simple("{{idaas.process.Empi}}"))
                 .routeId(EMPI_ROUTE_ID)
                 .to("log:" + EMPI_ROUTE_ID + "?showAll=true")
                 //.log("${exchangeId} fully processed")
                 .to("micrometer:counter:deidentificationTransactions")
                 .to("kafka:{{idaas.deidentification.topic.name}}?brokers={{idaas.kafka.brokers}}")
                 // to the empi API
                 .endChoice();

         from("direct:publiccloud")
                 .choice()
                 .when(simple("{{idaas.process.PublicCloud}}"))
                 .routeId(PUBLICCLOUD_ROUTE_ID)
                 .to("log:" + PUBLICCLOUD_ROUTE_ID + "?showAll=true")
                 //.log("${exchangeId} fully processed")
                 .to("micrometer:counter:publiccloudTransactions")
                 .to("kafka:{{idaas.publiccloud.topic.name}}?brokers={{idaas.kafka.brokers}}")
                 .endChoice();

         from("direct:sdoh")
                 .choice()
                 .when(simple("{{idaas.process.Sdoh}}"))
                 .routeId(SDOH_ROUTE_ID)
                 .to("log:" + SDOH_ROUTE_ID + "?showAll=true")
                 //.log("${exchangeId} fully processed")
                 .to("micrometer:counter:sdohTransactions")
                 .to("kafka:{{idaas.sdoh.topic.name}}?brokers={{idaas.kafka.brokers}}")
                 .endChoice();

         /*
          *  FHIR Processing
          */
         //https://camel.apache.org/components/3.16.x/eips/multicast-eip.html
         rest("/fhirendpoint")
                 .post()
                 .produces(MediaType.TEXT_PLAIN_VALUE)
                 .route()
                 .routeId("FHIRProcessing")
                 .multicast().parallelProcessing()
                     //.to("direct:generalprocessing")
                     .to("direct:fhirmessaging")
                    // Process Terminologies
                    .to("direct:terminologies")
                    // Deidentification
                    .to("direct:deidentification")
                    // EMPI
                    .to("direct:empi")
                    // Public Cloud
                    .to("direct:publiccloud")
                    //SDOH
                    .to("direct:sdoh")
        .endRest();

         // General Data Integration
         /*from("direct:generalprocessing")
              .toD(("fhir_${headers.resourcename}"))
        ;*/

         // Send to FHIR Server
         from("direct:fhirmessaging")
             .routeId("FHIRMessaging")
                 // we should test before even trying this because if there is no
                 // ${headers.resourcename} in the message it will never work
                 .choice().when(simple("{{idaas.processToFHIR}}"))
                        .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
                        //.toD(getFHIRServerUri("AllergyIntolerance"))
                        //.toD(String.valueOf(simple("${headers.resourcename}")))
                        .setBody(simple("${body}"))
                        .toD(("${idaas.fhirserverURI}"+"${headers.resourcename}?bridgeEndpoint=true"))
                        // Process Response
                        .convertBodyTo(String.class)
             .endChoice();



     }
 }