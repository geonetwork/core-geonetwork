package org.fao.geonet.domain;

import javax.persistence.*;

/**
 * Entity representing metadata validation reports.
 * 
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "validation")
public class MetadataValidation {
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
     */
    public void setId(MetadataValidationId id) {
        this._id = id;
    }

    /**
     * Get the validation status for this entity.
     *
     * @return the validation status for this entity.
     */
    @Column(nullable=false)
    public MetadataValidationStatus getStatus() {
        return _status;
    }

    /**
     * Set the validation status for this entity.
     * @param status the validation status for this entity.
     */
    public void setStatus(MetadataValidationStatus status) {
        this._status = status;
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
     * @param isValid
     */
    public void setValid(boolean isValid) {
        setStatus(isValid ? MetadataValidationStatus.VALID : MetadataValidationStatus.INVALID);
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
     * @param tested
     */
    public void setTested(int tested) {
        this._tested = tested;
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
     */
    public void setFailed(int failed) {
        this._failed = failed;
    }

    /**
     * The moment that the validation completed.
     *
     * @return The moment that the validation completed.
     */
    @AttributeOverride(name="dateAndTime", column = @Column(name = "valdate", length = 30) )
    public ISODate getValidationDate() {
        return _validationDate;
    }

    /**
     * 
     * @param validationDate
     */
    public void setValidationDate(ISODate validationDate) {
        this._validationDate = validationDate;
    }
}
