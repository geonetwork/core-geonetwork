package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.SourceEntityListenerManager;

import javax.persistence.*;

/**
 * Entity representing a metadata source.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "Sources")
@EntityListeners(SourceEntityListenerManager.class)
public class Source extends GeonetEntity {
    private String _uuid;
    private String _name;
    private char _local = Constants.YN_TRUE;

    /**
     * Default constructor.  Required by framework.
     */
    public Source() {
    }

    /**
     * Convenience constructor for quickly making a Source object.
     *
     * @param uuid  the uuid of the source (also the ID)
     * @param name  the name
     * @param local if the source is the local system
     */
    public Source(String uuid, String name, boolean local) {
        this._uuid = uuid;
        this._name = name;
        this._local = Constants.toYN_EnabledChar(local);
    }

    /**
     * Get the uuid of the source.
     *
     * @return the uuid of the source.
     */
    @Id
    public String getUuid() {
        return _uuid;
    }

    /**
     * Set the uuid of the source.
     *
     *
     * @param uuid the uuid of the source.
     * @return this entity
     */
    public Source setUuid(String uuid) {
        this._uuid = uuid;
        return this;
    }

    /**
     * Get the name of the source.
     *
     * @return the name of the source.
     */
    public String getName() {
        return _name;
    }

    /**
     * Set the name of the source.
     *
     *
     * @param name the name of the source.
     * @return this entity
     */
    public Source setName(String name) {
        this._name = name;
        return this;
    }

    /**
     * For backwards compatibility we need the islocal column to be either 'n' or 'y'. This is a workaround to allow this until future
     * versions of JPA that allow different ways of controlling how types are mapped to the database.
     */
    @Column(name = "isLocal", nullable = false, length = 1)
    protected char getIsLocal_JpaWorkaround() {
        return _local;
    }

    /**
     * Set the column values.
     *
     * @param local Constants.YN_ENABLED or Constants.YN_DISABLED
     */
    protected void setIsLocal_JpaWorkaround(char local) {
        _local = local;
    }

    /**
     * Return true is the source refers to the local geonetwork.
     *
     * @return true is the source refers to the local geonetwork.
     */
    @Transient
    public boolean isLocal() {
        return Constants.toBoolean_fromYNChar(getIsLocal_JpaWorkaround());
    }

    /**
     * Set true is the source refers to the local geonetwork.
     *
     * @param local true is the source refers to the local geonetwork.
     * @return this entity
     */
    public Source setLocal(boolean local) {
        setIsLocal_JpaWorkaround(Constants.toYN_EnabledChar(local));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Source source = (Source) o;

        if (_local != source._local) return false;
        if (_name != null ? !_name.equals(source._name) : source._name != null) return false;
        if (_uuid != null ? !_uuid.equals(source._uuid) : source._uuid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _uuid != null ? _uuid.hashCode() : 0;
        result = 31 * result + (_name != null ? _name.hashCode() : 0);
        result = 31 * result + (int) _local;
        return result;
    }
}
