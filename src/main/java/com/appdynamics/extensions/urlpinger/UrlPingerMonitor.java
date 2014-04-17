package com.appdynamics.extensions.urlpinger;

import com.appdynamics.extensions.urlpinger.jaxb.MonitorUrl;
import com.appdynamics.extensions.urlpinger.jaxb.MonitorUrls;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * This class is the entry point into the url-pinger-monitoring-extension and it's execute method is called by the machine agent.
 */
public class UrlPingerMonitor extends AManagedMonitor {

    ExecutorService threadPool;

    private static final int NUMBER_OF_THREADS = 10;
    public static final Logger logger = Logger.getLogger(UrlPingerMonitor.class);

    public UrlPingerMonitor() {
        this(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
    }

    public UrlPingerMonitor(ExecutorService threadPool){
        String message = MetricConstants.EXTENSION_PREFIX + "Using Monitor Version [" + getImplementationVersion() + "]";
        logger.info(message);
        System.out.println(message);
        this.threadPool = threadPool;
    }

    /**
     * Entry point into the extension. It unmarshals the xml and creates parallel
     * task for making http calls and finally outputs the metrics.
     * @param taskArgs
     * @param taskExecutionContext
     * @return
     * @throws TaskExecutionException
     */
    public TaskOutput execute(Map<String, String> taskArgs, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        logger.info(MetricConstants.EXTENSION_PREFIX + "Starting the Url Pinger Monitoring task");
        if(logger.isDebugEnabled()){
            logger.debug(MetricConstants.EXTENSION_PREFIX + "Task Arguments Passed ::" + taskArgs);
        }
        try {
            UrlPingerContext context = new UrlPingerContext(taskArgs);
            List<Future<UrlPingerMetrics>> parallelTasks = createParallelTasks(context);
            collectAndPrintMetrics(context, parallelTasks);
            logger.info(MetricConstants.EXTENSION_PREFIX + "Successfully completed the Url Pinger Monitoring task");
            return new TaskOutput(MetricConstants.EXTENSION_PREFIX + "Url Pinger Metrics Upload completed successfully.");
        } catch (JAXBException e) {
            logger.error(MetricConstants.EXTENSION_PREFIX + "Issue in unmarshalling xml.",e);
        } catch (Exception e) {
            logger.error(MetricConstants.EXTENSION_PREFIX + "Issue in executing the task .",e);
        }
        return new TaskOutput(MetricConstants.EXTENSION_PREFIX + "Url Pinger Monitoring task completed with errors");
    }

    /**
     * Creates parrallel tasks and submits it to threadPool
     * @param context
     * @return
     */
    private List<Future<UrlPingerMetrics>> createParallelTasks(UrlPingerContext context) {
        List<Future<UrlPingerMetrics>> parallelTasks = new ArrayList<Future<UrlPingerMetrics>>();
        MonitorUrls monitorUrls = context.getMonitorUrls();
        if(monitorUrls != null && monitorUrls.getMonitorUrls() != null){
            for(MonitorUrl monitorUrl : monitorUrls.getMonitorUrls()){
                if(monitorUrl.isValid()){
                    UrlPingerTask pingerTask = new UrlPingerTask(context.getSimpleHttpClient(),monitorUrl);
                    parallelTasks.add(getThreadPool().submit(pingerTask));
                }
            }
        }
        return parallelTasks;
    }

    /**
     * Get the output from each task and report the metrics
     * @param context
     * @param parallelTasks
     */
    private void collectAndPrintMetrics(UrlPingerContext context, List<Future<UrlPingerMetrics>> parallelTasks)  {
        for(Future<UrlPingerMetrics> aParallelTask : parallelTasks){
            try {
                //timing out on the future just to be super safe
                UrlPingerMetrics metrics = aParallelTask.get(20, TimeUnit.SECONDS);
                printMetrics(context,metrics);
            } catch (InterruptedException e) {
                logger.error(MetricConstants.EXTENSION_PREFIX + "Task interrupted." + e);
            } catch (ExecutionException e) {
                logger.error(MetricConstants.EXTENSION_PREFIX + "Task execution failed." + e.getCause());
            } catch (TimeoutException e) {
                logger.error(MetricConstants.EXTENSION_PREFIX + "Task timed out." + e.getCause());
            }
        }
    }


    /**
     * Reporting all the metrics for UrlPinger
     * @param context
     * @param metrics
     */
    private void printMetrics(UrlPingerContext context, UrlPingerMetrics metrics) {

        printMetric(getCodeMetricName(context, metrics),
                Integer.toString(metrics.getStatusCode()),
                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);

        printMetric(getResponseTimeMetricName(context, metrics),
                Long.toString(metrics.getResponseTimeInMs()),
                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);

    }

    /**
     * A helper method to report the metrics.
     * @param metricName
     * @param metricValue
     * @param aggType
     * @param timeRollupType
     * @param clusterRollupType
     */
    private void printMetric(String metricName,String metricValue,String aggType,String timeRollupType,String clusterRollupType){
        MetricWriter metricWriter = getMetricWriter(metricName,
                aggType,
                timeRollupType,
                clusterRollupType
        );
        System.out.println(MetricConstants.EXTENSION_PREFIX + "Sending [" + aggType + MetricConstants.METRIC_SEPARATOR + timeRollupType + MetricConstants.METRIC_SEPARATOR + clusterRollupType
                + "] metric = " + metricName + " = " + metricValue);
        if (logger.isDebugEnabled()) {
            logger.debug(MetricConstants.EXTENSION_PREFIX + "Sending [" + aggType + MetricConstants.METRIC_SEPARATOR + timeRollupType + MetricConstants.METRIC_SEPARATOR + clusterRollupType
                    + "] metric = " + metricName + " = " + metricValue);
        }
        metricWriter.printMetric(metricValue);
    }


    /**
     * Returns the metrics path for Http status code metric.
     * @param context
     * @param metrics
     * @return
     */
    private String getCodeMetricName(UrlPingerContext context, UrlPingerMetrics metrics) {
        return context.getMetricPrefix() + metrics.getDisplayName() + MetricConstants.METRIC_SEPARATOR + MetricConstants.CODE;
    }

    /**
     * Reporting response time
     * @param context
     * @param metrics
     * @return
     */
    private String getResponseTimeMetricName(UrlPingerContext context, UrlPingerMetrics metrics) {
        return context.getMetricPrefix() + metrics.getDisplayName() + MetricConstants.METRIC_SEPARATOR + MetricConstants.RESPONSE_TIME;
    }


    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public String getImplementationVersion(){
        return this.getClass().getPackage().getImplementationTitle();
    }
}
