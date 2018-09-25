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

import java.util.List;

import org.apache.jcs.access.exception.ObjectNotFoundException;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.userfeedback.Rating;
import org.fao.geonet.domain.userfeedback.UserFeedback;

/**
 * Interface to implement by feedback service providers.
 *
 * @author Jose García
 */
public interface IUserFeedbackService {

    /**
     * Publishes a user feedback.
     *
     * @param feedbackUuid
     *          the feedback uuid
     * @param user
     *          the user
     */
    void publishUserFeedback(String feedbackUuid, User user) throws ObjectNotFoundException;

    /**
     * Deletes a user feedback.
     *
     * @param feedbackUuid
     *          the feedback uuid
     * @param ip
     * 			the ip of the user that did this
     */
    void removeUserFeedback(String feedbackUuid, String ip) throws Exception;

    /**
     * Retrieves the ratings associated to metadata.
     *
     * @param metadataUuid
     *          the metadata uuid
     * @param published
     *          the published
     * @return the list
     */
    public List<Rating> retrieveMetadataRatings(String metadataUuid, boolean published);

    /**
     * Retrieves the list of user feedback for a metadata.
     *
     * @param maxSize
     *          the max size
     * @param published
     *          the published
     * @return the list
     */
    List<UserFeedback> retrieveUserFeedback(int maxSize, boolean published);

    /**
     * Retrieves a user feedback by identifier.
     *
     * @param feedbackUuid
     *          the feedback uuid
     * @param published
     *          the published
     * @return the user feedback
     */
    UserFeedback retrieveUserFeedback(String feedbackUuid, boolean published);

    /**
     * Retrieves the list of user feedback for a metadata.
     *
     * @param metadataUuid
     *          the metadata uuid
     * @param maxSize
     *          the max size
     * @param published
     *          the published
     * @return the list
     */
    List<UserFeedback> retrieveUserFeedbackForMetadata(String metadataUuid, int maxSize, boolean published);

    /**
     * Saves a user feedback.
     *
     * @param userFeedback
     *          the user feedback
     */
    void saveUserFeedback(UserFeedback userFeedback, String ip) throws Exception;

}
