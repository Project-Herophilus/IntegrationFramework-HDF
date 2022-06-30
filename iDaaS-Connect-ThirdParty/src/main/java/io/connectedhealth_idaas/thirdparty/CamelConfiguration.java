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
package io.connectedhealth_idaas.thirdparty;

//import javax.jms.ConnectionFactory;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
//import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.stereotype.Component;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
//Parsers for any artifacts within this platform
import io.connectedhealth_idaas.parsers.*;

/*
 *    This sub module is intended to be a general catch all for all types of connectivity that
 *    are not healthcare industry standards or industry platforms like SAP. For a complete list
 *    of all the capabilities you can see the maintained and ongoing connectors:
 *
 *    https://camel.apache.org/components/3.17.x/
 */
@Component
public class CamelConfiguration extends RouteBuilder {
  private static final Logger log = LoggerFactory.getLogger(CamelConfiguration.class);

  @Autowired
  private ConfigProperties config;

  @Bean
  private KafkaEndpoint kafkaEndpoint(){
    KafkaEndpoint kafkaEndpoint = new KafkaEndpoint();
    return kafkaEndpoint;
  }

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
  private String getKafkaTopicUri(String topic) {
    return "kafka:" + topic +
            "?brokers=" +
            config.getKafkaBrokers();
  }

  @Override
  public void configure() throws Exception {
    /*
     *   HIDN
     *   HIDN - Health information Data Network
     *   Intended to enable simple movement of data aside from specific standards
     *   Common Use Cases are areas to support remote (iOT/Edge) and any other need for small footprints to larger
     *   footprints
     *
     */
    from("direct:hidn")
            .routeId("HIDN-Processing")
            .setHeader("messageprocesseddate").simple("${date:now:yyyy-MM-dd}")
            .setHeader("messageprocessedtime").simple("${date:now:HH:mm:ss:SSS}")
            .setHeader("eventdate").simple("eventdate")
            .setHeader("eventtime").simple("eventtime")
            .setHeader("processingtype").exchangeProperty("processingtype")
            .setHeader("industrystd").exchangeProperty("industrystd")
            .setHeader("component").exchangeProperty("componentname")
            .setHeader("processname").exchangeProperty("processname")
            .setHeader("organization").exchangeProperty("organization")
            .setHeader("careentity").exchangeProperty("careentity")
            .setHeader("customattribute1").exchangeProperty("customattribute1")
            .setHeader("customattribute2").exchangeProperty("customattribute2")
            .setHeader("customattribute3").exchangeProperty("customattribute3")
            .setHeader("camelID").exchangeProperty("camelID")
            .setHeader("exchangeID").exchangeProperty("exchangeID")
            .setHeader("internalMsgID").exchangeProperty("internalMsgID")
            .setHeader("bodyData").exchangeProperty("bodyData")
            .setHeader("bodySize").exchangeProperty("bodySize")
            .convertBodyTo(String.class).to(getKafkaTopicUri("hidn"))
    ;
    /*
     *  Direct actions used across platform
     *
     */
    from("direct:auditing")
        .routeId("KIC-KnowledgeInsightConformance")
        .setHeader("messageprocesseddate").simple("${date:now:yyyy-MM-dd}")
        .setHeader("messageprocessedtime").simple("${date:now:HH:mm:ss:SSS}")
        .setHeader("processingtype").exchangeProperty("processingtype")
        .setHeader("industrystd").exchangeProperty("industrystd")
        .setHeader("component").exchangeProperty("component")
        .setHeader("messagetrigger").exchangeProperty("messagetrigger")
        .setHeader("processname").exchangeProperty("processname")
        .setHeader("auditdetails").exchangeProperty("auditdetails")
        .setHeader("camelID").exchangeProperty("camelID")
        .setHeader("exchangeID").exchangeProperty("exchangeID")
        .setHeader("internalMsgID").exchangeProperty("internalMsgID")
        .setHeader("bodyData").exchangeProperty("bodyData")
        .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.integrationTopic}}"))
    ;
    /*
     *  Logging
     */
    from("direct:logging")
        .routeId("Logging")
        .log(LoggingLevel.INFO, log, "Transaction Message: [${body}]")
    ;

    /*
     *  Servlet common endpoint accessable to process transactions
     */
    from("servlet://hidn")
        .routeId("HIDN-Servlet")
        // Data Parsing and Conversions
        // Normal Processing
        .convertBodyTo(String.class)
        .setHeader("messageprocesseddate").simple("${date:now:yyyy-MM-dd}")
        .setHeader("messageprocessedtime").simple("${date:now:HH:mm:ss:SSS}")
        .setHeader("eventdate").simple("eventdate")
        .setHeader("eventtime").simple("eventtime")
        .setHeader("processingtype").exchangeProperty("processingtype")
        .setHeader("industrystd").exchangeProperty("industrystd")
        .setHeader("component").exchangeProperty("component")
        .setHeader("processname").exchangeProperty("processname")
        .setHeader("organization").exchangeProperty("organization")
        .setHeader("careentity").exchangeProperty("careentity")
        .setHeader("customattribute1").exchangeProperty("customattribute1")
        .setHeader("customattribute2").exchangeProperty("customattribute2")
        .setHeader("customattribute3").exchangeProperty("customattribute3")
        .setHeader("camelID").exchangeProperty("camelID")
        .setHeader("exchangeID").exchangeProperty("exchangeID")
        .setHeader("internalMsgID").exchangeProperty("internalMsgID")
        .setHeader("bodyData").exchangeProperty("bodyData")
        .setHeader("bodySize").exchangeProperty("bodySize")
        .wireTap("direct:hidn")
    ;

    /*
     *  Servlet common endpoint accessable to process transactions
     */
    from("servlet://iot")
        .routeId("IoTServlet")
        // Data Parsing and Conversions
        // Normal Processing
        .convertBodyTo(String.class)
        // set Auditing Properties
        .convertBodyTo(String.class)
        .setProperty("processingtype").constant("data")
        .setProperty("appname").constant("iDAAS-Connect-ThirdParty")
        .setProperty("industrystd").constant("IoT")
        .setProperty("messagetrigger").constant("IoT Event Received")
        .setProperty("component").simple("${routeId}")
        //.setProperty("component").constant("IoTEventProcessor")
        .setProperty("camelID").simple("${camelId}")
        .setProperty("exchangeID").simple("${exchangeId}")
        .setProperty("internalMsgID").simple("${id}")
        .setProperty("bodyData").simple("${body}")
        .setProperty("processname").constant("Input")
        .setProperty("auditdetails").constant("IoT message received")
        // iDAAS KIC - Auditing Processing
        .wireTap("direct:auditing")
        // Send To Topic
        .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.iotintegrationTopic}}"))
    ;
    /*
     *  Kafka Implementation for implementing Third Party FHIR Server direct connection
     */


    /*
     *  MandatoryReporting
     *  Sample: CSV ETL Process to Topic
     *  parse and process to Topic
     *
     */
    // Specific filename - from("file:{{mandatory.reporting.directory}}/?fileName={{mandatory.reporting.file}}")
    from("file:{{mandatory.reporting.directory}}/")
        .routeId("FileProcessing-MandatoryReporting")
        .choice()
          .when(simple("${file:ext} == 'csv'"))
          .split(body().tokenize("\n"))
          .streaming().unmarshal(new BindyCsvDataFormat(ReportingOutput.class))
          .marshal(new JacksonDataFormat(ReportingOutput.class)).to(getKafkaTopicUri("MandatoryReporting"))
          // Auditing
          .setProperty("processingtype").constant("csv-data")
          .setProperty("appname").constant("iDAAS-Connect-ThirdParty")
          .setProperty("industrystd").constant("CSV")
          .setProperty("messagetrigger").constant("CSVFile")
          .setProperty("component").simple("${routeId}")
          .setProperty("camelID").simple("${camelId}")
          .setProperty("exchangeID").simple("${exchangeId}")
          .setProperty("internalMsgID").simple("${id}")
          .setProperty("bodyData").simple("${body}")
          .setProperty("processname").constant("Input")
          .setProperty("auditdetails").simple("${file:name} - was processed, parsed and put into topic")
          .wireTap("direct:auditing")
    ;

    /*
     *  Mandatory Reporting
     *  Sample: Topic to Postgres
     *
     */

    /*from(getKafkaTopicUri("MandatoryReporting")).unmarshal(new JacksonDataFormat(ReportingOutput.class))
        *//* .process(new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
            final ReportingOutput payload = exchange.getIn().getBody(ReportingOutput.class);
           }
        })*//*
        .routeId("DBProcessing-MandatoryReporting")
        .log(LoggingLevel.INFO, log, "Transaction Message: [${body}]")
        .to("sql:insert into etl_mandatoryreporting (organizationid,patientaccountnumber, patientlastname, patientfirstname, zipcode, roombed, " +
            "age, gender, admissiondate) values( :#${body.organizationId},:#${body.patientAccount},:#${body.patientLastName}," +
            ":#${body.patientFirstName},:#${body.zipCode},:#${body.roomBed},:#${body.age},:#${body.gender},:#${body.admissionDate})");
    ;*/

    /*
     *  Sample: CSV Covid Data to Topic
     *  Covid John Hopkins Data
     */
    // With "#,#...", it just iterates over the list to substitute the values.
    // No names are used there. If the message body would be ReportingOutput.class (instead of List), you can use ":#${body.organizationId}" expressions
    //
    from("file:{{covid.reporting.directory}}/")
        .routeId("FileProcessing-CovidReporting")
        .choice()
        .when(simple("${file:ext} == 'csv'"))
        //.when(simple("${file:ext} == ${covid.reporting.extension}"))
        .split(body().tokenize("\n")).streaming()
        .unmarshal(new BindyCsvDataFormat(CovidJohnHopkinsUSDailyData.class))
        //.marshal(new JacksonDataFormat(CovidJohnHopkinsUSDailyData.class))
        .to(getKafkaTopicUri("CovidDailyData"))
        .endChoice();
    /*
     *  Sample: CSV Research Data to Topic
     *
     */
    from("file:{{research.data.directory}}/")
        .routeId("FileProcessing-ResearchReporting")
        .choice()
        .when(simple("${file:ext} == 'csv'"))
        //.when(simple("${file:ext} == ${covid.reporting.extension}"))
        .split(body().tokenize("\n")).streaming()
        .unmarshal(new BindyCsvDataFormat(ResearchData.class))
        .marshal(new JacksonDataFormat(ResearchData.class))
        .to(getKafkaTopicUri("ResearchData"))
        // Auditing
        .setProperty("processingtype").constant("csv-data")
        .setProperty("appname").constant("iDAAS-Connect-ThirdParty")
        .setProperty("industrystd").constant("CSV")
        .setProperty("messagetrigger").constant("CSVFile-ResearchData")
        .setProperty("component").simple("${routeId}")
        .setProperty("camelID").simple("${camelId}")
        .setProperty("exchangeID").simple("${exchangeId}")
        .setProperty("internalMsgID").simple("${id}")
        .setProperty("bodyData").simple("${body}")
        .setProperty("processname").constant("Input")
        .setProperty("auditdetails").simple("${file:name} - was processed, parsed and put into topic")
        .wireTap("direct:auditing");
  }
}
