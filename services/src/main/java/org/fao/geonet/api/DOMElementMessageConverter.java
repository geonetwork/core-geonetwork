//=============================================================================
//===	Copyright (C) 2001-2016 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.api;

import org.fao.geonet.utils.TransformerFactoryFactory;
import org.jdom.Element;
import org.jdom.transform.JDOMSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.xml.AbstractXmlHttpMessageConverter;

import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

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
            transformer.transform(new JDOMSource((Element) t), result);
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
