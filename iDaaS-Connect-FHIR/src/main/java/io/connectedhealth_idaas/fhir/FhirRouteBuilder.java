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

 import ca.uhn.fhir.store.IAuditDataStore;
 import org.apache.camel.LoggingLevel;
 import org.apache.camel.builder.RouteBuilder;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
// Event Builder
 import io.connectedhealth_idaas.eventbuilder.events.platform.FHIRTerminologyProcessorEvent;
 import io.connectedhealth_idaas.eventbuilder.parsers.fhir.FHIRBundleParser;

 @Component
 public class CamelConfiguration extends RouteBuilder {
     @Autowired
     private ConfigProperties config;

     private String getFHIRServerUri(String fhirResource) {
         String fhirServerVendor = config.getFhirVendor();
         //String fhirEndPoint = "simple(${headers.resourcename})";
         String fhirServerURI = null;
         if (fhirServerVendor.equals("ibm"))
         {
             //.to("jetty:http://localhost:8090/fhir-server/api/v4/AdverseEvents?bridgeEndpoint=true&exchangePattern=InOut")
             //fhirServerURI = "http:"+config.getHapiURI()+fhirResource+"?bridgeEndpoint=true";
             fhirServerURI = config.getIbmURI()+fhirResource;
             fhirServerURI = fhirServerURI+"?bridgeEndpoint=true";
         }
         if (fhirServerVendor.equals("hapi"))
         {
             //fhirServerURI = "http:"+config.getHapiURI()+fhirEndPoint+"?bridgeEndpoint=true";
             //fhirServerURI = config.getHapiURI()+fhirEndPoint;
             fhirServerURI = config.getHapiURI()+fhirResource;
             fhirServerURI = fhirServerURI+"?bridgeEndpoint=true";
         }
         if (fhirServerVendor.equals("microsoft"))
         {
             fhirServerURI = "http:"+config.getMicrosoftURI()+fhirResource+"?bridgeEndpoint=true";
         }
         return fhirServerURI;
     }

     /*
      * Kafka implementation based upon https://camel.apache.org/components/latest/kafka-component.html
      *
      */
     @Override
     public void configure() throws Exception {

         /*
          *  FHIR Processing
          */
         //https://camel.apache.org/components/3.16.x/eips/multicast-eip.html
         from("rest:post/idaas/fhirendpoint")
                 .routeId("FHIRProcessing")
                 .multicast().parallelProcessing()
                     .to("direct:generalprocessing")
                     .to("direct:fhirmessaging")
                     .to("direct:terminologies")
                 .end();

         // General Data Integration
         from("direct:generalprocessing")
              .toD(getKafkaTopicUri("fhir_${headers.resourcename}"))
         ;

         // Send to FHIR Server
         from("direct:fhirmessaging")
             .routeId("FHIRMessaging")
                 .choice().when(simple("{{idaas.processToFHIR}}"))
                    .setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
                    //.toD(getFHIRServerUri("AllergyIntolerance"))
                    // http://simple%7BAdverseEvent%7D?bridgeEndpoint=true
                    //.toD(getFHIRServerUri(String.valueOf(simple("${headers.resourcename}"))))
                    .setBody(simple("${body}"))
                    .toD(getFHIRServerUri("${headers.resourcename}"))
                 // Process Response
                 .convertBodyTo(String.class)
             .endChoice();

         // Send to Terminology Processes
         from("direct:terminologies")
              .routeId("TerminologyProcessor")
                 .choice().when(simple("{{idaas.processTerminologies}}"))
                    .setBody(simple("${body}"))
                 .endChoice();

         from("rest:post/idaas/AdverseEvent")
              // Send To Topic
              //.toD(getKafkaTopicUri("fhir_testoutput"))
         ;

     }
 }