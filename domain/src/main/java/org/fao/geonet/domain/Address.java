package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

/**
 * Represents an address
 * @author Jesse
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class Address {
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    public String getAddress() {
        return address;
    }
    public Address setAddress(String address) {
        this.address = address;
        return this;
    }
    public String getCity() {
        return city;
    }
    public Address setCity(String city) {
        this.city = city;
        return this;
    }
    public String getState() {
        return state;
    }
    public Address setState(String state) {
        this.state = state;
        return this;
    }
    public String getZip() {
        return zip;
    }
    public Address setZip(String zip) {
        this.zip = zip;
        return this;
    }
    public String getCountry() {
        return country;
    }
    public Address setCountry(String country) {
        this.country = country;
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
}
