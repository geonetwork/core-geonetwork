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
package org.fao.geonet.repository.userfeedback;

import java.util.List;
import java.util.UUID;

import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.domain.userfeedback.UserFeedback.UserRatingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Interface UserFeedbackRepository.
 */
public interface UserFeedbackRepository extends JpaRepository<UserFeedback, UUID> {

    /**
     * Find by author id order by date desc.
     *
     * @param authorId the author id
     * @param p the p
     * @return the list
     */
    List<UserFeedback> findByAuthorIdOrderByCreationDateDesc(int authorId, Pageable p);

    /**
     * Find by metadata uuid.
     *
     * @param metadataUuid the metadata uuid
     * @return the list
     */
    List<UserFeedback> findByMetadata_Uuid(String metadataUuid);

    /**
     * Find by metadata uuid and status order by date desc.
     *
     * @param metadataUuid the metadata uuid
     * @param status the status
     * @param p the p
     * @return the list
     */
    List<UserFeedback> findByMetadata_UuidAndStatusOrderByCreationDateDesc(String metadataUuid, UserRatingStatus status, Pageable p);

    /**
     * Find by metadata uuid order by date desc.
     *
     * @param metadataUuid the metadata uuid
     * @param p the p
     * @return the list
     */
    List<UserFeedback> findByMetadata_UuidOrderByCreationDateDesc(String metadataUuid, Pageable p);

    /**
     * Find by order by date desc.
     *
     * @param p the p
     * @return the list
     */
    @Query("SELECT uf from GUF_UserFeedback uf order by uf.creationDate DESC ")
    List<UserFeedback> findByOrderByCreationDateDesc(Pageable p);


    /**
     * Find by status order by date desc.
     *
     * @param status the status
     * @param p the p
     * @return the list
     */
    List<UserFeedback> findByStatusOrderByCreationDateDesc(UserFeedback.UserRatingStatus status, Pageable p);

    /**
     * Find by uuid.
     *
     * @param uuid the uuid
     * @return the user feedback
     */
    UserFeedback findByUuid(String uuid);

    /**
     * Find by uuid and status.
     *
     * @param uuid the uuid
     * @param status the status
     * @return the user feedback
     */
    UserFeedback findByUuidAndStatus(String uuid, UserRatingStatus status);
}
