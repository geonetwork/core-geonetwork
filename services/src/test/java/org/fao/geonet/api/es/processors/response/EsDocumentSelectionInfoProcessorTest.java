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
import org.fao.geonet.api.es.ObjectNodeUtils;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EsDocumentSelectionInfoProcessorTest {

    private EsDocumentSelectionInfoProcessor processor;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        processor = new EsDocumentSelectionInfoProcessor();
        mapper = new ObjectMapper();
    }

    @Test
    public void process_ShouldSetSelectedToTrue_WhenUuidIsSelected() throws Exception {
        String uuid = "test-uuid";
        ObjectNode doc = createDocWithUuid(uuid);
        Set<String> selections = new HashSet<>();
        selections.add(uuid);

        Map<String, Object> params = new HashMap<>();
        params.put("selections", selections);
        processor.process(doc, null, params);

        assertTrue(doc.get(Edit.Info.Elem.SELECTED).asBoolean());
    }

    @Test
    public void process_ShouldSetSelectedToFalse_WhenUuidIsNotSelected() throws Exception {
        String uuid = "test-uuid";
        ObjectNode doc = createDocWithUuid(uuid);
        Set<String> selections = new HashSet<>();
        selections.add("other-uuid");

        Map<String, Object> params = new HashMap<>();
        params.put("selections", selections);
        processor.process(doc, null, params);

        assertFalse(doc.get(Edit.Info.Elem.SELECTED).asBoolean());
    }

    @Test
    public void process_ShouldSetSelectedToFalse_WhenUuidIsMissingInDoc() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        doc.set(ObjectNodeUtils.SOURCE_NODE, mapper.createObjectNode());
        Set<String> selections = new HashSet<>();
        selections.add("some-uuid");

        Map<String, Object> params = new HashMap<>();
        params.put("selections", selections);
        processor.process(doc, null, params);

        assertFalse(doc.get(Edit.Info.Elem.SELECTED).asBoolean());
    }

    @Test
    public void process_ShouldSetSelectedToFalse_WhenSourceNodeMissing() throws Exception {
        ObjectNode doc = mapper.createObjectNode();
        Set<String> selections = Collections.singleton("uuid");

        Map<String, Object> params = new HashMap<>();
        params.put("selections", selections);
        processor.process(doc, null, params);

        assertFalse(doc.get(Edit.Info.Elem.SELECTED).asBoolean());
    }

    private ObjectNode createDocWithUuid(String uuid) {
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.UUID, uuid);
        doc.set(ObjectNodeUtils.SOURCE_NODE, source);
        return doc;
    }
}
