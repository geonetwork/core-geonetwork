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
@Table(name = "Validation",
    indexes = { @Index(name = "idx_validation_metadataid", columnList = "metadataid") })
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
     * Set the status as either {@link MetadataValidationStatus#VALID) or {@link
     * MetadataValidationStatus#INVALID)
     *
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
     * Set if this type of validation is required for the metadata to be considered valid.  Some
     * validation tests are informational only (see {@link org.fao.geonet.domain.Schematron}) if the
     * test is informational only then required is false and it will not affect the metadata's
     * overall validity.
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
