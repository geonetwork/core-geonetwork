package org.fao.geonet.domain;

import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.fao.geonet.domain.SettingNamedQueries.QUERY_FIND_ALL_CHILDREN;
import org.fao.geonet.domain.SettingNamedQueries.QUERY_FIND_CHILDREN_BY_NAME;
import org.fao.geonet.domain.SettingNamedQueries.QUERY_FIND_ROOT;

/**
 * An entity representing a system configuration setting.
 * 
 * Settings are represented by a tree. One should use the {@link org.fao.geonet.repository.SettingRepository} to traverse the hierarchy.
 * 
 * @author Jesse
 */
@Entity
@Table(name = "settings")
@Cacheable
@Access(AccessType.PROPERTY)
@NamedQueries({ @NamedQuery(name = QUERY_FIND_CHILDREN_BY_NAME.NAME, query = QUERY_FIND_CHILDREN_BY_NAME.QUERY),
        @NamedQuery(name = QUERY_FIND_ALL_CHILDREN.NAME, query = QUERY_FIND_ALL_CHILDREN.QUERY),
        @NamedQuery(name = QUERY_FIND_ROOT.NAME, query = QUERY_FIND_ROOT.QUERY), })
public class Setting {
    public static final int ROOT_ID = 0;

    private int _id;
    private Setting _parent;
    private String _name;
    private String _value;

    /**
     * Get the setting id.  This is a generated value and as such new instances should not have this set as it will simply be ignored
     * and could result in reduced performance.
     * @return the setting id
     */
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    public int getId() {
        return _id;
    }

    /**
     * Set the setting id.  This is a generated value and as such new instances should not have this set as it will simply be ignored
     * and could result in reduced performance.
     * @param _id the setting id
     * @return this setting object
     */
    public Setting setId(int _id) {
        this._id = _id;
        return this;
    }

    /**
     * Get the parent setting object.  This is a nullable property.
     */
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = { PERSIST, MERGE, DETACH })
    @JoinColumn(name = "parentid")
    public @Nullable
    Setting getParent() {
        return _parent;
    }

    /**
     * Set the parent setting object for this setting.  The may be null.
     *
     * @param parent the parent setting object 
     * @return this setting object
     */
    public @Nonnull Setting setParent(@Nullable Setting parent) {
        this._parent = parent;
        return this;
    }

    /**
     * Get the setting name.  This is a required property.
     *
     * @return the setting name.
     */
    @Column(name = "name", nullable = false)
    public @Nonnull
    String getName() {
        return _name;
    }

    /**
     * Set the setting name.  This is a required property.
     * @param name the setting name.   This is a required property.
     * @return this setting object
     */
    public @Nonnull Setting setName(@Nonnull String name) {
        this._name = name;
        return this;
    }

    /**
     * Get the setting value. This is a nullable property.
     * @return
     */
    @Lob
    @Column(name = "value", nullable = true)
    public @Nullable String getValue() {
        return _value;
    }

    public Setting setValue(@Nullable String value) {
        this._value = value;
        return this;
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
            throw new NullPointerException("Setting value of "+getName()+" is null");
        }
        return Integer.parseInt(getValue());
    }

    /**
     * Set the value of setting with an integer.
     * 
     * @param value the new value
     * @return this setting object
     */
    public Setting setValue(int value) {
       return setValue(String.valueOf(value));
    }

    /**
     * Get the values as a boolean.  Returns false if the values is not a boolean.
     * @return the values as a boolean
     * @throws NullPointerException if the value is null.
     */
    @Transient
    public boolean getValueAsBool() throws NullPointerException {
        if (getValue() == null) {
            throw new NullPointerException("Setting value of "+getName()+" is null");
        }
        return Boolean.parseBoolean(_value);
    }

    /**
     * Set the value of setting with a boolean.
     * 
     * @param value the new value
     * @return this setting object
     */
    public Setting setValue(boolean value) {
       return setValue(String.valueOf(value));
    }

    @Override
    public String toString() {
        return "Setting [id=" + _id + ", name=" + _name + ", value=" + _value + "]";
    }
}
