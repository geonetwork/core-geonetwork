package org.fao.geonet.domain;

import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 * Id for service parameter.
 *
 * @author Jesse on 3/26/2015.
 */
@Embeddable
public class ServiceParamId implements Serializable {
    private static final long serialVersionUID = -1156594864816593097L;
    private String name;
    private String value;

    public ServiceParamId(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public ServiceParamId() {
        // for JPA
    }

    /**
     * Get the name of the service.
     *
     * @return the name of the service.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name of the service.
     *
     * @param name the name of the service.
     */
    public ServiceParamId setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the parameter value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the parameter value
     */
    public ServiceParamId setValue(String value) {
        this.value = value;
        return this;
    }

}
