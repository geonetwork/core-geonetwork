package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

@Embeddable
@Access(AccessType.PROPERTY)
public class ValidationId implements Serializable {
    private static final long serialVersionUID = -7162983572434017017L;
    private int _metadataId;
    private String _validationType;
    public int getMetadataId() {
        return _metadataId;
    }
    public void setMetadataId(int metadataid) {
        this._metadataId = metadataid;
    }
    public String getValidationType() {
        return _validationType;
    }
    public void setValidationType(String validationType) {
        this._validationType = validationType;
    }
}
