    /*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor.config;

import com.appdynamics.extensions.urlmonitor.auth.AuthTypeEnum;

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
    private boolean usePreemptiveAuth = false;
    private String encryptedPassword;
    private String encryptionKey;
    private Map<String, String> headers = new HashMap<String, String>();
    private List<MatchPattern> matchPatterns = new ArrayList<MatchPattern>();
    private String requestPayloadFile;
    private String authType;
    private String keyStoreType;
    private String keyStorePath;
    private String keyStorePassword;
    private String trustStorePath;
    private String trustStorePassword;
    private boolean followRedirects = true;

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

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
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

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public boolean isFollowRedirects()
    {
        return followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects)
    {
        this.followRedirects = followRedirects;
    }

    public boolean getUsePreemptiveAuth()
    {
        return usePreemptiveAuth;
    }

    public void setUsePreemptiveAuth(boolean usePreemptiveAuth)
    {
        this.usePreemptiveAuth = usePreemptiveAuth;
    }


    @Override
    public String toString()
    {
        return "SiteConfig{" +
                "name='" + groupName +  '-' +name + '\'' +
                '}';
    }
}

