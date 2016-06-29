package com.appdynamics.extensions.urlmonitor;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by balakrishnavadavalasa on 03/03/16.
 */
public class ThreadedUrlMonitorTest {

    private static final String CONFIG_FILE_PARAM = "config-file";

    @Test
    public void testExecute() throws Exception {
        Map<String, String> taskParams = new HashMap<String, String>();
        taskParams.put(CONFIG_FILE_PARAM, "src/test/resources/conf/config.yml");

        ThreadedUrlMonitor monitor = new ThreadedUrlMonitor();
        monitor.execute(taskParams, null);
    }
}
