package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * Entity representing a metadata source.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
public class Source {
    private String uuid;
    private String name;
    private boolean local;

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
    public boolean isLocal() {
        return local;
    }
    public void setLocal(boolean local) {
        this.local = local;
    }
    
    
}
