package org.fao.geonet.domain;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.entitylistener.UserEntityListenerManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.*;

/**
 * A user entity. A user is used in spring security, controlling access to metadata as well as in the {@link jeeves.server.UserSession}.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "Users")
@Cacheable
@EntityListeners(value = {UserEntityListenerManager.class})
@SequenceGenerator(name = User.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class User extends GeonetEntity implements UserDetails {
    public static final String NODE_APPLICATION_CONTEXT_KEY = "jeevesNodeApplicationContext_";
    private static final long serialVersionUID = 2589607276443866650L;

    static final String ID_SEQ_NAME = "user_id_seq";

    private int _id;
    private String _username;
    private String _surname;
    private String _name;
    private Set<String> _email = new HashSet<String>();
    private Set<Address> _addresses = new LinkedHashSet<Address>();
    private String _organisation;
    private String _kind;
    private Profile _profile = Profile.RegisteredUser;
    private UserSecurity _security = new UserSecurity();
    private String _lastLoginDate;

    /**
     * Get the userid.   This is a generated value and as such new instances should not have this set as it will simply be ignored
     * and could result in reduced performance.
     *
     * @return the user id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    public int getId() {
        return _id;
    }

    /**
     * Set the userid.   This is a generated value and as such new instances should not have this set as it will simply be ignored
     * and could result in reduced performance.
     *
     * @param id the userid
     * @return this user object
     */
    @Nonnull
    public User setId(int id) {
        this._id = id;
        return this;
    }

    /**
     * Get the username.  This is both required and must be unique
     *
     * @return the username
     */
    @Column(nullable = false, unique = true)
    @Nonnull
    public String getUsername() {
        return _username;
    }

    /**
     * Set the username.  This is both required and must be unique
     *
     * @param username the username.  This is both required and must be unique
     * @return this user object
     */
    @Nonnull
    public User setUsername(@Nonnull String username) {
        this._username = username;
        return this;
    }

    /**
     * Get the user's hashed password.  Actual passwords are not stored only hashes of the passwords.
     */
    @Transient
    @Override
    public String getPassword() {
        return new String(getSecurity().getPassword());
    }

    /**
     * Get the Surname/lastname of the user.  May be null
     *
     * @return the Surname/lastname of the user.  May be null
     */
    @Nullable
    public String getSurname() {
        return _surname;
    }

    /**
     * Set the Surname/lastname of the user.  May be null
     *
     * @param surname the Surname/lastname of the user.  May be null
     * @return this user object
     */
    @Nonnull
    public User setSurname(@Nullable String surname) {
        this._surname = surname;
        return this;
    }

    /**
     * Get the user's actual first name.  May be null.
     *
     * @return the user's actual first name.  May be null.
     */
    @Nullable
    public String getName() {
        return _name;
    }

    /**
     * Set the user's actual first name.  May be null.
     *
     * @param name the user's actual first name.  May be null.
     * @return this user object
     */
    @Nonnull
    public User setName(@Nullable String name) {
        this._name = name;
        return this;
    }

    /**
     * Get the main email of the user.
     *
     * @return the main email address of the user.
     */
    @Transient
    public String getEmail() {
        if (_email != null) {
            for (String email : _email) {
                if (email.contains("@")) {
                    return email;
                }
            }
        }
        return null;
    }

    /**
     * Get all the user's email addresses.
     *
     * @return the user's email addresses.
     */
    @ElementCollection(fetch = FetchType.EAGER, targetClass = String.class)
    @CollectionTable(name = "email")
    @Column(name = "email")
    public Set<String> getEmailAddresses() {
        return _email;
    }

    /**
     * Set all the email addresses.
     *
     * @param email all the email addresses.
     */
    protected void setEmailAddresses(Set<String> email) {
        this._email = email;
    }

    /**
     * Get all the user's addresses.
     *
     * @return all the user's addresses.
     */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "UserAddress", joinColumns = @JoinColumn(name = "userid"), inverseJoinColumns = {@JoinColumn(name = "addressid",
            referencedColumnName = "ID", unique = true)})
    public Set<Address> getAddresses() {
        return _addresses;
    }

    /**
     * Set all the user's addresses.
     *
     * @param addresses all the user's addresses.
     * @return this user object
     */
    protected User setAddresses(Set<Address> addresses) {
        this._addresses = addresses;
        return this;
    }

    /**
     * Get the first address in the list of the addresses.
     *
     * @return the first address in the list of the addresses.
     */
    @Transient
    public
    @Nonnull
    Address getPrimaryAddress() {
        Set<Address> addresses = getAddresses();

        final Address addressCopy = new Address();
        if (!addresses.isEmpty()) {
            final Address otherAddress = addresses.iterator().next();
            addressCopy.mergeAddress(otherAddress, true);
            addressCopy.setId(otherAddress.getId());
        }

        return addressCopy;
    }

    /**
     * Return the organization the user is a part of.
     *
     * @return the user's organization.
     */
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

    /**
     * Set the 'kind' of user. Just a sting representing the type or category of the user. It can be customized for a particular
     * application. An example is GOV or CONTRACTOR.
     *
     * @param kind the 'kind' of user. Just a sting representing the type or category of the user. It can be customized for a particular
     *             application. An example is GOV or CONTRACTOR.
     * @return this user object
     */
    public
    @Nonnull
    User setKind(String kind) {
        this._kind = kind;
        return this;
    }

    /**
     * Get the user's profile. This is a required property.
     *
     * @return the user's profile.
     */
    @Column(nullable = false)
    public
    @Nonnull
    Profile getProfile() {
        return _profile;
    }

    /**
     * Set the user's profile. This is a required property.
     *
     * @param profile the user's profile.
     * @return this user object
     */
    public
    @Nonnull
    User setProfile(@Nonnull Profile profile) {
        this._profile = profile;
        return this;
    }

    /**
     * Get the object containing the information regarding security.
     *
     * @return the object containing the information regarding security.
     */
    public
    @Nonnull
    UserSecurity getSecurity() {
        return _security;
    }

    /**
     * Set the UserSecurity object.  It is to be used by JPA framework.
     *
     * @param security the security object
     * @return this user object
     */
    protected
    @Nonnull
    User setSecurity(@Nonnull UserSecurity security) {
        this._security = security;
        return this;
    }

    /**
     * Get the last login date of the user.  May be null
     *
     * @return the last login date of the user.  May be null
     */
    @Nullable
    public String getLastLoginDate() {
        return _lastLoginDate;
    }

    /**
     * Set the last login date  of the user.  May be null
     *
     * @param lastLoginDate the last login date of the user.  May be null
     * @return this user object
     */
    @Nonnull
    public User setLastLoginDate(@Nullable String lastLoginDate) {
        this._lastLoginDate = lastLoginDate;
        return this;
    }

    @Transient
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        ArrayList<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
        final String nodeId = getSecurity().getNodeId();
        if (nodeId != null) {
            auths.add(new SimpleGrantedAuthority(NODE_APPLICATION_CONTEXT_KEY + nodeId));
        }

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
     * @param otherUser     other user to merge data from.
     * @param mergeNullData if true then also set null values from other user. If false then only merge non-null data
     */
    public void mergeUser(User otherUser, boolean mergeNullData) {
        if (mergeNullData || StringUtils.isNotBlank(otherUser.getUsername())) {
            setUsername(otherUser.getUsername());
        }
        if (mergeNullData || StringUtils.isNotBlank(otherUser.getSurname())) {
            setSurname(otherUser.getSurname());
        }
        if (mergeNullData || StringUtils.isNotBlank(otherUser.getName())) {
            setName(otherUser.getName());
        }
        if (mergeNullData || StringUtils.isNotBlank(otherUser.getOrganisation())) {
            setOrganisation(otherUser.getOrganisation());
        }
        if (mergeNullData || StringUtils.isNotBlank(otherUser.getKind())) {
            setKind(otherUser.getKind());
        }
        if (mergeNullData || StringUtils.isNotBlank(otherUser.getProfile().name())) {
            setProfile(otherUser.getProfile());
        }

        if (mergeNullData || !otherUser.getEmailAddresses().isEmpty()) {
            _email.clear();
            _email.addAll(otherUser.getEmailAddresses());
        }

        ArrayList<Address> otherAddresses = new ArrayList<Address>(otherUser.getAddresses());
        if (mergeNullData || !otherAddresses.isEmpty()) {
            for (Iterator<Address> iterator = _addresses.iterator(); iterator.hasNext(); ) {
                Address address = iterator.next();
                boolean found = false;

                for (Iterator<Address> iterator2 = otherAddresses.iterator(); iterator.hasNext(); ) {
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
        }

        getSecurity().mergeSecurity(otherUser.getSecurity(), mergeNullData);
    }

    @Override
    public String toString() {
        return getUsername() + "(" + getId() + ") - " + getProfile();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (_id != user._id) return false;
        if (!_addresses.equals(user._addresses)) return false;
        if (!_email.equals(user._email)) return false;
        if (_kind != null ? !_kind.equals(user._kind) : user._kind != null) return false;
        if (_name != null ? !_name.equals(user._name) : user._name != null) return false;
        if (_organisation != null ? !_organisation.equals(user._organisation) : user._organisation != null) return false;
        if (_profile != user._profile) return false;
        if (!_security.equals(user._security)) return false;
        if (_surname != null ? !_surname.equals(user._surname) : user._surname != null) return false;
        if (_username != null ? !_username.equals(user._username) : user._username != null) return false;
        if (_lastLoginDate != null ? !_lastLoginDate.equals(user._lastLoginDate) : user._lastLoginDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _id;
        result = 31 * result + (_username != null ? _username.hashCode() : 0);
        result = 31 * result + (_surname != null ? _surname.hashCode() : 0);
        result = 31 * result + (_name != null ? _name.hashCode() : 0);
        result = 31 * result + _email.hashCode();
        result = 31 * result + _addresses.hashCode();
        result = 31 * result + (_organisation != null ? _organisation.hashCode() : 0);
        result = 31 * result + (_kind != null ? _kind.hashCode() : 0);
        result = 31 * result + (_profile != null ? _profile.hashCode() : 0);
        result = 31 * result + _security.hashCode();
        result = 31 * result + (_lastLoginDate != null ? _lastLoginDate.hashCode() : 0);
        return result;
    }
}
