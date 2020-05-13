//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.datamanager.base;

import org.fao.geonet.utils.XmlErrorHandler;
import org.jdom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.HashMap;
import java.util.Map;

public class BaseErrorHandlerAttachingErrorToElem extends XmlErrorHandler {

    private ElementDecorator elementDecorator;
    private Map<Element, Element> reportsAttach= new HashMap();

    public void setElementDecorator(ElementDecorator elementDecorator) {
        this.elementDecorator = elementDecorator;
    }

    @Override
    public String addMessage(SAXParseException exception, String typeOfError) {
        String xPath = super.addMessage(exception, typeOfError);
        Element elem = (Element) so.getLocator().getNode();
        reportsAttach.put(elementDecorator.buildErrorReport("XSD", typeOfError, exception.getMessage(), xPath), elem);
        return xPath;
    }

    public interface ElementDecorator {
        Element buildErrorReport(String type, String errorCode, String message, String xpath);
    }

    public void attachReports() {
        reportsAttach.forEach((report, elem) -> elem.addContent(report));
    }
}
