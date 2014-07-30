package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.StatusValueEntityListenerManager;

import javax.persistence.*;
import java.util.Map;

/**
 * One of the enumerated status options that a metadata can be.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "StatusValues")
@Cacheable
@EntityListeners(StatusValueEntityListenerManager.class)
@SequenceGenerator(name=StatusValue.ID_SEQ_NAME, initialValue=100, allocationSize=1)
public class StatusValue extends Localized {
    static final String ID_SEQ_NAME = "status_value_id_seq";
    private int _id;
    private String _name;
    private char _reserved = Constants.YN_FALSE;
    private int displayOrder;

    /**
     * Get the id of the StatusValue object. This is a generated value and as such new instances should not have this set as it will
     * simply
     * be ignored and could result in reduced performance.
     *
     * @return the id of the StatusValue object
     */
    @Id
    @GeneratedValue (strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the StatusValue object. This is a generated value and as such new instances should not have this set as it will
     * simply
     * be ignored and could result in reduced performance.
     *
     * @param id the id of the StatusValue object
     */
    public void setId(final int id) {
        this._id = id;
    }

    /**
     * Get the name of the StatusValue object. This is a required property.
     *
     * @return the name of the StatusValue object.
     */
    @Column(nullable = false)
    public String getName() {
        return _name;
    }

    /**
     * Set the name of the StatusValue object. This is a required property.
     *
     * @param name the name of the StatusValue object.
     */
    public void setName(final String name) {
        this._name = name;
    }

    /**
     * For backwards compatibility we need the reserved column to be either 'n' or 'y'. This is a workaround to allow this until future
     * versions of JPA that allow different ways of controlling how types are mapped to the database.
     */
    @Column(name = "reserved", nullable = false, length = 1)
    protected char getReserved_JpaWorkaround() {
        return _reserved;
    }

    /**
     * Set the column value.
     *
     * @param reserved Constants.YN_ENABLED for true or Constants.YN_DISABLED for false.
     * @return
     */
    protected char setReserved_JpaWorkaround(final char reserved) {
        return _reserved = reserved;
    }

    /**
     * Return true if this is a reserved StatusValue.
     *
     * @return true if this is a reserved StatusValue.
     */
    @Transient
    public boolean isReserved() {
        return Constants.toBoolean_fromYNChar(getReserved_JpaWorkaround());
    }

    /**
     * Set true if this is a reserved StatusValue.
     *
     * @param reserved true if this is a reserved StatusValue.
     */
    public void setReserved(final boolean reserved) {
        setReserved_JpaWorkaround(Constants.toYN_EnabledChar(reserved));
    }

    @Override
    @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "idDes"), name = "StatusValuesDes")
    @MapKeyColumn(name = "langId", length = 5)
    @Column(name = "label", nullable = false)
    public Map<String, String> getLabelTranslations() {
        return super.getLabelTranslations();
    }

    /**
     * Get the order to display this value in the UI. This is just a hint to the ui that provides an priority/importance of the status
     * value
     * compared to the others.
     *
     * @return the order to display this value in the UI.
     */
    @Column(name = "displayorder")
    public int getDisplayOrder() {
        return displayOrder;
    }

    /**
     * Set the order to display this value in the UI. This is just a hint to the ui that provides an priority/importance of the status
     * value
     * compared to the others.
     *
     * @param displayOrder the order to display this value in the UI
     */
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
