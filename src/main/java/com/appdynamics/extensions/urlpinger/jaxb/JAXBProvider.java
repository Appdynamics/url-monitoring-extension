package com.appdynamics.extensions.urlpinger.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 *
 * A JAXB Provider to marshal/unmarshals xml.
 */
public class JAXBProvider {

    protected JAXBContext jaxbContext;
    protected Unmarshaller unmarshaller;

    public JAXBProvider(Class... clazzes)  {
        try {
            jaxbContext = JAXBContext.newInstance(clazzes);
            if(jaxbContext != null){
                unmarshaller = jaxbContext.createUnmarshaller();
            }
        } catch (JAXBException e) {
            unmarshaller = null;
        }
    }

    /**
     * Unmarshals the specified file into a java object.
     * @param filename -
     * @return Unmarshalled object
     * @throws JAXBException
     */
    public Object unmarshal(String filename) throws JAXBException {
        if(unmarshaller != null) {
            return unmarshaller.unmarshal(new File(filename));
        }
        return null;
    }



}
