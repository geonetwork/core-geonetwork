/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet.domain;

import java.util.Arrays;
import java.util.List;

import org.fao.geonet.auditable.UsernameAuditorAware;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Used on {@link SchematronCriteria}
 *
 * @author delawen
 */
public enum SchematronCriteriaType {
    /**
     * A criteria where the value must match one of the group ids in the value.
     * Multiple ids can be comma separated.
     */
    GROUP(new SchematronCriteriaEvaluator() {
        @Override
        public boolean accepts(ApplicationContext applicationContext, String value, int metadataId, Element metadata,
                               List<Namespace> metadataNamespaces) {


            String[] values = value.split(",");
            Integer[] ids = new Integer[values.length];
            for (int i = 0; i < values.length; i++) {
                ids[i] = Integer.valueOf(values[i]);
            }
            final Specification<MetadataDraft> correctOwnerDraft = (Specification<MetadataDraft>)MetadataSpecs.isOwnedByOneOfFollowingGroups(Arrays.asList(ids));
            final Specification<MetadataDraft> correctIdDraft = (Specification<MetadataDraft>)MetadataSpecs.hasMetadataId(metadataId);
            final Specification<MetadataDraft> finalSpecDraft = Specification.where(correctIdDraft).and(correctOwnerDraft);
            final Specification<Metadata> correctOwner = (Specification<Metadata>)MetadataSpecs.isOwnedByOneOfFollowingGroups(Arrays.asList(ids));
            final Specification<Metadata> correctId = (Specification<Metadata>)MetadataSpecs.hasMetadataId(metadataId);
            final Specification<Metadata> finalSpec = Specification.where(correctId).and(correctOwner);

            return applicationContext.getBean(MetadataRepository.class).count(finalSpec)
                + applicationContext.getBean(MetadataDraftRepository.class).count(finalSpecDraft) > 0;
        }

        @Override
        public boolean accepts(ApplicationContext applicationContext, String value, Element metadata,
                               List<Namespace> metadataNamespaces, Integer metadataGroupOwner) {

            String[] values = value.split(",");
            Integer[] ids = new Integer[values.length];
            for (int i = 0; i < values.length; i++) {
                ids[i] = Integer.valueOf(values[i]);
            }

            // used to evaluate an external metadata to import, metadataGroupOwner should be provided
            // with the destination metadata group owner
            return Arrays.asList(ids).contains(metadataGroupOwner);
        }
    }),

    /**
     * A criteria where the user highest profile value must match one of the profile ids values.
     * Multiple ids can be comma separated.
     */
    USER_MAIN_PROFILE(new SchematronCriteriaEvaluator() {
        @Override
        public boolean accepts(ApplicationContext applicationContext, String value, int metadataId, Element metadata,
                               List<Namespace> metadataNamespaces) {


            String[] values = value.split(",");
            List<String> profiles = Arrays.asList(values);

            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String username = UsernameAuditorAware.getUsername(authentication);

                UserRepository userRepo = applicationContext.getBean(UserRepository.class);
                User user = userRepo.findOneByUsername(username);
                if (profiles.contains(user.getProfile().name())) {
                    return true;
                }
            }
            return false;
        }
        @Override
        public boolean accepts(ApplicationContext applicationContext, String value, Element metadata,
                               List<Namespace> metadataNamespaces, Integer metadataGroupOwner) {

            // Use dummy value for metadataId, not used in the accepts method
           return accepts(applicationContext, value, -1, metadata, metadataNamespaces);
        }
    }),

    /**
     * An always true criteria.
     */
    ALWAYS_ACCEPT(new SchematronCriteriaEvaluator() {
        @Override
        public boolean accepts(ApplicationContext applicationContext, String value, int metadataId, Element metadata,
                               List<Namespace> metadataNamespaces) {
            return true;
        }

        @Override
        public boolean accepts(ApplicationContext applicationContext, String value, Element metadata,
                               List<Namespace> metadataNamespaces, Integer metadataGroupOwner) {
            return true;
        }
    }),
    /**
     * A criteria where the value must match a one of the ISO19139 keywords in the metadata.
     */
    XPATH(XPathCriteriaEvaluator.INSTANCE);

    private final SchematronCriteriaEvaluator evaluator;

    SchematronCriteriaType(SchematronCriteriaEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public boolean accepts(ApplicationContext applicationContext, String value, int metadataId, Element metadata,
                           List<Namespace> metadataNamespaces) {
        return evaluator.accepts(applicationContext, value, metadataId, metadata, metadataNamespaces);
    }

    public boolean accepts(ApplicationContext applicationContext, String value, Element metadata,
                           List<Namespace> metadataNamespaces, Integer groupOwnerId ) {
        return evaluator.accepts(applicationContext, value, metadata, metadataNamespaces, groupOwnerId);
    }
}
