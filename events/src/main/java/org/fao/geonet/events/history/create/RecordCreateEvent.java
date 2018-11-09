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

package org.fao.geonet.events.history.create;

import org.springframework.context.ApplicationContext;

public class RecordCreateEvent extends AbstractHistoryEvent {

    private static final long serialVersionUID = 1110999025730522535L;

    // Status related fields
    private String userObject, record;

    private RecordCreateEvent(Integer mdId, Integer userId) {
        super(mdId, userId);
    }

    public RecordCreateEvent(Integer mdId, Integer userId, String userObject, String record) {
        super(mdId, userId);
        setUserObject(userObject);
        setRecord(record);
    }

    private RecordCreateEvent(Long mdId, Integer userId) {
        super(mdId, userId);
    }

    public RecordCreateEvent(Long mdId, Integer userId, String userObject, String record) {
        super(mdId, userId);
        setUserObject(userObject);
        setRecord(record);
    }

    @Override
    public String getCurrentState() {
        return null;
    }

    @Override
    public String getPreviousState() {
        return null;
    }

    public String getRecord() {
        return record;

    }

    public String getUserObject() {
        return userObject;

    }

    @Override
    public void publish(ApplicationContext appContext) {
        appContext.publishEvent(this);
    }

    public void setRecord(String record) {
        this.record = record;

    }

    public void setUserObject(String userObject) {
        this.userObject = userObject;

    }

}
