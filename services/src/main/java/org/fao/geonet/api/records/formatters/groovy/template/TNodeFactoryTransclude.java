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
import com.google.common.collect.Maps;

import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.records.formatters.groovy.Handlers;
import org.fao.geonet.api.records.formatters.groovy.TransformationContext;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Creates nodes that import other templates into the current template.
 *
 * @author Jesse on 12/3/2014.
 */
@Component
public class TNodeFactoryTransclude extends TNodeFactoryByAttName {
    private static final String TRANSCLUDE = "transclude";
    private static final String MODEL_KEY = TRANSCLUDE + "-model";
    private static final String EXTRA_MODEL = TRANSCLUDE + "-extra-model";
    private static final String REPLACE = TRANSCLUDE + "-replace";

    protected TNodeFactoryTransclude() {
        super(TRANSCLUDE, null);
    }

    public TNodeFactoryTransclude(SystemInfo info, TextContentParser contentParser) {
        super(TRANSCLUDE, info);
        super.textContentParser = contentParser;
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        Attributes filteredAtts = new AttributesFiltered(attributes, TRANSCLUDE, REPLACE, MODEL_KEY, EXTRA_MODEL);
        String templatePath = getValue(attributes, TRANSCLUDE);
        String model = getValue(attributes, MODEL_KEY);
        String extraModelAtts = getValue(attributes, EXTRA_MODEL);

        Map<String, Object> extraModel = Maps.newHashMap();
        if (extraModelAtts != null) {

            for (String att : extraModelAtts.split("\\|")) {
                final String[] parts = att.split("=");
                String key = parts[0];
                String value = parts.length > 1 ? parts[1] : null;

                extraModel.put(key, value);
            }
        }

        boolean replace = getBooleanAttribute(attributes, REPLACE, false);

        return new TNodeTransclude(SystemInfo.getInfo(this.testingInfo), textContentParser, qName, filteredAtts, templatePath, replace, model, extraModel);
    }

    private static class TNodeTransclude extends TNode {

        private final String templatePath;
        private final boolean replace;
        private final String model;
        private final Map<String, Object> extraModel;

        public TNodeTransclude(SystemInfo info, TextContentParser parser, String qName, Attributes attributes, String templatePath, boolean replace,
                               String model, Map<String, Object> extraModel) throws IOException {
            super(info, parser, qName, attributes);
            this.replace = replace;
            this.templatePath = templatePath;
            this.model = model;
            this.extraModel = extraModel;
        }

        @Override
        protected Optional<String> canRender(TRenderContext context) {
            return Optional.absent();
        }

        @Override
        public void render(TRenderContext context) throws IOException {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final TRenderContext sepContext = new TRenderContext(outputStream, context.getModel(true));
            if (replace) {
                for (TNode childNode : getChildren()) {
                    childNode.render(sepContext);
                }
            } else {
                super.render(sepContext);
            }

            Map<String, Object> fullModel = Maps.newHashMap();
            fullModel.putAll(context.getModel(true));
            for (Map.Entry<String, Object> entry : this.extraModel.entrySet()) {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                textContentParser.parse(entry.getValue().toString()).render(new TRenderContext(o, context.getModel(true)));
                fullModel.put(entry.getKey(), new String(o.toByteArray(), Constants.CHARSET));
            }

            fullModel.put(this.model, new String(outputStream.toByteArray(), Constants.CHARSET));
            final Handlers handlers = TransformationContext.getContext().getHandlers();
            final FileResult fileResult = handlers.fileResult(this.templatePath, fullModel);
            context.append(fileResult.toString());
        }
    }

}
