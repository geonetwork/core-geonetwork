//=============================================================================
//===	Copyright (C) 2001-2010 Food and Agriculture Organization of the
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
package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * INSPIRE Atom feed entry model class.
 *
 * @author Jose García
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class InspireAtomFeedEntry  implements Serializable {
    private int _id;
    private String _title;

    private String _lang;
    private String _type;
    private String _crs;

    private String _url;

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    @Column(length = 255)
    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        this._title = title;
    }

    @Column(length = 3)
    public String getLang() {
        return _lang;
    }

    public void setLang(String lang) {
        this._lang = lang;
    }

    @Column(length = 255)
    public String getType() {
        return _type;
    }

    public void setType(String type) {
        this._type = type;
    }

    @Column(length = 255)
    public String getCrs() {
        return _crs;
    }

    public void setCrs(String crs) {
        this._crs = crs;
    }

    @Column(length = 255)
    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        this._url = url;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InspireAtomFeedEntry)) return false;

        InspireAtomFeedEntry that = (InspireAtomFeedEntry) o;

        if (_id != that._id) return false;
        if (_crs != null ? !_crs.equals(that._crs) : that._crs != null) return false;
        if (_lang != null ? !_lang.equals(that._lang) : that._lang != null) return false;
        if (_title != null ? !_title.equals(that._title) : that._title != null) return false;
        if (_type != null ? !_type.equals(that._type) : that._type != null) return false;
        if (_url != null ? !_url.equals(that._url) : that._url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _id;
        result = 31 * result + (_title != null ? _title.hashCode() : 0);
        result = 31 * result + (_lang != null ? _lang.hashCode() : 0);
        result = 31 * result + (_type != null ? _type.hashCode() : 0);
        result = 31 * result + (_crs != null ? _crs.hashCode() : 0);
        result = 31 * result + (_url != null ? _url.hashCode() : 0);
        return result;
    }
}