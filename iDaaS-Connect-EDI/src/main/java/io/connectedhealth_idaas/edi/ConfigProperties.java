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
package io.connectedhealth_idaas.edi;

import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = "idaas")
public class ConfigProperties {

    //Kafka
    private String kafkaBrokers;
    private String integrationTopic;
    private String terminologyTopic;
    //General
    private String processTerminologies;
    private String public_cloud;
    // Specific Topics
    private String topicName270;
    private String topicName271;
    private String topicName276;
    private String topicName277;
    private String topicName834;
    private String topicName835;
    private String topicName837;
    // Directory Outputs
    // Specific Topics
    private String topicName270_Output;
    private String topicName271_Output;
    private String topicName276_Output;
    private String topicName277_Output;
    private String topicName834_Output;
    private String topicName835_Output;
    private String topicName837_Output;

    public String getKafkaBrokers() {
        return kafkaBrokers;
    }
    public void setKafkaBrokers(String kafkaBrokers) {
        this.kafkaBrokers = kafkaBrokers;
    }

    public String getProcessTerminologies() {return processTerminologies;}
    public void setProcessTerminologies(String processTerminologies) { this.processTerminologies = processTerminologies;}

    // Topic
    public String getIntegrationTopic() {return integrationTopic;}
    public void setIntegrationTopic(String integrationTopic) { this.integrationTopic = integrationTopic;}
    public String getTerminologyTopic() {return terminologyTopic;}
    public void setTerminologyTopic(String terminologyTopic) { this.terminologyTopic = terminologyTopic;}
    public String getTopicName270() {
        return topicName270;
    }
    public void setTopicName270(String topicName270) {
        this.topicName270 = topicName270;
    }
    public String getTopicName271() {
        return topicName271;
    }
    public void setTopicName271(String topicName271) {
        this.topicName271 = topicName271;
    }
    public String getTopicName276() {
        return topicName276;
    }
    public void setTopicName276(String topicName276) {
        this.topicName276 = topicName276;
    }
    public String getTopicName277() {
        return topicName277;
    }
    public void setTopicName277(String topicName277) {
        this.topicName277 = topicName277;
    }
    public String getTopicName834() {
        return topicName834;
    }
    public void setTopicName834(String topicName834) {
        this.topicName834 = topicName834;
    }
    public String getTopicName835() {
        return topicName835;
    }
    public void setTopicName835(String topicName835) {
        this.topicName835 = topicName835;
    }
    public String getTopicName837() {
        return topicName837;
    }
    public void setTopicName837(String topicName837) {
        this.topicName837 = topicName837;
    }

    // Output Directories
    public String getTopicName270_Output() {
        return topicName270_Output;
    }
    public void setTopicName270_Output(String topicName270_Output) {
        this.topicName270_Output = topicName270_Output;
    }
    public String getTopicName271_Output() {
        return topicName271_Output;
    }
    public void setTopicName271_Output(String topicName271_Output) {
        this.topicName271_Output = topicName271_Output;
    }
    public String getTopicName276_Output() {
        return topicName276_Output;
    }
    public void setTopicName276_Output(String topicName276_Output) {
        this.topicName276_Output = topicName276_Output;
    }
    public String getTopicName277_Output() {
        return topicName277_Output;
    }
    public void setTopicName277_Output(String topicName277_Output) {
        this.topicName277_Output = topicName277_Output;
    }
    public String getTopicName834_Output() { return topicName834_Output; }
    public void setTopicName834_Output(String topicName834_Output) {
        this.topicName834_Output = topicName834_Output;
    }
    public String getTopicName835_Output() {
        return topicName835_Output;
    }
    public void setTopicName835_Output(String topicName835_Output) {
        this.topicName835_Output = topicName835_Output;
    }
    public String getTopicName837_Output() {
        return topicName837_Output;
    }
    public void setTopicName837_Output(String topicName837_Output) {
        this.topicName837_Output = topicName837_Output;
    }

    public String getPublic_cloud() { return public_cloud;}
    public void setPublic_cloud(String public_cloud) { this.public_cloud = public_cloud;}
}