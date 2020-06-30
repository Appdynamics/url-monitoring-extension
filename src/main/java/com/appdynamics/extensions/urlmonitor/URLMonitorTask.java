package com.appdynamics.extensions.urlmonitor;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.urlmonitor.auth.AuthSchemeFactory;
import com.appdynamics.extensions.urlmonitor.auth.AuthTypeEnum;
import com.appdynamics.extensions.urlmonitor.config.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.ning.http.client.*;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLMonitorTask implements AMonitorTaskRunnable {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(URLMonitorTask.class);

    private MonitorContextConfiguration configuration;

    private MetricWriteHelper metricWriter;

    private String metricPrefix;

    private Map<String, ?> configYml;

    private List<SiteConfig> siteConfigs;

    private DefaultSiteConfig defaults;

    private List<MetricConfig> metrics;

    private RequestConfig requestConfig = new RequestConfig();

    private ClientConfig clientConfig;

    private final ObjectMapper mapper = new ObjectMapper();

    public URLMonitorTask(TasksExecutionServiceProvider serviceProvider, MonitorContextConfiguration configuration) {
        this.configuration = configuration;
        this.metricPrefix = configuration.getMetricPrefix();
        this.metricWriter = serviceProvider.getMetricWriteHelper();
        this.configYml = configuration.getConfigYml();
        this.siteConfigs = Arrays.asList(mapper.convertValue(configYml.get("sites"), SiteConfig[].class));
        this.defaults = mapper.convertValue(configYml.get("defaultParams"), DefaultSiteConfig.class);
        this.clientConfig = mapper.convertValue(configYml.get("clientConfig"), ClientConfig.class);
        this.metrics = Arrays.asList(mapper.convertValue(configYml.get("metricConfig"), MetricConfig[].class));
    }

    public void run() {
        List<Metric> metricDataList = new CopyOnWriteArrayList<>();
        try {
            final CountDownLatch latch = new CountDownLatch(getTotalAttemptCount());
            logger.debug(String.format("Sending %d HTTP requests asynchronously to %d sites",
                    latch.getCount(), siteConfigs.size()));

            setSiteDefaults();

            final ConcurrentHashMap<SiteConfig, List<SiteResult>> results = buildResultMap();
            final long overallStartTime = System.currentTimeMillis();
            final Map<String, Integer> groupStatus = new HashMap<String, Integer>();

            List<RequestConfig> requestConfigList = requestConfig.setClientForSite(clientConfig, defaults, siteConfigs);

            try {
                for (final RequestConfig requestConfig : requestConfigList) {

                    final SiteConfig site = requestConfig.getSiteConfig();

                    for (int i = 0; i < site.getNumAttempts(); i++) {
                        RequestBuilder rb = new RequestBuilder()
                                .setMethod(site.getMethod())
                                .setUrl(site.getUrl())
                                .setFollowRedirects(site.isFollowRedirects())
                                .setRealm(AuthSchemeFactory.getAuth(AuthTypeEnum.valueOf(site.getAuthType()!=null ? site.getAuthType() : AuthTypeEnum.NONE.name()),site)
                                        .build());
                        if (!Strings.isNullOrEmpty(site.getRequestPayloadFile())) {
                            rb.setBody(readPostRequestFile(site));
                            if (!"post".equalsIgnoreCase(site.getMethod())) {
                                rb.setMethod("POST");
                            }
                        }
                        //proxy support
                        ProxyConfig proxyConfig = site.getProxyConfig();
                        if (proxyConfig != null) {
                            if (proxyConfig.getUsername() != null && proxyConfig.getPassword() != null) {
                                rb.setProxyServer(new ProxyServer(proxyConfig.getHost(), proxyConfig.getPort(), proxyConfig.getUsername(), proxyConfig.getPassword()));
                            } else {
                                rb.setProxyServer(new ProxyServer(proxyConfig.getHost(), proxyConfig.getPort()));
                            }
                        }

                        for (Map.Entry<String, String> header : site.getHeaders().entrySet()) {
                            rb.addHeader(header.getKey(), header.getValue());
                        }

                        logger.debug(String.format("Sending %s request %d of %d to %s at %s with redirect allowed as %s",
                                site.getMethod(), (i + 1),
                                site.getNumAttempts(), site.getName(), site.getUrl(), site.isFollowRedirects()));

                        final long startTime = System.currentTimeMillis();
                        final Request r = rb.build();

                        final SiteResult result = new SiteResult();
                        result.setStatus(SiteResult.ResultStatus.SUCCESS);

                        final ByteArrayOutputStream body = new ByteArrayOutputStream();

                        requestConfig.getClient().executeRequest(r, new AsyncCompletionHandler<Response>() {

                            private void finish(SiteResult result) {
                                results.get(site)
                                        .add(result);

                                latch.countDown();
                                printMetricsForRequestCompleted(results.get(site), site);
                                logger.debug(latch.getCount() + " requests remaining");

                            }

                            private void printMetricsForRequestCompleted(List<SiteResult> results, SiteConfig site) {
                                String metricPath = metricPrefix + "|" + site.getName();
                                int resultCount = results.size();

                                long totalFirstByteTime = 0;
                                long totalDownloadTime = 0;
                                long totalElapsedTime = 0;
                                int statusCode = 0;
                                long responseSize = 0;
                                HashMap<String, Integer> matches = null;
                                SiteResult.ResultStatus status = SiteResult.ResultStatus.UNKNOWN;
                                for (SiteResult result : results) {
                                    status = result.getStatus();
                                    statusCode = result.getResponseCode();
                                    responseSize = result.getResponseBytes();
                                    totalFirstByteTime += result.getFirstByteTime();
                                    totalDownloadTime += result.getDownloadTime();
                                    totalElapsedTime += result.getTotalTime();
                                    matches = result.getMatches();
                                }

                                long averageFirstByteTime = totalFirstByteTime / resultCount;
                                long averageDownloadTime = totalDownloadTime / resultCount;
                                long averageElapsedTime = totalElapsedTime / resultCount;

                                logger.debug(String.format("Results for site '%s': count=%d, total=%d ms, average=%d ms, respCode=%d, bytes=%d, status=%s",
                                        site.getName(), resultCount, totalFirstByteTime, averageFirstByteTime, statusCode, responseSize, status));


                                if (!Strings.isNullOrEmpty(site.getGroupName())) {
                                    metricPath = metricPrefix + "|" + site.getGroupName() + "|" + site.getName();

                                    if (statusCode == 200) {

                                        Integer count = groupStatus.get(site.getGroupName());

                                        if (count == null) {
                                            groupStatus.put(site.getGroupName(), 1);
                                        } else {
                                            groupStatus.put(site.getGroupName(), ++count);
                                        }
                                    }
                                }

                                Map<String, String> metricValuesMap = new HashMap<>();
                                metricValuesMap.put("Average Response Time (ms)", Long.toString(averageElapsedTime));
                                metricValuesMap.put("Download Time (ms)", Long.toString(averageDownloadTime));
                                metricValuesMap.put("First Byte Time (ms)", Long.toString(averageFirstByteTime));
                                metricValuesMap.put("Response Code", Integer.toString(statusCode));
                                metricValuesMap.put("Status", Long.toString(status.ordinal()));
                                metricValuesMap.put("Response Bytes", Long.toString(responseSize));

                                List<Metric> metricDataListInner = Lists.newArrayList();

                                for (Map.Entry<String, String> metricValue : metricValuesMap.entrySet()) {
                                    for (MetricConfig metricData : metrics) {
                                        if (metricData != null && metricData.getName().equalsIgnoreCase(metricValue.getKey())) {
                                            Map<String, String> propertiesMap = mapper.convertValue(metricData, Map.class);
                                            Metric metric = new Metric(metricData.getName(), String.valueOf(metricValue.getValue()), metricPath + "|" + metricData.getAlias(), propertiesMap);
                                            metricDataListInner.add(metric);
                                        }
                                    }
                                }

                                metricPath += "|Pattern Matches";
                                if (matches != null) {
                                    for (Map.Entry<String, Integer> match : matches.entrySet()) {
                                        Metric metric = new Metric("Count", String.valueOf(match.getValue()), metricPath + "|" + match.getKey() + "|Count", "SUM", "SUM", "COLLECTIVE");
                                        metricDataListInner.add(metric);
                                    }
                                }

                                if(metricDataListInner != null && metricDataListInner.size()>0){
                                    metricWriter.transformAndPrintMetrics(metricDataListInner);
                                }

                            }

                            @Override
                            public STATE onStatusReceived(HttpResponseStatus status) throws Exception {

                                result.setFirstByteTime(System.currentTimeMillis() - startTime);
                                result.setResponseCode(status.getStatusCode());
                                logger.debug(String.format("[%s] First byte received in %d ms",
                                        site.getName(),
                                        result.getFirstByteTime()));

                                if (status.getStatusCode() == 200) {
                                    logger.debug(String.format("[%s] %s %s -> %d %s",
                                            site.getName(),
                                            site.getMethod(),
                                            site.getUrl(),
                                            status.getStatusCode(),
                                            status.getStatusText()));
                                    return STATE.CONTINUE;
                                } else if (status.getStatusCode() == 401 && !site.isTreatAuthFailedAsError()) {
                                    logger.debug(String.format("[%s] %s %s -> %d %s [but OK]",
                                            site.getName(),
                                            site.getMethod(),
                                            site.getUrl(),
                                            status.getStatusCode(),
                                            status.getStatusText()));
                                    return STATE.CONTINUE;
                                }

                                logger.warn(String.format("[%s] %s %s -> %d %s",
                                        site.getName(),
                                        site.getMethod(),
                                        site.getUrl(),
                                        status.getStatusCode(),
                                        status.getStatusText()));
                                result.setStatus(SiteResult.ResultStatus.ERROR);

                                return STATE.ABORT;
                            }

                            @Override
                            public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
                                for (Map.Entry<String, List<String>> entry : headers.getHeaders().entrySet()) {
                                    for (String value : entry.getValue()) {
                                        body.write(entry.getKey().getBytes());
                                        body.write(':');
                                        body.write(' ');
                                        body.write(value.getBytes());
                                        body.write('\n');
                                    }
                                }
                                body.write("\n\n".getBytes());

                                long headerTime = System.currentTimeMillis() - startTime;
                                logger.debug(String.format("[%s] Headers received in %d ms",
                                        site.getName(), headerTime));

                                return STATE.CONTINUE;
                            }

                            @Override
                            public STATE onBodyPartReceived(HttpResponseBodyPart content) throws Exception {
                                content.writeTo(body);
                                return STATE.CONTINUE;
                            }

                            private int getStringMatchCount(String text, String pattern) {
                                return StringUtils.countMatches(text, pattern);
                            }

                            private int getRegexMatchCount(String text, String pattern) {

                                Pattern regexPattern = Pattern.compile(pattern);
                                Matcher regexMatcher = regexPattern.matcher(text);

                                int matchCount = 0;
                                while (regexMatcher.find()) {
                                    matchCount += 1;
                                }

                                return matchCount;
                            }

                            @Override
                            public Response onCompleted(Response response) throws Exception {
//
                                if (result.getStatus() == SiteResult.ResultStatus.SUCCESS) {
//
                                    result.setDownloadTime(System.currentTimeMillis() - (startTime + result.getFirstByteTime()));
                                    result.setTotalTime(System.currentTimeMillis() - startTime);
//
                                    String responseBody = body.toString();
                                    result.setResponseBytes(responseBody.length());
                                    logger.debug(String.format("[%s] Download time was %d ms for %d bytes",
                                            site.getName(),
                                            result.getDownloadTime(),
                                            result.getResponseBytes()));
//
                                    if (site.getMatchPatterns().size() > 0) {
//
                                        for (MatchPattern pattern : site.getMatchPatterns()) {
                                            MatchPattern.PatternType type = MatchPattern.PatternType.fromString(pattern.getType());
                                            logger.debug(String.format("[%s] Checking for a %s match against '%s'",
                                                    site.getName(),
                                                    pattern.getType(),
                                                    pattern.getPattern()));
//
                                            int matchCount;
                                            switch (type) {
                                                case SUBSTRING:
                                                    matchCount = getStringMatchCount(responseBody,
                                                            pattern.getPattern());
                                                    break;
                                                case CASE_INSENSITIVE_SUBSTRING:
                                                    matchCount = getStringMatchCount(responseBody.toLowerCase(),
                                                            pattern.getPattern().toLowerCase());
                                                    break;
                                                case REGEX:
                                                    matchCount = getRegexMatchCount(responseBody,
                                                            pattern.getPattern());
                                                    break;
                                                case WORD:
                                                    matchCount = getRegexMatchCount(responseBody,
                                                            "(?i)\\b" + pattern.getPattern() + "\\b");
                                                    break;
//
                                                default:
                                                    throw new IllegalArgumentException("Unknown pattern type: " + pattern.getType());
                                            }
//
                                            logger.debug(String.format("[%s] Match count for %s pattern '%s' = %d ",
                                                    site.getName(),
                                                    pattern.getType(),
                                                    pattern.getPattern(),
                                                    matchCount));
                                            result.getMatches().put(pattern.getName(), matchCount);
                                        }
                                    }
//
                                }
//
                                logger.debug(String.format("[%s] Request completed in %d ms",
                                        site.getName(),
                                        result.getTotalTime()));
                                finish(result);
//
//
                                return response;
                            }
                            //
                            @Override
                            public void onThrowable(Throwable t) {
                                logger.error(site.getUrl() + " -> FAILED: " + t.getMessage(), t);
                                finish(new SiteResult(SiteResult.ResultStatus.FAILED));
                            }
                        });
                    }
                }

                latch.await();

                final long overallElapsedTime = System.currentTimeMillis() - overallStartTime;



                metricDataList.add(new Metric("Requests Sent", String.valueOf(getTotalAttemptCount()), metricPrefix + "|Requests Sent", "SUM", "SUM", "COLLECTIVE"));
                metricDataList.add(new Metric("Elapsed Time (ms)", String.valueOf(overallElapsedTime), metricPrefix + "|Elapsed Time (ms)", "SUM", "SUM", "COLLECTIVE"));
                metricDataList.add(new Metric("Monitored Sites Count", String.valueOf(siteConfigs.size()), metricPrefix + "|Monitored Sites Count", "SUM", "SUM", "COLLECTIVE"));

                for (Map.Entry entry : groupStatus.entrySet()) {
                    String metricName = metricPrefix + "|" + entry.getKey() + "|Responsive Site Count";
                    Integer metricValue = groupStatus.get(entry.getKey());

                    metricDataList.add(new Metric("Responsive Sites Count", String.valueOf(metricValue), metricPrefix + "|Responsive Sites Count", "OBSERVATION", "CURRENT", "COLLECTIVE"));
                }

                //Wait for all tasks to finish
                logger.info("All requests completed in " + overallElapsedTime + " ms");
            } catch (Exception ex) {
                logger.error("Error in HTTP client: " + ex.getMessage(), ex);
                throw new TaskExecutionException(ex);
            } finally {
                requestConfig.closeClients(requestConfigList);
            }
            if (metricDataList != null && metricDataList.size() > 0) {
                metricWriter.transformAndPrintMetrics(metricDataList);
            }
        }catch(Exception e) {
            logger.error("Unexpected error while running the URL Monitor", e);
        }
    }

    private int getTotalAttemptCount()
    {
        int total = 0;
        for (SiteConfig site : siteConfigs)
        {
            if (site.getNumAttempts() == -1) {
                total += defaults.getNumAttempts();
            } else {
                total += site.getNumAttempts();
            }
        }

        return total;
    }

    private String readPostRequestFile(SiteConfig site) {
        String requestBody = "";
        try {
            requestBody = new String (Files.readAllBytes(Paths.get(site.getRequestPayloadFile())));
        }  catch (Exception e) {
            logger.error("Exception while reading PostRequest Body file for url " + site.getUrl(), e);
        }
        return requestBody;
    }

    protected void setSiteDefaults() {

        for (final SiteConfig site : siteConfigs){
            if (site.isTreatAuthFailedAsError() == null)
                site.setTreatAuthFailedAsError(defaults.isTreatAuthFailedAsError());
            if (site.getNumAttempts() == -1)
                site.setNumAttempts(defaults.getNumAttempts());
            if (Strings.isNullOrEmpty(site.getMethod()))
                site.setMethod(defaults.getMethod());
        }
    }

    protected final ConcurrentHashMap<SiteConfig, List<SiteResult>> buildResultMap() {
        final ConcurrentHashMap<SiteConfig, List<SiteResult>> results = new ConcurrentHashMap<SiteConfig, List<SiteResult>>();
        for (final SiteConfig site : siteConfigs) {
            results.put(site, Collections.synchronizedList(new ArrayList<SiteResult>()));
        }

        return results;
    }

    public void onTaskComplete() {
        logger.info("All tasks for URL Monitor finished");
    }
}
