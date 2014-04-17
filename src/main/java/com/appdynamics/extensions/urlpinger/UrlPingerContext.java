package com.appdynamics.extensions.urlpinger;


import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.http.SimpleHttpClientBuilder;
import com.appdynamics.extensions.urlpinger.jaxb.JAXBProvider;
import com.appdynamics.extensions.urlpinger.jaxb.MonitorUrls;
import com.google.common.base.Strings;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.Map;


public class UrlPingerContext {

    public static final int DEFAULT_TIMEOUT_MS = 10000;
    public static final String DEFAULT_CONFIG_FILE = "/conf/monitor-urls.xml";
    public static final String METRIC_PREFIX = "metric-prefix";
    public static final String CONN_TIMEOUT = "conn-timeout";
    public static final String SOCK_TIMEOUT = "sock-timeout";
    public static final String CONFIG_FILE = "config-file";

    //provider to unmarshall the monitor-urls.xml
    final JAXBProvider jaxbProvider = new JAXBProvider(MonitorUrls.class);

    //wrapper for httpClient
    final SimpleHttpClient simpleHttpClient;

    MonitorUrls monitorUrls;
    String metricPrefix;
    int connectionTimeout;
    int socketTimeout;
    private String configFile;

    public UrlPingerContext(Map<String,String> taskArgs) throws JAXBException {
        init(taskArgs);
        simpleHttpClient = SimpleHttpClient.builder(taskArgs)
                .socketTimeout(getSocketTimeout())
                .connectionTimeout(getConnectionTimeout())
                .build();
        monitorUrls = (MonitorUrls)jaxbProvider.unmarshal(getConfigFile());
    }

    private void init(Map<String, String> taskArgs) {

        setMetricPrefix(MetricConstants.DEFAULT_METRIC_PREFIX);
        setSocketTimeout(DEFAULT_TIMEOUT_MS);
        setConnectionTimeout(DEFAULT_TIMEOUT_MS);
        setConfigFile(DEFAULT_CONFIG_FILE);
        if(!Strings.isNullOrEmpty(taskArgs.get(METRIC_PREFIX))){
            setMetricPrefix(taskArgs.get(METRIC_PREFIX));
        }
        if(!Strings.isNullOrEmpty(taskArgs.get(CONFIG_FILE))){
            setConfigFile(taskArgs.get(CONFIG_FILE));
        }
        try {
            if (!Strings.isNullOrEmpty(taskArgs.get(CONN_TIMEOUT))) {
                setConnectionTimeout(Integer.parseInt(taskArgs.get(CONN_TIMEOUT)));
            }
            if (!Strings.isNullOrEmpty(taskArgs.get(SOCK_TIMEOUT))) {
                setSocketTimeout(Integer.parseInt(taskArgs.get(SOCK_TIMEOUT)));
            }
        }
        catch (NumberFormatException nfe){
        }
    }

    public MonitorUrls getMonitorUrls() {
        return monitorUrls;
    }

    public String getMetricPrefix() {
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }

    public SimpleHttpClient getSimpleHttpClient() {
        return simpleHttpClient;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }
}
