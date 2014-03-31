package org.fao.geonet.domain;

import org.jdom.Element;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test schematron criteria
 * Created by Jesse on 2/7/14.
 */
public class SchematronCriteriaGroupTest {

    @Test
    public void testSetNullSchematron() throws Exception {
        final SchematronCriteriaGroup schematronCriteriaGroup = new SchematronCriteriaGroup();
        schematronCriteriaGroup.setSchematron(null);
        // no error... good.

        final Schematron schematron = new Schematron();
        schematron.setId(123);

        schematronCriteriaGroup.setSchematron(schematron);

        assertEquals(schematron.getId(), schematronCriteriaGroup.getId().getSchematronId());

        schematronCriteriaGroup.setSchematron(null);
        assertEquals(schematron.getId(), schematronCriteriaGroup.getId().getSchematronId());
    }

    @Test
    public void testAsXml() throws Exception {
        Schematron schematron = new Schematron();
        schematron.setFile("file");
        schematron.setId(1);
        schematron.setSchemaName("schemaname");

        SchematronCriteria criteria = new SchematronCriteria();
        criteria.setType(SchematronCriteriaType.ALWAYS_ACCEPT);
        criteria.setValue("value");
        criteria.setId(2);

        SchematronCriteriaGroup group = new SchematronCriteriaGroup();
        group.setId(new SchematronCriteriaGroupId("Name", schematron))
                .setRequirement(SchematronRequirement.REQUIRED)
                .addCriteria(criteria);
        group.setSchematron(schematron);

        Element xml = group.asXml();

        assertNotNull(xml);

        Element criterialist = xml.getChild("criteria");
        assertNotNull(criterialist);
        assertEquals(1, criterialist.getContentSize());
        Element criteriaEl = criterialist.getChild("criteria");
        assertEquals(""+criteria.getId(), criteriaEl.getChildText("id"));
        assertEquals(criteria.getType().name(), criteriaEl.getChildText("type"));
        assertEquals(criteria.getValue(), criteriaEl.getChildText("value"));

        assertEquals(group.getId().getName(), xml.getChild("id").getChildText("name"));
        assertEquals(group.getId().getSchematronId(), Integer.parseInt(xml.getChild("id").getChildText("schematronid")));
        assertEquals(group.getRequirement().name(), xml.getChildText("requirement"));

        Element schematronEl = xml.getChild("schematron");

        assertEquals(""+schematron.getId(), schematronEl.getChildText("id"));
        assertEquals(""+schematron.getFile(), schematronEl.getChildText("file"));
        assertEquals(""+schematron.getSchemaName(), schematronEl.getChildText("schemaname"));
    }
}
