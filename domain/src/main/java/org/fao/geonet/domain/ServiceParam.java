package org.fao.geonet.domain;

import com.google.common.collect.Lists;

import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * One of the entities responsible for dynamic service configuration.
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "ServiceParameters")
public class ServiceParam extends GeonetEntity {
    private static final List<Character> LEGALVALUES = Lists.newArrayList('+', '-', ' ', null);
    private ServiceParamId id;
    private Service service;
    private Character occur = '+';

    public ServiceParam() {
        // for JPA
    }
    public ServiceParam(String name, String value) {
        this.id = new ServiceParamId(name, value);
    }


    @EmbeddedId
    public ServiceParamId getId() {
        return id;
    }

    public void setId(ServiceParamId id) {
        this.id = id;
    }

    /**
     * Get the request associated with this entity.
     *
     * @return the request associated with this entity.
     */
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "id", name = "service")
    public Service getService() {
        return this.service;
    }

    /**
     * Set the request associated with this entity.
     *
     * @param service the request associated with this entity.
     */
    public void setService(Service service) {
        this.service = service;
    }

    /**
     * Get the "occur" property which determines how this clause affects the filtering.
     * The allowed options are '+', '-', and ' '.  Where
     * <ul>
     *     <li>'+' means that the clause is required for a result to be included</li>
     *     <li>' ' means that the clause is should true, this has the effect of increasing relevancy of a particular
     *     result in the overall result set</li>
     *     <li>'-' means that the clause is must be false for a result to be included</li>
     * </ul>
     */
    public Character getOccur() {
        if (this.occur == null) {
            return '+';
        }
        return occur;
    }

    /**
     * Set occur property.
     * @param occur the new value.  Legal values: '+', ' ', '-'
     */
    public void setOccur(Character occur) {
        if (!LEGALVALUES.contains(occur)) {
            throw new IllegalArgumentException(occur + " is not a legal occur value.  Legal values are: " + LEGALVALUES);
        }
        this.occur = occur;
    }
}

