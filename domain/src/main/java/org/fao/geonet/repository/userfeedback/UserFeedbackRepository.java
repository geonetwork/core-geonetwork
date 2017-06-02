package org.fao.geonet.repository.userfeedback;

import java.util.List;
import java.util.UUID;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFeedbackRepository extends JpaRepository<UserFeedback, UUID> {

    UserFeedback findByUuid(String uuid);

    List<UserFeedback> findByAuthorOrderByDateDesc(User author);
    
    List<UserFeedback> findByMetadata_UuidOrderByDateDesc(String metadataUuid);
    
    List<UserFeedback> findByStatusOrderByDateDesc(UserFeedback.UserRatingStatus status);
}
