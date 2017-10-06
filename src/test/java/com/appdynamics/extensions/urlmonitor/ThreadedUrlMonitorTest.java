package com.appdynamics.extensions.urlmonitor;

import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.whenNew;


/**
 * Created by balakrishnavadavalasa on 03/03/16.
 */

public class ThreadedUrlMonitorTest {

    private static final String CONFIG_FILE_PARAM = "config-file";
    private static final String DEFAULT_METRIC_PATH = "Custom Metrics|URLMonitor";
    private String metricPath = DEFAULT_METRIC_PATH;

    ThreadedUrlMonitor monitor;
    ThreadedUrlMonitor monitorSpy;

    @Mock
    private MetricWriter metricWriter;


    @Before
    public void init() throws Exception {

        whenNew(MetricWriter.class).withArguments(any(AManagedMonitor.class),
                anyString()).thenReturn(metricWriter);

        monitor = new ThreadedUrlMonitor();
        monitorSpy = Mockito.spy(monitor);
    }

    @Test
    public void testExecute() throws Exception {
        Map<String, String> taskParams = new HashMap<String, String>();
        taskParams.put(CONFIG_FILE_PARAM, "src/test/resources/conf/config.yml");

        ThreadedUrlMonitor monitor = new ThreadedUrlMonitor();
        monitor.execute(taskParams, null);
    }

    @Test
    public void urlMonitorExceptionOccurredTest() throws Exception {
        Map<String, String> args = Maps.newHashMap();
        args.put("config-file", "src/test/resources/conf/config.yml");

        verifyZeroInteractions(monitorSpy);

    }

    @Test(expected = TaskExecutionException.class)
    public void testWithNullArgsShouldResultInException() throws Exception {
        monitorSpy.execute(null, null);
    }

    @Test
    public void testWithNoValidLogConfigResultInException() throws Exception {
        Map<String, String> args = Maps.newHashMap();
        args.put("config-file", "src/test/resources/conf/invalidConfig.yml");

        Assert.assertNull(monitorSpy.execute(args, null));

    }

    @Test
    public void urlMonitorSitesTest() throws Exception {
        Map<String, String> args = Maps.newHashMap();
        args.put("config-file", "src/test/resources/conf/config.yml");

        monitorSpy.execute(args, null);

        verify(monitorSpy, times(1)).printMetricWithValue(eq(metricPath + "|Google|Response Code"), eq("200"), anyString(), anyString(), anyString());
        verify(monitorSpy, times(1)).printMetricWithValue(eq(metricPath + "|Google|Status"), eq("4"), anyString(), anyString(), anyString());

    }

    @Test
    public void urlWithGroupNamesMonitorSitesTest() throws Exception {
        Map<String, String> args = Maps.newHashMap();
        args.put("config-file", "src/test/resources/conf/configWithGroupNames.yml");

        monitor = new ThreadedUrlMonitor();

        ThreadedUrlMonitor monitorSpy = Mockito.spy(monitor);

        monitorSpy.execute(args, null);

        verify(monitorSpy, times(1)).printMetricWithValue(eq(metricPath + "|MySites|Google|Response Code"), eq("200"), anyString(), anyString(), anyString());
        verify(monitorSpy, times(1)).printMetricWithValue(eq(metricPath + "|MySites|Google|Status"), eq("4"), anyString(), anyString(), anyString());

    }

    @Test
    public void reDirectTest() throws Exception {
        Map<String, String> taskParams = new HashMap<String, String>();
        taskParams.put(CONFIG_FILE_PARAM, "src/test/resources/conf/redirectTestConf.yml");

        ThreadedUrlMonitor monitor = new ThreadedUrlMonitor();
        monitor.execute(taskParams, null);
    }
}
