package com.appdynamics.extensions.sitemonitor.jaxb;

import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.File;

public class JAXBProviderTest {

    public static final String CONF_DIR = "conf";
    public static final String CONF_FILE = "site-config.xml";
    public static final String ERROR_CONF_FILE = "site-config.xml.error";

    JAXBProvider jaxbProvider = new JAXBProvider(SiteConfig.class);

    @Test
    public void canUnmarshalIntoObj() throws JAXBException {
        String filepath = this.getClass().getResource(File.separator + CONF_DIR + File.separator + CONF_FILE).getFile();
        SiteConfig siteConfig = (SiteConfig)jaxbProvider.unmarshal(filepath);
        assert(siteConfig != null);
    }

    @Test(expected = JAXBException.class)
    public void throwsExceptionForInvalidXml() throws JAXBException {
        String filepath = this.getClass().getResource(File.separator + CONF_DIR + File.separator + ERROR_CONF_FILE).getFile();
        SiteConfig siteConfig = (SiteConfig)jaxbProvider.unmarshal(filepath);
        assert(siteConfig == null);
    }

}
