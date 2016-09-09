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

import com.google.common.annotations.VisibleForTesting;

import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
 * Nodes in the tree can have custom behaviour that is determined according to the attributes on the
 * template XML.  For example a node might be an nonEmpty node where the node will only be rendered
 * if the attribute is nonEmpty (non-empty/non-null string or collection)
 *
 * @author Jesse on 11/29/2014.
 */
@Component
public class TemplateParser {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @VisibleForTesting
    List<TNodeFactory> tnodeFactories;
    @VisibleForTesting
    @Autowired
    TextContentParser textContentParser;

    public TNode parse(Path path) {
        try {
            final TNode root = parse(Files.readAllBytes(path), TemplateType.fromPath(path));
            root.setUnparsedSize(Files.size(path));
            return root;
        } catch (IOException e) {
            throw new TemplateException(e);
        } catch (TemplateException e) {
            if (e.getCause() != null) {
                throw new TemplateException("Error when parsing " + path + ":" + e.getCause().getMessage(), e.getCause());
            } else {
                throw new TemplateException("Error when parsing " + path + ":" + e.getMessage(), e);
            }
        }
    }

    public TNode parse(byte[] in, TemplateType type) {
        switch (type) {
            case XML:
                try {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser saxParser = factory.newSAXParser();

                    Handler handler = new Handler();

                    InputSource source = new InputSource(new ByteArrayInputStream(in));
                    source.setEncoding(Constants.ENCODING);

                    saxParser.parse(source, handler);
                    return handler.root;
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    throw new TemplateException(e);
                }
            default:
                try {
                    final String unparsedText = new String(in, Constants.ENCODING);
                    return new TNodeTextContent(getSystemInfo(), textContentParser, textContentParser.parse(unparsedText));
                } catch (IOException e) {
                    throw new TemplateException(e);
                }
        }
    }

    protected SystemInfo getSystemInfo() {
        return SystemInfo.getInfo();
    }

    private class Handler extends DefaultHandler {

        public TNode root;
        public Stack<TNode> stack = new Stack<>();
        public char[] textContent;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            try {
                processText();
                TNodeFactory found = null;
                for (TNodeFactory factory : tnodeFactories) {
                    if (factory.applicable(localName, qName, attributes)) {
                        if (found != null) {
                            throw new TemplateException(
                                "More than one directive attribute was found on " + qName + "\nFound: " +
                                    found.getClass().getSimpleName() + " and " + factory.getClass().getSimpleName());
                        }
                        setCurrentNode(factory.create(localName, qName, attributes));
                        found = factory;
                    }
                }
                if (found == null) {
                    setCurrentNode(new SimpleTNode(getSystemInfo(), textContentParser, qName, attributes));
                }
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
            processText();
            this.stack.pop();
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            final int copyToStart;
            if (this.textContent != null) {
                char[] tmp = this.textContent;
                this.textContent = new char[tmp.length + length];
                System.arraycopy(tmp, 0, this.textContent, 0, tmp.length);
                copyToStart = tmp.length;
            } else {
                copyToStart = 0;
                this.textContent = new char[length];
            }
            System.arraycopy(ch, start, this.textContent, copyToStart, length);
        }

        private void processText() {
            if (this.textContent != null && !this.stack.isEmpty()) {
                try {
                    this.stack.peek().setTextContent(new String(this.textContent));
                } catch (IOException e) {
                    throw new TemplateException(e);
                }
            }
            this.textContent = null;
        }

    }
}
