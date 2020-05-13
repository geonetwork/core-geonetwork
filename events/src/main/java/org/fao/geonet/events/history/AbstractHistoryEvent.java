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
import org.springframework.context.ApplicationEvent;

public abstract class AbstractHistoryEvent extends ApplicationEvent {

    private static final long serialVersionUID = 456874566246220509L;

    private Long mdId;
    private Integer userId;

    /**
     * Constructor used for the current type of mdId
     *
     * @param mdId
     * @param userId
     */
    protected AbstractHistoryEvent(Integer mdId, Integer userId) {
        super(mdId);
        this.mdId = new Long(mdId);
        this.userId = userId;
    }

    /**
     * Constructor for the long version of mdId
     *
     * @param mdId
     * @param userId
     */
    protected AbstractHistoryEvent(Long mdId, Integer userId) {
        super(mdId);
        this.mdId = mdId;
        this.userId = userId;
    }

    /**
     * A String represention of the state of the item after the changes Can be a primitive type, a JSON or an XML to String
     *
     * @return
     */
    public String getCurrentState() {
        return null;
    }

    /**
     * Numeric id identifier of the metadata
     *
     * @return
     */
    public Long getMdId() {
        return mdId;
    }

    /**
     * A String represention of the previous state of the item Can be a primitive type, a JSON or an XML to String
     *
     * @return
     */
    public String getPreviousState() {
        return null;
    }

    /**
     * The user id of the author of this change
     *
     * @return
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Publish the specific event
     *
     * @param appContext
     */
    public abstract void publish(ApplicationContext appContext);

}
