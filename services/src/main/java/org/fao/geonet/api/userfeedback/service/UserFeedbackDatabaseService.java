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
import java.util.UUID;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.userfeedback.Rating;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.userfeedback.RatingRepository;
import org.fao.geonet.repository.userfeedback.UserFeedbackRepository;
import org.springframework.stereotype.Service;

/**
 * User feedback provider that uses database persistence.
 *
 * @author Jose García
 */
@Service
public class UserFeedbackDatabaseService implements IUserFeedbackService {

    // TODO: REMOVE Mockup data
    public void init() {

        try {

            ApplicationContextHolder.get().getBean(UserFeedbackRepository.class);

            ApplicationContextHolder.get().getBean(UserRepository.class);

            ApplicationContextHolder.get().getBean(MetadataRepository.class);

            // userFeedbackRepository.deleteAllInBatch();

            // Init some data for test (just the first time)
            // if(userFeedbackRepository.findAll() == null ||
            // userFeedbackRepository.findAll().size() == 0) {
            //
            // System.out.println("Populating DB...");
            //
            // User user = userRepository.findOneByUsername("admin");
            // List<Metadata> metadatas = metadataRepository.findAll();
            //
            // Rating four = new Rating();
            // four.setRating(4);
            // four.setCategory(Rating.Category.AVG);
            // List<Rating> ratings = new ArrayList();
            // ratings.add(four);
            //
            // long currentTime = System.currentTimeMillis();
            //
            //
            // for (Metadata metadata : metadatas) {
            //
            //
            // UserFeedback uf1 = new UserFeedback();
            // uf1.setAuthorId(user);
            // uf1.setAuthorPrivacy(0);
            // uf1.setMetadata(metadata);
            // uf1.setComment("FIRST about " + metadata.getId() + " Lorem ipsum
            // dolor sit amet, consectetur adipiscing elit."
            // + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
            // + " accumsan, quam ligula gravida lectus, ut condimentum velit
            // sem "
            // + "a risus.");
            // uf1.setDate(new Date(currentTime - 86400000)); // yesterday
            // uf1.setDetailedRatingList(ratings);
            // uf1.setStatus(UserFeedback.UserRatingStatus.PUBLISHED);
            //
            //
            // UserFeedback uf2 = new UserFeedback();
            // uf2.setAuthorName("Pascal Like");
            // uf2.setAuthorOrganization("Geocat");
            // uf2.setAuthorPrivacy(0);
            // uf2.setMetadata(metadata);
            // uf2.setComment("SECOND Lorem ipsum dolor sit amet, consectetur
            // adipiscing elit."
            // + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
            // + " accumsan, quam ligula gravida lectus, ut condimentum velit
            // sem "
            // + "a risus.");
            // uf2.setDate(new Date(currentTime));
            // uf2.setDetailedRatingList(ratings);
            // uf2.setStatus(UserFeedback.UserRatingStatus.PUBLISHED);
            //
            // UserFeedback uf3 = new UserFeedback();
            // uf3.setAuthorId(user);
            // uf3.setAuthorPrivacy(1);
            // uf3.setMetadata(metadata);
            // uf3.setComment("I AM NOT APPROVED! Lorem ipsum dolor sit amet,
            // consectetur adipiscing elit."
            // + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
            // + " accumsan, quam ligula gravida lectus, ut condimentum velit
            // sem "
            // + "a risus.");
            // uf3.setDate(new Date(currentTime + 86400000)); // tomorrow
            // uf3.setDetailedRatingList(ratings);
            // uf3.setStatus(UserFeedback.UserRatingStatus.WAITING_FOR_APPROVAL);
            //
            // userFeedbackRepository.save(uf1);
            // userFeedbackRepository.save(uf2);
            // userFeedbackRepository.save(uf3);
            //
            // }
            // }

        } catch (final Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void publishUserFeedback(String feedbackUuid, User user) {
        final UserFeedbackRepository userFeedbackRepository = ApplicationContextHolder.get()
                .getBean(UserFeedbackRepository.class);

        // TODO: Manage UserFeedback not found
        final UserFeedback userFeedback = userFeedbackRepository.findByUuid(feedbackUuid);
        userFeedback.setApprover(user);

        userFeedbackRepository.save(userFeedback);
    }

    @Override
    public void removeUserFeedback(String feedbackUuid) {
        final UserFeedbackRepository userFeedbackRepository = ApplicationContextHolder.get()
                .getBean(UserFeedbackRepository.class);

        userFeedbackRepository.delete(UUID.fromString(feedbackUuid));
    }

    @Override
    public List<Rating> retrieveMetadataRatings(String metadataUuid) {
        final RatingRepository ratingRepository = ApplicationContextHolder.get().getBean(RatingRepository.class);

        return ratingRepository.findByMetadata_Uuid(metadataUuid);
    }

    @Override
    public UserFeedback retrieveUserFeedback(String feedbackUuid) {
        final UserFeedbackRepository userFeedbackRepository = ApplicationContextHolder.get()
                .getBean(UserFeedbackRepository.class);

        return userFeedbackRepository.findByUuid(feedbackUuid);
    }

    @Override
    public List<UserFeedback> retrieveUserFeedbackForMetadata(String metadataUuid, int maxSize) {
        init();

        final UserFeedbackRepository userFeedbackRepository = ApplicationContextHolder.get()
                .getBean(UserFeedbackRepository.class);

        // IMPROVE: optimize with a limit on the query
        final List<UserFeedback> result = userFeedbackRepository.findByMetadata_UuidOrderByDateDesc(metadataUuid);

        if (maxSize > 0 && result != null && maxSize < result.size()) {
            return result.subList(0, maxSize);
        }

        return result;
    }

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

    // ************************
}
