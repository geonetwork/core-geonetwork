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

import com.google.common.base.Optional;

import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.records.formatters.groovy.Handlers;
import org.fao.geonet.api.records.formatters.groovy.TransformationContext;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;

import java.io.IOException;

/**
 * Creates nodes that import other templates into the current template.
 *
 * @author Jesse on 12/3/2014.
 */
@Component
public class TNodeFactoryInclude extends TNodeFactoryByAttName {
    private static final String INCLUDE = "include";
    private static final String REPLACE = "include-replace";


    protected TNodeFactoryInclude() {
        super(INCLUDE, null);
    }

    public TNodeFactoryInclude(SystemInfo info, TextContentParser contentParser) {
        super(INCLUDE, info);
        super.textContentParser = contentParser;
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        Attributes filteredAtts = new AttributesFiltered(attributes, INCLUDE, REPLACE);
        String templatePath = getValue(attributes, INCLUDE);

        boolean replace = getBooleanAttribute(attributes, REPLACE, false);
        return new TNodeInclude(SystemInfo.getInfo(this.testingInfo), textContentParser, qName, filteredAtts, templatePath, replace);
    }

    private class TNodeInclude extends TNode {

        private final String templatePath;
        private final boolean replace;

        public TNodeInclude(SystemInfo info, TextContentParser parser, String qName, Attributes attributes, String templatePath, boolean replace)
            throws IOException {
            super(info, parser, qName, attributes);
            this.replace = replace;
            this.templatePath = templatePath;
            if (!replace) {
                this.addChild(new TNodeInclude(info, parser, qName, attributes, templatePath, true));
            }
        }

        @Override
        protected Optional<String> canRender(TRenderContext context) {
            return Optional.absent();
        }

        @Override
        public void render(TRenderContext context) throws IOException {
            if (replace) {
                final Handlers handlers = TransformationContext.getContext().getHandlers();
                final FileResult fileResult = handlers.fileResult(this.templatePath, context.getModel(true));
                context.append(fileResult.toString());
            } else {
                super.render(context);
            }
        }


    }
}
