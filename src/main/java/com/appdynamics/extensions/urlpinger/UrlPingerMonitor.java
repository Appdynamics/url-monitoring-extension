package com.appdynamics.extensions.urlpinger;

import com.appdynamics.extensions.urlpinger.common.MonitorArgs;
import com.appdynamics.extensions.urlpinger.jaxb.JAXBProvider;
import com.appdynamics.extensions.urlpinger.jaxb.MonitorUrl;
import com.appdynamics.extensions.urlpinger.jaxb.MonitorUrls;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * This class is the entry point into the url-pinger-monitoring-extension and it's execute method is called by the machine agent.
 */
public class UrlPingerMonitor extends AManagedMonitor {

    private static final int NUMBER_OF_THREADS = 10;
    private static final String EXTENSION_PREFIX = "[AppDExt] :: ";
    private static final String CONF_MONITOR_FILEPATH = File.separator + "conf" + File.separator + "monitor-urls.xml";

    ExecutorService threadPool;
    JAXBProvider<MonitorUrls> jaxbProvider;

    public static final Logger logger = Logger.getLogger(UrlPingerMonitor.class);

    public UrlPingerMonitor(){
        this(Executors.newFixedThreadPool(NUMBER_OF_THREADS),new JAXBProvider<MonitorUrls>());
    }

    public UrlPingerMonitor(ExecutorService threadPool, JAXBProvider<MonitorUrls> jaxbProvider){
        String message = "Using Monitor Version [" + getImplementationVersion() + "]";
        logger.info(message);
        System.out.println(message);
        this.threadPool = threadPool;
        this.jaxbProvider = jaxbProvider;
    }


    public TaskOutput execute(Map<String, String> argsMap, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        logger.info(EXTENSION_PREFIX + "Starting the Url Pinger Monitoring task");
        MonitorArgs monitorArgs = new MonitorArgs(argsMap);
        if(logger.isDebugEnabled()){
            logger.debug(EXTENSION_PREFIX + monitorArgs.toString());
        }
        UrlPingerContext context = new UrlPingerContext(monitorArgs);
        try {
            MonitorUrls monitorUrls = jaxbProvider.unmarshal(this.getClass().getResource(CONF_MONITOR_FILEPATH).getFile(),MonitorUrls.class);
            List<Future<UrlPingerMetrics>> parallelTasks = createParallelTasks(context, monitorUrls);
            collectAndPrintMetrics(context, parallelTasks);
            context.closeHttpClient();
            logger.info(EXTENSION_PREFIX + "Successfully completed the Url Pinger Monitoring task");
            return new TaskOutput(EXTENSION_PREFIX + "Url Pinger Metrics Upload completed successfully.");
        } catch (JAXBException e) {
            logger.error(EXTENSION_PREFIX + "Issue in unmarshalling xml.",e);
        } catch (Exception e) {
            logger.error(EXTENSION_PREFIX + "Issue in executing the task .",e);
        }
        return new TaskOutput(EXTENSION_PREFIX + "Url Pinger Monitoring task completed with errors");
    }

    private List<Future<UrlPingerMetrics>> createParallelTasks(UrlPingerContext context, MonitorUrls monitorUrls) {
        List<Future<UrlPingerMetrics>> parallelTasks = new ArrayList<Future<UrlPingerMetrics>>();
        if(monitorUrls != null && monitorUrls.getMonitorUrls() != null){
            for(MonitorUrl monitorUrl : monitorUrls.getMonitorUrls()){
                if(monitorUrl.isValid()){
                    UrlPingerTask pingerTask = new UrlPingerTask(context,monitorUrl);
                    parallelTasks.add(getThreadPool().submit(pingerTask));
                }
            }
        }
        return parallelTasks;
    }

    private void collectAndPrintMetrics(UrlPingerContext context, List<Future<UrlPingerMetrics>> parallelTasks)  {
        for(Future<UrlPingerMetrics> aParallelTask : parallelTasks){
            try {
                //timing out on the future just to be super safe
                UrlPingerMetrics metrics = aParallelTask.get(20, TimeUnit.SECONDS);
                printMetrics(context,metrics);
            } catch (InterruptedException e) {
                logger.error(EXTENSION_PREFIX + "Task interrupted." + e);
            } catch (ExecutionException e) {
                logger.error(EXTENSION_PREFIX + "Task execution failed." + e.getCause());
            } catch (TimeoutException e) {
                logger.error(EXTENSION_PREFIX + "Task timed out." + e.getCause());
            }
        }
    }

    private void printMetrics(UrlPingerContext context, UrlPingerMetrics metrics) {

        printMetric(getCodeMetricName(context, metrics),
                Integer.toString(metrics.getStatusCode()),
                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);

        printMetric(getLastSeenMetricName(context, metrics),
                metrics.getLastSeen().toString(),
                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);
    }

    private void printMetric(String metricName,String metricValue,String aggType,String timeRollupType,String clusterRollupType){
        MetricWriter metricWriter = getMetricWriter(metricName,
                aggType,
                timeRollupType,
                clusterRollupType
        );
      //  System.out.println(EXTENSION_PREFIX + "Sending [" + aggType + MetricConstants.METRIC_SEPARATOR + timeRollupType + MetricConstants.METRIC_SEPARATOR + clusterRollupType
      //          + "] metric = " + metricName + " = " + metricValue);
        if (logger.isDebugEnabled()) {
            logger.debug(EXTENSION_PREFIX + "Sending [" + aggType + MetricConstants.METRIC_SEPARATOR + timeRollupType + MetricConstants.METRIC_SEPARATOR + clusterRollupType
                    + "] metric = " + metricName + " = " + metricValue);
        }
        metricWriter.printMetric(metricValue);
    }

    private String getLastSeenMetricName(UrlPingerContext context, UrlPingerMetrics metrics) {
        return context.getMonitorArgs().getMetricPrefix() + metrics.getDisplayName() + MetricConstants.METRIC_SEPARATOR + MetricConstants.LAST_SEEN;
    }

    private String getCodeMetricName(UrlPingerContext context, UrlPingerMetrics metrics) {
        return context.getMonitorArgs().getMetricPrefix() + metrics.getDisplayName() + MetricConstants.METRIC_SEPARATOR + MetricConstants.CODE;
    }


    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public String getImplementationVersion(){
        return this.getClass().getPackage().getImplementationTitle();
    }
}
