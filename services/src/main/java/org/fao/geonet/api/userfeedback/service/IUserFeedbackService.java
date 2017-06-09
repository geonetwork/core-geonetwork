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
     * @param user
     */
    void publishUserFeedback(String feedbackUuid, User user);

    /**
     * Deletes a user feedback.
     *
     * @param feedbackUuid
     */
    void removeUserFeedback(String feedbackUuid);

    /**
     * Retrieves the ratings associated to metadata.
     *
     * @param feedbackUuid
     * @return
     */
    public List<Rating> retrieveMetadataRatings(String metadataUuid);

    /**
     * Retrieves a user feedback by identifier.
     *
     * @param feedbackUuid
     * @return
     */
    UserFeedback retrieveUserFeedback(String feedbackUuid);

    /**
     * Retrieves the list of user feedback for a metadata.
     *
     * @param metadataUuid
     * @return
     */
    List<UserFeedback> retrieveUserFeedbackForMetadata(String metadataUuid, int maxSize);

    /**
     * Saves a user feedback.
     *
     * @param userFeedback
     */
    void saveUserFeedback(UserFeedback userFeedback);

}
