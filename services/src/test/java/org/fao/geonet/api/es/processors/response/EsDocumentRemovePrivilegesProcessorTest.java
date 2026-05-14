/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.api.es.processors.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.fao.geonet.domain.ReservedOperation;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EsDocumentRemovePrivilegesProcessorTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final EsDocumentRemovePrivilegesProcessor processor = new EsDocumentRemovePrivilegesProcessor();

    @Test
    public void testRemovePrivileges() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = doc.putObject("_source");

        // Add some privileges
        source.put("op0", true);
        source.put("op1", true);
        source.put("op2", false);
        source.put("otherField", "value");

        processor.process(doc, null, Collections.emptyMap());

        assertNotNull(doc.get("_source"));
        assertNull(source.get("op0"));
        assertNull(source.get("op1"));
        assertNull(source.get("op2"));
        assertTrue(source.has("otherField"));
    }

    @Test
    public void testRemovePrivilegesNoSource() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        doc.put("something", "else");

        processor.process(doc, null, Collections.emptyMap());

        assertNull(doc.get("_source"));
        assertTrue(doc.has("something"));
    }

    @Test
    public void testRemovePrivilegesEmptySource() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        doc.putObject("_source");

        processor.process(doc, null, Collections.emptyMap());

        assertNotNull(doc.get("_source"));
        assertTrue(doc.get("_source").isEmpty());
    }

    @Test
    public void testAllReservedOperationsAreRemoved() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = doc.putObject("_source");

        for (ReservedOperation op : ReservedOperation.values()) {
            source.put("op" + op.getId(), true);
        }

        processor.process(doc, null, Collections.emptyMap());

        for (ReservedOperation op : ReservedOperation.values()) {
            assertFalse("Privilege op" + op.getId() + " should have been removed", source.has("op" + op.getId()));
        }
    }
}
