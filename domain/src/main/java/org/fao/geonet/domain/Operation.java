package org.fao.geonet.domain;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Represents an operation that can be performed on a metadata.
 * @author Jesse
 */
@Entity
@Table(name="operations")
@Cacheable
public class Operation {
    private int _id;
    private String _name;

    @ManyToMany(mappedBy = "operations", fetch = FetchType.LAZY)
    private List<Metadata> _metadata = new ArrayList<Metadata>();
    /**
     * Get Id of the operation
     */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id", nullable = false)
    public int getId() {
        return _id;
    }

    public Operation setId(int id) {
        this._id = id;
        return this;
    }
    /**
     * Return true if the operation is one of the reserved operations.  
     * If this returns true then getReservedOperation method should return a value
     */
    @Transient
    public boolean isReserved() {
        return getReservedOperation() != null;
    }

    /**
     * Return the name (untranslated) of the operation.
     */
    @Column(name="name", nullable = false)
    public @Nonnull String getName() {
        return _name;
    }

    public Operation setName(String name) {
        this._name = name;
        return this;
    }
    
    /**
     * Return a ReservedOperation if this operation is one of the reserved operation or
     * null otherwise.
     */
    @Transient
    public @Nullable ReservedOperation getReservedOperation() {
        return ReservedOperation.lookup(_id);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _id;
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
        Operation other = (Operation) obj;
        if (_id != other._id)
            return false;
        return true;
    }

    public boolean is(ReservedOperation reservedOperation) {
        return reservedOperation.getId() == getId();
    }

    @Override
    public String toString() {
        return "Operation [_id=" + _id + ", _name=" + _name + "]";
    }
    
    
}
