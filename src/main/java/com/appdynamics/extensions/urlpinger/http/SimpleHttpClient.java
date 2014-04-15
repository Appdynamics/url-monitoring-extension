package com.appdynamics.extensions.urlpinger.http;

import com.google.common.base.Strings;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

/**
 * SimpleHttpClient that serves as a wrapper for Apache httpClient
 */
public class SimpleHttpClient {

    public static final String HTTPS = "https";
    public static final String HTTP = "http";
    public static final int HTTPS_PORT = 443;
    public static final int HTTP_PORT = 80;
    public static final int DEFAULT_TIMEOUT_MS = 10000;

    SimpleHttpConfig _config;
    CloseableHttpClient _httpClient;


    public SimpleHttpClient(SimpleHttpConfig config){
        this._config = config;
        _httpClient = buildHttpClient();
    }

    public SimpleHttpClient(SimpleHttpConfig config,HttpClientBuilder builder){
        this._config = config;
        _httpClient = builder.build();
    }

    /**
     * Returns a HTTPResponse by performing a GET operation on the given url.
     * @param url
     * @return
     * @throws Exception
     */
    public HttpResponse get(String url) throws Exception {
        if(Strings.isNullOrEmpty(url)){
            return null;
        }
        HttpHost target = getHttpHost(url,null);
        HttpGet httpGet = new HttpGet("/");
        RequestConfig requestConfig = buildRequestConfig();
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            response = _httpClient.execute(target, httpGet);
        }
        finally{
            if(response != null) {
                response.close();
            }
        }
        return response;
    }

    public void close() throws IOException {
        if(_httpClient != null){
            _httpClient.close();
        }
    }
    private CloseableHttpClient buildHttpClient() {
        HttpClientBuilder builder = HttpClients.custom();
        setUpProxyCreds(builder);
        return builder.build();
    }

    private void setUpProxyCreds(HttpClientBuilder builder) {
        if(areProxyParamsAvailable()){
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(_config.getProxyHost(), Integer.parseInt(_config.getProxyPort())),
                    new UsernamePasswordCredentials(_config.getProxyUser(), _config.getProxyPassword()));
           builder.setDefaultCredentialsProvider(credsProvider);
        }
    }

    private boolean areProxyParamsAvailable() {
        return !Strings.isNullOrEmpty(_config.getProxyUser()) &&
                !Strings.isNullOrEmpty(_config.getProxyPassword()) &&
                !Strings.isNullOrEmpty(_config.getProxyHost()) &&
                !Strings.isNullOrEmpty(_config.getProxyPort());
    }


    private HttpHost getHttpHost(String url,Integer port) {
        boolean isSSL = isSSLEnabled(url);
        String host = parseHost(url);
        port = port != null ? port : isSSL ? HTTPS_PORT : HTTP_PORT;
        String protocol = isSSL ? HTTPS : HTTP;
        return new HttpHost(host, port, protocol);
    }




    private RequestConfig buildRequestConfig() {
        RequestConfig.Builder builder = RequestConfig.custom();
        if(!Strings.isNullOrEmpty(_config.getProxyHost()) && !Strings.isNullOrEmpty(_config.getProxyPort())){
            HttpHost proxy = getHttpHost(_config.getProxyHost(),Integer.parseInt(_config.getProxyPort()));
            builder.setProxy(proxy);
        }

        builder.setConnectTimeout(_config.getConnectionTimeout() != null ? _config.getConnectionTimeout() : DEFAULT_TIMEOUT_MS);
        builder.setSocketTimeout(_config.getSocketTimeout() != null ? _config.getSocketTimeout() : DEFAULT_TIMEOUT_MS);
        return builder.build();
    }

    private String parseHost(String url) {
        if(url.startsWith(HTTPS)){
            return url.substring(8); //accounting for ://
        }
        else if(url.startsWith("http")){
            return url.substring(7); //accounting for ://
        }
        return url;
    }


    private boolean isSSLEnabled(String url) {
        if(url.startsWith(HTTPS)){
            return true;
        }
        return false;
    }

}
