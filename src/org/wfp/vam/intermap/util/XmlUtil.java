/**
 * XmlUtil.java
 *
 * @author ETj: Decompiled by Jad v1.5.8e.
 */

package org.wfp.vam.intermap.util;

import org.apache.xpath.XPathAPI;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;

/**
 * @author ETj: Decompiled by Jad v1.5.8e.
 */
public class XmlUtil
{
	public static Element getElement(Element xml, String xpath)
        throws Exception
    {
        DOMOutputter outputter = new DOMOutputter();
        org.w3c.dom.Document w3cDoc = outputter.output(new Document((Element)xml.clone()));
        org.w3c.dom.Node result = XPathAPI.selectSingleNode(w3cDoc, xpath);
        if(result instanceof org.w3c.dom.Element)
        {
            DOMBuilder builder = new DOMBuilder();
            return builder.build((org.w3c.dom.Element)result);
        }
		else
        {
            return null;
        }
    }
}

