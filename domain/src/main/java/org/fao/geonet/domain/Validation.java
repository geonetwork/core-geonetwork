package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * Entity representing metadata validation reports.
 * 
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
public class Validation {
    private ValidationId id;
    private int status;
    private int tested;
    private int failed;
    private String validationDate;

    @EmbeddedId
    public ValidationId getId() {
        return id;
    }

    public void setId(ValidationId id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTested() {
        return tested;
    }

    public void setTested(int tested) {
        this.tested = tested;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    @Column(name = "valdate", length = 30)
    public String getValidationDate() {
        return validationDate;
    }

    public void setValidationDate(String validationDate) {
        this.validationDate = validationDate;
    }
}
