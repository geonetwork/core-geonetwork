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
import org.springframework.context.ApplicationEvent;

public abstract class AbstractHistoryEvent extends ApplicationEvent {

    private static final long serialVersionUID = 456874566246220509L;

    private Long mdId;
    private Integer userId;

    public AbstractHistoryEvent(Integer mdId, Integer userId) {
        super(mdId);
        if (mdId == null) {
            throw new NullPointerException("Metadata UUID cannot be null");
        }
        this.mdId = new Long(mdId);
        this.userId = userId;
    }

    public AbstractHistoryEvent(Long mdId, Integer userId) {
        super(mdId);
        if (mdId == null) {
            throw new NullPointerException("Metadata UUID cannot be null");
        }
        this.mdId = mdId;
        this.userId = userId;
    }

    public abstract String getCurrentState();

    public Long getMdId() {
        return mdId;
    }

    public abstract String getPreviousState();

    public Integer getUserId() {
        return userId;
    }

    public abstract void publish(ApplicationContext appContext);

}
