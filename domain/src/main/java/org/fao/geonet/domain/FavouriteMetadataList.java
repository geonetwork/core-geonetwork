/*
 * Copyright (C) 2023 Food and Agriculture Organization of the
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


import org.fao.geonet.entitylistener.FavouriteMetadataListListenerManager;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@Entity
@Access(AccessType.PROPERTY)
@Table(
    name = "FavouriteMetadataList"
    // ,indexes = {@Index(name = "idx_usersavedselections_metadatauuid", columnList = "metadataUuid")}
)
@SequenceGenerator(name = FavouriteMetadataList.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
@EntityListeners(FavouriteMetadataListListenerManager.class)
public class FavouriteMetadataList extends GeonetEntity implements Serializable {
    public static final String CHANGE_DATE_COLUMN_NAME = "changedate";
    public static final String CREATE_DATE_COLUMN_NAME = "createdate";

    static final String ID_SEQ_NAME = "fav_md_list_id_seq";

    /**
     * id - managed by hibernate/JPA
     */
    private int id;
    /**
     * name of the list (what the user entered)
     */
    private String _name;
    /**
     * If the list was created when the user is logged in, then this will be the user who created it.
     * NOTE: if this is set, sessionID is null (and visa-versa)
     */
    private User _user;
    /**
     * If the list was created when the user is NOT logged in, then this will be the value of the cookie used.
     * NOTE: if this is set, user is null (and visa-versa)
     */
    private String _sessionId;
    /**
     * type of the list
     */
    private ListType _listType;
    /**
     * if its public, then other people can see (but not edit it).
     */
    private boolean _isPublic;
    /**
     * when the list was created.
     */
    private ISODate _createDate;
    /**
     * when the list was last updated.
     */
    private ISODate _changeDate;
    /**
     *  list of metadata UUIDs.  Should be unique.
     */
    private List<FavouriteMetadataListItem> _selections;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return id;
    }

    public FavouriteMetadataList setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId", referencedColumnName = "id")
    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        this._user = user;
    }

    public String getSessionId() {
        return _sessionId;
    }

    public void setSessionId(String sessionId) {
        this._sessionId = sessionId;
    }

    @Enumerated(EnumType.STRING)
    public ListType getListType() {
        return _listType;
    }

    public void setListType(ListType listType) {
        this._listType = listType;
    }

    public boolean getIsPublic() {
        return _isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this._isPublic = isPublic;
    }

    @AttributeOverride(name = "dateAndTimeUtc", column = @Column(name = CREATE_DATE_COLUMN_NAME, nullable = false, length = 30))
    public ISODate getCreateDate() {
        return _createDate;
    }

    public void setCreateDate(ISODate createDate) {
        this._createDate = createDate;
    }

    @AttributeOverride(name = "dateAndTimeUtc", column = @Column(name = CHANGE_DATE_COLUMN_NAME, nullable = false, length = 30))
    public ISODate getChangeDate() {
        return _changeDate;
    }

    public void setChangeDate(ISODate changeDate) {
        this._changeDate = changeDate;
    }

    @OneToMany(
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JoinColumn(
        name = "selection_id",
        nullable = false)
    public List<FavouriteMetadataListItem> getSelections() {
        return _selections;
    }

    public void setSelections(List<FavouriteMetadataListItem> selections) {
        this._selections = selections;
    }

    public enum ListType {PreferredList, WatchList}
}