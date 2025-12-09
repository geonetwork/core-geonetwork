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

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.entitylistener.AddressEntityListenerManager;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 * Represents an address. This is a JPA Entity object and is contained in a database table.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@EntityListeners(AddressEntityListenerManager.class)
@SequenceGenerator(name = Address.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class Address extends GeonetEntity implements Serializable {
    static final String ID_SEQ_NAME = "address_id_seq";

    private static final int ZIP_COLUMN_LENGTH = 16;
    private int _id;
    private String _address;
    private String _city;
    private String _state;
    private String _zip;
    private String _country;

    /**
     * Id of the address. This is automatically generated so when creating a new object leave this
     * blank and allow the database or JPA set the value for you on save.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the address. This is automatically generated so when creating a new object
     * leave this blank and allow the database or JPA set the value for you on save.
     *
     * @param id the id
     * @return this address object
     */
    public Address setId(final int id) {
        this._id = id;
        return this;
    }

    /**
     * Get the address line of the address. This is typically the street name and number but varies
     * depending on the type of address.
     */
    public String getAddress() {
        return _address;
    }

    /**
     * Set the address data. See {@link #getAddress()} for details on what the "address" is.
     *
     * @param address the new address data
     * @return this address object
     */
    public Address setAddress(final String address) {
        this._address = address;
        return this;
    }

    /**
     * Get the city of the address.
     */
    public String getCity() {
        return _city;
    }

    /**
     * Set the city of the address.
     *
     * @param city the city
     * @return this address object
     */
    public Address setCity(final String city) {
        this._city = city;
        return this;
    }

    /**
     * Return the state/province/Kantone/departement/etc... of the address.
     *
     * @return the state of the address.
     */
    public String getState() {
        return _state;
    }

    /**
     * Return the state/province/Kantone/departement/etc... of the address.
     *
     * @param state the state
     * @return this address object
     */
    public Address setState(final String state) {
        this._state = state;
        return this;
    }

    /**
     * Return the zip/postal code of the address.
     *
     * @return the zip/postal code
     */
    @Column(length = ZIP_COLUMN_LENGTH)
    public String getZip() {
        return _zip;
    }

    /**
     * Set the zip/postal code of the address.
     *
     * @param zip the new value
     * @return this address object
     */
    public Address setZip(final String zip) {
        this._zip = zip;
        return this;
    }

    /**
     * Get iso2 country code of the address.
     *
     * @return the iso2 country code.
     */
    public String getCountry() {
        return _country;
    }

    /**
     * Set the iso2 country code of the address.
     *
     * @param iso2CountryCode the iso 2 country code.
     * @return this address object
     */
    public Address setCountry(final String iso2CountryCode) {
        this._country = iso2CountryCode;
        return this;
    }

    /**
     * Merge data from other address into this one.
     *
     * @param otherAddress  other address
     * @param mergeNullData if true then also set null values from other address. If false then only
     *                      merge non-null data
     */
    public void mergeAddress(final Address otherAddress, final boolean mergeNullData) {
        if (mergeNullData || StringUtils.isNotBlank(otherAddress.getAddress())) {
            setAddress(otherAddress.getAddress());
        }
        if (mergeNullData || StringUtils.isNotBlank(otherAddress.getCity())) {
            setCity(otherAddress.getCity());
        }
        if (mergeNullData || StringUtils.isNotBlank(otherAddress.getState())) {
            setState(otherAddress.getState());
        }
        if (mergeNullData || StringUtils.isNotBlank(otherAddress.getZip())) {
            setZip(otherAddress.getZip());
        }
        if (mergeNullData || StringUtils.isNotBlank(otherAddress.getCountry())) {
            setCountry(otherAddress.getCountry());
        }
    }
    // CSOFF: AvoidInlineConditionals
    // CSOFF: NeedBraces

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (_id != address._id) return false;
        if (_address != null ? !_address.equals(address._address) : address._address != null)
            return false;
        if (_city != null ? !_city.equals(address._city) : address._city != null) return false;
        if (_country != null ? !_country.equals(address._country) : address._country != null)
            return false;
        if (_state != null ? !_state.equals(address._state) : address._state != null) return false;
        if (_zip != null ? !_zip.equals(address._zip) : address._zip != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _id;
        result = 31 * result + (_address != null ? _address.hashCode() : 0);
        result = 31 * result + (_city != null ? _city.hashCode() : 0);
        result = 31 * result + (_state != null ? _state.hashCode() : 0);
        result = 31 * result + (_zip != null ? _zip.hashCode() : 0);
        result = 31 * result + (_country != null ? _country.hashCode() : 0);
        return result;
    }

    // CSON: AvoidInlineConditionals
    // CSON: NeedBraces
}
