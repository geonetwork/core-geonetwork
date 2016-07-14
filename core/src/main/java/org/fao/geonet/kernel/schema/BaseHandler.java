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

package org.fao.geonet.kernel.schema;

import org.jdom.Attribute;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heikki doeleman
 */
public abstract class BaseHandler {

    /**
     * TODO Javadoc.
     */
    protected void handleSequence(Element elChild, ArrayList<ElementEntry> alElements, ElementInfo ei) {
        @SuppressWarnings("unchecked")
        List<Element> sequence = elChild.getChildren();

        for (Element elElem : sequence) {

            if (isChoiceOrElementOrGroupOrSequence(elElem)) {
                alElements.add(new ElementEntry(elElem, ei.file, ei.targetNS, ei.targetNSPrefix));
            } else {
                Logger.log();
            }
        }
    }

    /**
     * TODO Javadoc.
     */
    protected boolean isChoiceOrElementOrGroupOrSequence(Element elElem) {
        return elElem.getName().equals("choice") || elElem.getName().equals("element") ||
            elElem.getName().equals("group") || elElem.getName().equals("sequence");
    }

    /**
     * TODO Javadoc.
     */
    protected String handleAttribs(ElementInfo ei, String name) {
        @SuppressWarnings("unchecked")
        List<Attribute> attribs = ei.element.getAttributes();
        for (Attribute at : attribs) {

            String attrName = at.getName();
            if (attrName.equals("name")) {
                name = at.getValue();
                if ((name.indexOf(':') == -1) && (ei.targetNSPrefix != null)) {
                    name = ei.targetNSPrefix + ":" + at.getValue();
                }
            } else {
                Logger.log();
            }
        }
        return name;
    }
}
