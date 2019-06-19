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

package org.fao.geonet.events.md.sharing;

import org.fao.geonet.domain.OperationAllowed;
import org.springframework.context.ApplicationEvent;

/**
 * Event launched when a metadata sharing/privileges is modified
 *
 * @author delawen
 */
public class MetadataShare extends ApplicationEvent {

    private static final long serialVersionUID = -748471747316454884L;

    public enum Type {
        ADD, UPDATE, REMOVE
    }

    private OperationAllowed op;
    private Type type;
    private Integer record;


    public MetadataShare(OperationAllowed op, Type type) {
        super(op);
        this.setOp(op);
        this.setType(type);
        this.setRecord(op.getId().getMetadataId());
    }

    public OperationAllowed getOp() {
        return op;
    }

    private void setOp(OperationAllowed op) {
        this.op = op;
    }

    public Type getType() {
        return type;
    }

    private void setType(Type type) {
        this.type = type;
    }
    
    private void setRecord(Integer record) {
		this.record = record;
	}
    
    public Integer getRecord() {
		return record;
	}

}
