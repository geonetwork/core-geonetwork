package org.fao.geonet.services.metadata.inspire;

import com.google.common.collect.Maps;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.apache.jcs.access.exception.CacheException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.AjaxEditUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.Map;

/**
 * @author Jesse on 5/21/2014.
 */
class SaveServiceTestImpl extends Save {
    final static IsoLanguagesMapper LANGUAGES_MAPPER = new IsoLanguagesMapper() {
        {
            iso639_1_to_iso639_2IsoLanguagesMap.put("en", "eng");
            iso639_1_to_iso639_2IsoLanguagesMap.put("de", "ger");
            iso639_1_to_iso639_2IsoLanguagesMap.put("ge", "ger");
            iso639_1_to_iso639_2IsoLanguagesMap.put("fr", "fre");
            iso639_1_to_iso639_2IsoLanguagesMap.put("it", "ita");
            iso639_1_to_iso639_2IsoLanguagesMap.put("rm", "roh");
        }

        {
            iso639_2_to_iso639_1IsoLanguagesMap.put("eng", "en");
            iso639_2_to_iso639_1IsoLanguagesMap.put("ger", "de");
            iso639_2_to_iso639_1IsoLanguagesMap.put("fre", "fr");
            iso639_2_to_iso639_1IsoLanguagesMap.put("ita", "it");
            iso639_2_to_iso639_1IsoLanguagesMap.put("roh", "rm");
        }
    };
    private Element testMetadata;
    Map<String, Element> sharedObjects = Maps.newHashMap();
    private boolean saved = false;
    private Element savedMetadata;
    private boolean enumerateOnGetMetadata = true;

    public SaveServiceTestImpl(Element testMetadata) throws IOException, JDOMException {
        this.testMetadata = testMetadata;
        Element contact1 = createContact("name", "lastname", "email", "role", Pair.read("eng", "en OrgName"), Pair.read("fre",
                "fr OrgName"), Pair.read("ger", "de OrgName"));
        sharedObjects.put("local://xml.user.get?id=1&amp;schema=iso19139.che&amp;role=owner", contact1);
        Element contact2 = createContact("Jesse", "Eichar", "jesse.eichar@camptocamp.com", "pointOfContact",
                Pair.read("eng", "Camptocamp SA"), Pair.read("ger", "Camptocamp AG"));
        sharedObjects.put("local://xml.user.get?id=2&amp;schema=iso19139.che&amp;role=pointOfContact", contact2);

        Element buildingsKeyword = createKeyword("eng", "Building", "external.theme.inspire-theme");
        sharedObjects.put("local://che.keyword.get?thesaurus=external.theme.inspire-theme&amp;id=http%3A%2F%2Frdfdata.eionet.europa" +
                          ".eu%2Finspirethemes%2Fthemes%2F15&amp;locales=fr,en,de,it", buildingsKeyword);

        Element hydrographyKeyword = createKeyword("eng", "Hydrography", "external.theme.inspire-theme");
        sharedObjects.put("local://che.keyword.get?thesaurus=external.theme.inspire-theme&amp;id=http%3A%2F%2Frdfdata.eionet.europa" +
                    ".eu%2Finspirethemes%2Fthemes%2F9&amp;locales=fr,en,de,it", hydrographyKeyword);
        Element nonThemeKeyword = createKeyword("eng", "otherWord", "external._none_.gemet");
        sharedObjects.put("local://che.keyword.get?thesaurus=external._none_.gemet&amp;id=http%3A%2F%2Frdfdata.eionet.europa.eu%2F" +
                          "inspirethemes%2Fthemes%2F9&amp;locales=fr,en,de,it", nonThemeKeyword);

        Element bernExtent = createExtent("Bern", "Bern");
        sharedObjects.put("local://xml.extent.get?id=2&amp;wfs=default&amp;typename=gn:kantoneBB&amp;format=gmd_complete&amp;extentTypeCode=true", bernExtent);
        Element fribourgExtent = createExtent("Fribourg", "Fribourg");
        sharedObjects.put("local://xml.extent.get?id=2196&amp;wfs=default&amp;typename=gn:gemeindenBB&amp;format=gmd_complete&amp;extentTypeCode=true", fribourgExtent);
    }

    static Element createExtent(String description, String id) throws IOException, JDOMException {
        String xml = "<gmd:EX_Extent" +
                     "        xmlns:che=\"http://www.geocat.ch/2008/che\" " +
                     "        xmlns:srv=\"http://www.isotc211.org/2005/srv\" " +
                     "        xmlns:gco=\"http://www.isotc211.org/2005/gco\" " +
                     "        xmlns:gml=\"http://www.opengis.net/gml\" " +
                     "        xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" " +
                     "        xmlns:geonet=\"http://www.fao.org/geonetwork\" " +
                     ">\n"
                     + "    <gmd:description>\n"
                     + "        <gmd:PT_FreeText>\n"
                     + "            <gmd:textGroup>\n"
                     + "                <gmd:LocalisedCharacterString locale=\"#EN\">%s</gmd:LocalisedCharacterString>\n"
                     + "            </gmd:textGroup>\n"
                     + "        </gmd:PT_FreeText>\n"
                     + "    </gmd:description>\n"
                     + "    <gmd:geographicElement>\n"
                     + "        <gmd:EX_GeographicDescription>\n"
                     + "            <gmd:geographicIdentifier>\n"
                     + "                <gmd:MD_Identifier>\n"
                     + "                    <gmd:code xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"gmd:PT_FreeText_PropertyType\">\n"
                     + "                        <gmd:PT_FreeText>\n"
                     + "                            <gmd:textGroup>\n"
                     + "                                <gmd:LocalisedCharacterString locale=\"#EN\">%s</gmd:LocalisedCharacterString>\n"
                     + "                            </gmd:textGroup>\n"
                     + "                        </gmd:PT_FreeText>\n"
                     + "                    </gmd:code>\n"
                     + "                </gmd:MD_Identifier>\n"
                     + "            </gmd:geographicIdentifier>\n"
                     + "        </gmd:EX_GeographicDescription>\n"
                     + "    </gmd:geographicElement>\n"
                     + "    <gmd:geographicElement>\n"
                     + "        <gmd:EX_BoundingPolygon>\n"
                     + "            <gmd:extentTypeCode>\n"
                     + "                <gco:Boolean>1</gco:Boolean>\n"
                     + "            </gmd:extentTypeCode>\n"
                     + "            <gmd:polygon>\n"
                     + "                <gml:MultiSurface gml:id=\"Nbe2cb829e38c46d3875357c58010f345\">\n"
                     + "                    <gml:surfaceMember>\n"
                     + "                        <gml:Polygon gml:id=\"Nbe2cb829e38c46d3875357c58010f345.1\">\n"
                     + "                            <gml:exterior>\n"
                     + "                                <gml:LinearRing>\n"
                     + "                                        <gml:posList>9.536 47.266 9.537 47.266 9.538 47.266 9.536 47" +
                     ".266</gml:posList>\n"
                     + "                                </gml:LinearRing>\n"
                     + "                            </gml:exterior>\n"
                     + "                        </gml:Polygon>\n"
                     + "                    </gml:surfaceMember>\n"
                     + "                </gml:MultiSurface>\n"
                     + "            </gmd:polygon>\n"
                     + "        </gmd:EX_BoundingPolygon>\n"
                     + "    </gmd:geographicElement>\n"
                     + "</gmd:EX_Extent>";
        return Xml.loadString(String.format(xml, description, id), false);
    }

    static Element createKeyword(String lang, String word, String thesaurus) throws IOException, JDOMException {
        StringBuilder builder = new StringBuilder();
        builder.append("<gmd:MD_Keywords \n").
                append("        xmlns:che=\"http://www.geocat.ch/2008/che\" ").
                append("        xmlns:srv=\"http://www.isotc211.org/2005/srv\" ").
                append("        xmlns:gco=\"http://www.isotc211.org/2005/gco\" ").
                append("        xmlns:gml=\"http://www.opengis.net/gml\" ").
                append("        xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" ").
                append("        xmlns:geonet=\"http://www.fao.org/geonetwork\" ").
                append("        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ").
                append(">\n").
                append("    <gmd:keyword xsi:type=\"gmd:PT_FreeText_PropertyType\">\n").
                append("        <gmd:PT_FreeText>\n").
                append("            <gmd:textGroup>\n").
                append("                <gmd:LocalisedCharacterString locale=\"#{{LANG}}\">{{WORD}}</gmd:LocalisedCharacterString>\n").
                append("            </gmd:textGroup>\n").
                append("        </gmd:PT_FreeText>\n").
                append("    </gmd:keyword>\n").
                append("    <gmd:type>\n").
                append("        <gmd:MD_KeywordTypeCode codeList=\"http://www.isotc211.org/2005/resources/codeList").
                append(".xml#MD_KeywordTypeCode\" codeListValue=\"theme\"/>\n").
                append("    </gmd:type>\n").
                append("    <gmd:thesaurusName>\n").
                append("        <gmd:CI_Citation>\n").
                append("            <gmd:title xsi:type=\"gmd:PT_FreeText_PropertyType\">\n").
                append("                <gco:CharacterString>{{THESAURUS}}</gco:CharacterString>\n").
                append("            </gmd:title>\n").
                append("        </gmd:CI_Citation>\n").
                append("    </gmd:thesaurusName>\n").
                append("</gmd:MD_Keywords>");
        return Xml.loadString(builder.toString().replace("{{LANG}}", lang).replace("{{WORD}}", word).replace("{{THESAURUS}}",
                thesaurus), false);
    }

    Element createContact(String name, String lastName, String email, String role, Pair<String,
            String>... orgNames) throws IOException, JDOMException {

        StringBuilder builder = new StringBuilder();

        builder.append("<che:CHE_CI_ResponsibleParty ").
                append("        xmlns:che=\"http://www.geocat.ch/2008/che\" ").
                append("        xmlns:srv=\"http://www.isotc211.org/2005/srv\" ").
                append("        xmlns:gco=\"http://www.isotc211.org/2005/gco\" ").
                append("        xmlns:gml=\"http://www.opengis.net/gml\" ").
                append("        xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" ").
                append("        xmlns:geonet=\"http://www.fao.org/geonetwork\" ").
                append("        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ").
                append("      gco:isoType=\"gmd:CI_ResponsibleParty\">\n").
                append("      <gmd:organisationName>\n").
                append("        <gmd:PT_FreeText>\n");

        for (Pair<String, String> orgName : orgNames) {
            builder.append("          <gmd:textGroup>").
                    append("            <gmd:LocalisedCharacterString locale=\"#").
                    append(getIsoLanguagesMapper().iso639_2_to_iso639_1(orgName.one()).toUpperCase()).
                    append("\">").
                    append(orgName.two()).
                    append("</gmd:LocalisedCharacterString>\n").
                    append("          </gmd:textGroup>\n");
        }
        builder.append("        </gmd:PT_FreeText>\n").
                append("      </gmd:organisationName>\n").
                append("      ").
                append("      <gmd:contactInfo>\n").
                append("        <gmd:CI_Contact>\n").
                append("          <gmd:address>\n").
                append("            <che:CHE_CI_Address ").
                append("gco:isoType=\"gmd:CI_Address\">\n").
                append("              <gmd:electronicMailAddress><gco:CharacterString>").append(email).append("</gco:CharacterString></gmd:electronicMailAddress>\n").
                append("            </che:CHE_CI_Address>\n").
                append("          </gmd:address>\n").
                append("        </gmd:CI_Contact>\n").
                append("      </gmd:contactInfo>\n").
                append("      <gmd:role>\n").
                append("        <gmd:CI_RoleCode codeListValue=\"").append(role).append("\" ").
                append("                   codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode\" />\n").
                append("      </gmd:role>\n").
                append("      <gco:individualFirstName xmlns:gco=\"http://www.geocat.ch/2008/che\">\n").
                append("        <gco:CharacterString xmlns:gco=\"http://www.isotc211.org/2005/gco\">").append(name).
                append("</gco:CharacterString>\n").
                append("      </gco:individualFirstName>\n").
                append("      <gco:individualLastName xmlns:gco=\"http://www.geocat.ch/2008/che\">\n").
                append("        <gco:CharacterString xmlns:gco=\"http://www.isotc211.org/2005/gco\">").append(lastName).
                append("</gco:CharacterString>\n").
                append("      </gco:individualLastName>\n").
                append("    </che:CHE_CI_ResponsibleParty>");

        return Xml.loadString(builder.toString(), false);
    }

    @Override
    protected Element resolveXlink(ServiceContext context, String xlinkHref) throws IOException, JDOMException, CacheException {
        if (this.sharedObjects.containsKey(xlinkHref)) {
            final Element element = this.sharedObjects.get(xlinkHref);
            if (element == null) {
                throw new RuntimeException("Intended error during resolve");
            }
            return (Element) element.clone();
        }
        throw new Error("Unexpected xlink: " + xlinkHref);
    }

    @Override
    protected Element getMetadata(ServiceContext context, EditLib lib, String id, AjaxEditUtils ajaxEditUtils) throws Exception {
        if (this.enumerateOnGetMetadata) {
            lib.removeEditingInfo(testMetadata);
            lib.enumerateTree(testMetadata);
        }
        return testMetadata;
    }

    @Override
    protected AjaxEditUtils getAjaxEditUtils(Element params, ServiceContext context) throws Exception {
        return null;
    }

    @Override
    protected Element getResponse(Element params, ServiceContext context, String id, EditLib editLib, Element metadata) throws Exception {
        return new Element("data").setText("{jsondata}");
    }

    @Override
    protected IsoLanguagesMapper getIsoLanguagesMapper() {
        return LANGUAGES_MAPPER;
    }

    @Override
    protected void saveMetadata(ServiceContext context, AjaxEditUtils ajaxEditUtils, String id, DataManager dataManager,
                                Element metadata, boolean finished, boolean commit) throws Exception {
        this.saved = true;
        this.savedMetadata = metadata;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setTestMetadata(Element metadata) {
        this.testMetadata = metadata;
    }

    public Element getSavedMetadata() {
        return this.savedMetadata;
    }

    public void addXLink(String xlinkHref, Element element) {
        this.sharedObjects.put(xlinkHref, element);
    }

    public void setEnumerateOnGetMetadata(boolean enumerateOnGetMetadata) {
        this.enumerateOnGetMetadata = enumerateOnGetMetadata;
    }

    public boolean isEnumerateOnGetMetadata() {
        return enumerateOnGetMetadata;
    }
}
