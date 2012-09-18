package org.fao.geonet.kernel.diff;

import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.test.TestCase;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

/**
 * @author heikki doeleman
 */
public class DifferenceAnnotatorTest extends TestCase {

    private XMLOutputter outputter = new XMLOutputter(Format.getRawFormat());

    public DifferenceAnnotatorTest(String name) throws Exception {
        super(name);
    }

    //
    // element nodes
    //

    public void testInsertNode() throws Exception {
        String xml1 = "<root />";
        String xml2 = "<root><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Log.debug(Geonet.DIFF, "*** diff:\n" + Xml.getString(diff));

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for inserting a node", xml1, outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for inserting a node", "<root><a xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:inserted=\"true\" class=\"0-0-0\" /></root>", outputter.outputString(annotatedTarget));
    }

    public void testDeleteNode() throws Exception {
        String xml1 = "<root><a/></root>";
        String xml2 = "<root />";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Log.debug(Geonet.DIFF, "*** diff:\n" + Xml.getString(diff));

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for deleting a node", "<root><a xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:deleted=\"true\" geonet:oldPos=\"0:0:0\" class=\"0-0-0\" /></root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for deleting a node", xml2, outputter.outputString(annotatedTarget));
    }

    public void testMixedInsertDeleteNode() throws Exception {
        String xml1 = "<root><a><b/><c/></a></root>";
        String xml2 = "<root><a><b><d/></b></a></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Log.debug(Geonet.DIFF, "*** diff:\n" + Xml.getString(diff));

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for mixed inserting and deleting a node", "<root><a><b /><c xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:deleted=\"true\" geonet:oldPos=\"0:0:0:1\" class=\"0-0-0-1\" /></a></root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for mixed inserting and deleting a node", "<root><a><b><d xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:inserted=\"true\" class=\"0-0-0-0-0\" /></b></a></root>", outputter.outputString(annotatedTarget));
    }

    //
    // text nodes
    //

    public void testInsertTextNode() throws Exception {
        String xml1 = "<root><a /></root>";
        String xml2 = "<root><a>xxx</a></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for inserting a text node", xml1, outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for inserting a text node", "<root><a xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:insertedText=\"1\" class=\"0-0-0-0\">xxx</a></root>", outputter.outputString(annotatedTarget));
    }

    public void testInsertTextNode2() throws Exception {
        String xml1 = "<root><a>xxx<b>yyy</b><c /></a></root>";
        String xml2 = "<root><a>xxx<b>yyy</b><c />zzz</a></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for inserting a text node", xml1, outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for inserting a text node", "<root><a xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:insertedText=\"4\" class=\"0-0-0-3\">xxx<b>yyy</b><c />zzz</a></root>", outputter.outputString(annotatedTarget));
    }

    public void testDeleteTextNode() throws Exception {
        String xml1 = "<root><a>xxx<b>yyy</b><c />zzz</a></root>";
        String xml2 = "<root><a>xxx<b>yyy</b><c /></a></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for deleting a text node", "<root><a xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:deletedText=\"4\" geonet:oldPos=\"0:0:0:3\" class=\"0-0-0-3\">xxx<b>yyy</b><c />zzz</a></root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for deleting a text node", xml2 , outputter.outputString(annotatedTarget));
    }

    public void testReplaceTextNode() throws Exception {
        String xml1 = "<root><a>xxx<b>yyy</b><c />zzz</a></root>";
        String xml2 = "<root><a>xxx<b>yyy</b><c />qqq</a></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Log.debug(Geonet.DIFF, "*** diff:\n" + Xml.getString(diff));

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for replacing a text node", "<root><a xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:updatedText=\"4\" geonet:oldPos=\"0:0:0:3\" class=\"0-0-0-3\">xxx<b>yyy</b><c />zzz</a></root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for replacing a text node", "<root><a xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:updatedText=\"4\" class=\"0-0-0-3\">xxx<b>yyy</b><c />qqq</a></root>" , outputter.outputString(annotatedTarget));
    }

    public void testUpdateTextNode() throws Exception {
        String xml1 = "<root><a>xxx<b>yyy</b><c />zzz</a></root>";
        String xml2 = "<root><a>xxx<b>yyy</b><c />zzzzzz</a></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for updating a text node", "<root><a xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:updatedText=\"4\" geonet:oldPos=\"0:0:0:3\" class=\"0-0-0-3\">xxx<b>yyy</b><c />zzz</a></root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for updating a text node", "<root><a xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:updatedText=\"4\" class=\"0-0-0-3\">xxx<b>yyy</b><c />zzzzzz</a></root>" , outputter.outputString(annotatedTarget));
    }

    public void testSelectNodeToAnnotate() throws Exception {
        String xpath = "/node()[1]/node()[12]/node()[27]/node()[3]/text()";
        String xml$ = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:xsi=\"http://www.w3" +
                ".org/2001/XMLSchema-instance\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gts=\"http://www" +
                ".isotc211.org/2005/gts\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:geonet=\"http://www" +
                ".fao.org/geonetwork\">" +
                    "<gmd:fileIdentifier>" +
                        "<gco:CharacterString>cc1b80d8-a1fb-48bb-9d0b-575b9ffea586</gco:CharacterString>" +
                    "</gmd:fileIdentifier>" +
                    "<gmd:language>" +
                        "<gmd:LanguageCode codeList=\"http://www.loc.gov/standards/iso639-2/\" codeListValue=\"eng\" />" +
                    "</gmd:language>" +
                    "<gmd:characterSet>" +
                        "<gmd:MD_CharacterSetCode codeListValue=\"utf8\" codeList=\"http://standards.iso" +
                            ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                            ".xml#MD_CharacterSetCode\" />" +
                    "</gmd:characterSet>" +
                "<gmd:contact>\n" +
                "<gmd:CI_ResponsibleParty>\n" +
                "<gmd:individualName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:individualName>\n" +
                "<gmd:organisationName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:organisationName>\n" +
                "<gmd:positionName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:positionName>\n" +
                "<gmd:contactInfo>\n" +
                "<gmd:CI_Contact>\n" +
                "<gmd:phone>\n" +
                "<gmd:CI_Telephone>\n" +
                "<gmd:voice gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:voice>\n" +
                "<gmd:facsimile gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:facsimile>\n" +
                "</gmd:CI_Telephone>\n" +
                "</gmd:phone>\n" +
                "<gmd:address>\n" +
                "<gmd:CI_Address>\n" +
                "<gmd:deliveryPoint gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:deliveryPoint>\n" +
                "<gmd:city gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:city>\n" +
                "<gmd:administrativeArea gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:administrativeArea>\n" +
                "<gmd:postalCode gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:postalCode>\n" +
                "<gmd:country gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:country>\n" +
                "<gmd:electronicMailAddress gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:electronicMailAddress>\n" +
                "</gmd:CI_Address>\n" +
                "</gmd:address>\n" +
                "</gmd:CI_Contact>\n" +
                "</gmd:contactInfo>\n" +
                "<gmd:role>\n" +
                "<gmd:CI_RoleCode codeListValue=\"pointOfContact\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_RoleCode\" />\n" +
                "</gmd:role>\n" +
                "</gmd:CI_ResponsibleParty>\n" +
                "</gmd:contact>\n" +
                "<gmd:dateStamp>\n" +
                "<gco:DateTime>2012-06-20T14:26:49</gco:DateTime>\n" +
                "</gmd:dateStamp>\n" +
                "<gmd:metadataStandardName>\n" +
                "<gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>\n" +
                "</gmd:metadataStandardName>\n" +
                "<gmd:metadataStandardVersion>\n" +
                "<gco:CharacterString>1.0</gco:CharacterString>\n" +
                "</gmd:metadataStandardVersion>\n" +
                "<gmd:referenceSystemInfo>\n" +
                "<gmd:MD_ReferenceSystem>\n" +
                "<gmd:referenceSystemIdentifier>\n" +
                "<gmd:RS_Identifier>\n" +
                "<gmd:code>\n" +
                "<gco:CharacterString>WGS 1984</gco:CharacterString>\n" +
                "</gmd:code>\n" +
                "</gmd:RS_Identifier>\n" +
                "</gmd:referenceSystemIdentifier>\n" +
                "</gmd:MD_ReferenceSystem>\n" +
                "</gmd:referenceSystemInfo>\n" +
                "<gmd:identificationInfo>\n" +
                "<gmd:MD_DataIdentification>\n" +
                "<gmd:citation>\n" +
                "<gmd:CI_Citation>\n" +
                "<gmd:title>\n" +
                "<gco:CharacterString>AA</gco:CharacterString>\n" +
                "</gmd:title>\n" +
                "<gmd:date>\n" +
                "<gmd:CI_Date>\n" +
                "<gmd:date>\n" +
                "<gco:DateTime />\n" +
                "</gmd:date>\n" +
                "<gmd:dateType>\n" +
                "<gmd:CI_DateTypeCode codeListValue=\"publication\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_DateTypeCode\" />\n" +
                "</gmd:dateType>\n" +
                "</gmd:CI_Date>\n" +
                "</gmd:date>\n" +
                "<gmd:edition gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:edition>\n" +
                "<gmd:presentationForm>\n" +
                "<gmd:CI_PresentationFormCode codeListValue=\"mapDigital\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_PresentationFormCode\" />\n" +
                "</gmd:presentationForm>\n" +
                "</gmd:CI_Citation>\n" +
                "</gmd:citation>\n" +
                "<gmd:abstract>\n" +
                "<gco:CharacterString>The ISO19115 metadata standard is the preferred metadata standard to use. If " +
                "unsure what templates to start with, use this one.</gco:CharacterString>\n" +
                "</gmd:abstract>\n" +
                "<gmd:purpose gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:purpose>\n" +
                "<gmd:status>\n" +
                "<gmd:MD_ProgressCode codeListValue=\"onGoing\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_ProgressCode\" />\n" +
                "</gmd:status>\n" +
                "<gmd:pointOfContact>\n" +
                "<gmd:CI_ResponsibleParty>\n" +
                "<gmd:individualName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:individualName>\n" +
                "<gmd:organisationName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:organisationName>\n" +
                "<gmd:positionName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:positionName>\n" +
                "<gmd:contactInfo>\n" +
                "<gmd:CI_Contact>\n" +
                "<gmd:phone>\n" +
                "<gmd:CI_Telephone>\n" +
                "<gmd:voice gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:voice>\n" +
                "<gmd:facsimile gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:facsimile>\n" +
                "</gmd:CI_Telephone>\n" +
                "</gmd:phone>\n" +
                "<gmd:address>\n" +
                "<gmd:CI_Address>\n" +
                "<gmd:deliveryPoint gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:deliveryPoint>\n" +
                "<gmd:city gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:city>\n" +
                "<gmd:administrativeArea gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:administrativeArea>\n" +
                "<gmd:postalCode gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:postalCode>\n" +
                "<gmd:country gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:country>\n" +
                "<gmd:electronicMailAddress gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:electronicMailAddress>\n" +
                "</gmd:CI_Address>\n" +
                "</gmd:address>\n" +
                "</gmd:CI_Contact>\n" +
                "</gmd:contactInfo>\n" +
                "<gmd:role>\n" +
                "<gmd:CI_RoleCode codeListValue=\"originator\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_RoleCode\" />\n" +
                "</gmd:role>\n" +
                "</gmd:CI_ResponsibleParty>\n" +
                "</gmd:pointOfContact>\n" +
                "<gmd:resourceMaintenance>\n" +
                "<gmd:MD_MaintenanceInformation>\n" +
                "<gmd:maintenanceAndUpdateFrequency>\n" +
                "<gmd:MD_MaintenanceFrequencyCode codeListValue=\"asNeeded\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_MaintenanceFrequencyCode\" />\n" +
                "</gmd:maintenanceAndUpdateFrequency>\n" +
                "</gmd:MD_MaintenanceInformation>\n" +
                "</gmd:resourceMaintenance>\n" +
                "<gmd:graphicOverview>\n" +
                "<gmd:MD_BrowseGraphic>\n" +
                "<gmd:fileName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:fileName>\n" +
                "<gmd:fileDescription>\n" +
                "<gco:CharacterString>thumbnail</gco:CharacterString>\n" +
                "</gmd:fileDescription>\n" +
                "</gmd:MD_BrowseGraphic>\n" +
                "</gmd:graphicOverview>\n" +
                "<gmd:graphicOverview>\n" +
                "<gmd:MD_BrowseGraphic>\n" +
                "<gmd:fileName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:fileName>\n" +
                "<gmd:fileDescription>\n" +
                "<gco:CharacterString>large_thumbnail</gco:CharacterString>\n" +
                "</gmd:fileDescription>\n" +
                "</gmd:MD_BrowseGraphic>\n" +
                "</gmd:graphicOverview>\n" +
                "<gmd:descriptiveKeywords>\n" +
                "<gmd:MD_Keywords>\n" +
                "<gmd:keyword gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:keyword>\n" +
                "<gmd:type>\n" +
                "<gmd:MD_KeywordTypeCode codeListValue=\"theme\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_KeywordTypeCode\" />\n" +
                "</gmd:type>\n" +
                "</gmd:MD_Keywords>\n" +
                "</gmd:descriptiveKeywords>\n" +
                "<gmd:descriptiveKeywords>\n" +
                "<gmd:MD_Keywords>\n" +
                "<gmd:keyword>\n" +
                "<gco:CharacterString>World</gco:CharacterString>\n" +
                "</gmd:keyword>\n" +
                "<gmd:type>\n" +
                "<gmd:MD_KeywordTypeCode codeListValue=\"place\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_KeywordTypeCode\" />\n" +
                "</gmd:type>\n" +
                "</gmd:MD_Keywords>\n" +
                "</gmd:descriptiveKeywords>\n" +
                "<gmd:resourceConstraints>\n" +
                "<gmd:MD_LegalConstraints>\n" +
                "<gmd:accessConstraints>\n" +
                "<gmd:MD_RestrictionCode codeListValue=\"copyright\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_RestrictionCode\" />\n" +
                "</gmd:accessConstraints>\n" +
                "<gmd:useConstraints>\n" +
                "<gmd:MD_RestrictionCode codeListValue=\"\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_RestrictionCode\" />\n" +
                "</gmd:useConstraints>\n" +
                "<gmd:otherConstraints gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:otherConstraints>\n" +
                "</gmd:MD_LegalConstraints>\n" +
                "</gmd:resourceConstraints>\n" +
                "<gmd:spatialRepresentationType>\n" +
                "<gmd:MD_SpatialRepresentationTypeCode codeListValue=\"vector\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_SpatialRepresentationTypeCode\" />\n" +
                "</gmd:spatialRepresentationType>\n" +
                "<gmd:spatialResolution>\n" +
                "<gmd:MD_Resolution>\n" +
                "<gmd:equivalentScale>\n" +
                "<gmd:MD_RepresentativeFraction>\n" +
                "<gmd:denominator>\n" +
                "<gco:Integer />\n" +
                "</gmd:denominator>\n" +
                "</gmd:MD_RepresentativeFraction>\n" +
                "</gmd:equivalentScale>\n" +
                "</gmd:MD_Resolution>\n" +
                "</gmd:spatialResolution>\n" +
                "<gmd:language>\n" +
                "<gmd:LanguageCode codeList=\"http://www.loc.gov/standards/iso639-2/\" codeListValue=\"eng\" />\n" +
                "</gmd:language>\n" +
                "<gmd:characterSet>\n" +
                "<gmd:MD_CharacterSetCode codeListValue=\"utf8\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_CharacterSetCode\" />\n" +
                "</gmd:characterSet>\n" +
                "<gmd:topicCategory>\n" +
                "<gmd:MD_TopicCategoryCode>boundaries</gmd:MD_TopicCategoryCode>\n" +
                "</gmd:topicCategory>\n" +
                "<gmd:extent>\n" +
                "<gmd:EX_Extent>\n" +
                "<gmd:temporalElement>\n" +
                "<gmd:EX_TemporalExtent>\n" +
                "<gmd:extent>\n" +
                "<gml:TimePeriod gml:id=\"d735e433a1052958\">\n" +
                "<gml:beginPosition />\n" +
                "<gml:endPosition />\n" +
                "</gml:TimePeriod>\n" +
                "</gmd:extent>\n" +
                "</gmd:EX_TemporalExtent>\n" +
                "</gmd:temporalElement>\n" +
                "</gmd:EX_Extent>\n" +
                "</gmd:extent>\n" +
                "<gmd:extent>\n" +
                "<gmd:EX_Extent>\n" +
                "<gmd:geographicElement>\n" +
                "<gmd:EX_GeographicBoundingBox>\n" +
                "<gmd:westBoundLongitude>\n" +
                "<gco:Decimal>-180</gco:Decimal>\n" +
                "</gmd:westBoundLongitude>\n" +
                "<gmd:eastBoundLongitude>\n" +
                "<gco:Decimal>180</gco:Decimal>\n" +
                "</gmd:eastBoundLongitude>\n" +
                "<gmd:southBoundLatitude>\n" +
                "<gco:Decimal>-90</gco:Decimal>\n" +
                "</gmd:southBoundLatitude>\n" +
                "<gmd:northBoundLatitude>\n" +
                "<gco:Decimal>90</gco:Decimal>\n" +
                "</gmd:northBoundLatitude>\n" +
                "</gmd:EX_GeographicBoundingBox>\n" +
                "</gmd:geographicElement>\n" +
                "</gmd:EX_Extent>\n" +
                "</gmd:extent>\n" +
                "<gmd:supplementalInformation>\n" +
                "<gco:CharacterString>You can customize the template to suit your needs. You can add and remove " +
                "fields and fill out default information (e.g. contact details). Fie\n" +
                "lds you can not change in the default view may be accessible in the more comprehensive (and more " +
                "complex) advanced view. You can even use the XML editor to create custom\n" +
                "structures, but they have to be validated by the system, " +
                "so know what you do :-)</gco:CharacterString>\n" +
                "</gmd:supplementalInformation>\n" +
                "</gmd:MD_DataIdentification>\n" +
                "</gmd:identificationInfo>\n" +
                "<gmd:distributionInfo>\n" +
                "<gmd:MD_Distribution>\n" +
                "<gmd:transferOptions>\n" +
                "<gmd:MD_DigitalTransferOptions>\n" +
                "<gmd:onLine>\n" +
                "<gmd:CI_OnlineResource>\n" +
                "<gmd:linkage>\n" +
                "<gmd:URL />\n" +
                "</gmd:linkage>\n" +
                "<gmd:protocol>\n" +
                "<gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>\n" +
                "</gmd:protocol>\n" +
                "<gmd:name gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:name>\n" +
                "<gmd:description gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:description>\n" +
                "</gmd:CI_OnlineResource>\n" +
                "</gmd:onLine>\n" +
                "<gmd:onLine>\n" +
                "<gmd:CI_OnlineResource>\n" +
                "<gmd:linkage>\n" +
                "<gmd:URL>http://localhost:8080/geonetwork/srv/en/resources" +
                ".get?id=h95a8c4c_9038_43aa_a005_c943bc731c8d&amp;fname=&amp;access=private</gmd:URL>\n" +
                "</gmd:linkage>\n" +
                "<gmd:protocol>\n" +
                "<gco:CharacterString>WWW:DOWNLOAD-1.0-http--download</gco:CharacterString>\n" +
                "</gmd:protocol>\n" +
                "<gmd:name>\n" +
                "<gmx:MimeFileType xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" type=\"\" />\n" +
                "</gmd:name>\n" +
                "<gmd:description>\n" +
                "<gco:CharacterString />\n" +
                "</gmd:description>\n" +
                "</gmd:CI_OnlineResource>\n" +
                "</gmd:onLine>\n" +
                "<gmd:onLine>\n" +
                "<gmd:CI_OnlineResource>\n" +
                "<gmd:linkage>\n" +
                "<gmd:URL />\n" +
                "</gmd:linkage>\n" +
                "<gmd:protocol>\n" +
                "<gco:CharacterString>OGC:WMS-1.1.1-http-get-map</gco:CharacterString>\n" +
                "</gmd:protocol>\n" +
                "<gmd:name gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:name>\n" +
                "<gmd:description gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:description>\n" +
                "</gmd:CI_OnlineResource>\n" +
                "</gmd:onLine>\n" +
                "</gmd:MD_DigitalTransferOptions>\n" +
                "</gmd:transferOptions>\n" +
                "</gmd:MD_Distribution>\n" +
                "</gmd:distributionInfo>\n" +
                "<gmd:dataQualityInfo>\n" +
                "<gmd:DQ_DataQuality>\n" +
                "<gmd:scope>\n" +
                "<gmd:DQ_Scope>\n" +
                "<gmd:level>\n" +
                "<gmd:MD_ScopeCode codeListValue=\"dataset\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_ScopeCode\" />\n" +
                "</gmd:level>\n" +
                "</gmd:DQ_Scope>\n" +
                "</gmd:scope>\n" +
                "<gmd:lineage>\n" +
                "<gmd:LI_Lineage>\n" +
                "<gmd:statement gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:statement>\n" +
                "</gmd:LI_Lineage>\n" +
                "</gmd:lineage>\n" +
                "</gmd:DQ_DataQuality>\n" +
                "</gmd:dataQualityInfo>\n" +
                "<geonet:info>\n" +
                "<id>h95a8c4c_9038_43aa_a005_c943bc731c8d</id>\n" +
                "<schema>iso19139</schema>\n" +
                "<createDate>2012-06-20T14:25:30</createDate>\n" +
                "<changeDate>2012-06-20T14:26:49</changeDate>\n" +
                "<isTemplate>n</isTemplate>\n" +
                "<title />\n" +
                "<source>ac116fbb-8da1-4beb-9e22-826b8b891c6d</source>\n" +
                "<uuid>cc1b80d8-a1fb-48bb-9d0b-575b9ffea586</uuid>\n" +
                "<isHarvested>n</isHarvested>\n" +
                "<isLocked>y</isLocked>\n" +
                "<popularity>1</popularity>\n" +
                "<rating>0</rating>\n" +
                "<displayOrder />\n" +
                "<view>true</view>\n" +
                "<notify>true</notify>\n" +
                "<download>true</download>\n" +
                "<dynamic>true</dynamic>\n" +
                "<featured>true</featured>\n" +
                "<status>2</status>\n" +
                "<statusName>approved</statusName>\n" +
                "<edit>true</edit>\n" +
                "<owner>true</owner>\n" +
                "<isPublishedToAll>false</isPublishedToAll>\n" +
                "<ownerId>1</ownerId>\n" +
                "<ownername>admin</ownername>\n" +
                "<valid_details>\n" +
                "<type>schematron-rules-geonetwork</type>\n" +
                "<status>1</status>\n" +
                "<ratio>0/1</ratio>\n" +
                "</valid_details>\n" +
                "<valid_details>\n" +
                "<type>schematron-rules-iso</type>\n" +
                "<status>0</status>\n" +
                "<ratio>6/59</ratio>\n" +
                "</valid_details>\n" +
                "<valid_details>\n" +
                "<type>xsd</type>\n" +
                "<status>0</status>\n" +
                "<ratio />\n" +
                "</valid_details>\n" +
                "<valid>0</valid>\n" +
                "<baseUrl>http://localhost:8080/geonetwork</baseUrl>\n" +
                "<locService>/srv/en</locService>\n" +
                "</geonet:info>\n" +
                "</gmd:MD_Metadata>";
        Element xmlE = Xml.loadString(xml$, false);
        Document xmlD = new Document(xmlE);

        for(int i = 1; i <= 12; i++) {
            DifferenceAnnotator.selectNodeToAnnotate("/node()[1]/node()["+ i + "]", xmlD);
        }


        String xpath1 = "/node()[1]";
        String xpath2 = "/node()[1]/node()[12]";
        String xpath3 = "/node()[1]/node()[12]/node()[27]";
        String xpath4 = "/node()[1]/node()[12]/node()[27]/node()[3]";
        String xpath5 = "/node()[1]/node()[12]/node()[27]/node()[3]/text()";
        String xpath6 = "/node()[1]/node()[12]/node()[27]/node()[3]/text()/..";

        Object selected = DifferenceAnnotator.selectNodeToAnnotate(xpath1, xmlD);
        selected = DifferenceAnnotator.selectNodeToAnnotate(xpath2, xmlD);
        selected = DifferenceAnnotator.selectNodeToAnnotate(xpath3, xmlD);
        selected = DifferenceAnnotator.selectNodeToAnnotate(xpath4, xmlD);
        selected = DifferenceAnnotator.selectNodeToAnnotate(xpath5, xmlD);
        selected = DifferenceAnnotator.selectNodeToAnnotate(xpath6, xmlD);


        selected = DifferenceAnnotator.selectNodeToAnnotate(xpath, xmlD);
        assertTrue(selected instanceof Text);
        assertNotNull(selected);
    }

    public void testSelectNodeToAnnotate2() throws Exception {
        String xpath = "/node()[1]/node()[12]/node()[27]/node()[3]/text()";
        String xml$ = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:xsi=\"http://www.w3" +
                ".org/2001/XMLSchema-instance\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gts=\"http://www" +
                ".isotc211.org/2005/gts\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:geonet=\"http://www" +
                ".fao.org/geonetwork\">\n" +
                "<gmd:fileIdentifier>\n" +
                "<gco:CharacterString>cc1b80d8-a1fb-48bb-9d0b-575b9ffea586</gco:CharacterString>\n" +
                "</gmd:fileIdentifier>\n" +
                "<gmd:language>\n" +
                "<gmd:LanguageCode codeList=\"http://www.loc.gov/standards/iso639-2/\" codeListValue=\"eng\" />\n" +
                "</gmd:language>\n" +
                "<gmd:characterSet>\n" +
                "<gmd:MD_CharacterSetCode codeListValue=\"utf8\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_CharacterSetCode\" />\n" +
                "</gmd:characterSet>\n" +
                "<gmd:contact>\n" +
                "<gmd:CI_ResponsibleParty>\n" +
                "<gmd:individualName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:individualName>\n" +
                "<gmd:organisationName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:organisationName>\n" +
                "<gmd:positionName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:positionName>\n" +
                "<gmd:contactInfo>\n" +
                "<gmd:CI_Contact>\n" +
                "<gmd:phone>\n" +
                "<gmd:CI_Telephone>\n" +
                "<gmd:voice gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:voice>\n" +
                "<gmd:facsimile gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:facsimile>\n" +
                "</gmd:CI_Telephone>\n" +
                "</gmd:phone>\n" +
                "<gmd:address>\n" +
                "<gmd:CI_Address>\n" +
                "<gmd:deliveryPoint gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:deliveryPoint>\n" +
                "<gmd:city gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:city>\n" +
                "<gmd:administrativeArea gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:administrativeArea>\n" +
                "<gmd:postalCode gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:postalCode>\n" +
                "<gmd:country gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:country>\n" +
                "<gmd:electronicMailAddress gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:electronicMailAddress>\n" +
                "</gmd:CI_Address>\n" +
                "</gmd:address>\n" +
                "</gmd:CI_Contact>\n" +
                "</gmd:contactInfo>\n" +
                "<gmd:role>\n" +
                "<gmd:CI_RoleCode codeListValue=\"pointOfContact\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_RoleCode\" />\n" +
                "</gmd:role>\n" +
                "</gmd:CI_ResponsibleParty>\n" +
                "</gmd:contact>\n" +
                "<gmd:dateStamp>\n" +
                "<gco:DateTime>2012-06-20T14:26:49</gco:DateTime>\n" +
                "</gmd:dateStamp>\n" +
                "<gmd:metadataStandardName>\n" +
                "<gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>\n" +
                "</gmd:metadataStandardName>\n" +
                "<gmd:metadataStandardVersion>\n" +
                "<gco:CharacterString>1.0</gco:CharacterString>\n" +
                "</gmd:metadataStandardVersion>\n" +
                "<gmd:referenceSystemInfo>\n" +
                "<gmd:MD_ReferenceSystem>\n" +
                "<gmd:referenceSystemIdentifier>\n" +
                "<gmd:RS_Identifier>\n" +
                "<gmd:code>\n" +
                "<gco:CharacterString>WGS 1984</gco:CharacterString>\n" +
                "</gmd:code>\n" +
                "</gmd:RS_Identifier>\n" +
                "</gmd:referenceSystemIdentifier>\n" +
                "</gmd:MD_ReferenceSystem>\n" +
                "</gmd:referenceSystemInfo>\n" +
                "<gmd:identificationInfo>\n" +
                "<gmd:MD_DataIdentification>\n" +
                "<gmd:citation>\n" +
                "<gmd:CI_Citation>\n" +
                "<gmd:title>\n" +
                "<gco:CharacterString>AA</gco:CharacterString>\n" +
                "</gmd:title>\n" +
                "<gmd:date>\n" +
                "<gmd:CI_Date>\n" +
                "<gmd:date>\n" +
                "<gco:DateTime />\n" +
                "</gmd:date>\n" +
                "<gmd:dateType>\n" +
                "<gmd:CI_DateTypeCode codeListValue=\"publication\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_DateTypeCode\" />\n" +
                "</gmd:dateType>\n" +
                "</gmd:CI_Date>\n" +
                "</gmd:date>\n" +
                "<gmd:edition gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:edition>\n" +
                "<gmd:presentationForm>\n" +
                "<gmd:CI_PresentationFormCode codeListValue=\"mapDigital\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_PresentationFormCode\" />\n" +
                "</gmd:presentationForm>\n" +
                "</gmd:CI_Citation>\n" +
                "</gmd:citation>\n" +
                "<gmd:abstract>\n" +
                "<gco:CharacterString>The ISO19115 metadata standard is the preferred metadata standard to use. If " +
                "unsure what templates to start with, use this one.</gco:CharacterString>\n" +
                "</gmd:abstract>\n" +
                "<gmd:purpose gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:purpose>\n" +
                "<gmd:status>\n" +
                "<gmd:MD_ProgressCode codeListValue=\"onGoing\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_ProgressCode\" />\n" +
                "</gmd:status>\n" +
                "<gmd:pointOfContact>\n" +
                "<gmd:CI_ResponsibleParty>\n" +
                "<gmd:individualName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:individualName>\n" +
                "<gmd:organisationName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:organisationName>\n" +
                "<gmd:positionName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:positionName>\n" +
                "<gmd:contactInfo>\n" +
                "<gmd:CI_Contact>\n" +
                "<gmd:phone>\n" +
                "<gmd:CI_Telephone>\n" +
                "<gmd:voice gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:voice>\n" +
                "<gmd:facsimile gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:facsimile>\n" +
                "</gmd:CI_Telephone>\n" +
                "</gmd:phone>\n" +
                "<gmd:address>\n" +
                "<gmd:CI_Address>\n" +
                "<gmd:deliveryPoint gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:deliveryPoint>\n" +
                "<gmd:city gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:city>\n" +
                "<gmd:administrativeArea gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:administrativeArea>\n" +
                "<gmd:postalCode gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:postalCode>\n" +
                "<gmd:country gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:country>\n" +
                "<gmd:electronicMailAddress gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:electronicMailAddress>\n" +
                "</gmd:CI_Address>\n" +
                "</gmd:address>\n" +
                "</gmd:CI_Contact>\n" +
                "</gmd:contactInfo>\n" +
                "<gmd:role>\n" +
                "<gmd:CI_RoleCode codeListValue=\"originator\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_RoleCode\" />\n" +
                "</gmd:role>\n" +
                "</gmd:CI_ResponsibleParty>\n" +
                "</gmd:pointOfContact>\n" +
                "<gmd:resourceMaintenance>\n" +
                "<gmd:MD_MaintenanceInformation>\n" +
                "<gmd:maintenanceAndUpdateFrequency>\n" +
                "<gmd:MD_MaintenanceFrequencyCode codeListValue=\"asNeeded\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_MaintenanceFrequencyCode\" />\n" +
                "</gmd:maintenanceAndUpdateFrequency>\n" +
                "</gmd:MD_MaintenanceInformation>\n" +
                "</gmd:resourceMaintenance>\n" +
                "<gmd:graphicOverview>\n" +
                "<gmd:MD_BrowseGraphic>\n" +
                "<gmd:fileName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:fileName>\n" +
                "<gmd:fileDescription>\n" +
                "<gco:CharacterString>thumbnail</gco:CharacterString>\n" +
                "</gmd:fileDescription>\n" +
                "</gmd:MD_BrowseGraphic>\n" +
                "</gmd:graphicOverview>\n" +
                "<gmd:graphicOverview>\n" +
                "<gmd:MD_BrowseGraphic>\n" +
                "<gmd:fileName gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:fileName>\n" +
                "<gmd:fileDescription>\n" +
                "<gco:CharacterString>large_thumbnail</gco:CharacterString>\n" +
                "</gmd:fileDescription>\n" +
                "</gmd:MD_BrowseGraphic>\n" +
                "</gmd:graphicOverview>\n" +
                "<gmd:descriptiveKeywords>\n" +
                "<gmd:MD_Keywords>\n" +
                "<gmd:keyword gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:keyword>\n" +
                "<gmd:type>\n" +
                "<gmd:MD_KeywordTypeCode codeListValue=\"theme\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_KeywordTypeCode\" />\n" +
                "</gmd:type>\n" +
                "</gmd:MD_Keywords>\n" +
                "</gmd:descriptiveKeywords>\n" +
                "<gmd:descriptiveKeywords>\n" +
                "<gmd:MD_Keywords>\n" +
                "<gmd:keyword>\n" +
                "<gco:CharacterString>World</gco:CharacterString>\n" +
                "</gmd:keyword>\n" +
                "<gmd:type>\n" +
                "<gmd:MD_KeywordTypeCode codeListValue=\"place\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_KeywordTypeCode\" />\n" +
                "</gmd:type>\n" +
                "</gmd:MD_Keywords>\n" +
                "</gmd:descriptiveKeywords>\n" +
                "<gmd:resourceConstraints>\n" +
                "<gmd:MD_LegalConstraints>\n" +
                "<gmd:accessConstraints>\n" +
                "<gmd:MD_RestrictionCode codeListValue=\"copyright\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_RestrictionCode\" />\n" +
                "</gmd:accessConstraints>\n" +
                "<gmd:useConstraints>\n" +
                "<gmd:MD_RestrictionCode codeListValue=\"\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_RestrictionCode\" />\n" +
                "</gmd:useConstraints>\n" +
                "<gmd:otherConstraints gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:otherConstraints>\n" +
                "</gmd:MD_LegalConstraints>\n" +
                "</gmd:resourceConstraints>\n" +
                "<gmd:spatialRepresentationType>\n" +
                "<gmd:MD_SpatialRepresentationTypeCode codeListValue=\"vector\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_SpatialRepresentationTypeCode\" />\n" +
                "</gmd:spatialRepresentationType>\n" +
                "<gmd:spatialResolution>\n" +
                "<gmd:MD_Resolution>\n" +
                "<gmd:equivalentScale>\n" +
                "<gmd:MD_RepresentativeFraction>\n" +
                "<gmd:denominator>\n" +
                "<gco:Integer />\n" +
                "</gmd:denominator>\n" +
                "</gmd:MD_RepresentativeFraction>\n" +
                "</gmd:equivalentScale>\n" +
                "</gmd:MD_Resolution>\n" +
                "</gmd:spatialResolution>\n" +
                "<gmd:language>\n" +
                "<gmd:LanguageCode codeList=\"http://www.loc.gov/standards/iso639-2/\" codeListValue=\"eng\" />\n" +
                "</gmd:language>\n" +
                "<gmd:characterSet>\n" +
                "<gmd:MD_CharacterSetCode codeListValue=\"utf8\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_CharacterSetCode\" />\n" +
                "</gmd:characterSet>\n" +
                "<gmd:topicCategory>\n" +
                "<gmd:MD_TopicCategoryCode>boundaries</gmd:MD_TopicCategoryCode>\n" +
                "</gmd:topicCategory>\n" +
                "<gmd:extent>\n" +
                "<gmd:EX_Extent>\n" +
                "<gmd:temporalElement>\n" +
                "<gmd:EX_TemporalExtent>\n" +
                "<gmd:extent>\n" +
                "<gml:TimePeriod gml:id=\"d735e433a1052958\">\n" +
                "<gml:beginPosition />\n" +
                "<gml:endPosition />\n" +
                "</gml:TimePeriod>\n" +
                "</gmd:extent>\n" +
                "</gmd:EX_TemporalExtent>\n" +
                "</gmd:temporalElement>\n" +
                "</gmd:EX_Extent>\n" +
                "</gmd:extent>\n" +
                "<gmd:extent>\n" +
                "<gmd:EX_Extent>\n" +
                "<gmd:geographicElement>\n" +
                "<gmd:EX_GeographicBoundingBox>\n" +
                "<gmd:westBoundLongitude>\n" +
                "<gco:Decimal>-180</gco:Decimal>\n" +
                "</gmd:westBoundLongitude>\n" +
                "<gmd:eastBoundLongitude>\n" +
                "<gco:Decimal>180</gco:Decimal>\n" +
                "</gmd:eastBoundLongitude>\n" +
                "<gmd:southBoundLatitude>\n" +
                "<gco:Decimal>-90</gco:Decimal>\n" +
                "</gmd:southBoundLatitude>\n" +
                "<gmd:northBoundLatitude>\n" +
                "<gco:Decimal>90</gco:Decimal>\n" +
                "</gmd:northBoundLatitude>\n" +
                "</gmd:EX_GeographicBoundingBox>\n" +
                "</gmd:geographicElement>\n" +
                "</gmd:EX_Extent>\n" +
                "</gmd:extent>\n" +
                "<gmd:supplementalInformation>\n" +
                "<gco:CharacterString>You can customize the template to suit your needs. You can add and remove " +
                "fields and fill out default information (e.g. contact details). Fie\n" +
                "lds you can not change in the default view may be accessible in the more comprehensive (and more " +
                "complex) advanced view. You can even use the XML editor to create custom\n" +
                "structures, but they have to be validated by the system, " +
                "so know what you do :-)</gco:CharacterString>\n" +
                "</gmd:supplementalInformation>\n" +
                "</gmd:MD_DataIdentification>\n" +
                "</gmd:identificationInfo>\n" +
                "<gmd:distributionInfo>\n" +
                "<gmd:MD_Distribution>\n" +
                "<gmd:transferOptions>\n" +
                "<gmd:MD_DigitalTransferOptions>\n" +
                "<gmd:onLine>\n" +
                "<gmd:CI_OnlineResource>\n" +
                "<gmd:linkage>\n" +
                "<gmd:URL />\n" +
                "</gmd:linkage>\n" +
                "<gmd:protocol>\n" +
                "<gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>\n" +
                "</gmd:protocol>\n" +
                "<gmd:name gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:name>\n" +
                "<gmd:description gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:description>\n" +
                "</gmd:CI_OnlineResource>\n" +
                "</gmd:onLine>\n" +
                "<gmd:onLine>\n" +
                "<gmd:CI_OnlineResource>\n" +
                "<gmd:linkage>\n" +
                "<gmd:URL>http://localhost:8080/geonetwork/srv/en/resources" +
                ".get?id=h95a8c4c_9038_43aa_a005_c943bc731c8d&amp;fname=&amp;access=private</gmd:URL>\n" +
                "</gmd:linkage>\n" +
                "<gmd:protocol>\n" +
                "<gco:CharacterString>WWW:DOWNLOAD-1.0-http--download</gco:CharacterString>\n" +
                "</gmd:protocol>\n" +
                "<gmd:name>\n" +
                "<gmx:MimeFileType xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" type=\"\" />\n" +
                "</gmd:name>\n" +
                "<gmd:description>\n" +
                "<gco:CharacterString />\n" +
                "</gmd:description>\n" +
                "</gmd:CI_OnlineResource>\n" +
                "</gmd:onLine>\n" +
                "<gmd:onLine>\n" +
                "<gmd:CI_OnlineResource>\n" +
                "<gmd:linkage>\n" +
                "<gmd:URL />\n" +
                "</gmd:linkage>\n" +
                "<gmd:protocol>\n" +
                "<gco:CharacterString>OGC:WMS-1.1.1-http-get-map</gco:CharacterString>\n" +
                "</gmd:protocol>\n" +
                "<gmd:name gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:name>\n" +
                "<gmd:description gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:description>\n" +
                "</gmd:CI_OnlineResource>\n" +
                "</gmd:onLine>\n" +
                "</gmd:MD_DigitalTransferOptions>\n" +
                "</gmd:transferOptions>\n" +
                "</gmd:MD_Distribution>\n" +
                "</gmd:distributionInfo>\n" +
                "<gmd:dataQualityInfo>\n" +
                "<gmd:DQ_DataQuality>\n" +
                "<gmd:scope>\n" +
                "<gmd:DQ_Scope>\n" +
                "<gmd:level>\n" +
                "<gmd:MD_ScopeCode codeListValue=\"dataset\" codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_ScopeCode\" />\n" +
                "</gmd:level>\n" +
                "</gmd:DQ_Scope>\n" +
                "</gmd:scope>\n" +
                "<gmd:lineage>\n" +
                "<gmd:LI_Lineage>\n" +
                "<gmd:statement gco:nilReason=\"missing\">\n" +
                "<gco:CharacterString />\n" +
                "</gmd:statement>\n" +
                "</gmd:LI_Lineage>\n" +
                "</gmd:lineage>\n" +
                "</gmd:DQ_DataQuality>\n" +
                "</gmd:dataQualityInfo>\n" +
                "<geonet:info>\n" +
                "<id>h95a8c4c_9038_43aa_a005_c943bc731c8d</id>\n" +
                "<schema>iso19139</schema>\n" +
                "<createDate>2012-06-20T14:25:30</createDate>\n" +
                "<changeDate>2012-06-20T14:26:49</changeDate>\n" +
                "<isTemplate>n</isTemplate>\n" +
                "<title />\n" +
                "<source>ac116fbb-8da1-4beb-9e22-826b8b891c6d</source>\n" +
                "<uuid>cc1b80d8-a1fb-48bb-9d0b-575b9ffea586</uuid>\n" +
                "<isHarvested>n</isHarvested>\n" +
                "<isLocked>y</isLocked>\n" +
                "<popularity>1</popularity>\n" +
                "<rating>0</rating>\n" +
                "<displayOrder />\n" +
                "<view>true</view>\n" +
                "<notify>true</notify>\n" +
                "<download>true</download>\n" +
                "<dynamic>true</dynamic>\n" +
                "<featured>true</featured>\n" +
                "<status>2</status>\n" +
                "<statusName>approved</statusName>\n" +
                "<edit>true</edit>\n" +
                "<owner>true</owner>\n" +
                "<isPublishedToAll>false</isPublishedToAll>\n" +
                "<ownerId>1</ownerId>\n" +
                "<ownername>admin</ownername>\n" +
                "<valid_details>\n" +
                "<type>schematron-rules-geonetwork</type>\n" +
                "<status>1</status>\n" +
                "<ratio>0/1</ratio>\n" +
                "</valid_details>\n" +
                "<valid_details>\n" +
                "<type>schematron-rules-iso</type>\n" +
                "<status>0</status>\n" +
                "<ratio>6/59</ratio>\n" +
                "</valid_details>\n" +
                "<valid_details>\n" +
                "<type>xsd</type>\n" +
                "<status>0</status>\n" +
                "<ratio />\n" +
                "</valid_details>\n" +
                "<valid>0</valid>\n" +
                "<baseUrl>http://localhost:8080/geonetwork</baseUrl>\n" +
                "<locService>/srv/en</locService>\n" +
                "</geonet:info>\n" +
                "</gmd:MD_Metadata>";
        Element xmlE = Xml.loadString(xml$, false);
        Document xmlD = new Document(xmlE);
        Object selected = DifferenceAnnotator.selectNodeToAnnotate(xpath, xmlD);
        assertNotNull(selected);
    }

    public void testJavaXPath() throws Exception {
        XPathFactory factory = XPathFactory.newInstance();
        javax.xml.xpath.XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile("/node()[1]/node()[2]/node()[1]");
        String xml$ = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<root>" +
                "<a>aaa</a>" +
                "<b>bbb</b>" +
                "<c>ccc</c>" +
                "</root>";

        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        dfactory.setNamespaceAware(true);
        DocumentBuilder builder = dfactory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(new InputSource(new StringReader(xml$)));

        //Element xmlE = Xml.loadString(xml$, false);
        //Document xmlD = new Document(xmlE);

        Object result = expr.evaluate(doc, XPathConstants.NODESET);

        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            Log.debug(Geonet.DIFF, nodes.item(i).getNodeValue());
        }
        assertEquals(1, nodes.getLength());
    }

    public void testPairClassnames() throws Exception {

        String doc = "<root><a><b><d/></b></a></root>";
        Element xmle = Xml.loadString(doc, false);

        String xml1 = "<root><a>xxx<b>yyy</b><c /></a></root>";
        String xml2 = "<root><xxx/><a>xxx<b>yyy</b><c />zzz</a></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);

        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);

        Log.debug(Geonet.DIFF, "\n\n%%%%%%%%%%%\n"+Xml.getString(annotatedSource));
        Log.debug(Geonet.DIFF, "\n\n%%%%%%%%%%%\n"+Xml.getString(annotatedTarget));

        Log.debug(Geonet.DIFF, "\n\n%%%%%%%%%%%\n"+Xml.getString(DifferenceAnnotator.addPairIdentifiers(xml1e, 0, 0, "")));
        Log.debug(Geonet.DIFF, "\n\n%%%%%%%%%%%\n"+Xml.getString(DifferenceAnnotator.addPairIdentifiers(xml2e, 0, 0, "")));
    }




    //
    // attributes
    //

    public void testInsertAttribute() throws Exception {
        String xml1 = "<root><a /></root>";
        String xml2 = "<root><a attr=\"zzz\"/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for inserting an attribute", xml1, outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for inserting an attribute", "<root><a attr=\"zzz\" xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:insertedAttribute=\"attr\" geonet:oldPos=\"0:0:0\" class=\"0-0-0\" /></root>" , outputter.outputString(annotatedTarget));
    }

    public void testDeleteAttribute() throws Exception {
        String xml1 = "<root><a attr=\"zzz\"/></root>";
        String xml2 = "<root><a /></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for deleting an attribute", "<root><a attr=\"zzz\" xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:deletedAttribute=\"attr\" geonet:oldPos=\"0:0:0\" class=\"0-0-0\" /></root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for deleting an attribute", xml2 , outputter.outputString(annotatedTarget));
    }

    public void testUpdateAttribute() throws Exception {
        String xml1 = "<root><a attr=\"zzz\"/></root>";
        String xml2 = "<root><a attr=\"xxx\"/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for deleting an attribute", "<root><a attr=\"zzz\" xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:updatedAttribute=\"attr\" geonet:oldPos=\"0:0:0\" class=\"0-0-0\" /></root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for deleting an attribute", "<root><a attr=\"xxx\" xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:updatedAttribute=\"attr\" geonet:oldPos=\"0:0:0\" class=\"0-0-0\" /></root>" , outputter.outputString(annotatedTarget));
    }

    //
    // namespace changes
    //

    public void testAddNamespace() throws Exception{
        String xml1 = "<root><a/></root>";
        String xml2 = "<root xmlns:zzz=\"http://zzzzzzz\"><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for adding a namespace", "<root><a /></root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for adding a namespace", "<root xmlns:zzz=\"http://zzzzzzz\" xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:insertedAttribute=\"xmlns:zzz\" geonet:oldPos=\"0:0\" class=\"0-0\"><a /></root>" , outputter.outputString(annotatedTarget));
    }

    public void testRemoveNamespace() throws Exception {
        String xml1 = "<root xmlns:zzz=\"http://zzzzzzz\"><a/></root>";
        String xml2 = "<root><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for deleting a namespace", "<root xmlns:zzz=\"http://zzzzzzz\" xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:deletedAttribute=\"xmlns:zzz\" geonet:oldPos=\"0:0\" class=\"0-0\"><a /></root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for deleting a namespace", "<root><a /></root>" , outputter.outputString(annotatedTarget));
    }


    public void testAddDefaultNamespace() throws Exception{
        String xml1 = "<root><a/></root>";
        String xml2 = "<root xmlns=\"http://zzzzzzz\"><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for adding default namespace", "<root xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:deleted=\"true\" geonet:oldPos=\"0:0\" class=\"0-0\"><a /></root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for adding default namespace", "<root xmlns=\"http://zzzzzzz\" xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:inserted=\"true\" class=\"0-0\"><a /></root>" , outputter.outputString(annotatedTarget));
    }

    public void testRemoveDefaultNamespace() throws Exception{
        String xml1 = "<root xmlns=\"http://zzzzzzz\"><a/></root>";
        String xml2 = "<root><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for deleting default namespace", "<root xmlns=\"http://zzzzzzz\" xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:deleted=\"true\" geonet:oldPos=\"0:0\" class=\"0-0\"><a /></root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for deleting default namespace", "<root xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:inserted=\"true\" class=\"0-0\"><a /></root>" , outputter.outputString(annotatedTarget));
    }

    public void testAddNamespaceInChildElement() throws Exception{
        String xml1 = "<root><a/></root>";
        String xml2 = "<root><a xmlns:zzz=\"http://zzzzzzz\"/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for adding a namespace", "<root><a /></root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for adding a namespace", "<root><a xmlns:zzz=\"http://zzzzzzz\" xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:insertedAttribute=\"xmlns:zzz\" geonet:oldPos=\"0:0:0\" class=\"0-0-0\" /></root>" , outputter.outputString(annotatedTarget));
    }

    public void testRemoveNamespaceInChildElement() throws Exception{
        String xml1 = "<root><a xmlns:zzz=\"http://zzzzzzz\"/></root>";
        String xml2 = "<root><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for removing a namespace", "<root><a xmlns:zzz=\"http://zzzzzzz\" xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:deletedAttribute=\"xmlns:zzz\" geonet:oldPos=\"0:0:0\" class=\"0-0-0\" /></root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for removing a namespace", "<root><a /></root>" , outputter.outputString(annotatedTarget));
    }

    public void testUnNamespacePrefixes() throws Exception {
        String xml1 = "<aaa:root xmlns:aaa=\"http://zzzzzzz\"><aaa:a/></aaa:root>";
        String xml2 = "<root xmlns=\"http://zzzzzzz\"><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        assertEquals("Unexpected annotation for un-prefixing default namespace", "<aaa:root xmlns:aaa=\"http://zzzzzzz\"><aaa:a /></aaa:root>", outputter.outputString(annotatedSource));

        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        assertEquals("Unexpected annotation for un-prefixing default namespace", "<root xmlns=\"http://zzzzzzz\"><a /></root>" , outputter.outputString(annotatedTarget));
    }



    public void testxRealWorldMetadataDiff() throws Exception {

        String xmlBeforeChange = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis" +
                ".net/gml\"\n" +
                "                 xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                "                 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "                 xmlns:gco=\"http://www.isotc211.org/2005/gco\"\n" +
                "                 xmlns:geonet=\"http://www.fao.org/geonetwork\"\n" +
                "                 xsi:schemaLocation=\"http://www.isotc211.org/2005/gmd http://www.isotc211" +
                ".org/2005/gmd/gmd.xsd\">\n" +
                "  <gmd:fileIdentifier>\n" +
                "      <gco:CharacterString xmlns:srv=\"http://www.isotc211" +
                ".org/2005/srv\">{8da1c239-93af-4159-948d-741134f705a4}</gco:CharacterString>\n" +
                "  </gmd:fileIdentifier>\n" +
                "  <gmd:language>\n" +
                "      <gco:CharacterString>dut</gco:CharacterString>\n" +
                "  </gmd:language>\n" +
                "  <gmd:characterSet>\n" +
                "      <gmd:MD_CharacterSetCode codeList=\"http://www.isotc211.org/2005/resources/codeList" +
                ".xml#MD_CharacterSetCode\"\n" +
                "                               codeListValue=\"xxx\"/>\n" +
                "  </gmd:characterSet>\n" +
                "  <gmd:hierarchyLevel>\n" +
                "      <gmd:MD_ScopeCode codeList=\"http://www.isotc211.org/2005/resources/codeList" +
                ".xml#MD_ScopeCode\"\n" +
                "                        codeListValue=\"dataset\"/>\n" +
                "  </gmd:hierarchyLevel>\n" +
                "  <gmd:contact>\n" +
                "      <gmd:CI_ResponsibleParty>\n" +
                "         <gmd:individualName>\n" +
                "            <gco:CharacterString>GeoService (GRID)</gco:CharacterString>\n" +
                "         </gmd:individualName>\n" +
                "         <gmd:organisationName>\n" +
                "            <gco:CharacterString>Provincie Gelderland</gco:CharacterString>\n" +
                "         </gmd:organisationName>\n" +
                "         <gmd:contactInfo>\n" +
                "            <gmd:CI_Contact>\n" +
                "               <gmd:phone>\n" +
                "                  <gmd:CI_Telephone>\n" +
                "                     <gmd:voice>\n" +
                "                        <gco:CharacterString>(026) 359 8888</gco:CharacterString>\n" +
                "                     </gmd:voice>\n" +
                "                     <gmd:facsimile>\n" +
                "                        <gco:CharacterString>(026) 359 9480</gco:CharacterString>\n" +
                "                     </gmd:facsimile>\n" +
                "                  </gmd:CI_Telephone>\n" +
                "               </gmd:phone>\n" +
                "               <gmd:address>\n" +
                "                  <gmd:CI_Address>\n" +
                "                     <gmd:deliveryPoint>\n" +
                "                        <gco:CharacterString>Postbus 9090</gco:CharacterString>\n" +
                "                     </gmd:deliveryPoint>\n" +
                "                     <gmd:city>\n" +
                "                        <gco:CharacterString>Arnhem</gco:CharacterString>\n" +
                "                     </gmd:city>\n" +
                "                     <gmd:administrativeArea>\n" +
                "                        <gco:CharacterString>Gelderland</gco:CharacterString>\n" +
                "                     </gmd:administrativeArea>\n" +
                "                     <gmd:postalCode>\n" +
                "                        <gco:CharacterString>6800 GX</gco:CharacterString>\n" +
                "                     </gmd:postalCode>\n" +
                "                     <gmd:country>\n" +
                "                        <gco:CharacterString>Nederland</gco:CharacterString>\n" +
                "                     </gmd:country>\n" +
                "                     <gmd:electronicMailAddress>\n" +
                "                        <gco:CharacterString>geoservice@prv.gelderland.nl</gco:CharacterString>\n" +
                "                     </gmd:electronicMailAddress>\n" +
                "                  </gmd:CI_Address>\n" +
                "               </gmd:address>\n" +
                "               <gmd:onlineResource>\n" +
                "                  <gmd:CI_OnlineResource>\n" +
                "                     <gmd:linkage>\n" +
                "                        <gmd:URL>http://www.gelderland.nl</gmd:URL>\n" +
                "                     </gmd:linkage>\n" +
                "                  </gmd:CI_OnlineResource>\n" +
                "               </gmd:onlineResource>\n" +
                "            </gmd:CI_Contact>\n" +
                "         </gmd:contactInfo>\n" +
                "         <gmd:role>\n" +
                "            <gmd:CI_RoleCode codeList=\"http://www.isotc211.org/2005/resources/codeList" +
                ".xml#CI_RoleCode\"\n" +
                "                             codeListValue=\"author\"/>\n" +
                "         </gmd:role>\n" +
                "      </gmd:CI_ResponsibleParty>\n" +
                "  </gmd:contact>\n" +
                "  <gmd:dateStamp>\n" +
                "      <gco:DateTime>1970-01-01T00:00:00</gco:DateTime>\n" +
                "  </gmd:dateStamp>\n" +
                "  <gmd:metadataStandardName>\n" +
                "      <gco:CharacterString>ISO 19115:2003</gco:CharacterString>\n" +
                "  </gmd:metadataStandardName>\n" +
                "  <gmd:metadataStandardVersion>\n" +
                "      <gco:CharacterString>Nederlandse metadata standaard voor geografie 1.1</gco:CharacterString>\n" +
                "  </gmd:metadataStandardVersion>\n" +
                "  <gmd:dataSetURI gco:nilReason=\"missing\">\n" +
                "      <gco:CharacterString/>\n" +
                "  </gmd:dataSetURI>\n" +
                "  <gmd:referenceSystemInfo>\n" +
                "      <gmd:MD_ReferenceSystem>\n" +
                "         <gmd:referenceSystemIdentifier>\n" +
                "            <gmd:RS_Identifier>\n" +
                "               <gmd:code>\n" +
                "                  <gco:CharacterString>Rijksdriehoekstelsel (0)</gco:CharacterString>\n" +
                "               </gmd:code>\n" +
                "               <gmd:codeSpace>\n" +
                "                  <gco:CharacterString>EPSG</gco:CharacterString>\n" +
                "               </gmd:codeSpace>\n" +
                "            </gmd:RS_Identifier>\n" +
                "         </gmd:referenceSystemIdentifier>\n" +
                "      </gmd:MD_ReferenceSystem>\n" +
                "  </gmd:referenceSystemInfo>\n" +
                "  <gmd:referenceSystemInfo>\n" +
                "      <gmd:MD_ReferenceSystem>\n" +
                "         <gmd:referenceSystemIdentifier>\n" +
                "            <gmd:RS_Identifier>\n" +
                "               <gmd:code>\n" +
                "                  <gco:CharacterString>Normaal Amsterdams Peil (5709)</gco:CharacterString>\n" +
                "               </gmd:code>\n" +
                "               <gmd:codeSpace>\n" +
                "                  <gco:CharacterString>EPSG</gco:CharacterString>\n" +
                "               </gmd:codeSpace>\n" +
                "            </gmd:RS_Identifier>\n" +
                "         </gmd:referenceSystemIdentifier>\n" +
                "      </gmd:MD_ReferenceSystem>\n" +
                "  </gmd:referenceSystemInfo>\n" +
                "  <gmd:identificationInfo>\n" +
                "      <gmd:MD_DataIdentification>\n" +
                "         <gmd:citation>\n" +
                "            <gmd:CI_Citation>\n" +
                "               <gmd:title>\n" +
                "                  <gco:CharacterString>Gemeenten Gelderland</gco:CharacterString>\n" +
                "               </gmd:title>\n" +
                "               <gmd:alternateTitle>\n" +
                "                  <gco:CharacterString>GEO.GzBe_gemeenten</gco:CharacterString>\n" +
                "               </gmd:alternateTitle>\n" +
                "               <gmd:date>\n" +
                "                  <gmd:CI_Date>\n" +
                "                     <gmd:date>\n" +
                "                        <gco:Date>1989-01-01</gco:Date>\n" +
                "                     </gmd:date>\n" +
                "                     <gmd:dateType>\n" +
                "                        <gmd:CI_DateTypeCode codeList=\"http://www.isotc211" +
                ".org/2005/resources/codeList.xml#CI_DateTypeCode\"\n" +
                "                                             codeListValue=\"creation\"/>\n" +
                "                     </gmd:dateType>\n" +
                "                  </gmd:CI_Date>\n" +
                "               </gmd:date>\n" +
                "               <gmd:date>\n" +
                "                  <gmd:CI_Date>\n" +
                "                     <gmd:date>\n" +
                "                        <gco:Date>2009-04-14</gco:Date>\n" +
                "                     </gmd:date>\n" +
                "                     <gmd:dateType>\n" +
                "                        <gmd:CI_DateTypeCode codeList=\"http://www.isotc211" +
                ".org/2005/resources/codeList.xml#CI_DateTypeCode\"\n" +
                "                                             codeListValue=\"revision\"/>\n" +
                "                     </gmd:dateType>\n" +
                "                  </gmd:CI_Date>\n" +
                "               </gmd:date>\n" +
                "               <gmd:edition>\n" +
                "                  <gco:CharacterString>13</gco:CharacterString>\n" +
                "               </gmd:edition>\n" +
                "               <gmd:series>\n" +
                "                  <gmd:CI_Series/>\n" +
                "               </gmd:series>\n" +
                "            </gmd:CI_Citation>\n" +
                "         </gmd:citation>\n" +
                "         <gmd:abstract>\n" +
                "            <gco:CharacterString>Gemeenten in Gelderland, met de gemeentelijke herindeling in Oost " +
                "Gelderland van 1-1-2005 en de naamwijziging van gemeente Groenlo in Gelre Oost (per 19-05-2006)" +
                "</gco:CharacterString>\n" +
                "         </gmd:abstract>\n" +
                "         <gmd:purpose>\n" +
                "            <gco:CharacterString>Algemene bestuurlijke organisatie: basis referentie " +
                "bestand</gco:CharacterString>\n" +
                "         </gmd:purpose>\n" +
                "         <gmd:status>\n" +
                "            <gmd:MD_ProgressCode codeList=\"http://www.isotc211.org/2005/resources/codeList" +
                ".xml#MD_ProgressCode\"\n" +
                "                                 codeListValue=\"onGoing\"/>\n" +
                "         </gmd:status>\n" +
                "         <gmd:pointOfContact>\n" +
                "            <gmd:CI_ResponsibleParty>\n" +
                "               <gmd:individualName>\n" +
                "                  <gco:CharacterString>Carel Stortelder   (809590)</gco:CharacterString>\n" +
                "               </gmd:individualName>\n" +
                "               <gmd:organisationName>\n" +
                "                  <gco:CharacterString>Provincie Gelderland  I&amp;A-GRID</gco:CharacterString>\n" +
                "               </gmd:organisationName>\n" +
                "               <gmd:positionName gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:positionName>\n" +
                "               <gmd:contactInfo>\n" +
                "                  <gmd:CI_Contact>\n" +
                "                     <gmd:phone>\n" +
                "                        <gmd:CI_Telephone>\n" +
                "                           <gmd:voice gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:voice>\n" +
                "                           <gmd:facsimile gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:facsimile>\n" +
                "                        </gmd:CI_Telephone>\n" +
                "                     </gmd:phone>\n" +
                "                     <gmd:address>\n" +
                "                        <gmd:CI_Address>\n" +
                "                           <gmd:deliveryPoint gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:deliveryPoint>\n" +
                "                           <gmd:city gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:city>\n" +
                "                           <gmd:administrativeArea gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:administrativeArea>\n" +
                "                           <gmd:postalCode gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:postalCode>\n" +
                "                           <gmd:country gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:country>\n" +
                "                           <gmd:electronicMailAddress gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:electronicMailAddress>\n" +
                "                        </gmd:CI_Address>\n" +
                "                     </gmd:address>\n" +
                "                     <gmd:onlineResource>\n" +
                "                        <gmd:CI_OnlineResource>\n" +
                "                           <gmd:linkage>\n" +
                "                              <gmd:URL>http://www.gelderland.nl</gmd:URL>\n" +
                "                           </gmd:linkage>\n" +
                "                        </gmd:CI_OnlineResource>\n" +
                "                     </gmd:onlineResource>\n" +
                "                  </gmd:CI_Contact>\n" +
                "               </gmd:contactInfo>\n" +
                "               <gmd:role>\n" +
                "                  <gmd:CI_RoleCode codeList=\"http://www.isotc211.org/2005/resources/codeList" +
                ".xml#CI_RoleCode\"\n" +
                "                                   codeListValue=\"pointOfContact\"/>\n" +
                "               </gmd:role>\n" +
                "            </gmd:CI_ResponsibleParty>\n" +
                "         </gmd:pointOfContact>\n" +
                "         <gmd:pointOfContact>\n" +
                "            <gmd:CI_ResponsibleParty>\n" +
                "               <gmd:individualName>\n" +
                "                  <gco:CharacterString>Jan Gerritsen   (259490)</gco:CharacterString>\n" +
                "               </gmd:individualName>\n" +
                "               <gmd:organisationName>\n" +
                "                  <gco:CharacterString>Provincie Gelderland  I&amp;A-GIC</gco:CharacterString>\n" +
                "               </gmd:organisationName>\n" +
                "               <gmd:positionName gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:positionName>\n" +
                "               <gmd:contactInfo>\n" +
                "                  <gmd:CI_Contact>\n" +
                "                     <gmd:phone>\n" +
                "                        <gmd:CI_Telephone>\n" +
                "                           <gmd:voice gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:voice>\n" +
                "                           <gmd:facsimile gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:facsimile>\n" +
                "                        </gmd:CI_Telephone>\n" +
                "                     </gmd:phone>\n" +
                "                     <gmd:address>\n" +
                "                        <gmd:CI_Address>\n" +
                "                           <gmd:deliveryPoint gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:deliveryPoint>\n" +
                "                           <gmd:city gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:city>\n" +
                "                           <gmd:administrativeArea gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:administrativeArea>\n" +
                "                           <gmd:postalCode gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:postalCode>\n" +
                "                           <gmd:country gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:country>\n" +
                "                           <gmd:electronicMailAddress gco:nilReason=\"missing\">\n" +
                "                              <gco:CharacterString/>\n" +
                "                           </gmd:electronicMailAddress>\n" +
                "                        </gmd:CI_Address>\n" +
                "                     </gmd:address>\n" +
                "                     <gmd:onlineResource>\n" +
                "                        <gmd:CI_OnlineResource>\n" +
                "                           <gmd:linkage>\n" +
                "                              <gmd:URL>http://www.gelderland.nl</gmd:URL>\n" +
                "                           </gmd:linkage>\n" +
                "                        </gmd:CI_OnlineResource>\n" +
                "                     </gmd:onlineResource>\n" +
                "                  </gmd:CI_Contact>\n" +
                "               </gmd:contactInfo>\n" +
                "               <gmd:role>\n" +
                "                  <gmd:CI_RoleCode codeList=\"http://www.isotc211.org/2005/resources/codeList" +
                ".xml#CI_RoleCode\"\n" +
                "                                   codeListValue=\"processor\"/>\n" +
                "               </gmd:role>\n" +
                "            </gmd:CI_ResponsibleParty>\n" +
                "         </gmd:pointOfContact>\n" +
                "         <gmd:resourceMaintenance>\n" +
                "            <gmd:MD_MaintenanceInformation>\n" +
                "               <gmd:maintenanceAndUpdateFrequency>\n" +
                "                  <gmd:MD_MaintenanceFrequencyCode codeList=\"http://www.isotc211" +
                ".org/2005/resources/codeList.xml#MD_MaintenanceFrequencyCode\"\n" +
                "                                                   codeListValue=\"asNeeded\"/>\n" +
                "               </gmd:maintenanceAndUpdateFrequency>\n" +
                "            </gmd:MD_MaintenanceInformation>\n" +
                "         </gmd:resourceMaintenance>\n" +
                "         <gmd:graphicOverview>\n" +
                "            <gmd:MD_BrowseGraphic>\n" +
                "               <gmd:fileName>\n" +
                "                  <gco:CharacterString>GzBe_Gemeenten_s.png</gco:CharacterString>\n" +
                "               </gmd:fileName>\n" +
                "               <gmd:fileDescription>\n" +
                "                  <gco:CharacterString>thumbnail</gco:CharacterString>\n" +
                "               </gmd:fileDescription>\n" +
                "               <gmd:fileType>\n" +
                "                  <gco:CharacterString>png</gco:CharacterString>\n" +
                "               </gmd:fileType>\n" +
                "            </gmd:MD_BrowseGraphic>\n" +
                "         </gmd:graphicOverview>\n" +
                "         <gmd:graphicOverview>\n" +
                "            <gmd:MD_BrowseGraphic>\n" +
                "               <gmd:fileName>\n" +
                "                  <gco:CharacterString>GzBe_Gemeenten.png</gco:CharacterString>\n" +
                "               </gmd:fileName>\n" +
                "               <gmd:fileDescription>\n" +
                "                  <gco:CharacterString>large_thumbnail</gco:CharacterString>\n" +
                "               </gmd:fileDescription>\n" +
                "               <gmd:fileType>\n" +
                "                  <gco:CharacterString>png</gco:CharacterString>\n" +
                "               </gmd:fileType>\n" +
                "            </gmd:MD_BrowseGraphic>\n" +
                "         </gmd:graphicOverview>\n" +
                "         <gmd:descriptiveKeywords>\n" +
                "            <gmd:MD_Keywords>\n" +
                "               <gmd:keyword>\n" +
                "                  <gco:CharacterString>Bestuurlijke grenzen</gco:CharacterString>\n" +
                "               </gmd:keyword>\n" +
                "               <gmd:thesaurusName>\n" +
                "                  <gmd:CI_Citation>\n" +
                "                     <gmd:title>\n" +
                "                        <gco:CharacterString>Voorbeeldenlijst GeoNovum</gco:CharacterString>\n" +
                "                     </gmd:title>\n" +
                "                     <gmd:date>\n" +
                "                        <gmd:CI_Date>\n" +
                "                           <gmd:date>\n" +
                "                              <gco:Date>2007-02-22</gco:Date>\n" +
                "                           </gmd:date>\n" +
                "                           <gmd:dateType>\n" +
                "                              <gmd:CI_DateTypeCode codeList=\"http://www.isotc211" +
                ".org/2005/resources/codeList.xml#CI_DateTypeCode\"\n" +
                "                                                   codeListValue=\"publication\"/>\n" +
                "                           </gmd:dateType>\n" +
                "                        </gmd:CI_Date>\n" +
                "                     </gmd:date>\n" +
                "                  </gmd:CI_Citation>\n" +
                "               </gmd:thesaurusName>\n" +
                "            </gmd:MD_Keywords>\n" +
                "         </gmd:descriptiveKeywords>\n" +
                "         <gmd:resourceSpecificUsage>\n" +
                "            <gmd:MD_Usage>\n" +
                "               <gmd:specificUsage gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:specificUsage>\n" +
                "               <gmd:userContactInfo/>\n" +
                "            </gmd:MD_Usage>\n" +
                "         </gmd:resourceSpecificUsage>\n" +
                "         <gmd:resourceConstraints>\n" +
                "            <gmd:MD_Constraints>\n" +
                "               <gmd:useLimitation>\n" +
                "                  <gco:CharacterString>Geen gebruiksbeperkingen</gco:CharacterString>\n" +
                "               </gmd:useLimitation>\n" +
                "            </gmd:MD_Constraints>\n" +
                "         </gmd:resourceConstraints>\n" +
                "         <gmd:resourceConstraints>\n" +
                "            <gmd:MD_LegalConstraints>\n" +
                "               <gmd:accessConstraints>\n" +
                "                  <gmd:MD_RestrictionCode codeList=\"http://www.isotc211.org/2005/resources/codeList" +
                ".xml#MD_RestrictionCode\"\n" +
                "                                          codeListValue=\"otherRestrictions\"/>\n" +
                "               </gmd:accessConstraints>\n" +
                "            </gmd:MD_LegalConstraints>\n" +
                "         </gmd:resourceConstraints>\n" +
                "         <gmd:resourceConstraints>\n" +
                "            <gmd:MD_SecurityConstraints>\n" +
                "               <gmd:classification>\n" +
                "                  <gmd:MD_ClassificationCode codeList=\"http://www.isotc211" +
                ".org/2005/resources/codeList.xml#MD_ClassificationCode\"\n" +
                "                                             codeListValue=\"unclassified\"/>\n" +
                "               </gmd:classification>\n" +
                "            </gmd:MD_SecurityConstraints>\n" +
                "         </gmd:resourceConstraints>\n" +
                "         <gmd:spatialRepresentationType>\n" +
                "            <gmd:MD_SpatialRepresentationTypeCode codeList=\"http://www.isotc211" +
                ".org/2005/resources/codeList.xml#MD_SpatialRepresentationTypeCode\"\n" +
                "                                                  codeListValue=\"vector\"/>\n" +
                "         </gmd:spatialRepresentationType>\n" +
                "         <gmd:spatialResolution>\n" +
                "            <gmd:MD_Resolution>\n" +
                "               <gmd:equivalentScale>\n" +
                "                  <gmd:MD_RepresentativeFraction>\n" +
                "                     <gmd:denominator>\n" +
                "                        <gco:Integer>10000</gco:Integer>\n" +
                "                     </gmd:denominator>\n" +
                "                  </gmd:MD_RepresentativeFraction>\n" +
                "               </gmd:equivalentScale>\n" +
                "            </gmd:MD_Resolution>\n" +
                "         </gmd:spatialResolution>\n" +
                "         <gmd:language>\n" +
                "            <gco:CharacterString>dut</gco:CharacterString>\n" +
                "         </gmd:language>\n" +
                "         <gmd:characterSet>\n" +
                "            <gmd:MD_CharacterSetCode codeList=\"http://www.isotc211.org/2005/resources/codeList" +
                ".xml#MD_CharacterSetCode\"\n" +
                "                                     codeListValue=\"utf8\"/>\n" +
                "         </gmd:characterSet>\n" +
                "         <gmd:topicCategory>\n" +
                "            <gmd:MD_TopicCategoryCode>boundaries</gmd:MD_TopicCategoryCode>\n" +
                "         </gmd:topicCategory>\n" +
                "         <gmd:extent>\n" +
                "            <gmd:EX_Extent>\n" +
                "               <gmd:geographicElement>\n" +
                "                  <gmd:EX_GeographicBoundingBox>\n" +
                "                     <gmd:westBoundLongitude>\n" +
                "                        <gco:Decimal>4.988</gco:Decimal>\n" +
                "                     </gmd:westBoundLongitude>\n" +
                "                     <gmd:eastBoundLongitude>\n" +
                "                        <gco:Decimal>6.851</gco:Decimal>\n" +
                "                     </gmd:eastBoundLongitude>\n" +
                "                     <gmd:southBoundLatitude>\n" +
                "                        <gco:Decimal>51.726</gco:Decimal>\n" +
                "                     </gmd:southBoundLatitude>\n" +
                "                     <gmd:northBoundLatitude>\n" +
                "                        <gco:Decimal>52.524</gco:Decimal>\n" +
                "                     </gmd:northBoundLatitude>\n" +
                "                  </gmd:EX_GeographicBoundingBox>\n" +
                "               </gmd:geographicElement>\n" +
                "               <gmd:geographicElement>\n" +
                "                  <gmd:EX_GeographicDescription>\n" +
                "                     <gmd:geographicIdentifier>\n" +
                "                        <gmd:MD_Identifier>\n" +
                "                           <gmd:code>\n" +
                "                              <gco:CharacterString>Gelderland</gco:CharacterString>\n" +
                "                           </gmd:code>\n" +
                "                        </gmd:MD_Identifier>\n" +
                "                     </gmd:geographicIdentifier>\n" +
                "                  </gmd:EX_GeographicDescription>\n" +
                "               </gmd:geographicElement>\n" +
                "               <gmd:temporalElement>\n" +
                "                  <gmd:EX_TemporalExtent>\n" +
                "                     <gmd:extent>\n" +
                "                        <gml:TimePeriod gml:id=\"tp1\">\n" +
                "                           <gml:begin>\n" +
                "                              <gml:TimeInstant gml:id=\"ti1\">\n" +
                "                                 <gml:timePosition>1995-01-01</gml:timePosition>\n" +
                "                              </gml:TimeInstant>\n" +
                "                           </gml:begin>\n" +
                "                           <gml:end>\n" +
                "                              <gml:TimeInstant gml:id=\"ti2\">\n" +
                "                                 <gml:timePosition>2009-01-31</gml:timePosition>\n" +
                "                              </gml:TimeInstant>\n" +
                "                           </gml:end>\n" +
                "                        </gml:TimePeriod>\n" +
                "                     </gmd:extent>\n" +
                "                  </gmd:EX_TemporalExtent>\n" +
                "               </gmd:temporalElement>\n" +
                "               <gmd:verticalElement>\n" +
                "                  <gmd:EX_VerticalExtent>\n" +
                "                     <gmd:minimumValue>\n" +
                "                        <gco:Real>0</gco:Real>\n" +
                "                     </gmd:minimumValue>\n" +
                "                     <gmd:maximumValue>\n" +
                "                        <gco:Real>0</gco:Real>\n" +
                "                     </gmd:maximumValue>\n" +
                "                     <gmd:verticalCRS/>\n" +
                "                  </gmd:EX_VerticalExtent>\n" +
                "               </gmd:verticalElement>\n" +
                "            </gmd:EX_Extent>\n" +
                "         </gmd:extent>\n" +
                "         <gmd:supplementalInformation>\n" +
                "            <gco:CharacterString>Document_referentie|Kadastrale ondergronden" +
                ".</gco:CharacterString>\n" +
                "         </gmd:supplementalInformation>\n" +
                "      </gmd:MD_DataIdentification>\n" +
                "  </gmd:identificationInfo>\n" +
                "  <gmd:distributionInfo>\n" +
                "      <gmd:MD_Distribution>\n" +
                "         <gmd:distributionFormat>\n" +
                "            <gmd:MD_Format>\n" +
                "               <gmd:name gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:name>\n" +
                "               <gmd:version gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:version>\n" +
                "            </gmd:MD_Format>\n" +
                "         </gmd:distributionFormat>\n" +
                "         <gmd:distributor>\n" +
                "            <gmd:MD_Distributor>\n" +
                "               <gmd:distributorContact>\n" +
                "                  <gmd:CI_ResponsibleParty>\n" +
                "                     <gmd:individualName>\n" +
                "                        <gco:CharacterString>GeoService</gco:CharacterString>\n" +
                "                     </gmd:individualName>\n" +
                "                     <gmd:organisationName>\n" +
                "                        <gco:CharacterString>Provincie Gelderland</gco:CharacterString>\n" +
                "                     </gmd:organisationName>\n" +
                "                     <gmd:positionName gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:positionName>\n" +
                "                     <gmd:contactInfo>\n" +
                "                        <gmd:CI_Contact>\n" +
                "                           <gmd:phone>\n" +
                "                              <gmd:CI_Telephone>\n" +
                "                                 <gmd:voice>\n" +
                "                                    <gco:CharacterString>(026) 359 8888</gco:CharacterString>\n" +
                "                                 </gmd:voice>\n" +
                "                                 <gmd:facsimile>\n" +
                "                                    <gco:CharacterString>(026) 359 9480</gco:CharacterString>\n" +
                "                                 </gmd:facsimile>\n" +
                "                              </gmd:CI_Telephone>\n" +
                "                           </gmd:phone>\n" +
                "                           <gmd:address>\n" +
                "                              <gmd:CI_Address>\n" +
                "                                 <gmd:deliveryPoint>\n" +
                "                                    <gco:CharacterString>Postbus 9090</gco:CharacterString>\n" +
                "                                 </gmd:deliveryPoint>\n" +
                "                                 <gmd:city>\n" +
                "                                    <gco:CharacterString>Arnhem</gco:CharacterString>\n" +
                "                                 </gmd:city>\n" +
                "                                 <gmd:administrativeArea>\n" +
                "                                    <gco:CharacterString>Gelderland</gco:CharacterString>\n" +
                "                                 </gmd:administrativeArea>\n" +
                "                                 <gmd:postalCode>\n" +
                "                                    <gco:CharacterString>6800 GX</gco:CharacterString>\n" +
                "                                 </gmd:postalCode>\n" +
                "                                 <gmd:country>\n" +
                "                                    <gco:CharacterString>Nederland</gco:CharacterString>\n" +
                "                                 </gmd:country>\n" +
                "                                 <gmd:electronicMailAddress>\n" +
                "                                    <gco:CharacterString>geoservice@prv.gelderland" +
                ".nl</gco:CharacterString>\n" +
                "                                 </gmd:electronicMailAddress>\n" +
                "                              </gmd:CI_Address>\n" +
                "                           </gmd:address>\n" +
                "                           <gmd:onlineResource>\n" +
                "                              <gmd:CI_OnlineResource>\n" +
                "                                 <gmd:linkage>\n" +
                "                                    <gmd:URL>http://www.gelderland.nl</gmd:URL>\n" +
                "                                 </gmd:linkage>\n" +
                "                              </gmd:CI_OnlineResource>\n" +
                "                           </gmd:onlineResource>\n" +
                "                        </gmd:CI_Contact>\n" +
                "                     </gmd:contactInfo>\n" +
                "                     <gmd:role>\n" +
                "                        <gmd:CI_RoleCode codeList=\"http://www.isotc211.org/2005/resources/codeList" +
                ".xml#CI_RoleCode\"\n" +
                "                                         codeListValue=\"distributor\"/>\n" +
                "                     </gmd:role>\n" +
                "                  </gmd:CI_ResponsibleParty>\n" +
                "               </gmd:distributorContact>\n" +
                "               <gmd:distributionOrderProcess>\n" +
                "                  <gmd:MD_StandardOrderProcess>\n" +
                "                     <gmd:fees gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:fees>\n" +
                "                     <gmd:orderingInstructions gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:orderingInstructions>\n" +
                "                     <gmd:turnaround gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:turnaround>\n" +
                "                  </gmd:MD_StandardOrderProcess>\n" +
                "               </gmd:distributionOrderProcess>\n" +
                "            </gmd:MD_Distributor>\n" +
                "         </gmd:distributor>\n" +
                "         <gmd:transferOptions>\n" +
                "            <gmd:MD_DigitalTransferOptions>\n" +
                "               <gmd:unitsOfDistribution>\n" +
                "                  <gco:CharacterString>shape</gco:CharacterString>\n" +
                "               </gmd:unitsOfDistribution>\n" +
                "               <gmd:onLine>\n" +
                "                  <gmd:CI_OnlineResource>\n" +
                "                     <gmd:linkage>\n" +
                "                        <gmd:URL>http://geodev.prvgld.nl/wmsconnector/com.esri.wms" +
                ".Esrimap?ServiceName=wms_mobiliteit</gmd:URL>\n" +
                "                     </gmd:linkage>\n" +
                "                     <gmd:protocol>\n" +
                "                        <gco:CharacterString>OGC:WMS</gco:CharacterString>\n" +
                "                     </gmd:protocol>\n" +
                "                     <gmd:name>\n" +
                "                        <gco:CharacterString>Gemeenten</gco:CharacterString>\n" +
                "                     </gmd:name>\n" +
                "                  </gmd:CI_OnlineResource>\n" +
                "               </gmd:onLine>\n" +
                "               <gmd:offLine>\n" +
                "                  <gmd:MD_Medium/>\n" +
                "               </gmd:offLine>\n" +
                "            </gmd:MD_DigitalTransferOptions>\n" +
                "         </gmd:transferOptions>\n" +
                "      </gmd:MD_Distribution>\n" +
                "  </gmd:distributionInfo>\n" +
                "  <gmd:dataQualityInfo>\n" +
                "      <gmd:DQ_DataQuality>\n" +
                "         <gmd:scope>\n" +
                "            <gmd:DQ_Scope>\n" +
                "               <gmd:level>\n" +
                "                  <gmd:MD_ScopeCode codeList=\"http://www.isotc211.org/2005/resources/codeList" +
                ".xml#MD_ScopeCode\"\n" +
                "                                    codeListValue=\"attributeType\"/>\n" +
                "               </gmd:level>\n" +
                "            </gmd:DQ_Scope>\n" +
                "         </gmd:scope>\n" +
                "         <gmd:report>\n" +
                "            <gmd:DQ_CompletenessOmission>\n" +
                "               <gmd:result>\n" +
                "                  <gmd:DQ_QuantitativeResult>\n" +
                "                     <gmd:valueUnit gco:nilReason=\"inapplicable\"/>\n" +
                "                     <gmd:value>\n";

        String changeFromXml1  = "                        <gco:Record>100 procent.</gco:Record>\n";
        String changeToXml2 =  "                        <gco:Record>100 %.</gco:Record>\n";

        String xmlAfterChange =
                "                     </gmd:value>\n" +
                        "                  </gmd:DQ_QuantitativeResult>\n" +
                        "               </gmd:result>\n" +
                        "            </gmd:DQ_CompletenessOmission>\n" +
                        "         </gmd:report>\n" +
                        "         <gmd:lineage>\n" +
                        "            <gmd:LI_Lineage>\n" +
                        "               <gmd:statement>\n" +
                        "                  <gco:CharacterString>Mutaties afgeleid van kadastrale percelen, overige grenzen uit vorige digitale versie.</gco:CharacterString>\n" +
                        "               </gmd:statement>\n" +
                        "            </gmd:LI_Lineage>\n" +
                        "         </gmd:lineage>\n" +
                        "      </gmd:DQ_DataQuality>\n" +
                        "  </gmd:dataQualityInfo>\n" +
                        "</gmd:MD_Metadata>";

        String xml1 = xmlBeforeChange.concat(changeFromXml1).concat(xmlAfterChange);
        String xml2 = xmlBeforeChange.concat(changeToXml2).concat(xmlAfterChange);

        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);

        Element diff = null;
        for(int x = 0; x < 10; x++) {
            long start = System.currentTimeMillis();
            diff = MetadataDifference.diff(xml1e, xml2e);
            long end = System.currentTimeMillis();

            Log.debug(Geonet.DIFF, "Diff took " + (end-start) + " ms");
        }

        Element annotatedSource = DifferenceAnnotator.addDelta(xml1e, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
        //assertEquals("Unexpected", true, outputter.outputString(annotatedSource).contains("deletedText"));


        Element annotatedTarget = DifferenceAnnotator.addDelta(xml2e, diff, DifferenceAnnotator.DifferenceDirection.TARGET);
        //assertEquals("Unexpected", true, outputter.outputString(annotatedTarget).contains("insertedText"));

        Log.debug(Geonet.DIFF, "\n\n%%%%%%%%%%%\n"+Xml.getString(annotatedSource));
        Log.debug(Geonet.DIFF, "\n\n%%%%%%%%%%%\n"+Xml.getString(annotatedTarget));
    }



}