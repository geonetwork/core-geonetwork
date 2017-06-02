package org.fao.geonet.repository.userfeedback;

import java.util.List;

import org.fao.geonet.domain.userfeedback.Rating;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<UserFeedback, Long>  {
    
    List<Rating> findByMetadata_Uuid(String metadataUuid);

}
