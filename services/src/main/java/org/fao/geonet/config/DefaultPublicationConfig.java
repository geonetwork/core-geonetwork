/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Bean with the default publication configuration.
 *
 * The default publication options publishes the metadata to the ALL group and the INTRANET group.
 */
public class DefaultPublicationConfig implements IPublicationConfig {
    protected List<PublicationOption> publicationOptions = new ArrayList<>();

    @PostConstruct
    public void init() {
        PublicationOption defaultPublication = new PublicationOption("default",
            ReservedGroup.all,
            Arrays.asList(new ReservedOperation[]{
                ReservedOperation.view,
                ReservedOperation.download,
                ReservedOperation.dynamic}));

        defaultPublication.addAdditionalPublicationInfo(ReservedGroup.intranet,
            Arrays.asList(new ReservedOperation[]{
                ReservedOperation.view,
                ReservedOperation.download,
                ReservedOperation.dynamic}));

        publicationOptions.add(defaultPublication);

    }

    @Override
    public List<String> getPublicationOptionNames() {
        return publicationOptions.stream().map(p -> p.getName()).collect(Collectors.toList());
    }

    @Override
    public List<PublicationOption> getPublicationOptions() {
        return publicationOptions;
    }

    @Override
    public Optional<PublicationOption> getPublicationOptionConfiguration(String configName) {
        return publicationOptions.stream().filter(p -> p.getName().equals(configName)).findFirst();
    }
}
