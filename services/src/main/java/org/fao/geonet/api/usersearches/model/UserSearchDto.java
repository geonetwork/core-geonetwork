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

package org.fao.geonet.api.usersearches.model;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserSearch;
import org.fao.geonet.domain.UserSearchFeaturedType;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserRepository;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * DTO class for user custom search information {@link org.fao.geonet.domain.UserSearch}.
 *
 */
public class UserSearchDto implements Serializable {

    private static final long serialVersionUID = -2111281874868436021L;

    public static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    private int id;
    private String url;
    private String featuredType = "";
    private String creationDate;
    private int creatorId;
    private String creator;
    private String logo;
    private Map<String, String> names = new HashMap<>();
    private List<Integer> groups = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFeaturedType() {
        return featuredType;
    }

    public void setFeaturedType(String featuredType) {
        this.featuredType = featuredType;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }


    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(Map<String, String> names) {
        this.names = names;
    }

    public void addName(String lang, String name) {
        this.names.put(lang, name);
    }

    public List<Integer> getGroups() {
        return groups;
    }

    public void setGroups(List<Integer> groups) {
        this.groups = groups;
    }

    public void addGroup(Integer groupId) {
        this.groups.add(groupId);
    }


    public UserSearch asUserSearch() {
        UserSearch userSearch = new UserSearch();

        userSearch.setId(this.getId());
        userSearch.setUrl(this.getUrl());
        userSearch.setLogo(this.getLogo());

        try {
            if (StringUtils.isNotEmpty(this.getFeaturedType()) &&
                (this.getFeaturedType().length() == 1))
            userSearch.setFeaturedType(UserSearchFeaturedType.byChar(this.getFeaturedType().charAt(0)));
        } catch (IllegalArgumentException ex) {
            // Ignore
        }

        try {
            userSearch.setCreationDate(ISO_DATE_FORMAT.parse(this.getCreationDate()));
        } catch (Exception ex) {
            userSearch.setCreationDate(new Date());
        }

        UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);
        User user = userRepository.findOne(this.getCreatorId());
        if (user != null) {
            userSearch.setCreator(user);
        }

        this.getNames().forEach((key, value) -> userSearch.getLabelTranslations().put(key, value));

        GroupRepository groupRepository = ApplicationContextHolder.get().getBean(GroupRepository.class);
        Set<Group> groups = new HashSet<>();
        getGroups().forEach(groupId -> {
            if (groupId != null) {
                Group g = groupRepository.findOne(groupId);

                if (g != null) {
                    groups.add(g);
                }
            }
        });
        userSearch.setGroups(groups);

        return userSearch;
    }


    public static UserSearchDto from(UserSearch userSearch) {
        UserSearchDto dto = new UserSearchDto();

        dto.setId(userSearch.getId());
        dto.setUrl(userSearch.getUrl());
        dto.setLogo(userSearch.getLogo());
        if (userSearch.getFeaturedType() != null) {
            dto.setFeaturedType(userSearch.getFeaturedType().asString());
        }
        dto.setCreatorId(userSearch.getCreator().getId());
        dto.setCreator(userSearch.getCreator().getUsername());

        dto.setCreationDate(ISO_DATE_FORMAT.format(userSearch.getCreationDate()));

        userSearch.getLabelTranslations().forEach((key, value) -> dto.addName(key, value));

        userSearch.getGroups().forEach(group -> dto.addGroup(group.getId()));

        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSearchDto that = (UserSearchDto) o;
        return id == that.id &&
            featuredType == that.featuredType &&
            creatorId == that.creatorId &&
            url.equals(that.url) &&
            creationDate.equals(that.creationDate) &&
            creator.equals(that.creator) &&
            Objects.equals(logo, that.logo) &&
            names.equals(that.names) &&
            Objects.equals(groups, that.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, featuredType, creationDate, creatorId, creator, logo, names, groups);
    }
}
