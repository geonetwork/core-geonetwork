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

import jakarta.persistence.*;

/**
 * Entity to model a metadata file download request: metadata identifier, download date, user, file
 * name, etc.
 *
 * It is related to a {@link org.fao.geonet.domain.MetadataFileUpload} entity.
 *
 * @author Jose García
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = MetadataFileDownload.TABLE_NAME,
    indexes = { @Index(name = "ix_metadatafiledownloads_mdid", columnList = "metadataid") })
@SequenceGenerator(name = MetadataFileDownload.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class MetadataFileDownload {
    public static final String TABLE_NAME = "MetadataFileDownloads";
    public static final String ID_COLUMN_NAME = "id";
    public static final String DOWNLOAD_DATE_COLUMN_NAME = "downloadDate";
    static final String ID_SEQ_NAME = "metadata_filedownload_id_seq";
    private int _id;
    private String _downloadDate;
    private String _requesterName;
    private String _requesterOrg;
    private String _requesterMail;
    private String _requesterComments;
    private String _userName;
    private String _fileName;
    private int _fileUploadId;
    private int _metadataId;

    /**
     * Get the id object for this MetadataFileDownload entity.
     *
     * @return the id object for this MetadataFileDownload entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(name = ID_COLUMN_NAME)
    public int getId() {
        return _id;
    }

    /**
     * Set the id object for this MetadataFileDownload entity.
     *
     * @param id the id object for this MetadataFileDownload entity.
     * @return thisMetadataFileDownload object
     */
    public MetadataFileDownload setId(int id) {
        this._id = id;
        return this;
    }


    /**
     * Get the download date for this entity.
     *
     * @return the download date for this entity.
     */
    @Column(name = DOWNLOAD_DATE_COLUMN_NAME, nullable = false)
    public String getDownloadDate() {
        return _downloadDate;
    }

    /**
     * Set the download date for this entity.
     *
     * @param downloadDate the download date for this entity.
     * @return this entity object
     */
    public MetadataFileDownload setDownloadDate(String downloadDate) {
        this._downloadDate = downloadDate;
        return this;
    }

    /**
     * Get the download filename for this entity.
     *
     * @return the download filename for this entity.
     */
    @Column(nullable = false)
    public String getFileName() {
        return _fileName;
    }

    /**
     * Set the download filename for this entity.
     *
     * @param fileName the download filename for this entity.
     * @return this entity object
     */
    public MetadataFileDownload setFileName(String fileName) {
        this._fileName = fileName;
        return this;
    }

    /**
     * Get the requester name for this entity.
     *
     * @return the requester name for this entity.
     */
    public String getRequesterName() {
        return _requesterName;
    }

    /**
     * Set the requester name for this entity.
     *
     * @param requesterName the requester name for this entity.
     * @return this entity object
     */
    public MetadataFileDownload setRequesterName(String requesterName) {
        this._requesterName = requesterName;
        return this;
    }

    /**
     * Get the requester organisation for this entity.
     *
     * @return the requester organisation for this entity.
     */
    public String getRequesterOrg() {
        return _requesterOrg;
    }

    /**
     * Set the requester organisation for this entity.
     *
     * @param requesterOrg the requester organisation for this entity.
     * @return this entity object
     */
    public MetadataFileDownload setRequesterOrg(String requesterOrg) {
        this._requesterOrg = requesterOrg;
        return this;
    }

    /**
     * Get the requester mail for this entity.
     *
     * @return the requester mail for this entity.
     */
    public String getRequesterMail() {
        return _requesterMail;
    }

    /**
     * Set the requester mail for this entity.
     *
     * @param requesterMail the requester mail for this entity.
     * @return this entity object
     */
    public MetadataFileDownload setRequesterMail(String requesterMail) {
        this._requesterMail = requesterMail;
        return this;
    }

    /**
     * Get the requester comments for this entity.
     *
     * @return the requester comments for this entity.
     */
    public String getRequesterComments() {
        return _requesterComments;
    }

    /**
     * Set the requester comments for this entity.
     *
     * @param requesterComments the requester comments for this entity.
     * @return this entity object
     */
    public MetadataFileDownload setRequesterComments(String requesterComments) {
        this._requesterComments = requesterComments;
        return this;
    }


    /**
     * Get the metadata id for this entity.
     *
     * @return the metadata id for this entity.
     */
    @Column(nullable = false)
    public int getMetadataId() {
        return _metadataId;
    }

    /**
     * Set the metadata id for this entity.
     *
     * @param metadataId the metadata id for this entity.
     * @return this entity object
     */
    public MetadataFileDownload setMetadataId(int metadataId) {
        this._metadataId = metadataId;
        return this;
    }

    /**
     * Get the related fileupload id for this entity.
     *
     * @return the related fileupload id for this entity.
     */
    @Column(nullable = false)
    public int getFileUploadId() {
        return _fileUploadId;
    }

    /**
     * Set the related fileupload id for this entity.
     *
     * @param fileUploadId the related fileupload id for this entity.
     * @return this entity object
     */
    public MetadataFileDownload setFileUploadId(int fileUploadId) {
        this._fileUploadId = fileUploadId;
        return this;
    }


    /**
     * Get the user name for this entity.
     *
     * @return the user name for this entity.
     */
    public String getUserName() {
        return _userName;
    }

    /**
     * Set the user name for this entity.
     *
     * @param userName the user name for this entity.
     * @return this entity object
     */
    public MetadataFileDownload setUserName(String userName) {
        this._userName = userName;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetadataFileDownload that = (MetadataFileDownload) o;

        if (_id != that._id) return false;
        if (_metadataId != that._metadataId) return false;
        if (!_downloadDate.equals(that._downloadDate)) return false;
        if (!_fileName.equals(that._fileName)) return false;
        // if (!_metadataUuid.equals(that._metadataUuid)) return false;
        if (_requesterComments != null ? !_requesterComments.equals(that._requesterComments) : that._requesterComments != null)
            return false;
        if (_requesterMail != null ? !_requesterMail.equals(that._requesterMail) : that._requesterMail != null)
            return false;
        if (_requesterName != null ? !_requesterName.equals(that._requesterName) : that._requesterName != null)
            return false;
        if (_requesterOrg != null ? !_requesterOrg.equals(that._requesterOrg) : that._requesterOrg != null)
            return false;
        if (!_userName.equals(that._userName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _id;
        result = 31 * result + _metadataId;
        result = 31 * result + _downloadDate.hashCode();
        result = 31 * result + (_requesterName != null ? _requesterName.hashCode() : 0);
        result = 31 * result + (_requesterOrg != null ? _requesterOrg.hashCode() : 0);
        result = 31 * result + (_requesterMail != null ? _requesterMail.hashCode() : 0);
        result = 31 * result + (_requesterComments != null ? _requesterComments.hashCode() : 0);
        result = 31 * result + _userName.hashCode();
        result = 31 * result + _fileName.hashCode();
        //result = 31 * result + _metadataUuid.hashCode();
        return result;
    }
}
