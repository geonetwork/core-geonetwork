package org.fao.geonet.utils;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.SAXOutputter;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Error handler that collects up validation errors.
 */
public class XmlErrorHandler extends DefaultHandler {

    private int errorCount = 0;
    private Element xpaths;
    private Namespace ns = Namespace.NO_NAMESPACE;
    protected SAXOutputter so;

    public void setSo(SAXOutputter so) {
        this.so = so;
    }

    public boolean errors() {
        return errorCount > 0;
    }

    public Element getXPaths() {
        return xpaths;
    }

    public String addMessage(SAXParseException exception, String typeOfError) {
        if (errorCount == 0) xpaths = new Element("xsderrors", ns);
        errorCount++;

        Element elem = (Element) so.getLocator().getNode();
        Element x = new Element("xpath", ns);
        try {
            String xpath = XPath.getXPath(elem);
            //-- remove the first element to ensure XPath fits XML passed with
            //-- root element
            if (xpath.startsWith("/")) {
                int ind = xpath.indexOf('/', 1);
                if (ind != -1) {
                    xpath = xpath.substring(ind + 1);
                } else {
                    xpath = "."; // error to be placed on the root element
                }
            }
            x.setText(xpath);
        } catch (JDOMException e) {
            Log.error(Log.ENGINE, e.getMessage(), e);
            x.setText("nopath");
        }
        String message = exception.getMessage() + " (Element: " + elem.getQualifiedName();
        String parentName;
        if (!elem.isRootElement()) {
            Element parent = (Element) elem.getParent();
            if (parent != null)
                parentName = parent.getQualifiedName();
            else
                parentName = "Unknown";
        } else {
            parentName = "/";
        }
        message += " with parent element: " + parentName + ")";

        Element m = new Element("message", ns).setText(message);
        Element errorType = new Element("typeOfError", ns).setText(typeOfError);
        Element errorNumber = new Element("errorNumber", ns).setText(String.valueOf(errorCount));
        Element e = new Element("error", ns);
        e.addContent(errorType);
        e.addContent(errorNumber);
        e.addContent(m);
        e.addContent(x);
        xpaths.addContent(e);
        return x.getText();
    }

    public void error(SAXParseException parseException) throws SAXException {
        addMessage(parseException, "ERROR");
    }

    public void fatalError(SAXParseException parseException) throws SAXException {
        addMessage(parseException, "FATAL ERROR");
    }

    public void warning(SAXParseException parseException) throws SAXException {
        addMessage(parseException, "WARNING");
    }

    public Namespace getNs() {
        return ns;
    }

    /**
     * Set namespace to use for report elements
     */
    public void setNs(Namespace ns) {
        this.ns = ns;
    }
}
