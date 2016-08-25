//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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
package org.fao.geonet.api.mapservers;

import org.fao.geonet.domain.MapServer;

/**
 * A Geoserver node configuration
 */
public class GeoServerNode {
    private String id;
    private String name;
    private String url;
    private String namespacePrefix;
    private String namespaceUrl;
    private String username;
    private String userpassword;

    public GeoServerNode(String id, String name, String url,
                         String namespacePrefix, String namespaceUrl, String username,
                         String userPassword) {
        setId(id);
        setName(name);
        setUrl(url);
        setNamespacePrefix(namespacePrefix);
        setNamespaceUrl(namespaceUrl);
        setUsername(username);
        setUserpassword(userPassword);
    }

    public GeoServerNode(MapServer m) {
        setId(String.valueOf(m.getId()));
        setName(m.getName());
        setUrl(m.getConfigurl());
        setNamespacePrefix(m.getNamespacePrefix());
        setNamespaceUrl(m.getNamespace());
        setUsername(m.getUsername());
        setUserpassword(m.getPassword());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }

    public String getNamespaceUrl() {
        return namespaceUrl;
    }

    public void setNamespaceUrl(String namespaceUrl) {
        this.namespaceUrl = namespaceUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserpassword() {
        return userpassword;
    }

    public void setUserpassword(String userpassword) {
        this.userpassword = userpassword;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
