package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Variable substitutions and extra information that goes in a CSW capabilities document.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name="cswservercapabilitiesinfo")
public class CswServerCapabilitiesInfo {
    private int _id;
    private String _langId;
    private String _field;
    private String _label;

    @Id
    @Column(name="idfield", length=10)
    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    @Column(name="langid", nullable=false, length=5)
    public String getLangId() {
        return _langId;
    }

    public void setLangId(String langid) {
        this._langId = langid;
    }

    @Column(nullable=false, length=32)
    public String getField() {
        return _field;
    }

    public void setField(String field) {
        this._field = field;
    }

    @Lob
    public String getLabel() {
        return _label;
    }

    public void setLabel(String label) {
        this._label = label;
    }

}
