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

public class UserFeedbackInputDTO {

    private String comment;
    private int ratingCOMPLETE;
    private int ratingREADABILITY;
    private int ratingFINDABILITY;
    private int ratingOTHER;
    private String metadataUUID;
    private String authorName;
    private String authorEmail;
    private String authorOrganization;
    private boolean optionPrivacy;

    public String getAuthorEmail() {
        return authorEmail;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorOrganization() {
        return authorOrganization;
    }

    public String getComment() {
        return comment;
    }

    public String getMetadataUUID() {
        return metadataUUID;
    }

    public boolean getOptionPrivacy() {
        return optionPrivacy;
    }

    public int getRatingCOMPLETE() {
        return ratingCOMPLETE;
    }

    public int getRatingFINDABILITY() {
        return ratingFINDABILITY;
    }

    public int getRatingOTHER() {
        return ratingOTHER;
    }

    public int getRatingREADABILITY() {
        return ratingREADABILITY;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setAuthorOrganization(String authorOrganization) {
        this.authorOrganization = authorOrganization;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setMetadataUUID(String metadataUUID) {
        this.metadataUUID = metadataUUID;
    }

    public void setOptionPrivacy(boolean optionPrivacy) {
        this.optionPrivacy = optionPrivacy;
    }

    public void setRatingCOMPLETE(int ratingCOMPLETE) {
        this.ratingCOMPLETE = ratingCOMPLETE;
    }

    public void setRatingFINDABILITY(int ratingFINDABILITY) {
        this.ratingFINDABILITY = ratingFINDABILITY;
    }

    public void setRatingOTHER(int ratingOTHER) {
        this.ratingOTHER = ratingOTHER;
    }

    public void setRatingREADABILITY(int ratingREADABILITY) {
        this.ratingREADABILITY = ratingREADABILITY;
    }

}
