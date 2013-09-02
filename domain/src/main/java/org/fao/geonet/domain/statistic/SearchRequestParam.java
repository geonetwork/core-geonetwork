package org.fao.geonet.domain.statistic;

import org.fao.geonet.domain.Constants;

import javax.persistence.*;

/**
 * Entity representing the search parameters of a request. Related to {@link SearchRequest}.
 * 
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "params")
public class SearchRequestParam {
    private int _id;
    private LuceneQueryParamType _queryType;
    private String _termField;
    private String _termText;
    private double _similarity;
    private String _lowerText;
    private String _upperText;
    private char _inclusive = Constants.YN_DISABLED;
    private SearchRequest _request;

    /**
     * Get the id of the request parameters this entity represents. This is a generated value and as such new instances should not have this
     * set as it will simply be ignored and could result in reduced performance.
     * 
     * @return the id of the request parameters this entity represents.
     */
    @Id
    @GeneratedValue
    public int getId() {
        return _id;
    }

    /**
     * Set the id of the request parameters this entity represents. This is a generated value and as such new instances should not have this
     * set as it will simply be ignored and could result in reduced performance.
     * 
     * @param id the id of the request parameters this entity represents.
     */
    public void setId(int id) {
        this._id = id;
    }

    /**
     * Get the request associated with this entity.
     *
     * @return the request associated with this entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id", name = "requestid")
    public SearchRequest getRequest() {
        return _request;
    }

    /**
     * Set the request associated with this entity.
     *
     * @param request the request associated with this entity.
     */
    public void setRequest(SearchRequest request) {
        this._request = request;
    }

    /**
     * Get the type of query parameter.
     * 
     * @return the type of query parameter.
     */
    @Column(name = "querytype")
    public LuceneQueryParamType getQueryType() {
        return _queryType;
    }

    /**
     * Set the type of query parameter
     * 
     * @param queryType the type of query parameter
     */
    public void setQueryType(LuceneQueryParamType queryType) {
        this._queryType = queryType;
    }

    /**
     * Return the name of the term used in the search parameter.
     * 
     * @return the name of the term used in the search parameter.
     */
    @Column(name = "termfield")
    public String getTermField() {
        return _termField;
    }

    /**
     * Set the name of the term used in the search parameter.
     * 
     * @param termField the name of the term used in the search parameter.
     */
    public void setTermField(String termField) {
        this._termField = termField;
    }

    /**
     * Get the value searched for in the current search parameter.
     * 
     * @return the value searched for in the current search parameter.
     */
    @Column(name = "termtext")
    public String getTermText() {
        return _termText;
    }

    /**
     * Set the value searched for in the current search parameter.
     * 
     * @param termText the value searched for in the current search parameter.
     */
    public void setTermText(String termText) {
        this._termText = termText;
    }

    /**
     * Set the similarity level.
     * 
     * @return the similarity level.
     */
    public double getSimilarity() {
        return _similarity;
    }

    /**
     * Set the similarity level.
     * 
     * @param similarity the similarity level.
     */
    public void setSimilarity(double similarity) {
        this._similarity = similarity;
    }

    /**
     * Get the lower level if the search parameter is a range query.
     * 
     * @return the lower level if the search parameter is a range query.
     */
    @Column(name = "lowertext")
    public String getLowerText() {
        return _lowerText;
    }

    /**
     * Set the lower level if the search parameter is a range query.
     * 
     * @param lowerText the lower level if the search parameter is a range query.
     */
    public void setLowerText(String lowerText) {
        this._lowerText = lowerText;
    }

    /**
     * Get the upper level if the search parameter is a range query.
     * 
     * @return the upper level if the search parameter is a range query.
     */
    @Column(name = "uppertext")
    public String getUpperText() {
        return _upperText;
    }

    /**
     * Set the upper level if the search parameter is a range query.
     * 
     * @param upperText the upper level if the search parameter is a range query.
     */
    public void setUpperText(String upperText) {
        this._upperText = upperText;
    }

    /**
     * For backwards compatibility we need the inclusive column to be either 'n' or 'y'. This is a workaround to allow this until future
     * versions of JPA that allow different ways of controlling how types are mapped to the database.
     */
    @Column(name = "inclusive", length = 1, nullable = true)
    protected char getInclusive_JPAWorkaround() {
        return _inclusive;
    }

    /**
     * Set the inclusive column value Constants.YN_ENABLED or Constants.YN_DISABLED
     * @param inclusive the inclusive column value Constants.YN_ENABLED or Constants.YN_DISABLED
     */
    protected void setInclusive_JPAWorkaround(char inclusive) {
        this._inclusive = inclusive;
    }

    /**
     * Return true if the query is a range query and is inclusive.
     * @return true if the query is a range query and is inclusive.
     */
    @Transient
    public boolean isInclusive() {
        return Constants.toBoolean_fromYNChar(getInclusive_JPAWorkaround());
    }

    /**
     * Set true if the query is a range query and is inclusive.
     * @param inclusive true if the query is a range query and is inclusive.
     */
    public void setInclusive(boolean inclusive) {
        setInclusive_JPAWorkaround(Constants.toYN_EnabledChar(inclusive));
    }
}
