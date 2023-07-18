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

package org.fao.geonet.util.xml;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;

/**
 * Adaptation of JDOM2's NamespaceAware implementation in Element, to apply to JDOM1 Elements.
 *
 * To avoid a need to wrap JDOM1 Element everywhere in GN, these methods are adapted to take a
 * (JDOM1) Element as parameter instead.
 *
 * Adapted code from https://github.com/hunterhacker/jdom/blob/master/core/src/java/org/jdom2/Element.java.
 *
 * When GeoNetwork upgrades to use JDOM2, this class becomes obsolete. Its usages should then be
 * replaced by directly using the same methods on your JDOM Elements.
 *
 * @author heikki doeleman
 */
public class NamespaceUtils {

    /**
     * Get the Namespaces that are in-scope on this Element. Element has the most complex rules for
     * the namespaces-in-scope. <p> The scope is built up from a number of sources following the
     * rules of XML namespace inheritence as follows: <ul> <li>The {@link Namespace#XML_NAMESPACE}
     * is added <li>The element's namespace is added (commonly {@link Namespace#NO_NAMESPACE})
     * <li>All the attributes are inspected and their Namespaces are included <li>All Namespaces
     * declared on this Element using addNamespaceDeclaration(Namespace) are included. <li>If the
     * element has a parent then the parent's Namespace scope is inspected, and any prefixes in the
     * parent scope that are not yet bound in this Element's scope are included. <li>If the default
     * Namespace (the no-prefix namespace) has not been encountered for this Element then {@link
     * Namespace#NO_NAMESPACE} is included. </ul> The Element's Namespace scope consist of it's
     * inherited Namespaces and any modifications to that scope derived from the Element itself. If
     * the element is detached then it's inherited scope consists of just If an element has no
     * parent then <p> Note that the Element's Namespace will always be reported first.
     */
    public static List<Namespace> getNamespacesInScope(Element element) {
        // The assumption here is that all namespaces are valid, that there are no namespace collisions on this element

        // This method is also the 'anchor' of the three getNamespaces*() methods
        // It does not make reference to this Element instance's other getNamespace*() methods

        TreeMap<String, Namespace> namespaces = new TreeMap<String, Namespace>();
        namespaces.put(Namespace.XML_NAMESPACE.getPrefix(), Namespace.XML_NAMESPACE);
        namespaces.put(element.getNamespacePrefix(), element.getNamespace());
        if (element.getAdditionalNamespaces() != null) {
            for (Object nso : element.getAdditionalNamespaces()) {
                Namespace ns = (Namespace) nso;
                if (!namespaces.containsKey(ns.getPrefix())) {
                    namespaces.put(ns.getPrefix(), ns);
                }
            }
        }
        if (element.getAttributes() != null) {
            for (Object attO : element.getAttributes()) {
                Attribute att = (Attribute) attO;
                Namespace ns = att.getNamespace();
                if (!namespaces.containsKey(ns.getPrefix())) {
                    namespaces.put(ns.getPrefix(), ns);
                }
            }
        }
        // Right, we now have all the namespaces that are current on this Element.
        // Include any other namespaces that are inherited.
        final Element pnt = element.getParentElement();
        if (pnt != null) {
            for (Namespace ns : getNamespacesInScope(pnt)) {
                if (!namespaces.containsKey(ns.getPrefix())) {
                    namespaces.put(ns.getPrefix(), ns);
                }
            }
        }

        if (pnt == null && !namespaces.containsKey("")) {
            // we are the root element, and there is no 'default' namespace.
            namespaces.put(Namespace.NO_NAMESPACE.getPrefix(), Namespace.NO_NAMESPACE);
        }

        ArrayList<Namespace> al = new ArrayList<Namespace>(namespaces.size());
        al.add(element.getNamespace());
        namespaces.remove(element.getNamespacePrefix());
        al.addAll(namespaces.values());

        return Collections.unmodifiableList(al);
    }

    /**
     * Obtain a list of all namespaces that are in scope for this content, but were not introduced
     * by this content. <p> The contents of this list will always be a subset (but in the same
     * order) of getNamespacesInScope(), and will never intersect getNamspacesIntroduced().
     *
     * @return a read-only list of Namespaces.
     */
    public static List<Namespace> getNamespacesInherited(Element element) {
        if (element.getParentElement() == null) {
            List<Namespace> ret = new ArrayList<Namespace>(getNamespacesInScope(element));
            for (Iterator<Namespace> it = ret.iterator(); it.hasNext(); ) {
                Namespace ns = it.next();
                if (ns == Namespace.NO_NAMESPACE || ns == Namespace.XML_NAMESPACE) {
                    continue;
                }
                it.remove();
            }
            return Collections.unmodifiableList(ret);
        }

        // OK, the things we inherit are the prefixes we have in scope that are also in our parent's scope.
        Map<String, Namespace> parents = new HashMap<String, Namespace>();
        for (Namespace ns : getNamespacesInScope(element.getParentElement())) {
            parents.put(ns.getPrefix(), ns);
        }

        ArrayList<Namespace> al = new ArrayList<Namespace>();
        for (Namespace ns : getNamespacesInScope(element)) {
            if (ns == parents.get(ns.getPrefix())) {
                // inherited
                al.add(ns);
            }
        }

        return Collections.unmodifiableList(al);
    }

    /**
     * Obtain a list of all namespaces that are introduced to the XML tree by this node. Only
     * Elements and Attributes can introduce namespaces, so all other Content types will return an
     * empty list. <p> The contents of this list will always be a subset (but in the same order) of
     * getNamespacesInScope(), and will never intersect getNamspacesInherited().
     *
     * @return a read-only list of Namespaces.
     */
    public static List<Namespace> getNamespacesIntroduced(Element element) {
        if (element.getParentElement() == null) {
            // we introduce everything... except Namespace.XML_NAMESPACE
            List<Namespace> ret = new ArrayList<Namespace>(getNamespacesInScope(element));
            for (Iterator<Namespace> it = ret.iterator(); it.hasNext(); ) {
                Namespace ns = it.next();
                if (ns == Namespace.XML_NAMESPACE || ns == Namespace.NO_NAMESPACE) {
                    it.remove();
                }
            }
            return Collections.unmodifiableList(ret);
        }

        // OK, the things we introduce are the prefixes we have in scope that are *not* in our parent's scope.
        Map<String, Namespace> parents = new HashMap<String, Namespace>();
        for (Namespace ns : getNamespacesInScope(element.getParentElement())) {
            parents.put(ns.getPrefix(), ns);
        }

        ArrayList<Namespace> al = new ArrayList<Namespace>();
        for (Namespace ns : getNamespacesInScope(element)) {
            if (!parents.containsKey(ns.getPrefix()) || ns != parents.get(ns.getPrefix())) {
                // introduced
                al.add(ns);
            }
        }

        return Collections.unmodifiableList(al);
    }

}
