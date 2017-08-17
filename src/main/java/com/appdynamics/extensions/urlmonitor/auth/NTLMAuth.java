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
    private String encryptedPassword;
    private String encryptionKey;
    private String url;
    private String domain;
    private String host;

    private static final Logger log = Logger.getLogger(NTLMAuth.class);

    public NTLMAuth(String username, String password, String url, String encryptedPassword, String encryptionKey) {
        this.username = username;
        this.password = password;
        this.encryptedPassword = encryptedPassword;
        this.encryptionKey = encryptionKey;
        this.url = url;
    }

    public String getUsername() {
        return username;
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

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    /**
     * Method to create RealmBuilder for NTLM authentication
     * @return
     */
    public RealmBuilder realmBuilderBase() {

        setHostAndDomain(getUrl());

        return new Realm.RealmBuilder()
                .setScheme(AuthScheme.NTLM)
                .setNtlmDomain(getDomain())
                .setNtlmHost(getHost())
                .setPrincipal(getUsername())
                .setPassword(AuthSchemeFactory.getPassword(getPassword(),getEncryptedPassword(),getEncryptionKey()));
    }

    /**
     * Extracts and sets hostname and domain from the url
     * @param url
     */
    private void setHostAndDomain(String url) {
        String hostname = null;
        try {
            URI uri = new URI(url);
            hostname = uri.getHost();
            if (hostname != null) {
                setHost(hostname.startsWith("www.") ? hostname.substring(4) : hostname);
            }
            int beginIndex = url.indexOf('/');
            int lastIndex = url.lastIndexOf(':');
            if(beginIndex < lastIndex)
                setDomain(url.substring(url.indexOf('/') + 2, url.lastIndexOf(':')));
            else
                setDomain("");
        } catch (URISyntaxException e) {
            log.error("Exception while setting host and domain for url " + url, e);
        }
    }
}
