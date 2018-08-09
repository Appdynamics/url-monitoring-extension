/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor.config;

@SuppressWarnings("unused")
public class ClientConfig
{
    private int maxConnTotal = 1000;
    private int maxConnPerRoute = 1000;
    private int threadCount = 10;
    private boolean ignoreSslErrors = false;
    private int maxRedirects = 10;
    private String enabledProtocols;
    private String userAgent = "Mozilla/5.0 (compatible; AppDynamics UrlMonitor; http://www.appdynamics.com/)";

    public int getThreadCount()
    {
        return threadCount;
    }

    public void setThreadCount(int threadCount)
    {
        this.threadCount = threadCount;
    }

    public int getMaxConnTotal()
    {
        return maxConnTotal;
    }

    public void setMaxConnTotal(int maxConnTotal)
    {
        this.maxConnTotal = maxConnTotal;
    }

    public int getMaxConnPerRoute()
    {
        return maxConnPerRoute;
    }

    public void setMaxConnPerRoute(int maxConnPerRoute)
    {
        this.maxConnPerRoute = maxConnPerRoute;
    }

    public boolean isIgnoreSslErrors()
    {
        return ignoreSslErrors;
    }

    public void setIgnoreSslErrors(boolean ignoreSslErrors)
    {
        this.ignoreSslErrors = ignoreSslErrors;
    }

    public int getMaxRedirects()
    {
        return maxRedirects;
    }

    public void setMaxRedirects(int maxRedirects)
    {
        this.maxRedirects = maxRedirects;
    }

    public String getUserAgent() { return userAgent; }

    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getEnabledProtocols() {
        return enabledProtocols;
    }

    public void setEnabledProtocols(String enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
    }

    @Override
    public String toString()
    {
        return "maxConnTotal=" + maxConnTotal +
                ", maxConnPerRoute=" + maxConnPerRoute +
                ", ignoreSslErrors=" + ignoreSslErrors;
    }
}
