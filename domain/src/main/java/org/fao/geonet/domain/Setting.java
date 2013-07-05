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
 * Settings are represented by a tree. One should use the {@link org.fao.geonet.repository.SettingRepository}
 * to traverse the hierarchy.
 *
 * @author Jesse
 */
@Entity
@Table(name = "settings")
@Cacheable
@Access(AccessType.PROPERTY)
@NamedQueries({ 
    @NamedQuery(name = QUERY_FIND_CHILDREN_BY_NAME.NAME, query = QUERY_FIND_CHILDREN_BY_NAME.QUERY),
    @NamedQuery(name = QUERY_FIND_ALL_CHILDREN.NAME, query = QUERY_FIND_ALL_CHILDREN.QUERY),
    @NamedQuery(name = QUERY_FIND_ROOT.NAME, query = QUERY_FIND_ROOT.QUERY),
    })
public class Setting {
    public static final int ROOT_ID = 0;

    private int _id;
    private Setting _parent;
    private String _name;
    private String _value;

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    public int getId() {
        return _id;
    }

    public Setting setId(int _id) {
        this._id = _id;
        return this;
    }

    /**
     * Get the parent setting.
     */
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = { PERSIST, MERGE, DETACH })
    @JoinColumn(name = "parentid")
    public @Nullable
    Setting getParent() {
        return _parent;
    }

    public Setting setParent(Setting parent) {
        this._parent = parent;
        return this;
    }

    @Column(name = "name", nullable = false)
    public @Nonnull
    String getName() {
        return _name;
    }

    public Setting setName(String name) {
        this._name = name;
        return this;
    }

    @Lob
    @Column(name = "value", nullable = true)
    public String getValue() {
        return _value;
    }

    public Setting setValue(String value) {
        this._value = value;
        return this;
    }

    @Transient
    public int getValueAsInt() {
        return Integer.parseInt(_value);
    }

    public Setting setValue(int value) {
        this._value = String.valueOf(value);
        return this;
    }

    @Transient
    public boolean getValueAsBool() {
        return Boolean.parseBoolean(_value);
    }

    @Override
    public String toString() {
        return "Setting [id=" + _id + ", name=" + _name + ", value=" + _value + "]";
    }
}
