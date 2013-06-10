package org.fao.geonet.domain;

import static org.fao.geonet.domain.OperationAllowed.TABLE_NAME;

import javax.annotation.Nonnull;
import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = TABLE_NAME)
@Cacheable
public class OperationAllowed {
    public static final String TABLE_NAME = "operationallowed";

    @EmbeddedId
    @Nonnull
    private OperationAllowedId id = new OperationAllowedId();

    @MapsId("metadataId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Metadata metadata;

    @MapsId("groupId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;
    @MapsId("operationId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Operation operation;

    /**
     * Constructor for use by JPA
     */
    public OperationAllowed() {
    }

    /**
     * Constructor for use by developers to easily create an instance
     */
    public OperationAllowed(OperationAllowedId id) {
        this.id = id;
    }

    /**
     * Return the primary key.
     */
    public OperationAllowedId getId() {
        return id;
    }

    @Transient
    public Metadata getMetadata() {
        return metadata;
    }

    public OperationAllowed setMetadata(Metadata metadata) {
        internalSetMetadata(metadata);
        metadata.internalAddOperationAllowed(this);
        return this;
    }

    @Transient
    public Group getGroup() {
        return group;
    }

    public OperationAllowed setGroup(Group group) {
        this.group = group;
        this.id.setGroupId(group.getId());
        return this;
    }

    @Transient
    public Operation getOperation() {
        return operation;
    }

    public OperationAllowed setOperation(Operation operation) {
        this.operation = operation;
        this.id.setOperationId(operation.getId());
        return this;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    /**
     * Set metadata without recursively updating metadata with this
     * 
     * @param metadata the new metadata
     */
    void internalSetMetadata(Metadata metadata) {
        this.metadata = metadata;
        this.id.setMetadataId(metadata.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
