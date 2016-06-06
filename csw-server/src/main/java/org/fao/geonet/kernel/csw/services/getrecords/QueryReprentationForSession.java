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

package org.fao.geonet.kernel.csw.services.getrecords;

import org.fao.geonet.util.Sha1Encoder;
import org.jdom.Element;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.utils.Xml;

public class QueryReprentationForSession {

    private final String language;
    private final String query;
    private final String userid;

    public QueryReprentationForSession(ServiceContext context, Element filterExpr) {
        this.language = context.getLanguage();
        this.query = Sha1Encoder.encodeString(Xml.getString(filterExpr));
        this.userid = context.getUserSession().getUserId();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((language == null) ? 0 : language.hashCode());
        result = prime * result + ((query == null) ? 0 : query.hashCode());
        result = prime * result + ((userid == null) ? 0 : userid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        QueryReprentationForSession other = (QueryReprentationForSession) obj;
        if (language == null) {
            if (other.language != null) return false;
        } else if (!language.equals(other.language)) return false;
        if (query == null) {
            if (other.query != null) return false;
        } else if (!query.equals(other.query)) return false;
        if (userid == null) {
            if (other.userid != null) return false;
        } else if (!userid.equals(other.userid)) return false;
        return true;
    }
}
