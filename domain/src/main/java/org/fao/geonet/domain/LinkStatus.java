/*
 * Copyright (C) 2001-2020 Food and Agriculture Organization of the
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.fao.geonet.domain.converter.BooleanToYNConverter;
import org.fao.geonet.entitylistener.LinkStatusEntityListenerManager;
import org.hibernate.annotations.Type;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Entity representing link status reports.
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = LinkStatus.TABLE_NAME,
    indexes = {@Index(name = "idx_linkstatus_isFailing", columnList = "failing")})
@EntityListeners(LinkStatusEntityListenerManager.class)
@SequenceGenerator(name = LinkStatus.ID_SEQ_NAME, initialValue = 1, allocationSize = 1)
public class LinkStatus extends GeonetEntity implements Comparable<LinkStatus> {
    static final String ID_SEQ_NAME = "linkstatus_id_seq";
    public static final String TABLE_NAME = "LinkStatus";
    public static final String ID_COLUMN_NAME = "id";
    public static final String CHECK_DATE_COLUMN_NAME = "checkDate";
    private int id;
    private Link link;
    private ISODate checkDate = new ISODate();
    private Boolean isFailing = Boolean.TRUE;
    private String statusValue;
    private String statusInfo;
    private String batchKey;


    /**
     * Return the id object of this entity.
     *
     * @return the id object of this entity
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    public int getId() {
        return id;
    }

    public LinkStatus setId(int id) {
        this.id = id;
        return this;
    }

    @ManyToOne(
            cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE},
            fetch = FetchType.EAGER)
    @JoinColumn(name = "link",
            referencedColumnName = "id",
            nullable = false)
    @JsonIgnore
    public Link getLink() {
        return link;
    }

    public LinkStatus setLink(Link link) {
        this.link = link;
        return this;
    }

    /**
     * Get the status for this entity.
     *
     * @return the status for this entity.
     */
    @Column(nullable = false)
    public String getStatusValue() {
        return statusValue;
    }

    /**
     * Set the status for this entity.
     *
     * @param statusValue the status for this entity.
     * @return this entity object
     */
    public LinkStatus setStatusValue(String statusValue) {
        this.statusValue = statusValue;
        return this;
    }

    /**
     * Get the status for this entity.
     *
     * @return the status for this entity.
     */
    @Column(nullable = true)
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Type(type = "org.hibernate.type.TextType")
    public String getStatusInfo() {
        return statusInfo;
    }

    /**
     * Set the status info for this entity.
     *
     * @param statusInfo the status info for this entity.
     * @return this entity object
     */
    public LinkStatus setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
        return this;
    }

    /**
     * The moment that the check was completed.
     *
     * @return The moment that the check completed.
     */

    @AttributeOverride(name = "dateAndTimeUtc",
        column = @Column(name = CHECK_DATE_COLUMN_NAME, length = 30))
    public ISODate getCheckDate() {
        return checkDate;
    }

    /**
     * @return this entity object
     */
    public LinkStatus setCheckDate(ISODate checkDate) {
        this.checkDate = checkDate;
        return this;

    }

    /**
     * eg. HTTP status != 2xx means that the link is ok.
     *
     * @return
     */
    @Column(name = "failing", nullable = false,
        length = 1,
        columnDefinition = "CHAR(1) DEFAULT 'y'")
    @Convert(converter = BooleanToYNConverter.class)
    public Boolean isFailing() {
        if (isFailing == null) {
            this.isFailing = true;
        }
        return isFailing;
    }

    public LinkStatus setFailing(Boolean isFailing) {
        this.isFailing = isFailing;
        return this;
    }

    /**
     * Get the batch key for this entity.
     * This information allows to find if a set of links
     * were checked in the same validation job run.
     *
     * @return the batch key for this entity.
     */
    @Column(nullable = true)
    public String getBatchKey() {
        return batchKey;
    }

    public void setBatchKey(String batchKey) {
        this.batchKey = batchKey;
    }

    @Override
    public String toString() {
        return "LinkStatus{" + id +
            ", link=" + (link == null? "null" : link.getId()) +  // null protection
            ", isFailing=" + isFailing +
            ", checkDate=" + checkDate +
            ", statusValue=" + statusValue +
            ", statusInfo=" + statusInfo +
            ", batchKey=" + batchKey +
            '}';
    }

    @Override
    public int compareTo(LinkStatus linkStatus) {
        return (linkStatus.checkDate.compareTo(this.checkDate));
    }
}
