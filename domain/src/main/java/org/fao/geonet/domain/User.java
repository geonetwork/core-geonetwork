package org.fao.geonet.domain;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import jeeves.guiservices.session.JeevesUser;
import jeeves.interfaces.Profile;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * A user entity.  A user is used in spring security, controlling access to metadata as well
 * as in the {@link jeeves.server.UserSession}.
 * 
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
public class User implements JeevesUser {
    private static final long serialVersionUID = 2589607276443866650L;

    int id;
    String username;
    String surname;
    String name;
    String email;
    Address address;
    String organisation;
    String kind;
    Profile profile;
    UserSecurity security;

    @Id
    public int getId() {
        return id;
    }

    public User setId(int id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    @Transient
    @Override
    public String getPassword() {
        return new String(getSecurity().getPassword());
    }

    public String getSurname() {
        return surname;
    }

    public User setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    @Embedded
    public Address getAddress() {
        return address;
    }

    public User setAddress(Address address) {
        this.address = address;
        return this;
    }

    public String getOrganisation() {
        return organisation;
    }

    public User setOrganisation(String organization) {
        this.organisation = organization;
        return this;
    }

    public String getKind() {
        return kind;
    }

    public User setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public Profile getProfile() {
        return profile;
    }

    public User setProfile(Profile profile) {
        this.profile = profile;
        return this;
    }

    /**
     * 
     * @return
     */
    public UserSecurity getSecurity() {
        return security;
    }

    protected User setSecurity(UserSecurity security) {
        this.security = security;
        return this;
    }

    @Transient
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
        if (profile != null) {
            for (String p : getProfile().getAllNames()) {
                auths.add(new SimpleGrantedAuthority(p));
            }
        }
        return auths;
    }

    @Transient
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Transient
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @Transient
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isEnabled() {
        return true;
    }

    /**
     * Merge all data from other user into this user.
     * 
     * @param otherUser other user to merge data from.
     * @param mergeNullData if true then also set null values from other user. If false then only merge non-null data
     */
    public void mergeUser(User otherUser, boolean mergeNullData) {
        if (mergeNullData || otherUser.getUsername() != null) {
            setUsername(otherUser.getUsername());
        }
        if (mergeNullData || otherUser.getSurname() != null) {
            setSurname(otherUser.getSurname());
        }
        if (mergeNullData || otherUser.getName() != null) {
            setName(otherUser.getName());
        }
        if (mergeNullData || otherUser.getOrganisation() != null) {
            setOrganisation(otherUser.getOrganisation());
        }
        if (mergeNullData || otherUser.getKind() != null) {
            setKind(otherUser.getKind());
        }
        if (mergeNullData || otherUser.getProfile() != null) {
            setProfile(otherUser.getProfile());
        }
        if (mergeNullData || otherUser.getEmail() != null){
            setEmail(otherUser.getEmail());
        }
        
        getAddress().mergeAddress(otherUser.getAddress(), mergeNullData);
        getSecurity().mergeSecurity(otherUser.getSecurity(), mergeNullData);
    }
}
