package org.fao.geonet.api.userfeedback.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.userfeedback.Rating;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.userfeedback.RatingRepository;
import org.fao.geonet.repository.userfeedback.UserFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User feedback provider that uses database persistence.
 *
 * @author Jose García
 */
@Service
public class UserFeedbackDatabaseService implements IUserFeedbackService {
    
    @Autowired
    private UserFeedbackRepository userFeedbackRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MetadataRepository metadataRepository;
    
    
    @Override
    public List<UserFeedback> retrieveUserFeedbackForMetadata(String metadataUuid) {
        init();
        return userFeedbackRepository.findByMetadata_UuidOrderByDateDesc(metadataUuid);
    }

    @Override
    public UserFeedback retrieveUserFeedback(String feedbackUuid) {
        
        return userFeedbackRepository.findByUuid(feedbackUuid);
    }

    @Override
    public List<Rating> retrieveMetadataRatings(String metadataUuid) {
        
        return ratingRepository.findByMetadata_Uuid(metadataUuid);
    }

    @Override
    public void publishUserFeedback(String feedbackUuid, User user) {

    }

    @Override
    public void saveUserFeedback(UserFeedback userFeedback) {

    }

    @Override
    public void removeUserFeedback(String feedbackUuid) {

    }

    // TODO: REMOVE Mockup data
    public void init() {
        
               
        // Init some data for test (just the first time)
        if(userFeedbackRepository.findAll() == null || userFeedbackRepository.findAll().size() == 0) {
            
            System.out.println("Initialyze");
            
            User user = userRepository.findOneByUsername("admin");
            List<Metadata> metadatas = metadataRepository.findAll();
            
            Rating four = new Rating();
            four.setRating(4);
            
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
            uf1.setRating(four);
            uf1.setStatus(UserFeedback.UserRatingStatus.PUBLISHED);
            

            UserFeedback uf2 = new UserFeedback();
            uf2.setAuthor(user);
            uf2.setMetadata(metadata);
            uf2.setComment("SECOND Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
                + " accumsan, quam ligula gravida lectus, ut condimentum velit sem "
                + "a risus.");
            uf2.setDate(new Date(currentTime));
            uf2.setRating(four);
            uf2.setStatus(UserFeedback.UserRatingStatus.PUBLISHED);
            
            UserFeedback uf3 = new UserFeedback();
            uf3.setAuthor(user);
            uf3.setMetadata(metadata);
            uf3.setComment("I AM NOT APPROVED! Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                + " Etiam ultrices ligula urna. Ut cursus, mauris sed auctor"
                + " accumsan, quam ligula gravida lectus, ut condimentum velit sem "
                + "a risus.");
            uf3.setDate(new Date(currentTime + 86400000)); // tomorrow
            uf3.setRating(four);
            uf3.setStatus(UserFeedback.UserRatingStatus.WAITING_FOR_APPROVAL);
            
            userFeedbackRepository.save(uf1);
            userFeedbackRepository.save(uf2);
            userFeedbackRepository.save(uf3);
            
            }
        }
        
        
    }
    

    // ************************
}
