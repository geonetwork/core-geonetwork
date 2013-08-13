package org.fao.geonet.domain;

import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * The superclass for settings tables.
 * 
 * @author Jesse
 * @see Setting
 */
@MappedSuperclass
public class AbstractSetting<T /*extends AbstractSetting<T> (the metamodel generator can't handle this*/> {

    public static final int ROOT_ID = 0;
    private int _id;
    private String _name;
    private String _value;
    private T _parent;
    
    public AbstractSetting() {
        super();
    }

    /**
     * Get the setting id. This is a generated value and as such new instances should not have this set as it will simply be ignored and
     * could result in reduced performance.
     * 
     * @return the setting id
     */
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    public int getId() {
        return _id;
    }

    /**
     * Set the setting id. This is a generated value and as such new instances should not have this set as it will simply be ignored and
     * could result in reduced performance.
     * 
     * @param _id the setting id
     * @return this setting object
     */
    @SuppressWarnings("unchecked")
    public T setId(int _id) {
        this._id = _id;
        return (T) this;
    }

    /**
     * Get the setting name. This is a required property.
     * 
     * @return the setting name.
     */
    @Column(name = "name", nullable = false)
    @Nonnull
    public String getName() {
        return _name;
    }

    /**
     * Set the setting name. This is a required property.
     * 
     * @param name the setting name. This is a required property.
     * @return this setting object
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public T setName(@Nonnull String name) {
        this._name = name;
        return (T) this;
    }


    /**
     * Get the parent setting object. This is a nullable property.
     */
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = { PERSIST, MERGE, DETACH })
    @JoinColumn(name = "parentid")
    @Nullable
    public T getParent() {
        return _parent;
    }

    /**
     * Set the parent setting object for this setting. The may be null.
     * 
     * @param parent the parent setting object
     * @return this setting object
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public T setParent(@Nullable T parent) {
        this._parent = parent;
        return (T) this;
    }

    /**
     * Get the setting value. This is a nullable property.
     * 
     * @return the setting.
     */
    @Lob
    @Column(name = "value", nullable = true)
    @Nullable
    public String getValue() {
        return _value;
    }

    /**
     * Set the value of this setting.  This is nullable.
     * @param value the value of this setting.  This is nullable.
     * @return this setting object
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public T setValue(@Nullable String value) {
        this._value = value;
        return (T)this;
    }

    /**
     * Get the value as an integer. This may throw {@link NullPointerException} if the value is null or {@link NumberFormatException} if the
     * value is not a valid number.
     * 
     * @return the value as an integer
     */
    @Transient
    public int getValueAsInt() throws NullPointerException, NumberFormatException {
        if (getValue() == null) {
            throw new NullPointerException("Setting value of " + getName() + " is null");
        }
        return Integer.parseInt(getValue());
    }

    /**
     * Set the value of setting with an integer.
     * 
     * @param value the new value
     * @return this setting object
     */
    public T setValue(int value) {
        return setValue(String.valueOf(value));
    }

    /**
     * Get the values as a boolean. Returns false if the values is not a boolean.
     * 
     * @return the values as a boolean
     * @throws NullPointerException if the value is null.
     */
    @Transient
    public boolean getValueAsBool() throws NullPointerException {
        if (getValue() == null) {
            throw new NullPointerException("Setting value of " + getName() + " is null");
        }
        return Boolean.parseBoolean(_value);
    }

    /**
     * Set the value of setting with a boolean.
     * 
     * @param value the new value
     * @return this setting object
     */
    public T setValue(boolean value) {
        return setValue(String.valueOf(value));
    }

    @Override
    public String toString() {
        return "Setting [id=" + _id + ", name=" + _name + ", value=" + _value + "]";
    }

}