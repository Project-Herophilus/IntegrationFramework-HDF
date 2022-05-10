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

import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = "idaas")
public class ConfigProperties {

    private String kafkaBrokers;
    private String integrationTopic;
    private String terminologyTopic;
    private String cloudTopic;
    private String general_knative;
    private String general_dropbox;
    private String gcp_bigquery;
    private String gcp_bigquerysql;
    private String gcp_pubsub;
    private String gcp_storage;
    private String azure_cosmos;
    private String azure_eventhubs;
    private String azure_servicebus;
    private String azure_blobservice;
    private String azure_datalake;
    private String azure_queue;
    private String aws_dynamodb;
    private String aws_dynamodbstreams;
    private String aws_eventbridge;
    private String aws_kinesis;
    private String aws_kinesisfirehose;
    private String aws_lambda;
    private String aws_kafkamsk;
    private String aws_mq;
    private String aws_s3;
    private String aws_simpleemail;
    private String aws_sqs;

    public String getKafkaBrokers() {
        return kafkaBrokers;
    }
    public void setKafkaBrokers(String kafkaBrokers) {
        this.kafkaBrokers = kafkaBrokers;
    }

    public String getIntegrationTopic() {return integrationTopic;}
    public void setIntegrationTopic(String integrationTopic) { this.integrationTopic = integrationTopic;}

    public String getTerminologyTopic() { return terminologyTopic; }
    public void setTerminologyTopic(String terminologyTopic) { this.terminologyTopic = terminologyTopic; }
    public String getCloudTopic() { return cloudTopic; }
    public void setCloudTopic(String cloudTopic) { this.cloudTopic = cloudTopic; }

    public String getGeneral_knative() {
        return general_knative;
    }

    public void setGeneral_knative(String general_knative) {
        this.general_knative = general_knative;
    }

    public String getGeneral_dropbox() {
        return general_dropbox;
    }

    public void setGeneral_dropbox(String general_dropbox) {
        this.general_dropbox = general_dropbox;
    }

    public String getGcp_bigquery() {
        return gcp_bigquery;
    }

    public void setGcp_bigquery(String gcp_bigquery) {
        this.gcp_bigquery = gcp_bigquery;
    }

    public String getGcp_bigquerysql() {
        return gcp_bigquerysql;
    }

    public void setGcp_bigquerysql(String gcp_bigquerysql) {
        this.gcp_bigquerysql = gcp_bigquerysql;
    }

    public String getGcp_pubsub() {
        return gcp_pubsub;
    }

    public void setGcp_pubsub(String gcp_pubsub) {
        this.gcp_pubsub = gcp_pubsub;
    }

    public String getGcp_storage() {
        return gcp_storage;
    }

    public void setGcp_storage(String gcp_storage) {
        this.gcp_storage = gcp_storage;
    }

    public String getAzure_cosmos() {
        return azure_cosmos;
    }

    public void setAzure_cosmos(String azure_cosmos) {
        this.azure_cosmos = azure_cosmos;
    }

    public String getAzure_eventhubs() {
        return azure_eventhubs;
    }

    public void setAzure_eventhubs(String azure_eventhubs) {
        this.azure_eventhubs = azure_eventhubs;
    }

    public String getAzure_servicebus() {
        return azure_servicebus;
    }

    public void setAzure_servicebus(String azure_servicebus) {
        this.azure_servicebus = azure_servicebus;
    }

    public String getAzure_blobservice() {
        return azure_blobservice;
    }

    public void setAzure_blobservice(String azure_blobservice) {
        this.azure_blobservice = azure_blobservice;
    }

    public String getAzure_datalake() {
        return azure_datalake;
    }

    public void setAzure_datalake(String azure_datalake) {
        this.azure_datalake = azure_datalake;
    }

    public String getAzure_queue() {
        return azure_queue;
    }

    public void setAzure_queue(String azure_queue) {
        this.azure_queue = azure_queue;
    }

    public String getAws_dynamodb() {
        return aws_dynamodb;
    }

    public void setAws_dynamodb(String aws_dynamodb) {
        this.aws_dynamodb = aws_dynamodb;
    }

    public String getAws_dynamodbstreams() {
        return aws_dynamodbstreams;
    }

    public void setAws_dynamodbstreams(String aws_dynamodbstreams) {
        this.aws_dynamodbstreams = aws_dynamodbstreams;
    }

    public String getAws_eventbridge() {
        return aws_eventbridge;
    }

    public void setAws_eventbridge(String aws_eventbridge) {
        this.aws_eventbridge = aws_eventbridge;
    }

    public String getAws_kinesis() {
        return aws_kinesis;
    }

    public void setAws_kinesis(String aws_kinesis) {
        this.aws_kinesis = aws_kinesis;
    }

    public String getAws_kinesisfirehose() {
        return aws_kinesisfirehose;
    }

    public void setAws_kinesisfirehose(String aws_kinesisfirehose) {
        this.aws_kinesisfirehose = aws_kinesisfirehose;
    }

    public String getAws_lambda() {
        return aws_lambda;
    }

    public void setAws_lambda(String aws_lambda) {
        this.aws_lambda = aws_lambda;
    }

    public String getAws_kafkamsk() {
        return aws_kafkamsk;
    }

    public void setAws_kafkamsk(String aws_kafkamsk) {
        this.aws_kafkamsk = aws_kafkamsk;
    }

    public String getAws_mq() {
        return aws_mq;
    }

    public void setAws_mq(String aws_mq) {
        this.aws_mq = aws_mq;
    }

    public String getAws_s3() {
        return aws_s3;
    }

    public void setAws_s3(String aws_s3) {
        this.aws_s3 = aws_s3;
    }

    public String getAws_simpleemail() {
        return aws_simpleemail;
    }

    public void setAws_simpleemail(String aws_simpleemail) {
        this.aws_simpleemail = aws_simpleemail;
    }

    public String getAws_sqs() {
        return aws_sqs;
    }

    public void setAws_sqs(String aws_sqs) {
        this.aws_sqs = aws_sqs;
    }
}
