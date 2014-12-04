package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.base.Optional;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.fao.geonet.services.metadata.format.groovy.TransformationContext;
import org.xml.sax.Attributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
* @author Jesse on 12/2/2014.
*/
class TNodeTranslate extends TNode {
    private static final TextContentParser TEXT_CONTENT_PARSER = new TextContentParser();

    private final Translator translator;

    public TNodeTranslate(SystemInfo info, String qName, Attributes attributes, String key) throws IOException {
        super(info, qName, attributes);
        String[] parts = key.split(">");
        String type = parts.length > 1 ? parts[1] : null;
        String context = parts.length > 2 ? parts[2] : null;
        if (parts[0].equalsIgnoreCase("codelist")) {
            translator = new CodeListTranslator(type, context);
        } else if (parts[0].equalsIgnoreCase("node")) {
            translator = new NodeTranslator(type, context);
        } else if (parts[0].equalsIgnoreCase("default") || parts[0].trim().isEmpty()) {
            translator = new DefaultTranslator(type);
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
            addChild(new Node(info, text));
        } catch (Exception e) {
            throw new TemplateException(e);
        }
    }

    private class Node extends TNode {
        private final String text;

        public Node(SystemInfo info, String text) throws IOException {
            super(info, "", EMPTY_ATTRIBUTES);
            this.text = text;
        }

        @Override
        protected Optional<String> canRender(TRenderContext context) {
            return Optional.absent();
        }

        @Override
        public void render(TRenderContext context) throws IOException {
            final TextBlock block = TEXT_CONTENT_PARSER.parse(text);

            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            block.render(new TRenderContext(stream, context.getModel(true)));

            try {
                translator.translate(new String(stream.toByteArray(), Constants.CHARSET)).render(context);
            } catch (Exception e) {
                throw new TemplateException(e);
            }
        }
    }

    private interface Translator {
        TextBlock translate(String text) throws Exception;
    }

    private static class CodeListTranslator implements Translator {
        private final String type;
        private final String context;

        private CodeListTranslator(String type, String context) {
            this.type = type;
            this.context = context;
        }

        public TextBlock translate(String text) throws Exception {
            final Functions functions = TransformationContext.getContext().getFunctions();
            final String translation = functions.codelistTranslation(text, context, type);
            return TEXT_CONTENT_PARSER.parse(translation);
        }
    }

    private static class NodeTranslator implements Translator {
        private final String type;
        private final String context;

        private NodeTranslator(String type, String context) {
            this.type = type;
            this.context = context;
        }

        public TextBlock translate(String text) throws Exception {
            final Functions functions = TransformationContext.getContext().getFunctions();
            final String translation = functions.nodeTranslation(text, context, type);
            return TEXT_CONTENT_PARSER.parse(translation);
        }
    }
    private static class DefaultTranslator implements Translator {
        private final String file;

        private DefaultTranslator(String file) {
            this.file = file;
        }

        public TextBlock translate(String text) throws Exception {
            final Functions functions = TransformationContext.getContext().getFunctions();
            final String translation = functions.translate(text, file);
            return TEXT_CONTENT_PARSER.parse(translation);
        }
    }
}
