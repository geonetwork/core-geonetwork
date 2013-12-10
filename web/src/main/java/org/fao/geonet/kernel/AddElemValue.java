package org.fao.geonet.kernel;

import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
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

    public AddElemValue(String stringValue) {
        String finalStringVal = stringValue;
        Element finalNodeVal = null;
        if (stringValue.trim().startsWith("<")) {
            try {
                finalNodeVal = Xml.loadString(stringValue, false);
                finalStringVal = null;
            } catch (JDOMException e) {
                Log.debug(Geonet.EDITORADDELEMENT, "Invalid XML fragment to insert " + stringValue + ". Error is: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
