/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
import org.hibernate.annotations.Type;
import org.jdom.Element;
import org.jdom.Namespace;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * INSPIRE Atom feed model class.
 *
 * @author Jose García
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "InspireAtomFeed",
    indexes = { @Index(name = "idx_inspireatomfeed_metadataid", columnList = "metadataid"),
                @Index(name = "idx_inspireatomfeed_atomDatasetid", columnList = "atomDatasetid"),
                @Index(name = "idx_inspireatomfeed_atomDatasetns", columnList = "atomDatasetns")})
@SequenceGenerator(name = InspireAtomFeed.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class InspireAtomFeed extends GeonetEntity implements Serializable {
    static final String ID_SEQ_NAME = "inspire_atom_feed_id_seq";
    private int _id;
    private int _metadataId;
    private String _title;

    private String _atom;
    private String _atomUrl;
    private String _atomDatasetid;
    private String _atomDatasetns;

    private String _subtitle;
    private String _rights;
    private String _lang;
    private String _authorName;
    private String _authorEmail;

    private List<InspireAtomFeedEntry> _entryList;


    public InspireAtomFeed() {
        _entryList = new ArrayList<>();
    }

    public static InspireAtomFeed build(Element atomDoc) {
        InspireAtomFeed inspireAtomFeed = new InspireAtomFeed();

        Namespace ns = Namespace.getNamespace("f", "http://www.w3.org/2005/Atom");
        Namespace nsXml = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");

        inspireAtomFeed.setTitle(StringUtils.left(atomDoc.getChildText("title", ns), 255));

        if (atomDoc.getChildText("subtitle", ns) != null) {
            inspireAtomFeed.setSubtitle(StringUtils.left(atomDoc.getChildText("subtitle", ns), 255));
        }

        if (atomDoc.getChildText("rights", ns) != null) {
            inspireAtomFeed.setRights(StringUtils.left(atomDoc.getChildText("rights", ns), 255));
        }

        inspireAtomFeed.setLang(atomDoc.getAttributeValue("lang", ns, ""));
        Element authorEl = atomDoc.getChild("author", ns);
        if (authorEl != null) {
            inspireAtomFeed.setAuthorName(
                StringUtils.left(atomDoc.getChild("author", ns).getChildText("name", ns), 255));
            inspireAtomFeed.setAuthorEmail(
                StringUtils.left(atomDoc.getChild("author", ns).getChildText("email", ns), 255));
        }
        inspireAtomFeed.setLang(atomDoc.getAttributeValue("lang", nsXml, ""));

        List<Element> entryList = atomDoc.getChildren("entry", ns);
        for (Element entry : entryList) {
            for (Element linkEl : (List<Element>) entry.getChildren("link", ns)) {
                // Process data feed download links:
                //  - alternate: single download files/links per datafeed entry
                //  - section: multiple download files/links per datafeed entry
                if (linkEl.getAttributeValue("rel", "").equals("alternate") ||
                    linkEl.getAttributeValue("rel", "").equals("section")) {
                    InspireAtomFeedEntry inspireAtomFeedEntry = new InspireAtomFeedEntry();

                    inspireAtomFeedEntry.setTitle(StringUtils.left(entry.getChildText("title", ns), 255));

                    if (entry.getChildText("category", ns) != null) {
                        inspireAtomFeedEntry.setCrs(entry.getChild("category", ns).getAttributeValue("term"));
                    }

                    inspireAtomFeedEntry.setType(linkEl.getAttributeValue("type", ""));
                    inspireAtomFeedEntry.setLang(linkEl.getAttributeValue("hreflang", ""));
                    inspireAtomFeedEntry.setUrl(linkEl.getAttributeValue("href"));

                    inspireAtomFeed.addEntry(inspireAtomFeedEntry);
                }
            }
        }

        return inspireAtomFeed;
    }

    /**
     * Get the id of the metadata. This is a generated value and as such new instances should not
     * have this set as it will simply be ignored and could result in reduced performance.
     *
     * @return the id of the metadata
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(nullable = false)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the metadata. This is a generated value and as such new instances should not
     * have this set as it will simply be ignored and could result in reduced performance.
     *
     * @param _id the id of the metadata
     * @return this entity object
     */
    public InspireAtomFeed setId(int _id) {
        this._id = _id;
        return this;
    }

    public int getMetadataId() {
        return _metadataId;
    }

    public void setMetadataId(Integer metadataId) {
        this._metadataId = metadataId;
    }

    @Column(length = 255)
    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        this._title = title;
    }

    @Column
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String getAtom() {
        return _atom;
    }

    public void setAtom(String atom) {
        this._atom = atom;
    }

    @Column(length = 255)
    public String getAtomUrl() {
        return _atomUrl;
    }

    public void setAtomUrl(String atomUrl) {
        this._atomUrl = atomUrl;
    }

    @Column(length = 255)
    public String getAtomDatasetid() {
        return _atomDatasetid;
    }

    @Column(length = 255)
    public void setAtomDatasetid(String atomDatasetid) {
        this._atomDatasetid = atomDatasetid;
    }

    public String getAtomDatasetns() {
        return _atomDatasetns;
    }

    public void setAtomDatasetns(String atomDatasetns) {
        this._atomDatasetns = atomDatasetns;
    }

    @Column(length = 255)
    public String getSubtitle() {
        return _subtitle;
    }

    public void setSubtitle(String subtitle) {
        this._subtitle = subtitle;
    }

    @Column(length = 255)
    public String getRights() {
        return _rights;
    }

    public void setRights(String rights) {
        this._rights = rights;
    }

    @Column(length = 3)
    public String getLang() {
        return _lang;
    }

    public void setLang(String lang) {
        this._lang = lang;
    }

    @Column(length = 255)
    public String getAuthorName() {
        return _authorName;
    }

    public void setAuthorName(String authorName) {
        this._authorName = authorName;
    }

    @Column(length = 255)
    public String getAuthorEmail() {
        return _authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this._authorEmail = authorEmail;
    }

    @ElementCollection(fetch = FetchType.EAGER, targetClass = InspireAtomFeedEntry.class)
    public List<InspireAtomFeedEntry> getEntryList() {
        return _entryList;
    }

    public void setEntryList(List<InspireAtomFeedEntry> entryList) {
        this._entryList = entryList;
    }

    public void addEntry(InspireAtomFeedEntry entry) {
        _entryList.add(entry);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InspireAtomFeed that = (InspireAtomFeed) o;

        if (_id != that._id) return false;
        if (_metadataId != that._metadataId) return false;
        if (!Objects.equals(_title, that._title)) return false;
        if (!Objects.equals(_atom, that._atom)) return false;
        if (!Objects.equals(_atomUrl, that._atomUrl)) return false;
        if (!Objects.equals(_atomDatasetid, that._atomDatasetid))
            return false;
        if (!Objects.equals(_atomDatasetns, that._atomDatasetns))
            return false;
        if (!Objects.equals(_subtitle, that._subtitle)) return false;
        if (!Objects.equals(_rights, that._rights)) return false;
        if (!Objects.equals(_lang, that._lang)) return false;
        if (!Objects.equals(_authorName, that._authorName)) return false;
        if (!Objects.equals(_authorEmail, that._authorEmail)) return false;
        return Objects.equals(_entryList, that._entryList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, _metadataId, _title, _atom, _atomUrl, _atomDatasetid, _atomDatasetns, _subtitle,
            _rights, _lang, _authorName, _authorEmail, _entryList);
    }
}
