package com.appdynamics.extensions.urlpinger;

import com.appdynamics.extensions.urlpinger.http.SimpleHttpClient;
import com.appdynamics.extensions.urlpinger.jaxb.MonitorUrl;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.concurrent.Callable;


public class UrlPingerTask implements Callable<UrlPingerMetrics> {

    private UrlPingerContext context;
    private MonitorUrl data;

    public static final Logger logger = Logger.getLogger(UrlPingerTask.class);

    public UrlPingerTask(UrlPingerContext context, MonitorUrl data){
        this.context = context;
        this.data = data;
    }


    public UrlPingerMetrics call() throws Exception {
        if(data != null){
            SimpleHttpClient httpClient = context.getSimpleHttpClient();
            try {
                HttpResponse response = httpClient.get(data.getUrl());
                if (response != null && response.getStatusLine() != null) {
                    return new UrlPingerMetrics(data.getDisplayName(), response.getStatusLine().getStatusCode(), new Date());
                }
            }catch(Exception e){
                logger.error("[AppDExt::] Error in executing http request " ,e);
            }
        }
        return new UrlPingerMetrics(data.getDisplayName(), MetricConstants.DEFAULT_STATUS_CODE,new Date());
    }

}
