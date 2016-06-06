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

package org.fao.geonet.repository.statistic;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo_;
import org.fao.geonet.domain.Metadata_;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

/**
 * An interface that creates the selection object that creates the calculation for {@link
 * MetadataStatisticsQueries} methods.
 * <p/>
 * Some statndard implementations are in {@link MetadataStatisticSpec.StandardSpecs}
 * <p/>
 * User: Jesse Date: 9/27/13 Time: 1:22 PM
 *
 * @see MetadataStatisticsQueries
 * @see MetadataStatisticSpec.StandardSpecs
 */
public interface MetadataStatisticSpec {

    /**
     * Get the selection object (the calculation).
     *
     * @param criteriaBuilder the criteria builder
     * @param root            the query metadata root
     * @return the selection object (the calculation).
     */
    Expression<? extends Number> getSelection(CriteriaBuilder criteriaBuilder, Root<Metadata> root);

    public final class StandardSpecs {
        private StandardSpecs() {
            // this should not be instantiated.  Just static methods
        }

        public static MetadataStatisticSpec metadataCount() {
            return new MetadataStatisticSpec() {
                @Override
                public Expression<Long> getSelection(CriteriaBuilder criteriaBuilder, Root<Metadata> root) {
                    return criteriaBuilder.count(root);
                }
            };
        }

        public static MetadataStatisticSpec popularitySum() {
            return new MetadataStatisticSpec() {
                @Override
                public Expression<Integer> getSelection(CriteriaBuilder criteriaBuilder, Root<Metadata> root) {
                    return criteriaBuilder.sum(root.get(Metadata_.dataInfo).get(MetadataDataInfo_.popularity));
                }
            };
        }

        public static MetadataStatisticSpec ratingSum() {
            return new MetadataStatisticSpec() {
                @Override
                public Expression<Integer> getSelection(CriteriaBuilder criteriaBuilder, Root<Metadata> root) {
                    return criteriaBuilder.sum(root.get(Metadata_.dataInfo).get(MetadataDataInfo_.rating));
                }
            };
        }
    }
}
