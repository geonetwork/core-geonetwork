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

package iso19139;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.fao.geonet.constants.Geonet.Namespaces.GMX;
import static org.fao.geonet.constants.Geonet.Namespaces.XLINK;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.SRV;
import static org.junit.Assert.assertEquals;

/**
 * @author Jesse on 3/2/2015.
 */
public class UpdateFixedInfoTest extends AbstractServiceIntegrationTest {
    private static final List<Namespace> NAMESPACES = Arrays.asList(SRV, GCO, GMD, GMX);
    @Autowired
    private DataManager dataManager;

    @Test
    public void testDescriptiveKeywords() throws Exception {
        Element md = Xml.loadFile(UpdateFixedInfoTest.class.getResource("descriptiveKeywordsUpdateFixedInfo.xml"));
        ServiceContext context = createServiceContext();
        final Element updatedXml = dataManager.updateFixedInfo("iso19139", Optional.<Integer>absent(), "test-uuid-123", md, null,
            UpdateDatestamp.NO, context);

        assertEquals(4, Xml.selectNodes(updatedXml,
            "*//gmd:descriptiveKeywords", NAMESPACES).size());
        assertEquals(1, Xml.selectNodes(updatedXml,
            "*//gmd:descriptiveKeywords[normalize-space(.//gmd:thesaurusName//gmd:code/*) = 'geonetwork.thesaurus.local._none_.geocat.ch']//gmd:keyword", NAMESPACES).size());
        assertEquals(3, Xml.selectNodes(updatedXml,
            "*//gmd:descriptiveKeywords[normalize-space(.//gmd:thesaurusName//gmd:code/*) = 'geonetwork.thesaurus.external.theme.gemet-theme']//gmd:keyword", NAMESPACES).size());
        assertEquals(1, Xml.selectNodes(updatedXml,
            "*//gmd:descriptiveKeywords[normalize-space(.//gmd:thesaurusName//gmd:code/*) = 'geonetwork.thesaurus.external.place.regions']//gmd:keyword", NAMESPACES).size());

        // Ordering of existing is preserved
        assertEqualsText("geonetwork.thesaurus.external.place.regions", updatedXml,
            "*//gmd:descriptiveKeywords[1]//gmd:thesaurusName//gmx:Anchor", GCO, GMD);
        assertEqualsText("geonetwork.thesaurus.external.theme.gemet-theme", updatedXml,
            "*//gmd:descriptiveKeywords[2]//gmd:thesaurusName//gmx:Anchor", GCO, GMD);


        assertEqualsText("Africa", updatedXml,
            "*//gmd:descriptiveKeywords[1]//gmd:keyword[1]/gco:CharacterString", GCO, GMD);
        assertEqualsText("noise", updatedXml,
            "*//gmd:descriptiveKeywords[2]//gmd:keyword[1]/gco:CharacterString", GCO, GMD);
        assertEqualsText("water", updatedXml,
            "*//gmd:descriptiveKeywords[2]//gmd:keyword[2]/gco:CharacterString", GCO, GMD);
        assertEqualsText("agriculture", updatedXml,
            "*//gmd:descriptiveKeywords[2]//gmd:keyword[3]/gco:CharacterString", GCO, GMD);


        assertEquals(1, Xml.selectNodes(updatedXml,
            "*//gmd:descriptiveKeywords[3]/@xlink:href", NAMESPACES).size());

        assertEqualsText("Geodata", updatedXml,
            "*//gmd:descriptiveKeywords[4]//gmd:keyword[1]//gmd:LocalisedCharacterString[@locale = '#EN']", GCO, GMD);
        assertEqualsText("Geodaten", updatedXml,
            "*//gmd:descriptiveKeywords[4]//gmd:keyword[1]//gmd:LocalisedCharacterString[@locale = '#DE']", GCO, GMD);
    }

    @Test
    public void testXLinkedDescriptiveKeywords() throws Exception {
        Element md = Xml.loadFile(UpdateFixedInfoTest.class.getResource("descriptiveKeywordsUpdateFixedInfoXLinked.xml"));
        ServiceContext context = createServiceContext();
        final Element updatedXml = dataManager.updateFixedInfo("iso19139", Optional.<Integer>absent(), "test-uuid-123", md, null,
            UpdateDatestamp.NO, context);

        assertEquals(Xml.getString(updatedXml), 4, Xml.selectNodes(updatedXml,
            "*//gmd:descriptiveKeywords", NAMESPACES).size());

        // One keyword not xlinked is preserved
        assertEquals(Xml.getString(updatedXml), 1, Xml.selectNodes(updatedXml,
            "*//gmd:descriptiveKeywords//gmd:keyword", NAMESPACES).size());

        // Xlinked descKeyword contains no keyword
        assertEquals(Xml.getString(updatedXml), 0, Xml.selectNodes(updatedXml,
            "*//gmd:descriptiveKeywords[1]//gmd:keyword", NAMESPACES).size());
        assertEquals(Xml.getString(updatedXml), 0, Xml.selectNodes(updatedXml,
            "*//gmd:descriptiveKeywords[3]//gmd:keyword", NAMESPACES).size());
        assertEquals(Xml.getString(updatedXml), 0, Xml.selectNodes(updatedXml,
            "*//gmd:descriptiveKeywords[4]//gmd:keyword", NAMESPACES).size());

        // and order is preserved
        assertEquals(Xml.getString(updatedXml), 1, Xml.selectNodes(updatedXml, "" +
            "*//gmd:descriptiveKeywords[2]//gmd:keyword", NAMESPACES).size());

        assertEqualsText(
            "local://api/registries/vocabularies/keyword?thesaurus=external.theme.httpinspireeceuropaeutheme-theme&id=http://rdfdata.eionet.europa.eu/inspirethemes/themes/5&multiple=false&lang=ger,fre,eng,ita&textgroupOnly",
            updatedXml,
            "*//gmd:descriptiveKeywords[1]/@xlink:href", XLINK, GCO, GMD);
        assertEqualsText("local://api/registries/vocabularies/keyword?skipdescriptivekeywords=true&thesaurus=local._none_.geocat.ch&id=http://geocat.ch/concept#1,http://geocat.ch/concept#20&multiple=true&lang=ger,fre,eng,ita&textgroupOnly=",
            updatedXml,
            "*//gmd:descriptiveKeywords[3]/@xlink:href", XLINK, GCO, GMD);
        assertEqualsText("local://api/registries/vocabularies/keyword?skipdescriptivekeywords=true&thesaurus=external.theme.inspire-service-taxonomy&id=urn:inspire:service:taxonomy:comGeographicCompressionService&multiple=true&lang=ger,fre,eng,ita&textgroupOnly=",
            updatedXml, "*//gmd:descriptiveKeywords[4]/@xlink:href", XLINK, GCO, GMD);
        assertEqualsText("Africa", updatedXml,
            "*//gmd:descriptiveKeywords[2]//gmd:keyword[1]/gco:CharacterString", GCO, GMD);
    }
}
