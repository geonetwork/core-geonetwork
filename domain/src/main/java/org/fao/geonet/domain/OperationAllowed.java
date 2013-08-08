package org.fao.geonet.domain;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.fao.geonet.domain.OperationAllowedNamedQueries.DeleteAllByMetadataIdExceptGroupId;
import org.fao.geonet.domain.OperationAllowedNamedQueries.DeleteByMetadataId;

/**
 * An entity that represents the relationship between a metadata, group and the operations that group is allowed to perform on the metadata.
 * 
 * @author Jesse
 */
@Entity
@Table(name = OperationAllowed.TABLE_NAME)
@Access(AccessType.PROPERTY)
@NamedQueries({ @NamedQuery(name = DeleteByMetadataId.NAME, query = DeleteByMetadataId.QUERY),
        @NamedQuery(name = DeleteAllByMetadataIdExceptGroupId.NAME, query = DeleteAllByMetadataIdExceptGroupId.QUERY) })
public class OperationAllowed {
    public static final String TABLE_NAME = "operationallowed";

    private OperationAllowedId _id = new OperationAllowedId();
    private Metadata _metadata;
    private Group _group;
    private Operation _operation;

    /**
     * Constructor for use by JPA
     */
    public OperationAllowed() {
    }

    /**
     * Constructor for use by developers to easily create an instance
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

    /**
     * Get the related metadata. Lazily fetched.
     *
     * @return the related metadata.
     */
    @MapsId("metadataId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metadataid", referencedColumnName = "id")
    public Metadata getMetadata() {
        return _metadata;
    }

    /**
     * Set the related metadata.
     * @param metadata the related metadata.
     * @return this entity object.
     */
    public OperationAllowed setMetadata(Metadata metadata) {
        if (this._metadata != metadata) {
            internalSetMetadata(metadata);
            if (metadata != null) {
                metadata.internalAddOperationAllowed(this);
            }
        }
        return this;
    }

    /**
     * Get the related group. Lazily fetched.
     *
     * @return the related group.
     */
    @MapsId("groupId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupid", referencedColumnName = "id")
    public Group getGroup() {
        return _group;
    }

    /**
     * Set the related group. Lazily fetched.
     * @param group the related group.
     * @return this entity object.
     */
    public OperationAllowed setGroup(Group group) {
        this._group = group;
        if (group != null) {
            this._id.setGroupId(group.getId());
        } else {
            this._id.setGroupId(-1);
        }
        return this;
    }

    /**
     * Get the related operation. Lazily fetched.
     * @return the related operation.
     */
    @MapsId("operationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operationid", referencedColumnName = "id")
    public Operation getOperation() {
        return _operation;
    }

    /**
     * Set the related operation.
     * @param operation the related operation.
     * @return this entity object.
     */
    public OperationAllowed setOperation(Operation operation) {
        this._operation = operation;
        if (operation != null) {
            this._id.setOperationId(operation.getId());
        } else {
            this._id.setOperationId(-1);
        }
        return this;
    }

    @Override
    public String toString() {
        return "OperationId: [" + _id.toString() + "]";
    }

    /**
     * Set metadata without recursively updating metadata with this
     * 
     * @param metadata the new metadata
     */
    void internalSetMetadata(Metadata metadata) {
        if (this._metadata != metadata) {
            if (this._metadata != null) {
                this._metadata.internalRemoveOperationAllowed(this);
            }
            this._metadata = metadata;
            if (metadata != null) {
                this._id.setMetadataId(metadata.getId());
            } else {
                this._id.setMetadataId(-1);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (_id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OperationAllowed other = (OperationAllowed) obj;
        if (!_id.equals(other._id))
            return false;
        return true;
    }

}
