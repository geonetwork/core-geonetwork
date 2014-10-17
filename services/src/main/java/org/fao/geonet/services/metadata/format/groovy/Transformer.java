package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.Maps;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Transforms the metadata using the Groovy formatter.
 *
 * @author Jesse on 10/15/2014.
 */
public class Transformer {

    private final Handlers handlers;
    private final String formatterPath;

    public Transformer(Handlers handlers, String formatterPath) {
        this.handlers = handlers;
        Collections.sort(handlers.handlers);
        this.formatterPath = formatterPath;
    }

    public Element apply(Element metadata, List<Namespace> namespaces) throws Exception {
        TransformationContext context = new TransformationContext();
        context.setThreadLocal();
        Map<String, String> namespaceUriToPrefix = Maps.newHashMap();
        for (Namespace namespace : namespaces) {
            namespaceUriToPrefix.put(namespace.getPrefix(), namespace.getURI());
        }
        final XmlSlurper xmlSlurper = new XmlSlurper(false, false);
        StringBuilder resultantXml = new StringBuilder();
        handlers.startHandler.handle(resultantXml);

        for (String rootXpath : this.handlers.roots) {
            @SuppressWarnings("unchecked")
            final List<Content> roots = (List<Content>) Xml.selectNodes(metadata, rootXpath, namespaces);
            for (Content root : roots) {
                // later for performance we could start a thread to write to a PipedInputStream and parse the PipedOutputStream
                // however until we if there is a performance issue here we will not do that.
                GPathResult md = xmlSlurper.parseText(Xml.getString((Element) root)).declareNamespace(namespaceUriToPrefix);
                if (md.size() == 0) {
                    throw new IllegalArgumentException("There are not elements parsed from the xml");
                }
                StringBuilder path = new StringBuilder();
                createPath(root.getParentElement(), path);

                context.setRootPath(path.toString());
                handleElement(context, md, resultantXml);
            }
        }
        handlers.endHandler.handle(resultantXml);
        try {
            return Xml.loadString(resultantXml.toString(), false);
        } catch (Exception e) {
            Log.error(Geonet.FORMATTER, "Error parsing the resulting XML from '" + formatterPath + "' formatter.  Resulting XML is: " + resultantXml, e);
            throw e;
        }
    }

    private void createPath(Element node, StringBuilder path) {
        if (node.getParentElement() != null) {
            createPath(node.getParentElement(), path);
            path.append(">");
        }
        path.append(node.getQualifiedName());
    }

    private void processChildren(TransformationContext context, GPathResult md, StringBuilder resultantXml) throws IOException {
        @SuppressWarnings("unchecked")
        final List<GPathResult> children  = md.children().list();

        for (GPathResult child : children) {
            handleElement(context, child, resultantXml);
        }
    }

    private void handleElement(TransformationContext context, GPathResult elem, StringBuilder resultantXml) throws IOException {
        boolean continueProcessing = true;
        for (Handler handler : handlers.handlers) {
            if (handler.canHandle(context, elem)) {
                StringBuilder childData = new StringBuilder();
                if (handler.processChildren()) {
                    processChildren(context, elem, childData);
                }
                handler.handle(context, elem, resultantXml, childData.toString());
                continueProcessing = false;
                break;
            }
        }
        if (continueProcessing) {
            processChildren(context, elem, resultantXml);
        }
    }
}
