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
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.records.formatters.groovy.Functions;
import org.fao.geonet.api.records.formatters.groovy.TransformationContext;
import org.xml.sax.Attributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Jesse on 12/2/2014.
 */
class TNodeTranslate extends TNode {

    private final Translator translator;

    public TNodeTranslate(SystemInfo info, TextContentParser textContentParser, String qName, Attributes attributes, String key) throws IOException {
        super(info, textContentParser, qName, attributes);
        String[] parts = key.split(">");
        String type = parts.length > 1 ? parts[1] : null;
        String context = parts.length > 2 ? parts[2] : null;
        if (parts[0].equalsIgnoreCase("codelist")) {
            translator = new CodeListTranslator(type, textContentParser, context);
        } else if (parts[0].equalsIgnoreCase("node")) {
            translator = new NodeTranslator(type, textContentParser, context);
        } else if (parts[0].equalsIgnoreCase("default") || parts[0].trim().isEmpty()) {
            translator = new DefaultTranslator(type, textContentParser);
        } else {
            throw new TemplateException(
                "Translate type: '" + parts[0] + "' is not one of the recognized type: ['', 'default', 'node', 'codelist']");
        }
    }

    @Override
    protected Optional<String> canRender(TRenderContext context) {
        return Optional.absent();
    }

    @Override
    public void setTextContent(String text) throws IOException {
        if (text.isEmpty()) {
            return;
        }
        try {
            addChild(new Node(info, textContentParser, text));
        } catch (Exception e) {
            throw new TemplateException(e);
        }
    }

    private interface Translator {
        TextBlock translate(String text) throws Exception;
    }

    private static class CodeListTranslator implements Translator {
        private final String type;
        private final String context;
        private final TextContentParser textContentParser;

        private CodeListTranslator(String type, TextContentParser parser, String context) {
            this.type = type;
            this.context = context;
            this.textContentParser = parser;
        }

        public TextBlock translate(String text) throws Exception {
            final Functions functions = TransformationContext.getContext().getFunctions();
            final String translation = functions.codelistTranslation(text, context, type);
            return textContentParser.parse(translation);
        }
    }

    private static class NodeTranslator implements Translator {
        private final String type;
        private final String context;
        private final TextContentParser textContentParser;

        private NodeTranslator(String type, TextContentParser parser, String context) {
            this.type = type;
            this.context = context;
            this.textContentParser = parser;
        }

        public TextBlock translate(String text) throws Exception {
            final Functions functions = TransformationContext.getContext().getFunctions();
            final String translation = functions.nodeTranslation(text, context, type);
            return textContentParser.parse(translation);
        }
    }

    private static class DefaultTranslator implements Translator {
        private final String file;
        private final TextContentParser textContentParser;

        private DefaultTranslator(String file, TextContentParser parser) {
            this.file = file;
            this.textContentParser = parser;
        }

        public TextBlock translate(String text) throws Exception {
            final Functions functions = TransformationContext.getContext().getFunctions();
            final String translation = functions.translate(text, file);
            return textContentParser.parse(translation);
        }
    }

    private class Node extends TNode {
        private final String text;

        public Node(SystemInfo info, TextContentParser parser, String text) throws IOException {
            super(info, parser, "", EMPTY_ATTRIBUTES);
            this.text = text;
        }

        @Override
        protected Optional<String> canRender(TRenderContext context) {
            return Optional.absent();
        }

        @Override
        public void render(TRenderContext context) throws IOException {
            final TextBlock block = textContentParser.parse(text);

            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            block.render(new TRenderContext(stream, context.getModel(true)));

            try {
                translator.translate(new String(stream.toByteArray(), Constants.CHARSET)).render(context);
            } catch (Exception e) {
                throw new TemplateException(e);
            }
        }
    }
}
