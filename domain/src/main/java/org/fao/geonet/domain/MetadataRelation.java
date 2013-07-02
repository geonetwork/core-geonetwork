package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 * Tables that links related metadata.
 * 
 * Object is its own entity so that it is easier to add relations without having to load the related metadata.
 * 
 * @author Jesse
 */
@Entity
@Table(name = "relations")
@Access(AccessType.PROPERTY)
public class MetadataRelation {
    private MetadataRelationId id;
    private Metadata metadata;
    private Metadata related;

    @EmbeddedId
    public MetadataRelationId getId() {
        return id;
    }

    public void setId(MetadataRelationId id) {
        this.id = id;
    }

    @MapsId("metadataId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id")
    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
    @MapsId("relatedId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="relatedid", referencedColumnName="id")
    public Metadata getRelated() {
        return related;
    }

    public void setRelated(Metadata related) {
        this.related = related;
    }
}
