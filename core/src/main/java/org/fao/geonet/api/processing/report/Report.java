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

package org.fao.geonet.api.processing.report;

import org.fao.geonet.domain.ISODate;

import javax.xml.bind.annotation.XmlElement;

/**
 * A simple report.
 */
public abstract class Report {
    private String message;
    private String uuid;
    private boolean draft;
    private boolean approved;
    private ISODate date;

    public Report(String message) {
        this.message = message;
        this.date = new ISODate();
    }

    public String getMessage() {
        return message;
    }

    public Report setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public Report setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public boolean isDraft() {
        return draft;
    }

    public Report setDraft(boolean draft) {
        this.draft = draft;
        return this;
    }

    public boolean isApproved() {
        return approved;
    }

    public Report setApproved(boolean approved) {
        this.approved = approved;
        return this;
    }

    @XmlElement(name = "datetime")
    public String getDate() {
        return date.getDateAndTime();
    }

    public Report setDate(ISODate date) {
        this.date = date;
        return this;
    }
}
