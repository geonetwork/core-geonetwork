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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.userfeedback.Rating;
import org.fao.geonet.domain.userfeedback.RatingCriteria;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.domain.userfeedback.UserFeedback.UserRatingStatus;
import org.fao.geonet.repository.userfeedback.RatingCriteriaRepository;
import org.fao.geonet.util.XslUtil;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.fao.geonet.domain.userfeedback.RatingCriteria.AVERAGE_ID;
import static org.fao.geonet.util.XslUtil.md5Hex;

/**
 * Utilities to convert Entities to DTOs and to calculate AVG in ratings.
 */
public class UserFeedbackUtils {

    /**
     * The Class RatingAverage.
     */
    public class RatingAverage {

        private int ratingCount;

        /** The rating average. */
        private Map<Integer, Integer> ratingAverages = new HashMap<>();

        /** The userfeedback count. */
        private int userfeedbackCount;

        /** The last comment. */
        private String lastComment;

        /**
         * Instantiates a new rating average.
         * @param ratingAverages the average for all rating categories
         * @param userfeedbackCount the userfeedback count
         * @param lastComment the last comment
         */
        public RatingAverage(Map<Integer, Integer> ratingAverages, int userfeedbackCount, String lastComment, int ratingCount) {
            this.ratingAverages = ratingAverages;
            this.userfeedbackCount = userfeedbackCount;
            this.lastComment = lastComment;
            this.ratingCount = ratingCount;
        }

        /**
         * Gets the last comment.
         *
         * @return the last comment
         */
        public String getLastComment() {
            return lastComment;
        }

        /**
         * Gets the rating average.
         *
         * @return the rating average
         */
        public Map<Integer, Integer> getRatingAverages() {
            return ratingAverages;
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
         * Sets the last comment.
         *
         * @param lastComment the new last comment
         */
        public void setLastComment(String lastComment) {
            this.lastComment = lastComment;
        }

        /**
         * Sets the rating average.
         *
         * @param ratingAverages the new rating average
         */
        public void setRatingAverages(Map<Integer, Integer> ratingAverages) {
            this.ratingAverages = ratingAverages;
        }

        /**
         * Sets the userfeedback count.
         *
         * @param userfeedbackCount the new userfeedback count
         */
        public void setUserfeedbackCount(int userfeedbackCount) {
            this.userfeedbackCount = userfeedbackCount;
        }

        public int getRatingCount() {
            return ratingCount;
        }

        public void setRatingCount(int ratingCount) {
            this.ratingCount = ratingCount;
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

        userfeedback.setCommentText(inputDto.getComment());

        // Detailed rating list
        List<Rating> ratingList = null;

        userfeedback.setDetailedRatingList(ratingList = new ArrayList<>());

        int avg = 0;
        int avgCount = 0;

        RatingCriteriaRepository ratingCriteriaRepository = ApplicationContextHolder.get().getBean(RatingCriteriaRepository.class);
        List<RatingCriteria> criteriaList = ratingCriteriaRepository.findAll();
        if (inputDto.getRating() != null) {
            Iterator<Integer> iterator = inputDto.getRating().keySet().iterator();
            while (iterator.hasNext()) {
                Integer criteriaId = iterator.next();
                RatingCriteria criteria = ratingCriteriaRepository.findById(criteriaId);
                if (criteria != null) {
                    Rating rating = new Rating();
                    rating.setUserfeedback(userfeedback);
                    rating.setCategory(criteria);
                    rating.setRating(inputDto.getRating().get(criteriaId));
                    ratingList.add(rating);
                } else {
                    // Criteria not found
                }
            }
        }

        if (inputDto.getRatingAVG() != null) {
            RatingCriteria criteria = ratingCriteriaRepository.findById(AVERAGE_ID);
            if (criteria != null) {
                Rating rating = new Rating();
                rating.setUserfeedback(userfeedback);
                rating.setCategory(criteria);
                rating.setRating(inputDto.getRatingAVG());
                ratingList.add(rating);
            } else {
                // Criteria not found
            }
        }

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

        userfeedback.setCreationDate(new ISODate(System.currentTimeMillis()).toDate());

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
        userfeedbackDto.setComment(input.getCommentText());
        userfeedbackDto.setMetadataUUID(input.getMetadata().getUuid());
        userfeedbackDto.setDate(new ISODate(input.getCreationDate().getTime()).getDateAndTime());

        if (input.getAuthorId() != null) {
            userfeedbackDto.setAuthorUserId(input.getAuthorId().getId());
        } else {
            userfeedbackDto.setAuthorUserId(0);
        }
        if (input.getParent() != null) {
            userfeedbackDto.setParentUuid(input.getParent().getUuid());
        }

        final List<Rating> ratingList = input.getDetailedRatingList();
        Map<Integer, Integer> ratings = new HashMap<>();
        for (final Rating rating : ratingList) {
            Integer id = rating.getCategory().getId();
            if (id == RatingCriteria.AVERAGE_ID) {
                userfeedbackDto.setRatingAVG(rating.getRating());
            } else {
                ratings.put(id, rating.getRating());
            }
        }
        userfeedbackDto.setRating(ratings);

        if (input.getAuthorPrivacy() == 0) {
            userfeedbackDto.setAuthorName("Anonymous");
            userfeedbackDto.setAuthorOrganization("");
        } else {
            userfeedbackDto.setAuthorName(input.getAuthorName());
            // Add md5 email hash for gravatar
            if (isNotBlank(input.getAuthorEmail())) {
                userfeedbackDto.setAuthorEmail(md5Hex(input.getAuthorEmail()));
            }
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

        userfeedbackDto.setMetadataTitle(XslUtil.getIndexField(null, userfeedbackDto.getMetadataUUID(), "title", ""));

        return userfeedbackDto;
    }

    /**
     * Gets the average.
     *
     * @param list the list
     * @return the average
     */
    public RatingAverage getAverage(List<UserFeedback> list) {
        // Number of average rating taken into account
        // You can have 6 feedbacks, 4 with rating.
        int ratingCount = 0;
        ISODate maxDate = null; // LAST COMMENT DATE
        RatingAverage v = null;

        if (list.size() > 0) {
            Map<Integer, Integer> ratingAverages = new HashMap<>();
            RatingCriteriaRepository criteriaRepository = ApplicationContextHolder.get().getBean(RatingCriteriaRepository.class);
            List<RatingCriteria> criteriaList = criteriaRepository.findAll();
            // Init map of averages
            criteriaList.forEach(e -> ratingAverages.put(e.getId(), 0));

            for (final UserFeedback userFeedback : list) {

                if (maxDate == null || userFeedback.getCreationDate() != null && userFeedback.getCreationDate().after(maxDate.toDate())) {
                    maxDate = new ISODate(userFeedback.getCreationDate().getTime());
                }

                if (userFeedback.getDetailedRatingList() != null && userFeedback.getDetailedRatingList().size() > 0) {

                    for (final Rating rating : userFeedback.getDetailedRatingList()) {
                        Integer criteriaId = rating.getCategory().getId();
                        Integer value = rating.getRating();
                        if (value != null && value > 0 && ratingAverages.get(criteriaId) != null) {
                            ratingAverages.put(criteriaId, (ratingAverages.get(criteriaId) + value) / 2);
                        }
                        if (criteriaId == AVERAGE_ID) {
                            ratingCount ++;
                        }
                    }

                }
            }

            v = new RatingAverage(ratingAverages, list.size(), maxDate.getDateAndTime(), ratingCount);

        } else {
            v = new RatingAverage(new HashMap<>(), 0, null, ratingCount);
        }

        return v;
    }

}
