package org.fao.geonet.domain;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * An entity representing a system configuration setting.
 * 
 * Settings are represented by a tree. One should use the {@link org.fao.geonet.repository.SettingRepository} to traverse the hierarchy.
 * 
 * @author Jesse
 */
@Entity
@Table(name = "harvestersettings")
@Cacheable
@Access(AccessType.PROPERTY)
public class HarvesterSetting extends AbstractSetting<HarvesterSetting> {
}
