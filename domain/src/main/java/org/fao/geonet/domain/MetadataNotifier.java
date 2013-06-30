package org.fao.geonet.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * An entity representing a service that desires to be notified when
 * a metadata is modified.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "metadatanotifier")
public class MetadataNotifier {
    private int id;
    private String name;
    private String url;
    private boolean enabled;
    private String username;
    private char[] password;
    private List<MetadataNotification> notification = new ArrayList<MetadataNotification>();

    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password.toCharArray();
    }

    @JoinColumn(name="notifierId")
    @OneToMany(fetch=FetchType.LAZY)
    public List<MetadataNotification> getNotification() {
        return notification;
    }

    public void setNotification(List<MetadataNotification> notification) {
        this.notification = notification;
    }

}
