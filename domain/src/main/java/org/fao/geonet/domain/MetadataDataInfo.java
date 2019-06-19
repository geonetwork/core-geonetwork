/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.domain;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * Encapsulates the metadata about a metadata document. (title, rating, schema etc...) This is a JPA
 * Embeddable object that is embedded into a {@link Metadata} Entity
 *
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataDataInfo implements Serializable {
    private static final long serialVersionUID = 8049813754167665960L;
    private String _title;
    private ISODate _changeDate = new ISODate();
    private ISODate _createDate = new ISODate();
    private String _schemaId;
    private char _template = Constants.YN_FALSE;
    private String _root;
    private String _doctype;
    private String _extra;
    private Integer _displayOrder = 0;
    private int _rating = 0;
    private int _popularity = 0;

    /**
     * Get title of metadata.
     *
     * @deprecated
     */
    @Column
    public String getTitle() {
        return _title;
    }

    /**
     * Set title of metadata. This only updates the database not the actual XML document. It is
     * important that this value is retrieved from metadata document.
     *
     * @param title new value.
     * @return this data info object
     * @deprecated
     */
    public MetadataDataInfo setTitle(String title) {
        this._title = title;
        return this;
    }

    /**
     * Get the "extra" data associates with the metadata.  This is situation specific for example it
     * might provide extra parameters for searching for metadata.
     *
     * This data is normally used for custom geonetwork applications which need this data for some
     * application specific task.
     */
    @Column
    public String getExtra() {
        return _extra;
    }

    /**
     * Set the "extra" data associates with the metadata.  This is situation specific for example it
     * might provide extra parameters for searching for metadata.
     *
     * This data is normally used for custom geonetwork applications which need this data for some
     * application specific task.
     *
     * @param extra new value.
     * @return this data info object
     */
    public MetadataDataInfo setExtra(String extra) {
        this._extra = extra;
        return this;
    }

    /**
     * Get ID of the schema the metadata belongs to.
     */
    @Column(name = "schemaId", length = 32, nullable = false)
    public String getSchemaId() {
        return _schemaId;
    }

    /**
     * Set ID of the schema the metadata belongs to.
     *
     * @param schemaId the ID of the schema the metadata belongs to.
     * @return this data info object
     */
    public MetadataDataInfo setSchemaId(String schemaId) {
        this._schemaId = schemaId;
        return this;
    }

    /**
     * For backwards compatibility we need the istemplate column to be either 'n' or 'y'. This is a
     * workaround to allow this until future versions of JPA that allow different ways of
     * controlling how types are mapped to the database.
     */
    @Column(name = "isTemplate", length = 1, nullable = false)
    protected char getType_JPAWorkaround() {
        return _template;
    }

    /**
     * Set the code representing true or false for istemplate column.
     *
     * @param template Constants.YN_ENABLED or Constants.YN_DISABLED
     */
    protected void setType_JPAWorkaround(char template) {
        this._template = template;
    }

    /**
     * Get true if the metadata is a template.
     *
     * @return true if the metadata is a template.
     */
    @Transient
    public MetadataType getType() {
        return MetadataType.lookup(getType_JPAWorkaround());
    }

    /**
     * Set true if the metadata is a template.
     *
     * @param template true if the metadata is a template
     * @return this data info object
     */
    public MetadataDataInfo setType(MetadataType template) {
        setType_JPAWorkaround(template.code);
        return this;
    }

    /**
     * Get the name of the root element of the data related to the metadata entity.
     *
     * @return the name of the root element of the data related to the metadata entity.
     */
    @Column
    public String getRoot() {
        return _root;
    }

    /**
     * Set the name of the root element of the data related to the metadata entity.
     * <p/>
     * TODO explain what is it for.
     *
     * @param root the name of the root element of the data related to the metadata entity.
     * @return this data info object
     */
    public MetadataDataInfo setRoot(String root) {
        this._root = root;
        return this;
    }

    /**
     * Get the doctype to use for validation of the metadata. This will be a reference to a dtd
     * typically.
     *
     * @return the doctype to use for validation of the metadata
     */
    @Column
    public String getDoctype() {
        return _doctype;
    }

    /**
     * Set the doctype to use for validation of the metadata. This will be a reference to a dtd
     * typically.
     *
     * @param doctype the doctype to use for validation of the metadata
     * @return this data info object
     */
    public MetadataDataInfo setDoctype(String doctype) {
        this._doctype = doctype;
        return this;
    }

    /**
     * Get the display order. A hint for ordering templates when displayed in a list. May also be
     * used when displaying sub-templates. <p> Should be non-negative and lower number indicates
     * higher/earlier in the list. </p>
     *
     * @return the display order
     */
    @Column(name = "displayorder")
    public Integer getDisplayOrder() {
        return _displayOrder;
    }

    /**
     * Set the display order. A hint for ordering templates when displayed in a list. May also be
     * used when displaying sub-templates. <p> Should be non-negative and lower number indicates
     * higher/earlier in the list. </p>
     *
     * @param displayOrder the display orderthe display order
     * @return this data info object
     */
    public MetadataDataInfo setDisplayOrder(Integer displayOrder) {
        this._displayOrder = displayOrder;
        return this;
    }

    /**
     * Get the current rating of the metadata.
     *
     * @return the rating of the metadata.
     */
    @Column(nullable = false)
    public int getRating() {
        return _rating;
    }

    /**
     * Set the current rating of the metadata.
     *
     * @param rating set the metadata rating.
     * @return this data info object
     */
    public MetadataDataInfo setRating(int rating) {
        this._rating = rating;
        return this;
    }

    /**
     * Get the popularity of the metadata. (Number of views).
     *
     * @return the popularity of the metadata. (Number of views).
     */
    @Column(nullable = false)
    public int getPopularity() {
        return _popularity;
    }

    /**
     * Set the popularity of the metadata. (Number of views).
     *
     * @param popularity the popularity of the metadata. (Number of views).
     * @return this data info object
     */
    public MetadataDataInfo setPopularity(int popularity) {
        this._popularity = popularity;
        return this;
    }

    /**
     * Get the date of the last change made to the metadata.
     *
     * @return the date of the last change made to the metadata.
     */

    @AttributeOverride(name = "dateAndTime", column = @Column(name = "changeDate", nullable = false, length = 30))
    public ISODate getChangeDate() {
        return _changeDate;
    }

    /**
     * Set the date of the last change made to the metadata.
     *
     * @param changeDate the date of the last change made to the metadata.
     * @return this data info object
     */
    public MetadataDataInfo setChangeDate(ISODate changeDate) {
        this._changeDate = changeDate;
        return this;
    }

    /**
     * Get the date that the metadata was created.
     *
     * @return the creation date.
     */
    @AttributeOverride(name = "dateAndTime", column = @Column(name = "createDate", nullable = false, length = 30))
    public ISODate getCreateDate() {
        return _createDate;
    }

    /**
     * Set the date that the metadata was created.
     *
     * @param createDate the creation date.
     * @return this data info object
     */
    public MetadataDataInfo setCreateDate(ISODate createDate) {
        this._createDate = createDate;
        return this;
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
        if (!_displayOrder.equals(other._displayOrder))
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

	@Override
	public String toString() {
		return "MetadataDataInfo [" + (_title != null ? "_title=" + _title + ", " : "")
				+ (_changeDate != null ? "_changeDate=" + _changeDate + ", " : "")
				+ (_createDate != null ? "_createDate=" + _createDate + ", " : "")
				+ (_schemaId != null ? "_schemaId=" + _schemaId + ", " : "") + "_template=" + _template + ", "
				+ (_root != null ? "_root=" + _root + ", " : "")
				+ (_doctype != null ? "_doctype=" + _doctype + ", " : "")
				+ (_extra != null ? "_extra=" + _extra + ", " : "")
				+ (_displayOrder != null ? "_displayOrder=" + _displayOrder + ", " : "") + "_rating=" + _rating
				+ ", _popularity=" + _popularity + "]";
	}

	public MetadataDataInfo clone() {
		MetadataDataInfo clon = new MetadataDataInfo();
		if (this.getChangeDate() != null) {
			clon.setChangeDate(this.getChangeDate().clone());
		}
		if (this.getCreateDate() != null) {
			clon.setCreateDate(this.getCreateDate().clone());
		}
		clon.setDisplayOrder(this.getDisplayOrder());
		clon.setDoctype(this.getDoctype());
		clon.setExtra(this.getExtra());
		clon.setPopularity(this.getPopularity());
		clon.setRating(this.getRating());
		clon.setRoot(this.getRoot());
		clon.setSchemaId(this.getSchemaId());
		clon.setType(this.getType());

		return clon;
	}
}
