/**
 * XmlTransform.java
 *
 */

package org.wfp.vam.intermap.util;

import java.io.File;
import java.io.OutputStream;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

/**
 * @author ETj: Decompiled by Jad v1.5.8e.
 */
public class XmlTransformer
{
    public static Element transform(Element xml, String styleSheet)
        throws Exception
    {
        return transform(xml, ((Source) (new StreamSource(styleSheet))));
    }

    public static Element transform(Element xml, Element styleSheet)
        throws Exception
    {
        return transform(xml, ((Source) (new JDOMSource(new Document((Element)styleSheet.detach())))));
    }

    public static Element transform(Element xml, Source srcSheet)
        throws Exception
    {
        TransformerFactory transFact = TransformerFactory.newInstance();
        Source srcXml = new JDOMSource(new Document((Element)xml.detach()));
        JDOMResult resXml = new JDOMResult();
        Transformer t = transFact.newTransformer(srcSheet);
        t.transform(srcXml, resXml);
        return (Element)resXml.getDocument().getRootElement().detach();
    }

    public static void transform(Element xml, String styleSheet, OutputStream out)
        throws Exception
    {
        transform(xml, ((Source) (new StreamSource(new File(styleSheet)))), out);
    }

    public static void transform(Element xml, Element styleSheet, OutputStream out)
        throws Exception
    {
        transform(xml, ((Source) (new JDOMSource(new Document((Element)styleSheet.detach())))), out);
    }

    public static void transform(Element xml, Source srcSheet, OutputStream out)
        throws Exception
    {
        TransformerFactory transFact = TransformerFactory.newInstance();
        Source srcXml = new JDOMSource(new Document((Element)xml.detach()));
        javax.xml.transform.Result resStream = new StreamResult(out);
        Transformer t = transFact.newTransformer(srcSheet);
        t.transform(srcXml, resStream);
    }

}

