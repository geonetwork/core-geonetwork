package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class OperationAllowedId implements Serializable {
    private static final long serialVersionUID = -5759713154514715316L;

    private int _metadataId;
    private int _groupId;
    private int _operationId;

    /**
     * Default constructor. Setters must be used to initialize object.
     */
    public OperationAllowedId() {
        // default constructor.
    }

    public OperationAllowedId(int metadataId, int groupId, int operationId) {
        this._metadataId = metadataId;
        this._groupId = groupId;
        this._operationId = operationId;
    }

    public int getMetadataId() {
        return _metadataId;
    }

    public OperationAllowedId setMetadataId(int newMetadataId) {
        this._metadataId = newMetadataId;
        return this;
    }

    public int getGroupId() {
        return _groupId;
    }

    public OperationAllowedId setGroupId(int newGroupId) {
        this._groupId = newGroupId;
        return this;
    }

    public int getOperationId() {
        return _operationId;
    }

    public OperationAllowedId setOperationId(int newOperationId) {
        this._operationId = newOperationId;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _groupId;
        result = prime * result + _metadataId;
        result = prime * result + _operationId;
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
        if (_groupId != other._groupId)
            return false;
        if (_metadataId != other._metadataId)
            return false;
        if (_operationId != other._operationId)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "OperationAllowedId [metadataId=" + _metadataId + ", groupId=" + _groupId + ", operationId=" + _operationId + "]";
    }

    public OperationAllowedId copy() {
        OperationAllowedId copy = new OperationAllowedId();
        copy._groupId = _groupId;
        copy._metadataId = _metadataId;
        copy._operationId = _operationId;
        return copy;
    }
}
