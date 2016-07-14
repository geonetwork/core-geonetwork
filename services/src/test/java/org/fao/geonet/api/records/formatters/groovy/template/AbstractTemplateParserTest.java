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

import org.fao.geonet.SystemInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public abstract class AbstractTemplateParserTest {

    public static void assertCorrectRender(TNode parseTree, Map<String, Object> model, String expected) throws IOException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        TRenderContext context = new TRenderContext(result, model);

        parseTree.render(context);

        assertEquals(expected + "\n" + result, expected.replaceAll("\\n|\\r|\\s+", ""), result.toString().replaceAll("\\n|\\r|\\s+", ""));
    }

    public static TemplateParser createTestParser(String systemInfoStage) throws IllegalAccessException, InstantiationException {
        SystemInfo info = SystemInfo.createForTesting(systemInfoStage);
        final TemplateParser parser = new TemplateParser();
        final TextContentParser contentParser = TextContentParserTest.createTestTextContentParser();
        parser.textContentParser = contentParser;
        parser.tnodeFactories = Lists.<TNodeFactory>newArrayList(
            new TNodeFactoryIf(info, contentParser), new TNodeFactoryRepeat(info, contentParser), new TNodeFactoryTranslate(info, contentParser),
            new TNodeFactoryInclude(info, contentParser), new TNodeFactoryTransclude(info, contentParser));
        return parser;
    }
}
