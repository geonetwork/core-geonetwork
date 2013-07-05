package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Information about the source and owner of the metadata
 * document.
 * This is a JPA Embeddable object that is embedded into a {@link Metadata} Entity
 *
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataSourceInfo {
    private String _source;
    private int _groupOwner;
    private int _owner;
    @Column(nullable=false)
    public String getSource() {
        return _source;
    }
    public void setSource(String source) {
        this._source = source;
    }
    @Column(name="groupowner")
    public int getGroupOwner() {
        return _groupOwner;
    }
    public void setGroupOwner(int groupOwner) {
        this._groupOwner = groupOwner;
    }
    @Column(nullable=false)
    public int getOwner() {
        return _owner;
    }
    public void setOwner(int owner) {
        this._owner = owner;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _groupOwner;
        result = prime * result + _owner;
        result = prime * result + ((_source == null) ? 0 : _source.hashCode());
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
        MetadataSourceInfo other = (MetadataSourceInfo) obj;
        if (_groupOwner != other._groupOwner)
            return false;
        if (_owner != other._owner)
            return false;
        if (_source == null) {
            if (other._source != null)
                return false;
        } else if (!_source.equals(other._source))
            return false;
        return true;
    }
}
