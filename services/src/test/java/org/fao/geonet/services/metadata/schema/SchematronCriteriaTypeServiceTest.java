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

package org.fao.geonet.services.metadata.schema;

import jeeves.server.context.ServiceContext;

import net.arnx.jsonic.JSON;

import org.fao.geonet.domain.Schematron;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.io.File;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.fao.geonet.domain.SchematronCriteriaType.ALWAYS_ACCEPT;
import static org.fao.geonet.domain.SchematronCriteriaType.GROUP;
import static org.fao.geonet.domain.SchematronCriteriaType.XPATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for the json service.
 *
 * Created by Jesse on 2/14/14.
 */
public class SchematronCriteriaTypeServiceTest extends AbstractServiceIntegrationTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testExec() throws Exception {
        addSchematron("iso19139");
        addSchematron("dublin-core");

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams();

        final SchematronCriteriaTypeService service = new SchematronCriteriaTypeService();
        Element results = service.exec(params, context);
        Element schemas = results.getChild("schemas");
        Element requirements = results.getChild("requirements");

        assertEquals(SchematronRequirement.values().length, requirements.getChildren().size());

        String json = Xml.getJSON(schemas);
        new JSON().parse(json);
        // no exception ??? good

        assertEqualsText("iso19139", schemas, "iso19139/name");
        String resultAsString = Xml.getString(schemas);

        // the number 4 can change depending on the definition of the iso19139 criteria definition.
        // It should normally be [the number of criteria in criteria-type.xml] + 2(ALL ways true and XPATH)
        assertEquals(resultAsString, 5, Xml.selectNumber(schemas, "count(iso19139/criteriaTypes/type)").intValue());
        assertEqualsText(XPATH.toString(), schemas, "iso19139/criteriaTypes/type[1]/type");
        assertEqualsText("Keyword", schemas, "iso19139/criteriaTypes/type[1]/name");
        assertEquals(resultAsString, 1, Xml.selectNumber(schemas, "count(iso19139/criteriaTypes/type[type = '" +
            ALWAYS_ACCEPT.name() + "'])").intValue());
        assertEquals(resultAsString, 0, Xml.selectNumber(schemas, "count(iso19139/criteriaTypes/type[type = '" +
            ALWAYS_ACCEPT.name() + "']/value)").intValue());
        assertTrue(resultAsString, 0 < Xml.selectNumber(schemas, "count(iso19139/criteriaTypes/type[type = '" +
            GROUP.name() + "'])").intValue());
        assertEquals(resultAsString, 1, Xml.selectNumber(schemas, "count(iso19139/criteriaTypes/type[upper-case(name) = '" +
            XPATH.name() + "'])").intValue());

        assertTrue(resultAsString, 0 < Xml.selectNumber(schemas, "count(iso19139/schematron)").intValue());
        assertEquals(resultAsString, 1, Xml.selectNumber(schemas, "count(iso19139/schematron[1]/file)").intValue());
        assertEquals(resultAsString, 1, Xml.selectNumber(schemas, "count(iso19139/schematron[1]/id)").intValue());
        assertEquals(resultAsString,
            Xml.selectNumber(schemas, "count(iso19139/schematron)").intValue(),
            Xml.selectNumber(schemas, "count(iso19139/schematron/title)").intValue());
        assertEquals(resultAsString, 1, Xml.selectNumber(schemas, "count(iso19139/schematron[1]/rulename)").intValue());
        assertTrue(resultAsString, 0 < Xml.selectNumber(schemas, "count(iso19139/schematron[1]/label)").intValue());
        assertEquals(resultAsString, 1, Xml.selectNumber(schemas, "count(iso19139/schematron[1]/schemaname)").intValue());
        assertEquals(resultAsString, 1, Xml.selectNumber(schemas, "count(iso19139/schematron[1]/groupCount)").intValue());

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        final List<Element> types = (List<Element>) Xml.selectNodes(schemas, "iso19139/criteriaTypes/type");
        for (Element type : types) {
            final Element typeahead = type.getChild("typeahead");
            if (typeahead != null) {
                final Element remote = typeahead.getChild("remote");
                if (remote != null) {
                    checkForJSFunction(engine, remote, "selectRecordArray");
                    checkForJSFunction(engine, remote, "selectLabelFunction");
                    if (typeahead.getChild("selectTokensFunction") != null) {
                        checkForJSFunction(engine, remote, "selectTokensFunction");
                    }
                    checkForJSFunction(engine, remote, "selectValueFunction");
                } else {
                    final Element values = typeahead.getChild("local");
                    assertNotNull(values);

                    assertTrue(values.getChildren().size() > 0);

                    for (Object o : values.getChildren()) {
                        Element element = (Element) o;
                        assertNotNull(element.getChild("value"));
                        final Element tokens = element.getChild("tokens");
                        assertNotNull(tokens);
                    }
                }
            }
        }
    }

    private void checkForJSFunction(ScriptEngine engine, Element remote, String functionName) throws ScriptException {
        final Element typeaheadEl = remote.getParentElement();
        final Element typeEl = typeaheadEl.getParentElement();
        final Element schemaEl = typeEl.getParentElement().getParentElement();
        String name = schemaEl.getName() + "/" + typeEl.getChild("name").getText();
        final Element function = remote.getChild(functionName);
        assertNotNull(name + " criteria-types missing element: " + functionName, function);
        try {
            engine.eval("var x = function " + function.getText()); // check that no exception is thrown:
        } catch (ScriptException e) {
            throw new AssertionError(functionName + " in " + name + " does not compile due to: '" + e.getMessage() + "'\n\n" + function.getText());
        }
    }

    private void addSchematron(String schemaName) {
        final SchemaManager schemaManager = _applicationContext.getBean(SchemaManager.class);
        final SchematronRepository repo = _applicationContext.getBean(SchematronRepository.class);
        Schematron schematron = new Schematron();
        schematron.setSchemaName(schemaName);
        schematron.setFile(schemaManager.getSchemaDir(schemaName) + File.separator + "schematron" + File.separator
            + "schematron-rules-geonetwork.sch");
        repo.save(schematron);
    }
}
