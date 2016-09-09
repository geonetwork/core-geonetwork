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

package jeeves.config.springutil;

import org.fao.geonet.NodeInfo;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * An authentications details object that adds extra information for multinode security.
 *
 * User: Jesse Date: 12/4/13 Time: 8:14 AM
 */
public class JeevesAuthenticationDetails extends WebAuthenticationDetails {
    private final NodeInfo _nodeInfo;

    public JeevesAuthenticationDetails(HttpServletRequest context, NodeInfo nodeInfo) {
        super(context);
        this._nodeInfo = nodeInfo;
    }

    public String getNodeId() {
        return _nodeInfo.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        JeevesAuthenticationDetails that = (JeevesAuthenticationDetails) o;

        if (_nodeInfo != null ? !_nodeInfo.equals(that._nodeInfo) : that._nodeInfo != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (_nodeInfo != null ? _nodeInfo.hashCode() : 0);
        return result;
    }
}
