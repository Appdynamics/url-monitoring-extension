/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor.config;

@SuppressWarnings("unused")
public class SiteConfigBase
{
    String method;
    int socketTimeout = -1;
    int connectTimeout = -1;
    Boolean treatAuthFailedAsError;
    int numAttempts = -1;
    ProxyConfig proxyConfig;

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public int getSocketTimeout()
    {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout)
    {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectTimeout()
    {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout)
    {
        this.connectTimeout = connectTimeout;
    }

    public int getNumAttempts()
    {
        return numAttempts;
    }

    public void setNumAttempts(int numAttempts)
    {
        this.numAttempts = numAttempts;
    }

    public Boolean isTreatAuthFailedAsError()
    {
        return treatAuthFailedAsError;
    }

    public void setTreatAuthFailedAsError(Boolean treatAuthFailedAsError)
    {
        this.treatAuthFailedAsError = treatAuthFailedAsError;
    }

    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    public void setProxyConfig(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    @Override
    public String toString()
    {
        return "method='" + method + '\'' +
                ", socketTimeout=" + socketTimeout +
                ", connectTimeout=" + connectTimeout +
                ", numAttempts=" + numAttempts;
    }
}
