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
import java.util.Collections;
// AWS
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.component.aws2.kinesis.Kinesis2Constants;
import org.apache.camel.component.aws2.sns.Sns2Constants;
import org.apache.camel.component.aws2.ses.Ses2Constants;
//import org.apache.camel.component.aws2.ddb.Ddb2Constants;

/*
 *  Apache Camel Connectors Can Be found here
 *
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
  // Config for AWS
  private String getAWSConfig(String awsInput)
  {
    //.to("aws2-kinesis://testhealthcarekinesisstream?accessKey=RAW(AKIAUYXCPSBTBTP2F5WZ)&secretKey=RAW(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)")
    //.to("aws2-kinesis://testhealthcarekinesisstream2?accessKey=RAW(AKIAUYXCPSBTBTP2F5WZ)&secretKey=RAW(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)")
    //.to("aws2-sqs://testhealthcarequeue?accessKey=RAW(AKIAUYXCPSBTBTP2F5WZ)&secretKey=RAW(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)")

    String awsSecuritySettings= awsInput+"accessKey=RAW("+config.getAwsAccessKey()+")&secretKey=RAW("+config.getAwsSecretKey()+")";
    return awsSecuritySettings;
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
     *  Logging
     */
    from("direct:logging")
        .routeId("Logging")
        .log(LoggingLevel.INFO, log, "Transaction Message: [${body}]")
    ;

    /*
     *  Testing EndPoints
     *  Servlet
     */
    from("servlet://test_publiccloud")
         .routeId("test_http_cloud")
         // set Auditing Properties
         .convertBodyTo(String.class)
         .setProperty("processingtype").constant("data")
         .setProperty("appname").constant("iDAAS-Connect-Cloud")
         .setProperty("industrystd").constant("N/A")
         .setProperty("messagetrigger").constant("N/A")
         .setProperty("component").simple("${routeId}")
         .setProperty("camelID").simple("${camelId}")
         .setProperty("exchangeID").simple("${exchangeId}")
         .setProperty("internalMsgID").simple("${id}")
         .setProperty("bodyData").simple("${body}")
         .setProperty("processname").constant("Input")
         .setProperty("auditdetails").simple("Servlet message received ${exchangeId}")
         // iDAAS KIC - Auditing Processing
         .wireTap("direct:auditing")
         // Send To Topic
         .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.test_cloudTopic}}"))
    ;
    /*
     *  File Processing
     */
    from("file://data-input/cloud/")
         .routeId("test_fileinput_cloud")
         // set Auditing Properties
         .convertBodyTo(String.class)
         .setProperty("processingtype").constant("data")
         .setProperty("appname").constant("iDAAS-Connect-Cloud")
         .setProperty("industrystd").constant("N/A")
         .setProperty("messagetrigger").constant("N/A")
         .setProperty("component").simple("${routeId}")
         .setProperty("camelID").simple("${camelId}")
         .setProperty("exchangeID").simple("${exchangeId}")
         .setProperty("internalMsgID").simple("${id}")
         .setProperty("bodyData").simple("${body}")
         .setProperty("processname").constant("Input")
         .setProperty("auditdetails").simple("File Processed ${exchangeId}")
         // iDAAS KIC - Auditing Processing
         .wireTap("direct:auditing")
         .to(getKafkaTopicUri("{{idaas.cloudTopic}}"))
    ;

    /*
     *  Core Outbound Cloud Processing
     *
     */
    from("servlet://publiccloud")
            .routeId("http_cloud")
            // set Auditing Properties
            .convertBodyTo(String.class)
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Connect-Cloud")
            .setProperty("industrystd").constant("N/A")
            .setProperty("messagetrigger").constant("N/A")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("Input")
            .setProperty("auditdetails").simple("Servlet message received ${exchangeId}")
            // iDAAS KIC - Auditing Processing
            .wireTap("direct:auditing")
            // Send To Topic
            .convertBodyTo(String.class).to(getKafkaTopicUri("{{idaas.cloudTopic}}"))
    ;
    // Kafka Topic
    from(getKafkaTopicUri("{{idaas.cloudTopic}}"))
        .routeId("Cloud-Kafka-Topic")
        //.multicast().parallelProcessing()
        //.to("direct:generalprocessing")
        // Auditing
        .setProperty("processingtype").constant("data")
        .setProperty("appname").constant("iDAAS-Cloud")
        .setProperty("industrystd").constant("N/A")
        .setProperty("messagetrigger").constant("N/A")
        .setProperty("component").simple("${routeId}")
        .setProperty("camelID").simple("${camelId}")
        .setProperty("exchangeID").simple("${exchangeId}")
        .setProperty("internalMsgID").simple("${id}")
        .setProperty("bodyData").simple("${body}")
        .setProperty("processname").constant("MTier Cloud Topic")
        .setProperty("auditdetails").simple("Cloud Event received for Message ID: ${exchangeId}")
        .wireTap("direct:auditing")
        // Send To S3
        .choice().when(simple("{{idaas.awsS3}}"))
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Cloud")
            .setProperty("industrystd").constant("N/A")
            .setProperty("messagetrigger").constant("N/A")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("AWS-S3")
            .setProperty("auditdetails").simple("AWS S3 Event processed for Message ID: ${exchangeId}")
            .wireTap("direct:auditing")
            // S3 Specifics
            .setHeader(AWS2S3Constants.KEY, simple("${exchangeId}"+".dat"))
            .to("aws2-s3://testhealthcarebucket")
        // Send to SQS
        .choice().when(simple("{{idaas.awsSqs}}"))
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Cloud")
            .setProperty("industrystd").constant("N/A")
            .setProperty("messagetrigger").constant("N/A")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("AWS-SQS")
            .setProperty("auditdetails").simple("AWS SQS Event processed for Message ID: ${exchangeId}")
            .wireTap("direct:auditing")
            // SQS Specifics
            .to(getAWSConfig("aws2-sqs://testhealthcarequeue?"))
        // Send to SNS
        .choice().when(simple("{{idaas.awsSns}}"))
            //.to("aws2-sns://testhealthcaretopic?subject=The+subject+message&autoCreateTopic=true");
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Cloud")
            .setProperty("industrystd").constant("N/A")
            .setProperty("messagetrigger").constant("N/A")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("AWS-SNS")
            .setProperty("auditdetails").simple("AWS SNS Event processed for Message ID: ${exchangeId}")
            .wireTap("direct:auditing")
            // SNS Specifics
            .setHeader(Sns2Constants.SUBJECT,simple("iOT Data Received"))
            .setHeader(Sns2Constants.MESSAGE_ID,simple("${exchangeId}"))
            .to("aws2-sns://TestSNS?accessKey=RAW(AKIAUYXCPSBTBTP2F5WZ)&secretKey=RAW(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)")
        // Send to Kinesis
        .choice().when(simple("{{idaas.awsKinesis}}"))
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Cloud")
            .setProperty("industrystd").constant("N/A")
            .setProperty("messagetrigger").constant("N/A")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("AWS-Kinesis")
            .setProperty("auditdetails").simple("AWS Kinesis Event processed for Message ID: ${exchangeId}")
            .wireTap("direct:auditing")
            // Kinesis
            .setHeader(Kinesis2Constants.PARTITION_KEY,simple("Shard1"))
            .to(getAWSConfig("aws2-kinesis://testhealthcarekinesisstream?"))
        // Send to SES
        .choice().when(simple("{{idaas.awsSes}}"))
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Cloud")
            .setProperty("industrystd").constant("N/A")
            .setProperty("messagetrigger").constant("N/A")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("AWS-SES")
            .setProperty("auditdetails").simple("AWS SES Event processed for Message ID: ${exchangeId}")
            .wireTap("direct:auditing")
            // SES
            .setHeader(Ses2Constants.SUBJECT, simple("New Publish Data to AWS"))
            .setHeader(Ses2Constants.TO, constant(Collections.singletonList("balanscott@outlook.com")))
            .setBody(simple("Data was received on ${date:now:yyyy-MM-dd} at ${date:now:HH:mm:ss:SSS}."))
            //.to("aws2-ses://alscott@redhat.com?accessKey=RAW(AKIAUYXCPSBTBTP2F5WZ)&secretKey=RAW(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)")
        // Send to AMQ
        .choice().when(simple("{{idaas.awsMq}}"))
            .setProperty("processingtype").constant("data")
            .setProperty("appname").constant("iDAAS-Cloud")
            .setProperty("industrystd").constant("N/A")
            .setProperty("messagetrigger").constant("N/A")
            .setProperty("component").simple("${routeId}")
            .setProperty("camelID").simple("${camelId}")
            .setProperty("exchangeID").simple("${exchangeId}")
            .setProperty("internalMsgID").simple("${id}")
            .setProperty("bodyData").simple("${body}")
            .setProperty("processname").constant("AWS-Kinesis")
            .setProperty("auditdetails").simple("AWS AMQ Event processed for Message ID: ${exchangeId}")
            .wireTap("direct:auditing")
            .convertBodyTo(String.class)
            .to(ExchangePattern.InOnly, "amqp:topic:myHealthcareTopic?connectionFactory=#jmsConnectionFactory")
            .to(ExchangePattern.InOnly, "amqp:queue:myHealthcareQueue?connectionFactory=#jmsConnectionFactory")
            // Lambda
        .endChoice()
    .end();

  }
}
