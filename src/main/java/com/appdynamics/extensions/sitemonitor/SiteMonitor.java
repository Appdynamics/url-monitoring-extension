/**
 * Copyright 2014 AppDynamics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.sitemonitor;

import com.appdynamics.extensions.sitemonitor.jaxb.Site;
import com.appdynamics.extensions.sitemonitor.jaxb.SiteConfig;
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
 * This class is the entry point into the site-monitoring-extension and it's execute method is called by the machine agent.
 */
public class SiteMonitor extends AManagedMonitor {

    ExecutorService threadPool;

    private static final int NUMBER_OF_THREADS = 10;
    public static final Logger logger = Logger.getLogger(SiteMonitor.class);

    public SiteMonitor() {
        this(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
    }

    public SiteMonitor(ExecutorService threadPool){
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
     * @return TaskOutput
     * @throws TaskExecutionException
     */
    public TaskOutput execute(Map<String, String> taskArgs, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        if(taskArgs != null) {
            logger.info(MetricConstants.EXTENSION_PREFIX + "Starting the Url Pinger Monitoring task");
            if (logger.isDebugEnabled()) {
                logger.debug(MetricConstants.EXTENSION_PREFIX + "Task Arguments Passed ::" + taskArgs);
            }
            try {
                SiteMonitorContext context = new SiteMonitorContext(taskArgs);
                List<Future<SiteMonitorMetrics>> parallelTasks = createParallelTasks(context);
                collectAndPrintMetrics(context, parallelTasks);
                logger.info(MetricConstants.EXTENSION_PREFIX + "SiteMonitor metrics upload completed successfully.");
                return new TaskOutput(MetricConstants.EXTENSION_PREFIX + "SiteMonitor metrics upload completed successfully.");
            } catch (JAXBException e) {
                logger.error(MetricConstants.EXTENSION_PREFIX + "Issue in unmarshalling xml.", e);
            } catch (Exception e) {
                logger.error(MetricConstants.EXTENSION_PREFIX + "Issue in executing the task .", e);
            }
        }
        throw new TaskExecutionException(MetricConstants.EXTENSION_PREFIX + "SiteMonitor completed with errors");
    }

    /**
     * Creates parrallel tasks and submits it to threadPool
     * @param context
     * @return
     */
    private List<Future<SiteMonitorMetrics>>    createParallelTasks(SiteMonitorContext context) {
        List<Future<SiteMonitorMetrics>> parallelTasks = new ArrayList<Future<SiteMonitorMetrics>>();
        SiteConfig siteConfig = context.getSiteConfig();
        if(siteConfig != null && siteConfig.getSites() != null){
            for(Site site : siteConfig.getSites()){
                if(site.isValid()){
                    SiteMonitorTask siteMonitorTask = new SiteMonitorTask(context.getSimpleHttpClient(),site);
                    parallelTasks.add(getThreadPool().submit(siteMonitorTask));
                }
                else{
                    logger.error("Site is invalid " + site);
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
    private void collectAndPrintMetrics(SiteMonitorContext context, List<Future<SiteMonitorMetrics>> parallelTasks)  {
        for(Future<SiteMonitorMetrics> aParallelTask : parallelTasks){
            try {
                //timing out on the future just to be super safe
                SiteMonitorMetrics metrics = aParallelTask.get(20, TimeUnit.SECONDS);
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
    private void printMetrics(SiteMonitorContext context, SiteMonitorMetrics metrics) {

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
        //System.out.println(MetricConstants.EXTENSION_PREFIX + "Sending [" + aggType + MetricConstants.METRIC_SEPARATOR + timeRollupType + MetricConstants.METRIC_SEPARATOR + clusterRollupType
        //        + "] metric = " + metricName + " = " + metricValue);
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
    private String getCodeMetricName(SiteMonitorContext context, SiteMonitorMetrics metrics) {
        if(context.getSiteConfig() != null) {
            return context.getSiteConfig().getMetricPrefix() + metrics.getDisplayName() + MetricConstants.METRIC_SEPARATOR + MetricConstants.CODE;
        }
        return "";
    }

    /**
     * Reporting response time
     * @param context
     * @param metrics
     * @return
     */
    private String getResponseTimeMetricName(SiteMonitorContext context, SiteMonitorMetrics metrics) {
        if(context.getSiteConfig() != null) {
            return context.getSiteConfig().getMetricPrefix() + metrics.getDisplayName() + MetricConstants.METRIC_SEPARATOR + MetricConstants.RESPONSE_TIME;
        }
        return "";
    }


    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public String getImplementationVersion(){
        return this.getClass().getPackage().getImplementationTitle();
    }
}
