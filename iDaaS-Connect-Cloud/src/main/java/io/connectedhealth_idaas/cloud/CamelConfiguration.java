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
package io.connectedhealth_idaas.cloud;

//import javax.jms.ConnectionFactory;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
//import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.stereotype.Component;
import org.apache.camel.component.jackson.JacksonDataFormat;

/*
 *  Kafka implementation based on https://camel.apache.org/components/latest/kafka-component.html
 *  JDBC implementation based on https://camel.apache.org/components/latest/dataformats/hl7-dataformat.html
 *  JPA implementayion based on https://camel.apache.org/components/latest/jpa-component.html
 *  File implementation based on https://camel.apache.org/components/latest/file-component.html
 *  FileWatch implementation based on https://camel.apache.org/components/latest/file-watch-component.html
 *  FTP/SFTP and FTPS implementations based on https://camel.apache.org/components/latest/ftp-component.html
 *  JMS implementation based on https://camel.apache.org/components/latest/jms-component.html
 *  JT400 (AS/400) implementation based on https://camel.apache.org/components/latest/jt400-component.html
 *  HTTP implementation based on https://camel.apache.org/components/latest/http-component.html
 *  HDFS implementation based on https://camel.apache.org/components/latest/hdfs-component.html
 *  jBPMN implementation based on https://camel.apache.org/components/latest/jbpm-component.html
 *  MongoDB implementation based on https://camel.apache.org/components/latest/mongodb-component.html
 *  RabbitMQ implementation based on https://camel.apache.org/components/latest/rabbitmq-component.html
 *  There are lots of third party implementations to support cloud storage from Amazon AC2, Box and so forth
 *  There are lots of third party implementations to support cloud for Amazon Cloud Services
 *  Awaiting update to 3.1 for functionality
 *  Apache Kudu implementation
 *  REST API implementations
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
  private KafkaComponent kafkaComponent(KafkaEndpoint kafkaEndpoint){
    KafkaComponent kafka = new KafkaComponent();
    return kafka;
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
  // Connector for JMS
  private String getKafkaTopicUri(String topic) {
    return "kafka:" + topic +
            "?brokers=" +
            config.getKafkaBrokers();
  }
  // List of components can be found here: https://camel.apache.org/components/3.16.x/index.html
  // Connector for AWS
  private String createAWSConfig(String topic) {
    return "";
  }
  // Connector for Azure
  private String createAzureConfig(String topic) {
    return "";
  }
  // Connector for GCP
  private String createGCPConfig(String topic) {
    return "";
  }
  // Connector for GCP
  private String createGeneralConfig(String topic) {
    return "";
  }

  @Override
  public void configure() throws Exception {
    /*
     *  Direct actions used across platform
     *
     */
    from("direct:auditing")
        .routeId("iDaaS-KIC")
        .setHeader("messageprocesseddate").simple("${date:now:yyyy-MM-dd}")
        .setHeader("messageprocessedtime").simple("${date:now:HH:mm:ss:SSS}")
        .setHeader("processingtype").exchangeProperty("processingtype")
        .setHeader("industrystd").exchangeProperty("industrystd")
        .setHeader("component").exchangeProperty("componentname")
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
     *   Terminologies component for processing terminology events
     */
    from("direct:terminologies")
            .routeId("iDaaS-Terminologies")
            .setHeader("messageprocesseddate").simple("${date:now:yyyy-MM-dd}")
            .setHeader("messageprocessedtime").simple("${date:now:HH:mm:ss:SSS}")
            .setHeader("processingtype").exchangeProperty("processingtype")
            .setHeader("industrystd").exchangeProperty("industrystd")
            .setHeader("component").exchangeProperty("componentname")
            .setHeader("messagetrigger").exchangeProperty("messagetrigger")
            .setHeader("processname").exchangeProperty("processname")
            .setHeader("auditdetails").exchangeProperty("auditdetails")
            .setHeader("camelID").exchangeProperty("camelID")
            .setHeader("exchangeID").exchangeProperty("exchangeID")
            .setHeader("internalMsgID").exchangeProperty("internalMsgID")
            .setHeader("bodyData").exchangeProperty("bodyData")
            .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.terminologyTopic}}"));
    /*
     *  Logging
     */
    from("direct:logging")
        .routeId("Logging")
        .log(LoggingLevel.INFO, log, "Transaction Message: [${body}]")
    ;

    // Sample Using Kafka Topic
    from(getKafkaTopicUri("{{idaas.cloudTopic}}"))
        .routeId("Cloud-Topic")
        // Auditing
        .setProperty("processingtype").constant("data")
        .setProperty("appname").constant("iDAAS-Cloud")
        //.setProperty("industrystd").simple("${industrystd}")
        //.setProperty("messagetrigger").simple("${messagetrigger}")
        .setProperty("component").simple("${routeId}")
        .setProperty("camelID").simple("${camelId}")
        .setProperty("exchangeID").simple("${exchangeId}")
        .setProperty("internalMsgID").simple("${id}")
        .setProperty("bodyData").simple("${body}")
        .setProperty("processname").constant("MTier")
        .setProperty("auditdetails").simple("Cloud Event received for Message ID: ${exchangeId}")
        .wireTap("direct:auditing")
        /*
         * Choice Based on component
         */
        //.convertBodyTo(String.class).to(getKafkaTopicUri("ent_fhirsvr_adverseevent"))
    ;

  }
}
