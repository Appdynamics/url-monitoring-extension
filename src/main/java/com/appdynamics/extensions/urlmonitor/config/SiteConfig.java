package com.appdynamics.extensions.urlmonitor.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class SiteConfig extends SiteConfigBase
{
    private String name;
    private String url;
    private String username;
    private String password;
    private Map<String, String> headers = new HashMap<String, String>();
    private List<MatchPattern> matchPatterns = new ArrayList<MatchPattern>();
    private String requestPayloadFile;
    private String metricPrefix;

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

    @Override
    public String toString()
    {
        return "SiteConfig{" +
                "name='" + name + '\'' +
                '}';
    }
    public String getMetricPrefix() {
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }
}

