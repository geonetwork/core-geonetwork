package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by jose on 18/03/14.
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class MetadataFileUploadId implements Serializable {
    private static final long serialVersionUID = -2277795832721906890L;
    private int _metadataId;
    private String _fileName;

    /**
     * Get the id of the metadata file upload is related to.
     *
     * @return the id of the metadata file upload is related to.
     */
    public int getMetadataId() {
        return _metadataId;
    }

    /**
     * Set the id of the metadata file upload is related to.
     *
     * @param metadataId the id of the metadata file upload is related to.
     * @return this id object
     */
    public MetadataFileUploadId setMetadataId(int metadataId) {
        this._metadataId = metadataId;
        return this;
    }

    /**
     * Get the id of the file upload.
     *
     * @return the id of the file upload.
     * @see MetadataFileUpload
     */
    public String getFileName() {
        return _fileName;
    }

    /**
     * Set the id of the file upload.
     *
     * @param fileName the file name
     * @return the id of the metadata file upload
     * @see MetadataFileUploadId
     */
    public MetadataFileUploadId setFileName(String fileName) {
        this._fileName = fileName;
        return this;
    }


    /**
     * Default constructor. Setters must be used to initialize object.
     */
    public MetadataFileUploadId() {
        // default constructor.
    }

    /**
     * Create a new instance from the required id objects.
     *
     * @param metadataId  the metadata id
     * @param fileName    the file name
     */
    public MetadataFileUploadId(int metadataId, String fileName) {
        this._metadataId = metadataId;
        this._fileName = fileName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _metadataId;
        result = prime * result + ((_fileName == null) ? 0 : _fileName.hashCode());
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
        MetadataFileUploadId other = (MetadataFileUploadId) obj;
        if (_metadataId != other._metadataId)
            return false;
        if (_fileName == null) {
            if (other._fileName != null)
                return false;
        } else if (!_fileName.equals(other._fileName))
            return false;
        return true;
    }
}
