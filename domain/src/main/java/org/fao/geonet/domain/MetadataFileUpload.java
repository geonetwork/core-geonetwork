/*
 * Copyright (C) 2001-2020 Food and Agriculture Organization of the
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

import org.apache.commons.lang.StringUtils;

import jakarta.persistence.*;

/**
 * Entity to model a metadata file upload request: metadata identifier, upload date, user, file
 * name, etc.
 *
 * @author Jose García
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = MetadataFileUpload.TABLE_NAME,
    indexes = { @Index(name = "ix_metadatafileuploads_mdid", columnList = "metadataid") })
@SequenceGenerator(name = MetadataFileUpload.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class MetadataFileUpload extends GeonetEntity {
    public static final String TABLE_NAME = "MetadataFileUploads";
    public static final String ID_COLUMN_NAME = "id";
    public static final String UPLOAD_DATE_COLUMN_NAME = "uploadDate";
    public static final String DELETED_DATE_COLUMN_NAME = "deletedDate";
    static final String ID_SEQ_NAME = "metadata_fileupload_id_seq";
    private int _id;
    private int _metadataId;
    private String _fileName;
    private String _uploadDate;
    private Double _fileSize;
    private String _userName;
    private String _deletedDate;

    /**
     * Get the id object for this MetadataFileUpload entity.
     *
     * @return the id object for this MetadataFileUpload entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(name = ID_COLUMN_NAME)
    public int getId() {
        return _id;
    }

    /**
     * Set the id object for this MetadataFileUpload entity.
     *
     * @param id the id object for this MetadataFileUpload entity.
     * @return thisMetadataFileUpload object
     */
    public MetadataFileUpload setId(int id) {
        this._id = id;
        return this;
    }


    /**
     * Get the upload date for this entity.
     *
     * @return the validation status for this entity.
     */
    @Column(name = UPLOAD_DATE_COLUMN_NAME, nullable = false)
    public String getUploadDate() {
        return _uploadDate;
    }

    /**
     * Set the upload date for this entity.
     *
     * @param uploadDate the upload date for this entity.
     * @return this entity object
     */
    public MetadataFileUpload setUploadDate(String uploadDate) {
        this._uploadDate = uploadDate;
        return this;
    }

    /**
     * Get the file name for this entity.
     *
     * @return the file name for this entity.
     */
    @Column(nullable = false)
    public Double getFileSize() {
        return _fileSize;
    }

    /**
     * Set the file size for this entity.
     *
     * @param fileSize the file size for this entity.
     * @return this entity object
     */
    public MetadataFileUpload setFileSize(Double fileSize) {
        this._fileSize = fileSize;
        return this;
    }

    /**
     * Get the user name for this entity.
     *
     * @return the user name for this entity.
     */
    @Column(nullable = false)
    public int getMetadataId() {
        return _metadataId;
    }

    /**
     * Set the user name for this entity.
     *
     * @param metadataId the user name for this entity.
     * @return this entity object
     */
    public MetadataFileUpload setMetadataId(int metadataId) {
        this._metadataId = metadataId;
        return this;
    }

    /**
     * Get the user name for this entity.
     *
     * @return the user name for this entity.
     */
    @Column(nullable = false)
    public String getUserName() {
        return _userName;
    }

    /**
     * Set the user name for this entity.
     *
     * @param userName the user name for this entity.
     * @return this entity object
     */
    public MetadataFileUpload setUserName(String userName) {
        this._userName = userName;
        return this;
    }


    /**
     * Get the user name for this entity.
     *
     * @return the user name for this entity.
     */
    @Column(nullable = false)
    public String getFileName() {
        return _fileName;
    }

    /**
     * Set the user name for this entity.
     *
     * @param fileName the user name for this entity.
     * @return this entity object
     */
    public MetadataFileUpload setFileName(String fileName) {
        this._fileName = fileName;
        return this;
    }


    /**
     * Get the user name for this entity.
     *
     * @return the user name for this entity.
     */
    @Column(name = DELETED_DATE_COLUMN_NAME, nullable = true)
    public String getDeletedDate() {
        return _deletedDate;
    }

    /**
     * Set the user name for this entity.
     *
     * @param deletedDate the user name for this entity.
     * @return this entity object
     */
    public MetadataFileUpload setDeletedDate(String deletedDate) {
        this._deletedDate = deletedDate;
        return this;
    }


    /**
     * Get the deleted status for this entity.
     *
     * @return the deleted status for this entity.
     */
    @Transient
    public boolean isDeleted() {
        return StringUtils.isNotEmpty(_deletedDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetadataFileUpload that = (MetadataFileUpload) o;

        if (_id != that._id) return false;
        if (_metadataId != that._metadataId) return false;
        if (_deletedDate != null ? !_deletedDate.equals(that._deletedDate) : that._deletedDate != null)
            return false;
        if (!_fileName.equals(that._fileName)) return false;
        if (!_fileSize.equals(that._fileSize)) return false;
        if (!_uploadDate.equals(that._uploadDate)) return false;
        if (_userName != null ? !_userName.equals(that._userName) : that._userName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _id;
        result = 31 * result + _metadataId;
        result = 31 * result + _fileName.hashCode();
        result = 31 * result + _uploadDate.hashCode();
        result = 31 * result + _fileSize.hashCode();
        result = 31 * result + (_userName != null ? _userName.hashCode() : 0);
        result = 31 * result + (_deletedDate != null ? _deletedDate.hashCode() : 0);
        return result;
    }
}
