/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.util.Base64;
import java.util.Objects;


@Entity
@SequenceGenerator(name = AnonymousAccessLink.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class AnonymousAccessLink {
	private static final String ID_COLUMN_NAME = "id";
	protected static final String ID_SEQ_NAME = "anonymous_access_link_id_seq";

	private int id;
	private int metadataId;
	private String metadataUuid;
	private String hash;


	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
	@Column(name = ID_COLUMN_NAME)
	public int getId() {
		return id;
	}

	@Nonnull
	public AnonymousAccessLink setId(int id) {
		this.id = id;
		return this;
	}

	public int getMetadataId() {
		return metadataId;
	}

	public AnonymousAccessLink setMetadataId(int metadataId) {
		this.metadataId = metadataId;
		return this;
	}

	@Column(unique=true)
	public String getMetadataUuid() {
		return metadataUuid;
	}

	public AnonymousAccessLink setMetadataUuid(String metadataUuid) {
		this.metadataUuid = metadataUuid;
		return this;
	}

	public String getHash() {
		return hash;
	}

	public AnonymousAccessLink setHash(String hash) {
		this.hash = hash;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		AnonymousAccessLink that = (AnonymousAccessLink) o;
		return id == that.id && metadataId == that.metadataId && Objects.equals(metadataUuid, that.metadataUuid) && Objects.equals(hash, that.hash);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, metadataId, metadataUuid, hash);
	}

	public static String getRandomHash() {
		BytesKeyGenerator generator = KeyGenerators.secureRandom(64);
		return Base64.getUrlEncoder().encodeToString(generator.generateKey());
	}
}
