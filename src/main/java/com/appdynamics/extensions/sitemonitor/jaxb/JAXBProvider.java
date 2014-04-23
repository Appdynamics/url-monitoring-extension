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

package com.appdynamics.extensions.sitemonitor.jaxb;

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
