package org.fao.geonet.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.fao.geonet.entitylistener.OpenwisDownloadEntityListenerManager;

/**
 * 
 * 
 *
 * @author María Arias de Reyna
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "openwis_download")
@EntityListeners(OpenwisDownloadEntityListenerManager.class)
public class OpenwisDownload extends GeonetEntity {
    private Integer _id;
    private User _user;
    private String _url;
    private Integer _requestId;
    private String _urn;

    /**
     * Get the id. This is a generated value and as such new instances should
     * not have this set as it will simply be ignored and could result in
     * reduced performance.
     *
     * @return the user id
     */
    @Id
    @GeneratedValue
    public Integer getId() {
        return _id;
    }

    @Nonnull
    public OpenwisDownload setId(Integer id) {
        this._id = id;
        return this;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId", referencedColumnName = "id")
    @Nonnull
    public User getUser() {
        return _user;
    }

    public OpenwisDownload setUser(User user) {
        this._user = user;
        return this;
    }

    @Nonnull
    public Integer getRequestId() {
        return _requestId;
    }

    public void setRequestId(Integer _requestId) {
        this._requestId = _requestId;
    }

    @Nullable
    public String getUrl() {
        return _url;
    }

    public void setUrl(String _url) {
        this._url = _url;
    }

    @Nonnull
    public String getUrn() {
        return _urn;
    }

    public void setUrn(String _urn) {
        this._urn = _urn;
    }
}
