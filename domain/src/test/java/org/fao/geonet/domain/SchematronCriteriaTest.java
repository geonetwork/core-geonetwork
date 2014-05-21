package org.fao.geonet.domain;

import static org.junit.Assert.*;
import org.fao.geonet.repository.SchematronCriteriaGroupRepositoryTest;
import org.fao.geonet.repository.SchematronCriteriaRepositoryTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test schematron criteria class
 * Created by Jesse on 2/28/14.
 */
public class SchematronCriteriaTest {

    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testAsXml() throws Exception {
        final SchematronCriteria criteria = SchematronCriteriaGroupRepositoryTest.newSchematronCriteria(_inc);
        criteria.setValue("");
        final Element xml = criteria.asXml();
        assertEquals("''", xml.getChildText("value"));
    }

    @Test
    public void testAsNullUiPropertiesDefaultToNormalValues() throws Exception {
        final SchematronCriteria criteria = SchematronCriteriaGroupRepositoryTest.newSchematronCriteria(_inc);
        criteria.setUiType(null);
        criteria.setUiValue(null);
        final Element xml = criteria.asXml();
        assertEquals(criteria.getType().toString(), xml.getChildText(SchematronCriteria.EL_UI_TYPE));
        assertEquals(criteria.getValue(), xml.getChildText(SchematronCriteria.EL_UI_VALUE));
    }

    @Test
    public void testCopy() throws Exception {
        final SchematronCriteria criteria = SchematronCriteriaGroupRepositoryTest.newSchematronCriteria(_inc);
        final SchematronCriteria copy = criteria.copy();

        Element expectedXml = criteria.asXml();
        expectedXml.removeChild("id");

        Element actualXml = copy.asXml();
        actualXml.removeChild("id");
        assertEquals(Xml.getString(expectedXml), Xml.getString(actualXml));

    }
}
