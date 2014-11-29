package org.fao.geonet.services.metadata.format.groovy.template;

import org.fao.geonet.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Stack;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * This class parses a XML Template file into a tree data structure representing the template.
 * <p/>
 * Nodes in the tree can have custom behaviour that is determined according to the attributes on the template XML.  For example
 * a node might be an nonEmpty node where the node will only be rendered if the attribute is nonEmpty
 * (non-empty/non-null string or collection)
 *
 * @author Jesse on 11/29/2014.
 */
@Component
public class XmlTemplateParser {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    List<TNodeFactory> tnodeFactories;

    public TNode parse(Path path) throws ParserConfigurationException, SAXException, IOException {

        try (InputStream in = Files.newInputStream(path)) {
            return parse(in);
        }

    }

    public TNode parse(InputStream in) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        Handler handler = new Handler(tnodeFactories);

        InputSource source = new InputSource(in);
        source.setEncoding(Constants.ENCODING);

        saxParser.parse(source, handler);
        return handler.root;
    }

    private static class Handler extends DefaultHandler {

        private final List<TNodeFactory> factories;
        public TNode root;
        public Stack<TNode> stack = new Stack<>();

        public Handler(List<TNodeFactory> factories) {
            this.factories = factories;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            try {
                for (TNodeFactory factory : factories) {
                    if (factory.applicable(localName, qName, attributes)) {
                        setCurrentNode(factory.create(localName, qName, attributes));
                        return;
                    }
                }

                setCurrentNode(new SimpleTNode(qName, attributes));
            } catch (IOException e) {
                throw new TemplateException(e);
            }
        }

        private void setCurrentNode(TNode node) {
            if (root == null) {
                this.root = node;
            } else {
                this.stack.peek().addChild(node);
            }
            this.stack.push(node);

        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            this.stack.pop();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            final char[] content = new char[length];
            System.arraycopy(ch, start, content, 0, length);
            this.stack.peek().setTextContent(new String(content));
        }
    }
}
