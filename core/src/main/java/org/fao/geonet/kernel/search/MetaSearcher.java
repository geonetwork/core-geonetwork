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

package org.fao.geonet.kernel.search;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.services.util.SearchDefaults;
import org.jdom.Document;
import org.jdom.Element;

import java.io.Closeable;
import java.util.List;

//--------------------------------------------------------------------------------
// interface to search metadata
//--------------------------------------------------------------------------------

public abstract class MetaSearcher implements Closeable {
    private int _from, _to;
    private boolean _valid = false;

    //--------------------------------------------------------------------------------
    // MetaSearcher API

    protected static void addElement(Element root, String name, String value) {
        root.addContent(new Element(name).setText(value));
    }

    public abstract void search(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception;

    public abstract Element present(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception;

    public abstract List<Document> presentDocuments(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception;

    public abstract int getSize();

    public abstract Element getSummary() throws Exception;
    
    public abstract long getVersionToken();

    //--------------------------------------------------------------------------------
    // utilities

    public abstract void close();

    protected void initSearchRange(ServiceContext srvContext) {
        // get from and to default values
        _from = 1;
        try {
            Element defaultSearch = SearchDefaults.getDefaultSearch(srvContext, null);
            _to = Integer.parseInt(defaultSearch.getChildText(Geonet.SearchResult.HITS_PER_PAGE));
        } catch (Exception e) {
            _to = 10;
        }
    }

    protected void updateSearchRange(Element request) {
        // get request parameters
        _from = readFrom(request);
        _to = readTo(request);


        int count = getSize();

        if (_from > count) {
            //The search is out of scope
            _from = count + 1;
            _to = count;
        } else {
            _from = _from > count ? count : _from;
            _to = _to > count ? count : _to;
        }
    }

    protected int readFrom(Element request) {
        return loadParam(request, "from", _from);
    }

    protected int readTo(Element request) {
        return loadParam(request, "to", _to);
    }

    private int loadParam(Element request, String name, int defaultVal) {
        String sFrom = request.getChildText(name);
        if (sFrom != null) {
            try {
                return Integer.parseInt(sFrom);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Bad '" + name + "' parameter: " + sFrom);
            }
        }
        return defaultVal;
    }

    protected int getFrom() {
        return _from;
    }

    protected int getTo() {
        return _to;
    }

    protected boolean isValid() {
        return _valid;
    }

    public Element get(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {
        String id = Util.getParam(request, Params.ID);

        // save _from and _to
        int from = _from;
        int to = _to;

        // perform search
        Element req = new Element("request");
        addElement(req, "from", id);
        addElement(req, "to", id);
        Element result = present(srvContext, req, config);

        // restore _from and _to
        _from = from;
        _to = to;

        // skip summary
        for (Object o : result.getChildren()) {
            Element child = (Element) o;

            if (!child.getName().equals(Geonet.Elem.SUMMARY)) {
                return child;
            }
        }
        return null;
    }
}

