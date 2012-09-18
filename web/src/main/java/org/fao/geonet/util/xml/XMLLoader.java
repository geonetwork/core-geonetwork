package org.fao.geonet.util.xml;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.StringReader;

/**
 * Created with IntelliJ IDEA.
 * User: heikki
 * Date: 6/4/12
 * Time: 7:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class XMLLoader {

    public static Element load(String s) throws Exception {
        //SAXBuilder saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);
        //saxBuilder.setFeature("http://xml.org/sax/features/namespaces", false);
        //Reader stringReader = new StringReader(s);
        //Document jdomDocument = saxBuilder.build(stringReader);

        SAXBuilder builder=new SAXBuilder();

        Document doc = builder.build(new StringReader(s));


        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        DefaultHandler handler = new DefaultHandler() ;

        saxParser.parse(new ByteArrayInputStream(s.getBytes()), handler);

        return new Element("zz");//Element)jdomDocument.getRootElement().detach();

    }
}
