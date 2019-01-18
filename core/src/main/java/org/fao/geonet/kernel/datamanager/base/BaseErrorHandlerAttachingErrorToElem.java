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
