//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.util;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;

public class ElementFinder implements Filter
{
    private static final long serialVersionUID = 1L;
    private final String            elemName;
    private final transient Namespace         ns;
    private final String            parentName;

    public ElementFinder(String name, Namespace ns, String parent)
    {
        this.elemName = name;
        this.ns = ns;
        this.parentName = parent;
    }

    public boolean matches(Object obj)
    {
        if (obj instanceof Element) {
            Element elem = (Element) obj;

            boolean sameName = elem.getName().equals(elemName);
            boolean sameNS = ns == elem.getNamespace() || (ns != null && ns.equals(elem.getNamespace()));
            boolean sameParent = parentName.equals("*") || elem.getParentElement().getName().equals(parentName);
            boolean otherChecks = otherChecks(elem);
            return sameName && sameNS && sameParent && otherChecks;
        }
        return false;
    }

    protected boolean otherChecks(Element elem)
    {
        return true;
    }

}