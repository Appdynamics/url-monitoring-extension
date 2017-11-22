package com.appdynamics.extensions.urlmonitor.httpClient;

import com.appdynamics.extensions.urlmonitor.auth.AuthTypeEnum;
import com.appdynamics.extensions.urlmonitor.auth.SSLCertAuth;
import com.appdynamics.extensions.urlmonitor.config.ClientConfig;
import com.appdynamics.extensions.urlmonitor.config.DefaultSiteConfig;
import com.appdynamics.extensions.urlmonitor.config.MonitorConfig;
import com.appdynamics.extensions.urlmonitor.config.ProxyConfig;
import com.appdynamics.extensions.urlmonitor.config.SiteConfig;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;


/**
 * Factory to create the suitable client object based on the authentication type
 */
public class ClientFactory {

    private static final Logger logger = Logger.getLogger(ClientFactory.class);

    /**
     * For every SSL connection old client is closed and a new one is created, otherwise old connectionis returned
     * @param config
     * @param siteConfig
     * @param client
     * @return
     */
    public AsyncHttpClient getValidClient(MonitorConfig config, SiteConfig siteConfig, AsyncHttpClient client) throws TaskExecutionException{

        switch (AuthTypeEnum.valueOf(siteConfig.getAuthType())) {
            case SSL:
                closeClient(AuthTypeEnum.SSL, client);
                System.setProperty("javax.net.ssl.trustStore", siteConfig.getTrustStorePath());
                System.setProperty("javax.net.ssl.trustStorePassword", siteConfig.getTrustStorePassword());
                SSLContext sslContext = new SSLCertAuth().getSSLContext(siteConfig.getKeyStorePath(), siteConfig.getKeyStoreType(), siteConfig.getPassword());
                client = createHttpClient(config, AuthTypeEnum.SSL.name(), sslContext);
                break;
            default:
                if (client.isClosed()) {
                    closeClient(AuthTypeEnum.NONE, client);
                    client = createHttpClient(config, AuthTypeEnum.SSL.name(), null);
                }

        }
        logger.debug(String.format("Client %s created successfully for authType %s",client,siteConfig.getAuthType()));
        return client;
    }

    public void closeClient(AuthTypeEnum authType, AsyncHttpClient client){
        switch (authType){
            case SSL:
                client.close();
                break;
            default:
                if(client.getConfig().getSSLContext()!=null){
                    client.close();
                }
                logger.debug("Client need not be closed, conitnuing with same client");
                break;
        }
    }

    /**
     * Creates the actual client with relevant properties
     * @param config
     * @param authType
     * @param sslContext
     * @return
     */
    public AsyncHttpClient createHttpClient(MonitorConfig config, String authType, SSLContext sslContext) throws TaskExecutionException{

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

            ProxyConfig proxyConfig = defaultSiteConfig.getProxyConfig();
            if (proxyConfig != null) {
                builder.setProxyServer(new ProxyServer(proxyConfig.getHost(), proxyConfig.getPort()));
            }
        }catch(Exception ex){
            logger.error("Error in HTTP client: " + ex.getMessage(), ex);
            throw new TaskExecutionException(ex);
        }
        return new AsyncHttpClient(builder.build());
    }

}
