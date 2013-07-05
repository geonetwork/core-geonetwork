package org.fao.geonet.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * An entity representing a service that desires to be notified when
 * a metadata is modified.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "metadatanotifiers")
public class MetadataNotifier {
    private int _id;
    private String _name;
    private String _url;
    private char _enabled = 'n';
    private String _username;
    private char[] _password;
    private List<MetadataNotification> _notification = new ArrayList<MetadataNotification>();

    @Id
    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    @Column(nullable=false, length=32)
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    @Column(nullable=false)
    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        this._url = url;
    }
    /**
     * For backwards compatibility we need the deleted column to
     * be either 'n' or 'y'.  This is a workaround to allow this
     * until future versions of JPA that allow different ways 
     * of controlling how types are mapped to the database.
     */
    @Column(name="enabled", length=1, nullable=false)
    public char isEnabled_JPAWorkaround() {
        return _enabled;
    }
    public void setEnabled_JPAWorkaround(char enabled) {
        this._enabled = enabled;
    }
    @Transient
    public boolean isEnabled() {
        return _enabled == 'y';
    }

    public void setEnabled(boolean enabled) {
        this._enabled = enabled ? 'y' : 'n';
    }

    @Column(length=32)
    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        this._username = username;
    }

    public char[] getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        this._password = password.toCharArray();
    }

    @JoinColumn(name="notifierId")
    @OneToMany(fetch=FetchType.LAZY)
    public List<MetadataNotification> getNotification() {
        return _notification;
    }

    public void setNotification(List<MetadataNotification> notification) {
        this._notification = notification;
    }

}
