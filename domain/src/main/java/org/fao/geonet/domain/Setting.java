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
    private SettingDataType dataType;
    private int position;

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

    public int getPosition() {
        return position;
    }

    public Setting setPosition(int position) {
        this.position = position;
        return this;
    }


}
