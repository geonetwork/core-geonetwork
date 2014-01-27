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
    private ISODate _validationDate;

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
     * TODO DOC: The purpose for this is unknown as it is always 0 at the this class is created.
     *
     * @return
     */
    public int getTested() {
        return _tested;
    }

    /**
     * TODO DOC: The purpose for this is unknown as it is always 0 at the this class is created.
     *
     * @param tested
     * @return this entity object
     */
    public MetadataValidation setTested(int tested) {
        this._tested = tested;
        return this;
    }

    /**
     * TODO DOC: The purpose for this is unknown as it is always 0 at the this class is created.
     *
     * @return
     */
    public int getFailed() {
        return _failed;
    }

    /**
     * TODO DOC: The purpose for this is unknown as it is always 0 at the this class is created.
     *
     * @param failed
     * @return this entity object
     */
    public MetadataValidation setFailed(int failed) {
        this._failed = failed;
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
}
