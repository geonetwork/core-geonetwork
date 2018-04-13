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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.userfeedback.Rating;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.domain.userfeedback.UserFeedback.UserRatingStatus;
import org.jdom.Element;
import org.jdom.xpath.XPath;

/**
 * Utilities to convert Entities to DTOs and to calculate AVG in ratings.
 */
public class UserFeedbackUtils {

    /**
     * The Class RatingAverage.
     */
    public class RatingAverage {

        /** The metadata title. */
        private String metadataTitle;

        /** The rating average. */
        private int ratingAverage;

        /** The rating count. */
        private int ratingCount;

        /** The avg COMPLETE. */
        private int avgCOMPLETE;

        /** The avg READABILITY. */
        private int avgREADABILITY;

        /** The avg FINDABILITY. */
        private int avgFINDABILITY;

        /** The avg DATAQUALITY. */
        private int avgDATAQUALITY;

        /** The avg SERVICEQUALITY. */
        private int avgSERVICEQUALITY;

        /** The avg OTHER. */
        private int avgOTHER;

        /** The userfeedback count. */
        private int userfeedbackCount;

        /** The last comment. */
        private Date lastComment;

        /**
         * Instantiates a new rating average.
         *
         * @param metadataTitle the metadata title
         * @param ratingAverage the rating average
         * @param userfeedbackCount the userfeedback count
         * @param lastComment the last comment
         * @param ratingCount the rating count
         */
        public RatingAverage(String metadataTitle, int ratingAverage, int userfeedbackCount, Date lastComment, int ratingCount) {
            this.metadataTitle = metadataTitle;
            this.ratingAverage = ratingAverage;
            this.userfeedbackCount = userfeedbackCount;
            this.lastComment = lastComment;
            this.ratingCount = ratingCount;
        }

        /**
         * Gets the avg COMPLETE.
         *
         * @return the avg COMPLETE
         */
        public Integer getAvgCOMPLETE() {
            return avgCOMPLETE;
        }

        /**
         * Gets the avg DATAQUALITY.
         *
         * @return the avg DATAQUALITY
         */
        public Integer getAvgDATAQUALITY() {
            return avgDATAQUALITY;
        }

        /**
         * Gets the avg FINDABILITY.
         *
         * @return the avg FINDABILITY
         */
        public Integer getAvgFINDABILITY() {
            return avgFINDABILITY;
        }

        /**
         * Gets the avg OTHER.
         *
         * @return the avg OTHER
         */
        public Integer getAvgOTHER() {
            return avgOTHER;
        }

        /**
         * Gets the avg READABILITY.
         *
         * @return the avg READABILITY
         */
        public Integer getAvgREADABILITY() {
            return avgREADABILITY;
        }

        /**
         * Gets the avg SERVICEQUALITY.
         *
         * @return the avg SERVICEQUALITY
         */
        public Integer getAvgSERVICEQUALITY() {
            return avgSERVICEQUALITY;
        }

        /**
         * Gets the last comment.
         *
         * @return the last comment
         */
        public Date getLastComment() {
            return lastComment;
        }

        /**
         * Gets the metadata title.
         *
         * @return the metadata title
         */
        public String getMetadataTitle() {
            return metadataTitle;
        }

        /**
         * Gets the rating average.
         *
         * @return the rating average
         */
        public int getRatingAverage() {
            return ratingAverage;
        }

        /**
         * Gets the rating count.
         *
         * @return the rating count
         */
        public int getRatingCount() {
            return ratingCount;
        }

        /**
         * Gets the userfeedback count.
         *
         * @return the userfeedback count
         */
        public int getUserfeedbackCount() {
            return userfeedbackCount;
        }

        /**
         * Sets the avg COMPLETE.
         *
         * @param avgCOMPLETE the new avg COMPLETE
         */
        public void setAvgCOMPLETE(Integer avgCOMPLETE) {
            this.avgCOMPLETE = avgCOMPLETE;
        }

        /**
         * Sets the avg DATAQUALITY.
         *
         * @param avgDATAQUALITY the new avg DATAQUALITY
         */
        public void setAvgDATAQUALITY(Integer avgDATAQUALITY) {
            this.avgDATAQUALITY = avgDATAQUALITY;
        }

        /**
         * Sets the avg FINDABILITY.
         *
         * @param avgFINDABILITY the new avg FINDABILITY
         */
        public void setAvgFINDABILITY(Integer avgFINDABILITY) {
            this.avgFINDABILITY = avgFINDABILITY;
        }

        /**
         * Sets the avg OTHER.
         *
         * @param avgOTHER the new avg OTHER
         */
        public void setAvgOTHER(Integer avgOTHER) {
            this.avgOTHER = avgOTHER;
        }

        /**
         * Sets the avg READABILITY.
         *
         * @param avgREADABILITY the new avg READABILITY
         */
        public void setAvgREADABILITY(Integer avgREADABILITY) {
            this.avgREADABILITY = avgREADABILITY;
        }

        /**
         * Sets the avg SERVICEQUALITY.
         *
         * @param avgSERVICEQUALITY the new avg SERVICEQUALITY
         */
        public void setAvgSERVICEQUALITY(Integer avgSERVICEQUALITY) {
            this.avgSERVICEQUALITY = avgSERVICEQUALITY;
        }

        /**
         * Sets the last comment.
         *
         * @param lastComment the new last comment
         */
        public void setLastComment(Date lastComment) {
            this.lastComment = lastComment;
        }

        /**
         * Sets the metadata title.
         *
         * @param metadataTitle the new metadata title
         */
        public void setMetadataTitle(String metadataTitle) {
            this.metadataTitle = metadataTitle;
        }

        /**
         * Sets the rating average.
         *
         * @param ratingAverage the new rating average
         */
        public void setRatingAverage(int ratingAverage) {
            this.ratingAverage = ratingAverage;
        }

        /**
         * Sets the rating count.
         *
         * @param ratingCount the new rating count
         */
        public void setRatingCount(int ratingCount) {
            this.ratingCount = ratingCount;
        }

        /**
         * Sets the userfeedback count.
         *
         * @param userfeedbackCount the new userfeedback count
         */
        public void setUserfeedbackCount(int userfeedbackCount) {
            this.userfeedbackCount = userfeedbackCount;
        }

    }

    /**
     * Convert from dto.
     *
     * @param inputDto the input dto
     * @param author the author
     * @return the user feedback
     */
    // Convert from DTO to an entity object
    public static UserFeedback convertFromDto(UserFeedbackDTO inputDto, User author) {

        final UserFeedback userfeedback = new UserFeedback();

        if (inputDto.getUuid() == null || inputDto.getUuid().equals("")) {
            userfeedback.setUuid(UUID.randomUUID().toString());
        } else {
            userfeedback.setUuid(inputDto.getUuid());
        }

        userfeedback.setComment(inputDto.getComment());

        // Detailed rating list
        List<Rating> ratingList = null;

        userfeedback.setDetailedRatingList(ratingList = new ArrayList<>());

        int avg = 0;
        int avgCount = 0;

        if (inputDto.getRatingCOMPLETE() != null && inputDto.getRatingCOMPLETE() != 0) {

            final Rating r = new Rating();

            r.setRating(inputDto.getRatingCOMPLETE());
            r.setCategory(Rating.Category.COMPLETE);

            avg = avg + r.getRating();
            avgCount++;

            ratingList.add(r);
        }
        if (inputDto.getRatingFINDABILITY() != null && inputDto.getRatingFINDABILITY() != 0) {

            final Rating r = new Rating();

            r.setRating(inputDto.getRatingFINDABILITY());
            r.setCategory(Rating.Category.FINDABILITY);

            avg = avg + r.getRating();
            avgCount++;

            ratingList.add(r);
        }
        if (inputDto.getRatingREADABILITY() != null && inputDto.getRatingREADABILITY() != 0) {

            final Rating r = new Rating();

            r.setRating(inputDto.getRatingREADABILITY());
            r.setCategory(Rating.Category.READABILITY);

            avg = avg + r.getRating();
            avgCount++;

            ratingList.add(r);
        }
        if (inputDto.getRatingDATAQUALITY() != null && inputDto.getRatingDATAQUALITY() != 0) {

            final Rating r = new Rating();

            r.setRating(inputDto.getRatingDATAQUALITY());
            r.setCategory(Rating.Category.DATAQUALITY);

            avg = avg + r.getRating();
            avgCount++;

            ratingList.add(r);
        }
        if (inputDto.getRatingSERVICEQUALITY() != null && inputDto.getRatingSERVICEQUALITY() != 0) {

            final Rating r = new Rating();

            r.setRating(inputDto.getRatingSERVICEQUALITY());
            r.setCategory(Rating.Category.SERVICEQUALITY);

            avg = avg + r.getRating();
            avgCount++;

            ratingList.add(r);
        }
        if (inputDto.getRatingOTHER() != null && inputDto.getRatingOTHER() != 0) {

            final Rating r = new Rating();

            r.setRating(inputDto.getRatingOTHER());
            r.setCategory(Rating.Category.OTHER);

            avg = avg + r.getRating();
            avgCount++;

            ratingList.add(r);
        }

        if (avgCount > 0 && avg > 0) {
            avg = avg / avgCount;
        }

        // Calculate and add the AVG
        final Rating r = new Rating();
        r.setRating(avg);
        r.setCategory(Rating.Category.AVG);
        ratingList.add(r);

        // Associated metadata
        final Metadata metadata = new Metadata();
        metadata.setUuid(inputDto.getMetadataUUID());
        userfeedback.setMetadata(metadata);

        // parent userfeedback (so is a comment o a comment)
        if (inputDto.getParentUuid() != null) {
            final UserFeedback parent = new UserFeedback();
            parent.setUuid(inputDto.getParentUuid());
            userfeedback.setParent(parent);
        }

        userfeedback.setDate(new Date(System.currentTimeMillis()));

        if (author != null) {
            userfeedback.setAuthorId(author);
            userfeedback.setAuthorName(author.getName() + " " + author.getSurname());
            userfeedback.setAuthorEmail(author.getEmail());
            userfeedback.setAuthorOrganization(author.getOrganisation());
            userfeedback.setStatus(UserRatingStatus.PUBLISHED);
            userfeedback.setAuthorPrivacy(1);
        } else {
            userfeedback.setAuthorId(null);
            userfeedback.setAuthorName(inputDto.getAuthorName());
            userfeedback.setAuthorEmail(inputDto.getAuthorEmail());
            userfeedback.setAuthorOrganization(inputDto.getAuthorOrganization());
            userfeedback.setStatus(UserRatingStatus.WAITING_FOR_APPROVAL);
            if (inputDto.isOptionPrivacy()) {
                userfeedback.setAuthorPrivacy(1);
            } else {
                userfeedback.setAuthorPrivacy(0);
            }
        }

        // Fields to implement
        userfeedback.setKeywords(null);
        userfeedback.setCitation(null);
        userfeedback.setApprover(null);

        return userfeedback;
    }

    /**
     * Convert to dto.
     *
     * @param input the input
     * @return the user feedback DTO
     */
    public static UserFeedbackDTO convertToDto(UserFeedback input) {

        final UserFeedbackDTO userfeedbackDto = new UserFeedbackDTO();

        userfeedbackDto.setUuid(input.getUuid());
        userfeedbackDto.setComment(input.getComment());
        userfeedbackDto.setMetadataUUID(input.getMetadata().getUuid());
        userfeedbackDto.setDate(input.getDate());

        if (input.getAuthorId() != null) {
            userfeedbackDto.setAuthorUserId(input.getAuthorId().getId());
        } else {
            userfeedbackDto.setAuthorUserId(0);
        }
        if (input.getParent() != null) {
            userfeedbackDto.setParentUuid(input.getParent().getUuid());
        }

        final List<Rating> ratingList = input.getDetailedRatingList();

        for (final Rating rating : ratingList) {
            if (rating.getCategory().equals(Rating.Category.AVG)) {
                userfeedbackDto.setRatingAVG(rating.getRating());
            }
            if (rating.getCategory().equals(Rating.Category.COMPLETE)) {
                userfeedbackDto.setRatingCOMPLETE(rating.getRating());
            }
            if (rating.getCategory().equals(Rating.Category.FINDABILITY)) {
                userfeedbackDto.setRatingFINDABILITY(rating.getRating());
            }
            if (rating.getCategory().equals(Rating.Category.READABILITY)) {
                userfeedbackDto.setRatingREADABILITY(rating.getRating());
            }
            if (rating.getCategory().equals(Rating.Category.DATAQUALITY)) {
                userfeedbackDto.setRatingDATAQUALITY(rating.getRating());
            }
            if (rating.getCategory().equals(Rating.Category.SERVICEQUALITY)) {
                userfeedbackDto.setRatingSERVICEQUALITY(rating.getRating());
            }
            if (rating.getCategory().equals(Rating.Category.OTHER)) {
                userfeedbackDto.setRatingOTHER(rating.getRating());
            }
        }

        if (input.getAuthorPrivacy() == 0) {
            userfeedbackDto.setAuthorName("Anonymous");
            userfeedbackDto.setAuthorOrganization("");
        } else {
            userfeedbackDto.setAuthorName(input.getAuthorName());
            userfeedbackDto.setAuthorOrganization(input.getAuthorOrganization());
        }

        // Workflow fields
        if (input.getStatus().equals(UserRatingStatus.PUBLISHED)) {
            userfeedbackDto.setPublished(true);
            if (input.getApprover() != null) {
                userfeedbackDto.setApproverName(input.getApprover().getName());
            }
        } else {
            userfeedbackDto.setPublished(false);
        }

        String metadataTitle;
        try {
            final Element element = input.getMetadata().getXmlData(false);
            final XPath xpath = XPath.newInstance("gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
            metadataTitle = xpath.valueOf(element);
        } catch (final Exception e) {
            metadataTitle = "Metadata comments";
        }

        userfeedbackDto.setMetadataTitle(metadataTitle);

        return userfeedbackDto;
    }

    /**
     * Gets the average.
     *
     * @param list the list
     * @return the average
     */
    public RatingAverage getAverage(List<UserFeedback> list) {
        int avg = 0; // AVG
        int countAvg = 0; // Elements in AVG
        Date maxDate = null; // LAST COMMENT DATE
        String metadataTitle = null;
        RatingAverage v = null;

        if (list.size() > 0) {

            if (list.get(0).getMetadata() != null) {

                try {
                    final Element element = list.get(0).getMetadata().getXmlData(false);
                    final XPath xpath = XPath
                            .newInstance("gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
                    metadataTitle = xpath.valueOf(element);
                } catch (final Exception e) {
                    metadataTitle = "Metadata comments";
                }
            } else {
                metadataTitle = "Metadata comments";
            }

            // metadataTitle =
            // list.get(0).getMetadata().getXmlData(false).getAttributeValue("");

            for (final UserFeedback userFeedback : list) {

                if (maxDate == null || userFeedback.getDate() != null && userFeedback.getDate().after(maxDate)) {
                    maxDate = userFeedback.getDate();
                }

                if (userFeedback.getDetailedRatingList() != null && userFeedback.getDetailedRatingList().size() > 0) {

                    for (final Rating rating : userFeedback.getDetailedRatingList()) {

                        if (rating.getCategory().equals(Rating.Category.AVG) && rating.getRating() > 0) {
                            countAvg++;
                            avg = avg + rating.getRating();
                        }
                    }

                }
            }
            if (avg > 0 && countAvg > 0) {
                avg = avg / countAvg;
            }

            v = new RatingAverage(metadataTitle, avg, list.size(), maxDate, countAvg);

        } else {
            v = new RatingAverage("Metadata comments", 0, 0, null, 0);
        }

        return v;
    }

}
