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

    //getters
    public String getAccess_token() {
        return access_token;
    }
    public double getExpires_in() { return expires_in; }
    public String getToken_type() {
        return token_type;
    }
    public String getScope() {
        return scope;
    }
    public String getRefresh_token() {
        return refresh_token;
    }
    public String getBluebuttonclientid() { return bluebuttonclientid; }
    public String getBluebuttonclientsecret() { return bluebuttonclientsecret; }
    public String getBluebuttonHost() {
        return bluebuttonHost;
    }
    public String getBluebuttoncallbackpath() {
        return bluebuttoncallbackpath;
    }
    public String getBluebuttoncallbackhostname() {
        return bluebuttoncallbackhostname;
    }
    public int getBluebuttoncallbackportnumber() {
        return bluebuttoncallbackportnumber;
    }

    //setters
    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
    public void setExpires_in(double expires_in) {
        this.expires_in = expires_in;
    }
    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }
    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }
    public void setBluebuttonclientid(String bluebuttonclientid) {
        this.bluebuttonclientid = bluebuttonclientid;
    }
    public void setBluebuttonclientsecret(String bluebuttonclientsecret) {
        this.bluebuttonclientsecret = bluebuttonclientsecret;
    }
    public void setBluebuttonHost(String bluebuttonHost) {
        this.bluebuttonHost = bluebuttonHost;
    }
    public void setBluebuttoncallbackpath(String bluebuttoncallbackpath) {
        this.bluebuttoncallbackpath = bluebuttoncallbackpath;
    }
    public void setBluebuttoncallbackhostname(String bluebuttoncallbackhostname1) {
        this.bluebuttoncallbackhostname = bluebuttoncallbackhostname1;
    }
    public void setBluebuttoncallbackportnumber(int bluebuttoncallbackportnumber) {
        this.bluebuttoncallbackportnumber = bluebuttoncallbackportnumber;
    }

}
