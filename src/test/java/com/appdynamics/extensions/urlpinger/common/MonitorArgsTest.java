package com.appdynamics.extensions.urlpinger.common;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;


public class MonitorArgsTest {

    public static final String SAMPLE_HOST = "localhost";
    public static final String SAMPLE_PORT = "8080";
    public static final String ADMIN = "admin";
    public static final String CUSTOM_METRICS_URL_PINGER = "Custom Metrics|Url Pinger";
    MonitorArgs monitorArgs;

    @Before
    public void setUp(){
        Map<String,String> taskArgs = Maps.newHashMap();
        taskArgs.put(MonitorArgs.PROXY_HOST, SAMPLE_HOST);
        taskArgs.put(MonitorArgs.PROXY_PORT, SAMPLE_PORT);
        taskArgs.put(MonitorArgs.PROXY_USER, ADMIN);
        taskArgs.put(MonitorArgs.PROXY_PASSWORD, ADMIN);
        taskArgs.put(MonitorArgs.METRIC_PREFIX,"Custom Metrics|Url Pinger");
        monitorArgs = new MonitorArgs(taskArgs);
    }

    @Test
    public void validateAllArgs(){
        assert(monitorArgs.getProxyHost().equals(SAMPLE_HOST));
        assert(monitorArgs.getProxyPort().equals(SAMPLE_PORT));
        assert(monitorArgs.getProxyUser().equals(ADMIN));
        assert(monitorArgs.getProxyPassword().equals(ADMIN));
        assert(monitorArgs.getMetricPrefix().equals(CUSTOM_METRICS_URL_PINGER));
    }
}
