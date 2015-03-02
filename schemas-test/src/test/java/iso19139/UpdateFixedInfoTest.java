package iso19139;

import com.google.common.base.Optional;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GCO;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.GMD;
import static org.fao.geonet.schema.iso19139.ISO19139Namespaces.SRV;
import static org.junit.Assert.assertEquals;

/**
 * @author Jesse on 3/2/2015.
 */
public class UpdateFixedInfoTest extends AbstractCoreIntegrationTest {
    private static final List<Namespace> NAMESPACES = Arrays.asList(SRV, GCO, GMD);
    @Autowired
    private DataManager dataManager;
    @Test
    public void testDescriptiveKeywords() throws Exception {
        Element md = Xml.loadFile(UpdateFixedInfoTest.class.getResource("descriptiveKeywordsUpdateFixedInfo.xml"));
        ServiceContext context = createServiceContext();
        final Element updatedXml = dataManager.updateFixedInfo("iso19139", Optional.<Integer>absent(), "test-uuid-123", md, null,
                UpdateDatestamp.NO, context);

        assertEquals(4, Xml.selectNodes(updatedXml, "*//gmd:descriptiveKeywords", NAMESPACES).size());
        assertEquals(6, Xml.selectNodes(updatedXml, "*//gmd:descriptiveKeywords//gmd:keyword", NAMESPACES).size());
        assertEquals(1, Xml.selectNodes(updatedXml, "*//gmd:descriptiveKeywords[1]//gmd:keyword", NAMESPACES).size());
        assertEquals(3, Xml.selectNodes(updatedXml, "*//gmd:descriptiveKeywords[2]//gmd:keyword", NAMESPACES).size());
        assertEquals(2, Xml.selectNodes(updatedXml, "*//gmd:descriptiveKeywords[3]//gmd:keyword", NAMESPACES).size());
        assertEquals(0, Xml.selectNodes(updatedXml, "*//gmd:descriptiveKeywords[4]//gmd:keyword", NAMESPACES).size());

        assertEqualsText("geonetwork.thesaurus.external.place.regions", updatedXml, "*//gmd:descriptiveKeywords[1]//gmd:thesaurusName//gmx:Anchor", GCO, GMD);

        assertEqualsText("Africa", updatedXml, "*//gmd:descriptiveKeywords[1]//gmd:keyword[1]/gco:CharacterString", GCO, GMD);
        assertEqualsText("water", updatedXml, "*//gmd:descriptiveKeywords[2]//gmd:keyword[1]/gco:CharacterString", GCO, GMD);
        assertEqualsText("agriculture", updatedXml, "*//gmd:descriptiveKeywords[2]//gmd:keyword[2]/gco:CharacterString", GCO, GMD);
        assertEqualsText("abc", updatedXml, "*//gmd:descriptiveKeywords[2]//gmd:keyword[3]/gco:CharacterString", GCO, GMD);
        assertEqualsText("geonetwork.thesaurus.external.theme.gemet-theme", updatedXml, "*//gmd:descriptiveKeywords[2]//gmd:thesaurusName//gmx:Anchor", GCO, GMD);

        assertEqualsText("Geodata", updatedXml, "*//gmd:descriptiveKeywords[3]//gmd:keyword[1]//gmd:LocalisedCharacterString[@locale = '#EN']", GCO, GMD);
        assertEqualsText("zxcen", updatedXml, "*//gmd:descriptiveKeywords[3]//gmd:keyword[2]//gmd:LocalisedCharacterString[@locale = '#EN']", GCO, GMD);
        assertEqualsText("geonetwork.thesaurus.local._none_.geocat.ch", updatedXml, "*//gmd:descriptiveKeywords[3]//gmd:thesaurusName//gmx:Anchor", GCO, GMD);

    }
}
