package org.fao.geonet.services.metadata.format.groovy.template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * This class parses a XML Template file into a tree data structure representing the template.
 *
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
        saxParser.parse(in, handler);
        return handler.root;
    }

    private static class Handler extends DefaultHandler {

        private final List<TNodeFactory> factories;
        public TNode root, current;

        public Handler(List<TNodeFactory> factories) {
            this.factories = factories;
        }


        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            for (TNodeFactory factory : factories) {
                if (factory.applicable(localName, qName, attributes)) {
                    try {
                        setCurrentNode(factory.create(localName, qName, attributes));
                        break;
                    } catch (IOException e) {
                        throw new TemplateException(e);
                    }
                }
            }

            try {
                setCurrentNode(new SimpleTNode(localName, qName, attributes));
            } catch (IOException e) {
                throw new TemplateException(e);
            }
        }

        private void setCurrentNode(TNode node) {
            if (root == null) {
                this.root = node;
            } else {
                this.current.addChild(node);
            }
            this.current = node;

        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
        }
    }
}
