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

public class RecordProcessingChangeEvent extends AbstractHistoryEvent {

    public static final String FIELD = "process";

    private static final long serialVersionUID = -1052505918706510478L;

    private String process;

    private String xmlRecordBefore, xmlRecordAfter;

    public RecordProcessingChangeEvent(Integer mdId, Integer userId, String xmlRecordBefore, String xmlRecordAfter, String process) {
        super(mdId, userId);
        this.xmlRecordBefore = xmlRecordBefore;
        this.xmlRecordAfter = xmlRecordAfter;
        this.process = process;
    }

    public RecordProcessingChangeEvent(Long mdId, Integer userId, String xmlRecordBefore, String xmlRecordAfter, String process) {
        super(mdId, userId);
        this.xmlRecordBefore = xmlRecordBefore;
        this.xmlRecordAfter = xmlRecordAfter;
        this.process = process;
    }

    @Override
    public String getCurrentState() {
        JSONObject json = new JSONObject();
        json.put(RecordProcessingChangeEvent.FIELD, process);
        json.put("xmlRecord", xmlRecordAfter);

        return json.toString();
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
