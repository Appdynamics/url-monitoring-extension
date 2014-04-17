package com.appdynamics.extensions.urlpinger;

import com.appdynamics.extensions.http.Response;
import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.urlpinger.jaxb.MonitorUrl;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * Unit of task performed by the UrlPinger monitoring extension.
 */

public class UrlPingerTask implements Callable<UrlPingerMetrics> {

    private SimpleHttpClient httpClient;
    private MonitorUrl data;

    public static final Logger logger = Logger.getLogger(UrlPingerTask.class);

    public UrlPingerTask(SimpleHttpClient httpClient, MonitorUrl data){
        this.httpClient = httpClient;
        this.data = data;
    }


    public UrlPingerMetrics call() throws Exception {
        if(data != null){
            try {
                long startTime = System.currentTimeMillis();
                Response response = httpClient.target(data.getUrl()).get();
                long responseTime = System.currentTimeMillis() - startTime;
                if (response != null) {
                    return new UrlPingerMetrics(data.getDisplayName(), response.getStatus(),responseTime);
                }
            }catch(Exception e){
                logger.error("[AppDExt::] Error in executing http request " ,e);
            }
        }
        return new UrlPingerMetrics(data.getDisplayName(), MetricConstants.DEFAULT_STATUS_CODE,-1l);
    }

}
