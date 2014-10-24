package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.Lists;
import groovy.util.slurpersupport.GPathResult;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The class that does the actual transforming of the Metadata XML.
 *
 * @author Jesse on 10/24/2014.
 */
class TransformEngine {
    private final Handlers handlers;

    TransformEngine(Handlers handlers) {
        this.handlers = handlers;
    }

    String processElementsInMode(String mode, Iterable selection, GPathResult sortByElement) throws IOException {
        StringBuilder resultantXml = new StringBuilder();
        final TransformationContext context = TransformationContext.getContext();
        String oldMode = context.getCurrentMode();
        try {
            context.setCurrentMode(mode);
            Sorter sorter = null;
            if (sortByElement != null) {
                sorter = this.handlers.findSorter(context, sortByElement);
            }
            if (sorter == null) {
                Logging.debug("Not sorting input elements.  No sorter found for sortElement:%s. ", sortByElement);
                processUnsorted(selection, resultantXml, context);
            } else {
                Logging.debug("Sorting with: %2$s for sortElement:%1$s.", sortByElement, sorter);
                processSorted(selection, resultantXml, context, sorter);
            }

            return resultantXml.toString();
        } finally {
            context.setCurrentMode(oldMode);
        }
    }

    private void processSorted(Iterable selection, StringBuilder resultantXml, TransformationContext context, Sorter sorter) throws IOException {
        List<GPathResult> flattenedSelection = flattenGPathResults(selection);
        Collections.sort(flattenedSelection, sorter);
        for (GPathResult el : flattenedSelection) {
            processElement(context, el, resultantXml);
        }
    }

    private void processUnsorted(Iterable selection, StringBuilder resultantXml, TransformationContext context) throws IOException {
        for (Object path : selection) {
            final GPathResult gpath = (GPathResult) path;
            if (!gpath.isEmpty()) {
                for (Object el : gpath) {
                    processElement(context, (GPathResult) el, resultantXml);
                }
            }
        }
    }

    private List<GPathResult> flattenGPathResults(Iterable selection) {
        List<GPathResult> result = Lists.newArrayList();
        for (Object path : selection) {
            final GPathResult gpath = (GPathResult) path;
            if (!gpath.isEmpty()) {
                for (Object el : gpath) {
                    result.add((GPathResult) el);
                }
            }
        }
        return result;
    }

    private void processChildren(TransformationContext context, GPathResult md, StringBuilder resultantXml) throws IOException {
        final GPathResult childrenPath = md.children();
        if (Logging.isDebugMode()) {
            Logging.debug("Starting to process %2$d children of: %1$s.", md, childrenPath.size());
        }
        @SuppressWarnings("unchecked")
        final Iterator children = childrenPath.iterator();
        if (!children.hasNext()) {
            return;
        }

        Sorter sorter = this.handlers.findSorter(context, md);

        if (sorter == null) {
            while (children.hasNext()) {
                processElement(context, (GPathResult) children.next(), resultantXml);
            }
        } else {
            @SuppressWarnings("unchecked")
            List<GPathResult> sortedChildren = childrenPath.list();
            Collections.sort(sortedChildren, sorter);

            for (GPathResult child : sortedChildren) {
                processElement(context, child, resultantXml);
            }
        }
    }


    void processElement(TransformationContext context, GPathResult elem, StringBuilder resultantXml) throws IOException {
        Logging.debug("Starting to process element: %s", elem);
        boolean processChildren = true;
        for (Handler handler : this.handlers.getHandlers().get(context.getCurrentMode())) {
            if (handler.select(context, elem)) {
                handler.handle(context, elem, resultantXml);
                processChildren = false;
                break;
            }
        }
        if (processChildren) {
            Logging.debug("No Handler found for element: %s", elem);
            processChildren(context, elem, resultantXml);
        }
    }
}
