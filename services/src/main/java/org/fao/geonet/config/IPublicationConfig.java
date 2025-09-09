/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
package org.fao.geonet.config;

import jeeves.server.context.ServiceContext;

import java.util.List;
import java.util.Optional;

public interface IPublicationConfig {
    List<String> getPublicationOptionNames();

    List<PublicationOption> getPublicationOptions();

    Optional<PublicationOption> getPublicationOptionConfiguration(String configName);

    /**
     * Method interface that can be implemented by a custom publication configuration class to do additional processing
     * of the metadata published / unpublished.
     *
     * For example, assigning / removing a certain metadata category when the metadata is published / unpublished.
     *
     * @param serviceContext            The service context.
     * @param publicationOption         Publication option selected.
     * @param metadataId                Metadata internal identifier to process.
     * @param publish                   Boolean to indicate publish/un-publish.
     * @throws Exception
     */
    default void processMetadata(ServiceContext serviceContext, PublicationOption publicationOption,
                                 Integer metadataId, boolean publish) throws Exception {
        // Empty implementation
    }
}
