package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import jeeves.interfaces.Profile;

/**
 * The mapping between user, the groups a user is a part of and the profiles the user 
 * has for each group.
 *
 * @author Jesse
 *
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name=UserGroupNamedQueries.TABLE_NAME)
public class UserGroup {
    private UserGroupId _id;
    private Group _group;
    private User _user;
    private Profile _profile;
    
    @EmbeddedId
    public UserGroupId getId() {
        return _id;
    }
    
    public void setId(UserGroupId id) {
        this._id = id;
    }
    @MapsId("groupId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="groupid", referencedColumnName="id")
    public Group getGroup() {
        return _group;
    }
    public UserGroup setGroup(Group group) {
        this._group = group;
        return this;
    }
    
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="userid", referencedColumnName="id")
    public User getUser() {
        return _user;
    }
    public UserGroup setUser(User user) {
        this._user = user;
        return this;
    }
    @Column(nullable=false)
    public Profile getProfile() {
        return _profile;
    }
    public UserGroup setProfile(Profile profile) {
        this._profile = profile;
        return this;
    }
    
    
}
