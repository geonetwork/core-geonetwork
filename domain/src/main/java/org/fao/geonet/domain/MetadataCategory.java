package org.fao.geonet.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Map;

/**
 * A Metadata category. This is separate from any category listed in the metadata xml itself and is geonetwork specific.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Cacheable
@Table(name = "Categories")
public class MetadataCategory extends Localized {
    private int _id;
    private String _name;

    /**
     * The id of the category. This is a generated value and not controlled by the developer.
     *
     * @return the id
     */
    @Id
    @GeneratedValue
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the category. This is typically set by the JPA entity manager and should only be set by the developer when they
     * want to
     * merge new data with an existing entity or want to perform query by example. But even then it is not generally recommended.
     *
     * @param id the id.
     */
    public void setId(int id) {
        this._id = id;
    }

    /**
     * The name of the category. This is a required property.
     *
     * @return the name.
     */
    @Column(nullable = false)
    public String getName() {
        return _name;
    }

    /**
     * Set the name of the category. This is a required non-null property.
     *
     * @param name the new name.
     */
    public void setName(String name) {
        this._name = name;
    }

    @Override
    @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "idDes"), name = "CategoriesDes")
    @MapKeyColumn(name = "langId", length = 5)
    @Column(name = "label", nullable = false)
    public Map<String, String> getLabelTranslations() {
        return super.getLabelTranslations();
    }

    // CHECKSTYLE:OFF
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetadataCategory category = (MetadataCategory) o;

        if (_id != category._id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return _id;
    }
}
