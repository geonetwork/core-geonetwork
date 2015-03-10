package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.SettingEntityListenerManager;
import org.hibernate.annotations.Type;

import javax.persistence.*;

/**
 * An entity representing a system configuration setting.
 * <p/>
 * Settings are represented by a tree. One should use the {@link org.fao.geonet.repository.HarvesterSettingRepository} to traverse the
 * hierarchy.
 *
 * @author Jesse
 */
@Entity
@Table(name = "Settings")
@Cacheable
@Access(AccessType.PROPERTY)
@EntityListeners(SettingEntityListenerManager.class)
public class Setting extends GeonetEntity {
    private String name;
    private String value;
    private SettingDataType dataType = SettingDataType.STRING;
    private int position = 0;
    private char internal = Constants.YN_TRUE;

    @Id
    @Column(name = "name", nullable = false, length = 255/* mysql cannot accept it any bigger if it is to be the id */)
    public String getName() {
        return name;
    }

    public Setting setName(String name) {
        this.name = name;
        return this;
    }

    @Lob
    @Column(name = "value", nullable = true)
    @Type(type="org.hibernate.type.StringClobType") // this is a work around for postgres so postgres can correctly load clobs
    public String getValue() {
        return value;
    }

    public Setting setValue(String value) {
        this.value = value;
        return this;
    }

    @Column(name = "datatype")
    public SettingDataType getDataType() {
        return dataType;
    }

    public Setting setDataType(SettingDataType dataType) {
        this.dataType = dataType;
        return this;
    }

    @Column(name = "position", nullable = false, columnDefinition="int default 0")
    public int getPosition() {
        return position;
    }

    public Setting setPosition(int position) {
        this.position = position;
        return this;
    }

    /**
     * For backwards compatibility we need the activated column to be either 'n' or 'y'. 
     * This is a workaround to allow this until future
     * versions of JPA that allow different ways of controlling how types are mapped to the database.
     */
    @Column(name = "internal", nullable = false, length = 1, columnDefinition="char default 'y'")
    protected char getInternal_JpaWorkaround() {
        return internal;
    }

    /**
     * Set the column value. Constants.YN_ENABLED for true Constants.YN_DISABLED for false.
     *
     * @param internalValue the column value. Constants.YN_ENABLED for true Constants.YN_DISABLED for false.
     * @return
     */
    protected void setInternal_JpaWorkaround(char internalValue) {
      internal = internalValue;
    }

    /**
     * Return true if the setting is public.
     *
     * @return true if the setting is public.
     */
    @Transient
    public boolean isInternal() {
      return Constants.toBoolean_fromYNChar(getInternal_JpaWorkaround());
    }

    /**
     * Set true if the setting is private.
     *
     * @param internal true if the setting is private.
     */
    public Setting setInternal(boolean internal) {
      setInternal_JpaWorkaround(Constants.toYN_EnabledChar(internal));
      return this;
    }

    @Override
    public String toString() {
        return "Setting{'" + name + "' = '" + value + "'}";
    }

}
