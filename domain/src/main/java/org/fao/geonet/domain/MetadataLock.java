package org.fao.geonet.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.fao.geonet.entitylistener.MetadataLockEntityListenerManager;

/**
 * Represents a lock editing on a metadata. 
 * This is a JPA Entity object and is contained in a database table.
 *
 * @author delawen
 */
@Entity
@Access(AccessType.PROPERTY)
@EntityListeners(MetadataLockEntityListenerManager.class)
public class MetadataLock extends GeonetEntity implements Serializable {
    private static final long serialVersionUID = 1141773513894672274L;

    private int _id;
    private Integer metadata;
    private User user;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    /**
     * Id of the lock. This is automatically generated so when creating a new 
     * object leave this blank and allow the database or JPA set
     * the value for you on save.
     */
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the address. This is automatically generated so when creating a new object leave this blank and allow the database or
     * JPA set the value for you on save.
     *
     * @param id the id
     * @return this address object
     */
    public MetadataLock setId(final int id) {
        this._id = id;
        return this;
    }

    public Integer getMetadata() {
        return metadata;
    }

    public void setMetadata(Integer metadata) {
        this.metadata = metadata;
    }
    @ManyToOne(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
