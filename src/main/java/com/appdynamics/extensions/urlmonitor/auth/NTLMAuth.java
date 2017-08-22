package com.appdynamics.extensions.urlmonitor.auth;

import com.ning.http.client.Realm;
import com.ning.http.client.Realm.AuthScheme;
import com.ning.http.client.Realm.RealmBuilder;
import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

public class NTLMAuth {

    private String username;
    private String password;
    private String url;
    private String domain;
    private String host;

    private static final Logger log = Logger.getLogger(NTLMAuth.class);

    public NTLMAuth(String username, String password, String url) {
        this.username = username;
        this.password = password;
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Method to create RealmBuilder for NTLM authentication
     * @return
     */
    public RealmBuilder realmBuilderBase() {

        setHostAndDomain(getUrl(),getUsername());

        return new Realm.RealmBuilder()
                .setScheme(AuthScheme.NTLM)
                .setNtlmDomain(getDomain())
                .setNtlmHost(getHost())
                .setPrincipal(getUsername())
                .setPassword(getPassword());
    }

    /**
     * Extracts and sets hostname and domain from the url
     * @param url
     */
    private void setHostAndDomain(String url, String username) {
        String hostname = null;
        try {
            URI uri = new URI(url);
            hostname = uri.getHost();
            if (hostname != null) {
                setHost(hostname.startsWith("www.") ? hostname.substring(4) : hostname);
            }
            //User can provide domain/username in two separate formats
            // Either as DOMAIN user or user@DOMAIN
            int separator1Index = username.indexOf("\\");
            int separator2Index = username.indexOf("@");

            if(separator1Index>0) {
                setDomain(username.substring(0, separator1Index));
                setUsername(username.substring(separator1Index+1));
            }
            if(separator2Index>0) {
                setDomain(username.substring(separator2Index + 1));
                setUsername(username.substring(0,separator2Index-1));
            }
        } catch (URISyntaxException e) {
            log.error("Exception while setting host and domain for url " + url, e);
        }
    }
}
