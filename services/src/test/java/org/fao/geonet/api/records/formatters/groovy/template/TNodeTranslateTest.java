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
import org.fao.geonet.api.records.formatters.groovy.Functions;
import org.fao.geonet.api.records.formatters.groovy.TransformationContext;
import org.fao.geonet.utils.IO;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.util.Collections;

/**
 * @author Jesse on 12/3/2014.
 */
public class TNodeTranslateTest extends AbstractTemplateParserTest {

    @Test
    public void testTranslateDirective() throws Exception {
        final Functions mock = Mockito.mock(Functions.class);
        Mockito.when(mock.translate("testString", null)).thenReturn("translation null");
        Mockito.when(mock.translate("testString", "file")).thenReturn("translation file");
        Mockito.when(mock.translate("nameKey", null)).thenReturn("Translated After resolve");
        Mockito.when(mock.codelistTranslation("testString", null, "name")).thenReturn("translation codelist null name");
        Mockito.when(mock.codelistTranslation("testString", "context1", "desc")).thenReturn("translation codelist desc context1");
        Mockito.when(mock.codelistTranslation("testString", "context2", "desc")).thenReturn("translation codelist desc context2");
        Mockito.when(mock.nodeTranslation("testString", "context", "name")).thenReturn("translation node name context");
        Mockito.when(mock.nodeTranslation("testString", null, "desc")).thenReturn("translation node desc null");
        new TransformationContext(null, mock, null).setThreadLocal();

        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TNodeTranslateTest.class.getResource("translate.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));

        String expected = "<html>\n"
            + "    <div>translation null</div>\n"
            + "    <div>translation null</div>\n"
            + "    <div>translation file</div>\n"
            + "    <div>translation file</div>\n"
            + "    <div>translation codelist null name</div>\n"
            + "    <div>translation codelist desc context1</div>\n"
            + "    <div>translation codelist desc context2</div>\n"
            + "    <div>translation node name context</div>\n"
            + "    <div>translation node desc null</div>\n"
            + "    <div>Translated After resolve</div>\n"
            + "</html>";

        assertCorrectRender(parseTree, Collections.<String, Object>singletonMap("key", "nameKey"), expected);
    }
}
