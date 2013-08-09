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
 * The mapping between user, the groups a user is a part of and the profiles the user has for each group.
 * 
 * @author Jesse
 * 
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = UserGroupNamedQueries.TABLE_NAME)
public class UserGroup {
    private UserGroupId _id;
    private Group _group;
    private User _user;
    private Profile _profile;

    /**
     * Get the id object.
     * 
     * @return the id object.
     */
    @EmbeddedId
    public UserGroupId getId() {
        return _id;
    }

    /**
     * Set the id object.
     * 
     * @param id the id object.
     */
    public void setId(UserGroupId id) {
        this._id = id;
    }

    /**
     * Get the group in the relation. The group is lazily loaded.
     * 
     * @return the group in the relation.
     */
    @MapsId("groupId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupid", referencedColumnName = "id")
    public Group getGroup() {
        return _group;
    }

    /**
     * Set the group in the relation.
     * 
     * @param group the group in the relation.
     * @return this entity object
     */
    public UserGroup setGroup(Group group) {
        this._group = group;
        return this;
    }

    /**
     * Return the user in the relation. The user is lazily loaded.
     * 
     * @return the user in the relation.
     */
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", referencedColumnName = "id")
    public User getUser() {
        return _user;
    }

    /**
     * Set the user in the relation.
     * 
     * @param user the user in the relation.
     * @return this enity object
     */
    public UserGroup setUser(User user) {
        this._user = user;
        return this;
    }

    /**
     * Return the profile for this relation.
     * 
     * @return the profile for this relation.
     */
    @Column(nullable = false)
    public Profile getProfile() {
        return _profile;
    }

    /**
     * Set the profile for this relation.
     * 
     * @param profile the profile for this relation.
     * @return this entity object
     */
    public UserGroup setProfile(Profile profile) {
        this._profile = profile;
        return this;
    }

}
