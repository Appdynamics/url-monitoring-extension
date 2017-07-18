package com.appdynamics.extensions.urlmonitor.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class SiteConfig extends SiteConfigBase
{
    private String groupName;
    private String name;
    private String url;
    private String username;
    private String password;
    private String passwordEncrypted;
    private String encryptionKey;
    private Map<String, String> headers = new HashMap<String, String>();
    private List<MatchPattern> matchPatterns = new ArrayList<MatchPattern>();
    private String requestPayloadFile;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPasswordEncrypted()
    {
        return passwordEncrypted;
    }

    public void setPasswordEncrypted(String passwordEncrypted)
    {
        this.passwordEncrypted = passwordEncrypted;
    }

    public String getEncryptionKey()
    {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey)
    {
        this.encryptionKey = encryptionKey;
    }

    public List<MatchPattern> getMatchPatterns() {
        return matchPatterns;
    }

    public void setMatchPatterns(List<MatchPattern> matchPatterns) {
        this.matchPatterns = matchPatterns;
    }

    public String getRequestPayloadFile() {
        return requestPayloadFile;
    }

    public void setRequestPayloadFile(String requestPayloadFile) {
        this.requestPayloadFile = requestPayloadFile;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString()
    {
        return "SiteConfig{" +
                "name='" + groupName +  '-' +name + '\'' +
                '}';
    }
}

