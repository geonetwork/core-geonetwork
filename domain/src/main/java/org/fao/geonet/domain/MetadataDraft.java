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

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.lucene.document.Document;
import org.fao.geonet.entitylistener.MetadataDraftEntityListenerManager;

/**
 * This is a normal {@link Metadata} but on its draft version.
 * <p>
 * Privileges, categories and non-XML properties will not be stored.
 *
 * @author María Arias de Reyna
 */
@Entity
@Table(name = MetadataDraft.TABLENAME)
@Access(AccessType.PROPERTY)
@EntityListeners(MetadataDraftEntityListenerManager.class)
public class MetadataDraft extends AbstractMetadata implements Serializable {

	private static final long serialVersionUID = -1933627969445820867L;
	public static final String TABLENAME = "MetadataDraft";

	public MetadataDraft() {
	}

	public static MetadataDraft createFromLuceneIndexDocument(Document doc) {
		MetadataDraft metadata = new MetadataDraft();
		transform(doc, metadata);
		return metadata;
	}

	public Metadata approvedVersion;

	@Nonnull
    @JoinColumn(nullable=false, updatable=false, unique=true, insertable=true)
    @OneToOne(fetch=FetchType.EAGER, optional=false, orphanRemoval=false)
    public Metadata getApprovedVersion() {
		return approvedVersion;
	}

	public void setApprovedVersion(Metadata approvedVersion) {
		this.approvedVersion = approvedVersion;
	}

}
