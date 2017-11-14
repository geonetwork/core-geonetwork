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

package org.fao.geonet.kernel.xlink;

import jeeves.xlink.XLink;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by fgravin on 10/26/17.
 */
public final class Utils {


    static boolean isXLink(Element elem)
    {
        if( elem==null ) return false;
        return elem.getAttribute(XLink.HREF, XLink.NAMESPACE_XLINK) != null;
    }

    static <T> List<T> convertToList(Iterator<?> iter, Class<T> class1 ) {
        List<T> placeholders = new ArrayList<T>();
        while( iter.hasNext() ) {
            placeholders.add(class1.cast(iter.next()));
        }
        return placeholders;
    }

}
