//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.geonet.v21_3;

import com.google.common.base.Splitter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.harvest.harvester.geonet.BaseSearch;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.util.Iterator;

//=============================================================================

class Search extends BaseSearch {

    public boolean digital;
    public boolean hardcopy;
    public String sourceName;
    public String anyField;
    public String anyValue;

    public Search() {
        super();
    }

    public Search(Element search) throws BadParameterEx {
        super(search);
        digital = Util.getParam(search, "digital", false);
        hardcopy = Util.getParam(search, "hardcopy", false);
        anyField = Util.getParam(search, "anyField", "");
        anyValue = Util.getParam(search, "anyValue", "");

        Element source = search.getChild("source");
        sourceName = Util.getParam(source, "name", "");
    }

    public static Search createEmptySearch(int from, int to) throws BadParameterEx {
        Search s = new Search(new Element("search"));
        s.setRange(from, to);
        return s;
    }

    public Search copy() {
        Search s = new Search();

        s.freeText = freeText;
        s.title = title;
        s.abstrac = abstrac;
        s.keywords = keywords;
        s.digital = digital;
        s.hardcopy = hardcopy;
        s.sourceUuid = sourceUuid;
        s.sourceName = sourceName;
        s.anyField = anyField;
        s.anyValue = anyValue;
        s.from = from;
        s.to = to;

        return s;
    }

    public Element createRequest() {
        Element req = new Element("request");
        add(req, "from", Integer.toString(from));
        add(req, "to", Integer.toString(to));
        add(req, "any", freeText);
        add(req, "title", title);
        add(req, "abstract", abstrac);
        add(req, "themekey", keywords);
        add(req, "siteId", sourceUuid);

        try {
            Iterable<String> fields = Splitter.on(';').split(anyField);
            Iterable<String> values = Splitter.on(';').split(anyValue);
            Iterator<String> valuesIterator = values.iterator();
            for (String field : fields) {
                String value = valuesIterator.next();
                if (field != null && value != null) {
                    add(req, field, value);
                }
            }
        } catch (Exception e) {
            throw new OperationAbortedEx("Search request criteria error. " +
                "Check that the free criteria fields '" +
                anyField + "' and values '" +
                anyValue + "' are correct. You MUST have the same " +
                "number of criteria and values.", e);
        }

        if (digital)
            Lib.element.add(req, "digital", "on");

        if (hardcopy)
            Lib.element.add(req, "paper", "on");

        if (Log.isDebugEnabled(Geonet.HARVEST_MAN)) {
            Log.debug(Geonet.HARVEST_MAN, "Search request is " + Xml.getString(req));
        }

        return req;
    }

    private void add(Element req, String name, String value) {
        if (!value.isEmpty())
            req.addContent(new Element(name).setText(value));
    }

    public void setRange(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("from", from)
            .append("to", to)
            .append("freeText", freeText)
            .append("title", title)
            .append("abstrac", abstrac)
            .append("keywords", keywords)
            .append("digital", digital)
            .append("hardcopy", hardcopy)
            .append("sourceUuid", sourceUuid)
            .append("sourceName", sourceName)
            .append("anyField", anyField)
            .append("anyValue", anyValue)
            .toString();
    }
}



