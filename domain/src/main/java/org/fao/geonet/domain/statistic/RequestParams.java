package org.fao.geonet.domain.statistic;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity representing the search parameters of a request.  Related to {@link Request}.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "params")
public class RequestParams {
    private int _id;
    private String _queryType;
    private String _termField;
    private String _termText;
    private double _similarity;
    private String _lowerText;
    private String _upperText;
    private char _inclusive = 'n';
    private Request _request;

    @Id
    public int getId() {
        return _id;
    }
    public void setId(int id) {
        this._id = id;
    }
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName="id", name="requestid")
    public Request getRequest() {
        return _request;
    }
    public void setRequest(Request request) {
        this._request = request;
    }
    @Column(name="querytype")
    public String getQueryType() {
        return _queryType;
    }
    public void setQueryType(String queryType) {
        this._queryType = queryType;
    }
    @Column(name="termfield")
    public String getTermField() {
        return _termField;
    }
    public void setTermField(String termField) {
        this._termField = termField;
    }
    @Column(name="termtext")
    public String getTermText() {
        return _termText;
    }
    public void setTermText(String termText) {
        this._termText = termText;
    }
    public double getSimilarity() {
        return _similarity;
    }
    public void setSimilarity(double similarity) {
        this._similarity = similarity;
    }
    @Column(name="lowertext")
    public String getLowerText() {
        return _lowerText;
    }
    public void setLowerText(String lowerText) {
        this._lowerText = lowerText;
    }
    @Column(name="uppertext")
    public String getUpperText() {
        return _upperText;
    }
    public void setUpperText(String upperText) {
        this._upperText = upperText;
    }
    /**
     * For backwards compatibility we need the deleted column to
     * be either 'n' or 'y'.  This is a workaround to allow this
     * until future versions of JPA that allow different ways 
     * of controlling how types are mapped to the database.
     */
    @Column(name="inclusive", length=1, nullable=true)
    public char isInclusive_JPAWorkaround() {
        return _inclusive;
    }
    public void setInclusive_JPAWorkaround(char inclusive) {
        this._inclusive = inclusive;
    }

    @Transient
    public boolean isInclusive() {
        return _inclusive == 'y';
    }
    public void setInclusive(boolean inclusive) {
        this._inclusive = inclusive ? 'y' : 'n';
    }
}
