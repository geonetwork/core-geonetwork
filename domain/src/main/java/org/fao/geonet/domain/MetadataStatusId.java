package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataStatusId implements Serializable {
    private String _changedate;
    private int _metadataId;
    private int _statusId;
    private int _userId;

    public String getChangedate() {
        return _changedate;
    }

    public void setChangedate(String changedate) {
        this._changedate = changedate;
    }

    public int getMetadataId() {
        return _metadataId;
    }

    public void setMetadataId(int metadataId) {
        this._metadataId = metadataId;
    }

    public int getStatusId() {
        return _statusId;
    }

    public void setStatusId(int statusId) {
        this._statusId = statusId;
    }

    public int getUserId() {
        return _userId;
    }

    public void setUserId(int userId) {
        this._userId = userId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_changedate == null) ? 0 : _changedate.hashCode());
        result = prime * result + _metadataId;
        result = prime * result + _statusId;
        result = prime * result + _userId;
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
        MetadataStatusId other = (MetadataStatusId) obj;
        if (_changedate == null) {
            if (other._changedate != null)
                return false;
        } else if (!_changedate.equals(other._changedate))
            return false;
        if (_metadataId != other._metadataId)
            return false;
        if (_statusId != other._statusId)
            return false;
        if (_userId != other._userId)
            return false;
        return true;
    }
}
