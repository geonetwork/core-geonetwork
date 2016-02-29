package org.fao.geonet.kernel;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;

/**
 * A simple container class for some add methods in {@link EditLib}
 * Created by Jesse on 12/10/13.
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
                Log.debug(Geonet.EDITORADDELEMENT, "Invalid XML fragment to insert " + stringValue + ". Error is: " + e.getMessage());
                throw e;
            } catch (IOException e) {
                e.printStackTrace();
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
