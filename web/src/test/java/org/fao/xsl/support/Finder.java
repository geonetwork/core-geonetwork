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

package org.fao.xsl.support;

import java.util.Arrays;

import org.jdom.Element;
import org.jdom.filter.Filter;

public class Finder implements Filter{
    private static final long serialVersionUID = 1L;
    private final String[] _path;
    private final Requirement _requirement;

    /**
     * @param path a / separated path to match.  The algorithm finds the element that ends with the
     *             indicated path that matche the requirement
     */
    public Finder(String path, Requirement requirement)
    {
        this._path = path.split("/");
        this._requirement = requirement;
    }

    public Finder(String path)
    {
        this(path,Requirement.ACCEPT);
    }

    public boolean matches(Object node)
    {
        if (node instanceof Element) {
            Element e = (Element) node;
            int i = _path.length-1;
            while(i>=0){
                String name = _path[i];
                if(name.contains(":")){
                    String[] split = name.split(":",2);
                    name = split[1];
                }
                if( !e.getName().equals(name) ) return false;
                e = e.getParentElement();
                i--;
            }
            return _requirement.eval((Element) node);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return Arrays.toString(_path)+"["+_requirement+"]";
    }
}