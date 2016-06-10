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

package org.fao.geonet.api.records.formatters.groovy;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transforms the metadata using the Groovy formatter.
 *
 * @author Jesse on 10/15/2014.
 */
public class Transformer {

    private final Handlers handlers;
    private final Path formatterPath;
    private final Functions functions;
    private final Environment env;

    public Transformer(Handlers handlers, Functions functions, Environment env, Path formatterPath) {
        this.handlers = handlers;
        this.functions = functions;
        this.env = env;
        handlers.prepareForTransformer();
        this.formatterPath = formatterPath;
    }

    public String apply(Element metadata, List<Namespace> namespaces) throws Exception {
        TransformationContext context = new TransformationContext(handlers, functions, env);
        context.setThreadLocal();
        Map<String, String> namespaceUriToPrefix = Maps.newHashMap();
        for (Namespace namespace : namespaces) {
            namespaceUriToPrefix.put(namespace.getPrefix(), namespace.getURI());
        }
        final XmlSlurper xmlSlurper = new XmlSlurper(false, false);
        StringBuilder resultantXml = new StringBuilder();
        handlers.startHandler.handle(resultantXml);

        Set<String> rootXpaths = this.handlers.getRoots();
        if (rootXpaths.isEmpty()) {
            processRoot(context, namespaceUriToPrefix, xmlSlurper, resultantXml, metadata);
        }
        for (String rootXpath : rootXpaths) {
            @SuppressWarnings("unchecked")
            final List<Content> roots = (List<Content>) Xml.selectNodes(metadata, rootXpath, namespaces);
            for (Content root : roots) {
                processRoot(context, namespaceUriToPrefix, xmlSlurper, resultantXml, root);
            }
        }
        handlers.endHandler.handle(resultantXml);
        try {
            return resultantXml.toString();
        } catch (Exception e) {
            Log.error(Geonet.FORMATTER, "Error parsing the resulting XML from '" + formatterPath + "' formatter.  Resulting XML is: " +
                resultantXml, e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private void processRoot(TransformationContext context, Map<String, String> namespaceUriToPrefix, XmlSlurper xmlSlurper,
                             StringBuilder resultantXml, Content root) throws IOException, SAXException {
        // later for performance we could start a thread to write to a PipedInputStream and parse the PipedOutputStream
        // however until we if there is a performance issue here we will not do that.
        GPathResult md = xmlSlurper.parseText(Xml.getString((Element) root)).declareNamespace(namespaceUriToPrefix);
        if (md.size() == 0) {
            throw new IllegalArgumentException("There are no elements parsed from the xml");
        }
        StringBuilder path = new StringBuilder();
        createPath(root.getParentElement(), path);

        context.setRootPath(path.toString());
        handlers.transformationEngine.processElement(context, md, md.children().list(), resultantXml);
    }

    private void createPath(Element node, StringBuilder path) {
        if (node == null) {
            return;
        }
        if (node.getParentElement() != null) {
            createPath(node.getParentElement(), path);
            path.append(">");
        }
        path.append(node.getQualifiedName());
    }

    @VisibleForTesting
    public Handlers getHandlers() {
        return this.handlers;
    }
}
