package org.fao.geonet.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author Jesse
 *
 */
@Entity
@Table(name = "groups")
@Cacheable
public class Group {

    private int _id;
    private String _name;
    private String _description;
    private String _email;

    @ManyToMany(mappedBy = "groups", fetch = FetchType.LAZY)
    private List<Metadata> _metadata = new ArrayList<Metadata>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    public int getId() {
        return _id;
    }

    /**
     * When creating a new Group DO NOT!!! Set this spring data will do this for you.
     * <p>
     * If you want to update an existing Group then you should set this id to the group you want to update and set the other values to the
     * desired values
     * </p>
     * 
     * @param id
     * @return
     */
    public Group setId(int id) {
        this._id = id;
        return this;
    }

    @Column(nullable = false, length = 32)
    public String getName() {
        return _name;
    }

    public Group setName(String name) {
        this._name = name;
        return this;
    }

    @Column(length = 255)
    public String getDescription() {
        return _description;
    }

    public Group setDescription(String description) {
        this._description = description;
        return this;
    }

    @Column(length = 32)
    public String getEmail() {
        return _email;
    }

    public Group setEmail(String email) {
        this._email = email;
        return this;
    }
    
    @Transient
    public List<Metadata> getMetadata() {
        return _metadata;
    }

    @Override
    public String toString() {
        return "Group [_id=" + _id + ", _name=" + _name + ", _email=" + _email + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Group other = (Group) obj;
        if (_id != other._id)
            return false;
        return true;
    }

    
}
