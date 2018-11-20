/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor.httpClient;

import com.appdynamics.extensions.urlmonitor.auth.AuthTypeEnum;
import com.appdynamics.extensions.urlmonitor.config.ClientConfig;
import com.appdynamics.extensions.urlmonitor.config.DefaultSiteConfig;
import com.appdynamics.extensions.urlmonitor.config.MonitorConfig;
import com.appdynamics.extensions.urlmonitor.config.ProxyConfig;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;


/**
 * Factory to create the suitable client object based on the authentication type
 */
public class ClientFactory {

    private static final Logger logger = Logger.getLogger(ClientFactory.class);

    /**
     * Creates the actual client with relevant properties
     * @param config
     * @param authType
     * @param sslContext
     * @return
     */
    public AsyncHttpClient createHttpClient(MonitorConfig config, String authType, SSLContext sslContext) {

        DefaultSiteConfig defaultSiteConfig = config.getDefaultParams();
        ClientConfig clientConfig = config.getClientConfig();


        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        try {
            builder.setAcceptAnyCertificate(clientConfig.isIgnoreSslErrors())
                    .setMaxRedirects(clientConfig.getMaxRedirects())
                    .setConnectTimeout(defaultSiteConfig.getConnectTimeout())
                    .setRequestTimeout(defaultSiteConfig.getSocketTimeout())
                    .setMaxConnectionsPerHost(clientConfig.getMaxConnPerRoute())
                    .setMaxConnections(clientConfig.getMaxConnTotal())
                    .setUserAgent(clientConfig.getUserAgent())
                    .setAcceptAnyCertificate(clientConfig.isIgnoreSslErrors())
                    .setSSLContext(AuthTypeEnum.SSL.name().equalsIgnoreCase(authType) ? sslContext : null);

            if(clientConfig.getEnabledProtocols()!=null) {
                String[] enabledProtocols = Iterables.toArray(Splitter.on(',').trimResults()
                        .omitEmptyStrings().split(clientConfig.getEnabledProtocols()), String.class);

                builder.setEnabledProtocols(enabledProtocols);
            }
            ProxyConfig proxyConfig = defaultSiteConfig.getProxyConfig();
            if (proxyConfig != null) {
                builder.setProxyServer(new ProxyServer(proxyConfig.getHost(), proxyConfig.getPort()));
            }
        }catch(Exception ex){
            logger.error("Error in HTTP client: " + ex.getMessage(), ex);
        }
        return new AsyncHttpClient(builder.build());
    }

}
