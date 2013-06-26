package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Encapsulates the metadata about a metadata document. (title, rating, schema etc...)
 *
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataDataInfo implements Serializable {
    private static final long serialVersionUID = 8049813754167665960L;
    private String _title;
    private String _schemaId;
    private boolean _template;
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
    @Column(name="schemaid")
    public String getSchemaId() {
        return _schemaId;
    }
    public void setSchemaId(String schemaId) {
        this._schemaId = schemaId;
    }
    @Column(name="istemplate")
    public boolean isTemplate() {
        return _template;
    }
    public void setTemplate(boolean template) {
        this._template = template;
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
    @Column
    public int getRating() {
        return _rating;
    }
    public void setRating(int rating) {
        this._rating = rating;
    }
    @Column
    public int getPopularity() {
        return _popularity;
    }
    public void setPopularity(int popularity) {
        this._popularity = popularity;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _displayOrder;
        result = prime * result + ((_doctype == null) ? 0 : _doctype.hashCode());
        result = prime * result + _popularity;
        result = prime * result + _rating;
        result = prime * result + ((_root == null) ? 0 : _root.hashCode());
        result = prime * result + ((_schemaId == null) ? 0 : _schemaId.hashCode());
        result = prime * result + (_template ? 1231 : 1237);
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
