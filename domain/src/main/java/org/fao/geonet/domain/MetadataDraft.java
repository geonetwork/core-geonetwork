package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

import org.apache.lucene.document.Document;
import org.fao.geonet.entitylistener.MetadataDraftEntityListenerManager;

/**
 * This is a normal {@link Metadata} but on its draft version.
 * 
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
}
