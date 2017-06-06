package org.fao.geonet.api.userfeedback.service;

import java.util.ArrayList;
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

    @Override
    public List<UserFeedback> retrieveUserFeedbackForMetadata(String metadataUuid) {
        init();

        UserFeedbackRepository userFeedbackRepository =
            ApplicationContextHolder.get().getBean(UserFeedbackRepository.class);

        return userFeedbackRepository.findByMetadata_UuidOrderByDateDesc(metadataUuid);
    }

    @Override
    public UserFeedback retrieveUserFeedback(String feedbackUuid) {
        UserFeedbackRepository userFeedbackRepository =
            ApplicationContextHolder.get().getBean(UserFeedbackRepository.class);

        return userFeedbackRepository.findByUuid(feedbackUuid);
    }

    @Override
    public List<Rating> retrieveMetadataRatings(String metadataUuid) {
        RatingRepository ratingRepository =
            ApplicationContextHolder.get().getBean(RatingRepository.class);

        return ratingRepository.findByMetadata_Uuid(metadataUuid);
    }

    @Override
    public void publishUserFeedback(String feedbackUuid, User user) {
        UserFeedbackRepository userFeedbackRepository =
            ApplicationContextHolder.get().getBean(UserFeedbackRepository.class);

        // TODO: Manage UserFeedback not found
        UserFeedback userFeedback = userFeedbackRepository.findByUuid(feedbackUuid);
        userFeedback.setApprover(user);

        userFeedbackRepository.save(userFeedback);
    }

    @Override
    public void saveUserFeedback(UserFeedback userFeedback) {
        UserFeedbackRepository userFeedbackRepository =
            ApplicationContextHolder.get().getBean(UserFeedbackRepository.class);
        
        MetadataRepository metadataRepository =
                ApplicationContextHolder.get().getBean(MetadataRepository.class);
        
        UserRepository userRepository =
                ApplicationContextHolder.get().getBean(UserRepository.class);
        
        User author = userRepository.findOneByUsername(userFeedback.getAuthor().getUsername());
        userFeedback.setAuthor(author);
        User approver = userRepository.findOneByUsername(userFeedback.getApprover().getUsername());
        userFeedback.setApprover(approver);
        Metadata metadata = metadataRepository.findOneByUuid(userFeedback.getMetadata().getUuid());
        userFeedback.setMetadata(metadata);

        userFeedbackRepository.save(userFeedback);
    }

    @Override
    public void removeUserFeedback(String feedbackUuid) {
        UserFeedbackRepository userFeedbackRepository =
            ApplicationContextHolder.get().getBean(UserFeedbackRepository.class);

        userFeedbackRepository.delete(UUID.fromString(feedbackUuid));
    }

    // TODO: REMOVE Mockup data
    public void init() {
        UserFeedbackRepository userFeedbackRepository =
            ApplicationContextHolder.get().getBean(UserFeedbackRepository.class);

        UserRepository userRepository =
            ApplicationContextHolder.get().getBean(UserRepository.class);

        MetadataRepository metadataRepository =
            ApplicationContextHolder.get().getBean(MetadataRepository.class);
        
        
        // Init some data for test (just the first time)
        if(userFeedbackRepository.findAll() == null || userFeedbackRepository.findAll().size() == 0) {
            
            System.out.println("Populating DB...");

            User user = userRepository.findOneByUsername("admin");
            List<Metadata> metadatas = metadataRepository.findAll();

            Rating four = new Rating();
            four.setRating(4);
            four.setCategory(Rating.Category.AVG);
            List<Rating> ratings = new ArrayList();
            ratings.add(four);

            long currentTime = System.currentTimeMillis();


            for (Metadata metadata : metadatas) {


            UserFeedback uf1 = new UserFeedback();
            uf1.setAuthor(user);
            uf1.setMetadata(metadata);
            uf1.setComment("FIRST about " + metadata.getId() + " Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
                + " accumsan, quam ligula gravida lectus, ut condimentum velit sem "
                + "a risus.");
            uf1.setDate(new Date(currentTime - 86400000)); // yesterday
            uf1.setDetailedRatingList(ratings);
            uf1.setStatus(UserFeedback.UserRatingStatus.PUBLISHED);


            UserFeedback uf2 = new UserFeedback();
            uf2.setAuthor(user);
            uf2.setMetadata(metadata);
            uf2.setComment("SECOND Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
                + " accumsan, quam ligula gravida lectus, ut condimentum velit sem "
                + "a risus.");
            uf2.setDate(new Date(currentTime));
            uf2.setDetailedRatingList(ratings);
            uf2.setStatus(UserFeedback.UserRatingStatus.PUBLISHED);

            UserFeedback uf3 = new UserFeedback();
            uf3.setAuthor(user);
            uf3.setMetadata(metadata);
            uf3.setComment("I AM NOT APPROVED! Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
                + " accumsan, quam ligula gravida lectus, ut condimentum velit sem "
                + "a risus.");
            uf3.setDate(new Date(currentTime + 86400000)); // tomorrow
            uf3.setDetailedRatingList(ratings);
            uf3.setStatus(UserFeedback.UserRatingStatus.WAITING_FOR_APPROVAL);

            userFeedbackRepository.save(uf1);
            userFeedbackRepository.save(uf2);
            userFeedbackRepository.save(uf3);

            }
        }


    }


    // ************************
}
