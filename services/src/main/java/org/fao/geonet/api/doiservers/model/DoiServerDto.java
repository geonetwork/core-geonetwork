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

package org.fao.geonet.api.doiservers.model;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.DoiServer;
import org.fao.geonet.domain.Group;
import org.fao.geonet.repository.GroupRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DoiServerDto {
    private int id;
    private String name;
    private String description;
    private String url;
    private String username;
    private String password;
    private String landingPageTemplate;
    private String publicUrl;
    private String pattern = "{{uuid}}";
    private String prefix;
    private Set<Integer> publicationGroups = new HashSet<>();


    public int getId() {
        return id;
    }

    public DoiServerDto setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public DoiServerDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DoiServerDto setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public DoiServerDto setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public DoiServerDto setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public DoiServerDto setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getLandingPageTemplate() {
        return landingPageTemplate;
    }

    public DoiServerDto setLandingPageTemplate(String landingPageTemplate) {
        this.landingPageTemplate = landingPageTemplate;
        return this;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public DoiServerDto setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
        return this;
    }

    public String getPattern() {
        return pattern;
    }

    public DoiServerDto setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public DoiServerDto setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public Set<Integer> getPublicationGroups() {
        return publicationGroups;
    }

    public DoiServerDto setPublicationGroups(Set<Integer> publicationGroups) {
        this.publicationGroups = publicationGroups;
        return this;
    }

    public static DoiServerDto from(DoiServer doiServer) {
        DoiServerDto doiServerDto = new DoiServerDto();

        doiServerDto.setId(doiServer.getId());
        doiServerDto.setName(doiServer.getName());
        doiServerDto.setDescription(doiServer.getDescription());
        doiServerDto.setUrl(doiServer.getUrl());
        doiServerDto.setUsername(doiServer.getUsername());
        doiServerDto.setPassword(doiServer.getPassword());
        doiServerDto.setPattern(doiServer.getPattern());
        doiServerDto.setLandingPageTemplate(doiServer.getLandingPageTemplate());
        doiServerDto.setPublicUrl(doiServer.getPublicUrl());
        doiServerDto.setPrefix(doiServer.getPrefix());
        doiServerDto.setPublicationGroups(doiServer.getPublicationGroups().stream().map(Group::getId).collect(Collectors.toSet()));

        return doiServerDto;
    }

    public DoiServer asDoiServer() {
        DoiServer doiServer = new DoiServer();

        doiServer.setId(getId());
        doiServer.setName(getName());
        doiServer.setDescription(getDescription());
        doiServer.setUrl(getUrl());
        doiServer.setUsername(getUsername());
        doiServer.setPassword(getPassword());
        doiServer.setPattern(getPattern());
        doiServer.setLandingPageTemplate(getLandingPageTemplate());
        doiServer.setPublicUrl(getPublicUrl());
        doiServer.setPrefix(getPrefix());

        GroupRepository groupRepository = ApplicationContextHolder.get().getBean(GroupRepository.class);
        Set<Group> groups = new HashSet<>();
        getPublicationGroups().forEach(groupId -> {
            if (groupId != null) {
                Optional<Group> g = groupRepository.findById(groupId);

                if (g.isPresent()) {
                    groups.add(g.get());
                }
            }
        });
        doiServer.setPublicationGroups(groups);

        return doiServer;
    }
}
