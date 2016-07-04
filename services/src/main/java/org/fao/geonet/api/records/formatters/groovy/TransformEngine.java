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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import groovy.util.slurpersupport.GPathResult;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

            List<GPathResult> flattenedSelection = flattenGPathResults(selection);
            processChildren(context, sortByElement, flattenedSelection, resultantXml);

            return resultantXml.toString();
        } finally {
            context.setCurrentMode(oldMode);
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

    Collection<GPathResult> processElement(TransformationContext context, GPathResult elem, List<GPathResult> siblings, StringBuilder resultantXml) throws IOException {
        @SuppressWarnings("unchecked")
        List<GPathResult> handledSiblings = Lists.newArrayList(elem);
        Logging.debug("Starting to process element: %s", elem);
        boolean processChildren = true;
        for (SkipElement skipElement : this.handlers.getSkipElements()) {
            if (skipElement.select(context, elem)) {
                processChildren(context, elem, processSkipElements(elem, skipElement), resultantXml);
                processChildren = false;
            }
        }

        if (processChildren) {
            for (Handler handler : this.handlers.getHandlers().get(context.getCurrentMode())) {
                if (handler.select(context, elem)) {
                    if (handler.isGroup()) {
                        for (GPathResult sibling : siblings) {
                            if (handler.select(context, sibling)) {
                                handledSiblings.add(sibling);
                            }
                        }
                    }
                    handler.handle(context, handledSiblings, resultantXml);
                    processChildren = false;
                    break;
                }
            }
        }

        if (processChildren) {
            Logging.debug("No Handler found for element: %s", elem);
            processChildren(context, elem, processSkipElements(elem, null), resultantXml);
        }

        return handledSiblings;
    }

    private void processChildren(TransformationContext context, GPathResult md, List<GPathResult> sortedChildren,
                                 StringBuilder resultantXml) throws IOException {
        if (sortedChildren.isEmpty()) {
            return;
        }

        if (md != null) {
            Sorter sorter = this.handlers.findSorter(context, md);
            if (sorter != null) {
                Collections.sort(sortedChildren, sorter);
            }
        }

        final Set<GPathResult> visitedByGroup = Sets.newIdentityHashSet();
        int size = sortedChildren.size();
        for (int i = 0; i < size; i++) {
            GPathResult child = sortedChildren.get(i);

            if (!visitedByGroup.contains(child)) {
                visitedByGroup.addAll(processElement(context, child, sortedChildren.subList(i + 1, size), resultantXml));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<GPathResult> processSkipElements(GPathResult md, SkipElement skipElement) {
        GPathResult childrenPath;
        if (skipElement == null) {
            childrenPath = md.children();
        } else {
            childrenPath = skipElement.selectChildren(md);
        }
        if (Logging.isDebugMode()) {
            Logging.debug("Starting to process %2$d children of: %1$s.", md, childrenPath.size());
        }
        return childrenPath.list();
    }

}
