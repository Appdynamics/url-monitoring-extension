package com.appdynamics.extensions.urlmonitor;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class URLMonitor extends ABaseMonitor {


    private static final String METRIC_PREFIX = "Custom Metrics|URL Monitor";

    @Override
    protected String getDefaultMetricPrefix() {
        return METRIC_PREFIX;
    }

    @Override
    public String getMonitorName() {
        return "URL Monitor";
    }


    @Override
    protected void doRun(TasksExecutionServiceProvider serviceProvider) {

        URLMonitorTask task = new URLMonitorTask(serviceProvider, this.getContextConfiguration());
        serviceProvider.submit("URLMonitor", task);
    }

    protected List<Map<String, ?>> getServers() {
        return new ArrayList<Map<String, ?>>();
    }

}