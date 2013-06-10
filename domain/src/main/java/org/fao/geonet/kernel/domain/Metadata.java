package org.fao.geonet.kernel.domain;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.vividsolutions.jts.util.Assert;

/**
 * Represents a metadata object in the database. The xml, groups and operations are lazily loaded so accessing then will need to be done in
 * a thread that has a bound EntityManager. Also they can trigger database access if they have not been cached and therefore can cause
 * slowdowns so they should only be accessed in need.
 * 
 * @author Jesse
 */
@Entity
@Table(name = "metadata")
@Cacheable
public class Metadata {

    private int id;
    private String uuid;
    private Date changeDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "operationallowed", joinColumns = @JoinColumn(name = "operationid"), inverseJoinColumns = @JoinColumn(name = "metadataid"))
    private Set<Operation> operations = new HashSet<Operation>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "operationallowed", joinColumns = @JoinColumn(name = "groupid"), inverseJoinColumns = @JoinColumn(name = "metadataid"))
    private Set<Group> groups = new HashSet<Group>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name="metadataid")
    private Set<OperationAllowed> operationAllowed = new HashSet<OperationAllowed>();
    
    @Id
    @GeneratedValue
    @Column(nullable = false)
    public int getId() {
        return id;
    }

    public Metadata setId(int _id) {
        this.id = _id;
        return this;
    }

    @Column(nullable = false)
    @Nonnull
    public String getUuid() {
        return uuid;
    }

    @Nonnull
    public Metadata setUuid(@Nonnull String uuid) {
        Assert.isTrue(uuid != null, "Cannot have null uuid");
        this.uuid = uuid;
        return this;
    }
    
    @Column
    public Date getChangeDate() {
        return changeDate;
    }
    
    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }
    

    /**
     * Get the read-only set of operations that are assocated with
     * this metadata.  This is essentially a view onto operations allowed
     * and isn't automatically updated when operationsAllowed is updated
     */
    @Nonnull
    @Transient
    public Set<Operation> getOperations() {
        return Collections.unmodifiableSet(operations);
    }

    /**
     * Get the read-only collection of groups that are assocated with
     * this metadata.  This is essentially a view onto operations allowed
     * and isn't automatically updated when operationsAllowed is updated
     */
    @Transient
    @Nonnull
    public Set<Group> getGroups() {
        return Collections.unmodifiableSet(groups);
    }
    
    public Metadata addOperationAllowed(OperationAllowed newOperationAllowed) {
        internalAddOperationAllowed(newOperationAllowed);
        newOperationAllowed.internalSetMetadata(this);
        return this;
    }

    void internalAddOperationAllowed(OperationAllowed newOperationAllowed) {
        Assert.isTrue(newOperationAllowed != null, OperationAllowed.class.getSimpleName()+" should not be null");
      this.operationAllowed.add(newOperationAllowed);
    }

    @Transient
    @Nonnull
    public Set<OperationAllowed> getOperationsAllowed() {
        return this.operationAllowed;
    }

}
