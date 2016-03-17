package org.fao.geonet.services.api;

import org.fao.geonet.utils.TransformerFactoryFactory;
import org.jdom.Element;
import org.jdom.transform.JDOMSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.xml.AbstractXmlHttpMessageConverter;

import javax.xml.transform.*;
import java.io.IOException;

/**
 * Convert a JDOM Element to response
 */
public class DOMElementMessageConverter extends AbstractXmlHttpMessageConverter<Object> {

    @Override
    protected Object readFromSource(Class<? extends Object> clazz,
                                    HttpHeaders headers, Source source) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void writeToResult(Object t, HttpHeaders headers, Result result) throws IOException {
        final TransformerFactory tf;
        try {
            tf = TransformerFactoryFactory.getTransformerFactory();
        } catch (TransformerConfigurationException e) {
            throw new IOException("TransformerFactory Exception", e);
        }
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new JDOMSource((Element)t), result);
        } catch (TransformerConfigurationException e) {
            throw new IOException("Transformer Config Exception", e);
        } catch (TransformerException e) {
            throw new IOException("Transformer Exception", e);
        }
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Element.class.isAssignableFrom(clazz);
    }
}