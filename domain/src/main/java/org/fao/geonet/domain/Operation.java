package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.OperationEntityListenerManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Map;

/**
 * An entity representing an operation that can be performed on a metadata.
 *
 * @author Jesse
 */
@Entity
@Table(name = "Operations")
@Cacheable
@Access(AccessType.PROPERTY)
@EntityListeners(OperationEntityListenerManager.class)
@SequenceGenerator(name=Operation.ID_SEQ_NAME, initialValue=100, allocationSize=1)
public class Operation extends Localized {
    static final String ID_SEQ_NAME = "operation_id_seq";
    private int _id;
    private String _name;

    /**
     * Get the Id of the operation. This is a generated value and as such new instances should not have this set as it will simply be
     * ignored and could result in reduced performance.
     *
     * @return the Id of the operation.
     */
    @Id
    @GeneratedValue (strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(name = "id", nullable = false)
    public int getId() {
        return _id;
    }

    /**
     * Set the Id of the operation. This is a generated value and as such new instances should not have this set as it will simply be
     * ignored and could result in reduced performance.
     *
     * @param id the Id of the operation
     * @return this entity object.
     */
    public Operation setId(final int id) {
        this._id = id;
        return this;
    }

    /**
     * Return true if the operation is one of the reserved operations. If this returns true then getReservedOperation method should
     * return a
     * value
     */
    @Transient
    public boolean isReserved() {
        return getReservedOperation() != null;
    }

    /**
     * Return the name (untranslated) of the operation.
     */
    @Column(name = "name", nullable = false, length = 32)
    @Nonnull
    public String getName() {
        return _name;
    }

    /**
     * Set the name (untranslated) of the operation.
     *
     * @param name the name (untranslated) of the operation.
     * @return this entity object
     */
    public Operation setName(String name) {
        this._name = name;
        return this;
    }

    @Override
    @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "idDes"), name = "OperationsDes")
    @MapKeyColumn(name = "langId", length = 5)
    @Column(name = "label", nullable = false)
    public Map<String, String> getLabelTranslations() {
        return super.getLabelTranslations();
    }

    /**
     * Return a ReservedOperation if this operation is one of the reserved operation or null otherwise.
     */
    @Transient
    public
    @Nullable
    ReservedOperation getReservedOperation() {
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
