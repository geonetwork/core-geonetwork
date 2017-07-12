/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;

/**
 * A simple container class for some add methods in {@link EditLib} Created by Jesse on 12/10/13.
 */
public class AddElemValue {
    private final String stringValue;
    private final Element nodeValue;

    public AddElemValue(String stringValue) throws JDOMException, IOException {
        String finalStringVal = stringValue;
        Element finalNodeVal = null;

        if (Xml.isXMLLike(stringValue)) {
            try {
                finalNodeVal = Xml.loadString(stringValue, false);
                finalStringVal = null;
            } catch (JDOMException e) {
                Log.debug(Geonet.EDITORADDELEMENT, "Invalid XML fragment to insert " + stringValue + ". Error is: " + e.getMessage(), e);
                throw e;
            } catch (IOException e) {
                Log.error(Geonet.EDITORADDELEMENT, "Error with XML fragment to insert " + stringValue + ". Error is: " + e.getMessage(), e);
                throw e;
            }
        }
        this.nodeValue = finalNodeVal;
        this.stringValue = finalStringVal;
    }

    public AddElemValue(Element nodeValue) {
        this.nodeValue = nodeValue;
        this.stringValue = null;
    }

    public boolean isXml() {
        return nodeValue != null;
    }

    public String getStringValue() {
        return stringValue;
    }

    public Element getNodeValue() {
        return nodeValue;
    }
}
