package org.fao.geonet.domain;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
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
}
