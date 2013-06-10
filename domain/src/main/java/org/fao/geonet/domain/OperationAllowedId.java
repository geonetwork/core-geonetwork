package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class OperationAllowedId implements Serializable {
    private static final long serialVersionUID = -5759713154514715316L;

    @Column(name="metadataId", nullable=false)
    private int metadataId;
    @Column(name="groupId", nullable=false)
    private int groupId;
    @Column(name="operationId", nullable=false)
    private int operationId;
    
    public int getMetadataId() {
        return metadataId;
    }
    public void setMetadataId(int newMetadataId) {
        this.metadataId = newMetadataId;
    }
    public int getGroupId() {
        return groupId;
    }
    public void setGroupId(int newGroupId) {
        this.groupId = newGroupId;
    }
    
    public int getOperationId() {
        return operationId;
    }
    public void setOperationId(int newOperationId) {
        this.operationId = newOperationId;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + groupId;
        result = prime * result + metadataId;
        result = prime * result + operationId;
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
        OperationAllowedId other = (OperationAllowedId) obj;
        if (groupId != other.groupId)
            return false;
        if (metadataId != other.metadataId)
            return false;
        if (operationId != other.operationId)
            return false;
        return true;
    }
    
    
    @Override
    public String toString() {
        return "OperationAllowedId [metadataId=" + metadataId + ", groupId=" + groupId + ", operationId=" + operationId + "]";
    }
    public OperationAllowedId copy() {
        OperationAllowedId copy = new OperationAllowedId();
        copy.groupId = groupId;
        copy.metadataId = metadataId;
        copy.operationId = operationId;
        return copy;
    }
}
