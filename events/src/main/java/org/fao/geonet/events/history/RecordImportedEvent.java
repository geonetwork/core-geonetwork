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

package org.fao.geonet.events.history;

import org.springframework.context.ApplicationContext;

import net.sf.json.JSONObject;

public class RecordImportedEvent extends AbstractHistoryEvent {

    public static final String FIELD = "user";

    private static final long serialVersionUID = 1110999025730522535L;

    private JSONObject userObject;

    private String xmlRecord;

    public RecordImportedEvent(Integer mdId, Integer userId, JSONObject userObject, String xmlRecord) {
        super(mdId, userId);
        this.userObject = userObject;
        this.xmlRecord = xmlRecord;
    }

    public RecordImportedEvent(Long mdId, Integer userId, JSONObject userObject, String xmlRecord) {
        super(mdId, userId);
        this.userObject = userObject;
        this.xmlRecord = xmlRecord;
    }

    @Override
    public String getCurrentState() {
        JSONObject json = new JSONObject();
        json.put("userObject", userObject);
        json.put("xmlRecord", xmlRecord);

        return json.toString();
    }

    @Override
    public void publish(ApplicationContext appContext) {
        appContext.publishEvent(this);
    }

}
