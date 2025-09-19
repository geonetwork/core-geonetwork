package org.fao.geonet.domain;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;


@Entity
@SequenceGenerator(name = AnonymousAccessLink.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class AnonymousAccessLink {
	private int _id;
	private static final String ID_COLUMN_NAME = "id";
	protected static final String ID_SEQ_NAME = "anonymous_access_link_id_seq";


	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
	@Column(name = ID_COLUMN_NAME)
	public int getId() {
		return _id;
	}

	@Nonnull
	public AnonymousAccessLink setId(int id) {
		this._id = id;
		return this;
	}

}
