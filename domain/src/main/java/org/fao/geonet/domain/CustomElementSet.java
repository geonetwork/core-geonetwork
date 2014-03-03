package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.CustomElementSetEntityListenerManager;

import javax.persistence.*;

/**
 * Csw custom element set. This is part of the CSW specification related to what elements are returned by GetRecords and GetRecordById.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "CustomElementSet")
@EntityListeners(CustomElementSetEntityListenerManager.class)
public class CustomElementSet extends GeonetEntity {
    private static final int XPATH_COLUMN_LENGTH = 1000;
    private String _xpath;
    private int _xpathHashcode;

    /**
     * The hashcode of the xpath.  This has to be the id because mysql + JPA have a problem with a Ids longer than 255 characters.
     *
     * @return the hashcode of the xpath.
     */
    @Id
    public int getXpathHashcode() {
        return _xpathHashcode;
    }

    /**
     * Set the xpath hashcode.
     * Method is protected because it is set when calling setXPath.
     *
     * @param xpathHashcode the hashcode.
     */
    protected void setXpathHashcode(int xpathHashcode) {
        this._xpathHashcode = xpathHashcode;
    }

    /**
     * Get the xpath of the element to include in the element set. Each included element is described by a full xpath relative to the
     * document root. <br/>
     * This is a required element.
     */
    @Column(length = XPATH_COLUMN_LENGTH, nullable = false)
    public String getXpath() {
        return _xpath;
    }

    /**
     * Get the xpath of the element to include in the element set. Each included element is described by a full xpath relative to the
     * document root. <br/>
     * This is a required element.
     *
     * @param xpath the xpath relative to document root.
     * @return this object
     */
    public CustomElementSet setXpath(final String xpath) {
        this._xpath = xpath;
        setXpathHashcode(xpath.hashCode());
        return this;
    }
}
