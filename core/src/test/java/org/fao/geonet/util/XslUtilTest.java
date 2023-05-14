/*
 * Copyright (C) 2001-2022 Food and Agriculture Organization of the
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

package org.fao.geonet.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XslUtilTest {

    @Test
    public void testHtml2text() {
        String html = "<div><span>Sample text</span><br/><span>Sample text 2</span><br/><span>Sample text 3</span></div>";
        String expectedText = "Sample text\nSample text 2\nSample text 3";
        String text = XslUtil.html2text(html);

        assertEquals(expectedText, text);
    }

    @Test
    public void testHtml2textSubstituteHtmlToTextLayoutElement() {
        String html = "<div><span>Sample text</span><br/><span>Sample text 2</span><br/><span>Sample text 3</span></div>";
        String expectedText = "Sample text\nSample text 2\nSample text 3";
        String text = XslUtil.html2text(html, true);

        assertEquals(expectedText, text);
    }

    @Test
    public void testHtml2textNormalized() {
        String html = "<div><span>Sample text</span><br/><span>Sample text 2</span></div>";
        String expectedText = "Sample text Sample text 2";
        String text = XslUtil.html2textNormalized(html);

        assertEquals(expectedText, text);
    }

}
