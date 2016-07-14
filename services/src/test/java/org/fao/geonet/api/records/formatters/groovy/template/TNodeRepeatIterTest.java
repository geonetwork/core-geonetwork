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

package org.fao.geonet.api.records.formatters.groovy.template;

import org.fao.geonet.SystemInfo;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TNodeRepeatIterTest {
    SystemInfo info = SystemInfo.createForTesting(SystemInfo.STAGE_TESTING);

    @Test
    public void testEmptyRender() throws Exception {
        final TNodeRepeatIter repeat = new TNodeRepeatIter(info, TextContentParserTest.createTestTextContentParser(),
            false, null, TNode.EMPTY_ATTRIBUTES, "iter", "key");
        Map<String, Object> model = Collections.<String, Object>singletonMap("iter", Collections.emptyList());
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        repeat.render(new TRenderContext(outputStream, model));

        assertEquals("<!-- fmt-repeat: key in iter is empty -->", outputStream.toString());
    }
}
