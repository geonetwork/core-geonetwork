package org.fao.geonet.api.userfeedback;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.fao.geonet.domain.userfeedback.Rating;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserFeedbackDTO implements Serializable {
    
    private static final long serialVersionUID = -5923736997554126836L;

    private String uuid;
    private String comment;
    private Integer ratingAVG;
    private Integer ratingCOMPLETE;
    private Integer ratingREADABILITY;
    private Integer ratingFINDABILITY;
    private Integer ratingOTHER;
    private String metadataUUID;
    private String authorName;
    private String approverName;
    
    @JsonProperty("keywords")
    private List<String> keywords;
    
    private Date date;
    private boolean showApproveButton;
        
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public Integer getRatingAVG() {
        return ratingAVG;
    }
    public void setRatingAVG(Integer ratingAVG) {
        this.ratingAVG = ratingAVG;
    }
   
    public Integer getRatingCOMPLETE() {
        return ratingCOMPLETE;
    }
    public void setRatingCOMPLETE(Integer ratingCOMPLETE) {
        this.ratingCOMPLETE = ratingCOMPLETE;
    }
    public Integer getRatingREADABILITY() {
        return ratingREADABILITY;
    }
    public void setRatingREADABILITY(Integer ratingREADABILITY) {
        this.ratingREADABILITY = ratingREADABILITY;
    }
    public Integer getRatingFINDABILITY() {
        return ratingFINDABILITY;
    }
    public void setRatingFINDABILITY(Integer ratingFINDABILITY) {
        this.ratingFINDABILITY = ratingFINDABILITY;
    }
    public Integer getRatingOTHER() {
        return ratingOTHER;
    }
    public void setRatingOTHER(Integer ratingOTHER) {
        this.ratingOTHER = ratingOTHER;
    }
    public String getMetadataUUID() {
        return metadataUUID;
    }
    public void setMetadataUUID(String metadataUUID) {
        this.metadataUUID = metadataUUID;
    }    
    public String getAuthorName() {
        return authorName;
    }
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    public String getApproverName() {
        return approverName;
    }
    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }
    public List<String> getKeywords() {
        return keywords;
    }
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public boolean isShowApproveButton() {
        return showApproveButton;
    }
    public void setShowApproveButton(boolean showApproveButton) {
        this.showApproveButton = showApproveButton;
    }
    
    

}
