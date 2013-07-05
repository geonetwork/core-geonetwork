package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Represents an address.
 * This is a JPA Embeddable object that is embedded into a {@link User} Entity
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
public class Address {
    private int _id;
    private String _address;
    private String _city;
    private String _state;
    private String _zip;
    private String _country;
    
    @Id
    @GeneratedValue
    public int getId() {
        return _id;
    }
    public void setId(int id) {
        this._id = id;
    }
    public String getAddress() {
        return _address;
    }
    public Address setAddress(String address) {
        this._address = address;
        return this;
    }
    public String getCity() {
        return _city;
    }
    public Address setCity(String city) {
        this._city = city;
        return this;
    }
    public String getState() {
        return _state;
    }
    public Address setState(String state) {
        this._state = state;
        return this;
    }
    public String getZip() {
        return _zip;
    }
    public Address setZip(String zip) {
        this._zip = zip;
        return this;
    }
    public String getCountry() {
        return _country;
    }
    public Address setCountry(String country) {
        this._country = country;
        return this;
    }
    /**
     * Merge data from other address into this one.
     *
     * @param otherAddress other address
     * @param mergeNullData  if true then also set null values from other address. If false then only merge non-null data
     */
    public void mergeAddress(Address otherAddress, boolean mergeNullData) {
        if (mergeNullData || otherAddress.getAddress()!= null){
            setAddress(otherAddress.getAddress());
        }
        if (mergeNullData || otherAddress.getCity()!= null){
            setCity(otherAddress.getCity());
        }
        if (mergeNullData || otherAddress.getState()!= null){
            setState(otherAddress.getState());
        }
        if (mergeNullData || otherAddress.getZip()!= null){
            setZip(otherAddress.getZip());
        }
        if (mergeNullData || otherAddress.getCountry()!= null){
            setCountry(otherAddress.getCountry());
        }
    }
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
    public boolean equals(Object obj) {
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

}
