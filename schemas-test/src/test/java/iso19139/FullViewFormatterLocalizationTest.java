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

package iso19139;

import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.api.records.formatters.FormatType;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.Assert.assertEquals;

/**
 * @author Jesse on 10/17/2014.
 */
// TODOES
@Ignore
public class FullViewFormatterLocalizationTest extends AbstractFullViewFormatterTest {

    @Autowired
    private IsoLanguagesMapper mapper;

    @Test
    @SuppressWarnings("unchecked")
    public void testPrintFormatLocales() throws Exception {
        final FormatType formatType = FormatType.testpdf;

        Format format = new Format(formatType);
        assertCorrectTranslation(format, "eng");
        assertCorrectTranslation(format, "fre");
        assertCorrectTranslation(format, "ger");

    }

    public void assertCorrectTranslation(Format format, String lang) throws Exception {
        format.setRequestLanguage(lang);
        format.invoke();
        String view = format.getView();

        assertEquals(view, lang.equalsIgnoreCase("eng"), view.contains("Identification EN Title"));
        assertEquals(view, lang.equalsIgnoreCase("fre"), view.contains("Identification FR Title"));
        assertEquals(view, lang.equalsIgnoreCase("ger"), view.contains("Identification DE Title"));

    }

}
