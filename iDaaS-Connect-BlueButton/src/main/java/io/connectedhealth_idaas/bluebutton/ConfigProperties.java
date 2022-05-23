package io.connectedhealth_idaas.bluebutton;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.attribute.FileTime;

@ConfigurationProperties(prefix = "idaas")
public class ConfigProperties {
    private String access_token;
    private double expires_in;
    private String token_type;
    private String scope;
    private String refresh_token;
    public String bluebuttonclientid;
    public String bluebuttonclientsecret;
    private String bluebuttonHost;
    public String bluebuttoncallbackpath;
    public String bluebuttoncallbackhostname;
    public int bluebuttoncallbackportnumber;
    private String kafkaBrokers;
    private String integrationTopic;
    private String terminologyTopic;
    private String processTerminologies;
    private String cloudTopic;
    private String cloudAPI;
    private Boolean processPublicCloud;

    // Getters
    public String getKafkaBrokers() {
        return kafkaBrokers;
    }
    public void setKafkaBrokers(String kafkaBrokers) { this.kafkaBrokers = kafkaBrokers;}

    public String getIntegrationTopic() {return integrationTopic;}
    public void setIntegrationTopic(String integrationTopic) { this.integrationTopic = integrationTopic;}

    public String getProcessTerminologies() {return processTerminologies;}
    public void setProcessTerminologies(String processTerminologies) { this.processTerminologies = processTerminologies;}

    public String getTerminologyTopic() {return terminologyTopic;}
    public void setTerminologyTopic(String terminologyTopic) { this.terminologyTopic = terminologyTopic;}

    public String getAccess_token() {
        return access_token;
    }
    public void setAccess_token(String access_token) {this.access_token = access_token;}

    public double getExpires_in() { return expires_in; }
    public void setExpires_in(double expires_in) {
        this.expires_in = expires_in;
    }

    public String getToken_type() {
        return token_type;
    }
    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getScope() {
        return scope;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRefresh_token() {
        return refresh_token;
    }
    public void setRefresh_token(String refresh_token) { this.refresh_token = refresh_token;}

    public String getBluebuttonclientid() { return bluebuttonclientid; }
    public void setBluebuttonclientid(String bluebuttonclientid) {
        this.bluebuttonclientid = bluebuttonclientid;
    }

    public String getBluebuttonclientsecret() { return bluebuttonclientsecret; }
    public void setBluebuttonclientsecret(String bluebuttonclientsecret) { this.bluebuttonclientsecret = bluebuttonclientsecret;}

    public String getBluebuttonHost() {
        return bluebuttonHost;
    }
    public void setBluebuttonHost(String bluebuttonHost) {
        this.bluebuttonHost = bluebuttonHost;
    }

    public String getBluebuttoncallbackpath() {
        return bluebuttoncallbackpath;
    }
    public void setBluebuttoncallbackpath(String bluebuttoncallbackpath) { this.bluebuttoncallbackpath = bluebuttoncallbackpath;}

    public String getBluebuttoncallbackhostname() {
        return bluebuttoncallbackhostname;
    }
    public void setBluebuttoncallbackhostname(String bluebuttoncallbackhostname1) {this.bluebuttoncallbackhostname = bluebuttoncallbackhostname1;}


    public int getBluebuttoncallbackportnumber() {
        return bluebuttoncallbackportnumber;
    }
    public void setBluebuttoncallbackportnumber(int bluebuttoncallbackportnumber) {this.bluebuttoncallbackportnumber = bluebuttoncallbackportnumber;}

    public String getCloudTopic() { return cloudTopic;}
    public void setCloudTopic(String cloudTopic) { this.cloudTopic = cloudTopic;}

    public String getCloudAPI() { return cloudAPI; }
    public void setCloudAPI(String cloudAPI) { this.cloudAPI = cloudAPI;}

    public Boolean getProcessPublicCloud() { return processPublicCloud;}
    public void setProcessPublicCloud(Boolean processPublicCloud) {this.processPublicCloud = processPublicCloud; }
}
