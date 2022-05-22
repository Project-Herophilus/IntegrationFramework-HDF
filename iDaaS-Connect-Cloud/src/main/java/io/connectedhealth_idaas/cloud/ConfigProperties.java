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

    // General
    private String kafkaBrokers;
    private String integrationTopic;
    private String terminologyTopic;
    private String cloudTopic;
    private String test_cloudTopic;
    private String general_knative;
    private String general_dropbox;
    // AWS
    private String awsAccessKey;
    private String awsSecretKey;
    private Boolean awsS3;
    private Boolean awsKinesis;
    private Boolean awsMq;
    private Boolean awsSns;
    private Boolean awsSes;
    private Boolean awsSqs;
    private Boolean awsLambda;
    private Boolean awsDyanmoDB;
    // Azure
    //private Boolean azureCosmosDB;
    //private Boolean azureBlobStorage;
   // GCP
   // private Boolean gcpBigquery;
   // private String gcpBigquerysql;
   // private String gcpPubsub;
   // private String gcpStorage;


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
    public String getTest_cloudTopic() { return test_cloudTopic;}
    public void setTest_cloudTopic(String test_cloudTopic) {this.test_cloudTopic = test_cloudTopic;}

    public String getGeneral_knative() { return general_knative; }
   public void setGeneral_knative(String general_knative) { this.general_knative = general_knative; }

    public String getGeneral_dropbox() { return general_dropbox;}
    public void setGeneral_dropbox(String general_dropbox) { this.general_dropbox = general_dropbox; }

    // AWS Access
    public String getAwsAccessKey() { return awsAccessKey;}
    public void setAwsAccessKey(String awsAccessKey) { this.awsAccessKey = awsAccessKey;}

    public String getAwsSecretKey() { return awsSecretKey; }
    public void setAwsSecretKey(String awsSecretKey) { this.awsSecretKey = awsSecretKey;}

    /*
     *   AWS PaaS Components
     */
    public Boolean getAwsS3() { return awsS3;}
    public void setAwsS3(Boolean awsS3) {this.awsS3 = awsS3;}
    public Boolean getAwsKinesis() { return awsKinesis;}
    public void setAwsKinesis(Boolean awsKinesis) {this.awsKinesis = awsKinesis;}
    public Boolean getAwsMq() { return awsMq;}
    public void setAwsMq(Boolean awsMq) {this.awsMq = awsMq;}
    public Boolean getAwsSns() {return awsSns;}
    public void setAwsSns(Boolean awsSns) { this.awsSns = awsSns;}
    public Boolean getAwsSes() { return awsSes;}
    public void setAwsSes(Boolean awsSes) {this.awsSes = awsSes;}
    public Boolean getAwsSqs() {return awsSqs;}
    public void setAwsSqs(Boolean awsSqs) { this.awsSqs = awsSqs; }
    public Boolean getAwsDyanmoDB() { return awsDyanmoDB; }
    public void setAwsDyanmoDB(Boolean awsDyanmoDB) { this.awsDyanmoDB = awsDyanmoDB; }
    public Boolean getAwsLambda() { return awsLambda;}
    public void setAwsLambda(Boolean awsLambda) { this.awsLambda = awsLambda;}


}
