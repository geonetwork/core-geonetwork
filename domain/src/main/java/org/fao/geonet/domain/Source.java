package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * Entity representing a metadata source.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name="sources")
public class Source {
    private String uuid;
    private String name;
    private char _local = 'y';

    @Id
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * For backwards compatibility we need the deleted column to
     * be either 'n' or 'y'.  This is a workaround to allow this
     * until future versions of JPA that allow different ways 
     * of controlling how types are mapped to the database.
     */
    @Column(name="islocal", nullable=false, length=1)
    protected char getIsLocal_JpaWorkaround() {
        return _local;
    }
    protected char setIsLocal_JpaWorkaround(char local) {
        return _local = local;
    }
    @Transient
    public boolean isLocal() {
        return _local == 'y';
    }
    public void setLocal(boolean local) {
        this._local = local ? 'y' : 'n';
    }
    
    
}
