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

public class RecordUpdatedEvent extends AbstractHistoryEvent {

    private static final long serialVersionUID = 1110999025730522535L;

    private String xmlRecordBefore, xmlRecordAfter;

    public RecordUpdatedEvent(Integer mdId, Integer userId, String xmlRecordBefore, String xmlRecordAfter) {
        super(mdId, userId);
        this.xmlRecordBefore = xmlRecordBefore;
        this.xmlRecordAfter = xmlRecordAfter;
    }

    public RecordUpdatedEvent(Long mdId, Integer userId, String xmlRecordBefore, String xmlRecordAfter) {
        super(mdId, userId);
        this.xmlRecordBefore = xmlRecordBefore;
        this.xmlRecordAfter = xmlRecordAfter;
    }

    @Override
    public String getCurrentState() {
        return xmlRecordAfter;
    }

    @Override
    public String getPreviousState() {
        return xmlRecordBefore;
    }

    @Override
    public void publish(ApplicationContext appContext) {
        appContext.publishEvent(this);
    }

}
