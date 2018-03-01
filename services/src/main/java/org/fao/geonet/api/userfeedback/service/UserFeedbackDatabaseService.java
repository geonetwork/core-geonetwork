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
package org.fao.geonet.api.userfeedback.service;

import java.util.Date;
import java.util.List;

import org.apache.jcs.access.exception.ObjectNotFoundException;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.userfeedback.Rating;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.domain.userfeedback.UserFeedback.UserRatingStatus;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.userfeedback.RatingRepository;
import org.fao.geonet.repository.userfeedback.UserFeedbackRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * User feedback provider that uses database persistence.
 *
 * @author Jose García
 */
@Service
public class UserFeedbackDatabaseService implements IUserFeedbackService {

    /*
     * (non-Javadoc)
     *
     * @see org.fao.geonet.api.userfeedback.service.IUserFeedbackService#
     * publishUserFeedback(java.lang.String, org.fao.geonet.domain.User)
     */
    @Override
    public void publishUserFeedback(String feedbackUuid, User user) throws ObjectNotFoundException {
        final UserFeedbackRepository userFeedbackRepository = ApplicationContextHolder.get()
                .getBean(UserFeedbackRepository.class);

        final UserFeedback userFeedback = userFeedbackRepository.findByUuid(feedbackUuid);

        if(userFeedback != null) {
            userFeedback.setStatus(UserRatingStatus.PUBLISHED);
            userFeedback.setApprover(user);

            userFeedbackRepository.save(userFeedback);
        } else {
            throw new ObjectNotFoundException();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.fao.geonet.api.userfeedback.service.IUserFeedbackService#
     * removeUserFeedback(java.lang.String)
     */
    @Override
    public void removeUserFeedback(String feedbackUuid) {
        final UserFeedbackRepository userFeedbackRepository = ApplicationContextHolder.get()
                .getBean(UserFeedbackRepository.class);

        final UserFeedback userFeedback = userFeedbackRepository.findByUuid(feedbackUuid);

        userFeedbackRepository.delete(userFeedback);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.fao.geonet.api.userfeedback.service.IUserFeedbackService#
     * retrieveMetadataRatings(java.lang.String, boolean)
     */
    @Override
    public List<Rating> retrieveMetadataRatings(String metadataUuid, boolean published) {
        final RatingRepository ratingRepository = ApplicationContextHolder.get().getBean(RatingRepository.class);

        return ratingRepository.findByMetadata_Uuid(metadataUuid);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.fao.geonet.api.userfeedback.service.IUserFeedbackService#
     * retrieveUserFeedback(int, boolean)
     */
    @Override
    public List<UserFeedback> retrieveUserFeedback(int maxSize, boolean published) {
        final UserFeedbackRepository userFeedbackRepository = ApplicationContextHolder.get()
                .getBean(UserFeedbackRepository.class);

        List<UserFeedback> result = null;

        if (published) {

            Pageable pageSize = null;

            if (maxSize > 0) {
                pageSize = new PageRequest(0, maxSize);
            }

            result = userFeedbackRepository.findByStatusOrderByDateDesc(UserRatingStatus.PUBLISHED, pageSize);
        } else {

            Pageable pageSize = null;

            if (maxSize > 0) {
                pageSize = new PageRequest(0, maxSize);
            }

            result = userFeedbackRepository.findByOrderByDateDesc(pageSize);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.fao.geonet.api.userfeedback.service.IUserFeedbackService#
     * retrieveUserFeedback(java.lang.String, boolean)
     */
    @Override
    public UserFeedback retrieveUserFeedback(String feedbackUuid, boolean published) {
        final UserFeedbackRepository userFeedbackRepository = ApplicationContextHolder.get()
                .getBean(UserFeedbackRepository.class);

        if (published) {
            return userFeedbackRepository.findByUuidAndStatus(feedbackUuid, UserRatingStatus.PUBLISHED);
        } else {
            return userFeedbackRepository.findByUuid(feedbackUuid);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.fao.geonet.api.userfeedback.service.IUserFeedbackService#
     * retrieveUserFeedbackForMetadata(java.lang.String, int, boolean)
     */
    @Override
    public List<UserFeedback> retrieveUserFeedbackForMetadata(String metadataUuid, int maxSize, boolean published) {

        final UserFeedbackRepository userFeedbackRepository = ApplicationContextHolder.get()
                .getBean(UserFeedbackRepository.class);

        List<UserFeedback> result = null;

        if (published) {
            Pageable pageSize = null;

            if (maxSize > 0) {
                pageSize = new PageRequest(0, maxSize);
            }
            result = userFeedbackRepository.findByMetadata_UuidAndStatusOrderByDateDesc(metadataUuid,
                    UserRatingStatus.PUBLISHED, pageSize);
        } else {
            Pageable pageSize = null;

            if (maxSize > 0) {
                pageSize = new PageRequest(0, maxSize);
            }
            result = userFeedbackRepository.findByMetadata_UuidOrderByDateDesc(metadataUuid, pageSize);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.fao.geonet.api.userfeedback.service.IUserFeedbackService#
     * saveUserFeedback(org.fao.geonet.domain.userfeedback.UserFeedback)
     */
    @Override
    public void saveUserFeedback(UserFeedback userFeedback) {
        final UserFeedbackRepository userFeedbackRepository = ApplicationContextHolder.get()
                .getBean(UserFeedbackRepository.class);

        final MetadataRepository metadataRepository = ApplicationContextHolder.get().getBean(MetadataRepository.class);

        final UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);

        if (userFeedback.getAuthorId() != null) {
            final User author = userRepository.findOneByUsername(userFeedback.getAuthorId().getUsername());
            userFeedback.setAuthorId(author);
        }
        if (userFeedback.getApprover() != null) {
            final User approver = userRepository.findOneByUsername(userFeedback.getApprover().getUsername());
            userFeedback.setApprover(approver);
        }
        final Metadata metadata = metadataRepository.findOneByUuid(userFeedback.getMetadata().getUuid());
        userFeedback.setMetadata(metadata);

        if (userFeedback.getDate() == null) {
            userFeedback.setDate(new Date(System.currentTimeMillis()));
        }

        userFeedbackRepository.save(userFeedback);
    }
}
