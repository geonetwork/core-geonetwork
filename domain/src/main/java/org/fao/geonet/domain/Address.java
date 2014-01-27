package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.AddressEntityListenerManager;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Represents an address. This is a JPA Entity object and is contained in a database table.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@EntityListeners(AddressEntityListenerManager.class)
@SequenceGenerator(name=Address.ID_SEQ_NAME, initialValue=100, allocationSize=1)
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
     * Id of the address. This is automatically generated so when creating a new object leave this blank and allow the database or JPA set
     * the value for you on save.
     */
    @Id
    @GeneratedValue (strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the address. This is automatically generated so when creating a new object leave this blank and allow the database or
     * JPA set the value for you on save.
     *
     * @param id the id
     * @return this address object
     */
    public Address setId(final int id) {
        this._id = id;
        return this;
    }

    /**
     * Get the address line of the address. This is typically the street name and number but varies depending on the type of address.
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
     * @param mergeNullData if true then also set null values from other address. If false then only merge non-null data
     */
    public void mergeAddress(final Address otherAddress, final boolean mergeNullData) {
        if (mergeNullData || otherAddress.getAddress() != null) {
            setAddress(otherAddress.getAddress());
        }
        if (mergeNullData || otherAddress.getCity() != null) {
            setCity(otherAddress.getCity());
        }
        if (mergeNullData || otherAddress.getState() != null) {
            setState(otherAddress.getState());
        }
        if (mergeNullData || otherAddress.getZip() != null) {
            setZip(otherAddress.getZip());
        }
        if (mergeNullData || otherAddress.getCountry() != null) {
            setCountry(otherAddress.getCountry());
        }
    }
    // CSOFF: AvoidInlineConditionals
    // CSOFF: NeedBraces
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_address == null) ? 0 : _address.hashCode());
        result = prime * result + ((_city == null) ? 0 : _city.hashCode());
        result = prime * result + ((_country == null) ? 0 : _country.hashCode());
        result = prime * result + _id;
        result = prime * result + ((_state == null) ? 0 : _state.hashCode());
        result = prime * result + ((_zip == null) ? 0 : _zip.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Address other = (Address) obj;
        if (_address == null) {
            if (other._address != null)
                return false;
        } else if (!_address.equals(other._address))
            return false;
        if (_city == null) {
            if (other._city != null)
                return false;
        } else if (!_city.equals(other._city))
            return false;
        if (_country == null) {
            if (other._country != null)
                return false;
        } else if (!_country.equals(other._country))
            return false;
        if (_id != other._id)
            return false;
        if (_state == null) {
            if (other._state != null)
                return false;
        } else if (!_state.equals(other._state))
            return false;
        if (_zip == null) {
            if (other._zip != null)
                return false;
        } else if (!_zip.equals(other._zip))
            return false;
        return true;
    }
    // CSON: AvoidInlineConditionals
    // CSON: NeedBraces
}
