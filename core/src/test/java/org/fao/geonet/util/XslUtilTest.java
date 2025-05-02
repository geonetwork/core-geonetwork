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
    public void testHtml2textSubstituteHtmlHyperlinkToText() {
        String text = XslUtil.html2text("<p>This data set is mainly based on data extracted from the European Union Transaction Log (EUTL) on 1st April 2022. The EUTL checks and records all transactions taking place within the trading system.</p>^M\n" +
            "<p>It is run by the European Commission. See: <a class=\"external\" href=\"http://ec.europa.eu/environment/ets\">http://ec.europa.eu/environment/ets</a>. The data set also includes information on auctions of allowances, compiled from auctioning platforms, as well as EEA estimates of ETS emissions for the period 2005 to 2012, to reflect the current scope of the ETS for the third trading period (2013–2020). See more information in the <a title=\"\" href=\"http://www.eea.europa.eu/data-and-maps/data/european-union-emissions-trading-scheme/eu-ets-data-viewer-manual\" class=\"internal-link\" target=\"_self\">manual</a>.</p>", true);

        String expectedText = "This data set is mainly based on data extracted from the European Union Transaction Log (EUTL) on 1st April 2022. The EUTL checks and records all transactions taking place within the trading system.^M\n" +
            "It is run by the European Commission. See: http://ec.europa.eu/environment/ets (http://ec.europa.eu/environment/ets). The data set also includes information on auctions of allowances, compiled from auctioning platforms, as well as EEA estimates of ETS emissions for the period 2005 to 2012, to reflect the current scope of the ETS for the third trading period (2013–2020). See more information in the manual (http://www.eea.europa.eu/data-and-maps/data/european-union-emissions-trading-scheme/eu-ets-data-viewer-manual).";

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
