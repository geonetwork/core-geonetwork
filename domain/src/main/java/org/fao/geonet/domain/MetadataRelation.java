package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.MetadataRelationEntityListenerManager;

import javax.persistence.*;

/**
 * Tables that links related metadata.
 * <p>
 * Object is its own entity so that it is easier to add relations without having to load the related metadata.
 * </p>
 * <p>
 * Note: It is important that both Metadata are managed (have been saved or loaded from the MetadataRepository.)
 * For example:
 * <pre><code>
 *      Metadata metadata1 = _metadataRepo.findOne(id);
 *      Metadata metadata2 = _metadataRepo.findOne(id2);
 *      new MetadataRelation(metadata1, metadata2);
 *     </code></pre>
 * </p>
 *
 * @author Jesse
 */
@Entity
@Table(name = "Relations")
@Access(AccessType.PROPERTY)
@EntityListeners(MetadataRelationEntityListenerManager.class)
public class MetadataRelation extends GeonetEntity {
    private MetadataRelationId _id = new MetadataRelationId();

    /**
     * Default constructor, required by JPA.
     */
    public MetadataRelation() {

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
    public MetadataRelation setId(final MetadataRelationId id) {
        this._id = id;
        return this;
    }
}
