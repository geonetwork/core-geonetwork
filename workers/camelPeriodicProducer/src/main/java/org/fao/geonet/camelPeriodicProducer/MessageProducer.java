/*
 * Copyright (C) 2001-2015 Food and Agriculture Organization of the
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

package org.fao.geonet.camelPeriodicProducer;

public class MessageProducer<M> {

    private Long id;
    private M message;
    private String cronExpession;
    private String targetUri;

    public MessageProducer<M> setTarget(String targetUri) {
        this.targetUri = targetUri;
        return this;
    }

    public Long getId() {
        return this.id;
    }

    public MessageProducer<M> setId(Long id) {
        this.id = id;
        return this;
    }

    public String getCronExpession() {
        return this.cronExpession;
    }

    public MessageProducer<M> setCronExpession(String cronExpession) {
        this.cronExpession = cronExpession;
        return this;
    }

    public M getMessage() {
        return message;
    }

    public MessageProducer<M> setMessage(M message) {
        this.message = message;
        return this;
    }

    public String getTargetUri() {
        return targetUri;
    }
}
