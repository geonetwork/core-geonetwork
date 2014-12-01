package org.fao.geonet.services.metadata.format.groovy.template;

import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Creates fmt-translate nodes.
 *
 * A translate node take the text content element and find a translation.  depending on the value of the
 * fmt-translate attribute the translation is resolved using the {@link org.fao.geonet.services.metadata.format.groovy.Functions}
 * translate methods.
 *
 * Examples:
 * <pre><code>
 * &lt;div fmt-translate="">testString&lt;/div>
 * &lt;div fmt-translate="default">testString&lt;/div>
 * &lt;div fmt-translate=">file">testString&lt;/div>
 * &lt;div fmt-translate="default>file">testString&lt;/div>
 * &lt;div fmt-translate="codelist>name">testString&lt;/div>
 * &lt;div fmt-translate="codelist>desc>context1">testString&lt;/div>
 * &lt;div fmt-translate="codelist>desc>context2">testString&lt;/div>
 * &lt;div fmt-translate="node>name>context">testString&lt;/div>
 * &lt;div fmt-translate="node>desc">testString&lt;/div>
 * </code></pre>
 *
 * @author Jesse on 11/29/2014.
 */
@Component
public class TNodeFactoryTranslate extends TNodeFactoryByAttName {
    private static final TextContentParser TEXT_CONTENT_PARSER = new TextContentParser();
    public static final String TRANSLATE = "translate";

    public TNodeFactoryTranslate() {
        super(TRANSLATE);
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        final String value = getValue(attributes, TRANSLATE);
        final FilteredAttributes filteredAttributes = new FilteredAttributes(attributes, TRANSLATE);
        return new TNodeTranslate(qName, filteredAttributes, value);
    }

    private static class TNodeTranslate extends TNode {
        private final Translator translator;

        public TNodeTranslate(String qName, Attributes attributes, String key) throws IOException {
            super(qName, attributes);
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
        protected boolean canRender(TRenderContext context) {
            return true;
        }

        @Override
        public void setTextContent(String text) throws IOException {
            if (text.isEmpty()) {
                return;
            }
            try {
                addChild(new Node(text));
            } catch (Exception e) {
                throw new TemplateException(e);
            }
        }

        private class Node extends TNode {
            private final String text;

            public Node(String text) throws IOException {
                super("", EMPTY_ATTRIBUTES);
                this.text = text;
            }

            @Override
            protected boolean canRender(TRenderContext context) {
                return true;
            }

            @Override
            public void render(TRenderContext context) throws IOException {
                final TextBlock block = TEXT_CONTENT_PARSER.parse(text);

                final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                block.render(new TRenderContext(stream, context.getModel()));

                try {
                    translator.translate(stream.toString()).render(context);
                } catch (Exception e) {
                    throw new TemplateException(e);
                }
            }
        }
    }

    private interface Translator {
        TextBlock translate(String text) throws Exception;
    }

    private static class CodeListTranslator implements Translator{
        private final String type;
        private final String context;

        private CodeListTranslator(String type, String context) {
            this.type = type;
            this.context = context;
        }

        public TextBlock translate(String text) throws Exception {
            final String translation = Functions.getThreadLocal().codelistTranslation(text, context, type);
            return TEXT_CONTENT_PARSER.parse(translation);
        }
    }

    private static class NodeTranslator implements Translator{
        private final String type;
        private final String context;

        private NodeTranslator(String type, String context) {
            this.type = type;
            this.context = context;
        }

        public TextBlock translate(String text) throws Exception {
            final String translation = Functions.getThreadLocal().nodeTranslation(text, context, type);
            return TEXT_CONTENT_PARSER.parse(translation);
        }
    }
    private static class DefaultTranslator implements Translator {
        private final String file;

        private DefaultTranslator(String file) {
            this.file = file;
        }

        public TextBlock translate(String text) throws Exception {
            final String translation = Functions.getThreadLocal().translate(text, file);
            return TEXT_CONTENT_PARSER.parse(translation);
        }
    }

}
