package org.fao.geonet.domain;

import static org.fao.geonet.domain.OperationAllowedNamedQueries.*;
import javax.annotation.Nonnull;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = OperationAllowed.TABLE_NAME)
//@Cacheable
@NamedQueries({
        @NamedQuery(name=DeleteByMetadataId.NAME, query=DeleteByMetadataId.QUERY),
        @NamedQuery(name=DeleteAllByMetadataIdExceptGroupId.NAME, query=DeleteAllByMetadataIdExceptGroupId.QUERY)
})
public class OperationAllowed {
    public static final String TABLE_NAME = "operationallowed";
    @EmbeddedId
    @Nonnull
    private OperationAllowedId id = new OperationAllowedId();

    @MapsId("metadataId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="metadataid", referencedColumnName="id")
    private Metadata metadata;

    @MapsId("groupId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="groupid", referencedColumnName="id")
    private Group group;
    @MapsId("operationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="operationid", referencedColumnName="id")
    private Operation operation;

    /**
     * Constructor for use by JPA
     */
    public OperationAllowed() {
    }

    /**
     * Constructor for use by developers to easily create an instance
     */
    public OperationAllowed(@Nonnull OperationAllowedId id) {
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
        if (this.metadata != metadata) {
            internalSetMetadata(metadata);
            if (metadata != null) {
                metadata.internalAddOperationAllowed(this);
            }
        }
        return this;
    }

    @Transient
    public Group getGroup() {
        return group;
    }

    public OperationAllowed setGroup(Group group) {
        this.group = group;
        if (group != null) {
            this.id.setGroupId(group.getId());
        } else {
            this.id.setGroupId(-1);
        }
        return this;
    }

    @Transient
    public Operation getOperation() {
        return operation;
    }

    public OperationAllowed setOperation(Operation operation) {
        this.operation = operation;
        if (operation != null) {
            this.id.setOperationId(operation.getId());
        } else {
            this.id.setOperationId(-1);
        }
        return this;
    }

    @Override
    public String toString() {
        return "OperationId: [" + id.toString() + "]";
    }

    /**
     * Set metadata without recursively updating metadata with this
     * 
     * @param metadata the new metadata
     */
    void internalSetMetadata(Metadata metadata) {
        if (this.metadata != metadata) {
            if (this.metadata != null) {
                this.metadata.internalRemoveOperationAllowed(this);
            }
            this.metadata = metadata;
            if (metadata != null) {
                this.id.setMetadataId(metadata.getId());
            } else {
                this.id.setMetadataId(-1);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id.hashCode());
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
        if (!id.equals(other.id))
            return false;
        return true;
    }

}
