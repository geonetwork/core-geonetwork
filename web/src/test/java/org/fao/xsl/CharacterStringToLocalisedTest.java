package org.fao.xsl;

import static org.junit.Assert.*;

import java.util.List;

import jeeves.utils.Xml;

import org.fao.geonet.util.XslUtil;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Test;

public class CharacterStringToLocalisedTest {

    @Test
    public void convertCharacterstrings() throws Exception {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp+"/xsl/characterstring-to-localisedcharacterstring.xsl";
        String testData = "/data/iso19139/contact_with_linkage.xml";
        Element data = TransformationTestSupport.transform(getClass(), pathToXsl, testData );

        assertNoLocalisationString(data, "gmd:language");
        assertLocalisationString(data, ".//che:CHE_CI_ResponsibleParty/gmd:organisationName");
        assertLocalisationString(data, ".//che:CHE_CI_ResponsibleParty/che:organisationAcronym");
        assertLocalisationURL(data, ".//che:CHE_CI_ResponsibleParty//gmd:linkage");
        assertNoLocalisationString(data, ".//che:CHE_CI_ResponsibleParty//gmd:electronicMailAddress");
        assertNoLocalisationString(data, "./gmd:dataSetURI");

        assertNoLocalisationString(data, ".//gmd:distributionFormat//gmd:name");
        assertNoLocalisationString(data, ".//gmd:distributionFormat//gmd:version");
    }
    @Test
    public void convertGmdLinkageIncorrectlyEmbeds() throws Exception
    {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp+"/xsl/characterstring-to-localisedcharacterstring.xsl";

        Element testData = Xml.loadString(
                "<gmd:linkage xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xsi:type=\"che:PT_FreeURL_PropertyType\">" +
                "<gmd:URL>http://wms.geo.admin.ch/?lang=de&amp;</gmd:URL>" +
                "<che:PT_FreeURL><che:URLGroup><che:LocalisedURL locale=\"#FR\">http://wms.geo.admin.ch/?lang=fr&amp;</che:LocalisedURL></che:URLGroup></che:PT_FreeURL>" +
                "</gmd:linkage>", false);

        Element transformed = Xml.transform(testData, pathToXsl);
        
        assertEquals(1, transformed.getChildren("PT_FreeURL", XslUtil.CHE_NAMESPACE).size());
        assertEquals(1, transformed.getChildren().size());
        assertEquals(2, transformed.getChild("PT_FreeURL", XslUtil.CHE_NAMESPACE).getChildren("URLGroup", XslUtil.CHE_NAMESPACE).size());
    }
    @Test
    public void xsiAttributeAddedIfNeeded() throws Exception
    {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp+"/xsl/characterstring-to-localisedcharacterstring.xsl";

        Element testData1 = Xml.loadString(
                "<che:CHE_MD_Metadata  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" gco:isoType=\"gmd:MD_Metadata\">" +
                "   <gmd:language><gco:CharacterString>deu</gco:CharacterString></gmd:language>" +
                "   <gmd:contactInstructions><gmd:PT_FreeText><gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">Kundencenter</gmd:LocalisedCharacterString></gmd:textGroup></gmd:PT_FreeText></gmd:contactInstructions>" +
                "</che:CHE_MD_Metadata>", false);
        Element testData2 = Xml.loadString(
                "<che:CHE_MD_Metadata  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" gco:isoType=\"gmd:MD_Metadata\">" +
                "   <gmd:language><gco:CharacterString>deu</gco:CharacterString></gmd:language>" +
                "   <gmd:contactInstructions><gco:CharacterString>Kundencenter</gco:CharacterString></gmd:contactInstructions>" +
                "</che:CHE_MD_Metadata>", false);

        Element transformed1 = Xml.transform(testData1, pathToXsl).getChild("contactInstructions", XslUtil.GMD_NAMESPACE);
        Element transformed2 = Xml.transform(testData2, pathToXsl).getChild("contactInstructions", XslUtil.GMD_NAMESPACE);
        
        assertNotNull(transformed1.getAttribute("type", XslUtil.XSI_NAMESPACE));
        assertNotNull(transformed2.getAttribute("type", XslUtil.XSI_NAMESPACE));
        assertEquals(1, transformed1.getChildren("PT_FreeText", XslUtil.GMD_NAMESPACE).size());
        assertEquals(1, transformed2.getChildren("PT_FreeText", XslUtil.GMD_NAMESPACE).size());
    }
    
    @Test
    public void noEmptyLocalisations() throws Exception {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp+"/xsl/characterstring-to-localisedcharacterstring.xsl";
        String testData = "/data/non_validating/iso19139che/problemTitle_remove_charstrings.xml";
        Element data = TransformationTestSupport.transform(getClass(), pathToXsl, testData );


        List titleTextGroups = data.getChild("identificationInfo", XslUtil.GMD_NAMESPACE).
            getChild("CHE_MD_DataIdentification", XslUtil.CHE_NAMESPACE).
            getChild("citation", XslUtil.GMD_NAMESPACE).
            getChild("CI_Citation", XslUtil.GMD_NAMESPACE).
            getChild("title", XslUtil.GMD_NAMESPACE).
            getChild("PT_FreeText", XslUtil.GMD_NAMESPACE).
            getChildren("textGroup", XslUtil.GMD_NAMESPACE);
        assertEquals(1, titleTextGroups.size());
    }
    private void assertNoLocalisationString(Element data, String baseXPath) throws Exception {
        assertNoLocalisation(data, baseXPath, "gmd:PT_FreeText", "gco:CharacterString");
    }
    private void assertLocalisationString(Element data, String baseXPath) throws Exception {
        assertLocalisation(data, baseXPath, "gmd:PT_FreeText", "gco:CharacterString", "gmd:PT_FreeText_PropertyType");
    }
    private void assertNoLocalisationURL(Element data, String baseXPath) throws Exception {
        assertNoLocalisation(data, baseXPath, "che:LocalisedURL", "gmd:URL");
    }
    private void assertLocalisationURL(Element data, String baseXPath) throws Exception {
        assertLocalisation(data, baseXPath, "che:LocalisedURL", "gmd:URL", "che:PT_FreeURL_PropertyType");
    }
    
    private void assertNoLocalisation(Element data, String baseXPath, String multiple, String single) throws Exception {
        assertEquals(0, Xml.selectNodes(data, baseXPath+"//"+multiple).size());
        assertFalse(Xml.selectNodes(data, baseXPath+"//"+single).isEmpty());
    }

    private void assertLocalisation(Element data, String baseXPath, String multiple, String single,String attribute) throws Exception {
        List<Element> e = (List<Element>)Xml.selectNodes(data, baseXPath);
        for (Element elem : e) {
            assertEquals(attribute, elem.getAttributeValue("type", Namespace.getNamespace("http://www.w3.org/2001/XMLSchema-instance")));
        }
        assertEquals(0, Xml.selectNodes(data, baseXPath+"//"+single).size());
        assertFalse(Xml.selectNodes(data, baseXPath+"//"+multiple).isEmpty());
    }

}
