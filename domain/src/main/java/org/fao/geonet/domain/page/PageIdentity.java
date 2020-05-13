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
package org.fao.geonet.domain.page;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class PageIdentity implements Serializable {

    /**
        *
        */

    private static final long serialVersionUID = 1L;
    private String language;
    private String linkText;

    public PageIdentity() {

    }

    public PageIdentity(String language, String linkText) {
        super();
        this.language = language;
        this.linkText = linkText;
    }

    public String getLanguage() {
        return language;
    }

    public String getLinkText() {
        return linkText;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PageIdentity that = (PageIdentity) o;

        if (!language.equals(that.language)) {
            return false;
        }
        return linkText.equals(that.linkText);
    }

    @Override
    public int hashCode() {
        int result = language.hashCode();
        result = 31 * result + linkText.hashCode();
        return result;
    }

}
