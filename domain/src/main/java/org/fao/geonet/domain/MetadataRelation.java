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

    /**
     * Get the metadata relation id object.
     * 
     * @return the metadata relation id object.
     */
    @EmbeddedId
    public MetadataRelationId getId() {
        return id;
    }

    /**
     * Set the metadata relation id object.
     * 
     * @param id the metadata relation id object.
     */
    public void setId(MetadataRelationId id) {
        this.id = id;
    }

    /**
     * Get the one of the metadata in the relationship.
     * 
     * @return the one of the metadata in the relationship.
     */
    @MapsId("metadataId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id")
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Set the one of the metadata in the relationship.
     * 
     * @param metadata the one of the metadata in the relationship.
     */
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Get the second of the related metadata.
     * 
     * @return the second of the related metadata.
     */
    @MapsId("relatedId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relatedid", referencedColumnName = "id")
    public Metadata getRelated() {
        return related;
    }

    /**
     * Set the second of the related metadata.
     * 
     * @param related the second of the related metadata.
     */
    public void setRelated(Metadata related) {
        this.related = related;
    }
}
