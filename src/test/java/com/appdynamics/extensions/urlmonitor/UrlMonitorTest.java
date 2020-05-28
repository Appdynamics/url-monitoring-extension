/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor;

import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.whenNew;


/**
 * Created by balakrishnavadavalasa on 03/03/16.
 */

public class UrlMonitorTest {

    private static final String CONFIG_FILE_PARAM = "config-file";
    private static final String DEFAULT_METRIC_PATH = "Custom Metrics|URLMonitor";
    private String metricPath = DEFAULT_METRIC_PATH;

    URLMonitor monitor;
    URLMonitor monitorSpy;

    @Mock
    private MetricWriter metricWriter;

    @Mock
    private URLMonitorTask urlMonitorTask;


    @Before
    public void init() throws Exception {

        whenNew(MetricWriter.class).withArguments(any(AManagedMonitor.class),
                anyString()).thenReturn(metricWriter);

        monitor = new URLMonitor();
        monitorSpy = Mockito.spy(monitor);
    }

    @Test
    public void testExecute() throws Exception {
        Map<String, String> taskParams = new HashMap<String, String>();
        taskParams.put(CONFIG_FILE_PARAM, "src/test/resources/conf/config.yml");

        URLMonitor monitor = new URLMonitor();
        monitor.execute(taskParams, null);
    }

    @Test
    public void urlMonitorExceptionOccurredTest() throws Exception {
        Map<String, String> args = Maps.newHashMap();
        args.put("config-file", "src/test/resources/conf/config.yml");

        verifyZeroInteractions(monitorSpy);

    }

    @Test(expected = NullPointerException.class)
    public void testWithNullArgsShouldResultInException() throws Exception {
        monitorSpy.execute(null, null);
    }

    @Test
    public void reDirectTest() throws Exception {
        Map<String, String> taskParams = new HashMap<String, String>();
        taskParams.put(CONFIG_FILE_PARAM, "src/test/resources/conf/redirectTestConf.yml");

        URLMonitor monitor = new URLMonitor();
        monitor.execute(taskParams, null);
    }
}
