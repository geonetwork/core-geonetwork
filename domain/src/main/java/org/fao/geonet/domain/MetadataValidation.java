package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.MetadataValidationEntityListenerManager;

import javax.persistence.*;

/**
 * Entity representing metadata validation reports.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "Validation")
@EntityListeners(MetadataValidationEntityListenerManager.class)
public class MetadataValidation extends GeonetEntity {
    private MetadataValidationId _id;
    private MetadataValidationStatus _status;
    private int _tested;
    private int _failed;
    private ISODate _validationDate = new ISODate();
    private boolean required = true;

    /**
     * Return the id object of this entity.
     *
     * @return the id object of this entity
     */
    @EmbeddedId
    public MetadataValidationId getId() {
        return _id;
    }

    /**
     * Set the id object of this entity.
     *
     * @param id the id object of this entity.
     * @return this entity object
     */
    public MetadataValidation setId(MetadataValidationId id) {
        this._id = id;
        return this;
    }

    /**
     * Get the validation status for this entity.
     *
     * @return the validation status for this entity.
     */
    @Column(nullable = false)
    public MetadataValidationStatus getStatus() {
        return _status;
    }

    /**
     * Set the validation status for this entity.
     *
     * @param status the validation status for this entity.
     * @return this entity object
     */
    public MetadataValidation setStatus(MetadataValidationStatus status) {
        this._status = status;
        return this;
    }

    /**
     * Returns true if {@link #getStatus() == {@link MetadataValidationStatus#VALID)
     *
     * @return true if {@link #getStatus() == {@link MetadataValidationStatus#VALID)
     */
    @Transient
    public boolean isValid() {
        return getStatus() == MetadataValidationStatus.VALID;
    }

    /**
     * Set the status as either {@link MetadataValidationStatus#VALID) or
     * {@link MetadataValidationStatus#INVALID)
     *
     * @param isValid
     * @return this entity object
     */
    public MetadataValidation setValid(boolean isValid) {
        setStatus(isValid ? MetadataValidationStatus.VALID : MetadataValidationStatus.INVALID);
        return this;
    }

    /**
     * The moment that the validation completed.
     *
     * @return The moment that the validation completed.
     */
    @AttributeOverride(name = "dateAndTime", column = @Column(name = "valDate", length = 30))
    public ISODate getValidationDate() {
        return _validationDate;
    }

    /**
     * @param validationDate
     * @return this entity object
     */
    public MetadataValidation setValidationDate(ISODate validationDate) {
        this._validationDate = validationDate;
        return this;

    }

    public MetadataValidation setRequired(boolean required) {
        this.required = required;
        return this;
    }

    /**
     * Set if this type of validation is required for the metadata to be considered valid.  Some validation tests are informational
     * only (see {@link org.fao.geonet.domain.Schematron}) if the test is informational only then required is false and it will not
     * affect the metadata's overall validity.
     */
    @Column(nullable = true)
    public boolean isRequired() {
        return required;
    }

    @Override
    public String toString() {
        String reqString = required ? "required" : "not-required";
        return "MetadataValidation{" + _id.getMetadataId() + ", " + _id.getValidationType()+ ", " + _status + ", " + reqString + "}";
    }
}
