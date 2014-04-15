package com.appdynamics.extensions.urlpinger;


import com.appdynamics.extensions.urlpinger.common.MonitorArgs;
import com.appdynamics.extensions.urlpinger.http.SimpleHttpClient;
import com.appdynamics.extensions.urlpinger.http.SimpleHttpConfig;

import java.io.IOException;


public class UrlPingerContext {

    public static final int DEFAULT_TIMEOUT_MS = 10000;
    private SimpleHttpClient simpleHttpClient;
    private MonitorArgs monitorArgs;

    public UrlPingerContext(MonitorArgs monitorArgs){
        this.monitorArgs = monitorArgs;
        SimpleHttpConfig simpleHttpConfig = new SimpleHttpConfig.Builder()
                .setProxyHost(monitorArgs.getProxyHost())
                .setProxyPort(monitorArgs.getProxyPort())
                .setProxyUser(monitorArgs.getProxyUser())
                .setProxyPassword(monitorArgs.getProxyPassword())
                .setConnectionTimeout(DEFAULT_TIMEOUT_MS)
                .setSocketTimeout(DEFAULT_TIMEOUT_MS)
                .build();
        this.simpleHttpClient = new SimpleHttpClient(simpleHttpConfig);
    }

    public SimpleHttpClient getSimpleHttpClient() {
        return simpleHttpClient;
    }

    public MonitorArgs getMonitorArgs() {
        return monitorArgs;
    }

    public void closeHttpClient() throws IOException {
        simpleHttpClient.close();
    }
}
