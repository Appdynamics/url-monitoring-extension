package com.appdynamics.extensions.urlmonitor;

import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.urlmonitor.SiteResult.ResultStatus;
import com.appdynamics.extensions.urlmonitor.config.*;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.ning.http.client.*;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThreadedUrlMonitor extends AManagedMonitor {
    private static final Logger log = Logger.getLogger(ThreadedUrlMonitor.class);
    private static final String DEFAULT_CONFIG_FILE = "config.yml";
    private static final String CONFIG_FILE_PARAM = "config-file";
    private static final String DEFAULT_METRIC_PATH = "Custom Metrics|URL Monitor";
    private String metricPath = DEFAULT_METRIC_PATH;
    protected MonitorConfig config;

    public ThreadedUrlMonitor() {
        System.out.println(logVersion());
    }

    private AsyncHttpClient createHttpClient(MonitorConfig config) {
        DefaultSiteConfig defaultSiteConfig = config.getDefaultParams();
        ClientConfig clientConfig = config.getClientConfig();

        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();

        builder.setFollowRedirect(clientConfig.isFollowRedirects())
                .setAcceptAnyCertificate(clientConfig.isIgnoreSslErrors())
                .setMaxRedirects(clientConfig.getMaxRedirects())
                .setConnectTimeout(defaultSiteConfig.getConnectTimeout())
                .setRequestTimeout(defaultSiteConfig.getSocketTimeout())
                .setMaxConnectionsPerHost(clientConfig.getMaxConnPerRoute())
                .setMaxConnections(clientConfig.getMaxConnTotal())
                .setUserAgent(clientConfig.getUserAgent());

        ProxyConfig proxyConfig = defaultSiteConfig.getProxyConfig();
        if(proxyConfig != null){
            builder.setProxyServer(new ProxyServer(proxyConfig.getHost(),proxyConfig.getPort()));
        }
        return new AsyncHttpClient(builder.build());
    }

    private String getConfigFilename(String filename) {
        if (filename == null) {
            return "";
        }
        // for absolute paths
        if (new File(filename).exists()) {
            return filename;
        }
        // for relative paths
        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = "";
        if (!Strings.isNullOrEmpty(filename)) {
            configFileName = jarPath + File.separator + filename;
        }
        return configFileName;
    }

    private String readPostRequestFile(SiteConfig site) {
        String requestBody = "";
        try {
             requestBody = Files.toString(new File(getConfigFilename(site.getRequestPayloadFile())), Charsets.UTF_8);
        } catch (FileNotFoundException e) {
            log.error("Post Request Payload file not found for url " + site.getUrl(), e);
        } catch (IOException e) {
            log.error("Exception while reading PostRequest Body file for url " + site.getUrl(), e);
        }
        return requestBody;
    }

    protected void setSiteDefaults() {
        DefaultSiteConfig defaults = config.getDefaultParams();

        for (final SiteConfig site : config.getSites()) {
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
        for (final SiteConfig site : config.getSites()) {
            results.put(site, Collections.synchronizedList(new ArrayList<SiteResult>()));
        }

        return results;
    }

    //    @Override
    public TaskOutput execute(Map<String, String> taskParams, TaskExecutionContext taskContext)
            throws TaskExecutionException {
        log.info(logVersion());

        String configFilename = DEFAULT_CONFIG_FILE;
        if (taskParams.containsKey(CONFIG_FILE_PARAM)) {
            configFilename = taskParams.get(CONFIG_FILE_PARAM);
        }

        config = YmlReader.readFromFile(configFilename, MonitorConfig.class);
        if (config == null)
            return null;

        if(!Strings.isNullOrEmpty(config.getMetricPrefix())) {
            metricPath = StringUtils.stripEnd(config.getMetricPrefix(), "|");
        }

        final CountDownLatch latch = new CountDownLatch(config.getTotalAttemptCount());
        log.info(String.format("Sending %d HTTP requests asynchronously to %d sites",
                latch.getCount(), config.getSites().length));

        setSiteDefaults();
        final ConcurrentHashMap<SiteConfig, List<SiteResult>> results = buildResultMap();
        final long overallStartTime = System.currentTimeMillis();
        final AsyncHttpClient client = createHttpClient(config);

        try {
            for (final SiteConfig site : config.getSites()) {
                for (int i = 0; i < site.getNumAttempts(); i++) {
                    RequestBuilder rb = new RequestBuilder()
                            .setMethod(site.getMethod())
                            .setUrl(site.getUrl())
                            .setFollowRedirects(config.getClientConfig().isFollowRedirects())
                            .setRealm(new Realm.RealmBuilder()
                                    .setScheme(Realm.AuthScheme.BASIC)
                                    .setPrincipal(site.getUsername())
                                    .setPassword(site.getPassword())
                                    .build());
                    if(!Strings.isNullOrEmpty(site.getRequestPayloadFile())) {
                        rb.setBody(readPostRequestFile(site));
                        if (!"post".equalsIgnoreCase(site.getMethod())) {
                            rb.setMethod("POST");
                        }
                    }
                    //proxy support
                    ProxyConfig proxyConfig = site.getProxyConfig();
                    if(proxyConfig != null){
                        if(proxyConfig.getUsername() != null && proxyConfig.getPassword() != null) {
                            rb.setProxyServer(new ProxyServer(proxyConfig.getHost(),proxyConfig.getPort(), proxyConfig.getUsername(), proxyConfig.getPassword()));
                        } else {
                            rb.setProxyServer(new ProxyServer(proxyConfig.getHost(),proxyConfig.getPort()));
                        }
                    }

                    for (Map.Entry<String, String> header : site.getHeaders().entrySet()) {
                        rb.addHeader(header.getKey(), header.getValue());
                    }

                    log.info(String.format("Sending %s request %d of %d to %s at %s",
                            site.getMethod(), (i + 1),
                            site.getNumAttempts(), site.getName(), site.getUrl()));

                    final long startTime = System.currentTimeMillis();
                    final Request r = rb.build();

                    final SiteResult result = new SiteResult();
                    result.setStatus(ResultStatus.SUCCESS);

                    final ByteArrayOutputStream body = new ByteArrayOutputStream();

                    client.executeRequest(r, new AsyncCompletionHandler<Response>() {

                        private void finish(SiteResult result) {
                            results.get(site)
                                    .add(result);
                            latch.countDown();
                            printMetricsForRequestCompleted(results.get(site),site);
                            log.info(latch.getCount() + " requests remaining");
                        }
                        private void printMetricsForRequestCompleted(List<SiteResult> results, SiteConfig site){
                            String myMetricPath = metricPath + "|" + site.getName();
                            int resultCount = results.size();

                            long totalFirstByteTime = 0;
                            long totalDownloadTime = 0;
                            long totalElapsedTime = 0;
                            int statusCode = 0;
                            long responseSize = 0;
                            //int availability = 0;
                            HashMap<String, Integer> matches = null;
                            SiteResult.ResultStatus status = SiteResult.ResultStatus.UNKNOWN;
                            for (SiteResult result : results) {
                                status = result.getStatus();
                                statusCode = result.getResponseCode();
                                /*if(statusCode == 200) {
                                    availability = 1;
                                }*/
                                responseSize = result.getResponseBytes();
                                totalFirstByteTime += result.getFirstByteTime();
                                totalDownloadTime += result.getDownloadTime();
                                totalElapsedTime += result.getTotalTime();
                                matches = result.getMatches();
                            }

                            long averageFirstByteTime = totalFirstByteTime / resultCount;
                            long averageDownloadTime = totalDownloadTime / resultCount;
                            long averageElapsedTime = totalElapsedTime / resultCount;

                            log.info(String.format("Results for site '%s': count=%d, total=%d ms, average=%d ms, respCode=%d, bytes=%d, status=%s",
                                    site.getName(), resultCount, totalFirstByteTime, averageFirstByteTime, statusCode, responseSize, status));

                            printMetric(myMetricPath + "|Average Response Time (ms)", Long.toString(averageElapsedTime));
                            printMetric(myMetricPath + "|Download Time (ms)", Long.toString(averageDownloadTime));
                            printMetric(myMetricPath + "|First Byte Time (ms)", Long.toString(averageFirstByteTime));
                            printMetric(myMetricPath + "|Response Code", Integer.toString(statusCode));
                            printMetric(myMetricPath + "|Status", Long.toString(status.ordinal()));
                            printMetric(myMetricPath + "|Response Bytes", Long.toString(responseSize));
                            //printMetric(myMetricPath + "|Availability", Integer.toString(availability));

                            myMetricPath += "|Pattern Matches";
                            if (matches != null) {
                                for (Map.Entry<String, Integer> match : matches.entrySet()) {
                                    getMetricWriter(myMetricPath + "|" + match.getKey() + "|Count",
                                            MetricWriter.METRIC_AGGREGATION_TYPE_SUM,
                                            MetricWriter.METRIC_TIME_ROLLUP_TYPE_SUM,
                                            MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE).printMetric(
                                            Long.toString(match.getValue()));
                                }
                            }
                        
                    }

                        @Override
                        public STATE onStatusReceived(HttpResponseStatus status) throws Exception {

                            result.setFirstByteTime(System.currentTimeMillis() - startTime);
                            result.setResponseCode(status.getStatusCode());
                            log.debug(String.format("[%s] First byte received in %d ms",
                                    site.getName(),
                                    result.getFirstByteTime()));

                            if (status.getStatusCode() == 200) {
                                log.info(String.format("[%s] %s %s -> %d %s",
                                        site.getName(),
                                        site.getMethod(),
                                        site.getUrl(),
                                        status.getStatusCode(),
                                        status.getStatusText()));
                                return STATE.CONTINUE;
                            }
                            else if (status.getStatusCode() == 401 && !site.isTreatAuthFailedAsError()) {
                                log.info(String.format("[%s] %s %s -> %d %s [but OK]",
                                        site.getName(),
                                        site.getMethod(),
                                        site.getUrl(),
                                        status.getStatusCode(),
                                        status.getStatusText()));
                                return STATE.CONTINUE;
                            }

                            log.warn(String.format("[%s] %s %s -> %d %s",
                                    site.getName(),
                                    site.getMethod(),
                                    site.getUrl(),
                                    status.getStatusCode(),
                                    status.getStatusText()));
                            result.setStatus(ResultStatus.ERROR);

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
                            log.debug(String.format("[%s] Headers received in %d ms",
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

                            if (result.getStatus() == ResultStatus.SUCCESS) {

                                result.setDownloadTime(System.currentTimeMillis() - (startTime + result.getFirstByteTime()));
                                result.setTotalTime(System.currentTimeMillis() - startTime);

                                String responseBody = body.toString();
                                result.setResponseBytes(responseBody.length());
                                log.info(String.format("[%s] Download time was %d ms for %d bytes",
                                        site.getName(),
                                        result.getDownloadTime(),
                                        result.getResponseBytes()));

                                if (site.getMatchPatterns().size() > 0) {

                                    for (MatchPattern pattern : site.getMatchPatterns()) {
                                        MatchPattern.PatternType type = MatchPattern.PatternType.fromString(pattern.getType());
                                        log.debug(String.format("[%s] Checking for a %s match against '%s'",
                                                site.getName(),
                                                pattern.getType(),
                                                pattern.getPattern()));

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

                                            default:
                                                throw new IllegalArgumentException("Unknown pattern type: " + pattern.getType());
                                        }

                                        log.info(String.format("[%s] Match count for %s pattern '%s' = %d ",
                                                site.getName(),
                                                pattern.getType(),
                                                pattern.getPattern(),
                                                matchCount));
                                        result.getMatches().put(pattern.getName(), matchCount);
                                    }
                                }

                            }

                            log.info(String.format("[%s] Request completed in %d ms",
                                    site.getName(),
                                    result.getTotalTime()));
                            finish(result);

                            return response;
                        }

                        @Override
                        public void onThrowable(Throwable t) {
                            log.error(site.getUrl() + " -> FAILED: " + t.getMessage(), t);
                            finish(new SiteResult(ResultStatus.FAILED));
                        }
                    });
                }
            }

            latch.await();
            client.close();

            final long overallElapsedTime = System.currentTimeMillis() - overallStartTime;

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
        } catch (Exception ex) {
            log.error("Error in HTTP client: " + ex.getMessage(), ex);
            throw new TaskExecutionException(ex);
        } finally {
            client.close();
        }

        return new TaskOutput("Success");
    }

    private void printMetric(String metricName, String metricValue) {
        if(!Strings.isNullOrEmpty(metricValue)) {
            getMetricWriter(metricName,
                    MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                    MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                    MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE).printMetric(metricValue);
        }
    }

    private String logVersion() {
        String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
        return msg;
    }

    private static String getImplementationVersion() {
        return ThreadedUrlMonitor.class.getPackage().getImplementationTitle();
    }
    
}
