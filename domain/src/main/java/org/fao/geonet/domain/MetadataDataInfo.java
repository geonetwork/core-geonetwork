package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * Encapsulates the metadata about a metadata document. (title, rating, schema etc...)
 * This is a JPA Embeddable object that is embedded into a {@link Metadata} Entity
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataDataInfo implements Serializable {
    private static final long serialVersionUID = 8049813754167665960L;
    private String _title;
    private String _changeDate;
    private String _createDate;
    private String _schemaId;
    private char _template = 'n';
    private String _root;
    private String _doctype;
    private int _displayOrder;
    private int _rating;
    private int _popularity;
    /**
     * Get title of metadata.
     */
    @Column
    public String getTitle() {
        return _title;
    }
    /**
     * Set title of metadata.  This only updates the database not the actual 
     * XML document.  It is important that this value is retrieved from 
     * metadata document.
     *
     * @param title new value.
     */
    public void setTitle(String title) {
        this._title = title;
    }
    /**
     * ID of the schema the metadata belongs to.
     */
    @Column(name="schemaid", length=32, nullable=false)
    public String getSchemaId() {
        return _schemaId;
    }
    public void setSchemaId(String schemaId) {
        this._schemaId = schemaId;
    }
    /**
     * For backwards compatibility we need the deleted column to
     * be either 'n' or 'y'.  This is a workaround to allow this
     * until future versions of JPA that allow different ways 
     * of controlling how types are mapped to the database.
     */
    @Column(name="istemplate", length=1, nullable=false)
    public char isTemplate_JPAWorkaround() {
        return _template;
    }
    public void setTemplate_JPAWorkaround(char template) {
        this._template = template;
    }
    @Transient
    public boolean isTemplate() {
        return _template == 'y';
    }
    public void setTemplate(boolean template) {
        this._template = template ? 'y' : 'n';
    }
    @Column
    public String getRoot() {
        return _root;
    }
    public void setRoot(String root) {
        this._root = root;
    }
    @Column
    public String getDoctype() {
        return _doctype;
    }
    public void setDoctype(String doctype) {
        this._doctype = doctype;
    }
    @Column(name="displayorder")
    public int getDisplayOrder() {
        return _displayOrder;
    }
    public void setDisplayOrder(int displayOrder) {
        this._displayOrder = displayOrder;
    }
    @Column(nullable=false)
    public int getRating() {
        return _rating;
    }
    public void setRating(int rating) {
        this._rating = rating;
    }
    @Column(nullable=false)
    public int getPopularity() {
        return _popularity;
    }
    public void setPopularity(int popularity) {
        this._popularity = popularity;
    }
    @Column(nullable=false, length=30)
    public String getChangeDate() {
        return _changeDate;
    }
    public void setChangeDate(String changeDate) {
        this._changeDate = changeDate;
    }
    @Column(nullable=false, length=30)
    public String getCreateDate() {
        return _createDate;
    }
    public void setCreateDate(String createDate) {
        this._createDate = createDate;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_changeDate == null) ? 0 : _changeDate.hashCode());
        result = prime * result + ((_createDate == null) ? 0 : _createDate.hashCode());
        result = prime * result + _displayOrder;
        result = prime * result + ((_doctype == null) ? 0 : _doctype.hashCode());
        result = prime * result + _popularity;
        result = prime * result + _rating;
        result = prime * result + ((_root == null) ? 0 : _root.hashCode());
        result = prime * result + ((_schemaId == null) ? 0 : _schemaId.hashCode());
        result = prime * result + _template;
        result = prime * result + ((_title == null) ? 0 : _title.hashCode());
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
        MetadataDataInfo other = (MetadataDataInfo) obj;
        if (_changeDate == null) {
            if (other._changeDate != null)
                return false;
        } else if (!_changeDate.equals(other._changeDate))
            return false;
        if (_createDate == null) {
            if (other._createDate != null)
                return false;
        } else if (!_createDate.equals(other._createDate))
            return false;
        if (_displayOrder != other._displayOrder)
            return false;
        if (_doctype == null) {
            if (other._doctype != null)
                return false;
        } else if (!_doctype.equals(other._doctype))
            return false;
        if (_popularity != other._popularity)
            return false;
        if (_rating != other._rating)
            return false;
        if (_root == null) {
            if (other._root != null)
                return false;
        } else if (!_root.equals(other._root))
            return false;
        if (_schemaId == null) {
            if (other._schemaId != null)
                return false;
        } else if (!_schemaId.equals(other._schemaId))
            return false;
        if (_template != other._template)
            return false;
        if (_title == null) {
            if (other._title != null)
                return false;
        } else if (!_title.equals(other._title))
            return false;
        return true;
    }
}
