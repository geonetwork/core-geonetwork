/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.schema;

import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.fao.geonet.constants.Geonet.Namespaces.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test some of the rules in the schematron-rules-iso.sch files in the iso19139 schema plugin.
 * <p>
 * Created by Jesse on 3/25/14.
 */
public class SchematronRulesIsoTest extends AbstractSchematronTest {


    protected Path schematronXsl;
    protected Element schematron;

    @Before
    public void before() {
        super.before();
        Pair<Element, Path> compiledResult = compileSchematron(getSchematronFile("iso19139", "schematron-rules-iso.sch"));
        schematron = compiledResult.one();
        schematronXsl = compiledResult.two();
    }


    protected Path getSchematronXsl() {
        return schematronXsl;
    }

    @Test
    public void testPasses() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        Element results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(0, countFailures(results));
    }

    @Test
    public void testMissingAbstract() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element charStringEl = testMetadata.getChild("identificationInfo", GMD)
            .getChild("MD_DataIdentification", GMD)
            .getChild("abstract", GMD);
        testEmptyStringErrors(testMetadata, charStringEl);
    }

    @Test
    public void testMissingTitle() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element charStringEl = testMetadata.getChild("identificationInfo", GMD)
            .getChild("MD_DataIdentification", GMD)
            .getChild("citation", GMD)
            .getChild("CI_Citation", GMD)
            .getChild("title", GMD);
        testEmptyStringErrors(testMetadata, charStringEl);
    }

    @Test
    public void testMissingCitation() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element citationEl = testMetadata.getChild("identificationInfo", GMD)
            .getChild("MD_DataIdentification", GMD)
            .getChild("citation", GMD)
            .getChild("CI_Citation", GMD);

        citationEl.detach();

        Element results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(1, countFailures(results));

        testMetadata.getChild("identificationInfo", GMD)
            .getChild("MD_DataIdentification", GMD)
            .getChild("citation", GMD).setAttribute("nilReason", "missing", GCO);

        results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(0, countFailures(results));

    }

    @Test
    public void testMissingContact() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element contact = testMetadata.getChild("contact", GMD);
        testNoStringErrors(testMetadata, contact);
    }

    @Test
    public void testMissingLanguage() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element contact = testMetadata.getChild("language", GMD);
        testNoStringErrors(testMetadata, contact);
    }

    @Test
    public void testMissingDataIdentificationLanguage() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element element = testMetadata.getChild("identificationInfo", GMD)
            .getChild("MD_DataIdentification", GMD)
            .getChild("language", GMD);
        testNoStringErrors(testMetadata, element);
    }

    @Test
    public void testMissingDataIdentificationCitationDate() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element dateEl = Xml.selectElement(testMetadata, "gmd:identificationInfo/*/gmd:citation/*/gmd:date/*/gmd:date",
            Arrays.asList(GMD, GCO));
        testNoStringErrors(testMetadata, dateEl);
    }

    @Test
    public void testMissingDataIdentificationCitationDateType() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element dateTypeEl = Xml.selectElement(testMetadata, "gmd:identificationInfo/*/gmd:citation/*/gmd:date/*/gmd:dateType",
            Arrays.asList(GMD, GCO));
        testNoStringErrors(testMetadata, dateTypeEl);
    }

    @Test
    public void testAggregationInfoCombinations() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element mdDataIdentification = Xml.selectElement(testMetadata, "gmd:identificationInfo/gmd:MD_DataIdentification", Collections.singletonList(GMD));
        assertNotNull(mdDataIdentification);

        Element results = Xml.transform(testMetadata, getSchematronXsl(), params);
        int errorsStart = countFailures(results);

        // first we test cases that should not introduce failures
        addDatasetIdentifier(mdDataIdentification, "uuid-1", null, "largerWorkCitation", null);
        assertEquals(errorsStart, countFailures(Xml.transform(testMetadata, getSchematronXsl(), params)));
        addDatasetIdentifier(mdDataIdentification, "uuid-2", null, "largerWorkCitation", null);
        assertEquals(errorsStart, countFailures(Xml.transform(testMetadata, getSchematronXsl(), params)));
        addDatasetIdentifier(mdDataIdentification, "uuid-2", null, "crossReference", null);
        assertEquals(errorsStart, countFailures(Xml.transform(testMetadata, getSchematronXsl(), params)));
        addDatasetIdentifier(mdDataIdentification, "uuid-3", null, "crossReference", "campaign");
        assertEquals(errorsStart, countFailures(Xml.transform(testMetadata, getSchematronXsl(), params)));
        addDatasetIdentifier(mdDataIdentification, "uuid-3", null, "crossReference", "collection");
        assertEquals(errorsStart, countFailures(Xml.transform(testMetadata, getSchematronXsl(), params)));
        addDatasetIdentifier(mdDataIdentification, "uuid-4", "href-4", "crossReference", "collection");
        assertEquals(errorsStart, countFailures(Xml.transform(testMetadata, getSchematronXsl(), params)));

        // adding duplicate uuid+initiativeType+associationType combinations should fail
        Element bad = addDatasetIdentifier(mdDataIdentification, "uuid-1", null, "largerWorkCitation", null);
        assertEquals(errorsStart + 2, countFailures(Xml.transform(testMetadata, getSchematronXsl(), params)));
        bad.detach();
        bad = addDatasetIdentifier(mdDataIdentification, "uuid-3", null, "crossReference", "campaign");
        assertEquals(errorsStart + 2, countFailures(Xml.transform(testMetadata, getSchematronXsl(), params)));
        bad.detach();
        bad = addDatasetIdentifier(mdDataIdentification, "uuid-1", "href-1", "largerWorkCitation", null);
        assertEquals(errorsStart + 2, countFailures(Xml.transform(testMetadata, getSchematronXsl(), params)));
        bad.detach();
        bad = addDatasetIdentifier(mdDataIdentification, "uuid-4", "href-4", "crossReference", "collection");
        assertEquals(errorsStart + 2, countFailures(Xml.transform(testMetadata, getSchematronXsl(), params)));
        bad.detach();
    }

    private static Element addDatasetIdentifier(Element parent, String uuid, String href, String associationType, String initiativeType) {
        Element ai = new Element("aggregationInfo", GMD);
        Element mdAi = new Element("MD_AggregateInformation", GMD);
        Element adsi = new Element("aggregateDataSetIdentifier", GMD);
        Element mdIdentifier = new Element("MD_Identifier", GMD);
        Element code = new Element("code", GMD);

        Element at = new Element("associationType", GMD);
        Element dsAt = new Element("DS_AssociationTypeCode", GMD);
        dsAt.setAttribute("codeListValue", associationType);
        dsAt.setAttribute("codeList", "http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#DS_AssociationTypeCode");

        at.addContent(dsAt);
        ai.addContent(mdAi);
        mdAi.addContent(adsi);
        mdAi.addContent(at);
        adsi.addContent(mdIdentifier);
        mdIdentifier.addContent(code);

        if (initiativeType != null) {
            Element it = new Element("initiativeType", GMD);
            Element dsIt = new Element("DS_InitiativeTypeCode", GMD);
            dsIt.setAttribute("codeListValue", initiativeType);
            dsIt.setAttribute("codeList", "http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#DS_InitiativeTypeCode");
            it.addContent(dsIt);
            mdAi.addContent(it);
        }

        if (href != null) {
            Element anchor = new Element("Anchor", GMX);
            anchor.setAttribute("href", href, XLINK);
            anchor.setText(uuid);
            code.addContent(anchor);
        } else {
            Element cs = new Element("CharacterString", GCO);
            cs.setText(uuid);
            code.addContent(cs);
        }

        parent.addContent(ai);

        return ai;
    }

    private void testNoStringErrors(Element testMetadata, Element contact) throws Exception {
        contact.setContent(Collections.emptyList());

        Element results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(1, countFailures(results));

        contact.setAttribute("nilReason", "missing", GCO);
        results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(0, countFailures(results));
    }

    private void testEmptyStringErrors(Element testMetadata, Element charStringEl) throws Exception {
        charStringEl.setContent(Arrays.asList());

        Element results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(1, countFailures(results));

        charStringEl.addContent(new Element("CharacterString", GCO).setText(""));

        results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(1, countFailures(results));

        charStringEl.addContent(
            new Element("PT_FreeText", GMD).addContent(
                new Element("textGroup", GMD).addContent(
                    new Element("LocalisedCharacterString", GMD).setAttribute("locale", "#DE").setText(" ")
                )));


        results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(1, countFailures(results));

        charStringEl.setAttribute("nilReason", "missing", GCO);
        results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(1, countFailures(results));

        charStringEl.setContent(Arrays.asList());
        results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(0, countFailures(results));

    }
}
