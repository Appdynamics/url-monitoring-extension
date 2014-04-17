package com.appdynamics.extensions.urlpinger;


import com.appdynamics.extensions.urlpinger.jaxb.JAXBProvider;
import com.appdynamics.extensions.urlpinger.jaxb.MonitorUrl;
import com.appdynamics.extensions.urlpinger.jaxb.MonitorUrls;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UrlPingerMonitorTest {

    private static final int NUMBER_OF_THREADS = 3;
    public static final String SAMPLE_HTTP_HOST = "221.10.40.234";
    public static final String SAMPLE_HTTP_PORT = "843";
    public static final String ADMIN = "admin";
    public static final String PROXY_HOST = "proxy-host";
    public static final String PROXY_PORT = "proxy-port";
    public static final String CONFIG_FILE = "src/test/resources/conf/monitor-urls.xml";
    // JAXBProvider jaxbProvider = mock(JAXBProvider.class);
   // MonitorUrls monitorUrls;

//    @Before
//    public void setup() throws JAXBException {
//        createData();
//        when(jaxbProvider.unmarshal(anyString())).thenReturn(monitorUrls);
//    }

//    private void createData() {
//        monitorUrls = new MonitorUrls();
//        List<MonitorUrl> monitors = new ArrayList<MonitorUrl>();
//        MonitorUrl m1 = new MonitorUrl();
//        m1.setDisplayName("Google");
//        m1.setUrl("https://www.google.com");
//        monitors.add(m1);
//        MonitorUrl m2 = new MonitorUrl();
//        m2.setDisplayName("AppDynamics");
//        m2.setUrl("http://www.appdynamics.com");
//        monitors.add(m2);
//        MonitorUrl m3 = new MonitorUrl();
//        m3.setDisplayName("Amazon");
//        m3.setUrl("http://amazon.com");
//        monitors.add(m3);
//        MonitorUrl m4 = new MonitorUrl();
//        m4.setDisplayName("Facebook");
//        m4.setUrl("www.facebook.com");
//        monitors.add(m4);
//        MonitorUrl m4a = new MonitorUrl();
//        m4a.setDisplayName("AppdGov");
//        m4a.setUrl("www.appd.gov");
//        monitors.add(m4a);
//        MonitorUrl m5 = new MonitorUrl();
//        m5.setUrl("www.ning.com");
//        monitors.add(m5);
//        monitorUrls.setMonitorUrls(monitors);
//    }

    @Test
    public void testUrlPingerExtension() throws TaskExecutionException {
        UrlPingerMonitor pingerMonitor = new UrlPingerMonitor(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
        Map<String,String> taskArgs = Maps.newHashMap();
        taskArgs.put(UrlPingerContext.CONFIG_FILE, CONFIG_FILE);
        TaskOutput output = pingerMonitor.execute(taskArgs, null);
        Assert.assertTrue(output.getStatusMessage().contains("successfully"));
    }

    @Test
    public void testUrlPingerExtensionWithHttpProxy() throws TaskExecutionException {
        UrlPingerMonitor pingerMonitor = new UrlPingerMonitor(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
        Map<String,String> taskArgs = Maps.newHashMap();
        taskArgs.put(PROXY_HOST, SAMPLE_HTTP_HOST);
        taskArgs.put(PROXY_PORT, SAMPLE_HTTP_PORT);
        taskArgs.put(UrlPingerContext.CONFIG_FILE,CONFIG_FILE);
        TaskOutput output = pingerMonitor.execute(taskArgs, null);
        Assert.assertTrue(output.getStatusMessage().contains("successfully"));
    }

}
