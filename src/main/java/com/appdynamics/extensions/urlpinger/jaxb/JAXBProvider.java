package com.appdynamics.extensions.urlpinger.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 *
 *
 */
public class JAXBProvider<T> {

    public T unmarshal(String filename,Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (T)unmarshaller.unmarshal(new File(filename));
    }



}
