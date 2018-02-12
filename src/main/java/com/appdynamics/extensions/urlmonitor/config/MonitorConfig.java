/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor.config;

@SuppressWarnings("unused")
public class MonitorConfig
{
    private ClientConfig clientConfig;
    private DefaultSiteConfig defaultParams;
    private SiteConfig[] sites;
    private String metricPrefix;

    public ClientConfig getClientConfig()
    {
        return clientConfig;
    }

    public void setClientConfig(ClientConfig clientConfig)
    {
        this.clientConfig = clientConfig;
    }

    public DefaultSiteConfig getDefaultParams()
    {
        return defaultParams;
    }

    public void setDefaultParams(DefaultSiteConfig defaultParams)
    {
        this.defaultParams = defaultParams;
    }

    public SiteConfig[] getSites()
    {
        return sites;
    }

    public void setSites(SiteConfig[] sites)
    {
        this.sites = sites;
    }

    public int getTotalAttemptCount()
    {
        int total = 0;
        for (SiteConfig site : sites)
        {
            if (site.getNumAttempts() == -1) {
                total += getDefaultParams().getNumAttempts();
            } else {
                total += site.getNumAttempts();
            }
        }

        return total;
    }

    public int getSitesCount(){
        return sites.length;

    }

    public String getMetricPrefix() {
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }
}
