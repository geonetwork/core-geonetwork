//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.server;

import jeeves.constants.ConfigFile;

import org.jdom.Element;

import java.util.Hashtable;
import java.util.List;

//=============================================================================

public class ServiceConfig {
    private Hashtable<String, String> values = new Hashtable<String, String>();
    private Hashtable<String, Element> elements = new Hashtable<String, Element>();

    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    public ServiceConfig() {
    }

    //--------------------------------------------------------------------------

    public ServiceConfig(List<Element> params) {
        for (int i = 0; i < params.size(); i++) {
            Element param = params.get(i);

            String name = param.getAttributeValue(ConfigFile.Param.Attr.NAME);
            String value = param.getAttributeValue(ConfigFile.Param.Attr.VALUE);

            if (name == null)
                throw new IllegalArgumentException("Missing 'name' attrib in parameter");

            if (value != null) values.put(name, value);
            elements.put(name, param);
        }
    }

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    public String getValue(String name) {
        return values.get(name);
    }

    public void setValue(String name, String value) {
        values.put(name, value);
    }

    //--------------------------------------------------------------------------

    public String getValue(String name, String def) {
        String value = getValue(name);

        if (value == null)
            value = def;

        return value;
    }

    //--------------------------------------------------------------------------

    public String getMandatoryValue(String name) {
        String value = getValue(name);

        if (value == null)
            throw new IllegalArgumentException("Missing '" + name + "' parameter");

        return value;
    }

    //--------------------------------------------------------------------------

    public List<Element> getChildren(String paramName) {
        return getChildren(paramName, null);
    }

    //--------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public List<Element> getChildren(String paramName, String elemName) {
        Element elem = elements.get(paramName);

        if (elem == null)
            return null;
        else {
            if (elemName == null)
                return ((List<Element>) elem.getChildren());
            else
                return ((List<Element>) elem.getChildren(elemName));
        }
    }
}

//=============================================================================

