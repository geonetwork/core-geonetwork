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
 * A user entity
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
    String organization;
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

    public String getOrganization() {
        return organization;
    }

    public User setOrganization(String organization) {
        this.organization = organization;
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

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
