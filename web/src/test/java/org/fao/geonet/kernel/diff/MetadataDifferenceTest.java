package org.fao.geonet.kernel.diff;

import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.test.TestCase;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.util.Set;

/**
 *
 * Unit test for metadata difference.
 *
 * @author heikki doeleman
 *
 */
public class MetadataDifferenceTest extends TestCase {

    private XMLOutputter outputter = new XMLOutputter(Format.getRawFormat());

    public MetadataDifferenceTest(String name) throws Exception {
		super(name);
	}

    //
    // general tests
    //

    public void testNoDifference() throws Exception {
        String xml1 = "<?xml version=\"1.0\"?><root/>";
        String xml2 = "<?xml version=\"1.0\"?><root/>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("XML input is not different", "<delta />", outputter.outputString(diff));
    }

    public void testXmlDeclarationOrNot() throws Exception {
        String xml1 = "<?xml version=\"1.0\"?><root/>";
        String xml2 = "<root/>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("XML input is not different", "<delta />", outputter.outputString(diff));
    }

    public void testAttributeOrder() throws Exception {
        String xml1 = "<?xml version=\"1.0\"?><root attr1=\"one\" attr2=\"two\"/>";
        String xml2 = "<?xml version=\"1.0\"?><root attr2=\"two\" attr1=\"one\"/>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("XML input is not different", "<delta />", outputter.outputString(diff));
    }


    //
    // namespace
    //

    public void testAddNamespace() throws Exception{
        String xml1 = "<root><a/></root>";
        String xml2 = "<root xmlns:zzz=\"http://zzzzzzz\"><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for adding a namespace", "<delta><AttributeInserted name=\"xmlns:zzz\" value=\"http://zzzzzzz\" oldpos=\"0:0\" pos=\"0:0\" /></delta>", outputter.outputString(diff));
    }

    public void testRemoveNamespace() throws Exception{
        String xml1 = "<root xmlns:zzz=\"http://zzzzzzz\"><a/></root>";
        String xml2 = "<root><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for adding a namespace", "<delta><AttributeDeleted name=\"xmlns:zzz\" oldpos=\"0:0\" pos=\"0:0\" /></delta>", outputter.outputString(diff));
    }

    public void testSwapNamespacePrefixes() throws Exception{
        String xml1 = "<root xmlns:aaa=\"http://zzzzzzz\" xmlns:bbb=\"http://yyyyyyy\"><a/></root>";
        String xml2 = "<root xmlns:aaa=\"http://yyyyyyy\" xmlns:bbb=\"http://zzzzzzz\"><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for swapping namespace prefixes", "<delta />", outputter.outputString(diff));
    }

    public void testDeleteme() throws Exception{
        String xml1 = "<root xmlns:aaa=\"http://zzzzzzz\" xmlns:bbb=\"http://yyyyyyy\"><x></x><a>a</a><b></b></root>";
        String xml2 = "<root xmlns:aaa=\"http://zzzzzzz\" xmlns:bbb=\"http://yyyyyyy\"><x></x><a>zzz</a><b></b></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for swapping namespace prefixes", "<delta />", outputter.outputString(diff));
    }


    public void testDeletemee() throws Exception{
        String xml1 = "<root xmlns:aaa=\"http://zzzzzzz\" xmlns:bbb=\"http://yyyyyyy\"> " + "<x></x><a>a</a><b></b></root>";
        String xml2 = "<root xmlns:aaa=\"http://zzzzzzz\" xmlns:bbb=\"http://yyyyyyy\"> " + "<x></x><a>zzz</a><b></b></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for swapping namespace prefixes", "<delta />", outputter.outputString(diff));
    }

    public void testDeleteMe2() throws Exception  {
        String xml1="<?xml version=\"1.0\" encoding=\"UTF-8\"?><gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gts=\"http://www.isotc211.org/2005/gts\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:geonet=\"http://www.fao.org/geonetwork\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.isotc211.org/2005/gmd http://www.isotc211.org/2005/gmd/gmd.xsd http://www.isotc211.org/2005/srv http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd\"><gmd:fileIdentifier><gco:CharacterString>cc1b80d8-a1fb-48bb-9d0b-575b9ffea444</gco:CharacterString></gmd:fileIdentifier>" +
                "  <gmd:language>" +
                "      <gmd:LanguageCode codeList=\"http://www.loc.gov/standards/iso639-2/\" " +
                "codeListValue=\"eng\"/>" +
                "  </gmd:language>" +
                "  <gmd:characterSet>\n" +
                "      <gmd:MD_CharacterSetCode codeListValue=\"utf8\"\n" +
                "                               codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_CharacterSetCode\"/>\n" +
                "  </gmd:characterSet>\n" +
                "  <gmd:contact>\n" +
                "      <gmd:CI_ResponsibleParty>\n" +
                "         <gmd:individualName gco:nilReason=\"missing\">\n" +
                "            <gco:CharacterString/>\n" +
                "         </gmd:individualName>\n" +
                "         <gmd:organisationName gco:nilReason=\"missing\">\n" +
                "            <gco:CharacterString/>\n" +
                "         </gmd:organisationName>\n" +
                "         <gmd:positionName gco:nilReason=\"missing\">\n" +
                "            <gco:CharacterString/>\n" +
                "         </gmd:positionName>\n" +
                "         <gmd:contactInfo>\n" +
                "            <gmd:CI_Contact>\n" +
                "               <gmd:phone>\n" +
                "                  <gmd:CI_Telephone>\n" +
                "                     <gmd:voice gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:voice>\n" +
                "                     <gmd:facsimile gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:facsimile>\n" +
                "                  </gmd:CI_Telephone>\n" +
                "               </gmd:phone>\n" +
                "               <gmd:address>\n" +
                "                  <gmd:CI_Address>\n" +
                "                     <gmd:deliveryPoint gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:deliveryPoint>\n" +
                "                     <gmd:city gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:city>\n" +
                "                     <gmd:administrativeArea gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:administrativeArea>\n" +
                "                     <gmd:postalCode gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:postalCode>\n" +
                "                     <gmd:country gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:country>\n" +
                "                     <gmd:electronicMailAddress gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:electronicMailAddress>\n" +
                "                  </gmd:CI_Address>\n" +
                "               </gmd:address>\n" +
                "            </gmd:CI_Contact>\n" +
                "         </gmd:contactInfo>\n" +
                "         <gmd:role>\n" +
                "            <gmd:CI_RoleCode codeListValue=\"pointOfContact\"\n" +
                "                             codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_RoleCode\"/>\n" +
                "         </gmd:role>\n" +
                "      </gmd:CI_ResponsibleParty>\n" +
                "  </gmd:contact>\n" +
                "  <gmd:dateStamp>\n" +
                "      <gco:DateTime>2012-06-20T14:26:49</gco:DateTime>\n" +
                "  </gmd:dateStamp>\n" +
                "  <gmd:metadataStandardName>\n" +
                "      <gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>\n" +
                "  </gmd:metadataStandardName>\n" +
                "  <gmd:metadataStandardVersion>\n" +
                "      <gco:CharacterString>1.0</gco:CharacterString>\n" +
                "  </gmd:metadataStandardVersion>\n" +
                "  <gmd:referenceSystemInfo>\n" +
                "      <gmd:MD_ReferenceSystem>\n" +
                "         <gmd:referenceSystemIdentifier>\n" +
                "            <gmd:RS_Identifier>\n" +
                "               <gmd:code>\n" +
                "                  <gco:CharacterString>WGS 1984</gco:CharacterString>\n" +
                "               </gmd:code>\n" +
                "            </gmd:RS_Identifier>\n" +
                "         </gmd:referenceSystemIdentifier>\n" +
                "      </gmd:MD_ReferenceSystem>\n" +
                "  </gmd:referenceSystemInfo>\n" +
                "  <gmd:identificationInfo>\n" +
                "      <gmd:MD_DataIdentification>\n" +
                "         <gmd:citation>\n" +
                "            <gmd:CI_Citation>\n" +
                "               <gmd:title>\n" +
                "                  <gco:CharacterString>AA</gco:CharacterString>\n" +
                "               </gmd:title>\n" +
                "               <gmd:date>\n" +
                "                  <gmd:CI_Date>\n" +
                "                     <gmd:date>\n" +
                "                        <gco:DateTime/>\n" +
                "                     </gmd:date>\n" +
                "                     <gmd:dateType>\n" +
                "                        <gmd:CI_DateTypeCode codeListValue=\"publication\"\n" +
                "                                             codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_DateTypeCode\"/>\n" +
                "                     </gmd:dateType>\n" +
                "                  </gmd:CI_Date>\n" +
                "               </gmd:date>\n" +
                "               <gmd:edition gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:edition>\n" +
                "               <gmd:presentationForm>\n" +
                "                  <gmd:CI_PresentationFormCode codeListValue=\"mapDigital\"\n" +
                "                                               codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_PresentationFormCode\"/>\n" +
                "               </gmd:presentationForm>\n" +
                "            </gmd:CI_Citation>\n" +
                "         </gmd:citation>\n" +
                "         <gmd:abstract>\n" +
                "            <gco:CharacterString>The ISO19115 metadata standard is the preferred metadata standard " +
                "to use. If unsure what templates to start with, use this one.</gco:CharacterString>\n" +
                "         </gmd:abstract>\n" +
                "         <gmd:purpose gco:nilReason=\"missing\">\n" +
                "            <gco:CharacterString/>\n" +
                "         </gmd:purpose>\n" +
                "         <gmd:status>\n" +
                "            <gmd:MD_ProgressCode codeListValue=\"onGoing\"\n" +
                "                                 codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_ProgressCode\"/>\n" +
                "         </gmd:status>\n" +
                "         <gmd:pointOfContact>\n" +
                "            <gmd:CI_ResponsibleParty>\n" +
                "               <gmd:individualName gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:individualName>\n" +
                "               <gmd:organisationName gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
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
                "                  </gmd:CI_Contact>\n" +
                "               </gmd:contactInfo>\n" +
                "               <gmd:role>\n" +
                "                  <gmd:CI_RoleCode codeListValue=\"originator\"\n" +
                "                                   codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_RoleCode\"/>\n" +
                "               </gmd:role>\n" +
                "            </gmd:CI_ResponsibleParty>\n" +
                "         </gmd:pointOfContact>\n" +
                "         <gmd:resourceMaintenance>\n" +
                "            <gmd:MD_MaintenanceInformation>\n" +
                "               <gmd:maintenanceAndUpdateFrequency>\n" +
                "                  <gmd:MD_MaintenanceFrequencyCode codeListValue=\"asNeeded\"\n" +
                "                                                   codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_MaintenanceFrequencyCode\"/>\n" +
                "               </gmd:maintenanceAndUpdateFrequency>\n" +
                "            </gmd:MD_MaintenanceInformation>\n" +
                "         </gmd:resourceMaintenance>\n" +
                "         <gmd:graphicOverview>\n" +
                "            <gmd:MD_BrowseGraphic>\n" +
                "               <gmd:fileName gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:fileName>\n" +
                "               <gmd:fileDescription>\n" +
                "                  <gco:CharacterString>thumbnail</gco:CharacterString>\n" +
                "               </gmd:fileDescription>\n" +
                "            </gmd:MD_BrowseGraphic>\n" +
                "         </gmd:graphicOverview>\n" +
                "         <gmd:graphicOverview>\n" +
                "            <gmd:MD_BrowseGraphic>\n" +
                "               <gmd:fileName gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:fileName>\n" +
                "               <gmd:fileDescription>\n" +
                "                  <gco:CharacterString>large_thumbnail</gco:CharacterString>\n" +
                "               </gmd:fileDescription>\n" +
                "            </gmd:MD_BrowseGraphic>\n" +
                "         </gmd:graphicOverview>\n" +
                "         <gmd:descriptiveKeywords>\n" +
                "            <gmd:MD_Keywords>\n" +
                "               <gmd:keyword gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:keyword>\n" +
                "               <gmd:type>\n" +
                "                  <gmd:MD_KeywordTypeCode codeListValue=\"theme\"\n" +
                "                                          codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_KeywordTypeCode\"/>\n" +
                "               </gmd:type>\n" +
                "            </gmd:MD_Keywords>\n" +
                "         </gmd:descriptiveKeywords>\n" +
                "         <gmd:descriptiveKeywords>\n" +
                "            <gmd:MD_Keywords>\n" +
                "               <gmd:keyword>\n" +
                "                  <gco:CharacterString>World</gco:CharacterString>\n" +
                "               </gmd:keyword>\n" +
                "               <gmd:type>\n" +
                "                  <gmd:MD_KeywordTypeCode codeListValue=\"place\"\n" +
                "                                          codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_KeywordTypeCode\"/>\n" +
                "               </gmd:type>\n" +
                "            </gmd:MD_Keywords>\n" +
                "         </gmd:descriptiveKeywords>\n" +
                "         <gmd:resourceConstraints>\n" +
                "            <gmd:MD_LegalConstraints>\n" +
                "               <gmd:accessConstraints>\n" +
                "                  <gmd:MD_RestrictionCode codeListValue=\"copyright\"\n" +
                "                                          codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_RestrictionCode\"/>\n" +
                "               </gmd:accessConstraints>\n" +
                "               <gmd:useConstraints>\n" +
                "                  <gmd:MD_RestrictionCode codeListValue=\"\"\n" +
                "                                          codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_RestrictionCode\"/>\n" +
                "               </gmd:useConstraints>\n" +
                "               <gmd:otherConstraints gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:otherConstraints>\n" +
                "            </gmd:MD_LegalConstraints>\n" +
                "         </gmd:resourceConstraints>\n" +
                "         <gmd:spatialRepresentationType>\n" +
                "            <gmd:MD_SpatialRepresentationTypeCode codeListValue=\"vector\"\n" +
                "                                                  codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_SpatialRepresentationTypeCode\"/>\n" +
                "         </gmd:spatialRepresentationType>\n" +
                "         <gmd:spatialResolution>\n" +
                "            <gmd:MD_Resolution>\n" +
                "               <gmd:equivalentScale>\n" +
                "                  <gmd:MD_RepresentativeFraction>\n" +
                "                     <gmd:denominator>\n" +
                "                        <gco:Integer/>\n" +
                "                     </gmd:denominator>\n" +
                "                  </gmd:MD_RepresentativeFraction>\n" +
                "               </gmd:equivalentScale>\n" +
                "            </gmd:MD_Resolution>\n" +
                "         </gmd:spatialResolution>\n" +
                "         <gmd:language>\n" +
                "            <gmd:LanguageCode codeList=\"http://www.loc.gov/standards/iso639-2/\" " +
                "codeListValue=\"eng\"/>\n" +
                "         </gmd:language>\n" +
                "         <gmd:characterSet>\n" +
                "            <gmd:MD_CharacterSetCode codeListValue=\"utf8\"\n" +
                "                                     codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_CharacterSetCode\"/>\n" +
                "         </gmd:characterSet>\n" +
                "         <gmd:topicCategory>\n" +
                "            <gmd:MD_TopicCategoryCode>boundaries</gmd:MD_TopicCategoryCode>\n" +
                "         </gmd:topicCategory>\n" +
                "         <gmd:extent>\n" +
                "            <gmd:EX_Extent>\n" +
                "               <gmd:temporalElement>\n" +
                "                  <gmd:EX_TemporalExtent>\n" +
                "                     <gmd:extent>\n" +
                "                        <gml:TimePeriod gml:id=\"d735e433a1052958\">\n" +
                "                           <gml:beginPosition/>\n" +
                "                           <gml:endPosition/>\n" +
                "                        </gml:TimePeriod>\n" +
                "                     </gmd:extent>\n" +
                "                  </gmd:EX_TemporalExtent>\n" +
                "               </gmd:temporalElement>\n" +
                "            </gmd:EX_Extent>\n" +
                "         </gmd:extent>\n" +
                "         <gmd:extent>\n" +
                "            <gmd:EX_Extent>\n" +
                "               <gmd:geographicElement>\n" +
                "                  <gmd:EX_GeographicBoundingBox>\n" +
                "                     <gmd:westBoundLongitude>\n" +
                "                        <gco:Decimal>-180</gco:Decimal>\n" +
                "                     </gmd:westBoundLongitude>\n" +
                "                     <gmd:eastBoundLongitude>\n" +
                "                        <gco:Decimal>180</gco:Decimal>\n" +
                "                     </gmd:eastBoundLongitude>\n" +
                "                     <gmd:southBoundLatitude>\n" +
                "                        <gco:Decimal>-90</gco:Decimal>\n" +
                "                     </gmd:southBoundLatitude>\n" +
                "                     <gmd:northBoundLatitude>\n" +
                "                        <gco:Decimal>90</gco:Decimal>\n" +
                "                     </gmd:northBoundLatitude>\n" +
                "                  </gmd:EX_GeographicBoundingBox>\n" +
                "               </gmd:geographicElement>\n" +
                "            </gmd:EX_Extent>\n" +
                "         </gmd:extent>\n" +
                "         <gmd:supplementalInformation>\n" +
                "            <gco:CharacterString>You can customize the template to suit your needs. You can add and " +
                "remove fields and fill out default information (e.g. contact details). Fields you can not change in " +
                "the default view may be accessible in the more comprehensive (and more complex) advanced view. You " +
                "can even use the XML editor to create custom structures, but they have to be validated by the " +
                "system, so know what you do :-)</gco:CharacterString>\n" +
                "         </gmd:supplementalInformation>\n" +
                "      </gmd:MD_DataIdentification>\n" +
                "  </gmd:identificationInfo>\n" +
                "  <gmd:distributionInfo>\n" +
                "      <gmd:MD_Distribution>\n" +
                "         <gmd:transferOptions>\n" +
                "            <gmd:MD_DigitalTransferOptions>\n" +
                "               <gmd:onLine>\n" +
                "                  <gmd:CI_OnlineResource>\n" +
                "                     <gmd:linkage>\n" +
                "                        <gmd:URL/>\n" +
                "                     </gmd:linkage>\n" +
                "                     <gmd:protocol>\n" +
                "                        <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>\n" +
                "                     </gmd:protocol>\n" +
                "                     <gmd:name gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:name>\n" +
                "                     <gmd:description gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:description>\n" +
                "                  </gmd:CI_OnlineResource>\n" +
                "               </gmd:onLine>\n" +
                "               <gmd:onLine>\n" +
                "                  <gmd:CI_OnlineResource>\n" +
                "                     <gmd:linkage>\n" +
                "                        <gmd:URL>http://localhost:8080/geonetwork/srv/en/resources" +
                ".get?id=h95a8c4c_9038_43aa_a005_c943bc731c8d&amp;fname=&amp;access=private</gmd:URL>\n" +
                "                     </gmd:linkage>\n" +
                "                     <gmd:protocol>\n" +
                "                        <gco:CharacterString>WWW:DOWNLOAD-1.0-http--download</gco:CharacterString>\n" +
                "                     </gmd:protocol>\n" +
                "                     <gmd:name>\n" +
                "                        <gmx:MimeFileType xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" " +
                "type=\"\"/>\n" +
                "                     </gmd:name>\n" +
                "                     <gmd:description>\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:description>\n" +
                "                  </gmd:CI_OnlineResource>\n" +
                "               </gmd:onLine>\n" +
                "               <gmd:onLine>\n" +
                "                  <gmd:CI_OnlineResource>\n" +
                "                     <gmd:linkage>\n" +
                "                        <gmd:URL/>\n" +
                "                     </gmd:linkage>\n" +
                "                     <gmd:protocol>\n" +
                "                        <gco:CharacterString>OGC:WMS-1.1.1-http-get-map</gco:CharacterString>\n" +
                "                     </gmd:protocol>\n" +
                "                     <gmd:name gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:name>\n" +
                "                     <gmd:description gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:description>\n" +
                "                  </gmd:CI_OnlineResource>\n" +
                "               </gmd:onLine>\n" +
                "            </gmd:MD_DigitalTransferOptions>\n" +
                "         </gmd:transferOptions>\n" +
                "      </gmd:MD_Distribution>\n" +
                "  </gmd:distributionInfo>\n" +
                "  <gmd:dataQualityInfo>\n" +
                "      <gmd:DQ_DataQuality>\n" +
                "         <gmd:scope>\n" +
                "            <gmd:DQ_Scope>\n" +
                "               <gmd:level>\n" +
                "                  <gmd:MD_ScopeCode codeListValue=\"dataset\"\n" +
                "                                    codeList=\"http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_ScopeCode\"/>\n" +
                "               </gmd:level>\n" +
                "            </gmd:DQ_Scope>\n" +
                "         </gmd:scope>\n" +
                "         <gmd:lineage>\n" +
                "            <gmd:LI_Lineage>\n" +
                "               <gmd:statement gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:statement>\n" +
                "            </gmd:LI_Lineage>\n" +
                "         </gmd:lineage>\n" +
                "      </gmd:DQ_DataQuality>\n" +
                "  </gmd:dataQualityInfo>\n" +
                "</gmd:MD_Metadata>";

        String xml2="<?xml version=\"1.0\" encoding=\"UTF-8\"?><gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gts=\"http://www.isotc211.org/2005/gts\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:geonet=\"http://www.fao.org/geonetwork\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.isotc211.org/2005/gmd http://www.isotc211.org/2005/gmd/gmd.xsd http://www.isotc211.org/2005/srv http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd\"><gmd:fileIdentifier><gco:CharacterString>cc1b80d8-a1fb-48bb-9d0b-575b9ffea666</gco:CharacterString></gmd:fileIdentifier>" +
                "  <gmd:language>" +
                "      <gmd:LanguageCode codeList=\"http://www.loc.gov/standards/iso639-2/\" " +
                "codeListValue=\"eng\"/>" +
                "  </gmd:language>" +
                "  <gmd:characterSet>\n" +
                "      <gmd:MD_CharacterSetCode codeListValue=\"utf8\"\n" +
                "                               codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_CharacterSetCode\"/>\n" +
                "  </gmd:characterSet>\n" +
                "  <gmd:contact>\n" +
                "      <gmd:CI_ResponsibleParty>\n" +
                "         <gmd:individualName gco:nilReason=\"missing\">\n" +
                "            <gco:CharacterString/>\n" +
                "         </gmd:individualName>\n" +
                "         <gmd:organisationName gco:nilReason=\"missing\">\n" +
                "            <gco:CharacterString/>\n" +
                "         </gmd:organisationName>\n" +
                "         <gmd:positionName gco:nilReason=\"missing\">\n" +
                "            <gco:CharacterString/>\n" +
                "         </gmd:positionName>\n" +
                "         <gmd:contactInfo>\n" +
                "            <gmd:CI_Contact>\n" +
                "               <gmd:phone>\n" +
                "                  <gmd:CI_Telephone>\n" +
                "                     <gmd:voice gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:voice>\n" +
                "                     <gmd:facsimile gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:facsimile>\n" +
                "                  </gmd:CI_Telephone>\n" +
                "               </gmd:phone>\n" +
                "               <gmd:address>\n" +
                "                  <gmd:CI_Address>\n" +
                "                     <gmd:deliveryPoint gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:deliveryPoint>\n" +
                "                     <gmd:city gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:city>\n" +
                "                     <gmd:administrativeArea gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:administrativeArea>\n" +
                "                     <gmd:postalCode gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:postalCode>\n" +
                "                     <gmd:country gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:country>\n" +
                "                     <gmd:electronicMailAddress gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:electronicMailAddress>\n" +
                "                  </gmd:CI_Address>\n" +
                "               </gmd:address>\n" +
                "            </gmd:CI_Contact>\n" +
                "         </gmd:contactInfo>\n" +
                "         <gmd:role>\n" +
                "            <gmd:CI_RoleCode codeListValue=\"pointOfContact\"\n" +
                "                             codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_RoleCode\"/>\n" +
                "         </gmd:role>\n" +
                "      </gmd:CI_ResponsibleParty>\n" +
                "  </gmd:contact>\n" +
                "  <gmd:dateStamp>\n" +
                "      <gco:DateTime>2012-06-20T14:26:49</gco:DateTime>\n" +
                "  </gmd:dateStamp>\n" +
                "  <gmd:metadataStandardName>\n" +
                "      <gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>\n" +
                "  </gmd:metadataStandardName>\n" +
                "  <gmd:metadataStandardVersion>\n" +
                "      <gco:CharacterString>1.0</gco:CharacterString>\n" +
                "  </gmd:metadataStandardVersion>\n" +
                "  <gmd:referenceSystemInfo>\n" +
                "      <gmd:MD_ReferenceSystem>\n" +
                "         <gmd:referenceSystemIdentifier>\n" +
                "            <gmd:RS_Identifier>\n" +
                "               <gmd:code>\n" +
                "                  <gco:CharacterString>WGS 1984</gco:CharacterString>\n" +
                "               </gmd:code>\n" +
                "            </gmd:RS_Identifier>\n" +
                "         </gmd:referenceSystemIdentifier>\n" +
                "      </gmd:MD_ReferenceSystem>\n" +
                "  </gmd:referenceSystemInfo>\n" +
                "  <gmd:identificationInfo>\n" +
                "      <gmd:MD_DataIdentification>\n" +
                "         <gmd:citation>\n" +
                "            <gmd:CI_Citation>\n" +
                "               <gmd:title>\n" +
                "                  <gco:CharacterString>AA</gco:CharacterString>\n" +
                "               </gmd:title>\n" +
                "               <gmd:date>\n" +
                "                  <gmd:CI_Date>\n" +
                "                     <gmd:date>\n" +
                "                        <gco:DateTime/>\n" +
                "                     </gmd:date>\n" +
                "                     <gmd:dateType>\n" +
                "                        <gmd:CI_DateTypeCode codeListValue=\"publication\"\n" +
                "                                             codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_DateTypeCode\"/>\n" +
                "                     </gmd:dateType>\n" +
                "                  </gmd:CI_Date>\n" +
                "               </gmd:date>\n" +
                "               <gmd:edition gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:edition>\n" +
                "               <gmd:presentationForm>\n" +
                "                  <gmd:CI_PresentationFormCode codeListValue=\"mapDigital\"\n" +
                "                                               codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_PresentationFormCode\"/>\n" +
                "               </gmd:presentationForm>\n" +
                "            </gmd:CI_Citation>\n" +
                "         </gmd:citation>\n" +
                "         <gmd:abstract>\n" +
                "            <gco:CharacterString>The ISO19115 metadata standard is the preferred metadata standard " +
                "to use. If unsure what templates to start with, use this one.</gco:CharacterString>\n" +
                "         </gmd:abstract>\n" +
                "         <gmd:purpose gco:nilReason=\"missing\">\n" +
                "            <gco:CharacterString/>\n" +
                "         </gmd:purpose>\n" +
                "         <gmd:status>\n" +
                "            <gmd:MD_ProgressCode codeListValue=\"onGoing\"\n" +
                "                                 codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_ProgressCode\"/>\n" +
                "         </gmd:status>\n" +
                "         <gmd:pointOfContact>\n" +
                "            <gmd:CI_ResponsibleParty>\n" +
                "               <gmd:individualName gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:individualName>\n" +
                "               <gmd:organisationName gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
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
                "                  </gmd:CI_Contact>\n" +
                "               </gmd:contactInfo>\n" +
                "               <gmd:role>\n" +
                "                  <gmd:CI_RoleCode codeListValue=\"originator\"\n" +
                "                                   codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#CI_RoleCode\"/>\n" +
                "               </gmd:role>\n" +
                "            </gmd:CI_ResponsibleParty>\n" +
                "         </gmd:pointOfContact>\n" +
                "         <gmd:resourceMaintenance>\n" +
                "            <gmd:MD_MaintenanceInformation>\n" +
                "               <gmd:maintenanceAndUpdateFrequency>\n" +
                "                  <gmd:MD_MaintenanceFrequencyCode codeListValue=\"asNeeded\"\n" +
                "                                                   codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_MaintenanceFrequencyCode\"/>\n" +
                "               </gmd:maintenanceAndUpdateFrequency>\n" +
                "            </gmd:MD_MaintenanceInformation>\n" +
                "         </gmd:resourceMaintenance>\n" +
                "         <gmd:graphicOverview>\n" +
                "            <gmd:MD_BrowseGraphic>\n" +
                "               <gmd:fileName gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:fileName>\n" +
                "               <gmd:fileDescription>\n" +
                "                  <gco:CharacterString>thumbnail</gco:CharacterString>\n" +
                "               </gmd:fileDescription>\n" +
                "            </gmd:MD_BrowseGraphic>\n" +
                "         </gmd:graphicOverview>\n" +
                "         <gmd:graphicOverview>\n" +
                "            <gmd:MD_BrowseGraphic>\n" +
                "               <gmd:fileName gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:fileName>\n" +
                "               <gmd:fileDescription>\n" +
                "                  <gco:CharacterString>large_thumbnail</gco:CharacterString>\n" +
                "               </gmd:fileDescription>\n" +
                "            </gmd:MD_BrowseGraphic>\n" +
                "         </gmd:graphicOverview>\n" +
                "         <gmd:descriptiveKeywords>\n" +
                "            <gmd:MD_Keywords>\n" +
                "               <gmd:keyword gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:keyword>\n" +
                "               <gmd:type>\n" +
                "                  <gmd:MD_KeywordTypeCode codeListValue=\"theme\"\n" +
                "                                          codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_KeywordTypeCode\"/>\n" +
                "               </gmd:type>\n" +
                "            </gmd:MD_Keywords>\n" +
                "         </gmd:descriptiveKeywords>\n" +
                "         <gmd:descriptiveKeywords>\n" +
                "            <gmd:MD_Keywords>\n" +
                "               <gmd:keyword>\n" +
                "                  <gco:CharacterString>World</gco:CharacterString>\n" +
                "               </gmd:keyword>\n" +
                "               <gmd:type>\n" +
                "                  <gmd:MD_KeywordTypeCode codeListValue=\"place\"\n" +
                "                                          codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_KeywordTypeCode\"/>\n" +
                "               </gmd:type>\n" +
                "            </gmd:MD_Keywords>\n" +
                "         </gmd:descriptiveKeywords>\n" +
                "         <gmd:resourceConstraints>\n" +
                "            <gmd:MD_LegalConstraints>\n" +
                "               <gmd:accessConstraints>\n" +
                "                  <gmd:MD_RestrictionCode codeListValue=\"copyright\"\n" +
                "                                          codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_RestrictionCode\"/>\n" +
                "               </gmd:accessConstraints>\n" +
                "               <gmd:useConstraints>\n" +
                "                  <gmd:MD_RestrictionCode codeListValue=\"\"\n" +
                "                                          codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_RestrictionCode\"/>\n" +
                "               </gmd:useConstraints>\n" +
                "               <gmd:otherConstraints gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:otherConstraints>\n" +
                "            </gmd:MD_LegalConstraints>\n" +
                "         </gmd:resourceConstraints>\n" +
                "         <gmd:spatialRepresentationType>\n" +
                "            <gmd:MD_SpatialRepresentationTypeCode codeListValue=\"vector\"\n" +
                "                                                  codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_SpatialRepresentationTypeCode\"/>\n" +
                "         </gmd:spatialRepresentationType>\n" +
                "         <gmd:spatialResolution>\n" +
                "            <gmd:MD_Resolution>\n" +
                "               <gmd:equivalentScale>\n" +
                "                  <gmd:MD_RepresentativeFraction>\n" +
                "                     <gmd:denominator>\n" +
                "                        <gco:Integer/>\n" +
                "                     </gmd:denominator>\n" +
                "                  </gmd:MD_RepresentativeFraction>\n" +
                "               </gmd:equivalentScale>\n" +
                "            </gmd:MD_Resolution>\n" +
                "         </gmd:spatialResolution>\n" +
                "         <gmd:language>\n" +
                "            <gmd:LanguageCode codeList=\"http://www.loc.gov/standards/iso639-2/\" " +
                "codeListValue=\"eng\"/>\n" +
                "         </gmd:language>\n" +
                "         <gmd:characterSet>\n" +
                "            <gmd:MD_CharacterSetCode codeListValue=\"utf8\"\n" +
                "                                     codeList=\"http://standards.iso" +
                ".org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists" +
                ".xml#MD_CharacterSetCode\"/>\n" +
                "         </gmd:characterSet>\n" +
                "         <gmd:topicCategory>\n" +
                "            <gmd:MD_TopicCategoryCode>boundaries</gmd:MD_TopicCategoryCode>\n" +
                "         </gmd:topicCategory>\n" +
                "         <gmd:extent>\n" +
                "            <gmd:EX_Extent>\n" +
                "               <gmd:temporalElement>\n" +
                "                  <gmd:EX_TemporalExtent>\n" +
                "                     <gmd:extent>\n" +
                "                        <gml:TimePeriod gml:id=\"d735e433a1052958\">\n" +
                "                           <gml:beginPosition/>\n" +
                "                           <gml:endPosition/>\n" +
                "                        </gml:TimePeriod>\n" +
                "                     </gmd:extent>\n" +
                "                  </gmd:EX_TemporalExtent>\n" +
                "               </gmd:temporalElement>\n" +
                "            </gmd:EX_Extent>\n" +
                "         </gmd:extent>\n" +
                "         <gmd:extent>\n" +
                "            <gmd:EX_Extent>\n" +
                "               <gmd:geographicElement>\n" +
                "                  <gmd:EX_GeographicBoundingBox>\n" +
                "                     <gmd:westBoundLongitude>\n" +
                "                        <gco:Decimal>-180</gco:Decimal>\n" +
                "                     </gmd:westBoundLongitude>\n" +
                "                     <gmd:eastBoundLongitude>\n" +
                "                        <gco:Decimal>180</gco:Decimal>\n" +
                "                     </gmd:eastBoundLongitude>\n" +
                "                     <gmd:southBoundLatitude>\n" +
                "                        <gco:Decimal>-90</gco:Decimal>\n" +
                "                     </gmd:southBoundLatitude>\n" +
                "                     <gmd:northBoundLatitude>\n" +
                "                        <gco:Decimal>90</gco:Decimal>\n" +
                "                     </gmd:northBoundLatitude>\n" +
                "                  </gmd:EX_GeographicBoundingBox>\n" +
                "               </gmd:geographicElement>\n" +
                "            </gmd:EX_Extent>\n" +
                "         </gmd:extent>\n" +
                "         <gmd:supplementalInformation>\n" +
                "            <gco:CharacterString>You can customize the template to suit your needs. You can add and " +
                "remove fields and fill out default information (e.g. contact details). Fields you can not change in " +
                "the default view may be accessible in the more comprehensive (and more complex) advanced view. You " +
                "can even use the XML editor to create custom structures, but they have to be validated by the " +
                "system, so know what you do :-)</gco:CharacterString>\n" +
                "         </gmd:supplementalInformation>\n" +
                "      </gmd:MD_DataIdentification>\n" +
                "  </gmd:identificationInfo>\n" +
                "  <gmd:distributionInfo>\n" +
                "      <gmd:MD_Distribution>\n" +
                "         <gmd:transferOptions>\n" +
                "            <gmd:MD_DigitalTransferOptions>\n" +
                "               <gmd:onLine>\n" +
                "                  <gmd:CI_OnlineResource>\n" +
                "                     <gmd:linkage>\n" +
                "                        <gmd:URL/>\n" +
                "                     </gmd:linkage>\n" +
                "                     <gmd:protocol>\n" +
                "                        <gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString>\n" +
                "                     </gmd:protocol>\n" +
                "                     <gmd:name gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:name>\n" +
                "                     <gmd:description gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:description>\n" +
                "                  </gmd:CI_OnlineResource>\n" +
                "               </gmd:onLine>\n" +
                "               <gmd:onLine>\n" +
                "                  <gmd:CI_OnlineResource>\n" +
                "                     <gmd:linkage>\n" +
                "                        <gmd:URL>http://localhost:8080/geonetwork/srv/en/resources" +
                ".get?id=h95a8c4c_9038_43aa_a005_c943bc731c8d&amp;fname=&amp;access=private</gmd:URL>\n" +
                "                     </gmd:linkage>\n" +
                "                     <gmd:protocol>\n" +
                "                        <gco:CharacterString>WWW:DOWNLOAD-1.0-http--download</gco:CharacterString>\n" +
                "                     </gmd:protocol>\n" +
                "                     <gmd:name>\n" +
                "                        <gmx:MimeFileType xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" " +
                "type=\"\"/>\n" +
                "                     </gmd:name>\n" +
                "                     <gmd:description>\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:description>\n" +
                "                  </gmd:CI_OnlineResource>\n" +
                "               </gmd:onLine>\n" +
                "               <gmd:onLine>\n" +
                "                  <gmd:CI_OnlineResource>\n" +
                "                     <gmd:linkage>\n" +
                "                        <gmd:URL/>\n" +
                "                     </gmd:linkage>\n" +
                "                     <gmd:protocol>\n" +
                "                        <gco:CharacterString>OGC:WMS-1.1.1-http-get-map</gco:CharacterString>\n" +
                "                     </gmd:protocol>\n" +
                "                     <gmd:name gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:name>\n" +
                "                     <gmd:description gco:nilReason=\"missing\">\n" +
                "                        <gco:CharacterString/>\n" +
                "                     </gmd:description>\n" +
                "                  </gmd:CI_OnlineResource>\n" +
                "               </gmd:onLine>\n" +
                "            </gmd:MD_DigitalTransferOptions>\n" +
                "         </gmd:transferOptions>\n" +
                "      </gmd:MD_Distribution>\n" +
                "  </gmd:distributionInfo>\n" +
                "  <gmd:dataQualityInfo>\n" +
                "      <gmd:DQ_DataQuality>\n" +
                "         <gmd:scope>\n" +
                "            <gmd:DQ_Scope>\n" +
                "               <gmd:level>\n" +
                "                  <gmd:MD_ScopeCode codeListValue=\"dataset\"\n" +
                "                                    codeList=\"http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_ScopeCode\"/>\n" +
                "               </gmd:level>\n" +
                "            </gmd:DQ_Scope>\n" +
                "         </gmd:scope>\n" +
                "         <gmd:lineage>\n" +
                "            <gmd:LI_Lineage>\n" +
                "               <gmd:statement gco:nilReason=\"missing\">\n" +
                "                  <gco:CharacterString/>\n" +
                "               </gmd:statement>\n" +
                "            </gmd:LI_Lineage>\n" +
                "         </gmd:lineage>\n" +
                "      </gmd:DQ_DataQuality>\n" +
                "  </gmd:dataQualityInfo>\n" +
                "</gmd:MD_Metadata>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for swapping namespace prefixes", "<delta />", outputter.outputString(diff));

    }

    public void testEquivalentNamespaces() throws Exception{
        String xml1 = "<aaa:root xmlns:aaa=\"http://zzzzzzz\"><aaa:a/></aaa:root>";
        String xml2 = "<root xmlns=\"http://zzzzzzz\"><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for swapping namespace prefixes", "<delta />", outputter.outputString(diff));
    }

    public void testEquivalentNamespaces2() throws Exception{
        String xml1 = "<aaa:root xmlns:aaa=\"http://zzzzzzz\"><aaa:a/><bbb:b xmlns:bbb=\"http://zzzzzzz\"/></aaa:root>";
        String xml2 = "<root xmlns=\"http://zzzzzzz\"><a/><b/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for swapping namespace prefixes", "<delta />", outputter.outputString(diff));
        assertEquals("Unexpected structural change", "<aaa:root xmlns:aaa=\"http://zzzzzzz\"><aaa:a /><bbb:b xmlns:bbb=\"http://zzzzzzz\" /></aaa:root>",
                outputter.outputString(xml1e));
        assertEquals("Unexpected structural change", "<root xmlns=\"http://zzzzzzz\"><a /><b /></root>",
                outputter.outputString(xml2e));
    }

    public void testNamespacePrefixDifference() throws Exception {
        String xml1 = "<?xml version=\"1.0\"?><root xmlns:tst=\"http://test.test\"><tst:a/></root>";
        String xml2 = "<?xml version=\"1.0\"?><root xmlns:tzt=\"http://test.test\"><tzt:a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("XML input is not different", "<delta />", outputter.outputString(diff));
    }

    public void testNamespacePrefixDifferenceRoot() throws Exception {
        String xml1 = "<?xml version=\"1.0\"?><tst:root xmlns:tst=\"http://test.test\"><tst:a/></tst:root>";
        String xml2 = "<?xml version=\"1.0\"?><tzt:root xmlns:tzt=\"http://test.test\"><tzt:a/></tzt:root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("XML input is not different", "<delta />", outputter.outputString(diff));
    }

    public void testNamespacePrefixDifferenceMixed() throws Exception {
        String xml1 = "<?xml version=\"1.0\"?><root xmlns=\"http://test.test\"><a/></root>";
        String xml2 = "<?xml version=\"1.0\"?><tzt:root xmlns:tzt=\"http://test.test\"><tzt:a/></tzt:root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("XML input is not different", "<delta />", outputter.outputString(diff));
    }

    public void testCalculateInterestingSet() {
        NodePosition nodePosition = new NodePosition("0:0:5:1");
        Set<String> interestingSet = nodePosition.interestingSet();
        assertContains("0", "0", interestingSet);
        assertContains("0:0", "0:0", interestingSet);
        assertContains("0:0:0", "0:0:0", interestingSet);
        assertContains("0:0:1", "0:0:1", interestingSet);
        assertContains("0:0:2", "0:0:2", interestingSet);
        assertContains("0:0:3", "0:0:3", interestingSet);
        assertContains("0:0:4", "0:0:4", interestingSet);
        assertContains("0:0:5", "0:0:5", interestingSet);
        assertContains("0:0:5:0", "0:0:5:0", interestingSet);
        assertContains("0:0:5:1", "0:0:5:1", interestingSet);
        assertEquals("unexpected size of interesting set", 10, interestingSet.size());
    }

    public void testPairDeleteAndInsert() throws Exception {
        Element delta = Xml.loadString("<Delta xmlns:xxx1=\"http://geonetwork.tv\" xmlns:xxx5=\"http://geonetwork.tv\"><Deleted oldpos=\"0:0:20\" pos=\"0:0:20\"></Deleted>" +
                "<Deleted move=\"yes\" oldpos=\"0:0:18\" pos=\"0:0:18\"></Deleted>" +
                "<Deleted oldpos=\"0:0:17:2\" pos=\"0:0:17:2\"></Deleted>" +
                "<Deleted oldpos=\"0:0:17:1:1:1:1:1:0\" update=\"yes\" pos=\"0:0:17:1:1:1:1:1:0\">Percentage of Urban" +
                " Population Map of Chittagong Hill Tracts, Bangladesh</Deleted>" +
                "<Deleted oldpos=\"0:0:17:0\" pos=\"0:0:17:0\"></Deleted>" +
                "<Deleted move=\"yes\" oldpos=\"0:0:16\" pos=\"0:0:16\"></Deleted>" +
                "<Deleted move=\"yes\" oldpos=\"0:0:14\" pos=\"0:0:14\"></Deleted>" +
                "<Deleted move=\"yes\" oldpos=\"0:0:12\" pos=\"0:0:12\"></Deleted>" +
                "<Deleted move=\"yes\" oldpos=\"0:0:11:2\" pos=\"0:0:11:2\"></Deleted>" +
                "<Deleted move=\"yes\" oldpos=\"0:0:11:0\" pos=\"0:0:11:0\"></Deleted>" +
                "<Deleted oldpos=\"0:0:11\" pos=\"0:0:11\"><xxx1:metadataStandardName><xxx5:CharacterString " +
                "xmlns:xxx8=\"http://www.isotc211.org/2005/srv\" xmlns:srv=\"http://www.isotc211.org/2005/srv\">ISO " +
                "19115:2003/19139</xxx5:CharacterString></xxx1:metadataStandardName></Deleted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:12\" pos=\"0:0:12\"></Inserted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:14\" pos=\"0:0:14\"></Inserted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:11:0\" pos=\"0:0:15:0\"></Inserted>" +
                "<Inserted update=\"yes\" pos=\"0:0:15:1:1:1:1:1:0\">HEEEEEEEE</Inserted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:11:2\" pos=\"0:0:15:2\"></Inserted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:16\" pos=\"0:0:16\"></Inserted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:18\" pos=\"0:0:18\"></Inserted></Delta>", false);

        Element delete = Xml.loadString("<Deleted oldpos=\"0:0:17:1:1:1:1:1:0\" update=\"yes\" pos=\"0:0:17:1:1:1:1:1:0\">Percentage of Urban" +
                " Population Map of Chittagong Hill Tracts, Bangladesh</Deleted>", false);

        assertEquals("Unexpected pairing of delete and insert",

                "<Deleted oldpos=\"0:0:17:1:1:1:1:1:0\" update=\"yes\" pos=\"0:0:15:1:1:1:1:1:0\">Percentage of Urban" +
                " Population Map of Chittagong Hill Tracts, Bangladesh</Deleted>",

                outputter.outputString(MetadataDifference.pairDeleteAndInsert(delete, delta)));
    }

    public void testPairDeletesAndInserts() throws Exception {
        Element delta = Xml.loadString("<Delta xmlns:xxx1=\"http://geonetwork.tv\" xmlns:xxx5=\"http://geonetwork.tv\"><Deleted oldpos=\"0:0:20\" pos=\"0:0:20\"></Deleted>" +
                "<Deleted move=\"yes\" oldpos=\"0:0:18\" pos=\"0:0:18\"></Deleted>" +
                "<Deleted oldpos=\"0:0:17:2\" pos=\"0:0:17:2\"></Deleted>" +
                "<Deleted oldpos=\"0:0:17:1:1:1:1:1:0\" update=\"yes\" pos=\"0:0:17:1:1:1:1:1:0\">Percentage of Urban" +
                " Population Map of Chittagong Hill Tracts, Bangladesh</Deleted>" +
                "<Deleted oldpos=\"0:0:17:0\" pos=\"0:0:17:0\"></Deleted>" +
                "<Deleted move=\"yes\" oldpos=\"0:0:16\" pos=\"0:0:16\"></Deleted>" +
                "<Deleted move=\"yes\" oldpos=\"0:0:14\" pos=\"0:0:14\"></Deleted>" +
                "<Deleted move=\"yes\" oldpos=\"0:0:12\" pos=\"0:0:12\"></Deleted>" +
                "<Deleted move=\"yes\" oldpos=\"0:0:11:2\" pos=\"0:0:11:2\"></Deleted>" +
                "<Deleted move=\"yes\" oldpos=\"0:0:11:0\" pos=\"0:0:11:0\"></Deleted>" +
                "<Deleted oldpos=\"0:0:11\" pos=\"0:0:11\"><xxx1:metadataStandardName><xxx5:CharacterString " +
                "xmlns:xxx8=\"http://www.isotc211.org/2005/srv\" xmlns:srv=\"http://www.isotc211.org/2005/srv\">ISO " +
                "19115:2003/19139</xxx5:CharacterString></xxx1:metadataStandardName></Deleted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:12\" pos=\"0:0:12\"></Inserted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:14\" pos=\"0:0:14\"></Inserted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:11:0\" pos=\"0:0:15:0\"></Inserted>" +
                "<Inserted update=\"yes\" pos=\"0:0:15:1:1:1:1:1:0\">HEEEEEEEE</Inserted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:11:2\" pos=\"0:0:15:2\"></Inserted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:16\" pos=\"0:0:16\"></Inserted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:18\" pos=\"0:0:18\"></Inserted></Delta>", false);

        assertEquals("Unexpected pairings of deletes and inserts",

                "<Delta xmlns:xxx1=\"http://geonetwork.tv\" xmlns:xxx5=\"http://geonetwork.tv\"><Deleted oldpos=\"0:0:20\" pos=\"0:0:20\" />" +
                "<Deleted move=\"yes\" oldpos=\"0:0:18\" pos=\"0:0:18\" />" +
                "<Deleted oldpos=\"0:0:17:2\" pos=\"0:0:17:2\" />" +
                "<Deleted oldpos=\"0:0:17:1:1:1:1:1:0\" update=\"yes\" pos=\"0:0:15:1:1:1:1:1:0\">Percentage of Urban" +
                " Population Map of Chittagong Hill Tracts, Bangladesh</Deleted>" +
                "<Deleted oldpos=\"0:0:17:0\" pos=\"0:0:17:0\" />" +
                "<Deleted move=\"yes\" oldpos=\"0:0:16\" pos=\"0:0:16\" />" +
                "<Deleted move=\"yes\" oldpos=\"0:0:14\" pos=\"0:0:14\" />" +
                "<Deleted move=\"yes\" oldpos=\"0:0:12\" pos=\"0:0:12\" />" +
                "<Deleted move=\"yes\" oldpos=\"0:0:11:2\" pos=\"0:0:11:2\" />" +
                "<Deleted move=\"yes\" oldpos=\"0:0:11:0\" pos=\"0:0:11:0\" />" +
                "<Deleted oldpos=\"0:0:11\" pos=\"0:0:11\"><xxx1:metadataStandardName><xxx5:CharacterString " +
                "xmlns:xxx8=\"http://www.isotc211.org/2005/srv\" xmlns:srv=\"http://www.isotc211.org/2005/srv\">ISO " +
                "19115:2003/19139</xxx5:CharacterString></xxx1:metadataStandardName></Deleted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:12\" pos=\"0:0:12\" />" +
                "<Inserted move=\"yes\" oldpos=\"0:0:14\" pos=\"0:0:14\" />" +
                "<Inserted move=\"yes\" oldpos=\"0:0:11:0\" pos=\"0:0:15:0\" />" +
                "<Inserted update=\"yes\" pos=\"0:0:15:1:1:1:1:1:0\">HEEEEEEEE</Inserted>" +
                "<Inserted move=\"yes\" oldpos=\"0:0:11:2\" pos=\"0:0:15:2\" />" +
                "<Inserted move=\"yes\" oldpos=\"0:0:16\" pos=\"0:0:16\" />" +
                "<Inserted move=\"yes\" oldpos=\"0:0:18\" pos=\"0:0:18\" /></Delta>",

                outputter.outputString(MetadataDifference.pairDeleteAndInserts(delta)));
    }


    //
    // element nodes
    //

    public void testInsertNode() throws Exception {
        String xml1 = "<root/>";
        String xml2 = "<root><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for inserting a node", "<delta><Inserted pos=\"0:0:0\"><a /></Inserted></delta>", outputter.outputString(diff));
    }

    public void testInsertNodeWithTextNode() throws Exception {
        String xml1 = "<root/>";
        String xml2 = "<root><a>xxx</a></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for inserting a node", "<delta><Inserted pos=\"0:0:0\"><a>xxx</a></Inserted></delta>", outputter.outputString(diff));
    }

    public void testDeleteNode() throws Exception {
        String xml1 = "<root><a/></root>";
        String xml2 = "<root/>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for deleting a node", "<delta><Deleted oldpos=\"0:0:0\" pos=\"0:0:0\"><a /></Deleted></delta>", outputter.outputString(diff));
    }


    //
    // attributes
    //

    public void testInsertAttribute() throws Exception {
        String xml1 = "<root><a/></root>";
        String xml2 = "<root><a attr=\"zzz\"/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for inserting an attribute", "<delta><AttributeInserted name=\"attr\" value=\"zzz\" oldpos=\"0:0:0\" pos=\"0:0:0\" /></delta>", outputter.outputString(diff));
    }

    public void testDeleteAttribute() throws Exception {
        String xml1 = "<root><a attr=\"zzz\"/></root>";
        String xml2 = "<root><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for deleting an attribute", "<delta><AttributeDeleted name=\"attr\" oldpos=\"0:0:0\" pos=\"0:0:0\" /></delta>", outputter.outputString(diff));
    }

    public void testUpdateAttribute() throws Exception {
        String xml1 = "<root><a attr=\"zzz\"/></root>";
        String xml2 = "<root><a attr=\"xxx\"/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for updating an attribute", "<delta><AttributeUpdated nv=\"xxx\" name=\"attr\" ov=\"zzz\" oldpos=\"0:0:0\" pos=\"0:0:0\" /></delta>", outputter.outputString(diff));
    }

    //
    // text nodes
    //

    public void testInsertTextNode() throws Exception {
        String xml1 = "<root><a/></root>";
        String xml2 = "<root><a>aaa</a></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for inserting a text node", "<delta><Inserted pos=\"0:0:0:0\">aaa</Inserted></delta>", outputter.outputString(diff));
    }

    public void testInsertTextNode2() throws Exception {
        String xml1 = "<root><a></a></root>";
        String xml2 = "<root><a>aaa</a></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for inserting a text node", "<delta><Inserted pos=\"0:0:0:0\">aaa</Inserted></delta>", outputter.outputString(diff));
    }

    public void testUpdateTextNode() throws Exception {
        String xml1 = "<root><a>a</a></root>";
        String xml2 = "<root><a>aa</a></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for updating a text node", "<delta><Deleted oldpos=\"0:0:0:0\" update=\"yes\" pos=\"0:0:0:0\">a</Deleted><Inserted update=\"yes\" pos=\"0:0:0:0\">aa</Inserted></delta>", outputter.outputString(diff));
    }

    public void testDeleteTextNode() throws Exception {
        String xml1 = "<root><a>a</a></root>";
        String xml2 = "<root><a></a></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for deleting a text node", "<delta><Deleted oldpos=\"0:0:0:0\" pos=\"0:0:0:0\">a</Deleted></delta>", outputter.outputString(diff));
    }
    public void testDeleteTextNode2() throws Exception {
        String xml1 = "<root><a>a</a></root>";
        String xml2 = "<root><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for deleting a text node", "<delta><Deleted oldpos=\"0:0:0:0\" pos=\"0:0:0:0\">a</Deleted></delta>", outputter.outputString(diff));
    }

    //
    // comments
    //

    public void testInsertComment() throws Exception {
        String xml1 = "<root><a/></root>";
        String xml2 = "<root><!-- added a comment --><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for inserting a comment", "<delta><Inserted pos=\"0:0:0\"><!-- added a comment --></Inserted></delta>", outputter.outputString(diff));
    }

    public void testDeleteComment() throws Exception {
        String xml1 = "<root><!-- remove a comment --><a/></root>";
        String xml2 = "<root><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for deleting a comment", "<delta><Deleted oldpos=\"0:0:0\" pos=\"0:0:0\"><!-- remove a comment --></Deleted></delta>", outputter.outputString(diff));
    }

    public void testUpdateComment() throws Exception {
        String xml1 = "<root><!-- update a comment --><a/></root>";
        String xml2 = "<root><!-- updated! --><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for updating a comment", "<delta><Deleted oldpos=\"0:0:0\" pos=\"0:0:0\"><!-- update a comment --></Deleted><Inserted pos=\"0:0:0\"><!-- updated! --></Inserted></delta>", outputter.outputString(diff));

    }

    public void testDoCommentsImpactElementPositionNumbers() throws Exception {
        String xml1 = "<root><!-- comment --></root>";
        String xml2 = "<root><!-- comment --><a/></root>";
        Element xml1e = Xml.loadString(xml1, false);
        Element xml2e = Xml.loadString(xml2, false);
        Element diff = MetadataDifference.diff(xml1e, xml2e);

        assertEquals("Unexpected difference for inserting a node after a comment", "<delta><Inserted pos=\"0:0:1\"><a /></Inserted></delta>", outputter.outputString(diff));

    }

    public void testRealWorldMetadataDiff() throws Exception {

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

            Log.debug(Geonet.DIFF, "Diff took " + (end - start) + " ms");
        }

        assertEquals("Unexpected difference for inserting a node after a comment", "<delta><Deleted oldpos=\"0:0:27:1:3:1:1:1:3:1:0\" update=\"yes\" pos=\"0:0:27:1:3:1:1:1:3:1:0\">100 procent.</Deleted><Inserted update=\"yes\" pos=\"0:0:27:1:3:1:1:1:3:1:0\">100 %.</Inserted></delta>", outputter.outputString(diff));
    }

}