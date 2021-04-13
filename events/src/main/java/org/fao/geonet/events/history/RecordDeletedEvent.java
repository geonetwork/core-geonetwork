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

import java.util.LinkedHashMap;

public class RecordDeletedEvent extends AbstractHistoryEvent {

    private static final long serialVersionUID = 1110999025730522535L;

    private String xmlRecord;
    private String uuid;
    private LinkedHashMap<String, String> titles;

    public RecordDeletedEvent(Integer mdId, String uuid, LinkedHashMap<String, String> titles, Integer userId, String xmlRecord) {
        super(mdId, userId);
        this.xmlRecord = xmlRecord;
        this.uuid=uuid;
        this.titles=titles;
    }

    public RecordDeletedEvent(Long mdId, String uuid, LinkedHashMap<String, String> titles, Integer userId) {
        super(mdId, userId);
        this.uuid=uuid;
        this.titles=titles;
    }

    @Override
    public String getPreviousState() {
        return xmlRecord;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public LinkedHashMap<String, String> getTitles() {
        return titles;
    }

    @Override
    public void publish(ApplicationContext appContext) {
        appContext.publishEvent(this);
    }

}
