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

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.ExchangePattern;
// AWS Imports
//import org.apache.camel.component.aws.kinesis.KinesisConstants;
//import org.apache.camel.component.aws.s3.S3Constants;
//import org.apache.camel.component.aws.ses.SesConstants;
//import org.apache.camel.component.aws.sns.SnsConstants;
//import org.apache.camel.component.kafka.KafkaComponent;
//import org.apache.camel.component.kafka.KafkaEndpoint;
//import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.Collections;

@Component
public class CamelConfiguration extends RouteBuilder {

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

  public static final String AWSS3_ROUTE_ID = "aws-s3-inbound";
  public static final String AWSMQQUEUE_ROUTE_ID = "aws-mqqueue-inbound";
  public static final String AWSMQTOPIC_ROUTE_ID = "aws-mqtopic-inbound";
  public static final String AWSSQS_ROUTE_ID = "aws-sqs-inbound";
  public static final String AWSKINESIS_ROUTE_ID = "aws-kinesis-inbound";

  // List of components can be found here: https://camel.apache.org/components/3.16.x/index.html
  // Config for AWS
  /*private String getAWSConfig(String awsInput)
  {
    String awsSecuritySettings= awsInput+"accessKey=RAW("+config.getAwsAccessKey()+")&secretKey=RAW("+config.getAwsSecretKey()+")";
    return awsSecuritySettings;
  }
*/
  @Override
  public void configure() throws Exception {
    onException(Exception.class)
        .handled(true)
        .log(LoggingLevel.ERROR,"${exception}")
        .to("micrometer:counter:cloud_exception_handled");

    rest("/s3-listfiles")
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
            .log("Request received for file ${header.file-name}.")
            .bean(s3Bean,"extract")
            .to("kafka:S3Files?brokers={{idaas.kafka.brokers}}")
            .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_XML_VALUE))
            .to("micrometer:counter:num_files_request")
            .end();

        // S3
    //https://github.com/Talend/apache-camel/blob/master/components/camel-aws/src/main/docs/aws-s3-component.adoc
    // from("aws-s3:helloBucket?accessKey=yourAccessKey&secretKey=yourSecretKey&prefix=hello.txt")
        /*from("aws2-s3://{{idaas.aws.bucketName}}")
        .choice()
            .when(simple("{{idaas.aws.S3}}"))
            // S3 Specifics
            //.setHeader(S3Constants.KEY, simple("${exchangeId}"+".dat"))
            .routeId(AWSS3_ROUTE_ID)
            .to("log:" + AWSS3_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:awsS3ProcessTransactions")
            .to("kafka:{{idaas.awss3.topic.name}}?brokers={{idaas.kafka.brokers}}")
        .endChoice();

        // AMQ Queue
        from(ExchangePattern.OutOnly, "amqp:queue:{{idaas.aws.mq.queue}}")
        .choice().when(simple("{{idaas.aws.MQ.Queue}}"))
            .routeId(AWSMQQUEUE_ROUTE_ID)
            .to("log:" + AWSMQQUEUE_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:awsMQQueueProcessTransactions")
            .to("kafka:{{idaas.awsmqqueue.topic.name}}?brokers={{idaas.kafka.brokers}}")
        .endChoice();

        // AMQ Topic
        from(ExchangePattern.InOnly, "amqp:topic:{{idaas.aws.mq.topic}}")
        .choice().when(simple("{{idaas.aws.MQ.Topic}}"))
            .routeId(AWSMQTOPIC_ROUTE_ID)
            .to("log:" + AWSMQTOPIC_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:awsMQTopicProcessTransactions")
            .to("kafka:{{idaas.awsmqtopic.topic.name}}?brokers={{idaas.kafka.brokers}}")
        .endChoice();

        // SQS
        from("aws2-sqs://{{idaas.aws.sqs}}")
        .choice().when(simple("{{idaas.aws.SQS}}"))
            // SQS Specifics
            .routeId(AWSSQS_ROUTE_ID)
            .to("log:" + AWSSQS_ROUTE_ID + "?showAll=true")
            .log("${exchangeId} fully processed")
            .to("micrometer:counter:awsSQSProcessTransactions")
            .to("kafka:{{idaas.awssqs.topic.name}}?brokers={{idaas.kafka.brokers}}")
        .endChoice();

        // Kinesis
       *//* .choice().when(simple("{{idaas.aws.Kinesis}}"))
            // Kinesis
            .setHeader(KinesisConstants.PARTITION_KEY,simple("Shard1"))
            //.to(getAWSConfig("aws2-kinesis://testhealthcarekinesisstream?"))
            .from("aws2-kinesis://testhealthcarekinesisstream?exchangePattern=OutOnly")
        .endChoice()*//*
*/

  }
}
