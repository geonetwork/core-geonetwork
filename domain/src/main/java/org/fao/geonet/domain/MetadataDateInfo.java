package org.fao.geonet.domain;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Version;

/**
 * Encapsulates date related information about the metadata
 * document.
 *
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataDateInfo {
    private Date changeDate;
    private Date createDate;
    
    @Version
    @Column(name="changedate")
    public Date getChangeDate() {
        return changeDate;
    }
    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }
    @Column(name="createdate")
    public Date getCreateDate() {
        return createDate;
    }
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((changeDate == null) ? 0 : changeDate.hashCode());
        result = prime * result + ((createDate == null) ? 0 : createDate.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetadataDateInfo other = (MetadataDateInfo) obj;
        if (changeDate == null) {
            if (other.changeDate != null)
                return false;
        } else if (!changeDate.equals(other.changeDate))
            return false;
        if (createDate == null) {
            if (other.createDate != null)
                return false;
        } else if (!createDate.equals(other.createDate))
            return false;
        return true;
    }
}
