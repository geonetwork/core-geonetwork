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

package org.fao.geonet.domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "url", "typeName" }))
@SequenceGenerator(name = MessageProducerEntity.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class MessageProducerEntity {
    public static final String ID_SEQ_NAME = "message_producer_entity_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    private Long id;

    private WfsHarvesterParamEntity wfsHarvesterParamEntity;

    @Nullable private String cronExpression;

    public WfsHarvesterParamEntity getWfsHarvesterParam() {
        return wfsHarvesterParamEntity;
    }

    public void setWfsHarvesterParam(WfsHarvesterParamEntity wfsHarvesterParamEntity) {
        this.wfsHarvesterParamEntity = wfsHarvesterParamEntity;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(@Nullable String cronExpession) {
        this.cronExpression = cronExpession;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
