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

package org.fao.geonet.api.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectNodeUtilsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void getSourceString_ShouldReturnString_WhenFieldExists() {
        ObjectNode source = mapper.createObjectNode();
        source.put("testField", "testValue");
        ObjectNode node = mapper.createObjectNode();
        node.set(ObjectNodeUtils.SOURCE_NODE, source);

        assertEquals("testValue", ObjectNodeUtils.getSourceString(node, "testField"));
    }

    @Test
    public void getSourceString_ShouldReturnNull_WhenFieldDoesNotExist() {
        ObjectNode source = mapper.createObjectNode();
        ObjectNode node = mapper.createObjectNode();
        node.set(ObjectNodeUtils.SOURCE_NODE, source);

        assertNull(ObjectNodeUtils.getSourceString(node, "missingField"));
    }

    @Test
    public void getSourceString_ShouldReturnNull_WhenSourceNodeMissing() {
        ObjectNode node = mapper.createObjectNode();
        assertNull(ObjectNodeUtils.getSourceString(node, "testField"));
    }

    @Test
    public void getSourceInteger_ShouldReturnInteger_WhenFieldExists() {
        ObjectNode source = mapper.createObjectNode();
        source.put("testField", 123);
        ObjectNode node = mapper.createObjectNode();
        node.set(ObjectNodeUtils.SOURCE_NODE, source);

        assertEquals(Integer.valueOf(123), ObjectNodeUtils.getSourceInteger(node, "testField"));
    }

    @Test
    public void getSourceInteger_ShouldReturnNull_WhenFieldDoesNotExist() {
        ObjectNode source = mapper.createObjectNode();
        ObjectNode node = mapper.createObjectNode();
        node.set(ObjectNodeUtils.SOURCE_NODE, source);

        assertNull(ObjectNodeUtils.getSourceInteger(node, "missingField"));
    }

    @Test
    public void getSourceInteger_ShouldReturnNull_WhenSourceNodeMissing() {
        ObjectNode node = mapper.createObjectNode();
        assertNull(ObjectNodeUtils.getSourceInteger(node, "testField"));
    }

    @Test
    public void getSourceNode_ShouldReturnNode_WhenSourceNodeExists() {
        ObjectNode source = mapper.createObjectNode();
        ObjectNode node = mapper.createObjectNode();
        node.set(ObjectNodeUtils.SOURCE_NODE, source);

        ObjectNode result = ObjectNodeUtils.getSourceNode(node);
        assertNotNull(result);
        assertEquals(source, result);
    }

    @Test
    public void getSourceNode_ShouldReturnNull_WhenSourceNodeMissing() {
        ObjectNode node = mapper.createObjectNode();
        assertNull(ObjectNodeUtils.getSourceNode(node));
    }
}
