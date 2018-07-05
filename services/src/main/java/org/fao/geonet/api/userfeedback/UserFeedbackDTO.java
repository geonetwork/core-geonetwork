/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.api.userfeedback;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.fao.geonet.domain.ISODate;

/**
 * A DTO to represent a user feedback entity optimized for GUI and front end use.
 */
public class UserFeedbackDTO implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5923736997554126836L;

    /** The uuid. */
    private String uuid;

    /** The comment. */
    private String comment;

    /** All ratings **/
    private Map<Integer, Integer> rating = new HashMap<>();

    /** The rating AVG. */
    private Integer ratingAVG;

    /** The metadata UUID. */
    private String metadataUUID;

    /** The metadata title. */
    private String metadataTitle;

    /** The author user id. */
    private Integer authorUserId;

    /** The author name. */
    private String authorName;

    /** The author email. */
    private String authorEmail;

    /** The author organization. */
    private String authorOrganization;

    /** The approver name. */
    private String approverName;

    /** The option privacy. */
    private boolean optionPrivacy;

    /** The published. */
    private boolean published;

    /** The parent uuid. */
    private String parentUuid;

    private String captcha;

    /** The date. */
    private String date;
    /** The keywords. */
    @JsonProperty("keywords")
    private List<String> keywords;

    /** The show approve button. */
    private boolean showApproveButton;

    /**
     * Gets the approver name.
     *
     * @return the approver name
     */
    public String getApproverName() {
        return approverName;
    }

    /**
     * Gets the author email.
     *
     * @return the author email
     */
    public String getAuthorEmail() {
        return authorEmail;
    }

    /**
     * Gets the author name.
     *
     * @return the author name
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * Gets the author organization.
     *
     * @return the author organization
     */
    public String getAuthorOrganization() {
        return authorOrganization;
    }

    /**
     * Gets the author user id.
     *
     * @return the author user id
     */
    public Integer getAuthorUserId() {
        return authorUserId;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the keywords.
     *
     * @return the keywords
     */
    public List<String> getKeywords() {
        return keywords;
    }

    /**
     * Gets the metadata UUID.
     *
     * @return the metadata UUID
     */
    public String getMetadataUUID() {
        return metadataUUID;
    }

    /**
     * Gets the parent uuid.
     *
     * @return the parent uuid
     */
    public String getParentUuid() {
        return parentUuid;
    }

    /**
     * Gets the rating AVG.
     *
     * @return the rating AVG
     */
    public Integer getRatingAVG() {
        return ratingAVG;
    }

    /**
     * Gets the uuid.
     *
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Checks if is option privacy.
     *
     * @return true, if is option privacy
     */
    public boolean isOptionPrivacy() {
        return optionPrivacy;
    }

    /**
     * Checks if is published.
     *
     * @return true, if is published
     */
    public boolean isPublished() {
        return published;
    }

    /**
     * Checks if is show approve button.
     *
     * @return true, if is show approve button
     */
    public boolean isShowApproveButton() {
        return showApproveButton;
    }

    /**
     * Sets the approver name.
     *
     * @param approverName the new approver name
     */
    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    /**
     * Sets the author email.
     *
     * @param authorEmail the new author email
     */
    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    /**
     * Sets the author name.
     *
     * @param authorName the new author name
     */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * Sets the author organization.
     *
     * @param authorOrganization the new author organization
     */
    public void setAuthorOrganization(String authorOrganization) {
        this.authorOrganization = authorOrganization;
    }

    /**
     * Sets the author user id.
     *
     * @param authorUserId the new author user id
     */
    public void setAuthorUserId(Integer authorUserId) {
        this.authorUserId = authorUserId;
    }

    /**
     * Sets the comment.
     *
     * @param comment the new comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Sets the date.
     *
     * @param date the new date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Sets the keywords.
     *
     * @param keywords the new keywords
     */
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    /**
     * Sets the metadata UUID.
     *
     * @param metadataUUID the new metadata UUID
     */
    public void setMetadataUUID(String metadataUUID) {
        this.metadataUUID = metadataUUID;
    }

    /**
     * Sets the option privacy.
     *
     * @param optionPrivacy the new option privacy
     */
    public void setOptionPrivacy(boolean optionPrivacy) {
        this.optionPrivacy = optionPrivacy;
    }

    /**
     * Sets the parent uuid.
     *
     * @param parentUuid the new parent uuid
     */
    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }

    /**
     * Sets the published.
     *
     * @param published the new published
     */
    public void setPublished(boolean published) {
        this.published = published;
    }

    /**
     * Sets the rating AVG.
     *
     * @param ratingAVG the new rating AVG
     */
    public void setRatingAVG(Integer ratingAVG) {
        this.ratingAVG = ratingAVG;
    }

    /**
     * Sets the show approve button.
     *
     * @param showApproveButton the new show approve button
     */
    public void setShowApproveButton(boolean showApproveButton) {
        this.showApproveButton = showApproveButton;
    }

    /**
     * Sets the uuid.
     *
     * @param uuid the new uuid
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Map<Integer, Integer> getRating() {
        return rating;
    }

    public void setRating(Map<Integer, Integer> rating) {
        this.rating = rating;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getMetadataTitle() {
        return metadataTitle;
    }

    public void setMetadataTitle(String metadataTitle) {
        this.metadataTitle = metadataTitle;
    }
}
