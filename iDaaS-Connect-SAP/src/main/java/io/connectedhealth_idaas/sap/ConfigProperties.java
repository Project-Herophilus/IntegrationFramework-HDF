/*
 * Copyright 2019 Red Hat, Inc.
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
package io.connectedhealth_idaas.sap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = "idaas")
public class ConfigProperties {

    private String kafkaBrokers;
    private String integrationTopic;

    // Public Cloud
    private String cloudTopic;
    private String cloudAPI;
    private Boolean processsPublicCloud;


    public String getKafkaBrokers() {
        return kafkaBrokers;
    }
    public void setKafkaBrokers(String kafkaBrokers) {
        this.kafkaBrokers = kafkaBrokers;
    }

    public String getIntegrationTopic() {return integrationTopic;}
    public void setIntegrationTopic(String integrationTopic) { this.integrationTopic = integrationTopic;}

    public String getCloudTopic() { return cloudTopic;}
    public void setCloudTopic(String cloudTopic) { this.cloudTopic = cloudTopic;}

    public String getCloudAPI() { return cloudAPI; }
    public void setCloudAPI(String cloudAPI) { this.cloudAPI = cloudAPI;}

    public Boolean getProcessPublicCloud() { return processPublicCloud;}
    public void setProcessPublicCloud(Boolean processPublicCloud) {this.processPublicCloud = processPublicCloud; }
}
