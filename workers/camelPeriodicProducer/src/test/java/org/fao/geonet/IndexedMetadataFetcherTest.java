/*
 * Copyright (C) 2001-2015 Food and Agriculture Organization of the
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

package org.fao.geonet;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.json.JSONException;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class IndexedMetadataFetcherTest {

    private EsSearchManager searchManager = Mockito.mock(EsSearchManager.class);

    @Test
    public void parsingTreeField() throws IOException, JSONException {
        String toParse = IOUtils.toString(this.getClass().getResourceAsStream("ApplicationProfile.json"));
        IndexedMetadataFetcher toTest = new IndexedMetadataFetcher(searchManager);
        toTest.setIndex(toParse);

        List<String> treeField = toTest.getTreeField();

        String[] expected = new String[]{"PARAMETRES"};
        assertArrayEquals("parse error", expected, treeField.toArray());
    }

    @Test
    public void parsingTokenizedField() throws IOException, JSONException {
        String toParse = IOUtils.toString(this.getClass().getResourceAsStream("ApplicationProfile.json"));
        IndexedMetadataFetcher toTest = new IndexedMetadataFetcher(searchManager);
        toTest.setIndex(toParse);

        Map<String, String> tokenizedField = toTest.getTokenizedField();

        assertEquals("parse error", ";", tokenizedField.get("PROGRAMMES"));
        assertEquals("parse error", ";", tokenizedField.get("PARAMETRES"));
        assertEquals("parse error", ";", tokenizedField.get("SUPPORTS_NIVEAUX_PRELEVEMENT"));
    }
}
