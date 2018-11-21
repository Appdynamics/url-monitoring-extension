/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor.config;

import com.appdynamics.extensions.urlmonitor.auth.AuthTypeEnum;
import com.appdynamics.extensions.urlmonitor.auth.SSLCertAuth;
import com.appdynamics.extensions.urlmonitor.httpClient.ClientFactory;
import com.ning.http.client.AsyncHttpClient;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper Class containing SiteConfig and the related Client object
 */

public class RequestConfig {

    private SiteConfig siteConfig;
    private AsyncHttpClient client;

    public SiteConfig getSiteConfig() {
        return siteConfig;
    }

    public void setSiteConfig(SiteConfig siteConfig) {
        this.siteConfig = siteConfig;
    }

    public AsyncHttpClient getClient() {
        return client;
    }

    public void setClient(AsyncHttpClient client) {
        this.client = client;
    }

    private AsyncHttpClient defaultClient = null;

    public List<RequestConfig> setClientForSite(MonitorConfig config, SiteConfig[] siteConfig) throws TaskExecutionException{

        List<RequestConfig> requestConfigList = new ArrayList<RequestConfig>();

        try {
            for (SiteConfig site : siteConfig) {
                RequestConfig requestConfig = new RequestConfig();
                requestConfig.setSiteConfig(site);
                if (site.getAuthType().equalsIgnoreCase(AuthTypeEnum.SSL.name())) {
                    if(site.getTrustStorePassword()!=null && site.getTrustStorePath()!=null) {
                        System.setProperty("javax.net.ssl.trustStore", site.getTrustStorePath());
                        System.setProperty("javax.net.ssl.trustStorePassword", site.getTrustStorePassword());
                    }
                    SSLContext sslContext = new SSLCertAuth().getSSLContext(site.getKeyStorePath(), site.getKeyStoreType(), site.getKeyStorePassword());
                    requestConfig.setClient(new ClientFactory().createHttpClient(config, AuthTypeEnum.SSL.name(), sslContext));
                } else {
                    if (defaultClient == null || defaultClient.isClosed()) {
                        defaultClient = new ClientFactory().createHttpClient(config, AuthTypeEnum.NONE.name(), null);
                    }
                    requestConfig.setClient(defaultClient);
                }
                requestConfigList.add(requestConfig);
            }
        }catch(Exception e){
            throw new TaskExecutionException(e);
        }

        return requestConfigList;
    }

    public void closeClients(List<RequestConfig> configList){
        for(RequestConfig requestConfig : configList){
            if(!(requestConfig.getClient().isClosed())){
                requestConfig.getClient().close();
            }
        }
    }
}
