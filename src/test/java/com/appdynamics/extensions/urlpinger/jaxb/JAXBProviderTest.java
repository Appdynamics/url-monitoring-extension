package com.appdynamics.extensions.urlpinger.jaxb;

import com.appdynamics.extensions.urlpinger.jaxb.JAXBProvider;
import com.appdynamics.extensions.urlpinger.jaxb.MonitorUrls;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.File;

public class JAXBProviderTest {

    public static final String CONF_DIR = "conf";
    public static final String CONF_FILE = "monitor-urls.xml";
    public static final String ERROR_CONF_FILE = "monitor-urls.xml.error";

    JAXBProvider<MonitorUrls> jaxbProvider = new JAXBProvider<MonitorUrls>();

    @Test
    public void canUnmarshalIntoObj() throws JAXBException {
        String filepath = this.getClass().getResource(File.separator + CONF_DIR + File.separator + CONF_FILE).getFile();
        MonitorUrls monitorUrls = jaxbProvider.unmarshal(filepath,MonitorUrls.class);
        assert(monitorUrls != null);
    }

    @Test(expected = JAXBException.class)
    public void throwsExceptionForInvalidXml() throws JAXBException {
        String filepath = this.getClass().getResource(File.separator + CONF_DIR + File.separator + ERROR_CONF_FILE).getFile();
        MonitorUrls monitorUrls = jaxbProvider.unmarshal(filepath,MonitorUrls.class);
        assert(monitorUrls == null);
    }

}
