package org.fao.geonet.util.xml;

import org.fao.geonet.test.TestCase;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * Unit test for normalizing namespaces across XML documents.
 *
 * @author heikki doeleman
 *
 */
public class XmlLoaderTest extends TestCase {

    private XMLOutputter outputter = new XMLOutputter(Format.getRawFormat());

    public XmlLoaderTest(String name) throws Exception {
		super(name);
	}

    public void xtestLoadXMLWithoutPrefixes() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root xmlns:a=\"http://aaa\"><a:a/></root>";
        Element xmle = XMLLoader.load(xml);
    }

    public void xtestLoadXMLWithUnboundPrefixes() throws Exception {
        String xml = "<?xml version=\"1.0\"?><delta>\n" +
                "<Deleted update=\"yes\" pos=\"0:0:23:22:0\">true</Deleted>\n" +
                "<Deleted update=\"yes\" pos=\"0:0:23:10:0\">4</Deleted>\n" +
                "<Deleted update=\"yes\" pos=\"0:0:23:7:0\">f133c470-75ea-4ac6-94aa-04fb1674be25</Deleted>\n" +
                "<Deleted update=\"yes\" pos=\"0:0:23:3:0\">2012-06-04T18:46:16</Deleted>\n" +
                "<Deleted update=\"yes\" pos=\"0:0:23:2:0\">2012-05-18T23:19:13</Deleted>\n" +
                "<Deleted update=\"yes\" pos=\"0:0:23:0:0\">c904f148_73ef_43da_a596_c4f78d636748</Deleted>\n" +
                "<Deleted move=\"yes\" pos=\"0:0:19:1:1:1:3:1:7\">\n" +
                "\t<xxx1:description>\n" +
                "\t\t<xxx5:CharacterString/>\n" +
                "\t</xxx1:description>\n" +
                "</Deleted>\n" +
                "<Deleted update=\"yes\" pos=\"0:0:19:1:1:1:3:1:1:1:0\">http://localhost:8080/geonetwork/srv/en" +
                "/resources.get?id=c904f148_73ef_43da_a596_c4f78d636748&amp;fname=&amp;access=private</Deleted>\n" +
                "<Deleted pos=\"0:0:19:1:1:1:1:1:7\">\n" +
                "\t<xxx1:description>\n" +
                "\t\t<xxx5:CharacterString>Description of online rreessoouurrcece</xxx5:CharacterString>\n" +
                "\t</xxx1:description>\n" +
                "</Deleted>\n" +
                "<Deleted pos=\"0:0:19:1:1:1:1:1:5:1:0\">ZZZZZZ</Deleted>\n" +
                "<Deleted pos=\"0:0:19:1:1:1:1:1:1:1:0\">http://zzzzzzzzzzz.zz</Deleted>\n" +
                "<Deleted update=\"yes\" pos=\"0:0:17:1:1:1:1:1:0\">WORKSPACE COPY A</Deleted>\n" +
                "<Deleted update=\"yes\" pos=\"0:0:9:1:0\">2012-06-04T18:46:16</Deleted>\n" +
                "<Deleted update=\"yes\" pos=\"0:0:1:1:0\">f133c470-75ea-4ac6-94aa-04fb1674be25</Deleted>\n" +
                "<Inserted update=\"yes\" pos=\"0:0:1:1:0\">aa06176c-4622-4057-8f2d-95f3e51455cb</Inserted>\n" +
                "<Inserted update=\"yes\" pos=\"0:0:9:1:0\">2012-06-01T14:12:24</Inserted>\n" +
                "<Inserted update=\"yes\" pos=\"0:0:17:1:1:1:1:1:0\">WORKSPACE COPY B</Inserted>\n" +
                "<Inserted move=\"yes\" pos=\"0:0:19:1:1:1:1:1:7\">\n" +
                "\t<xxx1:description gco:nilReason=\"missing\">\n" +
                "\t\t<xxx5:CharacterString/>\n" +
                "\t</xxx1:description>\n" +
                "</Inserted>\n" +
                "<Inserted update=\"yes\" pos=\"0:0:19:1:1:1:3:1:1:1:0\">http://localhost:8080/geonetwork/srv/en" +
                "/resources.get?id=f4bf727e_65de_4be0_b812_2a52ae8f0c89&amp;fname=&amp;access=private</Inserted>\n" +
                "<Inserted pos=\"0:0:19:1:1:1:3:1:7\">\n" +
                "\t<xxx1:description>\n" +
                "\t\t<xxx5:CharacterString/>\n" +
                "\t</xxx1:description>\n" +
                "</Inserted>\n" +
                "<Inserted update=\"yes\" pos=\"0:0:23:0:0\">f4bf727e_65de_4be0_b812_2a52ae8f0c89</Inserted>\n" +
                "<Inserted update=\"yes\" pos=\"0:0:23:2:0\">2012-06-01T14:10:53</Inserted>\n" +
                "<Inserted update=\"yes\" pos=\"0:0:23:3:0\">2012-06-01T14:12:24</Inserted>\n" +
                "<Inserted update=\"yes\" pos=\"0:0:23:7:0\">aa06176c-4622-4057-8f2d-95f3e51455cb</Inserted>\n" +
                "<Inserted update=\"yes\" pos=\"0:0:23:10:0\">0</Inserted>\n" +
                "<Inserted update=\"yes\" pos=\"0:0:23:22:0\">false</Inserted>\n" +
                "<AttributeInserted name=\"gco:nilReason\" value=\"missing\" pos=\"0:0:19:1:1:1:1:1:5\"/>\n" +
                "<AttributeInserted name=\"gco:nilReason\" value=\"missing\" pos=\"0:0:19:1:1:1:1:1:7\"/>\n" +
                "<AttributeInserted name=\"gco:nilReason\" value=\"missing\" pos=\"0:0:19:1:1:1:1:1:7\"/>\n" +
                "<AttributeUpdated nv=\"d2434e433a1050910\" name=\"gml:id\" ov=\"d627e433a1052958\" pos=\"0:0:17:1:33:1:1:1:1:1\"/>\n" +
                "</delta>";
        Element xmle = XMLLoader.load(xml);
    }
}