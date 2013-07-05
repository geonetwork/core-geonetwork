package org.fao.geonet.domain;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * An entity representing a harvesting task that may have been
 * completed or possibly ending in error.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name="harvesthistory")
public class HarvestHistory {
    private int _id;
    private String _harvestDate;
    private int _elapsedTime;
    private String _harvesterUuid;
    private String _harvesterName;
    private String _harvesterType;
    private char _deleted = 'n';
    private String _info;
    private String _params;

    @Id
    public int getId() {
        return _id;
    }
    public void setId(int id) {
        this._id = id;
    }
    @Column(name="harvestdate", length=30)
    public String getHarvestDate() {
        return _harvestDate;
    }
    public void setHarvestDate(String harvestDate) {
        this._harvestDate = harvestDate;
    }
    @Column(name="elapsedtime")
    public int getElapsedTime() {
        return _elapsedTime;
    }
    public void setElapsedTime(int elapsedTime) {
        this._elapsedTime = elapsedTime;
    }
    @Column(name="harvesteruuid")
    public String getHarvesterUuid() {
        return _harvesterUuid;
    }
    public void setHarvesterUuid(String harvesterUuid) {
        this._harvesterUuid = harvesterUuid;
    }
    @Column(name="harvestername")
    public String getHarvesterName() {
        return _harvesterName;
    }
    public void setHarvesterName(String harvesterName) {
        this._harvesterName = harvesterName;
    }
    @Column(name="harvestertype")
    public String getHarvesterType() {
        return _harvesterType;
    }
    public void setHarvesterType(String harvesterType) {
        this._harvesterType = harvesterType;
    }
    /**
     * For backwards compatibility we need the deleted column to
     * be either 'n' or 'y'.  This is a workaround to allow this
     * until future versions of JPA that allow different ways 
     * of controlling how types are mapped to the database.
     */
    @Column(name="deleted", nullable=false, length=1)
    protected char getDeleted_JpaWorkaround() {
        return _deleted;
    }
    protected char setDeleted_JpaWorkaround(char deleted) {
        return _deleted = deleted;
    }
    @Transient
    public boolean isDeleted() {
        return _deleted == 'y';
    }
    public void setDeleted(boolean deleted) {
        this._deleted = deleted? 'y' : 'n';
    }
    @Lob
    public String getInfo() {
        return _info;
    }
    public void setInfo(String info) {
        this._info = info;
    }
    @Lob
    public String getParams() {
        return _params;
    }
    public void setParams(String params) {
        this._params = params;
    }
    
    
}
