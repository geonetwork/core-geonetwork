package org.fao.geonet.api.userfeedback.service;

import org.fao.geonet.domain.User;
import org.fao.geonet.domain.userfeedback.Rating;
import org.fao.geonet.domain.userfeedback.UserFeedback;

import java.util.List;

/**
 * Interface to implement by feedback service providers.
 *
 * @author Jose García
 */
public interface IUserFeedbackService {

    /**
     * Retrieves the list of user feedback for a metadata.
     *
     * @param metadataUuid
     * @return
     */
    List<UserFeedback> retrieveUserFeedbackForMetadata(String metadataUuid);

    /**
     * Retrieves a user feedback by identifier.
     *
     * @param feedbackUuid
     * @return
     */
    UserFeedback retrieveUserFeedback(String feedbackUuid);

    /**
     * Retrieves the ratings associated to metadata.
     *
     * @param feedbackUuid
     * @return
     */
    public List<Rating> retrieveMetadataRatings(String metadataUuid);
   

    /**
     * Saves a user feedback.
     *
     * @param userFeedback
     */
    void saveUserFeedback(UserFeedback userFeedback);

    /**
     * Publishes a user feedback.
     *
     * @param feedbackUuid
     * @param user
     */
    void publishUserFeedback(String feedbackUuid, User user);

    /**
     * Deletes a user feedback.
     *
     * @param feedbackUuid
     */
    void removeUserFeedback(String feedbackUuid);

}
