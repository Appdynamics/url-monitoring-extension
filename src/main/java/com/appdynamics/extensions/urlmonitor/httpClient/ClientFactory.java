/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor.httpClient;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.urlmonitor.auth.AuthTypeEnum;
import com.appdynamics.extensions.urlmonitor.config.ClientConfig;
import com.appdynamics.extensions.urlmonitor.config.DefaultSiteConfig;
import com.appdynamics.extensions.urlmonitor.config.ProxyConfig;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import io.netty.handler.ssl.SslContext;

import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;

import org.asynchttpclient.proxy.ProxyServer;
import org.slf4j.Logger;


/**
 * Factory to create the suitable client object based on the authentication type
 */
public class ClientFactory {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ClientFactory.class);

    /**
     * Creates the actual client with relevant properties
     * @param clientConfig
     * @param defaultSiteConfig
     * @param authType
     * @param sslContext
     * @return
     */
    public DefaultAsyncHttpClient createHttpClient(ClientConfig clientConfig, DefaultSiteConfig defaultSiteConfig, String authType, SslContext sslContext) {

        DefaultAsyncHttpClientConfig.Builder builder = Dsl.config();

        try {
            builder.setUseInsecureTrustManager(clientConfig.isIgnoreSslErrors())
                    .setMaxRedirects(clientConfig.getMaxRedirects())
                    .setConnectTimeout(defaultSiteConfig.getConnectTimeout())
                    .setRequestTimeout(defaultSiteConfig.getSocketTimeout())
                    .setMaxConnectionsPerHost(clientConfig.getMaxConnPerRoute())
                    .setMaxConnections(clientConfig.getMaxConnTotal())
                    .setUserAgent(clientConfig.getUserAgent())
                    .setSslContext(AuthTypeEnum.SSL.name().equalsIgnoreCase(authType) ? sslContext : null);

            if(clientConfig.getEnabledProtocols()!=null) {
                String[] enabledProtocols = Iterables.toArray(Splitter.on(',').trimResults()
                        .omitEmptyStrings().split(clientConfig.getEnabledProtocols()), String.class);

                builder.setEnabledProtocols(enabledProtocols);
            }


            ProxyConfig proxyConfig = defaultSiteConfig.getProxyConfig();
            if (proxyConfig != null) {
                builder.setProxyServer(new ProxyServer.Builder(proxyConfig.getHost(),proxyConfig.getPort()).build());
            }
        }catch(Exception ex){
            logger.error("Error in HTTP client: " + ex.getMessage(), ex);
        }
        return new DefaultAsyncHttpClient(builder.build());
    }

}
