package org.fao.geonet.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import jeeves.guiservices.session.JeevesUser;
import jeeves.interfaces.Profile;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * A user entity. A user is used in spring security, controlling access to metadata as well as in the {@link jeeves.server.UserSession}.
 * 
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "users")
public class User implements JeevesUser {
    private static final long serialVersionUID = 2589607276443866650L;

    int _id;
    String _username;
    String _surname;
    String _name;
    Set<String> _email = new HashSet<String>();
    Set<Address> _addresses = new HashSet<Address>();
    String _organisation;
    String _kind;
    Profile _profile;
    UserSecurity _security;

    @Id
    public int getId() {
        return _id;
    }

    public User setId(int id) {
        this._id = id;
        return this;
    }

    @Column(nullable = false)
    public String getUsername() {
        return _username;
    }

    public User setUsername(String username) {
        this._username = username;
        return this;
    }

    @Transient
    @Override
    public String getPassword() {
        return new String(getSecurity().getPassword());
    }

    public String getSurname() {
        return _surname;
    }

    public User setSurname(String surname) {
        this._surname = surname;
        return this;
    }

    public String getName() {
        return _name;
    }

    public User setName(String name) {
        this._name = name;
        return this;
    }

    @Override
    @Transient
    public String getEmail() {
        if (_email != null) {
            return _email.iterator().next();
        }
        return null;
    }

    @ElementCollection(fetch = FetchType.EAGER, targetClass = String.class)
    @CollectionTable(name = "email")
    @Column(name = "email")
    public Set<String> getEmailAddresses() {
        return _email;
    }

    public User setEmailAddresses(Set<String> email) {
        this._email = email;
        return this;
    }

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "USER_ADDRESS", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = { @JoinColumn(name = "addressid", referencedColumnName = "ID", unique = true) })
    public Set<Address> getAddresses() {
        return _addresses;
    }

    public User setAddresses(Set<Address> addresses) {
        this._addresses = addresses;
        return this;
    }

    public String getOrganisation() {
        return _organisation;
    }

    public User setOrganisation(String organization) {
        this._organisation = organization;
        return this;
    }

    /**
     * Get the 'kind' of user. Just a sting representing the type or category of the user. It can be customized for a particular
     * application. An example is GOV or CONTRACTOR.
     */
    @Column(length = 16)
    public String getKind() {
        return _kind;
    }

    public User setKind(String kind) {
        this._kind = kind;
        return this;
    }

    @Column(nullable = false)
    public Profile getProfile() {
        return _profile;
    }

    public User setProfile(Profile profile) {
        this._profile = profile;
        return this;
    }

    /**
     * 
     * @return
     */
    public UserSecurity getSecurity() {
        return _security;
    }

    protected User setSecurity(UserSecurity security) {
        this._security = security;
        return this;
    }

    @Transient
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
        if (_profile != null) {
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

        _email.clear();
        _email.addAll(otherUser.getEmailAddresses());

        ArrayList<Address> otherAddresses = new ArrayList<Address>(otherUser.getAddresses());

        for (Iterator<Address> iterator = _addresses.iterator(); iterator.hasNext();) {
            Address address = (Address) iterator.next();
            boolean found = false;

            for (Iterator<Address> iterator2 = otherAddresses.iterator(); iterator.hasNext();) {
                Address otherAddress = iterator2.next();
                if (otherAddress.getId() == address.getId()) {
                    address.mergeAddress(otherAddress, mergeNullData);
                    found = true;
                    iterator2.remove();
                    break;
                }
            }

            if (!found) {
                iterator.remove();
            }
        }

        _addresses.addAll(otherAddresses);
        getSecurity().mergeSecurity(otherUser.getSecurity(), mergeNullData);
    }
}
