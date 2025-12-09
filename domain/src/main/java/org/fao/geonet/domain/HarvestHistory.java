/*
 * Copyright (C) 2001-2020 Food and Agriculture Organization of the
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

import org.fao.geonet.entitylistener.HarvestHistoryEntityListenerManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.hibernate.annotations.Type;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

/**
 * An entity representing a harvesting task that may have been completed or possibly ending in
 * error.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = HarvestHistory.TABLE_NAME)
@EntityListeners(HarvestHistoryEntityListenerManager.class)
@SequenceGenerator(name = HarvestHistory.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class HarvestHistory extends GeonetEntity {
    public static final String TABLE_NAME = "HarvestHistory";
    static final String ID_SEQ_NAME = "harvest_history_id_seq";
    public static final String HARVEST_DATE_COLUMN_NAME = "harvestdate";
    public static final String ID_COLUMN_NAME = "id";
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
     * Get the id of the harvest history record. This is a generated value and as such new instances
     * should not have this set as it will simply be ignored and could result in reduced
     * performance.
     *
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(name = ID_COLUMN_NAME)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the harvest history record. This is a generated value and as such new instances
     * should not have this set as it will simply be ignored and could result in reduced
     * performance.
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

    @AttributeOverride(name = "dateAndTimeUtc", column = @Column(name = HARVEST_DATE_COLUMN_NAME, length = 30))
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
     * Get the name of the harvester. This is a non-translated string and is used for UI purposes as
     * a human readable identifier (as opposed to the harvester uuid)
     *
     * @return the name
     */
    @Column(name = "harvestername")
    public String getHarvesterName() {
        return _harvesterName;
    }

    /**
     * Set the name of the harvester. This is a non-translated string and is used for UI purposes as
     * a human readable identifier (as opposed to the harvester uuid)
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
     * For backwards compatibility we need the deleted column to be either 'n' or 'y'. This is a
     * workaround to allow this until future versions of JPA that allow different ways of
     * controlling how types are mapped to the database.
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
    @Type(type = "org.hibernate.type.TextType")
    // this is a work around for postgres so postgres can correctly load clobs
    public String getInfo() {
        return _info;
    }

    /**
     * Set the arbitrary harvester specific data about the harvesting. Can be error information or
     * other related information.
     *
     * @param info the information to store.
     * @return this entity object.
     */
    @Nonnull
    public HarvestHistory setInfo(@Nullable Element info) {
        if (info == null) {
            setInfo((String) null);
        } else {
            setInfo(Xml.getString(info));
        }
        return this;
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
        Element infoAsXml = Xml.loadString(info, false);
        infoAsXml = checkInfoXml(infoAsXml);
        return infoAsXml;
    }

    /**
     * Check infoAsXml definition content.
     * <p>
     * Content checks:
     * <ul>
     * <li>Check if multiple logfile definitions are present</li>
     * <li>Check if logfile path file exists</li>
     * </ul>
     *
     * </p>
     *
     * @param infoAsXml Harvest info xml definition
     * @return Harvest info xml definition, with any content checks applied
     */
    private Element checkInfoXml(Element infoAsXml) {
        List<Element> logfileElements = infoAsXml.getChildren("logfile");
        // check logfiles present to ensure path exists
        // TODO: Would require to check in the app log folder
       if (logfileElements.size() > 0) {
           if (logfileElements.size() != 1) {
               Log.debug(Constants.DOMAIN_LOG_MODULE, "Harvest history unexpectedly lists multiple logfiles: " + logfileElements.size());
           }
           boolean isLogFileFound = false;
           for (Iterator iter = logfileElements.iterator(); iter.hasNext();) {
               Element logfile = (Element) iter.next();
               String path = Paths.get(Log.getLogfile().getParent()).resolve(logfile.getText()).toString();
               File file = new File(path);
               if (file.exists() && file.canRead()) {
                   if (isLogFileFound) {
                       // we already have one logfile
                       Log.debug(Constants.DOMAIN_LOG_MODULE, "Ignoring add unexpected logfile: `" + path + "`");
                       iter.remove();
                   } else {
                       isLogFileFound = true;
                   }
               } else {
                   Log.debug(Constants.DOMAIN_LOG_MODULE, "Harvest history logfile `" + path + "` ignored, no longer available");
                   iter.remove();
               }
           }
       }
        return infoAsXml;
    }

    /**
     * Set the arbitrary harvester specific data about the harvesting. Can be error information or
     * other related information.
     *
     * @param info the information to store.
     * @return this entity object
     */
    protected HarvestHistory setInfo(String info) {
        this._info = info;
        return this;
    }

    /**
     * Get the parameters used for performing the harvesting. As of 2.10 this is XML from the
     * setting table. Future versions will likely change this.
     *
     * @return the parameters used for performing the harvesting.
     */
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    // this is a work around for postgres so postgres can correctly load clobs
    public String getParams() {
        return _params;
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

    /**
     * Get the parameters used for performing the harvesting. As of 2.10 this is XML from the
     * setting table. Future versions will likely change this.
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
