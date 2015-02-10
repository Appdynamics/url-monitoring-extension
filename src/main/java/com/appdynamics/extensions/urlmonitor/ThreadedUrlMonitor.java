package com.appdynamics.extensions.urlmonitor;


import com.appdynamics.extensions.urlmonitor.config.ClientConfig;
import com.appdynamics.extensions.urlmonitor.config.DefaultSiteConfig;
import com.appdynamics.extensions.urlmonitor.config.MonitorConfig;
import com.appdynamics.extensions.urlmonitor.config.SiteConfig;
import com.google.common.base.Strings;
import com.ning.http.client.*;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class ThreadedUrlMonitor extends AManagedMonitor
{
    private static final Logger log = Logger.getLogger(ThreadedUrlMonitor.class);
    private static final String DEFAULT_CONFIG_FILE = "config.yml";
    private static final String CONFIG_FILE_PARAM = "config-file";
    protected MonitorConfig config;

    private AsyncHttpClient createHttpClient(MonitorConfig config)
    {
        DefaultSiteConfig defaultSiteConfig = config.getDefaultParams();
        ClientConfig clientConfig = config.getClientConfig();

        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();

        builder.setFollowRedirects(clientConfig.isFollowRedirects())
                .setMaximumNumberOfRedirects(clientConfig.getMaxRedirects())
                .setConnectionTimeoutInMs(defaultSiteConfig.getConnectTimeout())
                .setRequestTimeoutInMs(defaultSiteConfig.getSocketTimeout())
                .setMaximumConnectionsPerHost(clientConfig.getMaxConnPerRoute())
                .setMaximumConnectionsTotal(clientConfig.getMaxConnTotal());

        return new AsyncHttpClient(builder.build());
    }

    public MonitorConfig readConfigFile(String filename)
    {
        log.info("Reading configuration from " + filename);

        FileReader configReader;
        try
        {
            configReader = new FileReader(filename);
        }
        catch (FileNotFoundException e)
        {
            log.error("File not found: " + filename, e);
            return null;
        }

        Yaml yaml = new Yaml(new Constructor(MonitorConfig.class));
        return (MonitorConfig) yaml.load(configReader);
    }

    protected void setSiteDefaults()
    {
        DefaultSiteConfig defaults = config.getDefaultParams();

        for (final SiteConfig site : config.getSites())
        {
            if (site.isTreatAuthFailedAsError() == null)
                site.setTreatAuthFailedAsError(defaults.isTreatAuthFailedAsError());
            if (site.getNumAttempts() == -1)
                site.setNumAttempts(defaults.getNumAttempts());
            if (Strings.isNullOrEmpty(site.getMethod()))
                site.setMethod(defaults.getMethod());
        }
    }

    protected final ConcurrentHashMap<SiteConfig, List<SiteResult>> buildResultMap()
    {
        final ConcurrentHashMap<SiteConfig, List<SiteResult>> results = new ConcurrentHashMap<SiteConfig, List<SiteResult>>();
        for (final SiteConfig site : config.getSites())
        {
            results.put(site, Collections.synchronizedList(new ArrayList<SiteResult>()));
        }

        return results;
    }

    //    @Override
    public TaskOutput execute(Map<String, String> taskParams, TaskExecutionContext taskContext)
            throws TaskExecutionException
    {
        String configFilename = DEFAULT_CONFIG_FILE;
        if (taskParams.containsKey(CONFIG_FILE_PARAM))
        {
            configFilename = taskParams.get(CONFIG_FILE_PARAM);
        }

        config = readConfigFile(configFilename);
        if (config == null)
            return null;

        final CountDownLatch latch = new CountDownLatch(config.getTotalAttemptCount());
        log.info(String.format("Sending %d HTTP requests asynchronously to %d sites",
                latch.getCount(), config.getSites().length));

        setSiteDefaults();
        final ConcurrentHashMap<SiteConfig, List<SiteResult>> results = buildResultMap();
        final long overallStartTime = System.currentTimeMillis();
        final AsyncHttpClient client = createHttpClient(config);

        try
        {
            for (final SiteConfig site : config.getSites())
            {
                for (int i = 0; i < site.getNumAttempts(); i++)
                {
                    RequestBuilder rb = new RequestBuilder()
                            .setMethod(site.getMethod())
                            .setUrl(site.getUrl())
                            .setFollowRedirects(config.getClientConfig().isFollowRedirects())
                            .setRealm(new Realm.RealmBuilder()
                                    .setScheme(Realm.AuthScheme.BASIC)
                                    .setPrincipal(site.getUsername())
                                    .setPassword(site.getPassword())
                                    .build());

                    for (Map.Entry<String, String> header : site.getHeaders().entrySet())
                    {
                        rb.addHeader(header.getKey(), header.getValue());
                    }

                    log.info(String.format("Sending %s request %d of %d to %s at %s",
                            site.getMethod(), (i + 1),
                            site.getNumAttempts(), site.getName(), site.getUrl()));

                    final long startTime = System.currentTimeMillis();
                    final Request r = rb.build();

                    client.executeRequest(r, new AsyncCompletionHandler<Response>()
                    {
                        private void finish(SiteResult result)
                        {
                            results.get(site)
                                    .add(result);
                            latch.countDown();
                            log.info(latch.getCount() + " requests remaining");
                        }

                        @Override
                        public Response onCompleted(Response response) throws Exception
                        {
                            final long elapsedTime = System.currentTimeMillis() - startTime;
                            log.info("Request completed in " + elapsedTime + " ms");

                            int statusCode = response.getStatusCode();
                            SiteResult.ResultStatus status = SiteResult.ResultStatus.SUCCESS;

                            if (statusCode == 200)
                            {
                                log.info(String.format("%s %s -> %d %s",
                                        site.getMethod(),
                                        site.getUrl(),
                                        response.getStatusCode(),
                                        response.getStatusText()));
                            }
                            else if (statusCode == 401 && !site.isTreatAuthFailedAsError())
                            {
                                log.info(String.format("%s %s -> %d %s [but OK]",
                                        site.getMethod(),
                                        site.getUrl(),
                                        response.getStatusCode(),
                                        response.getStatusText()));
                            }
                            else
                            {
                                log.warn(String.format("%s %s -> %d %s",
                                        site.getMethod(),
                                        site.getUrl(),
                                        response.getStatusCode(),
                                        response.getStatusText()));
                                status = SiteResult.ResultStatus.ERROR;
                            }

                            finish(new SiteResult(elapsedTime, status, statusCode,
                                    response.getResponseBodyAsBytes().length));
                            return response;
                        }

                        @Override
                        public void onThrowable(Throwable t)
                        {
                            log.error(site.getUrl() + " -> FAILED: " + t.getMessage(), t);
                            finish(new SiteResult(0, SiteResult.ResultStatus.FAILED, 0, 0));
                        }
                    });
                }
            }

            latch.await();
            client.close();

            final long overallElapsedTime = System.currentTimeMillis() - overallStartTime;
            for (final SiteConfig site : config.getSites())
            {
                String metricPath = "Custom Metrics|URL Monitor|" + site.getName();
                int resultCount = results.get(site).size();

                long totalTime = 0;
                int statusCode = 0;
                long responseSize = 0;
                SiteResult.ResultStatus status = SiteResult.ResultStatus.UNKNOWN;
                for (SiteResult result : results.get(site))
                {
                    status = result.getStatus();
                    statusCode = result.getResponseCode();
                    responseSize = result.getResponseBytes();
                    totalTime += result.getElapsedTime();
                }

                long averageTime = totalTime / resultCount;

                log.info(String.format("Results for site '%s': count=%d, total=%d ms, average=%d ms, respCode=%d, bytes=%d, status=%s",
                        site.getName(), resultCount, totalTime, averageTime, statusCode, responseSize, status));

                getMetricWriter(metricPath + "|Average Response Time (ms)",
                        MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                        MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                        MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE).printMetric(
                        Long.toString(averageTime));
                getMetricWriter(metricPath + "|Response Code",
                        MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                        MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                        MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric(
                        Integer.toString(statusCode));
                getMetricWriter(metricPath + "|Status",
                        MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                        MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                        MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric(Long.toString(status.ordinal()));
                getMetricWriter(metricPath + "|Response Bytes",
                        MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                        MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                        MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric(
                        Long.toString(responseSize));
            }

            String metricPath = "Custom Metrics|URL Monitor";
            getMetricWriter(metricPath + "|Requests Sent",
                    MetricWriter.METRIC_AGGREGATION_TYPE_SUM,
                    MetricWriter.METRIC_TIME_ROLLUP_TYPE_SUM,
                    MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE).printMetric(
                    Long.toString(config.getTotalAttemptCount()));
            getMetricWriter(metricPath + "|Elapsed Time (ms)",
                    MetricWriter.METRIC_AGGREGATION_TYPE_SUM,
                    MetricWriter.METRIC_TIME_ROLLUP_TYPE_SUM,
                    MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE).printMetric(
                    Long.toString(overallElapsedTime));

            log.info("All requests completed in " + overallElapsedTime + " ms");
        }
        catch (Exception ex)
        {
            log.error("Error in HTTP client: " + ex.getMessage(), ex);
            throw new TaskExecutionException(ex);
        }
        finally
        {
            client.close();
        }

        return new TaskOutput("Success");
    }

    public static void main(String[] argv)
            throws Exception
    {
        Map<String, String> taskParams = new HashMap<String, String>();
        taskParams.put(CONFIG_FILE_PARAM, "config.yml");

        ThreadedUrlMonitor monitor = new ThreadedUrlMonitor();
        monitor.execute(taskParams, null);
    }
}