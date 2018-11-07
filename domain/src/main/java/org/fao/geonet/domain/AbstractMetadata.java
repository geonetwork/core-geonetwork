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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import org.apache.lucene.document.Document;
import org.fao.geonet.utils.Xml;
import org.hibernate.annotations.Type;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.vividsolutions.jts.util.Assert;

/**
 * An entity representing a metadata object in the database. The xml, groups and operations are lazily loaded so accessing then will need to
 * be done in a thread that has a bound EntityManager.
 * 
 * Also they can trigger database access if they have not been cached and therefore can cause slowdowns so they should only be accessed in
 * need.
 *
 * All classes/tables implemented will share by default the same sequence for the ID. So you can have different kinds of metadata (like
 * original and draft versioning) and the id will be unique on all the tables at the same time.
 *
 * @author María Arias de Reyna
 */
@MappedSuperclass
public abstract class AbstractMetadata extends GeonetEntity {
    static final String ID_SEQ_NAME = "metadata_id_seq";
    public static final String METADATA_CATEG_JOIN_TABLE_CATEGORY_ID = "categoryId";
    private int _id;
    private String _uuid;
    private String _data;
    private MetadataDataInfo _dataInfo = new MetadataDataInfo();
    private MetadataSourceInfo _sourceInfo = new MetadataSourceInfo();
    private MetadataHarvestInfo _harvestInfo = new MetadataHarvestInfo();

    /**
     * Get the id of the metadata. This is a generated value and as such new instances should not have this set as it will simply be ignored
     * and could result in reduced performance.
     *
     * @return the id of the metadata
     */
    @Id
    @SequenceGenerator(name=AbstractMetadata.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(nullable = false)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the metadata. This is a generated value and as such new instances should not have this set as it will simply be ignored
     * and could result in reduced performance.
     *
     * @param _id the id of the metadata
     * @return this entity object
     */
    public AbstractMetadata setId(int _id) {
        this._id = _id;
        return this;
    }

    /**
     * Get the uuid of the metadata. This is a required property and thus must not be null.
     *
     * @return the uuid of the metadata.
     */
    @Column(nullable = false, unique = true)
    @Nonnull
    public String getUuid() {
        return _uuid;
    }

    /**
     * Set the metadata uuid.
     *
     * @param uuid the new uuid of the metadata
     * @return this eneity object
     */
    @Nonnull
    public AbstractMetadata setUuid(@Nonnull String uuid) {
        Assert.isTrue(uuid != null, "Cannot have null uuid");
        this._uuid = uuid;
        return this;
    }

    /**
     * Get the metadata data as a string (typically XML)
     *
     * @return the metadata data as a string.
     */
    @Column(nullable = false)
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Type(type = "org.hibernate.type.StringClobType") // this is a work around for postgres so postgres can correctly load clobs
    public String getData() {
        return _data;
    }

    /**
     * Set the metadata data as a string (typically XML).
     *
     * Warning: Do not use it when the user is not authenticated.
     *
     * When using this method be sure that the data to be persisted are the complete metadata record. For example, if the current user in
     * session is not authenticated and element filters are applied (eg. withheld), do not set the data with the response of
     * {@link org.fao.geonet.kernel.DataManager#getMetadata} in such case as the original content may be altered.
     *
     * Use XmlSerializer instead in an authenticated session.
     *
     * @param data the data for this metadata record.
     * @return this metadata entity.
     */
    public AbstractMetadata setData(String data) {
        this._data = data;
        return this;
    }

    /**
     * Set the data and convert all the end of line characters to be only a \n character.
     *
     * Warning: Do not use it when the user is not authenticated.
     *
     * Use XmlSerializer instead in an authenticated session.
     *
     * @param xml the data as XML.
     * @return this entity.
     */
    public AbstractMetadata setDataAndFixCR(Element xml) {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

        String data = outputter.outputString(fixCR(xml));
        setData(data);

        return this;
    }

    private Element fixCR(Element xml) {
        List<?> list = xml.getChildren();
        if (list.size() == 0) {
            String text = xml.getText();
            xml.setText(replaceString(text, "\r\n", "\n"));
        } else {
            for (Object o : list) {
                fixCR((Element) o);
            }
        }
        return xml;
    }

    /**
     * Parse the data as xml and return the data.
     *
     * @param validate if true validate the XML while parsing.
     * @return the parsed metadata.
     * @throws IOException
     * @throws JDOMException
     */
    @Transient
    public Element getXmlData(boolean validate) throws IOException, JDOMException {
        return Xml.loadString(getData(), validate);
    }

    private static String replaceString(final String initialString, final String pattern, final String replacement) {
        StringBuilder result = new StringBuilder();
        String remainingString = initialString;
        int i;

        while ((i = remainingString.indexOf(pattern)) != -1) {
            result.append(remainingString.substring(0, i));
            result.append(replacement);
            remainingString = remainingString.substring(i + pattern.length());
        }

        result.append(remainingString);
        return result.toString();
    }

    /**
     * Get the object representing metadata about the metadata (metadata creation date, etc...)
     *
     * @return the {@link MetadataDataInfo} for the metadata entity.
     */
    @Embedded
    public MetadataDataInfo getDataInfo() {
        return _dataInfo;
    }

    /**
     * Set the {@link MetadataDataInfo}, the object representing metadata about the metadata (metadata creation date, etc...)
     *
     * @param dataInfo the new data info object
     */
    public void setDataInfo(MetadataDataInfo dataInfo) {
        this._dataInfo = dataInfo;
    }

    /**
     * Get the object containing the source information about the metadata entity.
     *
     * @return the object containing the source information about the metadata entity.
     */
    @Embedded
    public MetadataSourceInfo getSourceInfo() {
        return _sourceInfo;
    }

    /**
     * Set the object containing the source information about the metadata entity.
     *
     * @param sourceInfo the object containing the source information about the metadata entity.
     */
    public void setSourceInfo(MetadataSourceInfo sourceInfo) {
        this._sourceInfo = sourceInfo;
    }

    /**
     * Get the object containing information about how and from where the metadata was harvested (and whether it was harvested.)
     *
     * @return the harvest info object
     */
    @Embedded
    public MetadataHarvestInfo getHarvestInfo() {
        return _harvestInfo;
    }

    /**
     * Set the object containing information about how and from where the metadata was harvested (and whether it was harvested.)
     *
     * @param harvestInfo the harvest info object
     */
    public void setHarvestInfo(MetadataHarvestInfo harvestInfo) {
        this._harvestInfo = harvestInfo;
    }

    protected static void transform(Document in, AbstractMetadata out) {
        out.setId(Integer.valueOf(in.get("_id")));
        out.setUuid(in.get("_uuid"));

        final MetadataDataInfo dataInfo = out.getDataInfo();
        dataInfo.setSchemaId(in.get("_schema"));
        String metadataType = in.get("_isTemplate");
        if (metadataType != null) {
            dataInfo.setType(MetadataType.lookup(metadataType));
        }
        dataInfo.setCreateDate(new ISODate(in.get("_createDate")));
        dataInfo.setChangeDate(new ISODate(in.get("_changeDate")));
        dataInfo.setRoot(in.get("_root"));
        final String displayOrder = in.get("_displayOrder");
        if (displayOrder != null) {
            dataInfo.setDisplayOrder(Integer.valueOf(displayOrder));
        }

        String tmpIsHarvest = in.get("_isHarvested");
        if (tmpIsHarvest != null) {
            out.getHarvestInfo().setHarvested(in.get("_isHarvested").equals("y"));

        }
        final MetadataSourceInfo sourceInfo = out.getSourceInfo();
        sourceInfo.setSourceId(in.get("_source"));
        final String owner = in.get("_owner");
        if (owner != null) {
            sourceInfo.setOwner(Integer.valueOf(owner));
        }

        final String groupOwner = in.get("_groupOwner");
        if (groupOwner != null) {
            sourceInfo.setGroupOwner(Integer.valueOf(groupOwner));
        }
    }

    @Transient
    public Set<MetadataCategory> getCategories() {
        return (Set<MetadataCategory>) metadataCategories;
    }
    @Transient
    protected Set<MetadataCategory> metadataCategories = new HashSet<MetadataCategory>();

}
