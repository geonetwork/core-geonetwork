package org.fao.geonet.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import jeeves.utils.Xml;

import org.jdom.Element;
import org.jdom.JDOMException;

import com.vividsolutions.jts.util.Assert;

/**
 * An entity representing a metadata object in the database. The xml, groups and operations are lazily loaded so accessing then will need to
 * be done in a thread that has a bound EntityManager. Also they can trigger database access if they have not been cached and therefore can
 * cause slowdowns so they should only be accessed in need.
 * 
 * @author Jesse
 */
@Entity
@Table(name = "metadata")
@Access(AccessType.PROPERTY)
public class Metadata {

    private int _id;
    private String _uuid;
    private String _data;
    private MetadataDataInfo _dataInfo;
    private MetadataSourceInfo _sourceInfo;
    private MetadataHarvestInfo _harvestInfo;
    private List<OperationAllowed> _operationsAllowed = new ArrayList<OperationAllowed>();
    private Set<MetadataCategory> _metadataCategories = new HashSet<MetadataCategory>();

    // private List<MetadataStatus> _metadataStatus;
    // private Set<Operation> operations = new HashSet<Operation>();
    // private Set<Group> groups = new HashSet<Group>();
    /**
     * Get the id of the metadata. This is a generated value and as such new instances should not have this set as it will simply be ignored
     * and could result in reduced performance.
     * 
     * @return the id of the metadata
     */
    @Id
    @GeneratedValue
    @Column(nullable = false)
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the metadata. This is a generated value and as such new instances should not have this set as it will simply be ignored
     * and could result in reduced performance.
     * 
     * @param _id the id of the metadata
     * 
     * @return this entity object
     */
    public Metadata setId(int _id) {
        this._id = _id;
        return this;
    }

    /**
     * Get the uuid of the metadata. This is a required property and thus must not be null.
     * 
     * @return the uuid of the metadata.
     */
    @Column(nullable = false)
    @Nonnull
    public String getUuid() {
        return _uuid;
    }

    /**
     * Set the metadata uuid.
     * 
     * @param uuid the new uuid of the metadata
     * @return this eneity object
     */
    @Nonnull
    public Metadata setUuid(@Nonnull String uuid) {
        Assert.isTrue(uuid != null, "Cannot have null uuid");
        this._uuid = uuid;
        return this;
    }

    /**
     * Get the metadata data as a string (typically XML)
     * 
     * @return the metadata data as a string.
     */
    @Column(nullable = false)
    @Lob
    public String getData() {
        return _data;
    }

    /**
     * Set the metadata data as a string (typically XML)
     * 
     * @param data the data for this metadata record.
     * 
     * @return this metadata entity.
     */
    public Metadata setData(String data) {
        this._data = data;
        return this;
    }

    /**
     * Get the metadata data as xml. This method parses the data string and will throw exceptions if the data is not xml formatted.
     * 
     * @param validate true if the xml should be validated as it is parsed.
     * 
     * @return the metadata parsed into xml {@link Element} objects
     * 
     * @throws IOException if there is an error parsing
     * @throws JDOMException if there is an error parsing
     */
    @Transient
    public Element getXmlData(boolean validate) throws IOException, JDOMException {
        return Xml.loadString(this._data, validate);
    }

    /**
     * Set the metadata data.
     * 
     * @param data the data.
     * @return this metadata entity.
     */
    public Metadata setXmlData(Element data) {
        return setData(Xml.getString(data));
    }

    /**
     * Get the object representing metadata about the metadata (metadata creation date, etc...)
     * 
     * @return the {@link MetadataDataInfo} for the metadata entity.
     */
    @Embedded
    public MetadataDataInfo getDataInfo() {
        return _dataInfo;
    }

    /**
     * Set the {@link MetadataDataInfo}, the object representing metadata about the metadata (metadata creation date, etc...)
     * 
     * @param dataInfo the new data info object
     */
    public void setDataInfo(MetadataDataInfo dataInfo) {
        this._dataInfo = dataInfo;
    }

    /**
     * Get the object containing the source information about the metadata entity.
     * 
     * @return the object containing the source information about the metadata entity.
     */
    @Embedded
    public MetadataSourceInfo getSourceInfo() {
        return _sourceInfo;
    }

    /**
     * Set the object containing the source information about the metadata entity.
     * 
     * @param sourceInfo the object containing the source information about the metadata entity.
     */
    public void setSourceInfo(MetadataSourceInfo sourceInfo) {
        this._sourceInfo = sourceInfo;
    }

    /**
     * Get the object containing information about how and from where the metadata was harvested (and whether it was harvested.)
     * 
     * @return the harvest info object
     */
    @Embedded
    public MetadataHarvestInfo getHarvestInfo() {
        return _harvestInfo;
    }

    /**
     * Set the object containing information about how and from where the metadata was harvested (and whether it was harvested.)
     * 
     * @param harvestInfo the harvest info object
     */
    public void setHarvestInfo(MetadataHarvestInfo harvestInfo) {
        this._harvestInfo = harvestInfo;
    }

    // /**
    // * Get the read-only set of operations that are assocated with
    // * this metadata. This is essentially a view onto operations allowed
    // * and isn't automatically updated when operationsAllowed is updated
    // */
    // @ManyToMany(fetch = FetchType.LAZY)
    // @JoinTable(name = "operationallowed", joinColumns = @JoinColumn(name = "operationid"), inverseJoinColumns = @JoinColumn(name =
    // "metadataid"))
    // @Nonnull
    // public Set<Operation> getOperations() {
    // return Collections.unmodifiableSet(operations);
    // }
    //
    // /**
    // * Get the read-only collection of groups that are assocated with
    // * this metadata. This is essentially a view onto operations allowed
    // * and isn't automatically updated when operationsAllowed is updated
    // */
    // @ManyToMany(fetch = FetchType.LAZY)
    // @JoinTable(name = "operationallowed", joinColumns = @JoinColumn(name = "groupid"), inverseJoinColumns = @JoinColumn(name =
    // "metadataid"))
    // @Nonnull
    // public Set<Group> getGroups() {
    // return Collections.unmodifiableSet(groups);
    // }
    // /**
    // * Get the read-only collection of groups that are assocated with
    // * this metadata. This is essentially a view onto operations allowed
    // * and isn't automatically updated when operationsAllowed is updated
    // */
    // @ManyToMany(fetch = FetchType.LAZY)
    // @JoinTable(name = "operationallowed", joinColumns = @JoinColumn(name = "groupid"), inverseJoinColumns = @JoinColumn(name =
    // "metadataid"))
    // @Nonnull
    // public Set<Group> getGroups() {
    // return Collections.unmodifiableSet(groups);
    // }

    /**
     * Get the set of metadata categories this metadata is part of.
     * 
     * @return the metadata categories
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "metadatacateg", joinColumns = @JoinColumn(name = "categoryid"), inverseJoinColumns = @JoinColumn(name = "metadataid"))
    @Nonnull
    public Set<MetadataCategory> getCategories() {
        return _metadataCategories;
    }

    /**
     * Set the metadata category
     * 
     * @param categories
     */
    protected void setCategories(Set<MetadataCategory> categories) {
        this._metadataCategories = categories;
    }

    /**
     * Add a new operation allowed object to this metadata object.
     * 
     * @param newOperationAllowed the new operation allowed
     * 
     * @return this metadata entity
     */
    public Metadata addOperationAllowed(OperationAllowed newOperationAllowed) {
        internalAddOperationAllowed(newOperationAllowed);
        newOperationAllowed.internalSetMetadata(this);
        return this;
    }

    /**
     * Remove an operation allowed object to this metadata object.
     * 
     * @param oldOperationAllowed the operation allowed object to remove
     * 
     * @return this metadata entity
     */
    public Metadata removeOperationAllowed(OperationAllowed oldOperationAllowed) {
        internalRemoveOperationAllowed(oldOperationAllowed);
        oldOperationAllowed.internalSetMetadata(null);
        return this;
    }

    void internalAddOperationAllowed(OperationAllowed newOperationAllowed) {
        Assert.isTrue(newOperationAllowed != null, OperationAllowed.class.getSimpleName() + " should not be null");
        if (!_operationsAllowed.contains(newOperationAllowed)) {
            this._operationsAllowed.add(newOperationAllowed);
        }
    }

    /**
     * Get the collection containing the operations allowed
     */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "metadataid")
    public List<OperationAllowed> getOperationsAllowed() {
        return this._operationsAllowed;
    }

    /**
     * Set operationallowed collection. Should only be used when creating the object.
     * 
     * @param operationAllowed the operation allowed collection
     */
    protected void setOperationsAllowed(List<OperationAllowed> operationAllowed) {
        this._operationsAllowed = operationAllowed;
    }

    void internalRemoveOperationAllowed(OperationAllowed operationAllowed) {
        this._operationsAllowed.remove(operationAllowed);
    }
}
