package org.fao.geonet.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Access(AccessType.PROPERTY)
@Table(name = "params")
public class RequestParam {
    private int id;
    private int requestId;
    private String queryType;
    private String termField;
    private String termText;
    private double similarity;
    private String lowerText;
    private String upperText;
    private boolean inclusive;
    
    @Id
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    @Column(name="requestid")
    public int getRequestId() {
        return requestId;
    }
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
    @Column(name="querytype")
    public String getQueryType() {
        return queryType;
    }
    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }
    @Column(name="termfield")
    public String getTermField() {
        return termField;
    }
    public void setTermField(String termField) {
        this.termField = termField;
    }
    @Column(name="termtext")
    public String getTermText() {
        return termText;
    }
    public void setTermText(String termText) {
        this.termText = termText;
    }
    public double getSimilarity() {
        return similarity;
    }
    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }
    @Column(name="lowertext")
    public String getLowerText() {
        return lowerText;
    }
    public void setLowerText(String lowerText) {
        this.lowerText = lowerText;
    }
    @Column(name="uppertext")
    public String getUpperText() {
        return upperText;
    }
    public void setUpperText(String upperText) {
        this.upperText = upperText;
    }
    public boolean isInclusive() {
        return inclusive;
    }
    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }
}
