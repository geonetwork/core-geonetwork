package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Entity representing metadata validation reports.
 * 
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
public class Validation {
    private ValidationId _id;
    private ValidationStatus _status;
    private int _tested;
    private int _failed;
    private ISODate _validationDate;

    /**
     * Return the id object of this entity. 
     * 
     * @return the id object of this entity
     */
    @EmbeddedId
    public ValidationId getId() {
        return _id;
    }

    /**
     * Set the id object of this entity. 
     * 
     * @param id the id object of this entity. 
     */
    public void setId(ValidationId id) {
        this._id = id;
    }

    /**
     * Get the validation status for this entity.
     *
     * @return the validation status for this entity.
     */
    @Column(nullable=false)
    public ValidationStatus getStatus() {
        return _status;
    }

    /**
     * Set the validation status for this entity.
     * @param status the validation status for this entity.
     */
    public void setStatus(ValidationStatus status) {
        this._status = status;
    }

    /**
     * Returns true if {@link #getStatus() == {@link ValidationStatus#VALID)
     * 
     * @return true if {@link #getStatus() == {@link ValidationStatus#VALID)
     */
    @Transient
    public boolean isValid() {
        return getStatus() == ValidationStatus.VALID;
    }
    
    /**
     * Set the status as either {@link ValidationStatus#VALID) or
     * {@link ValidationStatus#INVALID)
     * @param isValid
     */
    public void setValid(boolean isValid) {
        setStatus(isValid ? ValidationStatus.VALID : ValidationStatus.INVALID);
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
