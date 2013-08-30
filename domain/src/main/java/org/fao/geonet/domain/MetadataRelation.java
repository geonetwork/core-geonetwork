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
 * <p>
 *     Object is its own entity so that it is easier to add relations without having to load the related metadata.
 * </p>
 * <p>
 *     Note: It is important that both Metadata are managed (have been saved or loaded from the MetadataRepository.)
 *     For example:
 *     <pre><code>
 *      Metadata metadata1 = _metadataRepo.findOne(id);
 *      Metadata metadata2 = _metadataRepo.findOne(id2);
 *      new MetadataRelation(metadata1, metadata2);
 *     </code></pre>
 * </p>
 * @author Jesse
 */
@Entity
@Table(name = "relations")
@Access(AccessType.PROPERTY)
public class MetadataRelation {
    private MetadataRelationId _id = new MetadataRelationId();
    private Metadata _metadata;
    private Metadata _related;

    /**
     * Default constructor, required by JPA.
     */
    public MetadataRelation() {

    }

    /**
     * Convenience constructor.
     *
     * @param metadata1 metadata on one end of relationship
     * @param related metadata on other end of relationship
     */
    public MetadataRelation(final Metadata metadata1, final Metadata related) {
        setMetadata(metadata1);
        setRelated(related);
    }

    /**
     * Get the metadata relation id object.
     * 
     * @return the metadata relation id object.
     */
    @EmbeddedId
    public MetadataRelationId getId() {
        return _id;
    }

    /**
     * Set the metadata relation id object.
     * 
     * @param id the metadata relation id object.
     */
    public void setId(final MetadataRelationId id) {
        this._id = id;
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
        return _metadata;
    }

    /**
     * Set the one of the metadata in the relationship.
     * <p>
     *     The metadata must be a managed entity. (returned from repository save method or one of the find methods)
     * </p>
     * 
     * @param metadata the one of the metadata in the relationship.
     */
    public void setMetadata(Metadata metadata) {
        this._metadata = metadata;
        _id.setMetadataId(metadata.getId());
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
        return _related;
    }

    /**
     * Set the second of the related metadata.
     * <p>
     *     The metadata must be a managed entity. (returned from repository save method or one of the find methods)
     * </p>
     *
     * @param related the second of the related metadata.
     */
    public void setRelated(Metadata related) {
        this._related = related;
        _id.setRelatedId(related.getId());
    }
}
