package org.fao.geonet.repository;

import com.google.common.base.Optional;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.criteria.Order;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Custom (Non spring-data) Query methods for {@link User} entities.
 * 
 * @author Jesse
 */
public interface UserRepositoryCustom {
    /**
     * Find the use with the given userid (where userid is a string).  The string will be converted to an integer for making the query.
     * 
     * @param userId the userid.
     * 
     * @return the use with the given userid 
     */
    @Nonnull User findOne(@Nonnull String userId);
    
    /**
     * Find all users with the given email address.
     *
     * @param email the email address to use in search query.
     * @return
     */
    @Nonnull List<User> findAllByEmail(@Nonnull String email);

    /**
     * Find all the users that are part of the ownerGroup of a particular set of metadata.
     *
     * @param metadataIds the metadataIds of the metadata to inspect.
     * @param profile if non-null then filter the users by the given profile.
     * @param sort if non-null then Sort the results by the <em>User</em> property.  The sort object must contain user properties only.
     *
     * @return all the users that are part of the ownerGroup of a particular set of metadata.
     */
    @Nonnull List<Pair<Integer,User>> findAllByGroupOwnerNameAndProfile(@Nonnull Collection<Integer> metadataIds,
                                                                        @Nullable Profile profile, @Nullable Sort sort);
}
