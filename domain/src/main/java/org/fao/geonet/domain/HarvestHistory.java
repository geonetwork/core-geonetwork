package org.fao.geonet.domain;

import org.fao.geonet.entitylistener.HarvestHistoryEntityListenerManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.hibernate.annotations.Type;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.IOException;
import java.util.IdentityHashMap;

/**
 * An entity representing a harvesting task that may have been completed or possibly ending in error.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "HarvestHistory")
@EntityListeners(HarvestHistoryEntityListenerManager.class)
@SequenceGenerator(name=HarvestHistory.ID_SEQ_NAME, initialValue=100, allocationSize=1)
public class HarvestHistory extends GeonetEntity {
    static final String ID_SEQ_NAME = "harvest_history_id_seq";
    private int _id;
    private ISODate _harvestDate;
    private int _elapsedTime;
    private String _harvesterUuid;
    private String _harvesterName;
    private String _harvesterType;
    private char _deleted = Constants.YN_FALSE;
    private String _info;
    private String _params;

    /**
     * Get the id of the harvest history record. This is a generated value and as such new instances should not have this set as it will
     * simply be ignored and could result in reduced performance.
     *
     * @return the id
     */
    @Id
    @GeneratedValue (strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the harvest history record. This is a generated value and as such new instances should not have this set as it will
     * simply be ignored and could result in reduced performance.
     *
     * @param id the new id
     * @return this entity object
     */
    public HarvestHistory setId(int id) {
        this._id = id;
        return this;
    }

    /**
     * Get the date of the harvest. This is a string in order to maintain backwards compatibility.
     *
     * @return the harvest date
     */

    @AttributeOverride(name = "dateAndTime", column = @Column(name = "harvestdate", length = 30))
    public ISODate getHarvestDate() {
        return _harvestDate;
    }

    /**
     * Set the date of the harvest. This is a string in order to maintain backwards compatibility.
     *
     * @param harvestDate the new harvest date
     * @return this entity object
     */
    public HarvestHistory setHarvestDate(ISODate harvestDate) {
        this._harvestDate = harvestDate;
        return this;
    }

    /**
     * Get the time taken (in milliseconds) to complete the harvest.
     *
     * @return the time taken for the harvest.
     */
    @Column(name = "elapsedtime")
    public int getElapsedTime() {
        return _elapsedTime;
    }

    /**
     * Get the time taken (in milliseconds) to complete the harvest.
     *
     * @param elapsedTime the elapsed time
     * @return this entity object
     */
    public HarvestHistory setElapsedTime(int elapsedTime) {
        this._elapsedTime = elapsedTime;
        return this;
    }

    /**
     * Get the uuid of the harvester that performed the harvesting related to this record.
     *
     * @return the uuid
     */
    @Column(name = "harvesteruuid")
    public String getHarvesterUuid() {
        return _harvesterUuid;
    }

    /**
     * Set the uuid of the harvester that performed the harvesting related to this record.
     *
     * @param harvesterUuid The uuid of the harvester
     * @return this entity object
     */
    public HarvestHistory setHarvesterUuid(String harvesterUuid) {
        this._harvesterUuid = harvesterUuid;
        return this;
    }

    /**
     * Get the name of the harvester. This is a non-translated string and is used for UI purposes as a human readable identifier (as
     * opposed
     * to the harvester uuid)
     *
     * @return the name
     */
    @Column(name = "harvestername")
    public String getHarvesterName() {
        return _harvesterName;
    }

    /**
     * Set the name of the harvester. This is a non-translated string and is used for UI purposes as a human readable identifier (as
     * opposed
     * to the harvester uuid)
     *
     * @param harvesterName the name of the harvester
     * @return this entity object
     */
    public HarvestHistory setHarvesterName(String harvesterName) {
        this._harvesterName = harvesterName;
        return this;
    }

    /**
     * Get the harvester type (for example CSW, WFS, WMS, Webdav etc...)
     *
     * @return the harvester type
     */
    @Column(name = "harvestertype")
    public String getHarvesterType() {
        return _harvesterType;
    }

    /**
     * Set the harvester type (for example CSW, WFS, WMS, Webdav etc...)
     *
     * @param harvesterType the type of the harvester
     * @return this entity object
     */
    public HarvestHistory setHarvesterType(String harvesterType) {
        this._harvesterType = harvesterType;
        return this;
    }

    /**
     * For backwards compatibility we need the deleted column to be either 'n' or 'y'. This is a workaround to allow this until future
     * versions of JPA that allow different ways of controlling how types are mapped to the database.
     */
    @Column(name = "deleted", nullable = false, length = 1)
    protected char getDeleted_JpaWorkaround() {
        return _deleted;
    }

    protected char setDeleted_JpaWorkaround(char deleted) {
        return _deleted = deleted;
    }

    /**
     * Return true is the record has been deleted.
     *
     * @return true is the record has been deleted.
     */
    @Transient
    public boolean isDeleted() {
        return Constants.toBoolean_fromYNChar(getDeleted_JpaWorkaround());
    }

    /**
     * Set true if is the record has been deleted.
     *
     * @param deleted if true then this indicates the record is deleted.
     * @return this entity object
     */
    public HarvestHistory setDeleted(boolean deleted) {
        setDeleted_JpaWorkaround(Constants.toYN_EnabledChar(deleted));
        return this;
    }

    /**
     * Get arbitrary harvester specific information about the harvesting.
     *
     * @return the harvester info.
     */
    @Lob
    @Type(type="org.hibernate.type.StringClobType") // this is a work around for postgres so postgres can correctly load clobs
    public String getInfo() {
        return _info;
    }

    /**
     * Get arbitrary harvester specific information about the harvesting.
     *
     * @return the harvester info.
     */
    @Transient
    @Nullable
    public Element getInfoAsXml() throws IOException, JDOMException {
        final String info = getInfo();
        if (info == null) {
            return null;
        }
        return Xml.loadString(info, false);
    }

    /**
     * Set the arbitrary harvester specific data about the harvesting. Can be error information or other related information.
     *
     * @param info the information to store.
     * @return this entity object
     */
    protected HarvestHistory setInfo(String info) {
        this._info = info;
        return this;
    }
    /**
     * Set the arbitrary harvester specific data about the harvesting. Can be error information or other related information.
     *
     *
     * @param info the information to store.
     * @return this entity object.
     */
    @Nonnull
    public HarvestHistory setInfo(@Nullable Element info) {
        if (info == null) {
            setInfo((String)null);
        } else {
            setInfo(Xml.getString(info));
        }
        return this;
    }

    /**
     * Get the parameters used for performing the harvesting. As of 2.10 this is XML from the setting table. Future versions will likely
     * change this.
     *
     * @return the parameters used for performing the harvesting.
     */
    @Lob
    @Type(type="org.hibernate.type.StringClobType") // this is a work around for postgres so postgres can correctly load clobs
    public String getParams() {
        return _params;
    }

    /**
     * Get the parameters used for performing the harvesting. As of 2.10 this is XML from the setting table. Future versions will likely
     * change this.
     *
     * @return the parameters used for performing the harvesting.
     */
    @Transient
    @Nullable
    public Element getParamsAsXml() throws IOException, JDOMException {
        final String params = getParams();
        if (params == null) {
            return null;
        }
        return Xml.loadString(params, false);
    }

    /**
     * Set the parameters used for performing the harvesting.
     *
     * @param params The parameters
     * @return this entity object
     */
    protected HarvestHistory setParams(final String params) {
        this._params = params;
        return this;
    }
    /**
     * Set the parameters used for performing the harvesting.
     *
     * @param params The parameters
     * @return this entity object
     */
    public HarvestHistory setParams(final Element params) {
        if (params == null) {
            setParams((String) null);
        } else {
            setParams(Xml.getString(params));
        }
        return this;
    }


    @Override
    protected Element asXml(IdentityHashMap<Object, Void> alreadyEncoded) {
        final Element element = super.asXml(alreadyEncoded);


        Element infoAsXml = null;
        try {
            infoAsXml = getInfoAsXml();
        } catch (IOException e) {
            Log.warning(Constants.DOMAIN_LOG_MODULE, "error parsing harvest history info element", e);
        } catch (JDOMException e) {
            Log.warning(Constants.DOMAIN_LOG_MODULE, "error parsing harvest history info element", e);
        }
        Element paramsAsXml = null;
        try {
            paramsAsXml = getParamsAsXml();
        } catch (IOException e) {
            Log.warning(Constants.DOMAIN_LOG_MODULE, "error parsing harvest history params element", e);
        } catch (JDOMException e) {
            Log.warning(Constants.DOMAIN_LOG_MODULE, "error parsing harvest history params element", e);
        }
        replaceWithXml(element, infoAsXml, "info");
        replaceWithXml(element, paramsAsXml, "params");
        return element;
    }

    private void replaceWithXml(Element element, Content asXml, String tagName) {
        final Element info = element.getChild(tagName);
        if (info != null) {
            if (asXml != null) {
                info.removeContent();
                info.addContent(asXml);
            } else {
                info.detach();
            }
        }
    }
}
