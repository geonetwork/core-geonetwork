package org.fao.geonet;

import jeeves.xlink.XLink;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test that metadata can be correctly validated against their schema.
 *
 * @author Jesse on 11/13/2014.
 */
public class XmlValidationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private DataManager manager;

    @Test
    public void testBasicValidation() throws Exception {
        Element metadata = Xml.loadFile(XmlValidationTest.class.getResource("kernel/valid-metadata.iso19139.xml"));
        removeHrefs(metadata);
        assertTrue(manager.validate(metadata));
        metadata = Xml.loadFile(XmlValidationTest.class.getResource("kernel/search/DE_Search_MD.iso19139.xml"));
        assertFalse(manager.validate(metadata));
    }

    protected void removeHrefs(Element metadata) {
        final Iterator descendants = metadata.getDescendants();
        while (descendants.hasNext()) {
            Object next = descendants.next();
            if (next instanceof Element) {
                Element element = (Element) next;
                element.removeAttribute(XLink.HREF, XLink.NAMESPACE_XLINK);
            }
        }
    }
}
