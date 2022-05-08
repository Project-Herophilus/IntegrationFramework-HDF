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
package io.connectedhealth_idaas.hl7;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "idaas")
public class ConfigProperties {
    //Variables
    //General
    private String convertCCDAtoFHIR;
    private String convertHL7toFHIR;
    private String processTerminologies;

    // Kafka
    private String kafkaBrokers;
    private String integrationTopic;
    private String appintegrationTopic;
    private String terminologyTopic;
    private String ccdaConversionTopic;


    //HL7 Ports
    private int adtPort;
    private int ormPort;
    private int oruPort;
    private int rdePort;
    private int mfnPort;
    private int mdmPort;
    private int schPort;
    private int vxuPort;

    // HL7 Directories
    private String hl7ADT_Directory;
    private String hl7ORM_Directory;
    private String hl7ORU_Directory;
    private String hl7MFN_Directory;
    private String hl7MDM_Directory;
    private String hl7RDE_Directory;
    private String hl7SCH_Directory;
    private String hl7VXU_Directory;
    private String hl7CCDA_Directory;

    // Platform Topics
    private String adtTopicName;
    private String ormTopicName;
    private String oruTopicName;
    private String mfnTopicName;
    private String mdmTopicName;
    private String rdeTopicName;
    private String schTopicName;
    private String vxuTopicName;
    private String ccdaTopicName;
    private String hl7HTTPTopicName;

    // Getters and Setters by Usage
    public String getConvertCCDAtoFHIR() {return convertCCDAtoFHIR;}
    public void setConvertCCDAtoFHIR(String convertCCDAtoFHIR) { this.convertCCDAtoFHIR = convertCCDAtoFHIR;}
    public String getConvertHL7toFHIR() {return convertHL7toFHIR;}
    public void setConvertHL7toFHIR(String convertHL7toFHIR) { this.convertHL7toFHIR = convertHL7toFHIR;}
    public String getProcessTerminologies() {return processTerminologies;}
    public void setProcessTerminologies(String processTerminologies) { this.processTerminologies = processTerminologies;}

    public String getKafkaBrokers() {return kafkaBrokers;}
    public void setKafkaBrokers(String kafkaBrokers) { this.kafkaBrokers = kafkaBrokers;}
    public String getIntegrationTopic() {return integrationTopic;}
    public void setIntegrationTopic(String integrationTopic) { this.integrationTopic = integrationTopic;}
    public String getAppintegrationTopic() {return appintegrationTopic;}
    public void setAppintegrationTopic(String appintegrationTopic) { this.appintegrationTopic = appintegrationTopic;}
    public String getTerminologyTopic() {return terminologyTopic;}
    public void setTerminologyTopic(String terminologyTopic) { this.terminologyTopic = terminologyTopic;}
    public String getCcdaConversionTopic() { return ccdaConversionTopic; }
    public void setCcdaConversionTopic(String ccdaConversionTopic) { this.ccdaConversionTopic = ccdaConversionTopic; }

    public int getAdtPort() { return adtPort;}
    public void setAdtPort(int adtPort) { this.adtPort = adtPort;}
    public String getHl7ADT_Directory() {
        return hl7ADT_Directory;
    }
    public void setHl7ADT_Directory(String hl7ADT_Directory) {
        this.hl7ADT_Directory = hl7ADT_Directory;
    }
    public String getadtTopicName() {
        return adtTopicName;
    }
    public void setAdtTopicName(String adtTopicName) {
        this.adtTopicName = adtTopicName;
    }

    public int getMdmPort() {
        return mdmPort;
    }
    public void setMdmPort(int mdmPort) {
        this.mdmPort = mdmPort;
    }
    public String getHl7MDM_Directory() {
        return hl7MDM_Directory;
    }
    public void setHl7MDM_Directory(String hl7MDM_Directory) {
        this.hl7MDM_Directory = hl7MDM_Directory;
    }
    public String getMdmTopicName() {
        return mdmTopicName;
    }
    public void setMdmTopicName(String mdmTopicName) {
        this.mdmTopicName = mdmTopicName;
    }

    public int getMfnPort() {
        return mfnPort;
    }
    public void setMfnPort(int mfnPort) {
        this.mfnPort = mfnPort;
    }
    public String getHl7MFN_Directory() {
        return hl7MFN_Directory;
    }
    public void setHl7MFN_Directory(String hl7MFN_Directory) {
        this.hl7MFN_Directory = hl7MFN_Directory;
    }
    public String getMfnTopicName() {
        return mfnTopicName;
    }
    public void setMfnTopicName(String mfnTopicName) {
        this.mfnTopicName = mfnTopicName;
    }

    public int getOrmPort() {
        return ormPort;
    }
    public void setOrmPort(int ormPort) {
        this.ormPort = ormPort;
    }
    public String getHl7ORM_Directory() {
        return hl7ORM_Directory;
    }
    public void setHl7ORM_Directory(String hl7ORM_Directory) {
        this.hl7ORM_Directory = hl7ORM_Directory;
    }
    public String getOrmTopicName() {
        return ormTopicName;
    }
    public void setOrmTopicName(String ormTopicName) {
        this.ormTopicName = ormTopicName;
    }

    public int getOruPort() {
        return oruPort;
    }
    public void setOruPort(int oruPort) {
        this.oruPort = oruPort;
    }
    public String getHl7ORU_Directory() {
        return hl7ORU_Directory;
    }
    public void setHl7ORU_Directory(String hl7ORU_Directory) {
        this.hl7ORU_Directory = hl7ORU_Directory;
    }
    public String getOruTopicName() {
        return oruTopicName;
    }
    public void setOruTopicName(String oruTopicName) {
        this.oruTopicName = oruTopicName;
    }

    public int getRdePort() {
        return rdePort;
    }
    public void setRdePort(int rdePort) {
        this.rdePort = rdePort;
    }
    public String getHl7RDE_Directory() {
        return hl7RDE_Directory;
    }
    public void setHl7RDE_Directory(String hl7RDE_Directory) {
        this.hl7RDE_Directory = hl7RDE_Directory;
    }
    public String getRdeTopicName() {
        return rdeTopicName;
    }
    public void setRdeTopicName(String rdeTopicName) {
        this.rdeTopicName = rdeTopicName;
    }

    public int getSchPort() {
        return schPort;
    }
    public void setSchPort(int schPort) {
        this.schPort = schPort;
    }
    public String getHl7SCH_Directory() {
        return hl7SCH_Directory;
    }
    public void setHl7SCH_Directory(String hl7SCH_Directory) {
        this.hl7SCH_Directory = hl7SCH_Directory;
    }
    public String getSchTopicName() {
        return schTopicName;
    }
    public void setSchTopicName(String schTopicName) {
        this.schTopicName = schTopicName;
    }

    public int getVxuPort() {
        return vxuPort;
    }
    public void setVxuPort(int vxuPort) {
        this.vxuPort = vxuPort;
    }
    public String getHl7VXU_Directory() {
        return hl7VXU_Directory;
    }
    public void setHl7VXU_Directory(String hl7VXU_Directory) {
        this.hl7VXU_Directory = hl7VXU_Directory;
    }
    public String getVxuTopicName() {
        return vxuTopicName;
    }
    public void setVxuTopicName(String vxuTopicName) {
        this.vxuTopicName = vxuTopicName;
    }

    public String getHl7CCDA_Directory() {
        return hl7CCDA_Directory;
    }
    public void setHl7CCDA_Directory(String hl7CCDA_Directory) {
        this.hl7CCDA_Directory = hl7CCDA_Directory;
    }
    public String getCcdaTopicName() {
        return ccdaTopicName;
    }
    public void setCcdaTopicName(String ccdaTopicName) {
        this.ccdaTopicName = ccdaTopicName;
    }
    public String getHl7HTTPTopicName() { return hl7HTTPTopicName; }
    public void setHl7HTTPTopicName(String hl7HTTPTopicName) { this.hl7HTTPTopicName = hl7HTTPTopicName; }


}
