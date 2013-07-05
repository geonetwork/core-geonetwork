package org.fao.geonet.domain;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * An entity representing an operation that can be performed on a metadata.
 *
 * @author Jesse
 */
@Entity
@Table(name="operations")
@Cacheable
@Access(AccessType.PROPERTY)
public class Operation {
    private int _id;
    private String _name;
    private Map<String, String> _labelTranslations = new HashMap<String, String>();

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
    @Column(name="name", nullable = false, length=32)
    public @Nonnull String getName() {
        return _name;
    }

    public Operation setName(String name) {
        this._name = name;
        return this;
    }

    /**
     * Get the map of langid -> label translations for operations
     */
    @ElementCollection(fetch=FetchType.LAZY, targetClass=String.class)
    @CollectionTable(joinColumns=@JoinColumn(name="iddes"),name="operationsdes")
    @MapKeyColumn(name="langid", length=5)
    @Column(name="label", nullable=false)
    public Map<String, String> getLabelTranslations() {
        return _labelTranslations;
    }
    /**
     * Set new translations this should only be used for initialization.  
     * to add and remove translations use "get" and modify map.
     *
     * @param localizedTranslations the translation map
     */
    protected void setLabelTranslations(Map<String, String> localizedTranslations) {
        this._labelTranslations = localizedTranslations;
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
