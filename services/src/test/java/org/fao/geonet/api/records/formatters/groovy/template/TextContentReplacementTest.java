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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TextContentReplacementTest {

    @Test
    public void testText() throws Exception {
        final TextContentReplacement textContentReplacement = new TextContentReplacement("key", Lists.<TextContentFilter>newArrayList());


        String expected = "value";
        Map<String, Object> model = Maps.newHashMap();
        model.put("key", expected);

        assertCorrectText(textContentReplacement, expected, model);
    }

    @Test
    public void testTextAttributeEscape() throws Exception {
        final TextContentReplacement textContentReplacement = new TextContentReplacement("key",
            Lists.newArrayList(new FilterEscapeXmlAttrs()));


        String expected = "value";
        Map<String, Object> model = Maps.newHashMap();
        model.put("key", expected);

        assertCorrectText(textContentReplacement, expected, model);
    }

    public void assertCorrectText(TextContentReplacement textContentReplacement, String expected, Map<String, Object> model) throws UnsupportedEncodingException {
        TRenderContext context = new TRenderContext(new ByteArrayOutputStream(), model);
        final String text = textContentReplacement.text(context);
        assertEquals(expected, text);
    }
}
