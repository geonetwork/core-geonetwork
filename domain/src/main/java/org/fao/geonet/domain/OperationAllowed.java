package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.OperationAllowedEntityListenerManager;

import javax.annotation.Nonnull;
import javax.persistence.*;

/**
 * An entity that represents the relationship between a metadata, group and the operations that group is allowed to perform on the
 * metadata.
 *
 * @author Jesse
 */
@Entity
@Table(name = OperationAllowed.TABLE_NAME)
@Access(AccessType.PROPERTY)
@EntityListeners(OperationAllowedEntityListenerManager.class)
public class OperationAllowed extends GeonetEntity {
    /**
     * Name of the operationallowed table.
     */
    public static final String TABLE_NAME = "OperationAllowed";

    private OperationAllowedId _id = new OperationAllowedId();

    /**
     * Constructor for use by JPA.
     */
    public OperationAllowed() {
    }

    /**
     * Constructor for use by developers to easily create an instance.
     */
    public OperationAllowed(@Nonnull OperationAllowedId id) {
        this._id = id;
    }

    /**
     * Return the Id object.
     */
    @EmbeddedId
    public OperationAllowedId getId() {
        return _id;
    }

    /**
     * Set the Id object.
     *
     * @param id new id
     */
    public void setId(OperationAllowedId id) {
        this._id = id;
    }

    @Override
    public String toString() {
        return "OperationId: [" + _id.toString() + "]";
    }

    public OperationAllowed setId(Metadata metadata, Group group, Operation operation) {
        setId(new OperationAllowedId(metadata.getId(), group.getId(), operation.getId()));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationAllowed that = (OperationAllowed) o;

        if (_id != null ? !_id.equals(that._id) : that._id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return _id != null ? _id.hashCode() : 0;
    }
}
