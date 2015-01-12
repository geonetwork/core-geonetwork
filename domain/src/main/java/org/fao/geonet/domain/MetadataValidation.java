package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.MetadataValidationEntityListenerManager;

import javax.annotation.Nonnull;
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
    private MetadataValidationId id;
    private MetadataValidationStatus status;
    private int numTests = 0;
    private int numFailures = 0;
    private ISODate validationDate = new ISODate();
    private Boolean required = Boolean.TRUE;

    /**
     * Return the id object of this entity.
     *
     * @return the id object of this entity
     */
    @EmbeddedId
    public MetadataValidationId getId() {
        return id;
    }

    /**
     * Set the id object of this entity.
     *
     * @param id the id object of this entity.
     * @return this entity object
     */
    public MetadataValidation setId(MetadataValidationId id) {
        this.id = id;
        return this;
    }

    /**
     * Get the validation status for this entity.
     *
     * @return the validation status for this entity.
     */
    @Column(nullable = false)
    public MetadataValidationStatus getStatus() {
        return status;
    }

    /**
     * Set the validation status for this entity.
     *
     * @param status the validation status for this entity.
     * @return this entity object
     */
    public MetadataValidation setStatus(MetadataValidationStatus status) {
        this.status = status;
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
        return validationDate;
    }

    /**
     * @param validationDate
     * @return this entity object
     */
    public MetadataValidation setValidationDate(ISODate validationDate) {
        this.validationDate = validationDate;
        return this;

    }

    public MetadataValidation setRequired(Boolean required) {
        this.required = required;
        return this;
    }

    /**
     * Set if this type of validation is required for the metadata to be considered valid.  Some validation tests are informational
     * only (see {@link org.fao.geonet.domain.Schematron}) if the test is informational only then required is false and it will not
     * affect the metadata's overall validity.
     */
    @Column(nullable = true)
    @Nonnull
    public Boolean isRequired() {
        return required == null ? Boolean.TRUE : required;
    }

    /**
     * Get the number of tests executed.
     */
    @Column(name = "tested")
    public int getNumTests() {
        return numTests;
    }

    /**
     * Set the number of tests executed
     *
     * @return this entity object
     */
    public MetadataValidation setNumTests(int numTests) {
        this.numTests = numTests;
        return this;
    }

    /**
     * Get the number of assertion/test failures.
     */
    @Column(name = "failed")
    public int getNumFailures() {
        return numFailures;
    }

    /**
     * Set the number of assertion/test failures.
     *
     * @return this entity object
     */
    public MetadataValidation setNumFailures(int numFailures) {
        this.numFailures = numFailures;
        return this;
    }

    @Override
    public String toString() {
        return "MetadataValidation{" + id +
               ", status=" + status +
               ", numTests=" + numTests +
               ", numFailures=" + numFailures +
               ", validationDate=" + validationDate +
               ", required=" + required +
               '}';
    }
}
