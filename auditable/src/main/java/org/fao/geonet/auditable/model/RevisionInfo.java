/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
package org.fao.geonet.auditable.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.fao.geonet.domain.ISODate;

public class RevisionInfo {
    private final int revisionNumber;
    private final String user;
    private final String date;
    private final String value;
    private final List<RevisionFieldChange> changes;

    public RevisionInfo(int revisionNumber, String user, Date date, String value) {
        this.revisionNumber = revisionNumber;
        this.user = user;
        this.date = new ISODate(date.getTime()).toString();
        this.value = value;
        this.changes = new ArrayList<>();
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public String getUser() {
        return user;
    }

    public String getDate() {
        return date;
    }

    public String getValue() {
        return value;
    }

    /**
     * @return an unmodifiable view of the list of changes.
     */
    public List<RevisionFieldChange> getChanges() {
        return Collections.unmodifiableList(changes);
    }

    public void addChange(RevisionFieldChange change) {
        changes.add(change);
    }
}
