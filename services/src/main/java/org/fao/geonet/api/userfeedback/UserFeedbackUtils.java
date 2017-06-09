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

import org.fao.geonet.domain.User;
import org.fao.geonet.domain.userfeedback.Rating;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.domain.userfeedback.UserFeedback.UserRatingStatus;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.modelmapper.ModelMapper;

public class UserFeedbackUtils {

    public class RatingAverage {

        private String metadataTitle;
        private int ratingAverage;
        private int ratingCount;

        private Integer avgCOMPLETE;
        private Integer avgREADABILITY;
        private Integer avgFINDABILITY;
        private Integer avgOTHER;

        private int userfeedbackCount;
        private Date lastComment;

        public RatingAverage(String metadataTitle, int ratingAverage, int userfeedbackCount, Date lastComment,
                int ratingCount) {
            this.metadataTitle = metadataTitle;
            this.ratingAverage = ratingAverage;
            this.userfeedbackCount = userfeedbackCount;
            this.lastComment = lastComment;
            this.ratingCount = ratingCount;
        }

        public Integer getAvgCOMPLETE() {
            return avgCOMPLETE;
        }

        public Integer getAvgFINDABILITY() {
            return avgFINDABILITY;
        }

        public Integer getAvgOTHER() {
            return avgOTHER;
        }

        public Integer getAvgREADABILITY() {
            return avgREADABILITY;
        }

        public Date getLastComment() {
            return lastComment;
        }

        public String getMetadataTitle() {
            return metadataTitle;
        }

        public int getRatingAverage() {
            return ratingAverage;
        }

        public int getRatingCount() {
            return ratingCount;
        }

        public int getUserfeedbackCount() {
            return userfeedbackCount;
        }

        public void setAvgCOMPLETE(Integer avgCOMPLETE) {
            this.avgCOMPLETE = avgCOMPLETE;
        }

        public void setAvgFINDABILITY(Integer avgFINDABILITY) {
            this.avgFINDABILITY = avgFINDABILITY;
        }

        public void setAvgOTHER(Integer avgOTHER) {
            this.avgOTHER = avgOTHER;
        }

        public void setAvgREADABILITY(Integer avgREADABILITY) {
            this.avgREADABILITY = avgREADABILITY;
        }

        public void setLastComment(Date lastComment) {
            this.lastComment = lastComment;
        }

        public void setMetadataTitle(String metadataTitle) {
            this.metadataTitle = metadataTitle;
        }

        public void setRatingAverage(int ratingAverage) {
            this.ratingAverage = ratingAverage;
        }

        public void setRatingCount(int ratingCount) {
            this.ratingCount = ratingCount;
        }

        public void setUserfeedbackCount(int userfeedbackCount) {
            this.userfeedbackCount = userfeedbackCount;
        }

    }

    // Convert to an object ready for INSERT
    public static UserFeedback convertFromInputDto(UserFeedbackInputDTO userfeedbackInputDto, User author) {

        final ModelMapper modelMapper = new ModelMapper();

        final UserFeedback userfeedback = modelMapper.map(userfeedbackInputDto, UserFeedback.class);

        // Force UUID (to remove in case of update)
        userfeedback.setUuid(UUID.randomUUID().toString());

        List<Rating> ratingList = null;

        userfeedback.setDetailedRatingList(ratingList = new ArrayList<>());

        int avg = 0;
        int avgCount = 0;

        if (userfeedbackInputDto.getRatingCOMPLETE() != 0) {

            final Rating r = new Rating();

            r.setRating(userfeedbackInputDto.getRatingCOMPLETE());
            r.setCategory(Rating.Category.COMPLETE);

            avg = avg + r.getRating();
            avgCount++;

            ratingList.add(r);
        }
        if (userfeedbackInputDto.getRatingFINDABILITY() != 0) {

            final Rating r = new Rating();

            r.setRating(userfeedbackInputDto.getRatingFINDABILITY());
            r.setCategory(Rating.Category.FINDABILITY);

            avg = avg + r.getRating();
            avgCount++;

            ratingList.add(r);
        }
        if (userfeedbackInputDto.getRatingREADABILITY() != 0) {

            final Rating r = new Rating();

            r.setRating(userfeedbackInputDto.getRatingREADABILITY());
            r.setCategory(Rating.Category.READABILITY);

            avg = avg + r.getRating();
            avgCount++;

            ratingList.add(r);
        }
        if (userfeedbackInputDto.getRatingOTHER() != 0) {

            final Rating r = new Rating();

            r.setRating(userfeedbackInputDto.getRatingOTHER());
            r.setCategory(Rating.Category.OTHER);

            avg = avg + r.getRating();
            avgCount++;

            ratingList.add(r);
        }

        if (avgCount > 0 && avg > 0) {
            System.out.println("AVG " + avg);
            System.out.println("AVG COUNT " + avgCount);

            avg = avg / avgCount;
        }

        // Calculate and add the AVG
        final Rating r = new Rating();
        r.setRating(avg);
        r.setCategory(Rating.Category.AVG);
        ratingList.add(r);

        if (author != null) {
            userfeedback.setAuthorId(author);
            userfeedback.setAuthorName(author.getName() + " " + author.getSurname());
            userfeedback.setAuthorEmail(author.getEmail());
            userfeedback.setAuthorOrganization(author.getOrganisation());
            userfeedback.setStatus(UserRatingStatus.PUBLISHED);
        } else {
            userfeedback.setAuthorId(null);
            userfeedback.setAuthorName(userfeedbackInputDto.getAuthorName());
            userfeedback.setAuthorEmail(userfeedbackInputDto.getAuthorEmail());
            userfeedback.setAuthorOrganization(userfeedbackInputDto.getAuthorOrganization());
            userfeedback.setStatus(UserRatingStatus.WAITING_FOR_APPROVAL);
        }

        if (userfeedbackInputDto.getOptionPrivacy()) {
            userfeedback.setAuthorPrivacy(1);
        } else {
            userfeedback.setAuthorPrivacy(0);
        }

        userfeedback.setApprover(null);

        return userfeedback;
    }

    public static UserFeedbackDTO convertToDto(UserFeedback userfeedback) {

        final ModelMapper modelMapper = new ModelMapper();

        final UserFeedbackDTO userfeedbackDto = modelMapper.map(userfeedback, UserFeedbackDTO.class);

        final List<Rating> ratingList = userfeedback.getDetailedRatingList();

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
            if (rating.getCategory().equals(Rating.Category.OTHER)) {
                userfeedbackDto.setRatingOTHER(rating.getRating());
            }
        }

        if (userfeedback.getAuthorPrivacy() == 0) {
            userfeedbackDto.setAuthorName("Anonymous");
            userfeedbackDto.setAuthorOrganization("");
        } else {
            userfeedbackDto.setAuthorName(userfeedback.getAuthorName());
            userfeedbackDto.setAuthorOrganization(userfeedback.getAuthorOrganization());
        }

        return userfeedbackDto;
    }

    public RatingAverage getAverage(List<UserFeedback> list) {
        int avg = 0; // AVG
        int countAvg = 0; // Elements in AVG
        Date maxDate = null; // LAST COMMENT DATE
        String metadataTitle = null;
        RatingAverage v = null;

        if (list.size() > 0) {

            try {
                final Element element = list.get(0).getMetadata().getXmlData(false);
                final XPath xpath = XPath.newInstance(
                        "gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
                metadataTitle = xpath.valueOf(element);
            } catch (final Exception e) {
                e.printStackTrace();
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

        }

        return v;
    }

}
