package org.fao.geonet.domain;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.apache.lucene.document.Document;
import org.fao.geonet.entitylistener.MetadataDraftEntityListenerManager;

/**
 * This is a normal {@link Metadata} but on its draft version.
 *
 * @author María Arias de Reyna
 */
@Entity
@Table(name = MetadataDraft.TABLENAME)
@Access(AccessType.PROPERTY)
@EntityListeners(MetadataDraftEntityListenerManager.class)
public class MetadataDraft extends AbstractMetadata {
    public static final String TABLENAME = "MetadataDraft";

    public MetadataDraft() {
    }
    
    /**
     * Get the set of metadata categories this metadata is part of.  This is lazily loaded and all operations are
     * cascaded
     *
     * @return the metadata categories
     */
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.REFRESH},
            fetch = FetchType.EAGER)
    @JoinTable(name = METADATA_CATEG_JOIN_TABLE_NAME + "Draft",
            joinColumns = @JoinColumn(name = "metadataId"),
            inverseJoinColumns = @JoinColumn(name =
            METADATA_CATEG_JOIN_TABLE_CATEGORY_ID))
    @Nonnull
    public Set<MetadataCategory> getMetadataCategories() {
        return metadataCategories;
    }

    /**
     * Set the metadata category
     *
     * @param categories
     */
    protected void setMetadataCategories(@Nonnull Set<MetadataCategory> categories) {
        this.metadataCategories = categories;
    }
    
    public static MetadataDraft createFromLuceneIndexDocument(Document doc) {
        MetadataDraft metadata = new MetadataDraft();
        transform(doc, metadata);
        return metadata;
    }
}
