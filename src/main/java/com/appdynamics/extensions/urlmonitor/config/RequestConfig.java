/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor.config;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.urlmonitor.auth.AuthTypeEnum;
import com.appdynamics.extensions.urlmonitor.auth.SSLCertAuth;
import com.appdynamics.extensions.urlmonitor.httpClient.ClientFactory;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import io.netty.handler.ssl.SslContext;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper Class containing SiteConfig and the related Client object
 */

public class RequestConfig {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(RequestConfig.class);

    private SiteConfig siteConfig;

    private DefaultAsyncHttpClient client;

    public SiteConfig getSiteConfig() {
        return siteConfig;
    }

    public void setSiteConfig(SiteConfig siteConfig) {
        this.siteConfig = siteConfig;
    }

    public DefaultAsyncHttpClient getClient() {
        return client;
    }

    public void setClient(DefaultAsyncHttpClient client) {
        this.client = client;
    }

    private DefaultAsyncHttpClient defaultClient = null;

    public List<RequestConfig> setClientForSite(ClientConfig clientConfig, DefaultSiteConfig defaultSiteConfig, List<SiteConfig> siteConfig) throws TaskExecutionException {

        List<RequestConfig> requestConfigList = new ArrayList<RequestConfig>();

        try {
            for (SiteConfig site : siteConfig) {
                RequestConfig requestConfig = new RequestConfig();
                requestConfig.setSiteConfig(site);

                try {
                    if (AuthTypeEnum.SSL.name().equalsIgnoreCase(site.getAuthType())) {
                        if (site.getTrustStorePassword() != null && site.getTrustStorePath() != null) {
                            System.setProperty("javax.net.ssl.trustStore", site.getTrustStorePath());
                            System.setProperty("javax.net.ssl.trustStorePassword", site.getTrustStorePassword());
                        }
                        SslContext sslContext = new SSLCertAuth().getSSLContext(site.getKeyStorePath(), site.getKeyStoreType(), site.getKeyStorePassword());
                        requestConfig.setClient(new ClientFactory().createHttpClient(clientConfig, defaultSiteConfig, AuthTypeEnum.SSL.name(), sslContext));


                    } else {
                        if (defaultClient == null || defaultClient.isClosed()) {
                            defaultClient = new ClientFactory().createHttpClient(clientConfig, defaultSiteConfig, AuthTypeEnum.NONE.name(), null);
                        }
                        requestConfig.setClient(defaultClient);
                    }

                } catch (Exception ex) {
                    logger.error("Error occurred for site: " + site.getName() + " and for uri: " + site.getUrl(), ex);
                }

                requestConfigList.add(requestConfig);
            }
        } catch (Exception e) {
            throw new TaskExecutionException(e);
        }

        return requestConfigList;
    }

    public void closeClients(List<RequestConfig> configList) {
        for (RequestConfig requestConfig : configList) {
            if (requestConfig.getClient() != null && !(requestConfig.getClient().isClosed())) {
                requestConfig.getClient().close();
            }
        }
    }
}
