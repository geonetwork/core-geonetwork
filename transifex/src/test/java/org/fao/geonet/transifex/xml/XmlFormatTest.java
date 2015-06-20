package org.fao.geonet.transifex.xml;

import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Jesse on 6/18/2015.
 */
public class XmlFormatTest {

    @Test
    public void testConfigure() throws Exception {
        throw new UnsupportedOperationException("To Implement");
    }

    @Test
    public void testCreateId() throws Exception {
        Element xml = Xml.loadString("<labels>\n"
                                     + "\t<label>\n"
                                     + "\t\t<element name=\"code\" id=\"207.0\" context=\"gmd:MD_Identifier\">\n"
                                     + "\t\t\t<label>Code</label>\n"
                                     + "\t\t\t<description>Alphanumeric value identifying an instance in the namespace</description>\n"
                                     + "\t\t\t<help>alphanumeric value identifying an instance in the namespace</help>\n"
                                     + "\t\t</element>\n"
                                     + "\t</label>\n"
                                     + "</labels>", false);
        XmlFormat format = new XmlFormat();
        String id = format.createId(xml.getChild("label").getChild("element"));
        assertEquals("labels/label/element[name=\"code\",id=\"207.0\",context=\"gmd:MD_Identifier\"]", id);
    }

}