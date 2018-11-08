/**
 * SchematronCriteriaType.java
 *
 * Copyright (C) 2013
 *
 * This file is part of project geonetwork
 *
 * This software is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this library; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301  USA
 *
 * As a special exception, if you link this library with other files to produce an executable, this
 * library does not by itself cause the resulting executable to be covered by the GNU General Public
 * License. This exception does not however invalidate any other reasons why the executable file
 * might be covered by the GNU General Public License.
 *
 * @author delawen
 */
package org.fao.geonet.domain;

import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import java.util.Arrays;
import java.util.List;

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
            final Specification<Metadata> correctOwner = (Specification<Metadata>)MetadataSpecs.isOwnedByOneOfFollowingGroups(Arrays.asList(ids));
            final Specification<Metadata> correctId = (Specification<Metadata>)MetadataSpecs.hasMetadataId(metadataId);
            final Specifications<Metadata> finalSpec = Specifications.where(correctId).and(correctOwner);
            return applicationContext.getBean(MetadataRepository.class).count(finalSpec) > 0;
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
}
